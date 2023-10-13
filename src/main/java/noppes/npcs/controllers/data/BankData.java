package noppes.npcs.controllers.data;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.containers.ContainerNPCBankInterface;
import noppes.npcs.controllers.BankController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.CustomNPCsScheduler;

public class BankData {
	
	public int bankId;
	public HashMap<Integer, NpcMiscInventory> itemSlots;
	public int unlockedSlots;
	public HashMap<Integer, Boolean> upgradedSlots;

	public BankData() {
		this.unlockedSlots = 0;
		this.bankId = -1;
		this.itemSlots = new HashMap<Integer, NpcMiscInventory>();
		this.upgradedSlots = new HashMap<Integer, Boolean>();
		for (int i = 0; i < 6; ++i) {
			this.itemSlots.put(i, new NpcMiscInventory(54));
			this.upgradedSlots.put(i, false);
		}
	}

	private ContainerNPCBankInterface getContainer(EntityPlayer player) {
		Container con = player.openContainer;
		if (con == null || !(con instanceof ContainerNPCBankInterface)) {
			return null;
		}
		return (ContainerNPCBankInterface) con;
	}

	private HashMap<Integer, NpcMiscInventory> getItemSlots(NBTTagList tagList) {
		HashMap<Integer, NpcMiscInventory> list = new HashMap<Integer, NpcMiscInventory>();
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			int slot = nbttagcompound.getInteger("Slot");
			NpcMiscInventory inv = new NpcMiscInventory(54);
			inv.setFromNBT(nbttagcompound.getCompoundTag("BankItems"));
			list.put(slot, inv);
		}
		return list;
	}

	public boolean isUpgraded(Bank bank, int slot) {
		return bank.isUpgraded(slot) || (bank.canBeUpgraded(slot) && this.upgradedSlots.get(slot));
	}

	private NBTTagList nbtItemSlots(HashMap<Integer, NpcMiscInventory> items) {
		NBTTagList list = new NBTTagList();
		for (int slot : items.keySet()) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("Slot", slot);
			nbttagcompound.setTag("BankItems", items.get(slot).getToNBT());
			list.appendTag(nbttagcompound);
		}
		return list;
	}

	public void openBankGui(EntityPlayer player, EntityNPCInterface npc, int bankId, int slot) {
		Bank bank = BankController.getInstance().getBank(bankId);
		if (bank.getMaxSlots() <= slot) {
			return;
		}
		if (bank.startSlots > this.unlockedSlots) {
			this.unlockedSlots = bank.startSlots;
		}
		ItemStack currency = ItemStack.EMPTY;
		if (this.unlockedSlots <= slot) {
			currency = bank.currencyInventory.getStackInSlot(slot);
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankUnlock, npc, slot, bank.id, 0);
		} else if (this.isUpgraded(bank, slot)) {
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankLarge, npc, slot, bank.id, 0);
		} else if (bank.canBeUpgraded(slot)) {
			currency = bank.upgradeInventory.getStackInSlot(slot);
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankUprade, npc, slot, bank.id, 0);
		} else {
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.PlayerBankSmall, npc, slot, bank.id, 0);
		}
		ItemStack cr = currency;
		CustomNPCsScheduler.runTack(() -> {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setInteger("MaxSlots", bank.getMaxSlots());
			compound.setInteger("UnlockedSlots", this.unlockedSlots);
			if (cr != null && !cr.isEmpty()) {
				compound.setTag("Currency", cr.writeToNBT(new NBTTagCompound()));
				ContainerNPCBankInterface container = this.getContainer(player);
				if (container != null) {
					container.setCurrency(cr);
				}
			}
			Server.sendDataChecked((EntityPlayerMP) player, EnumPacketClient.GUI_DATA, compound);
		}, 300);
	}

	public void readNBT(NBTTagCompound nbttagcompound) {
		this.bankId = nbttagcompound.getInteger("DataBankId");
		this.unlockedSlots = nbttagcompound.getInteger("UnlockedSlots");
		this.itemSlots = this.getItemSlots(nbttagcompound.getTagList("BankInv", 10));
		this.upgradedSlots = NBTTags.getBooleanList(nbttagcompound.getTagList("UpdatedSlots", 10));
	}

	public void writeNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("DataBankId", this.bankId);
		nbttagcompound.setInteger("UnlockedSlots", this.unlockedSlots);
		nbttagcompound.setTag("UpdatedSlots", NBTTags.nbtBooleanList(this.upgradedSlots));
		nbttagcompound.setTag("BankInv", this.nbtItemSlots(this.itemSlots));
	}
}
