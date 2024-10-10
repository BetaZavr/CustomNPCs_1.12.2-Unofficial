package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeContainer;
import noppes.npcs.CustomRegisters;

import javax.annotation.Nonnull;

// ContainerWorkbench
public class ContainerCarpentryBench
		extends Container
		implements IRecipeContainer {

	public InventoryCrafting craftMatrix = new InventoryCrafting(this, 4, 4);
	public InventoryCraftResult craftResult = new InventoryCraftResult();
	private final EntityPlayer player;
    private final BlockPos pos;
	private final World world;
	public boolean isShowBook = false;

	public ContainerCarpentryBench(InventoryPlayer playerInventory, World worldIn, BlockPos posIn) {
		this.world = worldIn;
		this.pos = posIn;
		this.player = playerInventory.player;
		this.addSlotToContainer(new SlotCrafting(playerInventory.player, this.craftMatrix, this.craftResult, 0, 140, 41));
		for (int var6 = 0; var6 < 4; ++var6) {
			for (int var7 = 0; var7 < 4; ++var7) {
				this.addSlotToContainer(new Slot(this.craftMatrix, var7 + var6 * 4, 30 + var7 * 18, 14 + var6 * 18));
			}
		}
		for (int var6 = 0; var6 < 3; ++var6) {
			for (int var7 = 0; var7 < 9; ++var7) {
				this.addSlotToContainer(new Slot(playerInventory, var7 + var6 * 9 + 9, 8 + var7 * 18, 98 + var6 * 18));
			}
		}
		for (int var6 = 0; var6 < 9; ++var6) {
			this.addSlotToContainer(new Slot(playerInventory, var6, 8 + var6 * 18, 156));
		}
		this.onCraftMatrixChanged(this.craftMatrix);
	}

	@Override
	public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
		if (this.world.getBlockState(this.pos).getBlock() != CustomRegisters.carpentyBench) { return false; }
		else { return playerIn.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D, (double) this.pos.getZ() + 0.5D) <= 64.0D; }
	}

	@Override
	public boolean canMergeSlot(@Nonnull ItemStack stack, @Nonnull Slot slotIn) {
		return slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn);
	}

	@Override
	public void onContainerClosed(@Nonnull EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		if (!this.world.isRemote) {
			clearContainer(playerIn, this.world, this.craftMatrix);
		}
	}

	@Override
	public void onCraftMatrixChanged(@Nonnull IInventory inventoryIn) {
		this.slotChangedCraftingGrid(this.world, this.player, this.craftMatrix, this.craftResult);
	}

	public void checkPos(boolean showBook) {
		if (isShowBook != showBook) {
			int offsetX = (showBook ? 1 : -1 ) * 77;
			for (Slot slot : inventorySlots) {
				slot.xPos += offsetX;
			}
			isShowBook = showBook;
		}
	}

	@Override
	public @Nonnull ItemStack transferStackInSlot(@Nonnull EntityPlayer playerIn, int index) {
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(index);
		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (index == 0) {
				itemstack1.getItem().onCreated(itemstack1, this.world, playerIn);
				if (!this.mergeItemStack(itemstack1, 17, 53, true)) {
					return ItemStack.EMPTY;
				}
				slot.onSlotChange(itemstack1, itemstack);
			} else if (index >= 17 && index < 44) {
				if (!this.mergeItemStack(itemstack1, 44, 53, false)) {
					return ItemStack.EMPTY;
				}
			} else if (index >= 44 && index < 53) {
				if (!this.mergeItemStack(itemstack1, 17, 44, false)) {
					return ItemStack.EMPTY;
				}
			} else if (!this.mergeItemStack(itemstack1, 17, 53, false)) {
				return ItemStack.EMPTY;
			}

			if (itemstack1.isEmpty()) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}

			if (itemstack1.getCount() == itemstack.getCount()) {
				return ItemStack.EMPTY;
			}

			ItemStack itemstack2 = slot.onTake(playerIn, itemstack1);
			if (index == 0) {
				playerIn.dropItem(itemstack2, false);
			}
		}
		return itemstack;
	}

	@Override
	public InventoryCraftResult getCraftResult() { return craftResult; }

	@Override
	public InventoryCrafting getCraftMatrix() { return craftMatrix;	}

}
