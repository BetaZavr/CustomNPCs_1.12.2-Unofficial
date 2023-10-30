package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class MarkupData {
	
	public int id= -1;
	public float buy = 0.0f, sell = 0.04f;
	
	public MarkupData(int id, float buy, float sell) {
		this.id = id;
		this.buy = buy;
		this.sell = sell;
	}
	
	public MarkupData(NBTTagCompound data) {
		this.setNBT(data);
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound data = new NBTTagCompound();
		data.setInteger("slot", this.id);
		data.setFloat("buy", this.buy);
		data.setFloat("sell", this.sell);
		return data;
	}

	public void setNBT(NBTTagCompound data) {
		this.id = data.getInteger("slot");
		this.buy = data.getFloat("buy");
		this.sell = data.getFloat("sell");
	}
	
}
