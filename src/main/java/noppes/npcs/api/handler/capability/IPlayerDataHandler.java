package noppes.npcs.api.handler.capability;

import net.minecraft.nbt.NBTTagCompound;

public interface IPlayerDataHandler {

	NBTTagCompound getNBT();

	void setNBT(NBTTagCompound compound);

}
