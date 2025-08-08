package noppes.npcs.roles.data;

import net.minecraft.nbt.NBTTagCompound;

public class HealerSettings {

	public boolean onHimself = false;
	public boolean possibleOnMobs = true;
	public boolean isMassive = true;
	public int id;
	public int range;
	public int speed;
	public int time = 100;
	public int amplifier;
	public byte type; // 0-friendly, 1-unfriendly, 2-all

	public HealerSettings(int idIn, int rangeIn, int speedIn, int amplifierIn, byte typeIn) {
		id = idIn;
		range = rangeIn;
		type = typeIn;
		speed = speedIn;
		amplifier = amplifierIn;
	}

	public HealerSettings(NBTTagCompound nbtSet) {
		onHimself = nbtSet.getBoolean("OnHimself");
		possibleOnMobs = nbtSet.getBoolean("PossibleOnMobs");
		isMassive = nbtSet.getBoolean("IsMassive");
		type = nbtSet.getByte("Type");
		id = nbtSet.getInteger("PotionID");
		range = nbtSet.getInteger("Distance");
		speed = nbtSet.getInteger("Speed");
		time = nbtSet.getInteger("Time");
		amplifier = nbtSet.getInteger("Amplifier");
	}

	public NBTTagCompound writeNBT() {
		NBTTagCompound nbtSet = new NBTTagCompound();
		nbtSet.setBoolean("OnHimself", onHimself);
		nbtSet.setBoolean("PossibleOnMobs", possibleOnMobs);
		nbtSet.setBoolean("IsMassive", isMassive);
		nbtSet.setByte("Type", type);
		nbtSet.setInteger("PotionID", id);
		nbtSet.setInteger("Distance", range);
		nbtSet.setInteger("Speed", speed);
		nbtSet.setInteger("Time", time);
		nbtSet.setInteger("Amplifier", amplifier);
		return nbtSet;
	}

}
