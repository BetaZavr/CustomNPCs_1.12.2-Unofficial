package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.CustomRegisters;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.items.crafting.NpcShapedRecipes;
import noppes.npcs.items.crafting.NpcShapelessRecipes;

import javax.annotation.Nonnull;
import java.util.Objects;

public class ContainerCarpentryBench extends Container {

	public InventoryCrafting craftMatrix = new InventoryCrafting(this, 4, 4);
	public InventoryCraftResult craftResult = new InventoryCraftResult();
	private final EntityPlayer player;
	private final BlockPos pos;
	private final World world;
	public int x = 125, y = 30;

	public ContainerCarpentryBench(InventoryPlayer playerInventory, World worldIn, BlockPos posIn) {
		this.world = worldIn;
		this.pos = posIn;
		this.player = playerInventory.player;
		this.addSlotToContainer(new SlotNpcCrafting(playerInventory.player, this.craftMatrix, this.craftResult, 0, 140, 41));
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

	public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
		if (this.world.getBlockState(this.pos).getBlock() != CustomRegisters.carpentyBench) {
			return false;
		} else {
			return playerIn.getDistanceSq((double) this.pos.getX() + 0.5D, (double) this.pos.getY() + 0.5D,
					(double) this.pos.getZ() + 0.5D) <= 64.0D;
		}
	}

	public boolean canMergeSlot(@Nonnull ItemStack stack, @Nonnull Slot slotIn) {
		return slotIn.inventory != this.craftResult && super.canMergeSlot(stack, slotIn);
	}

	public void onContainerClosed(@Nonnull EntityPlayer playerIn) {
		super.onContainerClosed(playerIn);
		if (!this.world.isRemote) {
			this.clearContainer(playerIn, this.world, this.craftMatrix);
		}
	}

	public void onCraftMatrixChanged(@Nonnull IInventory inventoryIn) {
		if (!this.world.isRemote) {
			INpcRecipe recipe = RecipeController.getInstance().findMatchingRecipe(this.craftMatrix);
			ItemStack item = ItemStack.EMPTY;
			if (recipe != null
					&& recipe.getAvailability().isAvailable((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(this.player))) {
				if (recipe instanceof NpcShapedRecipes) {
					item = ((NpcShapedRecipes) recipe).getCraftingResult(this.craftMatrix);
				} else {
					item = ((NpcShapelessRecipes) recipe).getCraftingResult(this.craftMatrix);
				}
			}
			this.craftResult.setInventorySlotContents(0, item);
			((EntityPlayerMP) this.player).connection.sendPacket(new SPacketSetSlot(this.windowId, 0, item));
		}
	}

	public void setPos(int u, int v) {
		if (this.x != u || this.y != v) {
			int offsetX = u - this.x;
			int offsetY = v - this.y;
			for (Slot slot : this.inventorySlots) {
				slot.xPos += offsetX;
				slot.yPos += offsetY;
			}
			this.x = u;
			this.y = v;
		}
	}

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

}
