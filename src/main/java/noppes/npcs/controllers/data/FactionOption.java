package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class FactionOption {

	public boolean decreaseFactionPoints = false;
	public int factionId = -1;
	public int factionPoints = 100;

	public FactionOption() {

	}

	public FactionOption(int factionId, int factionPoints, boolean take) {
		this.factionId = factionId;
		this.factionPoints = factionPoints;
		this.decreaseFactionPoints = take;
	}

	public FactionOption(NBTTagCompound compound) {
		this.readFromNBT(compound);
	}

	public void cheak() {
		if (this.factionPoints < 0) {
			this.factionPoints *= -1;
			if (this.decreaseFactionPoints) {
				this.decreaseFactionPoints = false;
			} else {
				this.decreaseFactionPoints = true;
			}
		}
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.factionId = compound.getInteger("FactionID");
		this.decreaseFactionPoints = compound.getBoolean("IsDecrease");
		this.factionPoints = compound.getInteger("Points");
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("FactionID", this.factionId);
		compound.setBoolean("IsDecrease", this.decreaseFactionPoints);
		compound.setInteger("Points", this.factionPoints);
		return compound;
	}
}
