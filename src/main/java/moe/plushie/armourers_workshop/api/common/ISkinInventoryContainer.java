package moe.plushie.armourers_workshop.api.common;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ISkinInventoryContainer {

	public void clear();

	public void dropItems(World world, Vec3d pos);

	public void readFromNBT(NBTTagCompound compound);

	public void writeToNBT(NBTTagCompound compound);
}
