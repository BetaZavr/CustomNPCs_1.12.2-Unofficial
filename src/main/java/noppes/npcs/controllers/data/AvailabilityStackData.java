package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.constants.EnumAvailabilityStackData;
import noppes.npcs.util.ValueUtil;

public class AvailabilityStackData {

    public EnumAvailabilityStackData type = EnumAvailabilityStackData.Always;
    public boolean ignoreNBT = false;
    public boolean ignoreDamage = false;

    public AvailabilityStackData() {}

    public AvailabilityStackData(NBTTagCompound compound) {
        int i = ValueUtil.correctInt(compound.getInteger("type"), 0, EnumAvailabilityStackData.values().length - 1);
        type = EnumAvailabilityStackData.values()[i];
        ignoreNBT = compound.getBoolean("ignoreNBT");
        ignoreDamage = compound.getBoolean("ignoreDamage");
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("type", type.ordinal());
        compound.setBoolean("ignoreNBT", ignoreNBT);
        compound.setBoolean("ignoreDamage", ignoreDamage);
        return compound;
    }

}
