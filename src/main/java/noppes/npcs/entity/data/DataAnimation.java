package noppes.npcs.entity.data;

import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.Server;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.entity.data.IEmotion;
import noppes.npcs.api.entity.data.INPCAnimation;
import noppes.npcs.api.event.AnimationEvent;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.model.animation.AddedPartConfig;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.client.model.animation.EmotionConfig;
import noppes.npcs.client.model.animation.EmotionFrame;
import noppes.npcs.client.model.animation.PartConfig;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

public class DataAnimation implements INPCAnimation {

	// Animation settings
	public final Map<AnimationKind, List<Integer>> data = Maps.newHashMap();
	private final Map<Integer, Long> waitData = Maps.newHashMap(); // animation ID, time

	// Animation run
	/**
	 * Integer key = 0:head, 1:left arm, 2:right arm, 3:body, 4:left leg, 5:right leg, 6:left stack, 7:right stack
	 * Float[] value = [ 0:rotX, 1:rotY, 2:rotZ, 3:ofsX, 4:ofsY, 5:ofsZ, 6:scX, 7:scY, 8:scZ, 9:rotX1, 10:rotY1 ]
	 */
	public final Map<Integer, Float[]> rots = Maps.newTreeMap();
	public final Map<Integer, List<AddedPartConfig>> addParts = Maps.newTreeMap();
	public AnimationConfig activeAnimation = AnimationConfig.EMPTY;
	// preAnimation preAnimation is never equal to activeAnimation
	public AnimationConfig preAnimation = AnimationConfig.EMPTY;
	private final Map<AnimationKind, AnimationConfig> animData = Maps.newHashMap(); // animation type, animation

	public long startAnimationTime = 0; // is set when the animation starts
	//public int animationFrame = -2;
	private boolean isFastSpeed = false;
	private boolean completeAnimation = false;
	public AnimationFrameConfig currentFrame = AnimationFrameConfig.STANDARD, nextFrame = AnimationFrameConfig.STANDARD;
	public boolean isJump = false, isSwing = false;
	private ResourceLocation animationSound = null;

	// Emotion run
	public final Map<Integer, Float[]> emts = Maps.newTreeMap();
	public EmotionConfig activeEmotion = null;
	public EmotionFrame currentEmotionFrame = null, nextEmotionFrame;
	public long startEmotionTime = 0;
	public int emotionFrame = 0;
	public int baseEmotionId = -1;

	// Tools
	private final EntityLivingBase entity;
	private final Random rnd = new Random();
	public final Map<EnumParts, Boolean> showParts, showArmorParts;
	private float val, valNext;

	public DataAnimation(EntityLivingBase entity) {
		this.entity = entity;
		this.showParts = Maps.newHashMap();
		this.showArmorParts = Maps.newHashMap();
		this.showParts.put(EnumParts.HEAD, true);
		this.showParts.put(EnumParts.BODY, true);
		this.showParts.put(EnumParts.ARM_RIGHT, true);
		this.showParts.put(EnumParts.ARM_LEFT, true);
		this.showParts.put(EnumParts.LEG_RIGHT, true);
		this.showParts.put(EnumParts.LEG_LEFT, true);
		this.showArmorParts.put(EnumParts.HEAD, true);
		this.showArmorParts.put(EnumParts.BODY, true);
		this.showArmorParts.put(EnumParts.ARM_RIGHT, true);
		this.showArmorParts.put(EnumParts.ARM_LEFT, true);
		this.showArmorParts.put(EnumParts.LEG_RIGHT, true);
		this.showArmorParts.put(EnumParts.LEG_LEFT, true);
		this.showArmorParts.put(EnumParts.FEET_RIGHT, true);
		this.showArmorParts.put(EnumParts.FEET_LEFT, true);
		this.checkData();
		this.clear();
	}

	private void checkData() {
		for (AnimationKind type : AnimationKind.values()) {
			if (!data.containsKey(type)) { data.put(type, Lists.newArrayList()); }
		}
		while (data.containsKey(null)) { data.remove(null); }
		for (AnimationKind type : AnimationKind.values()) {
			if (!animData.containsKey(type)) { animData.put(type, AnimationConfig.EMPTY); }
		}
	}

	private float calcValue(float value_0, float value_1, int speed, boolean isSmooth, float ticks, float pt) {
		if (ticks > speed) {
			ticks = speed;
			pt = 0.0f;
		}
		float pi = (float) Math.PI;
		if (isSmooth) {
			this.val = -0.5f * MathHelper.cos(ticks / (float) speed * pi) + 0.5f;
			this.valNext = -0.5f * MathHelper.cos((ticks + 1) / (float) speed * pi) + 0.5f;
		} else {
			this.val = ticks / (float) speed;
			this.valNext = (ticks + 1) / (float) speed;
		}
		float f = this.val + (this.valNext - this.val) * pt;
        return (value_0 + (value_1 - value_0) * f) * 2.0f * pi;
	}

	@Override
	public void clear() {
		this.stopAnimation();
		this.stopEmotion();
		for (List<Integer> ids : data.values()) { ids.clear(); }
		this.updateClient(0);
	}

	@Override
	public IAnimation[] getAnimations(int animationType) {
		if (animationType < 0 || animationType >= AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: " + animationType);
		}
		List<AnimationConfig> list = AnimationController.getInstance().getAnimations(data.get(AnimationKind.get(animationType)));
		return list.toArray(new IAnimation[0]);
	}

	@Override
	public boolean removeAnimation(int animationType, int animationId) {
		if (animationType < 0 || animationType >= AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: " + animationType);
		}
		AnimationKind type = AnimationKind.get(animationType);
		if (!data.containsKey(type)) { data.put(type, Lists.newArrayList()); }
		for (Integer id : data.get(type)) {
			if (id == animationId) {
				if (data.get(type).remove(id)) {
					if (this.activeAnimation != null && this.activeAnimation.getId() == id) { this.stopAnimation(); }
					this.updateClient(5);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void removeAnimations(int animationType) {
		if (animationType < 0 || animationType >= AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: " + animationType);
		}
		AnimationKind type = AnimationKind.get(animationType);
		if (!data.containsKey(type)) {
			this.data.put(type, Lists.newArrayList());
		}
		this.data.get(type).clear();
		if (this.activeAnimation != null && this.activeAnimation.type.get() == animationType) {
			this.stopAnimation();
			this.updateClient(5);
		}
	}

	public void resetAnimValues(float pt) {
		if (this.activeAnimation == null) { return; }

		// animation data
		int totalTicks = (int) (this.entity.world.getTotalWorldTime() - this.startAnimationTime) % this.activeAnimation.totalTicks;
		int animationFrame = this.activeAnimation.getAnimationFrameByTime(totalTicks); // current animation frame ID
		int ticks = totalTicks - this.activeAnimation.ticks.get(animationFrame); // running time of the current animation frame

		// current frame
		this.currentFrame = this.activeAnimation.frames.get(animationFrame);
		if (animationFrame == 0 && this.activeAnimation.type.isQuickStart()) { this.isFastSpeed = true; }
		if (animationFrame == this.activeAnimation.frames.size() - 1 && this.activeAnimation.type.isQuickEnd()) { this.isFastSpeed = true; }

		// Select next Frame
		this.nextFrame = null;
		if (this.activeAnimation.type == AnimationKind.JUMP && !this.isJump && this.completeAnimation) {
			// jump animation completion
			animationFrame = this.activeAnimation.frames.size() - 1;
			this.startAnimationTime = this.entity.world.getTotalWorldTime() + this.activeAnimation.ticks.get(animationFrame);
			this.nextFrame = this.activeAnimation.frames.get(animationFrame);
			if (this.activeAnimation.type.isQuickEnd()) { this.isFastSpeed = true; }
		}
		boolean isSimple = (this.activeAnimation.isSimple && this.activeAnimation.type.isCyclical()) || this.activeAnimation.isEdit == (byte) 2;
		if (animationFrame == 1 && isSimple) {
			// one frame animation
			this.nextFrame = this.activeAnimation.frames.get(animationFrame);
			this.startAnimationTime = this.entity.world.getTotalWorldTime() + this.activeAnimation.ticks.get(animationFrame);
		}
		else if (this.activeAnimation.frames.containsKey(animationFrame + 1)) {
			// can go to the next frame
			this.nextFrame = this.activeAnimation.frames.get(animationFrame + 1);
		}
		else if (this.activeAnimation.repeatLast > 0 || this.activeAnimation.type.isCyclical()) {
			// repeat frames until animation is turned off
			int f = this.activeAnimation.repeatLast <= 0 ? 1 : this.activeAnimation.repeatLast;
			animationFrame = this.activeAnimation.frames.size() - f - 1;
			this.completeAnimation = true;
			if (animationFrame < 0) { animationFrame = 0; }
			this.nextFrame = this.activeAnimation.frames.containsKey(animationFrame) ? this.activeAnimation.frames.get(animationFrame) : this.currentFrame;
			if (f == 1) { pt = 0.0f; }
			this.startAnimationTime = this.entity.world.getTotalWorldTime() + this.activeAnimation.ticks.get(animationFrame);
		}
		else {
			this.stopAnimation();
			if (this.activeAnimation != null) { this.resetAnimValues(pt); }
			return;
		}
		if (this.activeAnimation == null) { return; }

		// adjust frames
		if (this.currentFrame.id == -1) { this.currentFrame = AnimationFrameConfig.STANDARD.copy(); }
		if (this.nextFrame.id == -1) { this.nextFrame = AnimationFrameConfig.STANDARD.copy(); }

		// Speed ticks to next frame
		int speed = this.nextFrame.speed / (this.isFastSpeed ? 3 : 1);

		// for start or finish use settings from animation frame
		if (this.nextFrame.id != -1 && this.currentFrame.id == -1) {
			for (int id : this.nextFrame.parts.keySet()) {
				this.currentFrame.parts.get(id).setDisable(this.nextFrame.parts.get(id).isDisable());
				this.currentFrame.parts.get(id).setShow(this.nextFrame.parts.get(id).isShow());
			}
		}
		if (this.nextFrame.id == -1 && this.currentFrame.id != -1) {
			for (int id : this.nextFrame.parts.keySet()) {
				this.nextFrame.parts.get(id).setDisable(this.currentFrame.parts.get(id).isDisable());
				this.nextFrame.parts.get(id).setShow(this.currentFrame.parts.get(id).isShow());
			}
		}

		// set show parts
		for (PartConfig part : this.currentFrame.parts.values()) { this.showParts.put(part.getEnumType(), part.isShow()); }

		// movement speed depends on the NPCs movement speed
		IAttributeInstance attributeMovement = this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
		if (attributeMovement != null && attributeMovement.getAttributeValue() != 0.0) {
			speed = (int) ((double) speed * (this.entity instanceof EntityNPCInterface ? 0.25 : 0.0999999985098839) / attributeMovement.getAttributeValue());
		}

		// start sound (ignore unloaded entities in GUI)
		if (this.animationSound == null &&
				this.currentFrame.sound != null &&
				this.activeAnimation.isEdit == (byte) 0 &&
				!this.entity.isServerWorld() &&
				this.entity.world.loadedEntityList.contains(this.entity)) {
			MusicController.Instance.playSound(SoundCategory.AMBIENT, this.currentFrame.sound.toString(), (float) this.entity.posX, (float) this.entity.posY, (float) this.entity.posZ, 1.0f, 1.0f);
			this.animationSound = this.currentFrame.sound;
		}

		// show armor
		this.showArmorParts.put(EnumParts.HEAD, this.currentFrame.showHelmet);
		this.showArmorParts.put(EnumParts.BODY, this.currentFrame.showBody);
		this.showArmorParts.put(EnumParts.ARM_RIGHT, this.currentFrame.showBody);
		this.showArmorParts.put(EnumParts.ARM_LEFT, this.currentFrame.showBody);
		this.showArmorParts.put(EnumParts.LEG_RIGHT, this.currentFrame.showLegs);
		this.showArmorParts.put(EnumParts.LEG_LEFT, this.currentFrame.showLegs);
		this.showArmorParts.put(EnumParts.FEET_RIGHT, this.currentFrame.showFeets);
		this.showArmorParts.put(EnumParts.FEET_LEFT, this.currentFrame.showFeets);

		// calculation of exact values for a body part
		for (int partId = 0; partId < this.currentFrame.parts.size(); partId++) {
			PartConfig part0 = this.currentFrame.parts.get(partId);
			PartConfig part1 = this.nextFrame.parts.get(partId);
			if (part0.isDisable() || !part0.isShow() || part1 == null) {
				this.rots.put(part0.id, null);
				continue;
			}
			Float[] values = new Float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f };
			for (int t = 0; t < 3; t++) { // 0:rotations, 1:offsets, 2:scales
				for (int a = 0; a < 3; a++) { // x, y, z
					float value_0;
					float value_1;
					switch (t) {
						case 1: {
							value_0 = 10.0f * part0.offset[a] - 5.0f;
							value_1 = isSimple ? value_0 : 10.0f * part1.offset[a] - 5.0f;
							break;
						}
						case 2: {
							value_0 = part0.scale[a] * 5.0f;
							value_1 = isSimple ? value_0 : part1.scale[a] * 5.0f;
							break;
						}
						default: {
							value_0 = part0.rotation[a];
							value_1 = isSimple ? value_0 : part1.rotation[a];
							if (value_0 < 0.5f && Math.abs(value_0 + 1.0f - value_1) < Math.abs(value_0 - value_1)) {
								value_0 += 1.0f;
							} else if (value_1 < 0.5f && Math.abs(value_1 + 1.0f - value_0) < Math.abs(value_0 - value_1)) {
								value_1 += 1.0f;
							}
							value_0 -= 0.5f;
							value_1 -= 0.5f;
							break;
						}
					}
					values[t * 3 + a] = this.calcValue(value_0, value_1, speed, this.currentFrame.isSmooth(), ticks, pt);
					if (t != 0) { values[t * 3 + a] /= 2 * (float) Math.PI; } // offsets, scales - correction
					if (t == 0 && a == 2) {
						value_0 = part0.rotation[3];
						value_1 = part1.rotation[3];
						if (value_0 < 0.5f && Math.abs(value_0 + 1.0f - value_1) < Math.abs(value_0 - value_1)) {
							value_0 += 1.0f;
						} else if (value_1 < 0.5f && Math.abs(value_1 + 1.0f - value_0) < Math.abs(value_0 - value_1)) {
							value_1 += 1.0f;
						}
						value_0 -= 0.5f;
						value_1 -= 0.5f;
						values[9] = this.calcValue(value_0, value_1, speed, this.currentFrame.isSmooth(), ticks, pt);

						value_0 = part0.rotation[4];
						value_1 = part1.rotation[4];
						if (value_0 < 0.5f && Math.abs(value_0 + 1.0f - value_1) < Math.abs(value_0 - value_1)) {
							value_0 += 1.0f;
						} else if (value_1 < 0.5f && Math.abs(value_1 + 1.0f - value_0) < Math.abs(value_0 - value_1)) {
							value_1 += 1.0f;
						}
						value_0 -= 0.5f;
						value_1 -= 0.5f;
						value_0 *= 0.5f;
						value_1 *= 0.5f;
						values[10] = this.calcValue(value_0, value_1, speed, this.currentFrame.isSmooth(), ticks, pt);

					}
				}
			}
			this.rots.put(part0.id, values);
		}

		// attempt to move to the next frame
		if (ticks >= speed + this.nextFrame.getEndDelay()) {
			this.animationSound = null;
			if (this.nextFrame.id >= 0 || this.activeAnimation.isEdit != 0) {
				// as far as possible
				this.currentFrame = this.nextFrame;
				if (!this.activeAnimation.frames.containsKey(animationFrame + 1)) {
					if (this.activeAnimation.isEdit != (byte) 0) { // repeat in GUI
						if (this.activeAnimation.isEdit == (byte) 1) { this.startAnimationTime = this.entity.world.getTotalWorldTime(); }
						return;
					}
					// complete animation
					this.completeAnimation = true;
					if (this.activeAnimation.repeatLast == 0 && !this.activeAnimation.type.isCyclical()) { this.stopAnimation(); }
					if (this.activeAnimation == null) { return; }
				}
				if (this.activeAnimation.isEdit == (byte) 0) { this.startEvent(new AnimationEvent.NextFrameEvent(this.entity, this.activeAnimation)); }
			}
			else {
				// end animation
                this.stopAnimation();
            }
		}
		else {
			// next tick
			this.startEvent(new AnimationEvent.UpdateEvent(this.entity, this.activeAnimation));
		}
	}

	public void stopAnimation(AnimationKind type) {
		if (this.activeAnimation == null || this.activeAnimation.type != type) { return; }
		if (this.entity.isServerWorld()) { this.activeAnimation = null; }
	}

	@Override
	public void stopAnimation() {
		if (this.activeAnimation == null) { return; }
		if (this.activeAnimation.hasEmotion()) { this.stopEmotion(); }
		if (this.activeAnimation.isEdit == (byte) 0) { this.startEvent(new AnimationEvent.StopEvent(this.entity, this.activeAnimation)); }
		
		this.isJump = false;
		this.isSwing = false;
		this.rots.clear();
		this.val = 0.0f;
		this.valNext = 0.0f;
		if (this.animationSound != null && !this.entity.isServerWorld()) { MusicController.Instance.stopSound(this.animationSound.toString(), SoundCategory.AMBIENT); }
		this.animationSound = null;
		
		if (this.activeAnimation == this.preAnimation) {
			this.activeAnimation = null;
			this.startAnimationTime = 0;
            this.currentFrame = AnimationFrameConfig.STANDARD;
		}
		else {
			if (this.preAnimation != null) {
				this.setAnimation(this.preAnimation, this.preAnimation.type);
				this.preAnimation = this.activeAnimation;
			}
		}
		this.completeAnimation = false;
	}

	public boolean hasAnim(AnimationKind type) {
		if (!data.containsKey(type) || data.get(type).isEmpty()) { return false; }
		AnimationController aData = AnimationController.getInstance();
		for (int id : data.get(type)) {
			if (aData.animations.containsKey(id)) {return true; }
		}
		return false;
	}



	// Emotion
	@Override
	public IEmotion getEmotion() { return this.activeEmotion; }

	public void resetEmtnValues(EntityNPCInterface entity, float pt) {
		if (this.activeEmotion == null || this.activeEmotion.frames.isEmpty() || this.currentEmotionFrame == null) {
			this.activeEmotion = null;
			return;
		}
		if (this.startEmotionTime <= 0) { this.startEmotionTime = this.entity.world.getTotalWorldTime(); }
		// Current anim ticks
		int ticks = (int) (this.entity.world.getTotalWorldTime() - this.startEmotionTime);
		// Speed ticks to next frame
		int speed = this.currentEmotionFrame != null ? this.currentEmotionFrame.speed : 0;
		if (this.emotionFrame < 0) { // new animation
			if (this.activeEmotion == null) { // finishing the old animation
				this.nextEmotionFrame = EmotionFrame.STANDARD.copy();
			}
			else {
				this.nextEmotionFrame = this.activeEmotion.frames.get(0);
				//if (this.currentEmotionFrame == null && this.activeEmotion.isEdit == (byte) 2) { this.currentEmotionFrame = this.activeEmotion.frames.get(0); }
			}
			if (this.currentEmotionFrame == null) { this.currentEmotionFrame = EmotionFrame.STANDARD.copy(); } // start of new animation
			speed = this.nextEmotionFrame.speed;
		} else {
			if (this.activeEmotion == null) { // returns to original position
				this.nextEmotionFrame = EmotionFrame.STANDARD.copy();
				speed = this.nextEmotionFrame.speed;
			} else if (this.activeEmotion.frames.size() == 1) { // simple animation
				this.nextEmotionFrame = this.activeEmotion.frames.get(0);
			} else if (this.activeEmotion.frames.containsKey(this.emotionFrame + 1)) { // next frame
				this.nextEmotionFrame = this.activeEmotion.frames.get(this.emotionFrame + 1);
			} else if (this.activeEmotion.repeatLast > 0) { // repeat frames until animation is turned off
				int f = this.activeEmotion.repeatLast;
				this.emotionFrame = this.activeEmotion.frames.size() - f;
				if (this.emotionFrame < 0) { this.emotionFrame = 0; }
				this.nextEmotionFrame = this.activeEmotion.frames.containsKey(this.emotionFrame) ? this.activeEmotion.frames.get(this.emotionFrame) : this.currentEmotionFrame;
			} else {
				this.nextEmotionFrame = EmotionFrame.STANDARD.copy();
				speed = this.nextEmotionFrame.speed;
			}
		}
		// calculation of exact values for a body part
		for (int partId = 0; partId < 6; partId++) { // 0:eyeRight, 1:eyeLeft, 2:pupilRight, 3:pupilLeft, 4:browRight, 5:browLeft
			Float[] values = new Float[] { 0.0f, 0.0f, 0.0f, 1.0f, 1.0f }; // ofsX, ofsY, scX, scY, rot
			for (int t = 0; t < 3; t++) { // 0:offsets, 1:scales, 2:rotations
				for (int a = 0; a < 2; a++) { // x, y
					if (t == 2 && a == 1) { continue; }
					float value_0;
					float value_1;
					switch (t) {
						case 1: { // scales
							switch (partId) {
								case 1: { // eyeLeft
									value_0 = this.currentEmotionFrame.scaleEye[a + 2] + 0.5f;
									value_1 = this.nextEmotionFrame.scaleEye[a + 2] + 0.5f;
									break;
								}
								case 2: { // pupilRight
									value_0 = this.currentEmotionFrame.scalePupil[a] + 0.5f;
									value_1 = this.nextEmotionFrame.scalePupil[a] + 0.5f;
									break;
								}
								case 3: { // pupilLeft
									value_0 = this.currentEmotionFrame.scalePupil[a + 2] + 0.5f;
									value_1 = this.nextEmotionFrame.scalePupil[a + 2] + 0.5f;
									break;
								}
								case 4: { // browRight
									value_0 = this.currentEmotionFrame.scaleBrow[a] + 0.5f;
									value_1 = this.nextEmotionFrame.scaleBrow[a] + 0.5f;
									break;
								}
								case 5: { // browLeft
									value_0 = this.currentEmotionFrame.scaleBrow[a + 2] + 0.5f;
									value_1 = this.nextEmotionFrame.scaleBrow[a + 2] + 0.5f;
									break;
								}
								default: { // eyeRight
									value_0 = this.currentEmotionFrame.scaleEye[a] + 0.5f;
									value_1 = this.nextEmotionFrame.scaleEye[a] + 0.5f;
									break;
								}
							}
							break;
						}
						case 2: { // rotations
							switch (partId) {
								case 1: { // eyeLeft
									value_0 = (this.currentEmotionFrame.rotEye[1] - 0.5f) * 360.0f;
									value_1 = (this.nextEmotionFrame.rotEye[1] - 0.5f) * 360.0f;
									break;
								}
								case 2: { // pupilRight
									value_0 = (this.currentEmotionFrame.rotPupil[0] - 0.5f) * 360.0f;
									value_1 = (this.nextEmotionFrame.rotPupil[0] - 0.5f) * 360.0f;
									break;
								}
								case 3: { // pupilLeft
									value_0 = (this.currentEmotionFrame.rotPupil[1] - 0.5f) * 360.0f;
									value_1 = (this.nextEmotionFrame.rotPupil[1] - 0.5f) * 360.0f;
									break;
								}
								case 4: { // browRight
									value_0 = (this.currentEmotionFrame.rotBrow[0] - 0.5f) * 360.0f;
									value_1 = (this.nextEmotionFrame.rotBrow[0] - 0.5f) * 360.0f;
									break;
								}
								case 5: { // browLeft
									value_0 = (this.currentEmotionFrame.rotBrow[1] - 0.5f) * 360.0f;
									value_1 = (this.nextEmotionFrame.rotBrow[1] - 0.5f) * 360.0f;
									break;
								}
								default: { // eyeRight
									value_0 = (this.currentEmotionFrame.rotEye[0] - 0.5f) * 360.0f;
									value_1 = (this.nextEmotionFrame.rotEye[0] - 0.5f) * 360.0f;
									break;
								}
							}
							break;
						}
						default: { // offsets
							switch (partId) {
								case 1: { // eyeLeft
									value_0 = (this.currentEmotionFrame.offsetEye[a + 2] - 0.5f) * 2.0f;
									value_1 = (this.nextEmotionFrame.offsetEye[a + 2] - 0.5f) * 2.0f;
									break;
								}
								case 2: { // pupilRight
									value_0 = (this.currentEmotionFrame.offsetPupil[a] - 0.5f) * 2.0f;
									value_1 = (this.nextEmotionFrame.offsetPupil[a] - 0.5f) * 2.0f;
									break;
								}
								case 3: { // pupilLeft
									value_0 = (this.currentEmotionFrame.offsetPupil[a + 2] - 0.5f) * 2.0f;
									value_1 = (this.nextEmotionFrame.offsetPupil[a + 2] - 0.5f) * 2.0f;
									break;
								}
								case 4: { // browRight
									value_0 = (this.currentEmotionFrame.offsetBrow[a] - 0.5f) * 2.0f;
									value_1 = (this.nextEmotionFrame.offsetBrow[a] - 0.5f) * 2.0f;
									break;
								}
								case 5: { // browLeft
									value_0 = (this.currentEmotionFrame.offsetBrow[a + 2] - 0.5f) * 2.0f;
									value_1 = (this.nextEmotionFrame.offsetBrow[a + 2] - 0.5f) * 2.0f;
									break;
								}
								default: { // eyeRight
									value_0 = (this.currentEmotionFrame.offsetEye[a] - 0.5f) * 2.0f;
									value_1 = (this.nextEmotionFrame.offsetEye[a] - 0.5f) * 2.0f;
									break;
								}
							}
							break;
						}
					}
					values[t * 2 + a] = this.calcValue(value_0, value_1, speed, this.currentEmotionFrame.isSmooth(), ticks, pt);
					if (t != 0) { values[t * 2 + a] /= 2 * (float) Math.PI; }
				}
			}
			emts.put(partId, values);
		}

		if (ticks >= speed + this.nextEmotionFrame.getEndDelay()) {
			this.emotionFrame++;
			this.startEmotionTime = entity.world.getTotalWorldTime();
			this.currentEmotionFrame = this.nextEmotionFrame;
		}
	}

	@Override
	public void startEmotion(int emotionId) {
		IEmotion emotion = AnimationController.getInstance().getEmotion(emotionId);
		if (emotion == null) { return; }
		if (!this.entity.isServerWorld()) {
			this.activeEmotion = (EmotionConfig) emotion;
			this.emotionFrame = 0;
			this.startEmotionTime = 0;
		} else {
			this.updateClient(4, emotion.getId());
		}
	}

	@Override
	public void stopEmotion() {
		if (this.activeEmotion != null) {
			this.updateClient(3, this.activeEmotion.id);
			this.currentEmotionFrame = this.activeEmotion.frames.get(this.emotionFrame);
			this.activeEmotion = null;
		}
		else { this.currentEmotionFrame = EmotionFrame.STANDARD; }
		this.emts.clear();
		this.val = 0.0f;
		this.valNext = 0.0f;
		this.startEmotionTime = 0;
		this.emotionFrame = -1;
		this.currentEmotionFrame = null;
		this.nextEmotionFrame = null;
	}

	@Override
	public INbt getNbt() {
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(this.save(new NBTTagCompound()));
	}

	public void load(NBTTagCompound compound) {
		if (compound.hasKey("BaseEmotionId", 3)) { this.baseEmotionId = compound.getInteger("BaseEmotionId"); }
		data.clear();
		AnimationController aData = AnimationController.getInstance();
		if (compound.hasKey("AllAnimations", 9)) {
			for (int c = 0; c < compound.getTagList("AllAnimations", 10).tagCount(); c++) {
				NBTTagCompound nbtCategory = compound.getTagList("AllAnimations", 10).getCompoundTagAt(c);
				int t = nbtCategory.getInteger("Category");
				if (t < 0) { t *= -1; }
				AnimationKind type = AnimationKind.get(t % AnimationKind.values().length);
				if (type == null) {
					LogWriter.warn("Try load AnimationKind ID:"+t+". Missed.");
					continue;
				}
				List<Integer> list = Lists.newArrayList();
				int tagType = nbtCategory.getTag("Animations").getId();
				if (tagType == 11) { // OLD version
					for (int id : nbtCategory.getIntArray("Animations")) {
						if (!list.contains(id)) { list.add(id); }
					}
				}
				else if (tagType == 9) { // NEW version
					int listType = ((NBTTagList) nbtCategory.getTag("Animations")).getTagType();
					if (listType == 10 && entity != null && this.entity.isServerWorld()) { // OLD in main CNPCs mod
						for (int i = 0; i < nbtCategory.getTagList("Animations", 10).tagCount(); i++) {
							NBTTagCompound nbt = nbtCategory.getTagList("Animations", 10).getCompoundTagAt(i);
							int id = nbt.getInteger("ID");
							String name = entity.getName() + "_" + nbt.getString("Name");
							AnimationConfig anim = (AnimationConfig) aData.getAnimation(id);
							if (entity.world.getEntityByID(entity.getEntityId()) != null && anim == null || !anim.getName().equals(name)) {
								boolean found = false;
								if (anim != null) {
									for (AnimationConfig ac : aData.animations.values()) {
										if (ac.name.equals(anim.name)) {
											found = true;
											anim = ac;
										}
									}
								}
								if (!found) { // Converting
									anim = (AnimationConfig) aData.createNewAnim();
									id = anim.id;
									if (!anim.immutable) { anim.load(nbt); }
									anim.name = name;
									anim.id = id;
									Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, EnumSync.AnimationData, anim.save());
								}
							}
							if (!list.contains(id)) { list.add(id); }
						}
					}
					else if (listType == 3) { // NOW
						for (int i = 0; i < nbtCategory.getTagList("Animations", 3).tagCount(); i++) {
							int id = nbtCategory.getTagList("Animations", 3).getIntAt(i);
							if (!list.contains(id)) { list.add(id); }
						}
					}
				}
				Collections.sort(list);
				data.put(type, list);
			}
		}
		this.checkData();
		if (this.entity != null && this.entity.isServerWorld()) { this.reset(); }
	}

	private void reset() {
		for (AnimationKind type : AnimationKind.values()) {
			if (data.get(type).isEmpty()) { continue; }
			reset(type);
		}
	}

	public AnimationConfig reset(AnimationKind type) {
		List<AnimationConfig> list = AnimationController.getInstance().getAnimations(this.data.get(type));
		if (list.isEmpty()) { return null; }
		List<AnimationConfig> selectList = Lists.newArrayList();
		for (AnimationConfig ac : list) {
			if (this.waitData.containsKey(ac.id) && this.waitData.get(ac.id) > System.currentTimeMillis()) {
				continue;
			}
			float f = this.rnd.nextFloat();
			if (ac.chance <= f) {
				this.waitData.put(ac.id, System.currentTimeMillis() + 1000);
				continue;
			}
			selectList.add(ac);
		}
		AnimationConfig anim = null;
		if (!selectList.isEmpty()) { anim = selectList.get(this.rnd.nextInt(selectList.size())).copy(); }
		if (anim == null && type == AnimationKind.ATTACKING && !list.isEmpty()) { anim = list.get(this.rnd.nextInt(list.size())).copy(); }
		this.setAnimation(anim, type);
		return anim;
	}

	public NBTTagCompound save(NBTTagCompound compound) {
		this.checkData();
		NBTTagList allAnimations = new NBTTagList();
		NBTTagList allEmotions = new NBTTagList();
		for (AnimationKind type : data.keySet()) {
			if (data.get(type).isEmpty()) { continue; }
			NBTTagCompound nbtCategory = new NBTTagCompound();
			nbtCategory.setInteger("Category", type.get());
			NBTTagList animations = new NBTTagList();
			for (int id : data.get(type)) {
				animations.appendTag(new NBTTagInt(id));
			}
			nbtCategory.setTag("Animations", animations);
			allAnimations.appendTag(nbtCategory);
		}
		compound.setTag("AllAnimations", allAnimations);
		compound.setTag("AllEmotions", allEmotions);
		compound.setInteger("BaseEmotionId", this.baseEmotionId);
		return compound;
	}

	@Override
	public void setNbt(INbt nbt) {
		this.load(nbt.getMCNBT());
	}

	@Override
	public void update() {
		this.updateClient(0);
	}

	public void updateClient(int type, int... var) {
		if (this.entity == null) { return; }
		if (!this.entity.isServerWorld()) {
			if (type == 1) { NoppesUtilPlayer.sendData(EnumPlayerPacket.StopNPCAnimation, this.entity.getEntityId(), var[0], var[1]); }
			return;
		}
		NBTTagCompound compound = this.save(new NBTTagCompound());
		compound.setInteger("EntityId", this.entity.getEntityId());
		if (var != null && var.length > 0) {
			compound.setIntArray("Vars", var);
		}
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.UPDATE_NPC_ANIMATION, type, compound);
	}

	@Override
	public void addAnimation(int animationType, int animationID) {
		if (animationType < 0 || animationType >= AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: " + animationType);
		}
		List<Integer> list = data.get(AnimationKind.get(animationType));
		for (Integer id : list) {
			if (id == animationID) {
				throw new CustomNPCsException("Animation ID: " + animationID + " has in NPC.");
			}
		}
		list.add(animationID);
		Collections.sort(list);
		data.put(AnimationKind.get(animationType), list);
		this.updateClient(5);
	}

	public boolean isAnimated() {
		// no animation
		if (this.activeAnimation == null) { return false; }

		// animation has completed its duration
		if (this.completeAnimation) {
			/* for server, animation has finished its work
			   for client, the looping animation repeats its work */
			if (this.entity.isServerWorld()) { return false; }
			else return this.activeAnimation.type.isCyclical() || this.activeAnimation.isEdit != (byte) 0;
        }

		// animation running time
		boolean bo = this.entity.world.getTotalWorldTime() - this.startAnimationTime <= this.activeAnimation.totalTicks;
		if (this.entity.isServerWorld()) { this.completeAnimation = bo; }

		return bo;
	}

	public boolean isAnimated(AnimationKind ... types) {
		if (!this.isAnimated()) { return false; }

		for (AnimationKind type : types) {
			if (this.activeAnimation.type == type) { return true; }
		}
		return false;
	}

	@Override
	public boolean hasAnimations(int animationType) {
		if (animationType < 0 || animationType >= AnimationKind.values().length) { return false; }
		return this.data.containsKey(AnimationKind.get(animationType));
	}

	@Override
	public boolean hasAnimation(int animationType, int animationId) {
		if (animationType < 0 || animationType >= AnimationKind.values().length) { return false; }
		for (int id : this.data.get(AnimationKind.get(animationType))) {
			if (id == animationId) { return true; }
		}
		return false;
	}

	private void startEvent(AnimationEvent event) {
		if (event == null || (event.animation != null && event.animation.isEdit != (byte) 0)) { return; }
		IScriptHandler handler = null;
		if (!this.entity.isServerWorld()) { handler = ScriptController.Instance.clientScripts; }
		else if (this.entity instanceof EntityNPCInterface) { handler = ((EntityNPCInterface) this.entity).script; }
		else if (this.entity instanceof EntityPlayer) {
			PlayerData data = PlayerData.get((EntityPlayer) entity);
			if (data != null) { handler = data.scriptData; }
		}
		EventHooks.onEvent(handler, event.nameEvent, event);
	}

	public AnimationKind getAnimationType() {
		return this.activeAnimation == null ? null : this.activeAnimation.type;
	}

	public void resetShowParts() {
        showParts.replaceAll((k, v) -> true);
        showArmorParts.replaceAll((k, v) -> true);
	}

	public int getHitboxDamageType() {
		if (this.activeAnimation == null || this.activeAnimation.type != AnimationKind.ATTACKING) { return 0; }
		return this.activeAnimation.getDamageHitboxType();
	}

	public AxisAlignedBB getHitboxDamage() {
		if (this.activeAnimation == null || this.activeAnimation.type != AnimationKind.ATTACKING) { return null; }
		return this.activeAnimation.getDamageHitbox(this.entity);
	}

	public void updateAnimation() {
		if (!this.isAnimated()) { return; }
		if (this.entity.world.getTotalWorldTime() - this.startAnimationTime > this.activeAnimation.totalTicks) {
			this.completeAnimation = true;
			this.startEvent(new AnimationEvent.StopEvent(this.entity, this.activeAnimation));
		}
	}

	public void resetWalkOrStand() {
		boolean isMoving = this.isMoving();
		AnimationConfig anim;
		if (this.entity.isInWater() || this.entity.isInLava()) {
			anim = isMoving ? this.animData.get(AnimationKind.WATER_WALK) : this.animData.get(AnimationKind.WATER_STAND);
		} else if (!this.entity.onGround && (!(this.entity instanceof EntityNPCInterface) || ((EntityNPCInterface) this.entity).ais.getNavigationType() == 1)) {
			anim = isMoving ? this.animData.get(AnimationKind.FLY_WALK) : this.animData.get(AnimationKind.FLY_STAND);
		} else {
			anim = isMoving ? this.animData.get(AnimationKind.WALKING) : this.animData.get(AnimationKind.STANDING);
		}
		if (anim == null || anim.id == -1) { anim = this.animData.get(AnimationKind.BASE); }

		if (anim != null && (this.activeAnimation == null || this.activeAnimation.id != anim.id)) {
			AnimationFrameConfig currentAnimationFrame = AnimationFrameConfig.STANDARD;
			if (this.activeAnimation != null) {
				this.startEvent(new AnimationEvent.StopEvent(this.entity, this.activeAnimation));
				int id = this.activeAnimation.getAnimationFrameByTime((this.entity.world.getTotalWorldTime() - this.startAnimationTime) % this.activeAnimation.totalTicks);
				if (this.activeAnimation.frames.get(id).id != -1) { currentAnimationFrame = this.activeAnimation.frames.get(id); }
			}
			this.activeAnimation = anim.create(anim.type, currentAnimationFrame);
			this.startAnimationTime = this.entity.world.getTotalWorldTime();
			this.completeAnimation = false;
			this.startEvent(new AnimationEvent.StartEvent(this.entity, this.activeAnimation));
		}
	}

	private boolean isMoving() {
		double sp = this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
		double speed = 0.069d;
		if (sp != 0.0d) { speed = speed * 0.25d / sp; }
		double xz = Math.sqrt(Math.pow(this.entity.motionX, 2.0d) + Math.pow(this.entity.motionZ, 2.0d));
		return xz >= (speed / 2.0d) && (this.entity.motionY <= -speed || this.entity.motionY > 0.0d);
	}

	// this.reset(AnimationKind) or PacketHandlerClient
	public void setAnimation(AnimationConfig anim, AnimationKind type) {
		if (anim != null && anim.frames.isEmpty()) { anim = null; }
		if (anim != null) {
			// remember previous animation if current animation is temporary

			boolean isAnimated = this.isAnimated();

			// reset
			AnimationFrameConfig currentAnimationFrame = AnimationFrameConfig.STANDARD;
			if (isAnimated) {
				int id = this.activeAnimation.getAnimationFrameByTime((this.entity.world.getTotalWorldTime() - this.startAnimationTime) % this.activeAnimation.totalTicks);
				if (this.activeAnimation.frames.get(id).id != -1) {
					currentAnimationFrame = this.activeAnimation.frames.get(id);
				}
			}
			anim = anim.create(type, currentAnimationFrame);

			// change current animation
			boolean needSet = !isAnimated;
			if (!needSet) {
				needSet = !type.isCyclical() && this.activeAnimation.type.isCyclical();
				if (needSet && this.preAnimation != this.activeAnimation) { this.preAnimation = this.activeAnimation; }
			}
			if (needSet) {
				if (isAnimated) { this.stopAnimation(); }
				this.isFastSpeed = type.isQuickStart();
				this.activeAnimation = anim;
				this.startAnimationTime = this.entity.world.getTotalWorldTime();
				if (this.preAnimation != this.activeAnimation) { this.preAnimation = AnimationConfig.EMPTY; }
			}

			// remember option
			this.animData.put(type, anim);
			this.isJump = type == AnimationKind.JUMP;
			this.isSwing = type == AnimationKind.SWING;

			// special settings
			if (type == AnimationKind.DIES) {
				this.entity.motionX = 0.0d;
				this.entity.motionY = 0.0d;
				this.entity.motionZ = 0.0d;
			}
		}
		else { this.stopAnimation(); }

		if (this.entity.isServerWorld()) {
			NBTTagCompound compound = this.save(new NBTTagCompound());
			compound.setInteger("EntityId", this.entity.getEntityId());
			compound.setInteger("animID", anim == null ? -1 : anim.id);
			compound.setInteger("typeID", type.ordinal());
			Server.sendToAll(CustomNpcs.Server, EnumPacketClient.UPDATE_NPC_ANIMATION, 6, compound);
		}
		this.startEvent(new AnimationEvent.StartEvent(this.entity, this.activeAnimation));
	}

}
