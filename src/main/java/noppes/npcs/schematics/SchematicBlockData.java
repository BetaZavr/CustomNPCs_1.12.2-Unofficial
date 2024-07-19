package noppes.npcs.schematics;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.util.CustomNPCsScheduler;

import java.util.Objects;

public class SchematicBlockData {

	public BlockPos pos;
	public IBlockState state;
	public NBTTagCompound nbtTile;
	public World world;
	public int meta = 0, id = 0;

	public SchematicBlockData(World world, IBlockState state, BlockPos pos) {
		this.world = world;
		this.pos = pos;
		this.state = state;
		this.meta = this.state.getBlock().getMetaFromState(state);
		this.nbtTile = null;
		if (state.getBlock() instanceof ITileEntityProvider && world != null && world.getTileEntity(pos) != null) {
			this.nbtTile = new NBTTagCompound();
			Objects.requireNonNull(world.getTileEntity(pos)).writeToNBT(this.nbtTile);
		}
	}


	public SchematicBlockData(World world, ItemStack stack) {
		this.world = world;
		this.pos = null;
		Block b = Block.getBlockFromItem(stack.getItem());
		this.state = b.getDefaultState();
		if (stack.getItemDamage() < b.getBlockState().getValidStates().size()) {
			this.state = b.getStateFromMeta(stack.getItemDamage());
		}
		this.nbtTile = null;
		if (stack.hasTagCompound()) {
            assert stack.getTagCompound() != null;
            this.nbtTile = stack.getTagCompound().copy();
		}
	}

	public void set(BlockPos pos) {
		if (this.world == null || pos == null || this.state == null) {
			return;
		}
		this.world.setBlockState(pos, this.state);
		if (this.nbtTile != null) {
			this.nbtTile.setInteger("x", pos.getX());
			this.nbtTile.setInteger("y", pos.getY());
			this.nbtTile.setInteger("z", pos.getZ());
			CustomNPCsScheduler.runTack(() -> {
				TileEntity tile = this.world.getTileEntity(this.pos);
				if (tile == null) {
					tile = this.state.getBlock().createTileEntity(this.world, this.state);
				}
                assert tile != null;
                tile.readFromNBT(this.nbtTile);
				this.nbtTile.setInteger("x", this.pos.getX());
				this.nbtTile.setInteger("y", this.pos.getY());
				this.nbtTile.setInteger("z", this.pos.getZ());
			}, 200);
		}
	}

	public void setMeta(int meta) {
		this.meta = meta;
		if (meta < this.state.getBlock().getBlockState().getValidStates().size()) {
			this.state = this.state.getBlock().getBlockState().getValidStates().get(meta);
		}
	}

	public String toString() {
        return "SchematicBlockData [ ID:" + this.id + "; state:" + this.state + "," + "; pos:" + this.pos
				+ "; meta:" + this.meta + "; hasNbt:" + (this.nbtTile != null) + " ]";
	}

}
