package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

public class MarkupData {

	public int id = -1, level = 0, xp = 0;
	public float buy = 0.0f, sell = 0.04f;

	public MarkupData(int level, float buy, float sell, int xp) {
		this.level = level;
		this.buy = buy;
		this.sell = sell;
		this.xp = xp;
	}

	public MarkupData(int id, int level, int xp) {
		this.id = id;
		this.level = level;
		this.xp = xp;
	}

	public MarkupData(NBTTagCompound data) {
		this.setNBT(data);
	}

	public void addXP(int xp) {
		this.xp += xp;
		if (this.xp < 0) {
			this.xp = 0;
		}
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("level", this.level);
		data.setInteger("xp", this.xp);
		data.setFloat("buy", this.buy);
		data.setFloat("sell", this.sell);
		return data;
	}

	public NBTBase getPlayerNBT() {
		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("id", this.id);
		data.setInteger("xp", this.xp);
		data.setInteger("level", this.level);
		return data;
	}

	public void setNBT(NBTTagCompound data) {
		this.level = data.getInteger("level");
		this.xp = data.getInteger("xp");
		this.buy = data.getFloat("buy");
		this.sell = data.getFloat("sell");
	}
}
