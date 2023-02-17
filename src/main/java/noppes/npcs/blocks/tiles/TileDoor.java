package noppes.npcs.blocks.tiles;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;

public class TileDoor extends TileNpcEntity implements ITickable {
	public Block blockModel;
	public boolean needsClientUpdate;
	public TileEntity renderTile;
	public boolean renderTileErrored;
	public ITickable renderTileUpdate;
	public int ticksExisted;

	public TileDoor() {
		this.ticksExisted = 0;
		this.blockModel = CustomItems.scriptedDoor;
		this.needsClientUpdate = false;
		this.renderTileErrored = true;
		this.renderTileUpdate = null;
	}

	public NBTTagCompound getDoorNBT(NBTTagCompound compound) {
		compound.setString("ScriptDoorBlockModel", Block.REGISTRY.getNameForObject(this.blockModel) + "");
		return compound;
	}

	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	public NBTTagCompound getUpdateTag() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("x", this.pos.getX());
		compound.setInteger("y", this.pos.getY());
		compound.setInteger("z", this.pos.getZ());
		this.getDoorNBT(compound);
		return compound;
	}

	public void handleUpdateTag(NBTTagCompound compound) {
		this.setDoorNBT(compound);
	}

	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.handleUpdateTag(pkt.getNbtCompound());
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.setDoorNBT(compound);
	}

	public void setDoorNBT(NBTTagCompound compound) {
		this.blockModel = Block.REGISTRY.getObject(new ResourceLocation(compound.getString("ScriptDoorBlockModel")));
		if (this.blockModel == null || !(this.blockModel instanceof BlockDoor)) {
			this.blockModel = CustomItems.scriptedDoor;
		}
		this.renderTileUpdate = null;
		this.renderTile = null;
		this.renderTileErrored = false;
	}

	public void setItemModel(Block block) {
		if (block == null || !(block instanceof BlockDoor)) {
			block = CustomItems.scriptedDoor;
		}
		if (this.blockModel == block) {
			return;
		}
		this.blockModel = block;
		this.needsClientUpdate = true;
	}

	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}

	public void update() {
		if (this.renderTileUpdate != null) {
			try {
				this.renderTileUpdate.update();
			} catch (Exception e) {
				this.renderTileUpdate = null;
			}
		}
		++this.ticksExisted;
		if (this.ticksExisted >= 10) {
			this.ticksExisted = 0;
			if (this.needsClientUpdate) {
				this.markDirty();
				IBlockState state = this.world.getBlockState(this.pos);
				this.world.notifyBlockUpdate(this.pos, state, state, 3);
				this.needsClientUpdate = false;
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		this.getDoorNBT(compound);
		return super.writeToNBT(compound);
	}
}
