package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.data.NpcScriptData;

public class GuiScriptNPCs extends GuiScriptInterface {

	protected final NpcScriptData script;

	public GuiScriptNPCs() {
		super();
		script = new NpcScriptData();
		handler = script;
		Client.sendData(EnumPacketServer.ScriptNpcsGet);
	}

	@Override
	public void save() {
		super.save();
		Client.sendData(EnumPacketServer.ScriptNpcsSave, script.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		script.readFromNBT(compound);
		super.setGuiData(compound);
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiScriptEncrypt && ((SubGuiScriptEncrypt) subgui).send) {
			NBTTagCompound nbt = new NBTTagCompound();
			script.writeToNBT(nbt);
			nbt.setString("Name", subgui.getTextField(0).getText() + ((SubGuiScriptEncrypt) subgui).ext);
			nbt.setString("Path", path.replaceAll("\\\\", "/") + "/" + nbt.getString("Name"));
			nbt.setInteger("Tab", activeTab - 1);
			nbt.setByte("Type", (byte) 6);
			nbt.setBoolean("OnlyTab", ((SubGuiScriptEncrypt) subgui).onlyTab);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.ScriptEncrypt, nbt);
			displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}

}
