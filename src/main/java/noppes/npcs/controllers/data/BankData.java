package noppes.npcs.controllers.data;

import java.io.File;
import java.nio.file.Files;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

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
	public final Map<Integer, NpcMiscInventory> cells = new TreeMap<>();
	private final String uuid;

	public BankData(Bank bank, String uuid) {
		this.bank = bank;
		this.uuid = uuid;
		if (this.bank == null) {
			this.bank = new Bank();
		}
		clear();
	}

	public void clear() {
		this.cells.clear();
		boolean isStart = true;
		for (int ceil : this.bank.ceilSettings.keySet()) {
			CeilSettings cs = this.bank.ceilSettings.get(ceil);
			if (isStart) {
				isStart = cs.openStack.isEmpty();
			}
			this.cells.put(ceil, new NpcMiscInventory(isStart ? cs.startCells : 0));
		}
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound nbtBD = new NBTTagCompound();
		nbtBD.setInteger("id", this.bank.id);
		NBTTagList list = new NBTTagList();
		for (int ceil : this.cells.keySet()) {
			NBTTagCompound nbtCeil = new NBTTagCompound();
			nbtCeil.setInteger("ceil", ceil);
			nbtCeil.setInteger("slots", this.cells.get(ceil).getSizeInventory());
			NBTTagCompound invNbt = this.cells.get(ceil).getToNBT();
			nbtCeil.setTag("NpcMiscInv", invNbt.getTag("NpcMiscInv"));
			list.appendTag(nbtCeil);
		}
		nbtBD.setTag("cells", list);
		return nbtBD;
	}

	public UUID getUUID() {
		return UUID.fromString(this.uuid);
	}

	public void openBankGui(EntityPlayer player, EntityNPCInterface npc, int ceil) {
		if (!this.cells.containsKey(ceil) || !(player instanceof EntityPlayerMP)) {
			return;
		}
		if (this.bank.isPublic && !player.capabilities.isCreativeMode && !this.bank.access.isEmpty() && !this.bank.owner.equals(player.getName())) {
			if ((this.bank.isWhiteList && !this.bank.access.contains(player.getName())) || (!this.bank.isWhiteList && this.bank.access.contains(player.getName()))) {
				player.sendMessage(new TextComponentTranslation("message.bank.not.access"));
				return;
			}
		}
		NBTTagCompound nbtBank = new NBTTagCompound();
		this.bank.writeToNBT(nbtBank);
		Server.sendData((EntityPlayerMP) player, EnumPacketClient.SYNC_UPDATE, EnumSync.BankData, nbtBank);
		NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBank, npc, this.bank.id, ceil, this.cells.get(ceil).getSizeInventory());
	}

	public void readNBT(NBTTagCompound nbtBD) {
		this.bank = BankController.getInstance().banks.get(nbtBD.getInteger("id"));
		NBTTagList list = nbtBD.getTagList("cells", 10);
		if (list.tagCount() == 0 && nbtBD.hasKey("ceils", 9)) { // old typo
			list = nbtBD.getTagList("ceils", 10);
		}
		this.cells.clear();
		for (int ceil = 0; ceil < list.tagCount(); ceil++) {
			NBTTagCompound nbtCeil = list.getCompoundTagAt(ceil);
			NpcMiscInventory inv = new NpcMiscInventory(nbtCeil.getInteger("slots"));
			inv.setFromNBT(nbtCeil);
			this.cells.put(nbtCeil.getInteger("ceil"), inv);
		}
	}

	public void save() {
		File bankFile;
        File dir;
        if (this.bank.isPublic) {
            dir = CustomNpcs.getWorldSaveDirectory("banks");
        } else {
            dir = CustomNpcs.getWorldSaveDirectory("playerdata/" + this.uuid + "/banks");
        }
        bankFile = new File(dir, this.bank.id + ".dat");
        if (!bankFile.exists()) {
			try {
				bankFile.createNewFile();
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
		LogWriter.debug("Bank ID: " + this.bank.id + " save " + (this.bank.isPublic ? "Public" : "Player \"" + this.uuid + "\"") + " Inventory's");
		try {
			CompressedStreamTools.writeCompressed(this.getNBT(), Files.newOutputStream(bankFile.toPath()));
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	public void setNBT(NBTTagCompound nbtBD) {
		this.bank = BankController.getInstance().banks.get(nbtBD.getInteger("id"));
		NBTTagList list = nbtBD.getTagList("cells", 10);
		for (int ceil = 0; ceil < list.tagCount(); ceil++) {
			NBTTagCompound nbtCeil = list.getCompoundTagAt(ceil);
			NpcMiscInventory inv = new NpcMiscInventory(nbtCeil.getInteger("slots"));
			inv.setFromNBT(nbtCeil);
			this.cells.put(nbtCeil.getInteger("ceil"), inv);
		}
	}

}
