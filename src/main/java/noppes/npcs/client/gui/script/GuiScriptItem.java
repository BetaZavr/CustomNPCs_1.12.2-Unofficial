package noppes.npcs.client.gui.script;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomRegisters;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;

public class GuiScriptItem extends GuiScriptInterface {

	protected final ItemScriptedWrapper item;

	public GuiScriptItem() {
		super();
		ItemScriptedWrapper itemScriptedWrapper = new ItemScriptedWrapper(new ItemStack(CustomRegisters.scripted_item));
		item = itemScriptedWrapper;
		handler = itemScriptedWrapper;
		Client.sendData(EnumPacketServer.ScriptItemDataGet);
	}

	@Override
	public void save() {
		super.save();
		Client.sendData(EnumPacketServer.ScriptItemDataSave, item.getMCNbt());
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		item.setMCNbt(compound);
		super.setGuiData(compound);
	}

	@Override
	public void subGuiClosed(GuiScreen subgui) {
		if (subgui instanceof SubGuiScriptEncrypt && ((SubGuiScriptEncrypt) subgui).send) {
			NBTTagCompound nbt = new NBTTagCompound();
			((ItemScriptedWrapper) handler).getScriptNBT(nbt);
			nbt.setString("Name", ((SubGuiScriptEncrypt) subgui).getTextField(0).getText() + ((SubGuiScriptEncrypt) subgui).ext);
			nbt.setString("Path", path.replaceAll("\\\\", "/") + "/" + nbt.getString("Name"));
			nbt.setInteger("Tab", activeTab - 1);
			nbt.setByte("Type", (byte) 3);
			nbt.setBoolean("OnlyTab", ((SubGuiScriptEncrypt) subgui).onlyTab);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.ScriptEncrypt, nbt);
			displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}

}
