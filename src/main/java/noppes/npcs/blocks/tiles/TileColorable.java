package noppes.npcs.blocks.tiles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class TileColorable extends TileNpcEntity {
	public int color;
	public int rotation;

	public TileColorable() {
		this.color = 14;
	}

	public boolean canUpdate() {
		return false;
	}

	public AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(this.pos.getX(), this.pos.getY(), this.pos.getZ(), (this.pos.getX() + 1),
				(this.pos.getY() + 1), (this.pos.getZ() + 1));
	}

	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	public NBTTagCompound getUpdateTag() {
		NBTTagCompound compound = new NBTTagCompound();
		this.writeToNBT(compound);
		compound.removeTag("Items");
		compound.removeTag("ExtraData");
		return compound;
	}

	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		NBTTagCompound compound = pkt.getNbtCompound();
		this.readFromNBT(compound);
	}

	public int powerProvided() {
		return 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.color = compound.getInteger("BannerColor");
		this.rotation = compound.getInteger("BannerRotation");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("BannerColor", this.color);
		compound.setInteger("BannerRotation", this.rotation);
		return super.writeToNBT(compound);
	}
}
