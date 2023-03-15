package noppes.npcs.api.wrapper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.block.IBlockScriptedDoor;
import noppes.npcs.blocks.tiles.TileScriptedDoor;

public class BlockScriptedDoorWrapper
extends BlockWrapper
implements IBlockScriptedDoor {
	
	private TileScriptedDoor tile;

	public BlockScriptedDoorWrapper(World world, Block block, BlockPos pos) {
		super(world, block, pos);
		this.tile = (TileScriptedDoor) super.tile;
	}

	@Override
	public String getBlockModel() {
		return Block.REGISTRY.getNameForObject(this.tile.blockModel) + "";
	}

	@Override
	public float getHardness() {
		return this.tile.blockHardness;
	}

	@Override
	public boolean getOpen() {
		IBlockState state = this.world.getMCWorld().getBlockState(this.pos);
		return ((Boolean) state.getValue(BlockDoor.OPEN)).equals(true);
	}

	@Override
	public float getResistance() {
		return this.tile.blockResistance;
	}

	@Override
	public ITimers getTimers() {
		return this.tile.timers;
	}

	@Override
	public void setBlockModel(String name) {
		Block b = null;
		if (name != null) {
			b = Block.getBlockFromName(name);
		}
		this.tile.setItemModel(b);
	}

	@Override
	public void setHardness(float hardness) {
		this.tile.blockHardness = hardness;
	}

	@Override
	public void setOpen(boolean open) {
		if (this.getOpen() == open || this.isRemoved()) {
			return;
		}
		((BlockDoor) this.block).toggleDoor(this.world.getMCWorld(), this.pos, open);
	}

	@Override
	public void setResistance(float resistance) {
		this.tile.blockResistance = resistance;
	}

	@Override
	protected void setTile(TileEntity tile) {
		this.tile = (TileScriptedDoor) tile;
		super.setTile(tile);
	}
}
