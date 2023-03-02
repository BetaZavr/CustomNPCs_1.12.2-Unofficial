package noppes.npcs.controllers.data;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.util.AdditionalMethods;

public class PlayerGameData {

	public boolean isMoved = false;
	public List<Integer> keyPress = Lists.<Integer>newArrayList();
	public List<Integer> mousePress = Lists.<Integer>newArrayList();
	public long money;
	public boolean update; // ServerTickHandler
	public double[] windowSize = new double[] { 0, 0 };
	private String currentLanguage = "en_us";
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

	public int[] getKeyPressed() {
		int[] ids = new int[this.keyPress.size()];
		int i = 0;
		for (int key : this.keyPress) {
			ids[i] = key;
			i++;
		}
		return ids;
	}

	public int[] getMousePressed() {
		int[] ids = new int[this.mousePress.size()];
		int i = 0;
		for (int key : this.mousePress) {
			ids[i] = key;
			i++;
		}
		return ids;
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("IsMoved", this.isMoved);
		NBTTagList list = new NBTTagList();
		list.appendTag(new NBTTagDouble(this.windowSize[0]));
		list.appendTag(new NBTTagDouble(this.windowSize[1]));
		compound.setTag("WindowSize", list);
		compound.setLong("Money", this.money);
		compound.setString("CurrentLanguage", this.currentLanguage);
		compound.setBoolean("IsOP", this.op);
		
		compound.setIntArray("KeyPress", this.getKeyPressed());
		compound.setIntArray("MousePress", this.getMousePressed());
		
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
		for (int k : this.keyPress) {
			if (k == key) { return true; }
		}
		return this.keyPress.contains((Integer) key) ;
	}
	
	public boolean hasOrKeysPressed(int[] keys) {
		for (int key : keys) {
			if (this.hasKeyPressed(key)) { return true; }
		}
		return false;
	}

	public boolean hasMousePress(int key) {
		for (int k : this.mousePress) {
			if (k == key) { return true; }
		}
		return this.mousePress.contains((Integer) key) ;
	}

	public boolean isMoved() {
		return this.isMoved;
	}

	public void readFromNBT(NBTTagCompound compound) {
		if (compound != null && compound.hasKey("GameData", 10)) {
			NBTTagCompound gameNBT = compound.getCompoundTag("GameData");
			for (int i = 0; i < 2 && i < gameNBT.getTagList("WindowSize", 6).tagCount(); i++) {
				this.windowSize[i] = gameNBT.getTagList("WindowSize", 6).getDoubleAt(i);
			}
			this.money = gameNBT.getLong("Money");
			this.currentLanguage = gameNBT.getString("CurrentLanguage");
			this.op = gameNBT.getBoolean("IsOP");
			
			int[] iK = gameNBT.getIntArray("KeyPress");
			int[] iM = gameNBT.getIntArray("MousePress");
			this.keyPress.clear();
			this.mousePress.clear();
			for (int key : iK) { this.keyPress.add(key); }
			for (int key : iM) { this.mousePress.add(key); }
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
