package noppes.npcs.api.gui;

import net.minecraft.inventory.Slot;
import noppes.npcs.api.item.IItemStack;

public interface IItemSlot extends ICustomGuiComponent {

	Slot getMCSlot();

	IItemStack getStack();

	boolean hasStack();

	boolean isShowBack();

	void setShowBack(boolean bo);

	IItemSlot setStack(IItemStack stack);

}
