package noppes.npcs.roles.data;

import net.minecraft.nbt.NBTTagCompound;

public class HealerSettings {
	
	public boolean onHimself, possibleOnMobs, isMassive;
	public int id, range, speed, time, amplifier;
	public byte type;

	public HealerSettings(int id, int range, int speed, int amplifier, byte type) {
		this.id = id;
		this.range = range;
		this.amplifier = 0;
		this.type = type;
		this.speed = speed;
		this.time = 100;
		this.onHimself = false;
		this.possibleOnMobs = true;
		this.isMassive = true;
	}
	
	public HealerSettings() {
		this.id = 1;
		this.range = 8;
		this.amplifier = 0;
		this.type = (byte) 2; // 0-friendly, 1-unfriendly, 2-all
		this.speed = 10;
		this.time = 100;
		this.onHimself = false;
		this.possibleOnMobs = true;
		this.isMassive = true;
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
