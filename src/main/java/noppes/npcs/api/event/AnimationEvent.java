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
	public int frameId;
	public long ticks;
	public String nameEvent;

	public AnimationEvent(EntityLivingBase entity, AnimationConfig anim, String name) {
		super();
		this.entity = entity;
		nameEvent = name;
		animation = anim;
		if (entity instanceof EntityNPCInterface) {
			if (((EntityNPCInterface) entity).animation == null) {
				frameId = -1;
				ticks = -1;
			} else {
				frameId = ((EntityNPCInterface) entity).animation.animationFrame;
				int startFrameTick;
				if (!anim.ticks.containsKey(this.frameId)) { startFrameTick = anim.ticks.get(0); }
				else { startFrameTick = anim.ticks.get(this.frameId); }
				ticks = (int) (this.entity.world.getTotalWorldTime() - ((EntityNPCInterface) entity).animation.startAnimationTime)- startFrameTick;
			}
		}
		if (entity instanceof EntityPlayer) {
			PlayerData data = PlayerData.get((EntityPlayer) entity);
			if (data != null) {
				if (data.animation == null) {
					frameId = -1;
					ticks = -1;
				} else {
					frameId = data.animation.animationFrame;
					int startFrameTick;
					if (!anim.ticks.containsKey(this.frameId)) { startFrameTick = anim.ticks.get(0); }
					else { startFrameTick = anim.ticks.get(this.frameId); }
					ticks = (int) (this.entity.world.getTotalWorldTime() - data.animation.startAnimationTime) - startFrameTick;
				}
			}
		}
	}

}
