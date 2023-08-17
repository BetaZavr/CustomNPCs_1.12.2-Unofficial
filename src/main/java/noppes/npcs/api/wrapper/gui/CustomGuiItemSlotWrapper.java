package noppes.npcs.api.wrapper.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.containers.ContainerCustomGui;

public class CustomGuiItemSlotWrapper
extends CustomGuiComponentWrapper
implements IItemSlot {
	
	// New
	EntityPlayer player;
	int slotIndex = 0;
	IItemStack stack;

	public CustomGuiItemSlotWrapper() {
		this.stack = ItemStackWrapper.AIR;
	}

	public CustomGuiItemSlotWrapper(int x, int y, IItemStack stack) {
		this.stack = ItemStackWrapper.AIR;
		this.setPos(x, y);
		this.setStack(stack);
	}

	@Override
	public CustomGuiComponentWrapper fromNBT(NBTTagCompound nbt) {
		super.fromNBT(nbt);
		this.setStack(NpcAPI.Instance().getIItemStack(new ItemStack(nbt.getCompoundTag("Stack"))));
		return this;
	}

	@Override
	public Slot getMCSlot() {
		return null;
	}

	@Override
	public IItemStack getStack() {
		return this.stack;
	}

	@Override
	public int getType() {
		return GuiComponentType.ITEM_SLOT.get();
	}

	@Override
	public boolean hasStack() {
		return this.stack != null && !this.stack.isEmpty();
	}

	@Override
	public IItemSlot setStack(IItemStack itemStack) {
		if (itemStack == null) {
			this.stack = ItemStackWrapper.AIR;
		} else {
			this.stack = itemStack;
		}
		if (this.player != null && player.openContainer instanceof ContainerCustomGui) { // New
			this.player.openContainer.getSlot(this.slotIndex).inventory.setInventorySlotContents(this.slotIndex,
					this.stack.getMCItemStack());
			this.player.openContainer.getSlot(this.slotIndex).inventory.markDirty();
		}

		return this;
	}

	@Override
	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		super.toNBT(nbt);
		nbt.setTag("Stack", this.stack.getMCItemStack().serializeNBT());
		return nbt;
	}
}
