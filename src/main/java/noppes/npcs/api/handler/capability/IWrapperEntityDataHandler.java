package noppes.npcs.api.handler.capability;

import net.minecraft.nbt.NBTTagCompound;

public interface IWrapperEntityDataHandler {

	NBTTagCompound getNBT();

	void setNBT(NBTTagCompound compound);
}
