package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;

public class GuiScriptBlock extends GuiScriptInterface {

	protected final TileScripted script;

	public GuiScriptBlock(int x, int y, int z) {
		super();
		TileScripted tileScripted = (TileScripted) player.world.getTileEntity(new BlockPos(x, y, z));
		script = tileScripted;
		handler = tileScripted;
		Client.sendData(EnumPacketServer.ScriptBlockDataGet, x, y, z);
	}

	@Override
	public void save() {
		super.save();
		try {
			BlockPos pos = script.getPos();
			Client.sendData(EnumPacketServer.ScriptBlockDataSave, pos.getX(), pos.getY(), pos.getZ(), script.getNBT(new NBTTagCompound()));
		} catch (Exception e) { LogWriter.error(e); }
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {

		script.setNBT(compound);
		super.setGuiData(compound);
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiScriptEncrypt && ((SubGuiScriptEncrypt) subgui).send) {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagCompound data = new NBTTagCompound();
			BlockPos pos = script.getPos();
			data.setInteger("x", pos.getX());
			data.setInteger("y", pos.getY());
			data.setInteger("z", pos.getZ());
			nbt.setTag("data", data);
			script.getNBT(nbt);
			nbt.setString("Name", subgui.getTextField(0).getText() + ((SubGuiScriptEncrypt) subgui).ext);
			nbt.setString("Path", path.replaceAll("\\\\", "/") + "/" + nbt.getString("Name"));
			nbt.setInteger("Tab", activeTab - 1);
			nbt.setByte("Type", (byte) 0);
			nbt.setBoolean("OnlyTab", ((SubGuiScriptEncrypt) subgui).onlyTab);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.ScriptEncrypt, nbt);
			displayGuiScreen(null);
			mc.setIngameFocus();
		}
	}

}
