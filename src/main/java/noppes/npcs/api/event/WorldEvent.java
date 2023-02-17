package noppes.npcs.api.event;

import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;

public class WorldEvent extends CustomNPCsEvent {
	public static class ScriptCommandEvent extends WorldEvent {
		public String[] arguments;
		public IPos pos;

		public ScriptCommandEvent(IWorld world, IPos pos, String[] arguments) {
			super(world);
			this.arguments = arguments;
			this.pos = pos;
		}
	}

	public IWorld world;

	public WorldEvent(IWorld world) {
		this.world = world;
	}
}
