package noppes.npcs;

import net.minecraft.nbt.NBTTagCompound;

public interface ICompatibilty {

	int getVersion();

	void setVersion(int version);

	NBTTagCompound save(NBTTagCompound nbt);

}
