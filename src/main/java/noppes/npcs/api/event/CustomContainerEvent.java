package noppes.npcs.api.event;

import noppes.npcs.api.EventName;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumScriptType;

public class CustomContainerEvent extends CustomNPCsEvent {

	@EventName(EnumScriptType.CUSTOM_CHEST_CLOSED)
	public static class CloseEvent extends CustomContainerEvent {
		public CloseEvent(IPlayer<?> player, IContainer container) {
			super(player, container);
		}
	}

	@EventName(EnumScriptType.CUSTOM_CHEST_CLICKED)
	public static class SlotClickedEvent extends CustomContainerEvent {
		public IItemStack heldItem;
		public int slot;
		public IItemStack slotItem;

		public SlotClickedEvent(IPlayer<?> player, IContainer container, int slotId, IItemStack slotItem,
				IItemStack heldItem) {
			super(player, container);
			this.slotItem = slotItem;
			this.heldItem = heldItem;
			this.slot = slotId;
		}
	}

	public IContainer container;

	public IPlayer<?> player;

	public CustomContainerEvent(IPlayer<?> playerIn, IContainer containerIn) {
		container = containerIn;
		player = playerIn;
	}
}
