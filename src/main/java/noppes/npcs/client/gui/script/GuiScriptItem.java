package noppes.npcs.client.gui.script;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomRegisters;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;

public class GuiScriptItem extends GuiScriptInterface {
	private ItemScriptedWrapper item;

	public GuiScriptItem(EntityPlayer player) {
		ItemScriptedWrapper itemScriptedWrapper = new ItemScriptedWrapper(
				new ItemStack((Item) CustomRegisters.scripted_item));
		this.item = itemScriptedWrapper;
		this.handler = itemScriptedWrapper;
		Client.sendData(EnumPacketServer.ScriptItemDataGet, new Object[0]);
	}

	@Override
	public void save() {
		super.save();
		Client.sendData(EnumPacketServer.ScriptItemDataSave, this.item.getMCNbt());
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.item.setMCNbt(compound);
		super.setGuiData(compound);
	}
}
