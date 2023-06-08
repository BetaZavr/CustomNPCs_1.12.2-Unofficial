package noppes.npcs.api.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;

@Cancelable
public class ForgeEvent extends CustomNPCsEvent {
	
	@Cancelable
	public static class EntityEvent extends ForgeEvent {
		public IEntity<?> entity;

		public EntityEvent(net.minecraftforge.event.entity.EntityEvent event, IEntity<?> entity) {
			super((Event) event);
			this.entity = entity;
		}
	}

	public static class InitEvent extends ForgeEvent {
		public InitEvent() {
			super(null);
		}
	}

	@Cancelable
	public static class WorldEvent extends ForgeEvent {
		public IWorld world;

		public WorldEvent(net.minecraftforge.event.world.WorldEvent event, IWorld world) {
			super((Event) event);
			this.world = world;
		}
	}

	public Event event;

	public ForgeEvent(Event event) {
		this.event = event;
	}
	
}
