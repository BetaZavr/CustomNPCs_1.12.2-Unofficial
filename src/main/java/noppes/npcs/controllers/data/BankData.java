package noppes.npcs.controllers.data;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.data.Bank.CeilSettings;
import noppes.npcs.entity.EntityNPCInterface;

public class BankData {

	public Bank bank;
	public final Map<Integer, NpcMiscInventory> ceils;
	private String uuid;

	public BankData(Bank bank, String uuid) {
		this.bank = bank;
		this.uuid = uuid;
		if (this.bank == null) {
			this.bank = new Bank();
		}
		this.ceils = Maps.<Integer, NpcMiscInventory>newTreeMap();
		this.clear();
	}

	public void clear() {
		this.ceils.clear();
		boolean isStart = true;
		for (int ceil : this.bank.ceilSettings.keySet()) {
			CeilSettings cs = this.bank.ceilSettings.get(ceil);
			if (isStart) {
				isStart = cs.openStack.isEmpty();
			}
			this.ceils.put(ceil, new NpcMiscInventory(isStart ? cs.startCeils : 0));
		}
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound nbtBD = new NBTTagCompound();
		nbtBD.setInteger("id", this.bank.id);
		NBTTagList list = new NBTTagList();
		for (int ceil : this.ceils.keySet()) {
			NBTTagCompound nbtCeil = new NBTTagCompound();
			nbtCeil.setInteger("ceil", ceil);
			nbtCeil.setInteger("slots", this.ceils.get(ceil).getSizeInventory());
			NBTTagCompound invNbt = this.ceils.get(ceil).getToNBT();
			nbtCeil.setTag("NpcMiscInv", invNbt.getTag("NpcMiscInv"));
			list.appendTag(nbtCeil);
		}
		nbtBD.setTag("ceils", list);
		return nbtBD;
	}

	public UUID getUUID() {
		return UUID.fromString(this.uuid);
	}

	public void openBankGui(EntityPlayer player, EntityNPCInterface npc, int bankId, int ceil) {
		if (!this.ceils.containsKey(ceil) || !(player instanceof EntityPlayerMP)) {
			return;
		}
		if (this.bank.isPublic && !player.capabilities.isCreativeMode && !this.bank.access.isEmpty()
				&& !this.bank.owner.equals(player.getName())) {
			if ((this.bank.isWhiteList && !this.bank.access.contains(player.getName()))
					|| (!this.bank.isWhiteList && this.bank.access.contains(player.getName()))) {
				player.sendMessage(new TextComponentTranslation("message.bank.not.accsess"));
				return;
			}
		}
		NBTTagCompound nbtBank = new NBTTagCompound();
		this.bank.writeToNBT(nbtBank);
		Server.sendData((EntityPlayerMP) player, EnumPacketClient.SYNC_UPDATE, EnumSync.BankData, nbtBank);
		NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBank, npc, this.bank.id, ceil,
				this.ceils.get(ceil).getSizeInventory());
	}

	public void readNBT(NBTTagCompound nbtBD) {
		this.bank = BankController.getInstance().banks.get(nbtBD.getInteger("id"));
		NBTTagList list = nbtBD.getTagList("ceils", 10);
		this.ceils.clear();
		for (int ceil = 0; ceil < list.tagCount(); ceil++) {
			NBTTagCompound nbtCeil = list.getCompoundTagAt(ceil);
			NpcMiscInventory inv = new NpcMiscInventory(nbtCeil.getInteger("slots"));
			inv.setFromNBT(nbtCeil);
			this.ceils.put(nbtCeil.getInteger("ceil"), inv);
		}
	}

	public void save() {
		File bankFile;
		if (this.bank.isPublic) {
			File dir = CustomNpcs.getWorldSaveDirectory("banks");
			bankFile = new File(dir, this.bank.id + ".dat");
		} else {
			File dir = CustomNpcs.getWorldSaveDirectory("playerdata/" + this.uuid + "/banks");
			bankFile = new File(dir, this.bank.id + ".dat");
		}
		if (!bankFile.exists()) {
			try {
				bankFile.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		LogWriter.debug("Bank ID: " + this.bank.id + " save "
				+ (this.bank.isPublic ? "Public" : "Player \"" + this.uuid + "\"") + " Inventoryes");
		try {
			CompressedStreamTools.writeCompressed(this.getNBT(), new FileOutputStream(bankFile));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setNBT(NBTTagCompound nbtBD) {
		this.bank = BankController.getInstance().banks.get(nbtBD.getInteger("id"));
		NBTTagList list = nbtBD.getTagList("ceils", 10);
		for (int ceil = 0; ceil < list.tagCount(); ceil++) {
			NBTTagCompound nbtCeil = list.getCompoundTagAt(ceil);
			NpcMiscInventory inv = new NpcMiscInventory(nbtCeil.getInteger("slots"));
			inv.setFromNBT(nbtCeil);
			this.ceils.put(nbtCeil.getInteger("ceil"), inv);
		}
	}

}
