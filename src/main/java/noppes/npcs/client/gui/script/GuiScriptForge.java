package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.ForgeScriptData;

public class GuiScriptForge
extends GuiScriptInterface {
	
	private ForgeScriptData script;

	public GuiScriptForge() {
		this.script = new ForgeScriptData();
		this.handler = this.script;
		Client.sendData(EnumPacketServer.ScriptForgeGet, new Object[0]);
	}

	@Override
	public void save() {
		super.save();
		Client.sendData(EnumPacketServer.ScriptForgeSave, this.script.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.script.readFromNBT(compound);
		super.setGuiData(compound);
	}
	
}
