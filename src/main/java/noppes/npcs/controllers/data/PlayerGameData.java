package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.util.AdditionalMethods;

public class PlayerGameData {
	
	public long money;
	public boolean update; // ServerTickHandler
	public boolean op = false;

	public void addMoney(long money) {
		this.money += money;
		if (this.money < 0L) {
			this.money = 0L;
		} else if (this.money > Long.MAX_VALUE) {
			this.money = Long.MAX_VALUE;
		}
		this.update = true;
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setLong("Money", this.money);
		compound.setBoolean("IsOP", this.op);
		return compound;
	}

	public String getTextMoney() {
		return AdditionalMethods.getTextReducedNumber(this.money, true, true, false);
	}

	public void readFromNBT(NBTTagCompound compound) {
		if (compound != null && compound.hasKey("GameData", 10)) {
			NBTTagCompound gameNBT = compound.getCompoundTag("GameData");
			this.money = gameNBT.getLong("Money");
			this.op = gameNBT.getBoolean("IsOP");
		}
	}

	public NBTTagCompound saveNBTData(NBTTagCompound compound) {
		compound.setTag("GameData", this.getNBT());
		return compound;
	}

	public void setMoney(long money) {
		if (money < 0L) {
			money = 0L;
		} else if (money > Long.MAX_VALUE) {
			money = Long.MAX_VALUE;
		}
		this.money = money;
		this.update = true;
	}

}
