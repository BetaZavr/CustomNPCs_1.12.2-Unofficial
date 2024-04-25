package noppes.npcs.containers;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.CommonProxy;
import noppes.npcs.util.BuilderData;

public class ContainerBuilderSettings extends Container {

	public BuilderData builderData;
	EntityPlayer player;

	public ContainerBuilderSettings(EntityPlayer player, int id) {
		this.player = player;
		BuilderData base = CommonProxy.dataBuilder.get(id);
		this.builderData = new BuilderData();
		if (base == null) {
			this.builderData.id = id;
		} else {
			this.builderData.read(base.getNbt());
		}
		if (this.builderData != null && this.builderData.type < 3) {
			for (int i = 0; i < 9; ++i) {
				this.addSlotToContainer(new Slot(player.inventory, i, i * 18 + 8, 194));
			}
			for (int y = 0; y < 3; ++y) {
				for (int x = 0; x < 9; ++x) {
					this.addSlotToContainer(new Slot(player.inventory, x + y * 9 + 9, x * 18 + 8, 136 + y * 18));
				}
			}
			if (this.builderData.type == 2) {
				this.addSlotToContainer(new Slot(this.builderData.inv, 0, 62, 113));
			}
			for (int i = 1; i < 10; i++) {
				this.addSlotToContainer(
						new Slot(this.builderData.inv, i, 8 + (i / 6) * 54, 17 + ((i < 6 ? 0 : -5) + i - 1) * 24));
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer playerIn) {
		return true;
	}

	public boolean canMergeSlot(ItemStack stack, Slot slotId) {
		return slotId.inventory == this.player.inventory;
	}

	public void save() {
		if (this.builderData == null || this.builderData.type > 2) {
			return;
		}
		int s = this.builderData.type == 2 ? 0 : 1;
		for (int i = 36; i < this.inventorySlots.size(); i++) {
			this.builderData.inv.setInventorySlotContents(s + i - 36, this.getSlot(i).getStack());
		}
	}

	public ItemStack slotClick(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
		if (clickType == ClickType.QUICK_MOVE || clickType == ClickType.QUICK_CRAFT) {
			return ItemStack.EMPTY;
		}
		if (slotId >= 36) {
			Slot slot = this.inventorySlots.get(slotId);
			if (slot == null) {
				return ItemStack.EMPTY;
			}
			if (!player.inventory.getItemStack().isEmpty()) {
				Block b = Block.getBlockFromItem(player.inventory.getItemStack().getItem());
				boolean has = false;
				ItemStack stack = player.inventory.getItemStack().copy();
				stack.setCount(1);
				for (int i = 36; i < this.inventorySlots.size(); i++) {
					Slot s = this.inventorySlots.get(i);
					if (i == slotId || s == null) {
						continue;
					}
					if (s.getStack().isItemEqual(stack)) {
						has = true;
						break;
					}
				}
				if (!has && b != null && b != Blocks.AIR) {
					slot.putStack(stack);
					return ItemStack.EMPTY;
				}
			}
			slot.putStack(ItemStack.EMPTY);
			return ItemStack.EMPTY;
		}
		return super.slotClick(slotId, dragType, clickType, player);
	}

}
