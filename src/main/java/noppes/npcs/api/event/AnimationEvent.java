package noppes.npcs.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

public class AnimationEvent extends CustomNPCsEvent {

	public static class NextFrameEvent extends AnimationEvent {
		public NextFrameEvent(EntityLivingBase entity, AnimationConfig anim) {
			super(entity, anim, "animationNextFrameEvent");
		}
	}

	public static class StartEvent extends AnimationEvent {
		public StartEvent(EntityLivingBase entity, AnimationConfig anim) {
			super(entity, anim, "animationStartEvent");
		}
	}

	public static class StopEvent extends AnimationEvent {
		public StopEvent(EntityLivingBase entity, AnimationConfig anim) {
			super(entity, anim, "animationStopEvent");
		}
	}

	public static class UpdateEvent extends AnimationEvent {
		public UpdateEvent(EntityLivingBase entity, AnimationConfig anim) {
			super(entity, anim, "animationUpdateEvent");
		}
	}

	public EntityLivingBase entity;
	public AnimationConfig animation;
	public int frameId = -1;
	public long totalTicks = -1;
	public long animationTicks = -1;
	public String nameEvent;

	public AnimationEvent(EntityLivingBase entity, AnimationConfig anim, String name) {
		super();
		this.entity = entity;
		nameEvent = name;
		animation = anim;
		if (anim != null) {
			int tt = anim.totalTicks;
			if (anim.totalTicks == 0) {
				anim.resetTicks();
				tt = anim.totalTicks;
			}
			if (tt <= 0) { tt = 1; }
			if (entity instanceof EntityPlayer) {
				PlayerData data = PlayerData.get((EntityPlayer) entity);
				if (data != null && data.animation != null) {
					totalTicks = (int) (this.entity.world.getTotalWorldTime() - data.animation.startAnimationTime) % tt;
				}
			} else if (entity instanceof EntityNPCInterface && ((EntityNPCInterface) entity).animation != null) {
				totalTicks = (int) (this.entity.world.getTotalWorldTime() - ((EntityNPCInterface) entity).animation.startAnimationTime) % tt;
			}
			if (totalTicks >= 0) {
				frameId = anim.getAnimationFrameByTime(totalTicks);
				animationTicks = totalTicks - anim.ticks.get(frameId);
			}
		}
	}

}
