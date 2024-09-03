package noppes.npcs.entity.data;

import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
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
import noppes.npcs.client.model.animation.*;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

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
	public AnimationConfig activeAnimation = null;
	public AnimationConfig movementAnimation = null;
	public long startAnimationTime = 0; // is set when the animation starts
	private boolean hasAnimations = false;
	private boolean completeAnimation = false;
	public boolean isJump = false;
	public boolean isSwing = false;
	// current state, used to smoothly start another animation
	public @Nonnull AnimationFrameConfig preFrame = new AnimationFrameConfig();
	// current frame from animation
	public AnimationFrameConfig currentFrame = new AnimationFrameConfig();
	// next frame from animation
	public AnimationFrameConfig nextFrame;
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
		this.hasAnimations = false;
		for (List<Integer> list : this.data.values()) {
			if (!list.isEmpty()) {
				this.hasAnimations = true;
				break;
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
		this.hasAnimations = false;
		for (List<Integer> list : this.data.values()) {
			if (!list.isEmpty()) {
				this.hasAnimations = true;
				break;
			}
		}
		if (this.activeAnimation != null && this.activeAnimation.type.get() == animationType) {
			this.stopAnimation();
			this.updateClient(5);
		}
	}

	public void calculationAnimationBeforeRendering(float pt) {
		if (this.activeAnimation == null) { return; }

		// animation data
		int totalTicks = (int) (this.entity.world.getTotalWorldTime() - this.startAnimationTime) % this.activeAnimation.totalTicks;
		if (totalTicks < 0) {
			this.stopAnimation();
			return;
		}
		int animationFrame = this.activeAnimation.getAnimationFrameByTime(totalTicks); // current animation frame ID
		this.currentFrame = this.activeAnimation.frames.get(animationFrame); // current frame
		int startTick = 0;
		if (animationFrame > 0 && this.activeAnimation.endingFrameTicks.containsKey(animationFrame - 1)) { startTick = this.activeAnimation.endingFrameTicks.get(animationFrame - 1) + 1; }
		int ticks = totalTicks - startTick; // running time of the current animation frame


		// Select next Frame
		this.nextFrame = null;

		boolean isCyclical = this.activeAnimation.type == AnimationKind.EDITING ||
				this.activeAnimation.repeatLast > 0 ||
				(this.activeAnimation.type.isMovement() && this.activeAnimation.chance >= 1.0f) ||
				(this.activeAnimation.type == AnimationKind.DIES && this.entity.getHealth() <= 0.0f) ||
				(this.activeAnimation.type == AnimationKind.JUMP && this.isJump);
		boolean forcedlyCyclical = true;
		boolean canNext = this.activeAnimation.frames.containsKey(animationFrame + 1);
//System.out.println("CNPCs: totalTicks: "+totalTicks+"/"+(this.activeAnimation.totalTicks - 1)+"; animationFrame: "+animationFrame+"/"+(this.activeAnimation.frames.size()-1)+"; ticks: "+ticks+"; isEditing: "+(this.activeAnimation.type == AnimationKind.EDITING)+"; start: "+this.startAnimationTime);
		if (canNext) {
			// can go to the next frame
			this.nextFrame = this.activeAnimation.frames.get(animationFrame + 1);
			forcedlyCyclical = isCyclical && animationFrame == this.activeAnimation.frames.size() - 2;
//System.out.println("CNPCs: forcedlyCyclical: "+forcedlyCyclical+"; "+animationFrame+"/"+(this.activeAnimation.frames.size() - 1));
		}
		if (isCyclical && forcedlyCyclical) {
			// animation is finished but need to repeat the last frames until it turns off
			int f0 = this.activeAnimation.repeatLast;
			if (f0 <= 0) {
				f0 = 0;
				if (this.activeAnimation.type.isMovement() && this.activeAnimation.type != AnimationKind.AIM) {
					f0 = this.activeAnimation.frames.size() - 1;
					if (!this.activeAnimation.type.isQuickStart()) { f0--; }
				}
			}
//System.out.println("CNPCs: f0: "+f0+"/"+(this.activeAnimation.frames.size()-1));
			animationFrame = this.activeAnimation.frames.size() - f0 - 1;
			if (animationFrame < 0) { animationFrame = 0; }
			this.nextFrame = this.activeAnimation.frames.containsKey(animationFrame) ? this.activeAnimation.frames.get(animationFrame) : this.currentFrame;
			if (f0 == 0) { pt = 0.0f; }
			this.completeAnimation = (totalTicks == this.activeAnimation.totalTicks - 1);
//System.out.println("CNPCs: f0: "+f0+"/"+(this.activeAnimation.frames.size()-1)+"; complete: "+this.completeAnimation);
			if (this.completeAnimation) {
				startTick = 0;
				if (animationFrame > 0 && this.activeAnimation.endingFrameTicks.containsKey(animationFrame - 1)) { startTick = this.activeAnimation.endingFrameTicks.get(animationFrame - 1) + 1; }
				this.startAnimationTime = this.entity.world.getTotalWorldTime() - startTick;
				this.completeAnimation = false;
			}
		}
		else if (!canNext) {
			this.stopAnimation();
			return;
		}
		if (this.activeAnimation == null) { return; }

		// adjust frames
		if (this.currentFrame.id == -1) { this.currentFrame = this.preFrame; }
		if (this.nextFrame.id == -1) { this.nextFrame = this.preFrame; }

		// Speed ticks to next frame
		int speed = this.currentFrame.speed;

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
				this.activeAnimation.type != AnimationKind.EDITING &&
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
		boolean isSimple = this.currentFrame.equals(this.nextFrame);
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
		if (this.activeAnimation.type != AnimationKind.EDITING) {
			this.startEvent(new AnimationEvent.UpdateEvent(this.entity, this.activeAnimation, animationFrame, totalTicks, ticks));
		}
	}

	public void stopAnimation(AnimationKind type) {
		if (this.activeAnimation == null || this.activeAnimation.type != type) { return; }
		if (this.entity.isServerWorld()) { this.activeAnimation = null; }
	}

	@Override
	public void stopAnimation() {
		if (this.activeAnimation != null) {
			if (this.activeAnimation.hasEmotion()) {
				this.stopEmotion();
			}
			if (this.activeAnimation.type != AnimationKind.EDITING) {
				int totalTicks = (int) (this.entity.world.getTotalWorldTime() - this.startAnimationTime) % this.activeAnimation.totalTicks;
				int animationFrame = -1;
				int ticks = -1;
				if (totalTicks >= 0) {
					animationFrame = this.activeAnimation.getAnimationFrameByTime(totalTicks);
					int startTick = 0;
					if (animationFrame > 0 && this.activeAnimation.endingFrameTicks.containsKey(animationFrame - 1)) { startTick = this.activeAnimation.endingFrameTicks.get(animationFrame - 1) + 1; }
					ticks = totalTicks - startTick;
				}
				this.startEvent(new AnimationEvent.StopEvent(this.entity, this.activeAnimation, animationFrame, totalTicks, ticks));
			} else { return; }
		}
		this.isJump = false;
		this.isSwing = false;
		this.rots.clear();
		this.val = 0.0f;
		this.valNext = 0.0f;
		if (this.animationSound != null && !this.entity.isServerWorld()) { MusicController.Instance.stopSound(this.animationSound.toString(), SoundCategory.AMBIENT); }
		this.animationSound = null;
		this.startAnimationTime = 0;
		this.completeAnimation = false;
		this.activeAnimation = null;
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
		this.hasAnimations = false;
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
				if (!list.isEmpty()) { this.hasAnimations = true; }
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

	// used to select a new animation in EntityNPCInterface
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
		this.hasAnimations = true;
		this.updateClient(5);
	}

	public boolean isAnimated() {
		//System.out.println("CNPCs: "+this.activeAnimation);
		// no animation
		if (this.activeAnimation == null) { return false; }

		if (this.activeAnimation.type == AnimationKind.DIES && this.entity.getHealth() <= 0.0f) { return true; }

		// animation has completed its duration
		if (this.completeAnimation) {
			/* for server, animation has finished its work
			   for client, the looping animation repeats its work */
			if (this.entity.isServerWorld()) { return false; }
			else return this.activeAnimation.type.isMovement() || this.activeAnimation.type == AnimationKind.EDITING;
        }

		// animation running time
		boolean bo = this.entity.world.getTotalWorldTime() - this.startAnimationTime <= (this.activeAnimation.totalTicks - 1);
		this.completeAnimation = !bo;
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
		if (animationType < -1 || animationType >= AnimationKind.values().length) { return false; }
		if (animationType == -1) { return this.hasAnimations; }
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
		if (event == null || (event.animation != null && event.animation.type == AnimationKind.EDITING)) { return; }
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

	// a new motion animation is selected
	public void resetWalkOrStand() {
		if (!this.entity.isServerWorld() || (this.activeAnimation != null && !this.completeAnimation)) { return; }
		// exit if one-time animation is playing
		boolean isMoving = this.isMoving();
		this.movementAnimation = null;
		boolean isAttacking = false;

		// special animation selection by entity
		if (this.entity instanceof EntityNPCInterface) {
			EntityNPCInterface npc = (EntityNPCInterface) this.entity;
			isAttacking = npc.isAttacking();
			if (isAttacking) {
				// attack animation types
				if (npc.currentAnimation == 6 || (npc.inventory.getProjectile() != null && npc.stats.ranged.getHasAimAnimation()) && !this.isAnimated(AnimationKind.AIM)) {
					// attempt to animate aiming
					this.movementAnimation = this.reset(AnimationKind.AIM);
				}
			}
			else if (this.isAnimated(AnimationKind.AIM)) {
				// aiming animation is no longer needed
				if (this.entity.isServerWorld()) { this.updateClient(1, AnimationKind.AIM.get(), this.activeAnimation.id); }
				this.stopAnimation();
			}
		}
		else if (this.entity instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) this.entity;
			ItemStack stack = player.getActiveItemStack();
			// the player uses an item in his hand with property "pulling" [bow / crossbow]
			if (player.isHandActive() && !stack.isEmpty() && stack.getItem().getPropertyGetter(new ResourceLocation("pulling")) != null) {
				// attempt to animate aiming
				this.movementAnimation = this.reset(AnimationKind.AIM);
			} else if (this.isAnimated(AnimationKind.AIM)) {
				// aiming animation is no longer needed
				if (this.entity.isServerWorld()) { this.updateClient(1, AnimationKind.AIM.get(), this.activeAnimation.id); }
				this.stopAnimation();
			}
		}
//System.out.println("CNPCs: "+this.movementAnimation);
		AnimationKind type;
		if (this.movementAnimation == null && isAttacking) {
			// attempt to animate attack
			type = isMoving ? AnimationKind.REVENGE_WALK : AnimationKind.REVENGE_STAND;
			this.movementAnimation = this.reset(type);
		}
//System.out.println("CNPCs: "+this.movementAnimation);
		if (this.movementAnimation == null) {
			if (this.entity.isInWater() || this.entity.isInLava()) {
				// trying to select animations when npc is in water
				type = isMoving ? AnimationKind.WATER_WALK : AnimationKind.WATER_STAND;
				this.movementAnimation = this.reset(type);
			} else if (!this.entity.onGround && (!(this.entity instanceof EntityNPCInterface) || ((EntityNPCInterface) this.entity).ais.getNavigationType() == 1)) {
				// trying to select animations when npc is in the air
				type = isMoving ? AnimationKind.FLY_WALK : AnimationKind.FLY_STAND;
				this.movementAnimation = this.reset(type);
			}
		}
//System.out.println("CNPCs: "+this.movementAnimation);
		if (this.movementAnimation == null) {
			// trying to select animation standard animation
			type = isMoving ? AnimationKind.WALKING : AnimationKind.STANDING;
			this.movementAnimation = this.reset(type);
		}
//System.out.println("CNPCs: "+this.movementAnimations);
		if (this.movementAnimation == null) {
			// trying to select base animation
			this.movementAnimation = this.reset(AnimationKind.BASE);
		}
//System.out.println("CNPCs: "+(this.movementAnimation != null ? this.movementAnimation.type : "null"));
		if (this.movementAnimation != null && this.movementAnimation.id != -1 &&
				(this.activeAnimation == null || this.activeAnimation.id != this.movementAnimation.id || this.activeAnimation.type.isMovement())) {
			this.runAnimation(this.movementAnimation, this.movementAnimation.type);
//System.out.println("CNPCs: "+this.movementAnimation.name);
		}
	}

	// run new movement animation
	private void runAnimation(AnimationConfig anim, AnimationKind type) {
		if (this.activeAnimation != null) {
			if (this.activeAnimation.id == anim.id) { return; }
			int totalTicks = (int) (this.entity.world.getTotalWorldTime() - this.startAnimationTime) % this.activeAnimation.totalTicks;
			int animationFrame = -1;
			int ticks = -1;
			if (totalTicks >= 0) {
				animationFrame = this.activeAnimation.getAnimationFrameByTime(totalTicks);
				int startTick = 0;
				if (animationFrame > 0 && this.activeAnimation.endingFrameTicks.containsKey(animationFrame - 1)) { startTick = this.activeAnimation.endingFrameTicks.get(animationFrame - 1) + 1; }
				ticks = totalTicks - startTick;
			}
			this.startEvent(new AnimationEvent.StopEvent(this.entity, this.activeAnimation, animationFrame, totalTicks, ticks));
		}
//System.out.println("CNPCs: "+(this.activeAnimation != null)+" // "+this.preFrame);
		this.activeAnimation = anim.create(type, this.preFrame);
		this.startAnimationTime = this.entity.world.getTotalWorldTime();
		this.completeAnimation = false;
		this.startEvent(new AnimationEvent.StartEvent(this.entity, this.activeAnimation, 0, 0, 0));
	}

	private boolean isMoving() {
		double sp = this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
		double speed = 0.069d;
		if (sp != 0.0d) { speed = speed * 0.25d / sp; }
		double xz = Math.sqrt(Math.pow(this.entity.motionX, 2.0d) + Math.pow(this.entity.motionZ, 2.0d));
		return xz >= (speed / 2.0d) && (this.entity.motionY <= -speed || this.entity.motionY > 0.0d);
	}

	// (Player or NPC) -> this.reset(AnimationKind) or PacketHandlerClient
	public void setAnimation(AnimationConfig anim, AnimationKind type) {
		if (anim != null && anim.frames.isEmpty()) { anim = null; }

		if (anim != null) {
//System.out.println("CNPCs: "+anim.name);
			this.runAnimation(anim, type);
			// remember option
			this.isJump = type == AnimationKind.JUMP;
			this.isSwing = type == AnimationKind.SWING;
			// special settings
			if (type == AnimationKind.DIES) {
				this.entity.motionX = 0.0d;
				this.entity.motionY = 0.0d;
				this.entity.motionZ = 0.0d;
			}
		}
		else if (this.activeAnimation != null && !this.activeAnimation.type.isMovement()) {
			this.stopAnimation();
		}
		this.setToClient(anim, type);
	}

	private void setToClient(AnimationConfig anim, AnimationKind type) {
		if (!this.entity.isServerWorld()) { return; }
		NBTTagCompound compound = this.save(new NBTTagCompound());
		compound.setInteger("EntityId", this.entity.getEntityId());
		compound.setInteger("animID", anim == null ? -1 : anim.id);
		compound.setInteger("typeID", type.ordinal());
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.UPDATE_NPC_ANIMATION, 6, compound);
	}

}
