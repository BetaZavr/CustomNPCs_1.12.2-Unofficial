package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataScript;

public class GuiScript extends GuiScriptInterface {

	protected final DataScript script;

	public GuiScript(EntityNPCInterface npc) {
		super();
		this.npc = npc;
		script = npc.script;
		handler = npc.script;
		Client.sendData(EnumPacketServer.ScriptDataGet);
	}

	@Override
	public void save() {
		super.save();
		Client.sendData(EnumPacketServer.ScriptDataSave, script.writeToNBT(new NBTTagCompound()));
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
			nbt.setInteger("EntityID", npc.getEntityId());
			nbt.setByte("Type", (byte) 7);
			nbt.setBoolean("OnlyTab", ((SubGuiScriptEncrypt) subgui).onlyTab);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.ScriptEncrypt, nbt);
			displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}

}
