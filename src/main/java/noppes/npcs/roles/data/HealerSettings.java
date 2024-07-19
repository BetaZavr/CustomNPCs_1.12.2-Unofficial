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

	public HealerSettings(int id, int range, int speed, int amplifier, byte type) {
		this.id = id;
		this.range = range;
		this.type = type;
		this.speed = speed;
		this.amplifier = amplifier;
	}

	public HealerSettings(NBTTagCompound nbtSet) {
		this.onHimself = nbtSet.getBoolean("OnHimself");
		this.possibleOnMobs = nbtSet.getBoolean("PossibleOnMobs");
		this.isMassive = nbtSet.getBoolean("IsMassive");
		this.type = nbtSet.getByte("Type");
		this.id = nbtSet.getInteger("PotionID");
		this.range = nbtSet.getInteger("Distance");
		this.speed = nbtSet.getInteger("Speed");
		this.time = nbtSet.getInteger("Time");
		this.amplifier = nbtSet.getInteger("Amplifier");
	}

	public NBTTagCompound writeNBT() {
		NBTTagCompound nbtSet = new NBTTagCompound();
		nbtSet.setBoolean("OnHimself", this.onHimself);
		nbtSet.setBoolean("PossibleOnMobs", this.possibleOnMobs);
		nbtSet.setBoolean("IsMassive", this.isMassive);
		nbtSet.setByte("Type", this.type);
		nbtSet.setInteger("PotionID", this.id);
		nbtSet.setInteger("Distance", this.range);
		nbtSet.setInteger("Speed", this.speed);
		nbtSet.setInteger("Time", this.time);
		nbtSet.setInteger("Amplifier", this.amplifier);
		return nbtSet;
	}

}
