package noppes.npcs.api.handler.capability;

import net.minecraft.nbt.NBTTagCompound;

public interface INbtHandler {

	NBTTagCompound getCapabilityNBT();

	void setCapabilityNBT(NBTTagCompound compound);

}
