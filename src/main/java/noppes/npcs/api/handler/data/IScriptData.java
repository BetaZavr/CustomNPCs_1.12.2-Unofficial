package noppes.npcs.api.handler.data;

import net.minecraft.nbt.NBTTagCompound;

public interface IScriptData {

	String getName();

	NBTTagCompound getNBT();

	Object getObject();

	int getType();

	String getValue();

	void load(NBTTagCompound nbt);

}
