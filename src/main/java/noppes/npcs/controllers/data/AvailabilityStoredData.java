package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumAvailabilityStoredData;

public class AvailabilityStoredData {

	public String key;
	public String value;
	public EnumAvailabilityStoredData type;

	public AvailabilityStoredData(NBTTagCompound nbt) {
		key = nbt.getString("Key");
		value = nbt.getString("Value");
		if (nbt.hasKey("Has", 1)) {
			if (nbt.getBoolean("Has")) { type = EnumAvailabilityStoredData.ONLY; }
			else { type = EnumAvailabilityStoredData.EXCEPT; }
		} else {
			int t = nbt.getInteger("Type");
			if (t < 0) { t *= -1; }
			type = EnumAvailabilityStoredData.values()[t % EnumAvailabilityStoredData.values().length];
		}
	}

	public AvailabilityStoredData(String k, String v, EnumAvailabilityStoredData t) {
		key = k;
		value = v;
		type = t;
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("Key", this.key);
		nbt.setString("Value", this.value);
		nbt.setInteger("Type", this.type.ordinal());
		return nbt;
	}
	
}
