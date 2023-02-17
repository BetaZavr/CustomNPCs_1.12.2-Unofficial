package noppes.npcs.controllers.data;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.NpcMiscInventory;

public class Bank {
	public NpcMiscInventory currencyInventory;
	public int id;
	public int maxSlots;
	public String name;
	public HashMap<Integer, Integer> slotTypes;
	public int startSlots;
	public NpcMiscInventory upgradeInventory;

	public Bank() {
		this.id = -1;
		this.name = "";
		this.startSlots = 1;
		this.maxSlots = 6;
		this.slotTypes = new HashMap<Integer, Integer>();
		this.currencyInventory = new NpcMiscInventory(6);
		this.upgradeInventory = new NpcMiscInventory(6);
		for (int i = 0; i < 6; ++i) {
			this.slotTypes.put(i, 0);
		}
	}

	public boolean canBeUpgraded(int slot) {
		return this.upgradeInventory.getStackInSlot(slot) != null
				&& !this.upgradeInventory.getStackInSlot(slot).isEmpty()
				&& (this.slotTypes.get(slot) == null || this.slotTypes.get(slot) == 0);
	}

	public int getMaxSlots() {
		for (int i = 0; i < this.maxSlots; ++i) {
			if ((this.currencyInventory.getStackInSlot(i) == null || this.currencyInventory.getStackInSlot(i).isEmpty())
					&& i > this.startSlots - 1) {
				return i;
			}
		}
		return this.maxSlots;
	}

	public boolean isUpgraded(int slot) {
		return this.slotTypes.get(slot) != null && this.slotTypes.get(slot) == 2;
	}

	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		this.id = nbttagcompound.getInteger("BankID");
		this.name = nbttagcompound.getString("Username");
		this.startSlots = nbttagcompound.getInteger("StartSlots");
		this.maxSlots = nbttagcompound.getInteger("MaxSlots");
		this.slotTypes = NBTTags.getIntegerIntegerMap(nbttagcompound.getTagList("BankTypes", 10));
		this.currencyInventory.setFromNBT(nbttagcompound.getCompoundTag("BankCurrency"));
		this.upgradeInventory.setFromNBT(nbttagcompound.getCompoundTag("BankUpgrade"));
	}

	public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("BankID", this.id);
		nbttagcompound.setTag("BankCurrency", this.currencyInventory.getToNBT());
		nbttagcompound.setTag("BankUpgrade", this.upgradeInventory.getToNBT());
		nbttagcompound.setString("Username", this.name);
		nbttagcompound.setInteger("MaxSlots", this.maxSlots);
		nbttagcompound.setInteger("StartSlots", this.startSlots);
		nbttagcompound.setTag("BankTypes", NBTTags.nbtIntegerIntegerMap(this.slotTypes));
	}
}
