package noppes.npcs.api.item;

import net.minecraft.nbt.NBTTagCompound;

public interface ICustomItem {
	
	NBTTagCompound getData();
	
	String getCustomName();
	
}
