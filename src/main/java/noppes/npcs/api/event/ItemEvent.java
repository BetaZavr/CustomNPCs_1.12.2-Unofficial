package noppes.npcs.api.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IEntityItem;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemScripted;

public class ItemEvent extends CustomNPCsEvent {

	@Cancelable
	public static class AttackEvent extends ItemEvent {
		public IPlayer<?> player;
		public Object target;
		public int type;

		public AttackEvent(IItemScripted item, IPlayer<?> player, int type, Object target) {
			super(item);
			this.type = type;
			this.target = target;
			this.player = player;
		}
	}

	public static class InitEvent extends ItemEvent {
		public InitEvent(IItemScripted item) {
			super(item);
		}
	}

	@Cancelable
	public static class InteractEvent extends ItemEvent {
		public IPlayer<?> player;
		public Object target;
		public int type;

		public InteractEvent(IItemScripted item, IPlayer<?> player, int type, Object target) {
			super(item);
			this.type = type;
			this.target = target;
			this.player = player;
		}
	}

	public static class PickedUpEvent extends ItemEvent {
		public IEntityItem<?> entity;
		public IPlayer<?> player;

		public PickedUpEvent(IItemScripted item, IPlayer<?> player, IEntityItem<?> entity) {
			super(item);
			this.entity = entity;
			this.player = player;
		}
	}

	@Cancelable
	public static class SpawnEvent extends ItemEvent {
		public IEntityItem<?> entity;

		public SpawnEvent(IItemScripted item, IEntityItem<?> entity) {
			super(item);
			this.entity = entity;
		}
	}

	@Cancelable
	public static class TossedEvent extends ItemEvent {
		public IEntityItem<?> entity;
		public IPlayer<?> player;

		public TossedEvent(IItemScripted item, IPlayer<?> player, IEntityItem<?> entity) {
			super(item);
			this.entity = entity;
			this.player = player;
		}
	}

	public static class UpdateEvent extends ItemEvent {
		public IPlayer<?> player;

		public UpdateEvent(IItemScripted item, IPlayer<?> player) {
			super(item);
			this.player = player;
		}
	}

	public IItemScripted item;

	public ItemEvent(IItemScripted item) {
		this.item = item;
	}

}
