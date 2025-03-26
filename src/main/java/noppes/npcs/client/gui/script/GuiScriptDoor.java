package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.blocks.tiles.TileScriptedDoor;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.ISubGuiInterface;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;

public class GuiScriptDoor
extends GuiScriptInterface
implements ISubGuiListener {

	private final TileScriptedDoor script;

	public GuiScriptDoor(int x, int y, int z) {
		super();
		TileScriptedDoor tileScriptedDoor = (TileScriptedDoor) player.world.getTileEntity(new BlockPos(x, y, z));
		script = tileScriptedDoor;
		handler = tileScriptedDoor;
		Client.sendData(EnumPacketServer.ScriptDoorDataGet, x, y, z);
	}

	@Override
	public void save() {
		super.save();
		BlockPos pos = script.getPos();
		Client.sendData(EnumPacketServer.ScriptDoorDataSave, pos.getX(), pos.getY(), pos.getZ(), script.getNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		script.setNBT(compound);
		super.setGuiData(compound);
	}

	@Override
	public void subGuiClosed(ISubGuiInterface subgui) {
		if (subgui instanceof GuiScriptEncrypt && ((GuiScriptEncrypt) subgui).send) {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagCompound data = new NBTTagCompound();
			BlockPos pos = script.getPos();
			data.setInteger("x", pos.getX());
			data.setInteger("y", pos.getY());
			data.setInteger("z", pos.getZ());
			nbt.setTag("data", data);
			script.getNBT(nbt);
			nbt.setString("Name", subgui.getTextField(0).getFullText() + ((GuiScriptEncrypt) subgui).ext);
			nbt.setString("Path", path.replaceAll("\\\\", "/") + "/" + nbt.getString("Name"));
			nbt.setInteger("Tab", activeTab - 1);
			nbt.setByte("Type", (byte) 0);
			nbt.setBoolean("OnlyTab", ((GuiScriptEncrypt) subgui).onlyTab);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.ScriptEncrypt, nbt);
			displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}

}
