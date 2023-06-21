package noppes.npcs.api.event;

import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;

public class WorldEvent extends CustomNPCsEvent {

	public IWorld world;

	public WorldEvent(IWorld world) {
		this.world = world;
	}
	
	public static class ScriptCommandEvent extends WorldEvent {
		public String[] arguments;
		public IPos pos;

		public ScriptCommandEvent(IWorld world, IPos pos, String[] arguments) {
			super(world);
			this.arguments = arguments;
			this.pos = pos;
		}
	}
	
	public static class ScriptTriggerEvent extends WorldEvent {
		
		public Object[] arguments;
		public IPos pos;
		public IEntity<?> entity;
		public int id;
		
		public ScriptTriggerEvent(int id, IWorld world, IPos pos, IEntity<?> entity, Object[] arguments) {
			super(world);
			this.id = id;
			this.arguments = arguments;
			this.pos = pos;
			this.entity = entity;
		}
		
	}
	
}
