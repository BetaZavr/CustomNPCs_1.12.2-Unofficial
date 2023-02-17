package noppes.npcs;

import net.minecraft.nbt.NBTTagCompound;

public interface ICompatibilty {
	int getVersion();

	void setVersion(int p0);

	NBTTagCompound writeToNBT(NBTTagCompound p0);
}
