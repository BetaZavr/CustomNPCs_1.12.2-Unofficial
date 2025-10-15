package noppes.npcs.api.handler.capability;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.ParamName;

public interface IPlayerDataHandler {

	NBTTagCompound getNBT();

	void setNBT(@ParamName("compound") NBTTagCompound compound);

}
