package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.ISubGuiInterface;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.data.ClientScriptData;

public class GuiScriptClient
extends GuiScriptInterface
implements ISubGuiListener {

	private final ClientScriptData script;

	public GuiScriptClient() {
		super();
		script = new ClientScriptData();
		handler = script;
		Client.sendData(EnumPacketServer.ScriptClientGet);
	}

	@Override
	public void save() {
		super.save();
		Client.sendData(EnumPacketServer.ScriptClientSave, script.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		script.readFromNBT(compound);
		super.setGuiData(compound);
	}

	@Override
	public void subGuiClosed(ISubGuiInterface subgui) {
		if (subgui instanceof GuiScriptEncrypt && ((GuiScriptEncrypt) subgui).send) {
			NBTTagCompound nbt = new NBTTagCompound();
			script.writeToNBT(nbt);
			nbt.setString("Name", subgui.getTextField(0).getText() + ((GuiScriptEncrypt) subgui).ext);
			nbt.setString("Path", path.replaceAll("\\\\", "/") + "/" + nbt.getString("Name"));
			nbt.setInteger("Tab", activeTab - 1);
			nbt.setByte("Type", (byte) 5);
			nbt.setBoolean("OnlyTab", ((GuiScriptEncrypt) subgui).onlyTab);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.ScriptEncrypt, nbt);
			displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}

}
