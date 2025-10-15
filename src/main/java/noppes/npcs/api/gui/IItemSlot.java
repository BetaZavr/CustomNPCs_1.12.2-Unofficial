package noppes.npcs.api.gui;

import net.minecraft.inventory.Slot;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface IItemSlot extends ICustomGuiComponent {

	Slot getMCSlot();

	IItemStack getStack();

	boolean hasStack();

	boolean isShowBack();

	void setShowBack(@ParamName("bo") boolean bo);

	IItemSlot setStack(@ParamName("stack") IItemStack stack);

}
