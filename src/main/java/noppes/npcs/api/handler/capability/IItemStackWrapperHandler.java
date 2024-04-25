package noppes.npcs.api.handler.capability;

import net.minecraft.nbt.NBTTagCompound;

public interface IItemStackWrapperHandler {

	NBTTagCompound getMCNbt();

	void setMCNbt(NBTTagCompound compound);

}
