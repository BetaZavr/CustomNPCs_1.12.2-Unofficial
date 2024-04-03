package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.data.ForgeScriptData;

public class GuiScriptForge
extends GuiScriptInterface
implements ISubGuiListener {
	
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
	
	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof GuiScriptEncrypt && ((GuiScriptEncrypt) subgui).send) {
			NBTTagCompound nbt = new NBTTagCompound();
			this.script.writeToNBT(nbt);
			String p = new String(this.path);
			while (p.indexOf("\\") !=-1) { p = p.replace("\\", "/"); }
			nbt.setString("Name", ((GuiScriptEncrypt) subgui).getTextField(0).getText() + ((GuiScriptEncrypt) subgui).ext);
			nbt.setString("Path", p + "/" + nbt.getString("Name"));
			nbt.setInteger("Tab", this.activeTab - 1);
			nbt.setByte("Type", (byte) 1);
			nbt.setBoolean("OnlyTab", ((GuiScriptEncrypt) subgui).onlyTab);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.ScriptEncrypt, nbt);
			this.displayGuiScreen(null);
			this.mc.setIngameFocus();
		}
	}
	
}
