package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.blocks.tiles.TileScriptedDoor;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;

public class GuiScriptDoor extends GuiScriptInterface {
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
}
