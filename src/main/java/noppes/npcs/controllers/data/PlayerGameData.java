package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.util.AdditionalMethods;

public class PlayerGameData {

	public boolean isMoved = false;
	public NBTTagList keyPress = new NBTTagList();
	public long money;
	public NBTTagList mousePress = new NBTTagList();
	public boolean update; // ServerTickHandler
	public double[] windowSize = new double[] { 0, 0 };
	private String currentLanguage = "en_us";

	public void addMoney(long money) {
		this.money += money;
		if (this.money < 0L) {
			this.money = 0L;
		} else if (this.money > Long.MAX_VALUE) {
			this.money = Long.MAX_VALUE;
		}
		this.update = true;
	}

	public int[] getKeyPressed() {
		int[] ids = new int[this.keyPress.tagCount()];
		for (int k = 0; k < this.keyPress.tagCount(); k++) {
			ids[k] = this.keyPress.getIntAt(k);
		}
		return ids;
	}

	public int[] getMousePressed() {
		int[] ids = new int[this.keyPress.tagCount()];
		for (int k = 0; k < this.keyPress.tagCount(); k++) {
			ids[k] = this.keyPress.getIntAt(k);
		}
		return ids;
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("IsMoved", this.isMoved);
		compound.setTag("KeyPress", this.keyPress);
		compound.setTag("MousePress", this.mousePress);
		NBTTagList list = new NBTTagList();
		list.appendTag(new NBTTagDouble(this.windowSize[0]));
		list.appendTag(new NBTTagDouble(this.windowSize[1]));
		compound.setTag("WindowSize", list);
		compound.setLong("Money", this.money);
		compound.setString("currentLanguage", this.currentLanguage);
		return compound;
	}

	public String getCurrentLanguage() {
		return this.currentLanguage;
	}

	public String getTextMoney() {
		return AdditionalMethods.getTextReducedNumber(this.money, true, true, false);
	}

	public double[] getWindowSize() {
		return this.windowSize;
	}

	public boolean hasKeyPressed(int key) {
		for (NBTBase k : this.keyPress) {
			if (((NBTTagInt) k).getInt() == key) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasOrKeysPressed(int[] keys) {
		for (NBTBase k : this.keyPress) {
			for (int i=0; i<keys.length; i++) {
				if (((NBTTagInt) k).getInt() == keys[i]) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean hasMousePress(int key) {
		for (NBTBase k : this.mousePress) {
			if (((NBTTagInt) k).getInt() == key) {
				return true;
			}
		}
		return false;
	}

	public boolean isMoved() {
		return this.isMoved;
	}

	public void readFromNBT(NBTTagCompound compound) {
		if (compound != null && compound.hasKey("GameData", 10)) {
			NBTTagCompound comTemp = compound.getCompoundTag("GameData");
			this.keyPress = comTemp.getTagList("KeyPress", 3);
			this.mousePress = comTemp.getTagList("MousePress", 3);
			for (int i = 0; i < 2 && i < comTemp.getTagList("WindowSize", 6).tagCount(); i++) {
				this.windowSize[i] = comTemp.getTagList("WindowSize", 6).getDoubleAt(i);
			}
			this.money = comTemp.getLong("Money");
			this.currentLanguage = compound.getString("currentLanguage");
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

	public void setMoved(boolean moved) {
		this.isMoved = moved;
	}

}
