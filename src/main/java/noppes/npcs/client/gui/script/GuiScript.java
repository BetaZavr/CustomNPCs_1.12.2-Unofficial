package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.event.CustomGuiEvent;
import noppes.npcs.api.event.DialogEvent;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.event.RoleEvent;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataScript;
import noppes.npcs.roles.RoleBank;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.roles.RolePostman;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.roles.RoleTransporter;

public class GuiScript extends GuiScriptInterface {
	
	private DataScript script;

	public GuiScript(EntityNPCInterface npc) {
		DataScript script = npc.script;
		this.script = script;
		this.handler = script;
		Client.sendData(EnumPacketServer.ScriptDataGet, new Object[0]);
		// New
		this.baseFuncNames.put("init", NpcEvent.InitEvent.class);
		this.baseFuncNames.put("tick", NpcEvent.UpdateEvent.class);
		this.baseFuncNames.put("target", NpcEvent.TargetEvent.class);
		this.baseFuncNames.put("targetLost", NpcEvent.TargetLostEvent.class);
		this.baseFuncNames.put("interact", NpcEvent.InteractEvent.class);
		this.baseFuncNames.put("died", NpcEvent.DiedEvent.class);
		this.baseFuncNames.put("kill", NpcEvent.KilledEntityEvent.class);
		this.baseFuncNames.put("damagedEntity", NpcEvent.MeleeAttackEvent.class);
		this.baseFuncNames.put("attack", NpcEvent.MeleeAttackEvent.class);
		this.baseFuncNames.put("launched", NpcEvent.RangedLaunchedEvent.class);
		this.baseFuncNames.put("damaged", NpcEvent.DamagedEvent.class);
		this.baseFuncNames.put("collide", NpcEvent.CollideEvent.class);
		this.baseFuncNames.put("timer", NpcEvent.TimerEvent.class);
		// RoleEvent
		if (npc.roleInterface != null) {
			if (npc.roleInterface instanceof RoleBank) {
				this.baseFuncNames.put("role", RoleEvent.BankUnlockedEvent.class);
			}
			if (npc.roleInterface instanceof RoleFollower) {
				this.baseFuncNames.put("role", RoleEvent.FollowerHireEvent.class);
			}
			if (npc.roleInterface instanceof RoleTrader) {
				this.baseFuncNames.put("role", RoleEvent.TraderEvent.class);
			}
			if (npc.roleInterface instanceof RoleTransporter) {
				this.baseFuncNames.put("role", RoleEvent.TransporterUnlockedEvent.class);
			}
			if (npc.roleInterface instanceof RolePostman) {
				this.baseFuncNames.put("role", RoleEvent.MailmanEvent.class);
			}
		}
		// DialogEvent
		this.baseFuncNames.put("dialog", DialogEvent.OpenEvent.class);
		this.baseFuncNames.put("dialogClose", DialogEvent.CloseEvent.class);
		this.baseFuncNames.put("dialogOption", DialogEvent.OptionEvent.class);
		// CustomGuiEvent
		this.baseFuncNames.put("customGuiClosed", CustomGuiEvent.CloseEvent.class);
		this.baseFuncNames.put("customGuiButton", CustomGuiEvent.ButtonEvent.class);
		this.baseFuncNames.put("customGuiSlot", CustomGuiEvent.SlotEvent.class);
		this.baseFuncNames.put("customGuiScroll", CustomGuiEvent.ScrollEvent.class);
		this.baseFuncNames.put("customGuiSlotClicked", CustomGuiEvent.SlotClickEvent.class);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.script.readFromNBT(compound);
		super.setGuiData(compound);
	}


	@Override
	public void save() {
		super.save();
		Client.sendData(EnumPacketServer.ScriptDataSave, this.script.writeToNBT(new NBTTagCompound()));
	}
}
