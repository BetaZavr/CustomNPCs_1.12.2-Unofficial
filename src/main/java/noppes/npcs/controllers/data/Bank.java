package noppes.npcs.controllers.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.containers.ContainerNPCBank;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.PlayerDataController;

public class Bank {
	
	public final Map<Integer, CeilSettings> ceilSettings;
	public final List<String> access;
	public boolean isPublic, isWhiteList;
	public int id;
	public String name, owner;

	private BankData lastBank;
	
	public Bank() {
		this.id = -1;
		this.name = "Default Bank";
		this.owner = "";
		this.ceilSettings = Maps.<Integer, CeilSettings>newTreeMap();
		this.isPublic = false;
		this.isWhiteList = false;
		this.access = Lists.<String>newArrayList();
		for (int ceil = 0; ceil < 2; ceil++) {
			CeilSettings cs = new CeilSettings();
			cs.ceil = ceil;
			if (ceil == 1) {
				cs.startCeils = 9;
				cs.maxCeils = 27;
				cs.openStack = new ItemStack(Items.DIAMOND, 1, 0);
				cs.upgradeStack = new ItemStack(Items.GOLD_INGOT, 2, 0);
			}
			else {
				cs.startCeils = 27;
				cs.maxCeils = 54;
				cs.upgradeStack = new ItemStack(Items.GOLD_INGOT, 1, 0);
			}
			this.ceilSettings.put(ceil, cs);
		}
	}

	public int getMaxCeils() { return this.ceilSettings.size(); }

	public void readFromNBT(NBTTagCompound nbtBank) {
		this.id = nbtBank.getInteger("BankID");
		this.name = nbtBank.getString("Username");
		this.ceilSettings.clear();
		this.access.clear();
		
		String pldOwner = new String(this.owner);
		if (nbtBank.hasKey("StartSlots", 3)) {
			this.isPublic = false;
			this.isWhiteList = false;
			int maxCeils = nbtBank.getInteger("MaxSlots");
			NpcMiscInventory oldCInv = new NpcMiscInventory(maxCeils);
			NpcMiscInventory oldUInv = new NpcMiscInventory(maxCeils);
			oldCInv.setFromNBT(nbtBank.getCompoundTag("BankCurrency"));
			oldUInv.setFromNBT(nbtBank.getCompoundTag("BankUpgrade"));
			for (int ceil = 0; ceil < oldCInv.getSizeInventory(); ceil++) {
				CeilSettings cs = new CeilSettings();
				cs.ceil = ceil;
				cs.openStack = oldCInv.getStackInSlot(ceil);
				cs.upgradeStack = oldUInv.getStackInSlot(ceil);
				cs.upgradeStack.setCount(1);
				cs.startCeils = 27;
				cs.maxCeils = cs.upgradeStack.isEmpty() ? 27 : 54;
				this.ceilSettings.put(ceil, cs);
			}
		}
		else {
			NBTTagList list = nbtBank.getTagList("BankCeils", 10);
			for (int ceil = 0; ceil < list.tagCount(); ceil++) {
				this.ceilSettings.put(ceil, new CeilSettings(list.getCompoundTagAt(ceil)));
			}
			this.isPublic = nbtBank.getBoolean("IsPublic");
			this.isWhiteList = nbtBank.getBoolean("IsWhiteList");
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
					if (notFound) { this.owner = pldOwner; }
				}
			}
			if (!this.access.isEmpty()) {
				List<String> newAccess = Lists.<String>newArrayList();
				boolean isChanged = false;
				for (String acces : this.access) {
					if (!names.contains(acces)) {
						for (String name : names) {
							if (name.equalsIgnoreCase(acces)) {
								newAccess.add(name);
								isChanged = true;
								break;
							}
						}
						continue;
					}
					newAccess.add(acces);
				}
				if (this.access.size() != newAccess.size() || isChanged) {
					this.access.clear();
					this.access.addAll(newAccess);
				}
			}
			if (!this.access.isEmpty()) { Collections.sort(this.access); }
		}
	}

	public void writeToNBT(NBTTagCompound nbtBank) {
		nbtBank.setInteger("BankID", this.id);
		nbtBank.setString("Username", this.name);
		nbtBank.setBoolean("IsPublic", this.isPublic);
		nbtBank.setBoolean("IsWhiteList", this.isWhiteList);
		nbtBank.setString("Owner", this.owner);
		if (this.name.isEmpty()) { this.name = "Default Bank"; }
		
		NBTTagList listCS = new NBTTagList();
		for (int ceil = 0; ceil < this.ceilSettings.size(); ++ceil) {
			NBTTagCompound nbtCeil = new NBTTagCompound();
			nbtCeil.setInteger("Ceil", ceil);
			this.ceilSettings.get(ceil).writeTo(nbtCeil);
			listCS.appendTag(nbtCeil);
		}
		nbtBank.setTag("BankCeils", listCS);

		NBTTagList listNPA = new NBTTagList();
		for (String n : this.access) {
			listNPA.appendTag(new NBTTagString(n));
		}
		nbtBank.setTag("BankNamesPlayersAccess", listNPA);
	}

	public void removeCeil(int ceilId) {
		if (!this.ceilSettings.containsKey(ceilId)) { return; }
		if (CustomNpcs.Server!=null) {
			boolean save = false;
			for (String username : CustomNpcs.Server.getOnlinePlayerNames()) {
				EntityPlayerMP player = CustomNpcs.Server.getPlayerList().getPlayerByUsername(username);
				PlayerData data = PlayerData.get(player);
				if (player!=null && player.openContainer instanceof ContainerNPCBank && ((ContainerNPCBank) player.openContainer).bank.id == this.id) {
					player.closeContainer();
					if (!this.isPublic || !save) {
						BankData bd = PlayerData.get(player).bankData.get(this.id);
						if (bd != null) { bd.save(); }
						save = true;
					}
					player.sendMessage(new TextComponentTranslation("message.bank.changed"));
				}
				if (data.bankData.lastBank != null && data.bankData.lastBank.bank.id ==  this.id) {
					data.bankData.lastBank = null;
				}
			}
			if (this.isPublic) {
				File banksDir = CustomNpcs.getWorldSaveDirectory("banks");
				File fileBank = new File(banksDir, this.id + ".dat");
				BankData bd = new BankData(this, "");
				try { bd.readNBT(CompressedStreamTools.readCompressed(new FileInputStream(fileBank))); }
				catch (IOException e) { e.printStackTrace(); }
				Map<Integer, NpcMiscInventory> newCeils = Maps.<Integer, NpcMiscInventory>newTreeMap();
				int i = 0;
				for (NpcMiscInventory inv : bd.ceils.values()) {
					if (i == ceilId) { continue; }
					newCeils.put(i, inv);
					i++;
				}
				bd.ceils.clear();
				bd.ceils.putAll(newCeils);
				bd.save();
			} else {
				File datasDir = CustomNpcs.getWorldSaveDirectory("playerdata");
				for (File playerDir : datasDir.listFiles()) {
					if (!playerDir.isDirectory()) { continue; }
					for (File banksDir : playerDir.listFiles()) {
						if (!banksDir.isDirectory() || !banksDir.getName().equals("banks")) { continue; }
						File fileBank = new File(banksDir, this.id + ".dat");
						BankData bd = new BankData(this, playerDir.getName());
						try { bd.readNBT(CompressedStreamTools.readCompressed(new FileInputStream(fileBank))); }
						catch (IOException e) { e.printStackTrace(); }
						Map<Integer, NpcMiscInventory> newCeils = Maps.<Integer, NpcMiscInventory>newTreeMap();
						int i = 0;
						for (int c : bd.ceils.keySet()) {
							if (c == ceilId) { continue; }
							newCeils.put(i, bd.ceils.get(c));
							i++;
						}
						bd.ceils.clear();
						bd.ceils.putAll(newCeils);
						bd.save();
					}
				}
			}
		}
		Map<Integer, CeilSettings> newCS = Maps.<Integer, CeilSettings>newTreeMap();
		int i = 0;
		for (int c : this.ceilSettings.keySet()) {
			if (c == ceilId || this.ceilSettings.get(c).ceil == ceilId) { continue; }
			this.ceilSettings.get(c).ceil = i;
			newCS.put(i, this.ceilSettings.get(c));
			i++;
		}
		this.ceilSettings.clear();
		this.ceilSettings.putAll(newCS);
		BankController.getInstance().saveBank(this);
	}
	
	public CeilSettings addCeil() {
		CeilSettings cs = new CeilSettings();
		cs.ceil = this.ceilSettings.size();
		this.ceilSettings.put(cs.ceil, cs);
		if (CustomNpcs.Server!=null) {
			boolean save = false;
			for (String username : CustomNpcs.Server.getOnlinePlayerNames()) {
				EntityPlayerMP player = CustomNpcs.Server.getPlayerList().getPlayerByUsername(username);
				PlayerData data = PlayerData.get(player);
				if (player!=null && player.openContainer instanceof ContainerNPCBank && ((ContainerNPCBank) player.openContainer).bank.id == this.id) {
					player.closeContainer();
					if (!this.isPublic || !save) {
						BankData bd = PlayerData.get(player).bankData.get(this.id);
						if (bd != null) { bd.save(); }
						save = true;
					}
					player.sendMessage(new TextComponentTranslation("message.bank.changed"));
				}
				if (data.bankData.lastBank != null && data.bankData.lastBank.bank.id ==  this.id) {
					data.bankData.lastBank = null;
				}
			}
			if (this.isPublic) {
				File banksDir = CustomNpcs.getWorldSaveDirectory("banks");
				File fileBank = new File(banksDir, this.id + ".dat");
				BankData bd = new BankData(this, "");
				try { bd.readNBT(CompressedStreamTools.readCompressed(new FileInputStream(fileBank))); }
				catch (IOException e) { e.printStackTrace(); }
				if (!bd.ceils.containsKey(cs.ceil)) {
					bd.ceils.put(cs.ceil, new NpcMiscInventory(0));
					bd.save();
				}
			} else {
				File datasDir = CustomNpcs.getWorldSaveDirectory("playerdata");
				for (File playerDir : datasDir.listFiles()) {
					if (!playerDir.isDirectory()) { continue; }
					for (File banksDir : playerDir.listFiles()) {
						if (!banksDir.isDirectory() || !banksDir.getName().equals("banks")) { continue; }
						File fileBank = new File(banksDir, this.id + ".dat");
						BankData bd = new BankData(this, playerDir.getName());
						try { bd.readNBT(CompressedStreamTools.readCompressed(new FileInputStream(fileBank))); }
						catch (IOException e) { e.printStackTrace(); }
						if (!bd.ceils.containsKey(cs.ceil)) {
							bd.ceils.put(cs.ceil, new NpcMiscInventory(0));
							bd.save();
						}
					}
				}
			}
		}
		return cs;
	}
	
	public boolean hasBankData() { return this.lastBank != null; }
	
	public BankData getBankData() {
		if (this.lastBank != null) { return this.lastBank; }
		File dir = CustomNpcs.getWorldSaveDirectory("banks");
		File file = new File(dir, this.id + ".dat");
		this.lastBank = new BankData(this, "");
		if (!file.exists()) { // create new
			try {
				file.createNewFile();
				CompressedStreamTools.writeCompressed(this.lastBank.getNBT(), new FileOutputStream(file));
			}
			catch (Exception e) {
				e.printStackTrace();
				this.lastBank = null;
				return null;
			}
		}
		// load
		try {
			this.lastBank.setNBT(CompressedStreamTools.readCompressed(new FileInputStream(file)));
		} catch (IOException e) {
			e.printStackTrace();
			this.lastBank = null;
			return null;
		}
		if (this.lastBank.ceils.isEmpty()) { this.lastBank.clear(); }
		return this.lastBank;
	}
	
	public void clearBankData() { this.lastBank = null; } // BankController.update()
	
	public class CeilSettings {
		
		public ItemStack openStack;
		public ItemStack upgradeStack;
		public int ceil, startCeils, maxCeils;
		
		public CeilSettings() {
			this.ceil = 0;
			this.openStack = ItemStack.EMPTY;
			this.upgradeStack = ItemStack.EMPTY;
			this.startCeils = 1;
			this.maxCeils = 27;
		}

		public CeilSettings(NBTTagCompound nbtCeil) {
			this();
			this.read(nbtCeil);
		}

		public boolean canBeUpgraded() {
			return this.startCeils < this.maxCeils && !this.upgradeStack.isEmpty();
		}

		public void writeTo(NBTTagCompound nbtCeil) {
			if (this.openStack != null && !this.openStack.isEmpty()) {
				nbtCeil.setTag("CeilCurrency", this.openStack.writeToNBT(new NBTTagCompound()));
			}
			if (this.upgradeStack != null && !this.upgradeStack.isEmpty()) {
				nbtCeil.setTag("CeilUpgrade", this.upgradeStack.writeToNBT(new NBTTagCompound()));
			}
			nbtCeil.setInteger("StartCeil", this.startCeils);
			nbtCeil.setInteger("MaxCeil", this.maxCeils);
			nbtCeil.setInteger("CeilId", this.ceil);
		}

		public void read(NBTTagCompound nbtCeil) {
			if (nbtCeil.hasKey("CeilCurrency", 10)) {
				this.openStack = new ItemStack(nbtCeil.getCompoundTag("CeilCurrency"));
			}
			else { this.openStack = ItemStack.EMPTY; }
			if (nbtCeil.hasKey("CeilUpgrade", 10)) {
				this.upgradeStack = new ItemStack(nbtCeil.getCompoundTag("CeilUpgrade"));
			}
			else { this.upgradeStack = ItemStack.EMPTY; }
			this.startCeils = nbtCeil.getInteger("StartCeil");
			this.maxCeils = nbtCeil.getInteger("MaxCeil");
			this.ceil = nbtCeil.getInteger("CeilId");
		}

		public void set(CeilSettings settings) {
			this.openStack = settings.openStack;
			this.upgradeStack = settings.upgradeStack;
			this.startCeils = settings.startCeils;
			this.maxCeils = settings.maxCeils;
		}
	}

}
