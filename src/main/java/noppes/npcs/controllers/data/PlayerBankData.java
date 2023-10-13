package noppes.npcs.controllers.data;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.controllers.BankController;

public class PlayerBankData {
	
	public HashMap<Integer, BankData> banks;
	public String editPlayer = "";

	public PlayerBankData() {
		this.banks = new HashMap<Integer, BankData>();
	}

	public BankData getBank(int bankId) {
		return this.banks.get(bankId);
	}

	public BankData getBankOrDefault(int bankId) {
		BankData data = this.banks.get(bankId);
		if (data != null) {
			return data;
		}
		Bank bank = BankController.getInstance().getBank(bankId);
		return this.banks.get(bank.id);
	}

	public boolean hasBank(int bank) {
		return this.banks.containsKey(bank);
	}

	public void loadNBTData(NBTTagCompound compound) {
		HashMap<Integer, BankData> banks = new HashMap<Integer, BankData>();
		NBTTagList list = compound.getTagList("BankData", 10);
		if (list == null) {
			return;
		}
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
			BankData data = new BankData();
			data.readNBT(nbttagcompound);
			banks.put(data.bankId, data);
		}
		this.banks = banks;
		if (compound.hasKey("PlayerName", 8)) {
			this.editPlayer  = compound.getString("PlayerName");
		}
	}

	public void loadNew(int bank) {
		BankData data = new BankData();
		data.bankId = bank;
		this.banks.put(bank, data);
	}

	public void saveNBTData(NBTTagCompound playerData) {
		NBTTagList list = new NBTTagList();
		for (BankData data : this.banks.values()) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			data.writeNBT(nbttagcompound);
			list.appendTag(nbttagcompound);
		}
		playerData.setTag("BankData", list);
	}
}
