package noppes.npcs.controllers.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NBTTags;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.containers.ContainerNPCBank;
import noppes.npcs.controllers.BankController;

public class PlayerBankData {

	public BankData lastBank;
	private String uuid; // playerUUID
	private int delay;

	public BankData get(int bankId) {
		if (this.lastBank != null && this.lastBank.bank.id == bankId) {
			return this.lastBank;
		}
		Bank bank = BankController.getInstance().getBank(bankId);
		if (bank == null) {
			return null;
		}
		if (bank.isPublic) {
			return bank.getBankData();
		}

		File dir = CustomNpcs.getWorldSaveDirectory("playerdata/" + this.uuid + "/banks");
		File file = new File(dir, bank.id + ".dat");
		this.lastBank = new BankData(bank, this.uuid);
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

	public void loadNBTData(NBTTagCompound compound, String uuid) {
		this.uuid = uuid;
		// load old data
		if (compound.hasKey("BankData", 9)) {
			File dir = CustomNpcs.getWorldSaveDirectory("playerdata/" + this.uuid + "/banks");
			NBTTagList list = compound.getTagList("BankData", 10);
            for (int bankPos = 0; bankPos < list.tagCount(); bankPos++) {
				NBTTagCompound nbt = list.getCompoundTagAt(bankPos);
				Bank bank = BankController.getInstance().getBank(nbt.getInteger("DataBankId"));
				if (bank == null) {
					continue;
				}
				BankData bd = new BankData(bank, this.uuid);
				int unlockedCeils = nbt.getInteger("unlockedCeils");
				HashMap<Integer, Boolean> upgradedSlots = NBTTags.getBooleanList(nbt.getTagList("UpdatedSlots", 10));
				for (int ceil = 0; ceil < nbt.getTagList("BankInv", 10).tagCount(); ceil++) {
					NBTTagCompound nbtCeils = nbt.getTagList("BankInv", 10).getCompoundTagAt(ceil);
					int c = nbtCeils.getInteger("Slot");
					if (c > unlockedCeils) {
						break;
					}
					NpcMiscInventory inv = new NpcMiscInventory(upgradedSlots.get(c) ? 54 : 27);
					inv.setFromNBT(nbtCeils.getCompoundTag("BankItems"));
					bd.cells.put(c, inv);
				}
				// save has new data
				File file = new File(dir, bank.id + ".dat");
				if (!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException e) { LogWriter.error("Error:", e); }
				}
				try {
					CompressedStreamTools.writeCompressed(bd.getNBT(), Files.newOutputStream(file.toPath()));
				} catch (IOException e) { LogWriter.error("Error:", e); }
			}

		}
	}

	public void remove(int bankId) {
		File dir = CustomNpcs.getWorldSaveDirectory("playerdata/" + this.uuid + "/banks");
		File file = new File(dir, bankId + ".dat");
		if (file.exists()) {
			file.delete();
		}
	}

	public void update(EntityPlayerMP player) { // ServerTickHandler.onPlayerTick();
		if (this.delay > 0) {
			this.delay--;
			if (this.delay == 0) {
				this.lastBank = null;
			}
		}
		if (this.lastBank == null) {
			return;
		}
		if (player.openContainer instanceof ContainerNPCBank) {
			this.delay = 200;
        }
	}

}
