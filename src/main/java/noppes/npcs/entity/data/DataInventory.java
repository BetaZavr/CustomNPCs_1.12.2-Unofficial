package noppes.npcs.entity.data;

import java.util.*;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.ForgeHooks;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.api.entity.data.INPCInventory;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.DropController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;
import noppes.npcs.util.Util;
import noppes.npcs.util.ValueUtil;

import javax.annotation.Nonnull;

public class DataInventory implements IInventory, INPCInventory {

	/**
	 * 0: head
	 * 1: chest
	 * 2: legs
	 * 3: feet
	 */
	public Map<Integer, IItemStack> armor = new TreeMap<>();
	public final Map<Integer, DropSet> drops = new TreeMap<>();
	public boolean lootMode = true;
	private int maxExp = 0;
	private int minExp = 0;
	public int dropType = 0; // 0-npc drops, 1-template drops, 2-both
	EntityNPCInterface npc;
	public Map<Integer, IItemStack> weapons = new TreeMap<>();
	public Map<Integer, IItemStack> awItems = new TreeMap<>();
	public String saveDropsName = "";
	public int limitation = 0;
	public InventoryBasic deadLoot; // <- PlayerInteractEvent.RightClickBlock
	public Map<EntityLivingBase, InventoryBasic> deadLoots;

	public DataInventory(EntityNPCInterface npc) { this.npc = npc; }

	@Override
	public ICustomDrop addDropItem(IItemStack item, double chance) {
		if (this.drops.size() >= CustomNpcs.MaxItemInDropsNPC) {
			throw new CustomNPCsException("Bad maximum size: " + this.drops.size() + " (" + CustomNpcs.MaxItemInDropsNPC + " slots maximum)");
		}
		chance = ValueUtil.correctDouble(chance, 0.0001d, 100.0d);
		DropSet ds = new DropSet(this);
		ds.item = item;
		ds.chance = chance;
		ds.pos = this.drops.size();
		this.drops.put(ds.pos, ds);
		return ds;
	}

	public void clear() {
		this.armor.clear();
		this.drops.clear();
		this.weapons.clear();
		this.awItems.clear();
		this.minExp = 0;
		this.maxExp = 0;
	}

	public void closeInventory(@Nonnull EntityPlayer player) {}

	@Override
	public Map<IEntity<?>, List<IItemStack>> createDrops(int lootType, double baseChance) {
		List<DropSet> allDrops = new ArrayList<>();
		if (dropType != 0 && !saveDropsName.isEmpty()) { allDrops = DropController.getInstance().getDrops(saveDropsName); } // template drops
		if (dropType != 1) { allDrops.addAll(drops.values()); } // both
		List<IItemStack> anyItems = new ArrayList<>();
		Map<IEntity<?>, List<IItemStack>> map = new HashMap<>();
		for (DropSet ds : allDrops) {
			double c = ds.chance * baseChance / 100.0d;
			double r = Math.random();
			if (ds.item == null || ds.item.isEmpty() || lootType != ds.lootMode || (ds.amount[0] == 0 && ds.amount[1] == 0) || (c < 1.0d && c < r)) { continue; }
			IItemStack iStack = ds.createLoot(baseChance);
			if (iStack.isEmpty()) { continue; }
			int qID = ds.getQuestID();
			if (qID > 0) {
				for (EntityLivingBase attacking : npc.combatHandler.aggressors.keySet()) {
					if (!(attacking instanceof EntityPlayer)) { continue; }
					PlayerData data = PlayerData.get((EntityPlayer) attacking);
					if (data == null || !data.questData.activeQuests.containsKey(qID)) { continue; }
					Quest quest = QuestController.instance.quests.get(qID);
					if (quest == null) { continue; }
					boolean needAdd = true;
					for (IQuestObjective objQ : quest.getObjectives((EntityPlayer) attacking)) {
						if (((QuestObjective) objQ).getEnumType() != EnumQuestTask.ITEM) { continue; }
						if (((QuestObjective) objQ).getItemStack().isItemEqual(iStack.getMCItemStack()) && objQ.isCompleted()) {
							needAdd = false;
							break;
						}
					}
					if (needAdd) {
						IEntity<?> iEntity = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(attacking);
						if (iEntity != null) {
							if (!map.containsKey(iEntity)) { map.put(iEntity, new ArrayList<>()); }
							map.get(iEntity).add(iStack);
						}
					}
				}
			}
			else { anyItems.add(iStack); }
		}
		// put simple items
		if (!anyItems.isEmpty()) {
			// shuffle
			Collections.shuffle(anyItems, new Random());
			// sort aggressors
			LinkedHashMap<EntityLivingBase, Double> aggressors = Util.instance.sortByValue(npc.combatHandler.aggressors);
			// create aggressors list
			IEntity<?> damageLieder = null;
			Map<IEntity<?>, Double> entitys = new LinkedHashMap<>();
			double totalDamageValue = 0.0d;
			for (EntityLivingBase attacking : aggressors.keySet()) {
				if (attacking instanceof EntityPlayer || !npc.combatHandler.onlyPlayers && lootType != 2) {
					IEntity<?> iEntity = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(attacking);
					if (iEntity != null) {
						if (damageLieder == null) { damageLieder = iEntity; }
						entitys.put(iEntity, aggressors.get(attacking));
						totalDamageValue += aggressors.get(attacking);
					}
				}
			}
			// amount rewards
			Map<IEntity<?>, Integer> itemsToEntity = new HashMap<>();
			int s = anyItems.size();
			for (IEntity<?> attacking : entitys.keySet()) {
				int amount = (int) Math.round(totalDamageValue / entitys.get(attacking) * anyItems.size());
				if (amount > s) { amount = 2; }
				itemsToEntity.put(attacking, amount);
				s -= amount;
				if (s == 0) { break; }
			}
			if (damageLieder == null) {
				map.put(npc.wrappedNPC, anyItems);
			} else {
				if (s != 0) { itemsToEntity.put(damageLieder, itemsToEntity.get(damageLieder) + s); }
				// put items to entities
				for (IEntity<?> attacking : itemsToEntity.keySet()) {
					if (anyItems.isEmpty()) { break; }
					for (int i = 0; i < itemsToEntity.get(attacking); i++) {
						if (anyItems.isEmpty()) { break; }
						IItemStack iStack = anyItems.get(0);
						if (!map.containsKey(attacking)) { map.put(attacking, new ArrayList<>()); }
						map.get(attacking).add(iStack);
						anyItems.remove(iStack);
					}
				}
			}
		}
        return map;
	}

	public @Nonnull ItemStack decrStackSize(int slot0, int slot1) {
		int i;
		Map<Integer, IItemStack> var3;
		ItemStack var4 = null;
		if (slot0 >= 7) {
			var3 = this.awItems;
			slot0 -= 7;
			i = 3;
		} else if (slot0 >= 4) {
			var3 = this.weapons;
			slot0 -= 4;
			i = 1;
		} else {
			var3 = this.armor;
			i = 2;
		}

		if (var3.get(slot0) != null) {
			if (var3.get(slot0).getMCItemStack().getCount() <= slot1) {
				var4 = var3.get(slot0).getMCItemStack();
				var3.put(slot0, null);
			} else {
				var4 = var3.get(slot0).getMCItemStack().splitStack(slot1);
				if (var3.get(slot0).getMCItemStack().getCount() == 0) {
					var3.put(slot0, null);
				}
			}
		}
		if (i == 1) {
			this.weapons = var3;
		} else if (i == 2) {
			this.armor = var3;
		} else {
			this.awItems = var3;
		}
		if (var4 == null) {
			return ItemStack.EMPTY;
		}
		return var4;
	}

	public void dropStuff(NpcEvent.DiedEvent event, DamageSource damagesource) {
		deadLoot = null;
		deadLoots = null;
		// Vanilla
		ArrayList<EntityItem> list = new ArrayList<>();
		if (event.droppedItems != null) {
			for (IItemStack iStack : event.droppedItems) {
				if (iStack == null || iStack.isEmpty()) { continue; }
				EntityItem e = getEntityItem(iStack.getMCItemStack().copy(), event.droppedItems.length > 7);
				if (e != null) { list.add(e); }
			}
		}
		boolean notDropOnGround = ForgeHooks.onLivingDrops(npc, damagesource, list, 0, true);
		if (!notDropOnGround) {
            for (EntityItem e : list) {
                if (e == null) { continue; }
                npc.world.spawnEntity(e);
            }
		}

		list.clear();
		if (event.lootedItems != null) {
			for (IEntity<?> iEntity : event.lootedItems.keySet()) {
				for (IItemStack iStack : event.lootedItems.get(iEntity)) {
					if (iStack == null || iStack.isEmpty()) { continue; }
					EntityItem e = getEntityItem(iStack.getMCItemStack().copy(), event.lootedItems.get(iEntity).size() > 7);
					if (e == null) { continue; }
					if (iEntity instanceof IPlayer) {
						EntityPlayer player = (EntityPlayer) iEntity.getMCEntity();
						e.setPickupDelay(2);
						e.setOwner(player.getName());
						npc.world.spawnEntity(e);
						ItemStack stack = e.getItem();
						int i = stack.getCount();
						if (!player.inventory.addItemStackToInventory(stack)) { continue; }
						player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, ((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7f + 1.0f) * 2.0f);
						player.onItemPickup(e, i);
						if (stack.getCount() <= 0) { e.setDead(); }
					} else {
						e.setPosition(iEntity.getPos().getX(), iEntity.getPos().getY() + iEntity.getMCEntity().getEyeHeight() / 2.0d, iEntity.getPos().getZ());
						npc.world.spawnEntity(e);
					}
				}
			}
		}
		if (event.inventoryItems != null) {
			if (event.totalDamageOnlyPlayers == 0.0d) {
				List<ItemStack> stacks = new ArrayList<>();
				for (IEntity<?> iEntity : event.inventoryItems.keySet()) {
					for (IItemStack iStack : event.inventoryItems.get(iEntity)) {
						stacks.add(iStack.getMCItemStack());
						if (stacks.size() == 54) { break; }
					}
					if (stacks.size() == 54) { break; }
				}
				deadLoot = new InventoryBasic("NPC Loot", true, (int) (Math.ceil((double) stacks.size() / 9.0d) * 9.0d));
				System.out.println("CNPCs: ");
				int i = 0;
				for (ItemStack stack : stacks) {
					deadLoot.setInventorySlotContents(i, stack);
					i++;
				}
			}
			for (IEntity<?> iEntity : event.inventoryItems.keySet()) {
				InventoryBasic inv = new InventoryBasic("NPC Loot", true, (int) (Math.ceil((double) event.inventoryItems.get(iEntity).size() / 9.0d) * 9.0d));
				int i = 0;
				for (IItemStack iStack : event.inventoryItems.get(iEntity)) {
					inv.setInventorySlotContents(i, iStack.getMCItemStack());
					i++;
				}
				if (deadLoots == null) { deadLoots = new HashMap<>(); }
				deadLoots.put((EntityLivingBase) iEntity.getMCEntity(), inv);
			}
		}
		if (event.expDropped > 0) {
			if (!lootMode) {
				int exp = event.expDropped;
				while (exp > 0) {
					int currentValue = EntityXPOrb.getXPSplit(exp);
					exp -= currentValue;
					npc.world.spawnEntity(new EntityXPOrb(npc.world, npc.posX, npc.posY, npc.posZ, currentValue));
				}
			} else {
				for (IEntity<?> iEntity : event.damageMap.keySet()) {
					if (!(iEntity instanceof IPlayer)) { continue; }
					int exp = (int) ((double) event.expDropped * event.damageMap.get(iEntity) / event.totalDamageOnlyPlayers);
					Entity player = iEntity.getMCEntity();
					while (exp > 0) {
						int currentValue = EntityXPOrb.getXPSplit(exp);
						exp -= currentValue;
						this.npc.world.spawnEntity(new EntityXPOrb(player.world, player.posX, player.posY, player.posZ, currentValue));
					}
				}
			}
		}
	}

	public IItemStack getArmor(int slot) {
		return this.armor.get(slot);
	}

	public @Nonnull ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}

	public ICustomDrop getDrop(int slot) {
		if (slot < 0 || slot >= this.drops.size()) {
			throw new CustomNPCsException("Bad slot number: " + slot + " in " + this.drops.size() + " maximum");
		}
        return this.drops.get(slot);
	}

	public IItemStack getDropItem(int slot) {
		if (slot < 0 || slot >= this.drops.size()) {
			throw new CustomNPCsException("Bad slot number: " + slot + " in " + this.drops.size() + " maximum");
		}
		DropSet g = this.drops.get(slot);
		return g.getItem();
	}

	public ICustomDrop[] getDrops() {
		ICustomDrop[] dss = new ICustomDrop[this.drops.size()];
		int i = 0;
		for (DropSet ds : this.drops.values()) {
			dss[i] = ds;
			i++;
		}
		return dss;
	}

	public EntityItem getEntityItem(ItemStack itemstack, boolean throwFar) {
		if (itemstack == null || itemstack.isEmpty()) { return null; }
		EntityItem entityitem = new EntityItem(npc.world, npc.posX, npc.posY - 0.30000001192092896 + npc.getEyeHeight(), npc.posZ, itemstack);
		entityitem.setPickupDelay(40);
		if (throwFar) {
			float f2 = npc.getRNG().nextFloat() * 0.5f;
			float f3 = npc.getRNG().nextFloat() * 3.141593f * 2.0f;
			entityitem.motionX = -MathHelper.sin(f3) * f2;
			entityitem.motionZ = MathHelper.cos(f3) * f2;
			entityitem.motionY = 0.20000000298023224;
		}
		return entityitem;
	}

	public int getExpMax() {
		return this.npc.inventory.maxExp;
	}

	public int getExpMin() {
		return this.npc.inventory.minExp;
	}

	public int getExpRNG() {
		int exp = this.minExp;
		if (this.maxExp - this.minExp > 0) {
			exp += this.npc.world.rand.nextInt(this.maxExp - this.minExp);
		}
		return exp;
	}

	public int getField(int id) {
		return 0;
	}

	public int getFieldCount() {
		return 0;
	}

	public int getInventoryStackLimit() {
		return 64;
	}

	public IItemStack getLeftHand() {
		return this.weapons.get(2);
	}

	public @Nonnull String getName() {
		return "NPC Inventory";
	}

	public IItemStack getProjectile() {
		return this.weapons.get(1);
	}

	public IItemStack getRightHand() {
		return this.weapons.get(0);
	}

	public int getSizeInventory() {
		return 7 + this.drops.size();
	}

	public @Nonnull ItemStack getStackInSlot(int slot) {
		if (slot < 4) {
			return ItemStackWrapper.MCItem(this.getArmor(slot));
		}
		if (slot < 7) {
			return ItemStackWrapper.MCItem(this.weapons.get(slot - 4));
		}
		if (slot < 9) {
			return ItemStackWrapper.MCItem(this.awItems.get(slot - 7));
		}
		return ItemStack.EMPTY;
	}

	public boolean getXPLootMode() {
		return this.lootMode;
	}

	public boolean hasCustomName() {
		return true;
	}

	public boolean isEmpty() {
		for (int slot = 0; slot < this.getSizeInventory(); ++slot) {
			ItemStack item = this.getStackInSlot(slot);
			if (!NoppesUtilServer.IsItemStackNull(item) && !item.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public boolean isItemValidForSlot(int slot, @Nonnull ItemStack itemstack) {
		return true;
	}

	public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
		return true;
	}

	public void markDirty() {
	}

	public void openInventory(@Nonnull EntityPlayer player) {
	}

	public void readEntityFromNBT(NBTTagCompound compound) {
		this.minExp = compound.getInteger("MinExp");
		this.maxExp = compound.getInteger("MaxExp");
		this.armor = NBTTags.getIItemStackMap(compound.getTagList("Armor", 10));
		this.weapons = NBTTags.getIItemStackMap(compound.getTagList("Weapons", 10));
		this.awItems = NBTTags.getIItemStackMap(compound.getTagList("AWModItems", 10));

		Map<Integer, DropSet> drs = new HashMap<>();
		if (compound.hasKey("DropChance", 9)) { // if old items
			Map<Integer, IItemStack> d_old = NBTTags.getIItemStackMap(compound.getTagList("NpcInv", 10));
			Map<Integer, Integer> dc_old = NBTTags.getIntegerIntegerMap(compound.getTagList("DropChance", 10));
			int i = 0;
			for (int slot : d_old.keySet()) {
				if (dc_old.get(slot) <= 0) {
					continue;
				}
				DropSet ds = new DropSet(this);
				ds.item = d_old.get(slot);
				ds.chance = (double) dc_old.get(slot);
				ds.amount = new int[] { ds.item.getStackSize(), ds.item.getStackSize() };
				ds.pos = i;
				drs.put(i, ds);
				i++;
			}
		} else { // create data
			for (int i = 0; i < compound.getTagList("NpcInv", 10).tagCount(); i++) {
				DropSet ds = new DropSet(this);
				ds.load(compound.getTagList("NpcInv", 10).getCompoundTagAt(i));
				ds.pos = i;
				drs.put(ds.pos, ds);
			}
		}
		this.drops.clear();
		this.drops.putAll(drs);
		this.lootMode = compound.getBoolean("LootMode");
		this.saveDropsName = compound.getString("SaveDropsName");
		this.dropType = compound.getInteger("DropType");
		this.limitation = compound.getInteger("Limitation");
		if (this.dropType < 0) {
			this.dropType *= -1;
		}
		if (this.dropType > 2) {
			this.dropType %= 3;
		}
	}

	public boolean removeDrop(ICustomDrop drop) {
		Map<Integer, DropSet> newDrop = new TreeMap<>();
		boolean del = false;
		int j = 0;
		for (int slot : this.drops.keySet()) {
			if (this.drops.get(slot) == drop) {
				del = true;
				continue;
			}
			newDrop.put(j, this.drops.get(slot));
			newDrop.get(j).pos = j;
			j++;
		}
		if (del) {
			this.drops.clear();
			this.drops.putAll(newDrop);
		}
		return del;
	}

	public boolean removeDrop(int slot) {
		if (this.drops.containsKey(slot)) {
			this.drops.remove(slot);
			Map<Integer, DropSet> newDrop = new TreeMap<>();
			int j = 0;
			for (int s : this.drops.keySet()) {
				if (s == slot) {
					continue;
				}
				newDrop.put(j, this.drops.get(s));
				newDrop.get(j).pos = j;
				j++;
			}
			this.drops.clear();
			this.drops.putAll(newDrop);
			return true;
		}
		return false;
	}

	public @Nonnull ItemStack removeStackFromSlot(int slot) {
		int i;
		Map<Integer, IItemStack> var2;
		if (slot >= 7) {
			var2 = this.awItems;
			slot -= 7;
			i = 3;
		} else if (slot >= 4) {
			var2 = this.weapons;
			slot -= 4;
			i = 1;
		} else {
			var2 = this.armor;
			i = 2;
		}
		if (var2.get(slot) != null) {
			ItemStack var3 = var2.get(slot).getMCItemStack();
			var2.put(slot, null);
			if (i == 1) {
				this.weapons = var2;
			} else if (i == 2) {
				this.armor = var2;
			} else {
				this.awItems = var2;
			}
			return var3;
		}
		return ItemStack.EMPTY;
	}

	public void setArmor(int slot, IItemStack item) {
		this.armor.put(slot, item);
		this.npc.updateClient = true;
	}

	public void setExp(int min, int max) {
		min = Math.min(min, max);
		this.npc.inventory.minExp = min;
		this.npc.inventory.maxExp = max;
	}

	public void setField(int id, int value) {
	}

	public void setInventorySlotContents(int slot, @Nonnull ItemStack item) {
		int i;
		Map<Integer, IItemStack> var3;
		if (slot >= 7) {
			var3 = this.awItems;
			slot -= 7;
			i = 3;
		} else if (slot >= 4) {
			var3 = this.weapons;
			slot -= 4;
			i = 1;
		} else {
			var3 = this.armor;
			i = 2;
		}
		var3.put(slot, Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(item));
		if (i == 1) {
			this.weapons = var3;
		} else if (i == 2) {
			this.armor = var3;
		} else {
			this.awItems = var3;
		}
	}

	public void setLeftHand(IItemStack item) {
		this.weapons.put(2, item);
		this.npc.updateClient = true;
	}

	public void setProjectile(IItemStack item) {
		this.weapons.put(1, item);
		this.npc.updateAI = true;
	}

	public void setRightHand(IItemStack item) {
		this.weapons.put(0, item);
		this.npc.updateClient = true;
	}

	public void setXPLootMode(boolean mode) {
		this.lootMode = mode;
	}

	public NBTTagCompound writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("MinExp", this.minExp);
		compound.setInteger("MaxExp", this.maxExp);
		compound.setTag("Armor", NBTTags.nbtIItemStackMap(this.armor));
		compound.setTag("Weapons", NBTTags.nbtIItemStackMap(this.weapons));
		compound.setTag("AWModItems", NBTTags.nbtIItemStackMap(this.awItems));
		NBTTagList dropList = new NBTTagList();
		int s = 0;
		for (int slot : this.drops.keySet()) {
			if (this.drops.get(slot) == null) {
				continue;
			}
			if (this.drops.get(slot).pos != s) {
				this.drops.get(slot).pos = s;
			}
			dropList.appendTag(this.drops.get(slot).getNBT());
			s++;
		}
		compound.setTag("NpcInv", dropList);
		compound.setBoolean("LootMode", this.lootMode);
		compound.setString("SaveDropsName", this.saveDropsName);
		compound.setInteger("DropType", this.dropType);
		compound.setInteger("Limitation", this.limitation);
		return compound;
	}

}
