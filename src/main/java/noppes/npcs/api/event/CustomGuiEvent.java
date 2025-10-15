package noppes.npcs.api.event;

import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.EventName;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumScriptType;

public class CustomGuiEvent extends CustomNPCsEvent {

	@EventName(EnumScriptType.CUSTOM_GUI_BUTTON)
	public static class ButtonEvent extends CustomGuiEvent {
		public int buttonId;

		public ButtonEvent(IPlayer<?> player, ICustomGui gui, int buttonId) {
			super(player, gui);
			this.buttonId = buttonId;
		}
	}

	@EventName(EnumScriptType.CUSTOM_GUI_CLOSED)
	public static class CloseEvent extends CustomGuiEvent {
		public CloseEvent(IPlayer<?> player, ICustomGui gui) {
			super(player, gui);
		}
	}

	@EventName(EnumScriptType.KEY_GUI_UP)
	public static class KeyPressedEvent extends CustomGuiEvent {
		public int key;

		public KeyPressedEvent(IPlayer<?> player, ICustomGui gui, int k) {
			super(player, gui);
			this.key = k;
		}
	}

	@EventName(EnumScriptType.CUSTOM_GUI_SCROLL)
	public static class ScrollEvent extends CustomGuiEvent {
		public boolean doubleClick;
		public int scrollId;
		public int scrollIndex;
		public String[] selection;

		public ScrollEvent(IPlayer<?> player, ICustomGui gui, int scrollId, int scrollIndex, String[] selection,
				boolean doubleClick) {
			super(player, gui);
			this.scrollId = scrollId;
			this.selection = selection;
			this.doubleClick = doubleClick;
			this.scrollIndex = scrollIndex;
		}
	}

	@Cancelable
	@EventName(EnumScriptType.CUSTOM_GUI_SLOT_CLICKED)
	public static class SlotClickEvent extends CustomGuiEvent {

		public String clickType;
		public int dragType;
		public IItemStack heldItem;
		public int slotId;
		public IItemStack stack;
		public Slot slot;

		public SlotClickEvent(IPlayer<?> player, ICustomGui gui, int slotId, IItemStack stack, int dragType,
				String clickType, IItemStack heldItem, Slot slot) {
			super(player, gui);
			this.slotId = slotId;
			this.stack = stack;
			this.dragType = dragType;
			this.clickType = clickType;
			this.heldItem = heldItem;
			this.slot = slot;
		}
	}

	@EventName(EnumScriptType.CUSTOM_GUI_SLOT)
	public static class SlotEvent extends CustomGuiEvent {
		// New
		public IItemStack heldItem;
		public int slotId;
		public IItemStack stack;

		public SlotEvent(IPlayer<?> player, ICustomGui gui, int slotId, IItemStack stack, IItemStack heldItem) {
			super(player, gui);
			this.slotId = slotId;
			this.stack = stack;
			// New
			this.heldItem = heldItem;
		}
	}

	public ICustomGui gui;

	public IPlayer<?> player;

	public CustomGuiEvent(IPlayer<?> playerIn, ICustomGui guiIn) {
		player = playerIn;
		gui = guiIn;
	}

}
