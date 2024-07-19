package noppes.npcs.blocks.tiles;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.blocks.BlockNpcRedstone;
import noppes.npcs.controllers.data.Availability;

import javax.annotation.Nonnull;

public class TileRedstoneBlock extends TileNpcEntity implements ITickable {
	public Availability availability;
	public boolean isActivated;
	public boolean isDetailed;
	public int offRange;
	public int offRangeX;
	public int offRangeY;
	public int offRangeZ;
	public int onRange;
	public int onRangeX;
	public int onRangeY;
	public int onRangeZ;
	private int ticks;

	public TileRedstoneBlock() {
		this.onRange = 12;
		this.offRange = 20;
		this.onRangeX = 12;
		this.onRangeY = 12;
		this.onRangeZ = 12;
		this.offRangeX = 20;
		this.offRangeY = 20;
		this.offRangeZ = 20;
		this.isDetailed = false;
		this.availability = new Availability();
		this.isActivated = false;
		this.ticks = 10;
	}

	private List<EntityPlayer> getPlayerList(int x, int y, int z) {
		return this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.pos.getX(), this.pos.getY(),
				this.pos.getZ(), (this.pos.getX() + 1), (this.pos.getY() + 1), (this.pos.getZ() + 1)).grow(x, y, z));
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.onRange = compound.getInteger("BlockOnRange");
		this.offRange = compound.getInteger("BlockOffRange");
		this.isDetailed = compound.getBoolean("BlockIsDetailed");
		if (compound.hasKey("BlockOnRangeX")) {
			this.isDetailed = true;
			this.onRangeX = compound.getInteger("BlockOnRangeX");
			this.onRangeY = compound.getInteger("BlockOnRangeY");
			this.onRangeZ = compound.getInteger("BlockOnRangeZ");
			this.offRangeX = compound.getInteger("BlockOffRangeX");
			this.offRangeY = compound.getInteger("BlockOffRangeY");
			this.offRangeZ = compound.getInteger("BlockOffRangeZ");
		}
		if (compound.hasKey("BlockActivated")) {
			this.isActivated = compound.getBoolean("BlockActivated");
		}
		this.availability.readFromNBT(compound);
	}

	private void setActive(Block block, boolean bo) {
		this.isActivated = bo;
		IBlockState state = block.getDefaultState().withProperty(BlockNpcRedstone.ACTIVE, this.isActivated);
		this.world.setBlockState(this.pos, state, 2);
		this.markDirty();
		this.world.notifyBlockUpdate(this.pos, state, state, 3);
		block.onBlockAdded(this.world, this.pos, state);
	}

	public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	public void update() {
		if (this.world.isRemote) {
			return;
		}
		--this.ticks;
		if (this.ticks > 0) {
			return;
		}
		this.ticks = ((this.onRange > 10) ? 20 : 10);
		Block block = this.getBlockType();
		if (!(block instanceof BlockNpcRedstone)) {
			return;
		}
		if (CustomNpcs.FreezeNPCs) {
			if (this.isActivated) {
				this.setActive(block, false);
			}
			return;
		}
		if (!this.isActivated) {
			int x = this.isDetailed ? this.onRangeX : this.onRange;
			int y = this.isDetailed ? this.onRangeY : this.onRange;
			int z = this.isDetailed ? this.onRangeZ : this.onRange;
			List<EntityPlayer> list = this.getPlayerList(x, y, z);
			if (list.isEmpty()) {
				return;
			}
			for (EntityPlayer player : list) {
				if (this.availability.isAvailable(player)) {
					this.setActive(block, true);
				}
			}
		} else {
			int x = this.isDetailed ? this.offRangeX : this.offRange;
			int y = this.isDetailed ? this.offRangeY : this.offRange;
			int z = this.isDetailed ? this.offRangeZ : this.offRange;
			List<EntityPlayer> list = this.getPlayerList(x, y, z);
			for (EntityPlayer player : list) {
				if (this.availability.isAvailable(player)) {
					return;
				}
			}
			this.setActive(block, false);
		}
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
		compound.setInteger("BlockOnRange", this.onRange);
		compound.setInteger("BlockOffRange", this.offRange);
		compound.setBoolean("BlockActivated", this.isActivated);
		compound.setBoolean("BlockIsDetailed", this.isDetailed);
		if (this.isDetailed) {
			compound.setInteger("BlockOnRangeX", this.onRangeX);
			compound.setInteger("BlockOnRangeY", this.onRangeY);
			compound.setInteger("BlockOnRangeZ", this.onRangeZ);
			compound.setInteger("BlockOffRangeX", this.offRangeX);
			compound.setInteger("BlockOffRangeY", this.offRangeY);
			compound.setInteger("BlockOffRangeZ", this.offRangeZ);
		}
		this.availability.writeToNBT(compound);
		return super.writeToNBT(compound);
	}
}
