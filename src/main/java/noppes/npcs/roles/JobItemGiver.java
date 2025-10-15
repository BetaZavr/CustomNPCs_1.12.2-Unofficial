package noppes.npcs.roles;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.data.role.IJobItemGiver;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.controllers.GlobalDataController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerItemGiverData;
import noppes.npcs.entity.EntityNPCInterface;

public class JobItemGiver extends JobInterface implements IJobItemGiver {

	public Availability availability;
	public int cooldown;
	public int cooldownType; // 0:timer, 1:one, 2:rldaily
	public int givingMethod; // 0:rnd, 1:all, 2:owned, 3:doesn't own, 4:chained
	public NpcMiscInventory inventory;
	public int itemGiverId;
	public List<String> lines;
	private final List<EntityPlayer> recentlyChecked;
	private int ticks;
	private List<EntityPlayer> toCheck;

	public JobItemGiver(EntityNPCInterface npc) {
		super(npc);
		this.cooldownType = 0;
		this.givingMethod = 0;
		this.cooldown = 10;
		this.itemGiverId = 0;
		this.lines = new ArrayList<>();
		this.ticks = 10;
		this.recentlyChecked = new ArrayList<>();
		this.availability = new Availability();
		this.inventory = new NpcMiscInventory(9);
		this.lines.add("Have these items {player}");
		this.type = JobType.ITEM_GIVER;
	}

	@Override
	public boolean aiContinueExecute() {
		return false;
	}

	@Override
	public boolean aiShouldExecute() {
		if (this.npc.isAttacking()) {
			return false;
		}
		--this.ticks;
		if (this.ticks > 0) {
			return false;
		}
		this.ticks = 10;
		List<EntityPlayer> list = new ArrayList<>();
		try {
			list = npc.world.getEntitiesWithinAABB(EntityPlayer.class, npc.getEntityBoundingBox().grow(3.0, 3.0, 3.0));
		}
		catch (Exception ignored) { }
		(this.toCheck = list).removeAll(this.recentlyChecked);
		List<EntityPlayer> listMax = new ArrayList<>();
		try {
			listMax = this.npc.world.getEntitiesWithinAABB(EntityPlayer.class,
					this.npc.getEntityBoundingBox().grow(10.0, 10.0, 10.0));
		}
		catch (Exception ignored) { }
		this.recentlyChecked.retainAll(listMax);
		this.recentlyChecked.addAll(this.toCheck);
		return !this.toCheck.isEmpty();
	}

	@Override
	public void aiStartExecuting() {
		for (EntityPlayer player : this.toCheck) {
			if (this.npc.canSee(player) && this.availability.isAvailable(player)) {
				this.recentlyChecked.add(player);
				this.interact(player);
			}
		}
	}

	private boolean canPlayerInteract(PlayerItemGiverData data) {
		if (this.inventory.items.isEmpty()) {
			return false;
		}
		if (this.isOnTimer()) {
			return data.notInteractedBefore(this) || data.getTime(this) + this.cooldown * 1000L < System.currentTimeMillis();
		}
		if (this.isGiveOnce()) {
			return data.notInteractedBefore(this);
		}
		return this.isDaily() && (data.notInteractedBefore(this) || this.getDay() > data.getTime(this));
	}

    private int freeInventorySlots(EntityPlayer player) {
		int i = 0;
		for (ItemStack is : player.inventory.mainInventory) {
			if (NoppesUtilServer.IsItemStackNull(is)) {
				++i;
			}
		}
		return i;
	}

	private int getDay() {
		return (int) (this.npc.world.getTotalWorldTime() / 24000L);
	}

	private boolean giveItems(EntityPlayer player) {
		PlayerItemGiverData data = PlayerData.get(player).itemgiverData;
		if (!this.canPlayerInteract(data)) {
			return false;
		}
		Vector<ItemStack> items = new Vector<>();
		Vector<ItemStack> toGive = new Vector<>();
		for (ItemStack is : this.inventory.items) {
			if (!is.isEmpty()) {
				items.add(is.copy());
			}
		}
		if (items.isEmpty()) {
			return false;
		}
		if (this.isAllGiver()) {
			toGive = items;
		} else if (this.isRemainingGiver()) {
			for (ItemStack is : items) {
				if (!this.playerHasItem(player, is.getItem())) {
					toGive.add(is);
				}
			}
		} else if (this.isRandomGiver()) {
			toGive.add(items.get(this.npc.world.rand.nextInt(items.size())).copy());
		} else if (this.isGiverWhenNotOwnedAny()) {
			boolean ownsItems = false;
			for (ItemStack is2 : items) {
				if (this.playerHasItem(player, is2.getItem())) {
					ownsItems = true;
					break;
				}
			}
			if (ownsItems) {
				return false;
			}
			toGive = items;
		} else if (this.isChainedGiver()) {
			int itemIndex = data.getItemIndex(this);
			int i = 0;
			for (ItemStack item : this.inventory.items) {
				if (i == itemIndex) {
					toGive.add(item);
					break;
				}
				++i;
			}
		}
		if (toGive.isEmpty()) {
			return false;
		}
		if (this.givePlayerItems(player, toGive)) {
			if (!this.lines.isEmpty()) {
				this.npc.say(player, new Line(this.lines.get(this.npc.getRNG().nextInt(this.lines.size()))));
			}
			if (this.isDaily()) {
				data.setTime(this, this.getDay());
			} else {
				data.setTime(this, System.currentTimeMillis());
			}
			if (this.isChainedGiver()) {
				data.setItemIndex(this, (data.getItemIndex(this) + 1) % this.inventory.items.size());
			}
			return true;
		}
		return false;
	}

	private boolean givePlayerItems(EntityPlayer player, Vector<ItemStack> toGive) {
		if (toGive.isEmpty()) {
			return false;
		}
		if (this.freeInventorySlots(player) < toGive.size()) {
			return false;
		}
		for (ItemStack is : toGive) {
			this.npc.givePlayerItem(player, is);
		}
		return true;
	}

	private void interact(EntityPlayer player) {
		if (!this.giveItems(player)) {
			this.npc.say(player, this.npc.advanced.getInteractLine());
		}
	}

	private boolean isAllGiver() {
		return this.givingMethod == 1;
	}

	private boolean isChainedGiver() {
		return this.givingMethod == 4;
	}

	private boolean isDaily() {
		return this.cooldownType == 2;
	}

	private boolean isGiveOnce() {
		return this.cooldownType == 1;
	}

	private boolean isGiverWhenNotOwnedAny() {
		return this.givingMethod == 3;
	}

	public boolean isOnTimer() {
		return this.cooldownType == 0;
	}

	private boolean isRandomGiver() {
		return this.givingMethod == 0;
	}

	private boolean isRemainingGiver() {
		return this.givingMethod == 2;
	}

	private boolean playerHasItem(EntityPlayer player, Item item) {
		for (ItemStack is : player.inventory.mainInventory) {
			if (!is.isEmpty() && is.getItem() == item) {
				return true;
			}
		}
		for (ItemStack is : player.inventory.armorInventory) {
			if (!is.isEmpty() && is.getItem() == item) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void load(NBTTagCompound compound) {
		super.load(compound);
		this.type = JobType.ITEM_GIVER;
		this.itemGiverId = compound.getInteger("ItemGiverId");
		this.cooldownType = compound.getInteger("igCooldownType");
		this.givingMethod = compound.getInteger("igGivingMethod");
		this.cooldown = compound.getInteger("igCooldown");
		this.lines = NBTTags.getStringList(compound.getTagList("igLines", 10));
		this.inventory.load(compound.getCompoundTag("igJobInventory"));
		if (this.itemGiverId == 0 && GlobalDataController.instance != null) {
			this.itemGiverId = GlobalDataController.instance.incrementItemGiverId();
		}
		this.availability.load(compound.getCompoundTag("igAvailability"));
	}

	@Override
	public NBTTagCompound save(NBTTagCompound compound) {
		super.save(compound);
		compound.setInteger("igCooldownType", this.cooldownType);
		compound.setInteger("igGivingMethod", this.givingMethod);
		compound.setInteger("igCooldown", this.cooldown);
		compound.setInteger("ItemGiverId", this.itemGiverId);
		compound.setTag("igLines", NBTTags.nbtStringList(this.lines));
		compound.setTag("igJobInventory", this.inventory.save());
		compound.setTag("igAvailability", this.availability.save(new NBTTagCompound()));
		return compound;
	}

	@Override
	public IItemStack[] getItemStacks() {
		IItemStack[] items = new IItemStack[inventory.getSizeInventory()];
		NpcAPI api = NpcAPI.Instance();
		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			if (api != null) { items[i] = api.getIItemStack(inventory.getStackInSlot(i)); }
			else { items[i] = ItemStackWrapper.AIR; }
		}
		return items;
	}

	@Override
	public void setItemStacks(IItemStack[] stacks) {
		inventory.clear();
		if (stacks == null) { return; }
		for (int i = 0; i < inventory.getSizeInventory() && i < stacks.length; i++) {
			inventory.setInventorySlotContents(i, stacks[i].getMCItemStack());
		}
	}

	@Override
	public String[] getLines() {
		String[] ls = new String[3];
		for (int i = 0; i < 3; i++) {
			if (lines.get(i) != null) { ls[i] = lines.get(i); }
			else { ls[i] = ""; }
		}
		return ls;
	}

	@Override
	public void setLines(String[] linesIn) {
		lines.clear();
		if (linesIn == null) { return; }
		for (int i = 0; i < 3; i++) {
			if (i < linesIn.length) { lines.add(linesIn[i]); }
			else { lines.add(""); }
		}
	}

	@Override
	public int getCooldownType() { return cooldownType; }

	@Override
	public void setCooldownType(int type) {
		if (type < 0 || type > 2) {
			throw new CustomNPCsException("Cooldown type must be between 0 and 2");
		}
		cooldownType = type;
	}

	@Override
	public int getGivingType() { return givingMethod; }

	@Override
	public void setGivingType(int type) {
		if (type < 0 || type > 4) {
			throw new CustomNPCsException("Giving type must be between 0 and 4");
		}
		givingMethod = type;
	}

}
