package noppes.npcs.entity.data;

import java.util.*;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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
import noppes.npcs.constants.*;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class DataAnimation implements INPCAnimation {

	// Animation settings
	public final Map<AnimationKind, List<Integer>> data = new HashMap<>();
	private final Map<Integer, Long> waitData = new HashMap<>(); // animation ID, time

	// Animation run
	/**
	 * Integer key = 0:head, 1:left arm, 2:right arm, 3:body, 4:left leg, 5:right leg, 6:left stack, 7:right stack
	 * Float[] value = [ 0:rotX, 1:rotY, 2:rotZ, 3:ofsX, 4:ofsY, 5:ofsZ, 6:scX, 7:scY, 8:scZ, 9:rotX1, 10:rotY1 ]
	 */
	public final Map<Integer, Float[]> rots = new TreeMap<>();
	public AnimationConfig activeAnimation = null;
	public AnimationConfig movementAnimation = null;

	public long startAnimationTime = 0;
	public EnumAnimationStages stage = EnumAnimationStages.Waiting;

	private boolean hasAnimations = false;
	private boolean completeAnimation = false;
	public boolean isJump = false;
	public boolean isSwing = false;
	// current state, used to smoothly start another animation
	public AnimationFrameConfig preFrame = new AnimationFrameConfig();
	// current frame from animation
	public AnimationFrameConfig currentFrame;
	// next frame from animation
	public AnimationFrameConfig nextFrame;
	private ResourceLocation animationSound = null;

	// Emotion run
	public final Map<Integer, Float[]> emts = new TreeMap<>();
	public EmotionConfig activeEmotion = null;
	public EmotionFrame currentEmotionFrame = null, nextEmotionFrame;
	public long startEmotionTime = 0;
	public int emotionFrame = 0;
	public int baseEmotionId = -1;

	// Tools
	public final EntityLivingBase entity;
	private final Random rnd = new Random();
	public final Map<EnumParts, Boolean> showParts = new HashMap<>();
	public final Map<EnumParts, Boolean> showArmorParts = new HashMap<>();
	public final Map<EnumParts, Boolean> showAWParts = new HashMap<>();

	public DataAnimation(EntityLivingBase entity) {
		this.entity = entity;
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

		this.checkData();
		this.clear();
	}

	public void resetShowParts() {
		showParts.replaceAll((k, v) -> true);
		showArmorParts.replaceAll((k, v) -> true);
	}

	public void resetShowAWParts() {
		showAWParts.replaceAll((k, v) -> true);
	}

	private void checkData() {
		for (AnimationKind type : AnimationKind.values()) {
			if (!data.containsKey(type)) { data.put(type, new ArrayList<>()); }
		}
		while (data.containsKey(null)) { data.remove(null); }
	}

	private float calcValue(float value_0, float value_1, float speed, float ticks, boolean isSmooth, float partialTicks) {
		if (speed <= 0 || ticks < 0.0f) { return value_0; }
		float progress = Math.min((ticks + partialTicks) / speed, 1.0f);
		if (progress >= 1.0f) { return value_1; }
		if (isSmooth) { // Apply antialiasing if necessary
			progress = -0.5f * MathHelper.cos(progress * (float) Math.PI) + 0.5f;
		}
		return value_0 + (value_1 - value_0) * progress;
	}

	@Override
	public void clear() {
		stopAnimation();
		stopEmotion();
		for (List<Integer> ids : data.values()) { ids.clear(); }
		updateClient(0);
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
		if (!data.containsKey(type)) { data.put(type, new ArrayList<>()); }
		for (Integer id : data.get(type)) {
			if (id == animationId) {
				if (data.get(type).remove(id)) {
					if (this.activeAnimation != null && this.activeAnimation.getId() == id) { stopAnimation(); }
					updateClient(5);
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
			this.data.put(type, new ArrayList<>());
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
			stopAnimation();
			updateClient(5);
		}
	}

	public void calculationAnimationBeforeRendering(float partialTicks) {
		if (stage == EnumAnimationStages.Waiting || activeAnimation == null) { return; }

		updateTime();
		int ticks = (int) (entity.world.getTotalWorldTime() - startAnimationTime);
		int speed = activeAnimation.type.isQuickStart() ? 4 : 10;
		boolean isEdit = activeAnimation.type == AnimationKind.EDITING_All || activeAnimation.type == AnimationKind.EDITING_PART;
		switch (stage) {
			case Started: {
				currentFrame = preFrame;
				nextFrame = activeAnimation.frames.get(0);
				break;
			}
			case Run: {
				int animationFrame = activeAnimation.getAnimationFrameByTime(ticks);
				currentFrame = activeAnimation.frames.get(animationFrame);
				nextFrame = activeAnimation.frames.get(Math.min(animationFrame + 1, activeAnimation.frames.size() - 1));
				speed = currentFrame.speed;

				if (activeAnimation.endingFrameTicks.containsKey(animationFrame - 1)) {
					ticks -= activeAnimation.endingFrameTicks.get(animationFrame - 1);
				}
				break;
			}
			case Ending: {
				currentFrame = activeAnimation.frames.get(activeAnimation.frames.size() - 1);
				nextFrame = preFrame;
				break;
			}
			default: {
				stopAnimation();
			}
		}

		// for start or finish use settings from animation frame
		if (nextFrame.id != -1 && currentFrame.id == -1) {
			for (int id : nextFrame.parts.keySet()) {
				currentFrame.parts.get(id).setDisable(nextFrame.parts.get(id).isDisable());
				currentFrame.parts.get(id).setShow(nextFrame.parts.get(id).isShow());
			}
		}
		if (nextFrame.id == -1 && currentFrame.id != -1) {
			for (int id : nextFrame.parts.keySet()) {
				nextFrame.parts.get(id).setDisable(currentFrame.parts.get(id).isDisable());
				nextFrame.parts.get(id).setShow(currentFrame.parts.get(id).isShow());
			}
		}

		// set show parts
		for (PartConfig part : currentFrame.parts.values()) { showParts.put(part.getEnumType(), part.isShow()); }

		// movement speed depends on the NPCs movement speed
		float correctorRotations = !activeAnimation.type.isMovement() ? 1.0f : getCurrentXZSpeed();

		// start sound (ignore unloaded entities in GUI)
		if (animationSound == null &&
				currentFrame.sound != null &&
				!isEdit &&
				!entity.isServerWorld() &&
				entity.world.loadedEntityList.contains(entity)) {
			MusicController.Instance.playSound(SoundCategory.AMBIENT, currentFrame.sound.toString(), (float) entity.posX, (float) entity.posY, (float) entity.posZ, 1.0f, 1.0f);
			animationSound = currentFrame.sound;
		}

		// show armor
		showArmorParts.put(EnumParts.HEAD, currentFrame.showHelmet);
		showArmorParts.put(EnumParts.BODY, currentFrame.showBody);
		showArmorParts.put(EnumParts.ARM_RIGHT, currentFrame.showBody);
		showArmorParts.put(EnumParts.ARM_LEFT, currentFrame.showBody);
		showArmorParts.put(EnumParts.LEG_RIGHT, currentFrame.showLegs);
		showArmorParts.put(EnumParts.LEG_LEFT, currentFrame.showLegs);
		showArmorParts.put(EnumParts.FEET_RIGHT, currentFrame.showFeets);
		showArmorParts.put(EnumParts.FEET_LEFT, currentFrame.showFeets);

		// calculation of exact values for a body part
		rots.clear();
		if (currentFrame.delay != 0) { ticks -= currentFrame.delay; }
		for (int partId = 0; partId < currentFrame.parts.size(); partId++) {
			PartConfig part0 = currentFrame.parts.get(partId);
			PartConfig part1 = nextFrame.parts.get(partId);
			if (part0.isDisable() || !part0.isShow() || part1 == null) {
				rots.put(part0.id, null);
				continue;
			}
			Float[] values = new Float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f };
			for (int t = 0; t < 3; t++) { // 0:rotations, 1:offsets, 2:scales
				for (int a = 0; a < 5; a++) { // x, y, z, x1, y1
					float value_0;
					float value_1;
					if (t != 0 && a > 2) { continue; }
					switch (t) {
						case 1: {
							value_0 = part0.offset[a];
							value_1 = part1.offset[a];
							break;
						}
						case 2: {
							value_0 = part0.scale[a];
							value_1 = part1.scale[a];
							break;
						}
						default: {
							value_0 = part0.rotation[a];
							value_1 = part1.rotation[a];
							float result =  value_0 - value_1; // adjusting the nearest number
							if (Math.abs(result) > Math.PI) { // example: to rotate in a circle
								value_1 = result;
							}
							value_0 *= correctorRotations;
							value_1 *= correctorRotations;
							break;
						}
					}
					values[t * 3 + a] = calcValue(value_0, value_1, speed, ticks, currentFrame.isSmooth(), partialTicks);
				}
			}
			rots.put(part0.id, values);
		}
	}

	private float getCurrentXZSpeed() {
		Vec3d currentPosition = new Vec3d(entity.posX, 0.0f, entity.posZ);
		Vec3d delta = currentPosition.subtract(new Vec3d(entity.prevPosX, 0.0f, entity.prevPosZ));
		double distanceMoved = delta.lengthSquared();

		IAttributeInstance movementAttribute = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
		float speed = 1.0f;
		if (movementAttribute != null) {
			speed = (float) (distanceMoved / movementAttribute.getBaseValue() * 2.1475d);
		}
		return ValueUtil.correctFloat(speed, 0.0f, 1.0f);
	}

	@Override
	public void stopAnimation() {
		if (activeAnimation != null) {
			if (activeAnimation.hasEmotion()) {
				stopEmotion();
			}
			if (activeAnimation.type != AnimationKind.EDITING_All && activeAnimation.type != AnimationKind.EDITING_PART) {
				int ticks = (int) (entity.world.getTotalWorldTime() - startAnimationTime);
				int animationFrame = activeAnimation.getAnimationFrameByTime(ticks);
				int startFrameTime = 0;
				if (activeAnimation.endingFrameTicks.containsKey(animationFrame - 1)) {
					startFrameTime = activeAnimation.endingFrameTicks.get(animationFrame - 1);
				}
				startEvent(new AnimationEvent.StopEvent(entity, activeAnimation, animationFrame, ticks - startFrameTime, stage));
			} else { return; }
		}
		isJump = false;
		isSwing = false;
		rots.clear();
		if (animationSound != null && !entity.isServerWorld()) { MusicController.Instance.stopSound(animationSound.toString(), SoundCategory.AMBIENT); }
		animationSound = null;
		startAnimationTime = 0;
		completeAnimation = false;
		activeAnimation = null;
		stage = EnumAnimationStages.Waiting;
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
					values[t * 2 + a] = this.calcValue(value_0, value_1, speed, ticks, currentEmotionFrame.isSmooth(), pt);
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
			updateClient(4, emotion.getId());
		}
	}

	@Override
	public void stopEmotion() {
		if (this.activeEmotion != null) {
			updateClient(3, activeEmotion.id);
			this.currentEmotionFrame = this.activeEmotion.frames.get(this.emotionFrame);
			this.activeEmotion = null;
		}
		else { this.currentEmotionFrame = EmotionFrame.STANDARD; }
		this.emts.clear();
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
				List<Integer> list = new ArrayList<>();
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
		if (!CustomNpcs.ShowCustomAnimation || !entity.isServerWorld() || data.get(type).isEmpty()) { return null; }
		List<AnimationConfig> list = AnimationController.getInstance().getAnimations(data.get(type));
		if (list.isEmpty()) { return null; }
		List<AnimationConfig> selectList = new ArrayList<>();
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
		setAnimation(anim, type);
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
		updateClient(0);
	}

	/**
	 * @param type
	 * 0:resave;
	 * 1:stop animation;
	 * 2:base mod current animation;
	 * 3:stop emotion;
	 * 4:start emotion;
	 * 5:remove animation;
	 * 6:start animation from saved IDs
	 */
	public void updateClient(int type, int... var) {
		if (entity == null) { return; }
		if (!entity.isServerWorld()) {
			if (type == 1) { NoppesUtilPlayer.sendData(EnumPlayerPacket.StopNPCAnimation, entity.getEntityId(), var[0], var[1]); }
			return;
		}
		Object[] objects = new Object[] { entity.world.provider.getDimension(), type, entity.getEntityId(), save(new NBTTagCompound()) };
		if (type == 4) {
			objects = new Object[] { entity.world.provider.getDimension(), type, entity.getEntityId(), var[0] };
		}
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.UPDATE_NPC_ANIMATION, objects);
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
		hasAnimations = true;
		updateClient(0);
	}

	/**
	 * Server used this method how update event
	 */
	public boolean isAnimated() {
		// no animation
		if (activeAnimation == null || stage == EnumAnimationStages.Waiting) { return false; }
		if (activeAnimation.type == AnimationKind.DIES && entity.getHealth() <= 0.0f) { return true; }
		boolean isEdit = activeAnimation.type == AnimationKind.EDITING_All || activeAnimation.type == AnimationKind.EDITING_PART;
		return !completeAnimation || !entity.isServerWorld() && (isEdit || activeAnimation.type.isMovement());
	}

	public void updateTime() {
		// animation running time
		if (activeAnimation == null) {
			currentFrame = null;
			nextFrame = null;
			completeAnimation = false;
			stage = EnumAnimationStages.Waiting;
			return;
		}
		int ticks = (int) (entity.world.getTotalWorldTime() - startAnimationTime);
		int speed = activeAnimation.type.isQuickStart() ? 4 : 10;

		if (stage == EnumAnimationStages.Started && ticks >= speed) {
			ticks -= speed;
			startAnimationTime += speed;
			stage = EnumAnimationStages.Run;
		}

		int endFrameStartTicks = 1;
		if (activeAnimation.endingFrameTicks.containsKey(activeAnimation.endingFrameTicks.size() - 2)) { endFrameStartTicks = activeAnimation.endingFrameTicks.get(activeAnimation.endingFrameTicks.size() - 2); }

		if (stage == EnumAnimationStages.Run && ticks >= endFrameStartTicks) {
			if (activeAnimation.type == AnimationKind.EDITING_PART) {
				ticks = 0;
				startAnimationTime = entity.world.getTotalWorldTime();
			} else {
				ticks -= activeAnimation.totalTicks;
				startAnimationTime += endFrameStartTicks;
				stage = EnumAnimationStages.Ending;
			}
			completeAnimation = true;
		}

		if (stage == EnumAnimationStages.Ending && ticks >= speed) {
			if (activeAnimation.type == AnimationKind.EDITING_All) {
				ticks = 0;
				startAnimationTime = entity.world.getTotalWorldTime();
				stage = EnumAnimationStages.Started;
			} else {
				ticks = -1;
				startAnimationTime = 0;
				stage = EnumAnimationStages.Waiting;
			}
		}
		if (stage == EnumAnimationStages.Waiting) {
			stopAnimation();
			return;
		}

		// animation events
		int animationFrame = activeAnimation.getAnimationFrameByTime(ticks);
		switch (stage) {
			case Started: {
				if (ticks == 0) { startEvent(new AnimationEvent.StartEvent(entity, activeAnimation, animationFrame, 0, stage)); }
				else { startEvent(new AnimationEvent.UpdateEvent(entity, activeAnimation, animationFrame, ticks, stage)); }
				break;
			}
			case Run: {
				int startFrameTime = 0;
				if (activeAnimation.endingFrameTicks.containsKey(animationFrame - 1)) {
					startFrameTime = activeAnimation.endingFrameTicks.get(animationFrame - 1);
				}
				int frameTime = ticks - startFrameTime;
				if (frameTime == 0) { startEvent(new AnimationEvent.NextFrameEvent(entity, activeAnimation, animationFrame, 0, stage)); }
				else { startEvent(new AnimationEvent.UpdateEvent(entity, activeAnimation, animationFrame, frameTime, stage)); }
				break;
			}
			case Ending: {
				if (ticks == 0) { startEvent(new AnimationEvent.NextFrameEvent(entity, activeAnimation, animationFrame, 0, stage)); }
				else { startEvent(new AnimationEvent.UpdateEvent(entity, activeAnimation, animationFrame, ticks, stage)); }
				break;
			}
		}
	}

	public boolean isAnimated(AnimationKind ... types) {
		if (!isAnimated()) { return false; }
		for (AnimationKind type : types) {
			if (activeAnimation.type == type) { return true; }
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
		if (event == null || (event.animation != null && (event.animation.type == AnimationKind.EDITING_All || event.animation.type == AnimationKind.EDITING_PART))) { return; }
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
		return activeAnimation == null ? null : activeAnimation.type;
	}

	// a new motion animation is selected
	public void resetWalkOrStand() {
		if (!entity.isServerWorld() || (activeAnimation != null && !completeAnimation)) { return; }
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
				if (this.entity.isServerWorld()) { updateClient(1, AnimationKind.AIM.get(), activeAnimation.id); }
				stopAnimation();
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
				if (entity.isServerWorld()) { updateClient(1, AnimationKind.AIM.get(), activeAnimation.id); }
				stopAnimation();
			}
		}
		AnimationKind type;
		if (this.movementAnimation == null && isAttacking) {
			// attempt to animate attack
			type = isMoving ? AnimationKind.REVENGE_WALK : AnimationKind.REVENGE_STAND;
			this.movementAnimation = this.reset(type);
		}
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
		if (this.movementAnimation == null) {
			// trying to select animation standard animation
			type = isMoving ? AnimationKind.WALKING : AnimationKind.STANDING;
			this.movementAnimation = this.reset(type);
		}
		if (this.movementAnimation == null) {
			// trying to select base animation
			this.movementAnimation = this.reset(AnimationKind.BASE);
		}
		if (this.movementAnimation != null && this.movementAnimation.id != -1 &&
				(this.activeAnimation == null || this.activeAnimation.id != this.movementAnimation.id || this.activeAnimation.type.isMovement())) {
			this.runAnimation(this.movementAnimation, this.movementAnimation.type);
		}
	}

	// run new movement animation
	private void runAnimation(AnimationConfig anim, AnimationKind type) {
		boolean isEdit = type == AnimationKind.EDITING_All || type == AnimationKind.EDITING_PART;
		if (entity.isServerWorld() && isEdit) { return; }
		if (activeAnimation != null) {
			if (!isEdit && activeAnimation.id == anim.id) { return; }
		}
		activeAnimation = anim.copy();
		activeAnimation.type = type;
		stage = EnumAnimationStages.Started;
		if (type == AnimationKind.EDITING_PART) { stage = EnumAnimationStages.Run; }
		startAnimationTime = entity.world.getTotalWorldTime();
		completeAnimation = false;
	}

	private boolean isMoving() {
		double sp = entity.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
		double speed = 0.069d;
		if (sp != 0.0d) { speed = speed * 0.25d / sp; }
		double xz = Math.sqrt(Math.pow(this.entity.motionX, 2.0d) + Math.pow(this.entity.motionZ, 2.0d));
		return xz >= (speed / 2.0d) && (this.entity.motionY <= -speed || this.entity.motionY > 0.0d);
	}

	// (Player or NPC) -> this.reset(AnimationKind), or PacketHandlerClient, or Animation GUI
	public void setAnimation(AnimationConfig anim, AnimationKind type) {
		if (anim != null && anim.frames.isEmpty()) { anim = null; }
		if (anim != null) {
			runAnimation(anim, type);
			// remember option
			isJump = type == AnimationKind.JUMP;
			isSwing = type == AnimationKind.SWING;
			// special settings
			if (type == AnimationKind.DIES) {
				entity.motionX = 0.0d;
				entity.motionY = 0.0d;
				entity.motionZ = 0.0d;
			}
		}
		else if (activeAnimation != null && !activeAnimation.type.isMovement()) {
			stopAnimation();
		}
		setToClient(anim, type);
	}

	private void setToClient(AnimationConfig anim, AnimationKind type) {
		if (!entity.isServerWorld()) { return; }
		if (anim == null) {
			Server.sendToAll(CustomNpcs.Server, EnumPacketClient.UPDATE_NPC_ANIMATION, entity.world.provider.getDimension(), 1, entity.getEntityId());
		}
		else {
			Server.sendToAll(CustomNpcs.Server, EnumPacketClient.UPDATE_NPC_ANIMATION, entity.world.provider.getDimension(), 6, entity.getEntityId(), anim.id, type.ordinal());
		}
	}

	public AnimationConfig getAnimation() { return activeAnimation; }

}
