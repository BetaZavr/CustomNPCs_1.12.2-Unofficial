package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class AvailabilityStoredData {

	public String key, value;
	public boolean has;

	public AvailabilityStoredData(NBTTagCompound nbt) {
		key = nbt.getString("Key");
		value = nbt.getString("Value");
		has = nbt.getBoolean("Has");
	}

	public AvailabilityStoredData(String k, String v, boolean b) {
		key = k;
		value = v;
		has = b;
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("Key", this.key);
		nbt.setString("Value", this.value);
		nbt.setBoolean("Has", this.has);
		return nbt;
	}

}
