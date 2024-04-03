package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.blocks.tiles.TileScriptedDoor;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;

public class GuiScriptDoor
extends GuiScriptInterface
implements ISubGuiListener {
	
	private TileScriptedDoor script;

	public GuiScriptDoor(int x, int y, int z) {
		TileScriptedDoor tileScriptedDoor = (TileScriptedDoor) this.player.world.getTileEntity(new BlockPos(x, y, z));
		this.script = tileScriptedDoor;
		this.handler = tileScriptedDoor;
		Client.sendData(EnumPacketServer.ScriptDoorDataGet, x, y, z);
	}

	@Override
	public void save() {
		super.save();
		BlockPos pos = this.script.getPos();
		Client.sendData(EnumPacketServer.ScriptDoorDataSave, pos.getX(), pos.getY(), pos.getZ(),
				this.script.getNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.script.setNBT(compound);
		super.setGuiData(compound);
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof GuiScriptEncrypt && ((GuiScriptEncrypt) subgui).send) {
			NBTTagCompound nbt = new NBTTagCompound();
			NBTTagCompound data = new NBTTagCompound();
			BlockPos pos = this.script.getPos();
			data.setInteger("x", pos.getX());
			data.setInteger("y", pos.getY());
			data.setInteger("z", pos.getZ());
			nbt.setTag("data", data);
			this.script.getNBT(nbt);
			String p = new String(this.path);
			while (p.indexOf("\\") !=-1) { p = p.replace("\\", "/"); }
			nbt.setString("Name", ((GuiScriptEncrypt) subgui).getTextField(0).getText() + ((GuiScriptEncrypt) subgui).ext);
			nbt.setString("Path", p + "/" + nbt.getString("Name"));
			nbt.setInteger("Tab", this.activeTab - 1);
			nbt.setByte("Type", (byte) 0);
			nbt.setBoolean("OnlyTab", ((GuiScriptEncrypt) subgui).onlyTab);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.ScriptEncrypt, nbt);
			this.displayGuiScreen(null);
			this.mc.setIngameFocus();
		}
	}
	
}
