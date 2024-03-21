package noppes.npcs.api.event;

import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.entity.EntityNPCInterface;

public class AnimationEvent
extends CustomNPCsEvent {

	public static class NextFrameEvent extends AnimationEvent {
		public NextFrameEvent(EntityNPCInterface npc, AnimationConfig anim) { super(npc, anim, "animationNextFrameEvent"); }
	}
	
	public static class StartEvent extends AnimationEvent {
		public StartEvent(EntityNPCInterface npc, AnimationConfig anim) {super(npc, anim, "animationStartEvent"); }
	}
	
	public static class StopEvent extends AnimationEvent {
		public StopEvent(EntityNPCInterface npc, AnimationConfig anim) { super(npc, anim, "animationStopEvent"); }
	}

	public static class UpdateEvent extends AnimationEvent {
		public UpdateEvent(EntityNPCInterface npc, AnimationConfig anim) { super(npc, anim, "animationUpdateEvent"); }
	}
	
	public EntityNPCInterface npc;
	public AnimationConfig animation;
	public int frameId;
	public long ticks;
	public String nameEvent;
	
	public AnimationEvent(EntityNPCInterface npc, AnimationConfig anim, String name) {
		super();
		this.npc = npc;
		nameEvent = name;
		animation = anim;
		frameId = npc.animation.frame;
		ticks = npc.world.getTotalWorldTime() - npc.animation.startFrameTick;
	}
	
	
}
