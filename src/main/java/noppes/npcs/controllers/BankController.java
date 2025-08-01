package noppes.npcs.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Objects;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.containers.ContainerNPCBank;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.Bank.CeilSettings;
import noppes.npcs.controllers.data.BankData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.util.Util;

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
		this.banks = new HashMap<>();
		this.loadBanks();
		if (this.banks.isEmpty()) {
			Bank bank = new Bank();
			bank.id = 0;
			bank.name = "Default Bank";
			this.banks.put(bank.id, bank);
		}
	}

	public void change(Bank bank) {
		if (bank == null || !this.banks.containsKey(bank.id) || CustomNpcs.Server == null) {
			return;
		}
		for (String username : CustomNpcs.Server.getOnlinePlayerNames()) {
			EntityPlayerMP player = CustomNpcs.Server.getPlayerList().getPlayerByUsername(username);
			PlayerData data = PlayerData.get(player);
			if (player != null && player.openContainer instanceof ContainerNPCBank
					&& ((ContainerNPCBank) player.openContainer).bank.id == bank.id) {
				player.closeContainer();
				player.sendMessage(new TextComponentTranslation("message.bank.changed"));
			}
			if (data.bankData.lastBank != null && data.bankData.lastBank.bank.id == bank.id) {
				data.bankData.lastBank = null;
			}
		}
		if (bank.isPublic) {
			File banksDir = CustomNpcs.getWorldSaveDirectory("banks");
			File fileBank = new File(banksDir, bank.id + ".dat");
			BankData bd = new BankData(bank, "");
			try {
				bd.readNBT(CompressedStreamTools.readCompressed(Files.newInputStream(fileBank.toPath())));
			} catch (IOException e) { LogWriter.error(e); }
			boolean isChange = false;
			for (int c : bd.cells.keySet()) {
				if (!bank.ceilSettings.containsKey(c)) {
					isChange = true;
					break;
				}
				NpcMiscInventory inv = bd.cells.get(c);
				CeilSettings cs = bank.ceilSettings.get(c);
				if (inv.getSizeInventory() < cs.startCells) {
					bd.cells.put(c, new NpcMiscInventory(cs.openStack.isEmpty() ? cs.startCells : 0).fill(inv));
					isChange = true;
				} else if (inv.getSizeInventory() > cs.maxCells) {
					bd.cells.put(c, new NpcMiscInventory(cs.maxCells).fill(inv));
					isChange = true;
				}
			}
			if (isChange) {
				try {
					CompressedStreamTools.writeCompressed(bd.getNBT(), Files.newOutputStream(fileBank.toPath()));
				} catch (Exception e) { LogWriter.error(e); }
			}
		} else {
			File datasDir = CustomNpcs.getWorldSaveDirectory("playerdata");
            if (datasDir == null) { return; }
            for (File playerDir : Objects.requireNonNull(datasDir.listFiles())) {
				if (!playerDir.isDirectory()) {
					continue;
				}
				for (File banksDir : Objects.requireNonNull(playerDir.listFiles())) {
					if (!banksDir.isDirectory() || !banksDir.getName().equals("banks")) {
						continue;
					}
					File fileBank = new File(banksDir, bank.id + ".dat");
					if (fileBank.exists()) {
						BankData bd = new BankData(bank, "");
						try {
							bd.readNBT(CompressedStreamTools.readCompressed(Files.newInputStream(fileBank.toPath())));
						} catch (IOException e) { LogWriter.error(e); }
						boolean isChange = false;
						for (int c : bd.cells.keySet()) {
							if (!bank.ceilSettings.containsKey(c)) {
								isChange = true;
								bd.cells.remove(c);
								continue;
							}
							NpcMiscInventory inv = bd.cells.get(c);
							CeilSettings cs = bank.ceilSettings.get(c);
							if (inv.getSizeInventory() < cs.startCells) {
								bd.cells.put(c,
										new NpcMiscInventory(cs.openStack.isEmpty() ? cs.startCells : 0).fill(inv));
								isChange = true;
							} else if (inv.getSizeInventory() > cs.maxCells) {
								bd.cells.put(c, new NpcMiscInventory(cs.maxCells).fill(inv));
								isChange = true;
							}
						}
						if (isChange) {
							try {
								CompressedStreamTools.writeCompressed(bd.getNBT(), Files.newOutputStream(fileBank.toPath()));
							} catch (Exception e) { LogWriter.error(e); }
						}
					}
				}
			}
		}
	}

	public Bank getBank(int bankId) {
		if (this.banks.containsKey(bankId)) {
			return this.banks.get(bankId);
		}
		for (Bank bank : this.banks.values()) {
			if (bank.id == bankId) {
				return bank;
			}
		}
		return null;
	}

	public NBTTagCompound getNBT() {
		NBTTagList list = new NBTTagList();
		for (Bank bank : this.banks.values()) {
			NBTTagCompound nbtfactions = new NBTTagCompound();
			bank.writeToNBT(nbtfactions);
			list.appendTag(nbtfactions);
		}
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setTag("Data", list);
		return nbttagcompound;
	}

	public int getUnusedId() {
		int id = 0;
		while (this.banks.containsKey(id)) { id++; }
		return id;
	}

	private void loadBanks() {
		CustomNpcs.debugData.start(null);
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if (saveDir == null) {
			CustomNpcs.debugData.end(null);
			return;
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
			} catch (Exception ex) { LogWriter.error(ex); }
		}
		CustomNpcs.debugData.end(null);
	}

	private void loadBanks(File file) throws IOException {
		this.loadBanks(CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath())));
	}

	public void loadBanks(NBTTagCompound compound) {
		HashMap<Integer, Bank> banks = new HashMap<>();
		NBTTagList list = compound.getTagList("Data", 10);
        for (int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound nbtBank = list.getCompoundTagAt(i);
            Bank bank = new Bank();
            bank.readFromNBT(nbtBank);
            banks.put(bank.id, bank);
        }
        this.banks = banks;
	}

	public void removeBank(int bankId) {
		if (!this.banks.containsKey(bankId)) {
			return;
		}
		if (CustomNpcs.Server != null) {
			for (String username : CustomNpcs.Server.getOnlinePlayerNames()) {
				EntityPlayerMP player = CustomNpcs.Server.getPlayerList().getPlayerByUsername(username);
				PlayerData data = PlayerData.get(player);
				if (player != null && player.openContainer instanceof ContainerNPCBank
						&& ((ContainerNPCBank) player.openContainer).bank.id == bankId) {
					player.closeContainer();
					player.sendMessage(new TextComponentTranslation("message.bank.changed"));
				}
				if (data.bankData.lastBank != null && data.bankData.lastBank.bank.id == bankId) {
					data.bankData.lastBank = null;
				}
			}
			if (this.banks.get(bankId).isPublic) {
				File banksDir = CustomNpcs.getWorldSaveDirectory("banks");
				File fileBank = new File(banksDir, bankId + ".dat");
				if (fileBank.exists()) {
					Util.instance.removeFile(fileBank);
				}
			} else {
				File datasDir = CustomNpcs.getWorldSaveDirectory("playerdata");
				if (datasDir == null) { return; }
                for (File playerDir : Objects.requireNonNull(datasDir.listFiles())) {
					if (!playerDir.isDirectory()) {
						continue;
					}
					for (File banksDir : Objects.requireNonNull(playerDir.listFiles())) {
						if (!banksDir.isDirectory() || !banksDir.getName().equals("banks")) {
							continue;
						}
						File fileBank = new File(banksDir, bankId + ".dat");
						if (fileBank.exists()) {
							Util.instance.removeFile(fileBank);
						}
					}
				}
			}
		}
		this.banks.remove(bankId);
		this.saveBanks();
	}

	public void saveBank(Bank bank) {
		if (bank.id < 0) {
			bank.id = this.getUnusedId();
		}
		this.banks.put(bank.id, bank);
		this.saveBanks();
	}

	@SuppressWarnings("all")
	public void saveBanks() {
		CustomNpcs.debugData.start(null);
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			File file = new File(saveDir, "bank.dat_new");
			File file2 = new File(saveDir, "bank.dat_old");
			File file3 = new File(saveDir, "bank.dat");
			CompressedStreamTools.writeCompressed(this.getNBT(), Files.newOutputStream(file.toPath()));
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
		}
		catch (Exception e) { LogWriter.error(e); }
		CustomNpcs.debugData.end(null);
	}

	public void update() { // every 5 min --> ServerTickHandler.onServerTick()
		for (Bank bank : this.banks.values()) {
			if (bank.hasBankData()) {
				bank.getBankData().save();
				if (CustomNpcs.Server != null) {
					boolean clear = true;
					for (EntityPlayerMP player : CustomNpcs.Server.getPlayerList().getPlayers()) {
						if (player.openContainer instanceof ContainerNPCBank
								&& ((ContainerNPCBank) player.openContainer).bank.id == bank.id) {
							clear = false;
							break;
						}
					}
					if (clear) {
						bank.clearBankData();
					}
				}
			}
		}
	}

}
