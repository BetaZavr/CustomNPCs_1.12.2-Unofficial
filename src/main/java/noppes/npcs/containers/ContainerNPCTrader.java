package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;

public class ContainerNPCTrader extends ContainerNpcInterface {
	public Deal deal;
	// private EntityNPCInterface npc;
	public Marcet marcet;
	public RoleTrader role;

	public ContainerNPCTrader(EntityNPCInterface npc, EntityPlayer player) {
		super(player);
		// this.npc = npc;
		this.role = (RoleTrader) npc.roleInterface;
		this.marcet = MarcetController.getInstance().marcets.get(this.role.marcet);
		this.deal = this.marcet.data.get(0);

		for (int i2 = 0; i2 < 3; ++i2) {
			for (int l1 = 0; l1 < 9; ++l1) {
				this.addSlotToContainer(
						new Slot((IInventory) player.inventory, l1 + i2 * 9 + 9, 32 + l1 * 18, 140 + i2 * 18));
			}
		}
		for (int j1 = 0; j1 < 9; ++j1) {
			this.addSlotToContainer(new Slot((IInventory) player.inventory, j1, 32 + j1 * 18, 198));
		}
	}

	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	// New
	@Override
	public void onContainerClosed(EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		if (playerIn instanceof EntityPlayerMP) {
			this.marcet.removeListener((EntityPlayerMP) playerIn, true);
		}
	}

	/*
	 * public ItemStack slotClick(int i, int j, ClickType par3, EntityPlayer
	 * entityplayer) { if (par3 != ClickType.PICKUP) { return ItemStack.EMPTY; } if
	 * (i < 0 || i >= 18) { return super.slotClick(i, j, par3, entityplayer); } if
	 * (j == 1) { return ItemStack.EMPTY; } Slot slot = this.inventorySlots.get(i);
	 * if (slot == null || slot.getStack() == null) { return ItemStack.EMPTY; }
	 * ItemStack item = slot.getStack(); if (!this.canGivePlayer(item,
	 * entityplayer)) { return ItemStack.EMPTY; } ItemStack currency =
	 * this.role.inventoryCurrency.getStackInSlot(i); ItemStack currency2 =
	 * this.role.inventoryCurrency.getStackInSlot(i + 18); if
	 * (!this.canBuy(currency, currency2, entityplayer)) {
	 * RoleEvent.TradeFailedEvent event = new
	 * RoleEvent.TradeFailedEvent(entityplayer, this.npc.wrappedNPC, item, currency,
	 * currency2); EventHooks.onNPCRole(this.npc, event); return (event.receiving ==
	 * null) ? ItemStack.EMPTY : event.receiving.getMCItemStack(); }
	 * RoleEvent.TraderEvent event2 = new RoleEvent.TraderEvent(entityplayer,
	 * this.npc.wrappedNPC, item, currency, currency2); if
	 * (EventHooks.onNPCRole(this.npc, event2)) { return ItemStack.EMPTY; } if
	 * (event2.currency1 != null) { currency = event2.currency1.getMCItemStack(); }
	 * if (event2.currency2 != null) { currency2 =
	 * event2.currency2.getMCItemStack(); } if (!this.canBuy(currency, currency2,
	 * entityplayer)) { return ItemStack.EMPTY; }
	 * NoppesUtilPlayer.consumeItem(entityplayer, currency, this.role.ignoreDamage,
	 * this.role.ignoreNBT); NoppesUtilPlayer.consumeItem(entityplayer, currency2,
	 * this.role.ignoreDamage, this.role.ignoreNBT); ItemStack soldItem =
	 * ItemStack.EMPTY; if (event2.sold != null) { soldItem =
	 * event2.sold.getMCItemStack(); this.givePlayer(soldItem.copy(), entityplayer);
	 * } return soldItem; }
	 * 
	 * public boolean canBuy(ItemStack currency, ItemStack currency2, EntityPlayer
	 * player) { if (NoppesUtilServer.IsItemStackNull(currency) &&
	 * NoppesUtilServer.IsItemStackNull(currency2)) { return true; } if
	 * (NoppesUtilServer.IsItemStackNull(currency)) { currency = currency2;
	 * currency2 = ItemStack.EMPTY; } if (NoppesUtilPlayer.compareItems(currency,
	 * currency2, this.role.ignoreDamage, this.role.ignoreNBT)) { currency =
	 * currency.copy(); currency.grow(currency2.getCount()); currency2 =
	 * ItemStack.EMPTY; } if (NoppesUtilServer.IsItemStackNull(currency2)) { return
	 * NoppesUtilPlayer.compareItems(player, currency, this.role.ignoreDamage,
	 * this.role.ignoreNBT); } return NoppesUtilPlayer.compareItems(player,
	 * currency, this.role.ignoreDamage, this.role.ignoreNBT) &&
	 * NoppesUtilPlayer.compareItems(player, currency2, this.role.ignoreDamage,
	 * this.role.ignoreNBT); }
	 * 
	 * private boolean canGivePlayer(ItemStack item, EntityPlayer entityplayer) {
	 * ItemStack itemstack3 = entityplayer.inventory.getItemStack(); if
	 * (NoppesUtilServer.IsItemStackNull(itemstack3)) { return true; } if
	 * (NoppesUtilPlayer.compareItems(itemstack3, item, false, false)) { int k1 =
	 * item.getCount(); if (k1 > 0 && k1 + itemstack3.getCount() <=
	 * itemstack3.getMaxStackSize()) { return true; } } return false; }
	 * 
	 * private void givePlayer(ItemStack item, EntityPlayer entityplayer) {
	 * ItemStack itemstack3 = entityplayer.inventory.getItemStack(); if
	 * (NoppesUtilServer.IsItemStackNull(itemstack3)) {
	 * entityplayer.inventory.setItemStack(item); } else if
	 * (NoppesUtilPlayer.compareItems(itemstack3, item, false, false)) { int k1 =
	 * item.getCount(); if (k1 > 0 && k1 + itemstack3.getCount() <=
	 * itemstack3.getMaxStackSize()) { itemstack3.grow(k1); } } }
	 */

	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i) {
		return ItemStack.EMPTY;
	}
}
