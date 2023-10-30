package noppes.npcs.controllers.data;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.util.AdditionalMethods;

public class PlayerGameData {
	
	public long money;
	public boolean update; // ServerTickHandler
	public boolean op = false;
	private final Map<Integer, Integer> marketData = Maps.<Integer, Integer>newTreeMap(); // ID market, slot

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
		NBTTagList markup = new NBTTagList();
		for (int market : this.marketData.keySet()) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("MarketID", market);
			nbt.setInteger("Slot", this.marketData.get(market));
			markup.appendTag(nbt);
		}
		compound.setTag("MarketData", markup);
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
			if (gameNBT.hasKey("MarketData", 9)) {
				this.marketData.clear();
				for (int i = 0; i < gameNBT.getTagList("MarketData", 10).tagCount(); i++) {
					NBTTagCompound nbt = gameNBT.getTagList("MarketData", 10).getCompoundTagAt(i);
					this.marketData.put(nbt.getInteger("MarketID"), nbt.getInteger("Slot"));
				}
			}
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
	
	public int getMarcetSlot(int marketID) {
		if (this.marketData.containsKey(marketID)) { return this.marketData.get(marketID); }
		this.marketData.put(marketID, 0);
		return 0;
	}
	
	public void setMarcetSlot(int marketID, int slot) { this.marketData.put(marketID, slot); }
	
}
