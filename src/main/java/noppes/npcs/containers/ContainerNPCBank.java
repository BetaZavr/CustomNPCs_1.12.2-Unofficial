package noppes.npcs.containers;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.BankData;
import noppes.npcs.controllers.data.PlayerData;

public class ContainerNPCBank
extends ContainerNpcInterface {
	
	public static String editPlayerBankData;
	public BankData data;
	
	public int ceil, height;
	public NpcMiscInventory items;
	public Bank bank;
	/* -1 - not stack in inventory
	 * (max ceils) - done
	 * else - ceil ID (not open or update) */
	public int dataCeil; // ServerTickHandler.onPlayerTick();

	public ContainerNPCBank(EntityPlayer player, Bank bank, int ceil, int slots) {
		super(player);
		
		this.bank = bank;
		this.ceil = ceil;
		this.dataCeil = -2;
		this.items = new NpcMiscInventory(slots);
		// Server
		if (!player.world.isRemote) {
			this.data = null;
			PlayerData pd = null;
			if (ContainerNPCBank.editPlayerBankData != null) {
				try {
					List<PlayerData> list = PlayerDataController.instance.getPlayersData(player, ContainerNPCBank.editPlayerBankData);
					if (!list.isEmpty()) { pd = list.get(0); }
				}
				catch (CommandException e) { e.printStackTrace(); }
			}
			else { pd = PlayerData.get(player); }
			if (pd != null) { this.data = pd.bankData.get(bank.id); }
			if (this.data != null) { this.items = this.data.ceils.get(ceil); }
		}
		int h = ((int) Math.ceil((double) this.items.getSizeInventory() / 9.0d) - 4) * 18;
		int w = 0;
		if (this.items.getSizeInventory() > 45) { h = 18; }
		h -= 6;
		// Inventory
		if (this.items.getSizeInventory() > 45) { // Creative
			w = 8;
			this.height = 5 * 18;
			for (int i = 0; i < this.items.getSizeInventory(); i++) {
				this.addSlotToContainer(new Slot(this.items, i, -5000, -5000));
			}
		} else { // 9x(2 / 5)
			this.height = (int) Math.ceil((double) this.items.getSizeInventory() / 9.0d) * 18;
			int u = 0, e = this.items.getSizeInventory();
			if (this.items.getSizeInventory() % 9 != 0) { e -= this.items.getSizeInventory() % 9; }
			for (int i = 0; i < this.items.getSizeInventory(); i++) {
				if (i>=e) { u = (int) (((9.0d - ((double) this.items.getSizeInventory() % 9.0d)) / 2.0d) * 18.0d); }
				this.addSlotToContainer(new Slot(this.items, i, 8 + u + (i % 9) * 18, 18 + (int) Math.floor((double) i / 9.0d) * 18));
			}
		}
		this.height += 19;
		// Player Inventory
		for (int r = 0; r < 3; ++r) {
			for (int p = 0; p < 9; ++p) {
				this.addSlotToContainer(new Slot(player.inventory, p + r * 9 + 9, 8 + w + p * 18, 122 + r * 18 + h));
			}
		}
		for (int p = 0; p < 9; ++p) {
			this.addSlotToContainer(new Slot(player.inventory, p, 8 + w + p * 18, 180 + h));
		}
	}

	public void onContainerClosed(EntityPlayer player) {
		super.onContainerClosed(player);
		if (!player.world.isRemote && this.data != null) { // save
			if (this.bank.isPublic) {
				if (this.listeners.size()==1) {
					this.data.save();
				}
			} else {
				this.data.save();
			}
		}
	}

	public void onCraftMatrixChanged(IInventory inv) { }

	public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index < this.items.getSizeInventory()) {
				if (!this.mergeItemStack(itemstack1, this.items.getSizeInventory(), this.inventorySlots.size(), true)) { return ItemStack.EMPTY; }
			}
			else if (!this.mergeItemStack(itemstack1, 0, this.items.getSizeInventory(), false)) { return ItemStack.EMPTY; }
			if (itemstack1.isEmpty()) { slot.putStack(ItemStack.EMPTY); }
			else { slot.onSlotChanged(); }
		}
		return itemstack;
	}
	
}
