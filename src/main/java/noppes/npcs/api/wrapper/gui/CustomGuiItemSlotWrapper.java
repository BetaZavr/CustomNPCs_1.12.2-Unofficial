package noppes.npcs.api.wrapper.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.containers.ContainerCustomGui;

import java.util.Objects;

public class CustomGuiItemSlotWrapper extends CustomGuiComponentWrapper implements IItemSlot {

	protected IItemStack stack = ItemStackWrapper.AIR;
	protected EntityPlayer player = null;
	protected Slot slot = null;
	protected boolean showBack = true;
	protected int slotIndex = 0;

	public CustomGuiItemSlotWrapper() { this(0, 0, null); }

	public CustomGuiItemSlotWrapper(int x, int y, IItemStack stack) {
		setPos(x, y);
		setStack(stack);
	}

	@Override
	public CustomGuiComponentWrapper fromNBT(NBTTagCompound nbt) {
		super.fromNBT(nbt);
		setStack(Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(new ItemStack(nbt.getCompoundTag("Stack"))));
		showBack = nbt.getBoolean("ShowBack");
		return this;
	}

	@Override
	public Slot getMCSlot() { return slot; }

	@Override
	public IItemStack getStack() { return stack; }

	@Override
	public int getType() { return GuiComponentType.ITEM_SLOT.get(); }

	@Override
	public boolean hasStack() { return stack != null && !stack.isEmpty(); }

	@Override
	public boolean isShowBack() { return showBack; }

	@Override
	public void setShowBack(boolean bo) { showBack = bo; }

	@Override
	public IItemSlot setStack(IItemStack itemStack) {
		if (itemStack == null) { stack = ItemStackWrapper.AIR; }
		else { stack = itemStack; }
		if (player != null && player.openContainer instanceof ContainerCustomGui) {
			player.openContainer.getSlot(slotIndex).inventory.setInventorySlotContents(slotIndex, stack.getMCItemStack());
			player.openContainer.getSlot(slotIndex).inventory.markDirty();
		}
		return this;
	}

	@Override
	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		super.toNBT(nbt);
		nbt.setTag("Stack", stack.getMCItemStack().serializeNBT());
		nbt.setBoolean("ShowBack", showBack);
		return nbt;
	}

	public void setPlayer(EntityPlayerMP playerIn) { player = playerIn; }

	public void setIndex(int index) {
		if (index < 0) { index *= -1; }
		slotIndex = index;
	}

	public void setPlayerAndSlot(Slot slotIn, EntityPlayer playerIn) {
		slot = slotIn;
		player = playerIn;
	}

}
