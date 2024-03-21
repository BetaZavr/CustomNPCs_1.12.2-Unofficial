package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.PlayerScriptData;

public class GuiScriptPlayers extends GuiScriptInterface {
	private PlayerScriptData script;

	public GuiScriptPlayers() {
		this.script = new PlayerScriptData(null);
		this.handler = this.script;
		Client.sendData(EnumPacketServer.ScriptPlayerGet, new Object[0]);
	}

	@Override
	public void save() {
		super.save();
		Client.sendData(EnumPacketServer.ScriptPlayerSave, this.script.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.script.readFromNBT(compound);
		super.setGuiData(compound);
	}
}
