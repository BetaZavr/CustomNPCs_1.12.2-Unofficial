package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.api.event.BlockEvent;
import noppes.npcs.api.event.WorldEvent;
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
		this.baseFuncNames.put("interact", BlockEvent.InteractEvent.class);
		this.baseFuncNames.put("redstone", BlockEvent.RedstoneEvent.class);
		this.baseFuncNames.put("broken", BlockEvent.BreakEvent.class);
		this.baseFuncNames.put("exploded", BlockEvent.ExplodedEvent.class);
		this.baseFuncNames.put("rainFilled", BlockEvent.RainFillEvent.class);
		this.baseFuncNames.put("neighborChanged", BlockEvent.NeighborChangedEvent.class);
		this.baseFuncNames.put("init", BlockEvent.InitEvent.class);
		this.baseFuncNames.put("tick", BlockEvent.UpdateEvent.class);
		this.baseFuncNames.put("clicked", BlockEvent.ClickedEvent.class);
		this.baseFuncNames.put("harvested", BlockEvent.HarvestedEvent.class);
		this.baseFuncNames.put("collide", BlockEvent.CollidedEvent.class);
		this.baseFuncNames.put("timer", BlockEvent.TimerEvent.class);
		this.baseFuncNames.put("doorToggle", BlockEvent.DoorToggleEvent.class);
		// CommonEvents
		this.baseFuncNames.put("trigger", WorldEvent.ScriptTriggerEvent.class);
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
