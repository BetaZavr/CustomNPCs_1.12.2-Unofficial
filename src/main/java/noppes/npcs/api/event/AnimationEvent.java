package noppes.npcs.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

public class AnimationEvent extends CustomNPCsEvent {

	public static class NextFrameEvent extends AnimationEvent {
		public NextFrameEvent(EntityLivingBase entity, AnimationConfig anim, int fID, long tTicks, long aTicks) {
			super(entity, anim, "animationNextFrameEvent", fID, tTicks, aTicks);
		}
	}

	public static class StartEvent extends AnimationEvent {
		public StartEvent(EntityLivingBase entity, AnimationConfig anim, int fID, long tTicks, long aTicks) {
			super(entity, anim, "animationStartEvent", fID, tTicks, aTicks);
		}
	}

	public static class StopEvent extends AnimationEvent {
		public StopEvent(EntityLivingBase entity, AnimationConfig anim, int fID, long tTicks, long aTicks) {
			super(entity, anim, "animationStopEvent", fID, tTicks, aTicks);
		}
	}

	public static class UpdateEvent extends AnimationEvent {
		public UpdateEvent(EntityLivingBase entity, AnimationConfig anim, int fID, long tTicks, long aTicks) {
			super(entity, anim, "animationUpdateEvent", fID, tTicks, aTicks);
		}
	}

	public EntityLivingBase entity;
	public AnimationConfig animation;
	public int frameId = -1;
	public long totalTicks = -1;
	public long animationTicks = -1;
	public String nameEvent;

	public AnimationEvent(EntityLivingBase entity, AnimationConfig anim, String name, int fID, long tTicks, long aTicks) {
		super();
		this.entity = entity;
		nameEvent = name;
		animation = anim;
		frameId = fID;
		totalTicks = tTicks;
		animationTicks = aTicks;
	}

}
