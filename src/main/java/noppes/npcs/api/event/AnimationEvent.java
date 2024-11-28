package noppes.npcs.api.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.constants.EnumAnimationStages;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

public class AnimationEvent extends CustomNPCsEvent {

	public static class NextFrameEvent extends AnimationEvent {
		public NextFrameEvent(EntityLivingBase entity, AnimationConfig anim, int fID, long tTicks, EnumAnimationStages stage) {
			super(entity, anim, "animationNextFrameEvent", fID, tTicks, stage);
		}
	}

	public static class StartEvent extends AnimationEvent {
		public StartEvent(EntityLivingBase entity, AnimationConfig anim, int fID, long tTicks, EnumAnimationStages stage) {
			super(entity, anim, "animationStartEvent", fID, tTicks, stage);
		}
	}

	public static class StopEvent extends AnimationEvent {
		public StopEvent(EntityLivingBase entity, AnimationConfig anim, int fID, long tTicks, EnumAnimationStages stage) {
			super(entity, anim, "animationStopEvent", fID, tTicks, stage);
		}
	}

	public static class UpdateEvent extends AnimationEvent {
		public UpdateEvent(EntityLivingBase entity, AnimationConfig anim, int fID, long tTicks, EnumAnimationStages stage) {
			super(entity, anim, "animationUpdateEvent", fID, tTicks, stage);
		}
	}

	public EntityLivingBase entity;
	public AnimationConfig animation;
	public EnumAnimationStages stage;
	public int frameId = -1;
	public long ticks = -1;
	public String nameEvent;

	public AnimationEvent(EntityLivingBase entity, AnimationConfig anim, String name, int fID, long tTicks, EnumAnimationStages aStage) {
		super();
		this.entity = entity;
		nameEvent = name;
		animation = anim;
		frameId = fID;
		ticks = tTicks;
		stage = aStage;
	}

}
