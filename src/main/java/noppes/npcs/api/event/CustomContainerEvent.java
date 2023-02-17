package noppes.npcs.api.event;

import noppes.npcs.api.IContainer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;

public class CustomContainerEvent extends CustomNPCsEvent {

	public IContainer container;
	public IPlayer<?> player;

	public CustomContainerEvent(IPlayer<?> player, IContainer container) {
		this.container = container;
		this.player = player;
	}
	
	public static class CloseEvent extends CustomContainerEvent {
		public CloseEvent(IPlayer<?> player, IContainer container) {
			super(player, container);
		}
	}

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
}
