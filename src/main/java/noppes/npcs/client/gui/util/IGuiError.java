package noppes.npcs.client.gui.util;

import net.minecraft.nbt.NBTTagCompound;

public interface IGuiError {
	
	void setError(int type, NBTTagCompound nbt);
	
}
