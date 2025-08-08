package noppes.npcs.api.wrapper;

import java.util.Objects;
import java.util.TreeMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.EntityType;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.api.entity.data.INPCAdvanced;
import noppes.npcs.api.entity.data.INPCAi;
import noppes.npcs.api.entity.data.INPCAnimation;
import noppes.npcs.api.entity.data.INPCDisplay;
import noppes.npcs.api.entity.data.INPCInventory;
import noppes.npcs.api.entity.data.INPCJob;
import noppes.npcs.api.entity.data.INPCRole;
import noppes.npcs.api.entity.data.INPCStats;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IFaction;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

@SuppressWarnings("rawtypes")
public class NPCWrapper<T extends EntityNPCInterface> extends EntityLivingWrapper<T> implements ICustomNpc {

	public NPCWrapper(T npc) {
		super(npc);
	}

	@Override
	public String executeCommand(String command) {
		if (!Objects.requireNonNull(this.entity.getServer()).isCommandBlockEnabled()) {
			throw new CustomNPCsException("Command blocks need to be enabled to executeCommands");
		}
		return NoppesUtilServer.runCommand(this.entity, this.entity.getName(), command, null);
	}

	@Override
	public INPCAdvanced getAdvanced() {
		return this.entity.advanced;
	}

	@Override
	public long getAge() {
		return this.entity.totalTicksAlive;
	}

	@Override
	public INPCAi getAi() {
		return this.entity.ais;
	}

	@Override
	public INPCAnimation getAnimations() {
		return this.entity.animation;
	}

	@Override
	public IDialog getDialog(int slot) {
		if (slot < 0 || slot >= this.entity.dialogs.length) {
			throw new CustomNPCsException("Slot needs to be between 0 and " + (this.entity.dialogs.length - 1));
		}
		IDialog dialog = null;
		int s = 0;
		TreeMap<Integer, Dialog> dialogs = DialogController.instance.dialogs;
		for (int dialogId : this.entity.dialogs) {
			if (s == slot) {
				if (dialogs.containsKey(dialogId)) {
					dialog = dialogs.get(dialogId);
				}
				break;
			}
		}
		return dialog;
	}

	@Override
	public INPCDisplay getDisplay() {
		return this.entity.display;
	}

	@Override
	public IFaction getFaction() {
		return this.entity.faction;
	}

	@Override
	public int getHomeX() {
		return this.entity.ais.startPos().getX();
	}

	@Override
	public int getHomeY() {
		return this.entity.ais.startPos().getY();
	}

	@Override
	public int getHomeZ() {
		return this.entity.ais.startPos().getZ();
	}

	@Override
	public INPCInventory getInventory() {
		return this.entity.inventory;
	}

	@Override
	public INPCJob getJob() {
		return this.entity.advanced.jobInterface;
	}

	@Override
	public String getName() {
		return this.entity.display.getName();
	}

	public int getOffsetX() {
		return (int) this.entity.ais.bodyOffsetX;
	}

	public int getOffsetY() {
		return (int) this.entity.ais.bodyOffsetY;
	}

	public int getOffsetZ() {
		return (int) this.entity.ais.bodyOffsetZ;
	}

	@Override
	public IEntityLivingBase getOwner() {
		EntityLivingBase owner = this.entity.getOwner();
		if (owner != null) {
			return (IEntityLivingBase) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(owner);
		}
		return null;
	}

	@Override
	public INPCRole getRole() {
		return this.entity.advanced.roleInterface;
	}

	@Override
	public INPCStats getStats() {
		return this.entity.stats;
	}

	@Override
	public ITimers getTimers() {
		return this.entity.timers;
	}

	@Override
	public int getType() {
		return EntityType.NPC.get();
	}

	@Override
	public void giveItem(IPlayer player, IItemStack item) {
		this.entity.givePlayerItem(player.getMCEntity(), item.getMCItemStack());
	}

	@Override
	public void reset() {
		this.entity.reset();
	}

	@Override
	public void say(String message) {
		this.entity.saySurrounding(new Line(message));
	}

	@Override
	public void sayTo(IPlayer player, String message) {
		this.entity.say(player.getMCEntity(), new Line(message));
	}

	@Override
	public void setDialog(int slot, IDialog dialog) {
		if (slot >= 0 && slot <= entity.dialogs.length) {
			if (dialog == null && slot < entity.dialogs.length) {
				int[] newIDs = new int[entity.dialogs.length - 1];
				for (int i = 0, j = 0; i < entity.dialogs.length; i++) {
					if (i == slot) {
						continue;
					}
					newIDs[j] = entity.dialogs[i];
					j++;
				}
				entity.dialogs = newIDs;
			}
			else if (dialog != null) {
				if (slot == entity.dialogs.length) {
					int[] newIDs = new int[entity.dialogs.length + 1];
                    System.arraycopy(entity.dialogs, 0, newIDs, 0, entity.dialogs.length);
					entity.dialogs = newIDs;
				}
				entity.dialogs[slot] = dialog.getId();
			}
		}
		else {
			throw new CustomNPCsException("Slot needs to be between 0 and " + entity.dialogs.length);
		}
	}

	@Override
	public void setFaction(int id) {
		Faction faction = FactionController.instance.getFaction(id);
		if (faction == null) {
			throw new CustomNPCsException("Unknown faction id: " + id);
		}
		this.entity.setFaction(id);
	}

	@Override
	public void setHome(int x, int y, int z) {
		this.entity.ais.setStartPos(new BlockPos(x, y, z));
	}

	@Override
	public void setMaxHealth(float health) {
		if (health == this.entity.stats.maxHealth) {
			return;
		}
		super.setMaxHealth(health);
		this.entity.stats.maxHealth = (int) health;
		this.entity.updateClient = true;
	}

	@Override
	public void setName(String name) {
		this.entity.display.setName(name);
	}

	public void setOffset(int x, int y, int z) {
		this.entity.ais.bodyOffsetX = ValueUtil.correctFloat(x, 0.0f, 9.0f);
		this.entity.ais.bodyOffsetY = ValueUtil.correctFloat(y, 0.0f, 9.0f);
		this.entity.ais.bodyOffsetZ = ValueUtil.correctFloat(z, 0.0f, 9.0f);
		this.entity.updateClient = true;
	}

	@Override
	public void setRotation(float rotation) {
		super.setRotation(rotation);
		if (this.entity.ais.orientation != (int) this.entity.rotationYaw) {
			this.entity.ais.orientation = (int) this.entity.rotationYaw;
			this.entity.updateClient = true;
		}
	}

	@Override
	public IProjectile shootItem(double x, double y, double z, IItemStack item, int accuracy) {
		if (item == null) {
			throw new CustomNPCsException("No item was given");
		}
		accuracy = ValueUtil.correctInt(accuracy, 1, 100);
		return (IProjectile) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(this.entity.shoot(x, y, z, accuracy, item.getMCItemStack(), false));
	}

	@Override
	public IProjectile shootItem(IEntityLivingBase target, IItemStack item, int accuracy) {
		if (item == null) {
			throw new CustomNPCsException("No item was given");
		}
		if (target == null) {
			throw new CustomNPCsException("No target was given");
		}
		accuracy = ValueUtil.correctInt(accuracy, 1, 100);
		return (IProjectile) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(this.entity.shoot(target.getMCEntity(), accuracy, item.getMCItemStack(), false));
	}

	@Override
	public void trigger(int id, Object... arguments) {
		EventHooks.onScriptTriggerEvent(this.entity.script, id, this.getWorld(), this.getPos(), this, arguments);
	}

	@Override
	public boolean typeOf(int type) {
		return type == EntityType.NPC.get() || super.typeOf(type);
	}

	@Override
	public void updateClient() {
		this.entity.updateClient();
	}

}
