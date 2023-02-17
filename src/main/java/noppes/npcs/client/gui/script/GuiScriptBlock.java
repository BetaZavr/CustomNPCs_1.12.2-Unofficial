package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.api.event.BlockEvent;
import noppes.npcs.api.event.CustomGuiEvent;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;

public class GuiScriptBlock
extends GuiScriptInterface {
	
	private TileScripted script;

	public GuiScriptBlock(int x, int y, int z) {
		TileScripted tileScripted = (TileScripted) this.player.world.getTileEntity(new BlockPos(x, y, z));
		this.script = tileScripted;
		this.handler = tileScripted;
		Client.sendData(EnumPacketServer.ScriptBlockDataGet, x, y, z);
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
		// CustomGuiEvent
		this.baseFuncNames.put("customGuiClosed", CustomGuiEvent.CloseEvent.class);
		this.baseFuncNames.put("customGuiButton", CustomGuiEvent.ButtonEvent.class);
		this.baseFuncNames.put("customGuiSlot", CustomGuiEvent.SlotEvent.class);
		this.baseFuncNames.put("customGuiScroll", CustomGuiEvent.ScrollEvent.class);
		this.baseFuncNames.put("customGuiSlotClicked", CustomGuiEvent.SlotClickEvent.class);
	}

	@Override
	public void save() {
		super.save();
		BlockPos pos = this.script.getPos();
		Client.sendData(EnumPacketServer.ScriptBlockDataSave, pos.getX(), pos.getY(), pos.getZ(),
				this.script.getNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.script.setNBT(compound);
		super.setGuiData(compound);
	}

}
