package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.event.WorldEvent;
import noppes.npcs.api.event.potion.AffectEntity;
import noppes.npcs.api.event.potion.EndEffect;
import noppes.npcs.api.event.potion.IsReadyEvent;
import noppes.npcs.api.event.potion.PerformEffect;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.PotionScriptData;

public class GuiScriptPotion
extends GuiScriptInterface {
	
	private PotionScriptData script;

	public GuiScriptPotion() {
		this.script = new PotionScriptData();
		this.handler = this.script;
		Client.sendData(EnumPacketServer.ScriptPotionGet);
		this.baseFuncNames.put("isReady", IsReadyEvent.class);
		this.baseFuncNames.put("performEffect", PerformEffect.class);
		this.baseFuncNames.put("affectEntity", AffectEntity.class);
		this.baseFuncNames.put("endEffect", EndEffect.class);
		// CommonEvents
		this.baseFuncNames.put("trigger", WorldEvent.ScriptTriggerEvent.class);
	}

	@Override
	public void save() {
		super.save();
		Client.sendData(EnumPacketServer.ScriptPotionSave, this.script.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.script.readFromNBT(compound);
		super.setGuiData(compound);
	}
	
}
