package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.INpcRecipe;

import javax.annotation.Nonnull;
import java.util.Objects;

public class SlotNpcCrafting extends SlotCrafting {

	private final InventoryCrafting craftMatrix;

	public SlotNpcCrafting(EntityPlayer player, InventoryCrafting craftingInventory, IInventory inventory,
			int slotIndex, int x, int y) {
		super(player, craftingInventory, inventory, slotIndex, x, y);
		this.craftMatrix = craftingInventory;
	}

	@Override
	protected void onCrafting(@Nonnull ItemStack stack) {
		super.onCrafting(stack);
	}

	public @Nonnull ItemStack onTake(@Nonnull EntityPlayer player, @Nonnull ItemStack itemStack) {
		InventoryCraftResult inventorycraftresult = (InventoryCraftResult) this.inventory;
		IRecipe irecipe = inventorycraftresult.getRecipeUsed();
		if (irecipe instanceof INpcRecipe) { // Availability
			if (!((INpcRecipe) irecipe).getAvailability().isAvailable((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player))) {
				if (!player.world.isRemote) {
					player.sendMessage(new TextComponentTranslation("item.craft.not.availability"));
				}
				return ItemStack.EMPTY;
			}
		}
		this.onCrafting(itemStack);
		for (int i = 0; i < this.craftMatrix.getSizeInventory(); ++i) {
			ItemStack itemstack1 = this.craftMatrix.getStackInSlot(i);
			if (NoppesUtilServer.IsItemStackNull(itemstack1)) {
				continue;
			}
			this.craftMatrix.decrStackSize(i, 1);
			if (!itemstack1.getItem().hasContainerItem(itemstack1)) {
				continue;
			}
			ItemStack itemstack2 = itemstack1.getItem().getContainerItem(itemstack1);
			if (NoppesUtilServer.IsItemStackNull(itemstack2) || !itemstack2.isItemStackDamageable()
					|| itemstack2.getItemDamage() <= itemstack2.getMaxDamage()) {
				if (!player.inventory.addItemStackToInventory(itemstack2)) {
					if (NoppesUtilServer.IsItemStackNull(this.craftMatrix.getStackInSlot(i))) {
						this.craftMatrix.setInventorySlotContents(i, itemstack2);
					} else {
						player.dropItem(itemstack2, false);
					}
				}
			}
		}
		return itemStack;
	}

}
