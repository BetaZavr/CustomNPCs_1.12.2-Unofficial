package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.controllers.data.Bank;

public class BankController {
	
	private static BankController instance;

	public static BankController getInstance() {
		if (newInstance()) {
			BankController.instance = new BankController();
		}
		return BankController.instance;
	}

	private static boolean newInstance() {
		if (BankController.instance == null) {
			return true;
		}
		File file = CustomNpcs.getWorldSaveDirectory();
		return file != null && !BankController.instance.filePath.equals(file.getAbsolutePath());
	}

	public HashMap<Integer, Bank> banks;

	private String filePath;

	public BankController() {
		this.filePath = "";
		BankController.instance = this;
		this.banks = new HashMap<Integer, Bank>();
		this.loadBanks();
		if (this.banks.isEmpty()) {
			Bank bank = new Bank();
			bank.id = 0;
			bank.name = "Default Bank";
			for (int i = 0; i < 6; ++i) {
				bank.slotTypes.put(i, 0);
			}
			this.banks.put(bank.id, bank);
		}
	}

	public Bank getBank(int bankId) {
		Bank bank = this.banks.get(bankId);
		if (bank != null) {
			return bank;
		}
		return this.banks.values().iterator().next();
	}

	public NBTTagCompound getNBT() {
		NBTTagList list = new NBTTagList();
		for (Bank bank : this.banks.values()) {
			NBTTagCompound nbtfactions = new NBTTagCompound();
			bank.writeEntityToNBT(nbtfactions);
			list.appendTag(nbtfactions);
		}
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setTag("Data", list);
		return nbttagcompound;
	}

	public int getUnusedId() {
		int id;
		for (id = 0; this.banks.containsKey(id); ++id) {
		}
		return id;
	}

	private void loadBanks() {
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if (saveDir == null) {
			return;
		}
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.startDebug("Common", null, "loadBanks");
		}
		this.filePath = saveDir.getAbsolutePath();
		try {
			File file = new File(saveDir, "bank.dat");
			if (file.exists()) {
				this.loadBanks(file);
			}
		} catch (Exception e) {
			try {
				File file2 = new File(saveDir, "bank.dat_old");
				if (file2.exists()) {
					this.loadBanks(file2);
				}
			} catch (Exception ex) {
			}
		}
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.endDebug("Common", null, "loadBanks");
		}
	}

	private void loadBanks(File file) throws IOException {
		this.loadBanks(CompressedStreamTools.readCompressed(new FileInputStream(file)));
	}

	public void loadBanks(NBTTagCompound nbttagcompound1) throws IOException {
		HashMap<Integer, Bank> banks = new HashMap<Integer, Bank>();
		NBTTagList list = nbttagcompound1.getTagList("Data", 10);
		if (list != null) {
			for (int i = 0; i < list.tagCount(); ++i) {
				NBTTagCompound nbttagcompound2 = list.getCompoundTagAt(i);
				Bank bank = new Bank();
				bank.readEntityFromNBT(nbttagcompound2);
				banks.put(bank.id, bank);
			}
		}
		this.banks = banks;
	}

	public void removeBank(int bank) {
		if (bank < 0 || this.banks.size() <= 1) {
			return;
		}
		this.banks.remove(bank);
		this.saveBanks();
	}

	public void saveBank(Bank bank) {
		if (bank.id < 0) {
			bank.id = this.getUnusedId();
		}
		this.banks.put(bank.id, bank);
		this.saveBanks();
	}

	public void saveBanks() {
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			File file = new File(saveDir, "bank.dat_new");
			File file2 = new File(saveDir, "bank.dat_old");
			File file3 = new File(saveDir, "bank.dat");
			CompressedStreamTools.writeCompressed(this.getNBT(), (OutputStream) new FileOutputStream(file));
			if (file2.exists()) {
				file2.delete();
			}
			file3.renameTo(file2);
			if (file3.exists()) {
				file3.delete();
			}
			file.renameTo(file3);
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
