package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataScript;

public class GuiScript extends GuiScriptInterface {

	private final DataScript script;

	public GuiScript(EntityNPCInterface npc) {
		DataScript script = npc.script;
		this.script = script;
		this.handler = script;
		Client.sendData(EnumPacketServer.ScriptDataGet);
	}

	@Override
	public void save() {
		super.save();
		Client.sendData(EnumPacketServer.ScriptDataSave, this.script.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.script.readFromNBT(compound);
		super.setGuiData(compound);
	}
}
