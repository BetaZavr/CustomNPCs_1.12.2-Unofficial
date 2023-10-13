package noppes.npcs.roles;

import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.role.IRoleTransporter;
import noppes.npcs.api.event.RoleEvent;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerTransportData;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

public class RoleTransporter
extends RoleInterface
implements IRoleTransporter {
	
	public String name = "";
	private int ticks;
	public int transportId;

	public RoleTransporter(EntityNPCInterface npc) {
		super(npc);
		this.transportId = -1;
		this.ticks = 10;
		this.type = RoleType.TRANSPORTER;
	}

	@Override
	public boolean aiShouldExecute() {
		--this.ticks;
		if (this.ticks > 0) {
			return false;
		}
		this.ticks = 10;
		if (!this.hasTransport()) {
			return false;
		}
		TransportLocation loc = this.getLocation();
		if (loc.type != 0) {
			return false;
		}
		List<EntityPlayer> inRange = this.npc.world.getEntitiesWithinAABB(EntityPlayer.class,
				this.npc.getEntityBoundingBox().grow(6.0, 6.0, 6.0));
		for (EntityPlayer player : inRange) {
			if (!this.npc.canSee(player)) {
				continue;
			}
			this.unlock(player, loc);
		}
		return false;
	}

	@Override
	public TransportLocation getLocation() {
		if (this.npc.isRemote()) { return null; }
		return TransportController.getInstance().getTransport(this.transportId);
	}

	public boolean hasTransport() {
		TransportLocation loc = this.getLocation();
		return loc != null && loc.id == this.transportId;
	}

	@Override
	public void interact(EntityPlayer player) {
		if (this.hasTransport()) {
			TransportLocation loc = this.getLocation();
			if (loc.type == 2) {
				this.unlock(player, loc);
			}
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerTransporter, this.npc);
		}
	}

	public void setTransport(TransportLocation location) {
		this.transportId = location.id;
		this.name = location.name;
		location.npc = this.npc.getUniqueID();
	}

	public void transport(EntityPlayerMP player, int id) {
		TransportLocation loc = TransportController.getInstance().getTransport(id);
		PlayerData playerdata = PlayerData.get((EntityPlayer) player);
		if (loc == null || (!loc.isDefault() && !playerdata.transportData.transports.contains(loc.id))) {
			return;
		}
		RoleEvent.TransporterUseEvent event = new RoleEvent.TransporterUseEvent((EntityPlayer) player, this.npc.wrappedNPC, loc.copy());
		if (EventHooks.onNPCRole(this.npc, event) || event.location == null) { return; }
		TransportLocation locEvent = (TransportLocation) event.location;
		if (!player.capabilities.isCreativeMode) {
			if (locEvent.money>0) {
				if (locEvent.money > playerdata.game.money) {
					player.sendMessage(new TextComponentTranslation("transporter.hover.not.money"));
					return;
				}
				playerdata.game.addMoney(-1L * locEvent.money);
			}
			if (!locEvent.inventory.isEmpty()) {
				Map<ItemStack, Boolean> barterItems = AdditionalMethods.getInventoryItemCount(player, locEvent.inventory);
				for (ItemStack stack : barterItems.keySet()) {
					if (!barterItems.get(stack)) {
						player.sendMessage(new TextComponentTranslation("transporter.hover.not.money"));
						return;
					}
				}
				for (ItemStack stack : barterItems.keySet()) {
					int amount = stack.getCount();
					for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
						ItemStack is = player.inventory.getStackInSlot(i);
						if (is != null && this.isItemEqual(stack, is)) {
							if (amount < is.getCount()) {
								is.splitStack(amount);
								break;
							}
							player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
							amount -= is.getCount();
						}
					}
				}
				player.inventoryContainer.detectAndSendChanges();
				for (QuestData data : playerdata.questData.activeQuests.values()) {
					for (IQuestObjective obj : data.quest.getObjectives((IPlayer<?>) NpcAPI.Instance().getIEntity(player))) {
						if (obj.getType() != 0) { continue; }
						playerdata.questData.checkQuestCompletion(player, data);
					}
				}
			}
		}
		NoppesUtilPlayer.teleportPlayer(player, locEvent.pos.getX(), locEvent.pos.getY(), locEvent.pos.getZ(), locEvent.dimension, locEvent.yaw, locEvent.pitch);
	}

	private boolean isItemEqual(ItemStack stack, ItemStack other) {
		return !other.isEmpty() && stack.getItem() == other.getItem() && (stack.getItemDamage() < 0 || stack.getItemDamage() == other.getItemDamage());
	}

	private void unlock(EntityPlayer player, TransportLocation loc) {
		PlayerTransportData data = PlayerData.get(player).transportData;
		if (data.transports.contains(this.transportId)) {
			return;
		}
		RoleEvent.TransporterUnlockedEvent event = new RoleEvent.TransporterUnlockedEvent(player, this.npc.wrappedNPC);
		if (EventHooks.onNPCRole(this.npc, event)) {
			return;
		}
		data.transports.add(this.transportId);
		player.sendMessage(new TextComponentTranslation("transporter.unlock", new TextComponentTranslation(loc.name).getFormattedText(), new TextComponentTranslation(loc.category.title).getFormattedText()));
	}


	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = RoleType.TRANSPORTER;
		this.transportId = compound.getInteger("TransporterId");
		TransportLocation loc = this.getLocation();
		this.name = "";
		if (loc != null) { this.name = loc.name; }
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", RoleType.TRANSPORTER.get());
		compound.setInteger("TransporterId", this.transportId);
		return compound;
	}
}
