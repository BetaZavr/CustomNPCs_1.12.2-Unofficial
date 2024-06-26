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

public class SchematicBlockData {

	public BlockPos pos;
	public IBlockState state;
	public NBTTagCompound nbtTile;
	public World world;
	public int meta = 0, id = 0;

	public SchematicBlockData(SchematicBlockData bd) {
		this.world = bd.world;
		this.pos = bd.pos == null ? null : new BlockPos(bd.pos);
		this.state = bd.state;
		this.nbtTile = bd.nbtTile == null ? null : bd.nbtTile.copy();
	}

	public SchematicBlockData(World world, IBlockState state, BlockPos pos) {
		this.world = world;
		this.pos = pos;
		this.state = state;
		this.meta = this.state.getBlock().getMetaFromState(state);
		this.nbtTile = null;
		if (state.getBlock() instanceof ITileEntityProvider && world != null && world.getTileEntity(pos) != null) {
			this.nbtTile = new NBTTagCompound();
			world.getTileEntity(pos).writeToNBT(this.nbtTile);
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
				tile.readFromNBT(this.nbtTile);
				this.nbtTile.setInteger("x", this.pos.getX());
				this.nbtTile.setInteger("y", this.pos.getY());
				this.nbtTile.setInteger("z", this.pos.getZ());
			}, 200);
		}
	}

	/*
	 * public NBTTagCompound getNbt() { NBTTagCompound nbtBlocks = new
	 * NBTTagCompound(); if (this.world!=null) { nbtBlocks.setInteger("DimensionID",
	 * this.world.provider.getDimension()); } if (this.pos!=null) {
	 * nbtBlocks.setIntArray("Pos", new int[] { this.pos.getX(), this.pos.getY(),
	 * this.pos.getZ() }); } if (this.state!=null) { nbtBlocks.setInteger("Meta",
	 * this.state.getBlock().getMetaFromState(this.state));
	 * nbtBlocks.setString("Name",
	 * this.state.getBlock().getRegistryName().toString()); }
	 * nbtBlocks.setBoolean("HasNBT", this.nbtTile!=null); if (this.nbtTile!=null) {
	 * nbtBlocks.setTag("TileNBT", this.nbtTile); } return nbtBlocks; }
	 * 
	 * public void read(NBTTagCompound nbtBlocks) { int[] p =
	 * nbtBlocks.getIntArray("Pos"); if (p.length==3) { this.pos = new
	 * BlockPos(p[0], p[1], p[2]); } Block b =
	 * Block.getBlockFromName(nbtBlocks.getString("Name")); if (b!=null) {
	 * this.state = b.getStateFromMeta(nbtBlocks.getInteger("Meta")); } this.nbtTile
	 * = null; if (nbtBlocks.getBoolean("HasNBT")) { this.nbtTile =
	 * nbtBlocks.getCompoundTag("TileNBT"); } if (CustomNpcs.Server!=null) { for
	 * (WorldServer world : CustomNpcs.Server.worlds) { if
	 * (world.provider.getDimension() == nbtBlocks.getInteger("DimensionID")) {
	 * this.world = world; break; } } } }
	 */

	public void setMeta(int meta) {
		this.meta = meta;
		if (meta < this.state.getBlock().getBlockState().getValidStates().size()) {
			this.state = this.state.getBlock().getBlockState().getValidStates().get(meta);
		}
	}

	public String toString() {
		String text = "SchematicBlockData [ ID:" + this.id + "; state:" + this.state + "," + "; pos:" + this.pos
				+ "; meta:" + this.meta + "; hasNbt:" + (this.nbtTile != null) + " ]";
		return text;
	}

}
