package noppes.npcs.blocks.tiles;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.blocks.BlockNpcRedstone;
import noppes.npcs.controllers.data.Availability;

import javax.annotation.Nonnull;

public class TileRedstoneBlock
extends TileNpcEntity
implements ITickable {

	public Availability availability = new Availability();
	public boolean isActivated = false;
	public boolean isDetailed = false;
	public int offRange = 20;
	public int offRangeX = 20;
	public int offRangeY = 20;
	public int offRangeZ = 20;
	public int onRange = 12;
	public int onRangeX = 12;
	public int onRangeY = 12;
	public int onRangeZ = 12;
	private int ticks = 10;

	private List<EntityPlayer> getPlayerList(int x, int y, int z) {
		return world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), (pos.getX() + 1), (pos.getY() + 1), (pos.getZ() + 1)).grow(x, y, z));
	}

	public @Nonnull NBTTagCompound getUpdateTag() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("x", pos.getX());
		compound.setInteger("y", pos.getY());
		compound.setInteger("z", pos.getZ());
		compound.setInteger("OffRange", offRange);
		compound.setInteger("OffRangeX", offRangeX);
		compound.setInteger("OffRangeY", offRangeY);
		compound.setInteger("OffRangeZ", offRangeZ);
		compound.setInteger("OnRange", onRange);
		compound.setInteger("OnRangeX", onRangeX);
		compound.setInteger("OnRangeY", onRangeY);
		compound.setInteger("OnRangeZ", onRangeZ);
		compound.setBoolean("IsDetailed", isDetailed);
		return compound;
	}

	public void handleUpdateTag(@Nonnull NBTTagCompound compound) {
		offRange = compound.getInteger("OffRange");
		offRangeX = compound.getInteger("OffRangeX");
		offRangeY = compound.getInteger("OffRangeY");
		offRangeZ = compound.getInteger("OffRangeZ");
		onRange = compound.getInteger("OnRange");
		onRangeX = compound.getInteger("OnRangeX");
		onRangeY = compound.getInteger("OnRangeY");
		onRangeZ = compound.getInteger("OnRangeZ");
		isDetailed = compound.getBoolean("IsDetailed");
	}

	public void onDataPacket(@Nonnull NetworkManager net, @Nonnull SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound compound) {
		super.readFromNBT(compound);
		onRange = compound.getInteger("BlockOnRange");
		offRange = compound.getInteger("BlockOffRange");
		isDetailed = compound.getBoolean("BlockIsDetailed");
		onRangeX = compound.getInteger("BlockOnRangeX");
		onRangeY = compound.getInteger("BlockOnRangeY");
		onRangeZ = compound.getInteger("BlockOnRangeZ");
		offRangeX = compound.getInteger("BlockOffRangeX");
		offRangeY = compound.getInteger("BlockOffRangeY");
		offRangeZ = compound.getInteger("BlockOffRangeZ");
		if (compound.hasKey("BlockActivated")) { isActivated = compound.getBoolean("BlockActivated"); }
		availability.readFromNBT(compound);
	}

	private void setActive(Block block, boolean bo) {
		isActivated = bo;
		IBlockState state = block.getDefaultState().withProperty(BlockNpcRedstone.ACTIVE, isActivated);
		world.setBlockState(pos, state, 2);
		markDirty();
		world.notifyBlockUpdate(pos, state, state, 3);
		block.onBlockAdded(world, pos, state);
	}

	public boolean shouldRefresh(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState oldState, @Nonnull IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	public void update() {
		if (world.isRemote) {
			return;
		}
		--ticks;
		if (ticks > 0) {
			return;
		}
		ticks = ((onRange > 10) ? 20 : 10);
		Block block = getBlockType();
		if (!(block instanceof BlockNpcRedstone)) {
			return;
		}
		if (CustomNpcs.FreezeNPCs) {
			if (isActivated) {
				setActive(block, false);
			}
			return;
		}
		if (!isActivated) {
			int x = isDetailed ? onRangeX : onRange;
			int y = isDetailed ? onRangeY : onRange;
			int z = isDetailed ? onRangeZ : onRange;
			List<EntityPlayer> list = getPlayerList(x, y, z);
			if (list.isEmpty()) {
				return;
			}
			for (EntityPlayer player : list) {
				if (availability.isAvailable(player)) {
					setActive(block, true);
				}
			}
		} else {
			int x = isDetailed ? offRangeX : offRange;
			int y = isDetailed ? offRangeY : offRange;
			int z = isDetailed ? offRangeZ : offRange;
			List<EntityPlayer> list = getPlayerList(x, y, z);
			for (EntityPlayer player : list) {
				if (availability.isAvailable(player)) {
					return;
				}
			}
			setActive(block, false);
		}
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
		compound.setInteger("BlockOnRange", onRange);
		compound.setInteger("BlockOffRange", offRange);
		compound.setBoolean("BlockActivated", isActivated);
		compound.setBoolean("BlockIsDetailed", isDetailed);
		compound.setInteger("BlockOnRangeX", onRangeX);
		compound.setInteger("BlockOnRangeY", onRangeY);
		compound.setInteger("BlockOnRangeZ", onRangeZ);
		compound.setInteger("BlockOffRangeX", offRangeX);
		compound.setInteger("BlockOffRangeY", offRangeY);
		compound.setInteger("BlockOffRangeZ", offRangeZ);
		availability.writeToNBT(compound);
		return super.writeToNBT(compound);
	}
}
