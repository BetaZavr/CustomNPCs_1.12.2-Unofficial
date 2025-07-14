package noppes.npcs.blocks.tiles;

import java.util.ArrayList;
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

import javax.annotation.Nonnull;

@SuppressWarnings("rawtypes")
public class TileBorder
extends TileNpcEntity
implements Predicate, ITickable {

	public Availability availability = new Availability();
	public boolean creative = false;
	public int height = 10;
	public int rotation = 0;
	public String message = "availability.areaNotAvailable";

	public boolean apply(Object ob) {
		return isEntityApplicable((Entity) ob);
	}

	private boolean checkPlayer(EntityPlayer player, int startY) {
		if ((player.capabilities.isCreativeMode && !creative) || availability.isAvailable(player)) {
			return false;
		}
		BlockPos newPos = new BlockPos(pos.getX(), startY, pos.getZ());
		if (rotation == 2) {
			newPos = newPos.south();
		} else if (rotation == 0) {
			newPos = newPos.north();
		} else if (rotation == 1) {
			newPos = newPos.east();
		} else if (rotation == 3) {
			newPos = newPos.west();
		}
		int i = startY - pos.getY();
		while (i < height && (!world.isAirBlock(newPos) || !world.isAirBlock(newPos.up()))) {
			newPos = newPos.up();
			i++;
		}
		player.setPositionAndUpdate(newPos.getX() + 0.5, newPos.getY(), newPos.getZ() + 0.5);
		if (!message.isEmpty()) {
			player.sendStatusMessage(new TextComponentTranslation(message), true);
		}
		return true;
	}

	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
	}

	public @Nonnull NBTTagCompound getUpdateTag() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("x", pos.getX());
		compound.setInteger("y", pos.getY());
		compound.setInteger("z", pos.getZ());
		compound.setInteger("Rotation", rotation);
		compound.setInteger("Height", height);
		return compound;
	}

	public void handleUpdateTag(@Nonnull NBTTagCompound compound) {
		rotation = compound.getInteger("Rotation");
		height = compound.getInteger("Height");
	}

	public void onDataPacket(@Nonnull NetworkManager net, @Nonnull SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}

	public boolean isEntityApplicable(Entity var1) {
		return var1 instanceof EntityPlayerMP || var1 instanceof EntityEnderPearl;
	}

	public void readExtraNBT(NBTTagCompound compound) {
		availability.readFromNBT(compound.getCompoundTag("BorderAvailability"));
		rotation = compound.getInteger("BorderRotation");
		height = compound.getInteger("BorderHeight");
		message = compound.getString("BorderMessage");
		creative = compound.getBoolean("Bordercreative");
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound compound) {
		super.readFromNBT(compound);
		readExtraNBT(compound);
		if (world != null) {
			world.setBlockState(pos, CustomRegisters.border.getDefaultState().withProperty(BlockBorder.ROTATION, rotation));
		}
    }
	@SuppressWarnings("unchecked")
	public void update() {
		if (world.isRemote) { return; }
		for (int i = 1; i < height && i < 3; i++) {
			if (world.getBlockState(pos.up(i)).getBlock() instanceof BlockBorder) {
				return;
			}
		}
		AxisAlignedBB box = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), (pos.getX() + 1), (pos.getY() + height + 1), (pos.getZ() + 1));
		List<Entity> list = new ArrayList<>();
		try {
			list = world.getEntitiesWithinAABB(Entity.class, box, this);
		}
		catch (Exception ignored) { }
		for (Entity entity : list) {
			if (entity instanceof EntityEnderPearl) {
				EntityEnderPearl pearl = (EntityEnderPearl) entity;
				if (pearl.getThrower() instanceof EntityPlayer) {
					entity.isDead = checkPlayer((EntityPlayer) pearl.getThrower(), (int) (entity.posY + 0.5d));
				}
			} else if (entity instanceof EntityPlayer) {
				checkPlayer((EntityPlayer) entity, (int) (entity.posY + 0.5d));
			}
		}
	}

	public void writeExtraNBT(NBTTagCompound compound) {
		compound.setTag("BorderAvailability", availability.writeToNBT(new NBTTagCompound()));
		compound.setInteger("BorderRotation", rotation);
		compound.setInteger("BorderHeight", height);
		compound.setString("BorderMessage", message);
		compound.setBoolean("Bordercreative", creative);
	}

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
		writeExtraNBT(compound);
		return super.writeToNBT(compound);
	}

}
