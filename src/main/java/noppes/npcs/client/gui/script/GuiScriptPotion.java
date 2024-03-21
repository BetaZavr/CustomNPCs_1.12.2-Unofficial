package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
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
