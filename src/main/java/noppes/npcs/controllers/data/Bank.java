package noppes.npcs.controllers.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.containers.ContainerNPCBank;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.PlayerDataController;

public class Bank {

	public static class CeilSettings {

		public ItemStack openStack = ItemStack.EMPTY;
		public ItemStack upgradeStack = ItemStack.EMPTY;
		public int openMoney = 0;
		public int upgradeMoney = 0;
		public int ceil = 0;
		public int startCells = 1;
		public int maxCells = 27;

		public CeilSettings() {}

		public CeilSettings(NBTTagCompound nbtCeil) {
			this.read(nbtCeil);
		}

		public void read(NBTTagCompound nbtCeil) {
			if (nbtCeil.hasKey("CeilCurrency", 10)) {
				openStack = new ItemStack(nbtCeil.getCompoundTag("CeilCurrency"));
			} else {
				openStack = ItemStack.EMPTY;
			}
			if (nbtCeil.hasKey("CeilUpgrade", 10)) {
				upgradeStack = new ItemStack(nbtCeil.getCompoundTag("CeilUpgrade"));
			} else {
				upgradeStack = ItemStack.EMPTY;
			}
			startCells = nbtCeil.getInteger("StartCeil");
			maxCells = nbtCeil.getInteger("MaxCeil");
			ceil = nbtCeil.getInteger("CeilId");
			upgradeMoney = nbtCeil.getInteger("CeilUpgradeMoney");
			openMoney = nbtCeil.getInteger("CeilCurrencyMoney");
		}

		public void set(CeilSettings settings) {
			openStack = settings.openStack;
			upgradeStack = settings.upgradeStack;
			openMoney = settings.openMoney;
			upgradeMoney = settings.upgradeMoney;
			startCells = settings.startCells;
			maxCells = settings.maxCells;
		}

		public void writeTo(NBTTagCompound nbtCeil) {
			if (openStack != null && !openStack.isEmpty()) {
				nbtCeil.setTag("CeilCurrency", openStack.writeToNBT(new NBTTagCompound()));
			}
			if (upgradeStack != null && !upgradeStack.isEmpty()) {
				nbtCeil.setTag("CeilUpgrade", upgradeStack.writeToNBT(new NBTTagCompound()));
			}
			nbtCeil.setInteger("StartCeil", startCells);
			nbtCeil.setInteger("MaxCeil", maxCells);
			nbtCeil.setInteger("CeilId", ceil);
			nbtCeil.setInteger("CeilUpgradeMoney", upgradeMoney);
			nbtCeil.setInteger("CeilCurrencyMoney", openMoney);
		}
	}

	public final Map<Integer, CeilSettings> ceilSettings = new TreeMap<>();
	public final List<String> access = new ArrayList<>();
	public boolean isPublic = false;
	public boolean isWhiteList = false;
	public boolean isChanging = true;
	public int id = -1;

	public String name = "Default Bank";
	public String owner = "";

	private BankData lastBank;

	public Bank() {
		for (int ceil = 0; ceil < 2; ceil++) {
			CeilSettings cs = new CeilSettings();
			cs.ceil = ceil;
			if (ceil == 1) {
				cs.startCells = 9;
				cs.maxCells = 27;
				cs.openStack = new ItemStack(Items.DIAMOND, 1, 0);
				cs.upgradeStack = new ItemStack(Items.GOLD_INGOT, 2, 0);
			} else {
				cs.startCells = 27;
				cs.maxCells = 54;
				cs.upgradeStack = new ItemStack(Items.GOLD_INGOT, 1, 0);
			}
			ceilSettings.put(ceil, cs);
		}
	}

	public CeilSettings addCeil() {
		CeilSettings cs = new CeilSettings();
		cs.ceil = this.ceilSettings.size();
		this.ceilSettings.put(cs.ceil, cs);
		if (CustomNpcs.Server != null) {
			boolean save = false;
			for (String username : CustomNpcs.Server.getOnlinePlayerNames()) {
				EntityPlayerMP player = CustomNpcs.Server.getPlayerList().getPlayerByUsername(username);
				PlayerData data = PlayerData.get(player);
				if (player != null && player.openContainer instanceof ContainerNPCBank
						&& ((ContainerNPCBank) player.openContainer).bank.id == this.id) {
					player.closeContainer();
					if (!this.isPublic || !save) {
						BankData bd = PlayerData.get(player).bankData.get(this.id);
						if (bd != null) {
							bd.save();
						}
						save = true;
					}
					player.sendMessage(new TextComponentTranslation("message.bank.changed"));
				}
				if (data.bankData.lastBank != null && data.bankData.lastBank.bank.id == this.id) {
					data.bankData.lastBank = null;
				}
			}
			if (this.isPublic) {
				File banksDir = CustomNpcs.getWorldSaveDirectory("banks");
				File fileBank = new File(banksDir, this.id + ".dat");
				BankData bd = new BankData(this, "");
				try {
					bd.readNBT(CompressedStreamTools.readCompressed(Files.newInputStream(fileBank.toPath())));
				} catch (IOException e) { LogWriter.error("Error:", e); }
				if (!bd.cells.containsKey(cs.ceil)) {
					bd.cells.put(cs.ceil, new NpcMiscInventory(0));
					bd.save();
				}
			} else {
				File datasDir = CustomNpcs.getWorldSaveDirectory("playerdata");
				if (datasDir == null) { return null; }
				for (File playerDir : Objects.requireNonNull(datasDir.listFiles())) {
					if (!playerDir.isDirectory()) {
						continue;
					}
					for (File banksDir : Objects.requireNonNull(playerDir.listFiles())) {
						if (!banksDir.isDirectory() || !banksDir.getName().equals("banks")) {
							continue;
						}
						File fileBank = new File(banksDir, this.id + ".dat");
						BankData bd = new BankData(this, playerDir.getName());
						try {
							bd.readNBT(CompressedStreamTools.readCompressed(Files.newInputStream(fileBank.toPath())));
						} catch (IOException e) { LogWriter.error("Error:", e); }
						if (!bd.cells.containsKey(cs.ceil)) {
							bd.cells.put(cs.ceil, new NpcMiscInventory(0));
							bd.save();
						}
					}
				}
			}
		}
		return cs;
	}

	public void clearBankData() {
		this.lastBank = null;
	} // BankController.update()

	@SuppressWarnings("all")
	public BankData getBankData() {
		if (this.lastBank != null) {
			return this.lastBank;
		}
		File dir = CustomNpcs.getWorldSaveDirectory("banks");
		File file = new File(dir, this.id + ".dat");
		this.lastBank = new BankData(this, "");
		if (!file.exists()) { // create new
			try {
				file.createNewFile();
				CompressedStreamTools.writeCompressed(this.lastBank.getNBT(), Files.newOutputStream(file.toPath()));
			} catch (Exception e) {
				LogWriter.error("Error:", e);
				this.lastBank = null;
				return null;
			}
		}
		// load
		try {
			this.lastBank.setNBT(CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath())));
		} catch (IOException e) {
			LogWriter.error("Error:", e);
			this.lastBank = null;
			return null;
		}
		if (this.lastBank.cells.isEmpty()) {
			this.lastBank.clear();
		}
		return this.lastBank;
	}

	public boolean hasBankData() {
		return this.lastBank != null;
	}

	public void readFromNBT(NBTTagCompound nbtBank) {
		this.id = nbtBank.getInteger("BankID");
		this.name = nbtBank.getString("Username");
		this.ceilSettings.clear();
		this.access.clear();

		String pldOwner = this.owner;
		if (nbtBank.hasKey("StartSlots", 3)) {
			isPublic = false;
			isWhiteList = false;
			isChanging = true;
			int maxCells = nbtBank.getInteger("MaxSlots");
			NpcMiscInventory oldCInv = new NpcMiscInventory(maxCells);
			NpcMiscInventory oldUInv = new NpcMiscInventory(maxCells);
			oldCInv.load(nbtBank.getCompoundTag("BankCurrency"));
			oldUInv.load(nbtBank.getCompoundTag("BankUpgrade"));
			for (int ceil = 0; ceil < oldCInv.getSizeInventory(); ceil++) {
				CeilSettings cs = new CeilSettings();
				cs.ceil = ceil;
				cs.openStack = oldCInv.getStackInSlot(ceil);
				cs.upgradeStack = oldUInv.getStackInSlot(ceil);
				cs.upgradeStack.setCount(1);
				cs.startCells = 27;
				cs.maxCells = cs.upgradeStack.isEmpty() ? 27 : 54;
				this.ceilSettings.put(ceil, cs);
			}
		} else {
			NBTTagList list = nbtBank.getTagList("BankCells", 10);
			if (list.tagCount() ==0 && nbtBank.hasKey("BankCeils", 9)) { // old typo
				list = nbtBank.getTagList("BankCeils", 10);
			}
			for (int ceil = 0; ceil < list.tagCount(); ceil++) {
				this.ceilSettings.put(ceil, new CeilSettings(list.getCompoundTagAt(ceil)));
			}
			this.isPublic = nbtBank.getBoolean("IsPublic");
			this.isWhiteList = nbtBank.getBoolean("IsWhiteList");
			if (nbtBank.hasKey("IsChanging", 1)) { isChanging = nbtBank.getBoolean("IsChanging"); }
			this.owner = nbtBank.getString("Owner");
			list = nbtBank.getTagList("BankNamesPlayersAccess", 8);
			for (int i = 0; i < list.tagCount(); i++) {
				this.access.add(list.getStringTagAt(i));
			}
		}
		PlayerDataController pData = PlayerDataController.instance;
		if (pData != null) {
			List<String> names = PlayerDataController.instance.getPlayerNames();
			if (!this.owner.isEmpty()) {
				if (!names.contains(this.owner)) {
					boolean notFound = true;
					for (String name : names) {
						if (name.equalsIgnoreCase(this.owner)) {
							this.owner = name;
							notFound = false;
							break;
						}
					}
					if (notFound) {
						this.owner = pldOwner;
					}
				}
			}
			if (!this.access.isEmpty()) {
				List<String> newAccess = new ArrayList<>();
				boolean isChanged = false;
				for (String ac : this.access) {
					if (!names.contains(ac)) {
						for (String name : names) {
							if (name.equalsIgnoreCase(ac)) {
								newAccess.add(name);
								isChanged = true;
								break;
							}
						}
						continue;
					}
					newAccess.add(ac);
				}
				if (this.access.size() != newAccess.size() || isChanged) {
					this.access.clear();
					this.access.addAll(newAccess);
				}
			}
			if (!this.access.isEmpty()) {
				Collections.sort(this.access);
			}
		}
	}

	public void removeCeil(int ceilId) {
		if (!this.ceilSettings.containsKey(ceilId)) {
			return;
		}
		if (CustomNpcs.Server != null) {
			boolean save = false;
			for (String username : CustomNpcs.Server.getOnlinePlayerNames()) {
				EntityPlayerMP player = CustomNpcs.Server.getPlayerList().getPlayerByUsername(username);
				PlayerData data = PlayerData.get(player);
				if (player != null && player.openContainer instanceof ContainerNPCBank
						&& ((ContainerNPCBank) player.openContainer).bank.id == this.id) {
					player.closeContainer();
					if (!this.isPublic || !save) {
						BankData bd = PlayerData.get(player).bankData.get(this.id);
						if (bd != null) {
							bd.save();
						}
						save = true;
					}
					player.sendMessage(new TextComponentTranslation("message.bank.changed"));
				}
				if (data.bankData.lastBank != null && data.bankData.lastBank.bank.id == this.id) {
					data.bankData.lastBank = null;
				}
			}
			if (this.isPublic) {
				File banksDir = CustomNpcs.getWorldSaveDirectory("banks");
				File fileBank = new File(banksDir, this.id + ".dat");
				BankData bd = new BankData(this, "");
				try {
					bd.readNBT(CompressedStreamTools.readCompressed(Files.newInputStream(fileBank.toPath())));
				} catch (IOException e) { LogWriter.error("Error:", e); }
				Map<Integer, NpcMiscInventory> newCells = new TreeMap<>();
				int i = 0;
				for (NpcMiscInventory inv : bd.cells.values()) {
					if (i == ceilId) {
						continue;
					}
					newCells.put(i, inv);
					i++;
				}
				bd.cells.clear();
				bd.cells.putAll(newCells);
				bd.save();
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
						File fileBank = new File(banksDir, this.id + ".dat");
						BankData bd = new BankData(this, playerDir.getName());
						try {
							bd.readNBT(CompressedStreamTools.readCompressed(Files.newInputStream(fileBank.toPath())));
						} catch (IOException e) { LogWriter.error("Error:", e); }
						Map<Integer, NpcMiscInventory> newCells = new TreeMap<>();
						int i = 0;
						for (int c : bd.cells.keySet()) {
							if (c == ceilId) {
								continue;
							}
							newCells.put(i, bd.cells.get(c));
							i++;
						}
						bd.cells.clear();
						bd.cells.putAll(newCells);
						bd.save();
					}
				}
			}
		}
		Map<Integer, CeilSettings> newCS = new TreeMap<>();
		int i = 0;
		for (int c : this.ceilSettings.keySet()) {
			if (c == ceilId || this.ceilSettings.get(c).ceil == ceilId) {
				continue;
			}
			this.ceilSettings.get(c).ceil = i;
			newCS.put(i, this.ceilSettings.get(c));
			i++;
		}
		this.ceilSettings.clear();
		this.ceilSettings.putAll(newCS);
		BankController.getInstance().saveBank(this);
	}

	public void writeToNBT(NBTTagCompound nbtBank) {
		nbtBank.setInteger("BankID", this.id);
		nbtBank.setString("Username", this.name);
		nbtBank.setBoolean("IsPublic", this.isPublic);
		nbtBank.setBoolean("IsWhiteList", this.isWhiteList);
		nbtBank.setBoolean("IsChanging", this.isChanging);
		nbtBank.setString("Owner", this.owner);
		if (this.name.isEmpty()) {
			this.name = "Default Bank";
		}

		NBTTagList listCS = new NBTTagList();
		for (int ceil = 0; ceil < this.ceilSettings.size(); ++ceil) {
			NBTTagCompound nbtCeil = new NBTTagCompound();
			nbtCeil.setInteger("Ceil", ceil);
			this.ceilSettings.get(ceil).writeTo(nbtCeil);
			listCS.appendTag(nbtCeil);
		}
		nbtBank.setTag("BankCells", listCS);

		NBTTagList listNPA = new NBTTagList();
		for (String n : this.access) {
			listNPA.appendTag(new NBTTagString(n));
		}
		nbtBank.setTag("BankNamesPlayersAccess", listNPA);
	}

}
