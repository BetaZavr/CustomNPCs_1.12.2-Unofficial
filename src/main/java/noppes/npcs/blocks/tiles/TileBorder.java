package noppes.npcs.blocks.tiles;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomRegisters;
import noppes.npcs.blocks.BlockBorder;
import noppes.npcs.controllers.data.Availability;

@SuppressWarnings("rawtypes")
public class TileBorder
extends TileNpcEntity
implements Predicate, ITickable {
	
	public Availability availability;
	public AxisAlignedBB boundingbox;
	public int height;
	public String message;
	public int rotation;

	public TileBorder() {
		this.availability = new Availability();
		this.rotation = 0;
		this.height = 10;
		this.message = "availability.areaNotAvailble";
	}

	public boolean apply(Object ob) {
		return this.isEntityApplicable((Entity) ob);
	}

	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 0, this.getUpdateTag());
	}

	public NBTTagCompound getUpdateTag() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("x", this.pos.getX());
		compound.setInteger("y", this.pos.getY());
		compound.setInteger("z", this.pos.getZ());
		compound.setInteger("Rotation", this.rotation);
		return compound;
	}

	public void handleUpdateTag(NBTTagCompound compound) {
		this.rotation = compound.getInteger("Rotation");
	}

	public boolean isEntityApplicable(Entity var1) {
		return var1 instanceof EntityPlayerMP || var1 instanceof EntityEnderPearl;
	}

	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		this.handleUpdateTag(pkt.getNbtCompound());
	}

	public void readExtraNBT(NBTTagCompound compound) {
		this.availability.readFromNBT(compound.getCompoundTag("BorderAvailability"));
		this.rotation = compound.getInteger("BorderRotation");
		this.height = compound.getInteger("BorderHeight");
		this.message = compound.getString("BorderMessage");
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.readExtraNBT(compound);
		if (this.getWorld() != null) {
			this.getWorld().setBlockState(this.getPos(), CustomRegisters.border.getDefaultState().withProperty(BlockBorder.ROTATION, this.rotation));
		}
	}

	public void update() {
		if (this.world.isRemote) { return; }
		for (int i = 1; i < this.height && i < 3; i++) {
			if (this.world.getBlockState(this.pos.up(i)).getBlock() instanceof BlockBorder) {
				return;
			}
		}
		AxisAlignedBB box = new AxisAlignedBB(this.pos.getX(), this.pos.getY(), this.pos.getZ(), (this.pos.getX() + 1), (this.pos.getY() + this.height + 1), (this.pos.getZ() + 1));
		@SuppressWarnings("unchecked")
		List<Entity> list = this.world.getEntitiesWithinAABB(Entity.class, box, (Predicate) this);
		for (Entity entity : list) {
			if (entity instanceof EntityEnderPearl) {
				EntityEnderPearl pearl = (EntityEnderPearl) entity;
				if (pearl.getThrower() instanceof EntityPlayer) {
					entity.isDead = this.cheakPlayer((EntityPlayer) pearl.getThrower(), (int) (entity.posY + 0.5d));
				}
			} else if (entity instanceof EntityPlayer) {
				this.cheakPlayer((EntityPlayer) entity, (int) (entity.posY + 0.5d));
			}
		}
	}

	private boolean cheakPlayer(EntityPlayer player, int startY) {
		if (player.capabilities.isCreativeMode || this.availability.isAvailable(player)) { return false; }
		BlockPos newPos = new BlockPos(this.pos.getX(), startY, this.pos.getZ());
		if (this.rotation == 2) { newPos = newPos.south(); }
		else if (this.rotation == 0) { newPos = newPos.north(); }
		else if (this.rotation == 1) { newPos = newPos.east(); }
		else if (this.rotation == 3) { newPos = newPos.west(); }
		int i = startY - this.pos.getY();
		while (i < this.height && (!this.world.isAirBlock(newPos) || !this.world.isAirBlock(newPos.up()))) {
			newPos = newPos.up();
			i++;
		}
		player.setPositionAndUpdate(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5);
		if (!this.message.isEmpty()) {
			player.sendStatusMessage(new TextComponentTranslation(this.message, new Object[0]), true);
		}
		return true;
	}

	public void writeExtraNBT(NBTTagCompound compound) {
		compound.setTag("BorderAvailability", this.availability.writeToNBT(new NBTTagCompound()));
		compound.setInteger("BorderRotation", this.rotation);
		compound.setInteger("BorderHeight", this.height);
		compound.setString("BorderMessage", this.message);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		this.writeExtraNBT(compound);
		return super.writeToNBT(compound);
	}
}
