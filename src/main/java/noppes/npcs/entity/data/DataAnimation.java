package noppes.npcs.entity.data;

import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
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
	public AnimationConfig activeAnimation = AnimationConfig.EMPTY, preAnimation = AnimationConfig.EMPTY;
	private final Map<AnimationKind, AnimationConfig> animData = Maps.newHashMap(); // animation ID, time
	public long startAnimationTime = 0;
	public long startAnimationFrameTime = 0;
	public int animationFrame = -2;
	private boolean fastReturn = false, completeAnimation = false, isResetMoving = false;
	private ResourceLocation animationSound = null;

	public AnimationFrameConfig currentFrame = AnimationFrameConfig.EMPTY_PART, nextFrame = AnimationFrameConfig.EMPTY_PART;
	public boolean isJump = false, isSwing = false;

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
		if (this.startAnimationTime == 0 || this.animationFrame == -2) { return; }
		// Current anim ticks
		if (this.startAnimationFrameTime <= 0) {
			this.startAnimationFrameTime = this.entity.world.getTotalWorldTime();
		}
		int ticks = (int) (this.entity.world.getTotalWorldTime() - this.startAnimationFrameTime);
		// Speed ticks to next frame
		int speed;
		// Select Frame
		if (this.animationFrame < 0) { // start or finish animation
			if (this.activeAnimation != null) { // starting
				this.nextFrame = this.activeAnimation.frames.get(0);
				if (this.activeAnimation.isEdit == (byte) 2) { this.currentFrame = this.nextFrame; }
			}
			else { this.nextFrame = AnimationFrameConfig.EMPTY_PART.copy(); } // finishing
			speed = this.nextFrame.speed / (this.fastReturn ? 3 : 1);
		} else {
			if (this.activeAnimation == null) {
				this.nextFrame = AnimationFrameConfig.EMPTY_PART;
			}
			else if (this.activeAnimation.id == -1) {
				this.nextFrame = this.activeAnimation.frames.get(0);
			}
			else if (this.activeAnimation.type == AnimationKind.JUMP && !this.isJump && this.completeAnimation) {
				this.activeAnimation = null;
				this.nextFrame = AnimationFrameConfig.EMPTY_PART.copy();
				this.fastReturn = true;
			}
			else if (this.activeAnimation.frames.containsKey(this.animationFrame + 1)) { // next frame
				this.nextFrame = this.activeAnimation.frames.get(this.animationFrame + 1);
			}
			else if (this.activeAnimation.isEdit != (byte) 0) {
				this.animationFrame = 0;
				this.nextFrame = this.activeAnimation.frames.get(0);
				this.startAnimationTime = this.entity.world.getTotalWorldTime();
				this.startAnimationFrameTime = this.entity.world.getTotalWorldTime();
			}
			else if (this.activeAnimation.repeatLast > 0 || this.activeAnimation.type.isRepeat()) { // repeat frames until animation is turned off
				int f = this.activeAnimation.repeatLast <= 0 ? 1 : this.activeAnimation.repeatLast;
				this.animationFrame = this.activeAnimation.frames.size() - f - 1;
				this.completeAnimation = true;
				if (this.animationFrame < 0) { this.animationFrame = 0; }
				this.nextFrame = this.activeAnimation.frames.containsKey(this.animationFrame) ? this.activeAnimation.frames.get(this.animationFrame) : this.currentFrame;
				if (f == 1) { pt = 0.0f; }
				this.startAnimationTime = this.entity.world.getTotalWorldTime() + this.activeAnimation.ticks.get(this.animationFrame);
				this.startAnimationFrameTime = this.entity.world.getTotalWorldTime();
			}
			else if (this.activeAnimation.type.isMoving()) {
				this.animationFrame = 0;
				this.completeAnimation = true;
				this.nextFrame = this.activeAnimation.frames.get(0);
				this.startAnimationTime = this.entity.world.getTotalWorldTime();
				this.startAnimationFrameTime = this.entity.world.getTotalWorldTime();
			}
			else if (this.activeAnimation.frames.size() == 1) { // simple animation
                this.nextFrame = AnimationFrameConfig.EMPTY_PART.copy();
                if (this.activeAnimation.type.isFastReturn()) { this.fastReturn = true; }
            }
			else { this.stopAnimation(); }
			if (this.isResetMoving) { speed = 8; }
			else { speed = this.nextFrame.speed / (this.fastReturn ? 3 : 1); }
		}
		if (this.nextFrame.id == -1 && this.currentFrame.id != -1) {
			for (int id : this.nextFrame.parts.keySet()) {
				this.nextFrame.parts.get(id).setDisable(this.currentFrame.parts.get(id).isDisable());
				this.nextFrame.parts.get(id).setShow(this.currentFrame.parts.get(id).isShow());
			}
		}
		// Set show Parts
		for (PartConfig part : this.currentFrame.parts.values()) { this.showParts.put(part.getEnumType(), part.isShow()); }
		// movement speed depends on the NPCs movement speed
		float corrV;
		if (this.activeAnimation != null && this.activeAnimation.type.isMoving()) {
			double sp = this.entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
			if (sp != 0.0d) {
				corrV = (float) ((this.entity instanceof EntityNPCInterface ? 0.25d : 0.0999999985098839d) / sp);
				speed = (int) ((float) speed * corrV);
			}
		}
		// play sound
		if (this.currentFrame != null && !this.currentFrame.equals(this.nextFrame) && this.currentFrame.sound != null && this.entity.world.loadedEntityList.contains(this.entity)) {
			if (!this.entity.isServerWorld() && (this.activeAnimation == null || this.activeAnimation.isEdit == 0) && !MusicController.Instance.isPlaying(this.currentFrame.sound.toString())) {
				MusicController.Instance.playSound(SoundCategory.AMBIENT, this.currentFrame.sound.toString(), (float) this.entity.posX, (float) this.entity.posY, (float) this.entity.posZ, 1.0f, 1.0f);
			}
			this.animationSound = this.currentFrame.sound;
		}
		if (this.currentFrame == null) {
			this.stopAnimation();
			return;
		}
		// show armor
		if (this.activeAnimation != null) {
            this.showArmorParts.put(EnumParts.HEAD, this.currentFrame.showHelmet);
			this.showArmorParts.put(EnumParts.BODY, this.currentFrame.showBody);
			this.showArmorParts.put(EnumParts.ARM_RIGHT, this.currentFrame.showBody);
			this.showArmorParts.put(EnumParts.ARM_LEFT, this.currentFrame.showBody);
			this.showArmorParts.put(EnumParts.LEG_RIGHT, this.currentFrame.showLegs);
			this.showArmorParts.put(EnumParts.LEG_LEFT, this.currentFrame.showLegs);
			this.showArmorParts.put(EnumParts.FEET_RIGHT, this.currentFrame.showFeets);
			this.showArmorParts.put(EnumParts.FEET_LEFT, this.currentFrame.showFeets);
		}
		// calculation of exact values for a body part
		boolean notAnim = this.activeAnimation != null && this.activeAnimation.isEdit == (byte) 2;
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
							value_1 = notAnim ? value_0 : 10.0f * part1.offset[a] - 5.0f;
							break;
						}
						case 2: {
							value_0 = part0.scale[a] * 5.0f;
							value_1 = notAnim ? value_0 : part1.scale[a] * 5.0f;
							break;
						}
						default: {
							value_0 = part0.rotation[a];
							value_1 = notAnim ? value_0 : part1.rotation[a];
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
//System.out.println("CNPCs: ["+this.currentFrame.id+" > "+this.nextFrame.id+"]; ("+(this.entity.world.getTotalWorldTime() - this.startAnimationFrameTime)+")/"+ticks+" / "+(speed + this.nextFrame.getEndDelay()));
		if (ticks >= speed + this.nextFrame.getEndDelay()) {
//System.out.println("CNPCs: ["+this.currentFrame.id+" > "+this.nextFrame.id+"]; ("+(this.entity.world.getTotalWorldTime() - this.startAnimationFrameTime)+")/"+ticks+" / "+(speed + this.nextFrame.getEndDelay()));
			this.animationFrame++;
			this.isResetMoving = false;
			if (this.nextFrame.id >= 0 || (this.activeAnimation != null && this.activeAnimation.isEdit != 0)) { // further
				this.currentFrame = this.nextFrame;
				this.startAnimationFrameTime = this.entity.world.getTotalWorldTime();
				if (this.activeAnimation != null) {
					if (!this.activeAnimation.frames.containsKey(this.animationFrame)) {
						if (this.activeAnimation.isEdit != 0) { // repeat in GUI
							this.animationFrame = -1;
							return;
						} else { // complete animation
							this.fastReturn = this.activeAnimation.type.isFastReturn();
							this.completeAnimation = true;
							if (this.activeAnimation.repeatLast == 0 && !this.activeAnimation.type.isRepeat()) {
								this.activeAnimation = null;
								this.startEvent(new AnimationEvent.StopEvent(this.entity, this.activeAnimation));
							}
						}
					}
				}
				if (this.activeAnimation != null) {
					if (this.activeAnimation.isEdit == (byte) 0) {
						this.startEvent(new AnimationEvent.NextFrameEvent(this.entity, this.activeAnimation));
					}
					AnimationFrameConfig frame = this.activeAnimation.frames.get(this.animationFrame);
					if (frame != null && this.entity.world.loadedEntityList.contains(this.entity)) {
						if (frame.sound != null && !MusicController.Instance.isPlaying(frame.sound.toString())) {
							MusicController.Instance.playSound(SoundCategory.AMBIENT, frame.sound.toString(), (float) this.entity.posX, (float) this.entity.posY, (float) this.entity.posZ, 1.0f, 1.0f);
							this.animationSound = frame.sound;
						}
					}
				}
			}
			else { // end animation
                this.stopAnimation();
            }
		}
		else { // next tick
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
			this.animationFrame = -2;
			this.startAnimationTime = 0;
            this.currentFrame = AnimationFrameConfig.EMPTY_PART;
		}
		else {
			this.fastReturn = this.preAnimation.type.isFastReturn();
			this.activeAnimation = this.preAnimation;
			this.isJump = this.preAnimation.type == AnimationKind.JUMP;
			this.isSwing = this.preAnimation.type == AnimationKind.SWING;
			if (this.preAnimation.type.itStartsOver()) {
				this.animationFrame = 0;
				this.currentFrame = this.activeAnimation.frames.get(this.animationFrame);
			} else {
				this.animationFrame = -1;
			}
			this.startAnimationTime = this.entity.world.getTotalWorldTime();
			this.completeAnimation = false;
		}
		this.startAnimationFrameTime = 0;
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
				this.nextEmotionFrame = EmotionFrame.EMPTY_PART.copy();
			}
			else {
				this.nextEmotionFrame = this.activeEmotion.frames.get(0);
				//if (this.currentEmotionFrame == null && this.activeEmotion.isEdit == (byte) 2) { this.currentEmotionFrame = this.activeEmotion.frames.get(0); }
			}
			if (this.currentEmotionFrame == null) { this.currentEmotionFrame = EmotionFrame.EMPTY_PART.copy(); } // start of new animation
			speed = this.nextEmotionFrame.speed;
		} else {
			if (this.activeEmotion == null) { // returns to original position
				this.nextEmotionFrame = EmotionFrame.EMPTY_PART.copy();
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
				this.nextEmotionFrame = EmotionFrame.EMPTY_PART.copy();
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
		else { this.currentEmotionFrame = EmotionFrame.EMPTY_PART; }
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
		if (this.activeAnimation == null) { return false; }
		if (this.activeAnimation.isEdit != (byte) 0) { this.completeAnimation = false; }
		if (this.completeAnimation) {
			if (this.entity.isServerWorld() && this.activeAnimation != null && this.activeAnimation.type.isMoving()) { return true; }
			else if (this.startAnimationTime == 0 || this.animationFrame == -2) { return false; }
		}
		return !this.entity.isServerWorld() || (this.entity.world.getTotalWorldTime() - this.startAnimationTime) <= this.activeAnimation.totalTicks;
	}

	public boolean isAnimated(AnimationKind ... types) {
		if (this.activeAnimation == null) { return false; }
		for (AnimationKind type : types) {
			if (this.activeAnimation.type == type) { return this.isAnimated(); }
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

	public void setAnimation(AnimationConfig anim, AnimationKind type) { // <- reset(AnimationKind) or PacketHandlerClient
		if (anim != null && anim.frames.isEmpty()) { anim = null; }
		if (anim != null) {
			if (!type.isMoving() && this.isAnimated() && this.activeAnimation.id > -1 && this.activeAnimation.type != this.preAnimation.type && this.activeAnimation.id != this.preAnimation.id) {
				this.preAnimation = this.activeAnimation.copy();
			}
			else { this.preAnimation = AnimationConfig.EMPTY; }
			this.animData.put(type, anim);
			this.fastReturn = anim.type.isFastReturn();
			if (!type.isMoving() || this.entity.isServerWorld() || anim.isEdit != 0) {
				if (this.isAnimated()) { this.stopAnimation(); }
				this.activeAnimation = anim.copy();
				this.activeAnimation.type = type;
			}
			this.isJump = type == AnimationKind.JUMP;
			this.isSwing = type == AnimationKind.SWING;
			if (type.itStartsOver()) {
				this.animationFrame = 0;
				this.currentFrame = this.activeAnimation.frames.get(this.animationFrame);
				this.startAnimationTime = this.entity.world.getTotalWorldTime();
				this.startAnimationFrameTime = !this.entity.isServerWorld() ? 0 : this.entity.world.getTotalWorldTime();
			} else if (!type.isMoving() || this.entity.isServerWorld() || anim.isEdit != 0) {
				this.animationFrame = -1;
				this.startAnimationTime = this.entity.world.getTotalWorldTime();
				this.startAnimationFrameTime = !this.entity.isServerWorld() ? 0 : this.entity.world.getTotalWorldTime();
			}
			if (type == AnimationKind.DIES) {
				this.entity.motionX = 0.0d;
				this.entity.motionY = 0.0d;
				this.entity.motionZ = 0.0d;
			}
			if (this.activeAnimation != null && this.activeAnimation.isEdit == (byte) 1) {
				this.activeAnimation.frames.put(this.activeAnimation.frames.size(), AnimationFrameConfig.EMPTY_PART.copy());
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
		if (!type.isMoving() || this.entity.isServerWorld()) {
			this.startAnimationTime = this.entity.world.getTotalWorldTime();
			this.startAnimationFrameTime = this.entity.world.getTotalWorldTime();
			this.completeAnimation = false;
		}
		this.startEvent(new AnimationEvent.StartEvent(this.entity, this.activeAnimation));
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
		if (anim != null && this.activeAnimation != null && this.activeAnimation.id != anim.id) {
			this.startEvent(new AnimationEvent.StopEvent(this.entity, this.activeAnimation));
			this.activeAnimation = anim;
			this.startAnimationTime = this.entity.world.getTotalWorldTime();
			this.startAnimationFrameTime = this.entity.world.getTotalWorldTime();
			this.completeAnimation = false;
			this.animationFrame = 0;
			this.isResetMoving = true;
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

}
