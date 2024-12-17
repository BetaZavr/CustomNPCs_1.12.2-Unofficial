package noppes.npcs.entity.data;

import java.util.*;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.entity.data.INPCAnimation;
import noppes.npcs.client.model.animation.*;
import noppes.npcs.constants.*;
import noppes.npcs.entity.EntityNPCInterface;

public class DataAnimation implements INPCAnimation {

	public final EntityLivingBase entity;

	private final AnimationHandler animationHandler;
	private final EmotionHandler emotionHandler;

	// Tools
	public final Map<EnumParts, Boolean> showParts = new HashMap<>();
	public final Map<EnumParts, Boolean> showArmorParts = new HashMap<>();
	public final Map<EnumParts, Boolean> showAWParts = new HashMap<>();

	public DataAnimation(EntityLivingBase main) {
		entity = main;
		animationHandler = new AnimationHandler(main);
		emotionHandler = new EmotionHandler(main);
		showParts.put(EnumParts.HEAD, true);
		showParts.put(EnumParts.BODY, true);
		showParts.put(EnumParts.ARM_RIGHT, true);
		showParts.put(EnumParts.ARM_LEFT, true);
		showParts.put(EnumParts.LEG_RIGHT, true);
		showParts.put(EnumParts.LEG_LEFT, true);

		showAWParts.put(EnumParts.HEAD, true);
		showAWParts.put(EnumParts.BODY, true);
		showAWParts.put(EnumParts.ARM_RIGHT, true);
		showAWParts.put(EnumParts.ARM_LEFT, true);
		showAWParts.put(EnumParts.LEG_RIGHT, true);
		showAWParts.put(EnumParts.LEG_LEFT, true);

		showArmorParts.put(EnumParts.HEAD, true);
		showArmorParts.put(EnumParts.BODY, true);
		showArmorParts.put(EnumParts.ARM_RIGHT, true);
		showArmorParts.put(EnumParts.ARM_LEFT, true);
		showArmorParts.put(EnumParts.LEG_RIGHT, true);
		showArmorParts.put(EnumParts.LEG_LEFT, true);
		showArmorParts.put(EnumParts.FEET_RIGHT, true);
		showArmorParts.put(EnumParts.FEET_LEFT, true);
	}

	public void resetShowParts() {
		showParts.replaceAll((k, v) -> true);
		showArmorParts.replaceAll((k, v) -> true);
	}

	public void resetShowAWParts() {
		showAWParts.replaceAll((k, v) -> true);
	}

	public void reset() {
		stopAnimation();
		stopEmotion();
		if (entity == null || !entity.isServerWorld()) { return; }
		if (entity instanceof EntityPlayerMP) { Server.sendToAll(CustomNpcs.Server, EnumPacketClient.ANIMATION_DATA_SET, false, entity.world.provider.getDimension(), entity.getUniqueID(), save(new NBTTagCompound())); }
		else if (entity instanceof EntityNPCInterface) { Server.sendToAll(CustomNpcs.Server, EnumPacketClient.ANIMATION_DATA_SET, true, entity.world.provider.getDimension(), entity.getEntityId(), save(new NBTTagCompound())); }
	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, float partialTicks) {
		animationHandler.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, partialTicks);
		// set show parts
		if (animationHandler.currentFrame != null) {
			// show model parts
			for (PartConfig part : animationHandler.currentFrame.parts.values()) {
				showParts.put(part.getEnumType(), part.isShow());
			}
			// show armor
			showArmorParts.put(EnumParts.HEAD, animationHandler.currentFrame.showHelmet);
			showArmorParts.put(EnumParts.BODY, animationHandler.currentFrame.showBody);
			showArmorParts.put(EnumParts.ARM_RIGHT, animationHandler.currentFrame.showBody);
			showArmorParts.put(EnumParts.ARM_LEFT, animationHandler.currentFrame.showBody);
			showArmorParts.put(EnumParts.LEG_RIGHT, animationHandler.currentFrame.showLegs);
			showArmorParts.put(EnumParts.LEG_LEFT, animationHandler.currentFrame.showLegs);
			showArmorParts.put(EnumParts.FEET_RIGHT, animationHandler.currentFrame.showFeets);
			showArmorParts.put(EnumParts.FEET_LEFT, animationHandler.currentFrame.showFeets);
		}
	}

	public void stopAnimation() {
		if (animationHandler.activeAnimation != null) {
			if (animationHandler.activeAnimation.hasEmotion()) { stopEmotion(); }
			if (entity != null && entity.isServerWorld()) {
				if (entity instanceof EntityPlayerMP) { Server.sendToAll(CustomNpcs.Server, EnumPacketClient.ANIMATION_DATA_STOP_ANIMATION, false, entity.world.provider.getDimension(), entity.getUniqueID()); }
				else if (entity instanceof EntityNPCInterface) { Server.sendToAll(CustomNpcs.Server, EnumPacketClient.ANIMATION_DATA_STOP_ANIMATION, true, entity.world.provider.getDimension(), entity.getEntityId()); }
			}
		}
		animationHandler.stopAnimation();
	}

	public void stopEmotion() {
		if (entity != null && entity.isServerWorld() && emotionHandler.activeEmotion != null) {
			if (entity instanceof EntityPlayerMP) { Server.sendToAll(CustomNpcs.Server, EnumPacketClient.ANIMATION_DATA_STOP_EMOTION, false, entity.world.provider.getDimension(), entity.getUniqueID()); }
			else if (entity instanceof EntityNPCInterface) { Server.sendToAll(CustomNpcs.Server, EnumPacketClient.ANIMATION_DATA_STOP_EMOTION, true, entity.world.provider.getDimension(), entity.getEntityId()); }
		}
		emotionHandler.stopEmotion();
	}

	public boolean hasAnim(AnimationKind type) {
		return animationHandler.hasAnim(type);
	}

	public void load(NBTTagCompound compound) {
		animationHandler.load(compound);
		emotionHandler.load(compound);
	}

	public NBTTagCompound save(NBTTagCompound compound) {
		animationHandler.save(compound);
		emotionHandler.save(compound);
		return compound;
	}

	public boolean isAnimated() {
		return animationHandler.isAnimated();
	}

	public void updateTime() {
		animationHandler.updateTime();
		emotionHandler.updateTime();
	}

	public boolean isAnimated(AnimationKind ... types) {
		return animationHandler.isAnimated(types);
	}

	// a new motion animation is selected
	public void resetWalkAndStandAnimations() {
		if (entity == null || entity.world.getTotalWorldTime() % 20 != 0) { return; }
		Map<Integer, Integer> map = animationHandler.resetWalkAndStandAnimations();
		if (map != null) {
			if (entity instanceof EntityPlayerMP) { Server.sendToAll(CustomNpcs.Server, EnumPacketClient.ANIMATION_DATA_BASE_ANIMATIONS, false, entity.world.provider.getDimension(), entity.getUniqueID(), map); }
			else if (entity instanceof EntityNPCInterface) { Server.sendToAll(CustomNpcs.Server, EnumPacketClient.ANIMATION_DATA_BASE_ANIMATIONS, true, entity.world.provider.getDimension(), entity.getEntityId(), map); }
		}
	}

	// used to select a new animation in EntityNPCInterface
	public AnimationConfig tryRunAnimation(AnimationKind type) {
		AnimationConfig anim = animationHandler.selectAnimation(type);
		if (anim == null) { return null; }
		return tryRunAnimation(anim, type);
	}

	// (Player or NPC) -> this.reset(AnimationKind), or PacketHandlerClient, or Animation GUI
	public AnimationConfig tryRunAnimation(AnimationConfig anim, AnimationKind type) {
		if (entity == null) { return null; }
		if (anim != null && anim.frames.isEmpty()) { anim = null; }
		if (anim == null && animationHandler.activeAnimation != null && !animationHandler.activeAnimation.type.isMovement()) {
			stopAnimation();
			return null;
		}
		anim = animationHandler.tryRunAnimation(anim, type);
		if (!entity.isServerWorld()) { return anim; }
		if (anim == null) {
			if (entity instanceof EntityPlayerMP) { Server.sendToAll(CustomNpcs.Server, EnumPacketClient.ANIMATION_DATA_STOP_ANIMATION, false, entity.world.provider.getDimension(), entity.getUniqueID()); }
			else if (entity instanceof EntityNPCInterface) { Server.sendToAll(CustomNpcs.Server, EnumPacketClient.ANIMATION_DATA_STOP_ANIMATION, true, entity.world.provider.getDimension(), entity.getEntityId()); }
		}
		else {
			if (entity instanceof EntityPlayerMP) { Server.sendToAll(CustomNpcs.Server, EnumPacketClient.ANIMATION_DATA_RUN_ANIMATION, false, entity.world.provider.getDimension(), entity.getUniqueID(), anim.id, type.ordinal()); }
			else if (entity instanceof EntityNPCInterface) { Server.sendToAll(CustomNpcs.Server, EnumPacketClient.ANIMATION_DATA_RUN_ANIMATION, true, entity.world.provider.getDimension(), entity.getEntityId(), anim.id, type.ordinal()); }
		}
		return anim;
	}

	public AnimationConfig getAnimation() { return animationHandler.activeAnimation; }

	public boolean getJump() { return animationHandler.isJump; }

	public void setJump(boolean bo) { animationHandler.isJump = bo; }

	public boolean getSwing() { return animationHandler.isSwing; }

	public void setSwing(boolean bo) { animationHandler.isSwing = bo; }

	public EnumAnimationStages getAnimationStage() { return animationHandler.stage; }

	public boolean getAnimationPartShow(int partId) {
		return animationHandler.currentFrame == null || !animationHandler.currentFrame.parts.containsKey(partId) || animationHandler.currentFrame.parts.get(partId).show;
	}

	public Float[] getAnimationPartData(int partId) { return animationHandler.rotationAngles.get(partId); }

	public void loadBaseAnimations(Map<Object, Object> map) { animationHandler.loadBaseAnimations(map); }

	public AnimationFrameConfig getPreFrame() { return animationHandler.preFrame; }

	public void addAnimation(AnimationKind type, int id) { animationHandler.addAnimation(type, id); }

	public boolean removeAnimation(AnimationKind type, int id) { return animationHandler.removeAnimation(type, id); }

	public boolean hasAnimation(AnimationKind type, int id) { return animationHandler.hasAnimation(type, id); }

	public boolean canSetBaseRotationAngles() { return animationHandler.canSetBaseRotationAngles(); }

	public boolean canBeAnimated() { return animationHandler.canBeAnimated(); }

	public ItemStack getCurrentHeldStack(boolean isMainHand) { return animationHandler.getCurrentHeldStack(isMainHand); }

	// Emotions
	public EmotionFrame getCurrentEmotionFrame() { return emotionHandler.currentEmotionFrame; }

	public EmotionConfig getActiveEmotion() { return emotionHandler.activeEmotion; }

	public int getBaseEmotionId() { return emotionHandler.baseEmotionId; }

	public void setBaseEmotionId(int emotionId) { emotionHandler.baseEmotionId = emotionId; }

	public long getStartEmotionTime() { return emotionHandler.startEmotionTime; }

	public Map<Integer, Float[]> getEmotionData() { return emotionHandler.emts; }

	public void setActiveEmotion(EmotionConfig emotion) { emotionHandler.activeEmotion = emotion; }

}
