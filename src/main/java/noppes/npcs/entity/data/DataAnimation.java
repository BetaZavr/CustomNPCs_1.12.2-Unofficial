package noppes.npcs.entity.data;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
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
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;

public class DataAnimation implements INPCAnimation {
	
	// Animation
	public final Map<AnimationKind, List<Integer>> data = Maps.<AnimationKind, List<Integer>>newHashMap();

	/**
	 * Integer key = 0:head, 1:left arm, 2:right arm, 3:body, 4:left leg, 5:right leg, 6:left stack, 7:right stack
	 * Float[] value = [ 0:rotX, 1:rotY, 2:rotZ, 3:ofsX, 4:ofsY, 5:ofsZ, 6:scX, 7:scY, 8:scZ, 9:rotX1, 10:rotY1 ]
	 */
	public final Map<Integer, Float[]> rots = Maps.<Integer, Float[]>newTreeMap(); // 0
	public final Map<Integer, List<AddedPartConfig>> addParts = Maps.<Integer, List<AddedPartConfig>>newTreeMap();
	private AnimationConfig activeAnimation = null;
	public AnimationFrameConfig currentFrame = null, nextFrame;
	public long startAnimationTime = 0; // used in event
	public int animationFrame = 0; // used in event
	private boolean isCompleteAnimation = false, isAnimated = false;
	private ResourceLocation animationSound = null;
	
	private boolean isAttaking = false;
	private boolean isJump = false;
	
	// Emotion
	public EmotionConfig activeEmotion = null;
	public EmotionFrame oldEmotionFrame = null;
	public long startEmotionTime = 0;
	public int emotionFrame = 0;
	public int baseEmotionId = -1;
	public boolean isCompleteEmotion = false;
	public int startEmotionId = -1;
	
	// Common
	private EntityNPCInterface npc;
	private Random rnd = new Random();
	public final Map<EnumParts, Boolean> showParts;
	private float val, valNext;
	
	public DataAnimation(EntityNPCInterface npc) {
		this.npc = npc;
		this.showParts = Maps.<EnumParts, Boolean>newHashMap();
		this.showParts.put(EnumParts.HEAD, true);
		this.showParts.put(EnumParts.BODY, true);
		this.showParts.put(EnumParts.ARM_RIGHT, true);
		this.showParts.put(EnumParts.ARM_LEFT, true);
		this.showParts.put(EnumParts.LEG_RIGHT, true);
		this.showParts.put(EnumParts.LEG_LEFT, true);
		this.cheakData();
		this.clear();
	}

	private void cheakData() {
		for (AnimationKind type : AnimationKind.values()) {
			if (!data.containsKey(type)) { data.put(type, Lists.<Integer>newArrayList()); }
		}
		while (data.containsKey(null)) { data.remove(null); }
	}

	private float calcValue(float value_0, float value_1, int speed, boolean isSmooth, float ticks, float pt) {
		if (ticks > speed) {
			ticks = speed;
			pt = 1.0f;
		}
		float pi = (float) Math.PI;
		if (isSmooth) {
			this.val = -0.5f * MathHelper.cos((float) ticks / (float) speed * pi) + 0.5f;
			this.valNext = -0.5f * MathHelper.cos((float) (ticks + 1) / (float) speed * pi) + 0.5f;
		} else {
			this.val = (float) ticks / (float) speed;
			this.valNext = (float) (ticks + 1) / (float) speed;
		}
		float f = this.val + (this.valNext - this.val) * pt;
		float value = (value_0 + (value_1 - value_0) * f) * 2.0f * pi;
		return value;
	}

	@Override
	public void clear() {
		this.stopAnimation();
		this.stopEmotion();
		for (List<Integer> ids : data.values()) { ids.clear(); }
		this.updateClient(0);
	}
	
	public void setRotationAngles(float swingProgress, float partialTicks) {
		this.resetAnimation(this.activeAnimation == null ? null : this.activeAnimation.type);
		// Dies
		if (this.hasAnim(AnimationKind.DIES) && this.npc.isKilled() && !this.npc.stats.hideKilledBody) {
			if (this.activeAnimation == null || this.activeAnimation.type != AnimationKind.DIES) {
				this.resetAnimation(AnimationKind.DIES);
			}
		} else {
			if (this.activeAnimation != null && this.activeAnimation.type == AnimationKind.DIES && this.isCompleteAnimation && !this.npc.isKilled() || this.npc.stats.hideKilledBody) {
				this.stopAnimation();
			}
			// Hit
			if (this.hasAnim(AnimationKind.HIT) && this.npc.hurtTime > 0 && this.npc.hurtTime == this.npc.maxHurtTime && this.npc.getHealth() != 0) {
				this.resetAnimation(AnimationKind.HIT);
			}
			if (this.activeAnimation == null || this.activeAnimation.type != AnimationKind.INIT) {
				// Swing
				if (this.hasAnim(AnimationKind.ATTACKING)) {
					this.isAttaking = false;
					if (this.isAttaking && swingProgress > 0) {
						this.npc.swingProgress = 0.0f;
						this.npc.swingProgressInt = 5;
					}
					if (swingProgress > 0) {
						resetAnimation(AnimationKind.ATTACKING);
						if (this.activeAnimation != null) {
							this.npc.swingProgress = 0.0f;
							this.npc.swingProgressInt = 5;
							this.isAttaking = true;
						}
					}
				}
				// Jump
				if (this.hasAnim(AnimationKind.JUMP)) {
					if (!this.isJump && !(this.npc.isInWater() || this.npc.isInLava()) && this.npc.ais.getNavigationType() == 0) {
						if (!this.npc.onGround && this.npc.motionY > 0.0d) {
							this.resetAnimation(AnimationKind.JUMP);
							if (this.activeAnimation != null) { this.isJump = true; }
						}
					}
					else if (this.npc.onGround) { this.isJump = false; }
				}
			}
			// INIT started in EntityNPCInterface.reset()
			if (this.activeAnimation == null || !this.activeAnimation.isEdit) {
				// Moving or Standing
				if (this.activeAnimation == null) {
					boolean isNavigate = this.npc.navigating != null || this.npc.motionX != 0.0d || this.npc.motionZ != 0.0d;
					// Revenge Target
					if (this.npc.isAttacking()) {
						if (this.hasAnim(AnimationKind.REVENGE_WALK) && isNavigate && (this.activeAnimation == null || this.activeAnimation.type != AnimationKind.REVENGE_WALK)) {
							this.resetAnimation(AnimationKind.REVENGE_WALK);
						} else if (this.hasAnim(AnimationKind.REVENGE_STAND) && !isNavigate && (this.activeAnimation == null || this.activeAnimation.type != AnimationKind.REVENGE_STAND)) {
							this.resetAnimation(AnimationKind.REVENGE_STAND);
						}
					} else {
						if (this.npc.isInWater() || this.npc.isInLava()) {
							if (this.hasAnim(AnimationKind.WATER_WALK) && isNavigate && (this.activeAnimation == null || this.activeAnimation.type != AnimationKind.WATER_WALK)) {
								this.resetAnimation(AnimationKind.WATER_WALK);
							} else if (this.hasAnim(AnimationKind.WATER_STAND) && !isNavigate && (this.activeAnimation == null || this.activeAnimation.type != AnimationKind.WATER_STAND)) {
								this.resetAnimation(AnimationKind.WATER_STAND);
							}
						} else {
							if (!this.npc.onGround && this.npc.ais.getNavigationType() == 1) {
								if (this.hasAnim(AnimationKind.FLY_WALK) && isNavigate && (this.activeAnimation == null || this.activeAnimation.type != AnimationKind.FLY_WALK)) {
									this.resetAnimation(AnimationKind.FLY_WALK);
								} else if (this.hasAnim(AnimationKind.FLY_STAND) && !isNavigate && (this.activeAnimation == null || this.activeAnimation.type != AnimationKind.FLY_STAND)) {
									this.resetAnimation(AnimationKind.FLY_STAND);
								}
							}
						}
						if (this.activeAnimation == null) {
							if (this.hasAnim(AnimationKind.WALKING) && isNavigate && (this.activeAnimation == null || this.activeAnimation.type != AnimationKind.WALKING)) {
								this.resetAnimation(AnimationKind.WALKING);
							} else if (this.hasAnim(AnimationKind.STANDING) && !isNavigate && (this.activeAnimation == null || this.activeAnimation.type != AnimationKind.STANDING)) {
								this.resetAnimation(AnimationKind.STANDING);
							}
						}
					}
				}
			}
		}
		if (this.activeAnimation != null && this.npc.animation.isAnimated) {
			AnimationEvent event = new AnimationEvent.UpdateEvent(this.npc, this.activeAnimation);
			EventHooks.onEvent(ScriptController.Instance.clientScripts, event.nameEvent, event);
		}
		this.resetAnimValues(partialTicks);
	}
	
	private void resetAnimation(AnimationKind type) {
		if (this.activeAnimation != null && this.activeAnimation.type == type) {
			if (this.animationFrame < this.activeAnimation.frames.size() || this.activeAnimation.isEdit) {
				if (this.animationFrame >= this.activeAnimation.frames.size() && this.activeAnimation.isEdit) {
					this.animationFrame = -1;
				}
				return;
			}
		}
		if (this.activeAnimation != null) {
			this.updateClient(1, this.activeAnimation.type.get(), this.activeAnimation.id);
			this.activeAnimation = null;
			this.val = 0.0f;
			this.valNext = 0.0f;
			this.startAnimationTime = 0;
		}
		List<Integer> ids = data.get(type);
		if (ids == null) { data.put(type, ids = Lists.<Integer>newArrayList()); }
		if (ids.isEmpty() && (type == AnimationKind.FLY_STAND || type == AnimationKind.WATER_STAND)) {
			type = AnimationKind.STANDING;
			ids = data.get(type);
		}
		if (ids.isEmpty() && (type == AnimationKind.FLY_WALK || type == AnimationKind.WATER_WALK)) {
			type = AnimationKind.WALKING;
			ids = data.get(type);
		}
		if (ids.isEmpty()) {
			type = AnimationKind.BASE;
			ids = data.get(type);
		}
		AnimationController aData = AnimationController.getInstance();
		List<AnimationConfig> list = aData.getAnimations(ids);
		if (list.size() > 0) {
			List<AnimationConfig> selectList = Lists.<AnimationConfig>newArrayList();
			for (AnimationConfig ac : list) {
				selectList.add(ac);
			}
			if (selectList.size() > 0) {
				this.activeAnimation = selectList.get(this.rnd.nextInt(selectList.size()));
			}
		}
		if (this.activeAnimation != null) {
			this.animationFrame = -1;
			this.activeAnimation.type = type;
		}
	}

	@Override
	public IAnimation[] getAnimations(int animationType) {
		if (animationType < 0 || animationType >= AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: " + animationType);
		}
		List<AnimationConfig> list = AnimationController.getInstance().getAnimations(data.get(AnimationKind.get(animationType)));
		return list.toArray(new IAnimation[list.size()]);
	}

	@Override
	public boolean removeAnimation(int animationType, int animationId) {
		if (animationType < 0 || animationType >= AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: " + animationType);
		}
		AnimationKind type = AnimationKind.get(animationType);
		if (!data.containsKey(type)) { data.put(type, Lists.<Integer>newArrayList()); }
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
			this.data.put(type, Lists.<Integer>newArrayList());
		}
		this.data.get(type).clear();
		if (this.activeAnimation != null && this.activeAnimation.type.get() == animationType) {
			this.stopAnimation();
			this.updateClient(5);
		}
	}

	private void resetAnimValues(float pt) {
		if ((this.activeAnimation == null || this.activeAnimation.frames.isEmpty()) && this.currentFrame == null) {
			this.isAnimated = false;
			return;
		}
		if (this.startAnimationTime <= 0) {
			this.startAnimationTime = this.npc.world.getTotalWorldTime();
		}
		int ticks = (int) (this.npc.world.getTotalWorldTime() - this.startAnimationTime);
		if (this.currentFrame == null) { this.currentFrame = AnimationFrameConfig.EMPTY_PART; }
		this.nextFrame = null;
		boolean isNewStart = this.animationFrame == -1 && ticks == 0;
		if (isNewStart) {
			this.animationFrame = 0;
			this.isCompleteAnimation = false;
			if (this.activeAnimation != null) {
				AnimationEvent event = new AnimationEvent.StartEvent(this.npc, this.activeAnimation);
				EventHooks.onEvent(ScriptController.Instance.clientScripts, event.nameEvent, event);
			}
		}
		if (this.animationFrame == -1) { // start
			this.nextFrame = this.activeAnimation != null ? this.activeAnimation.frames.get(0) : AnimationFrameConfig.EMPTY_PART;
		} else if (this.activeAnimation != null) {
			if (this.activeAnimation.frames.size() == 1) { // simple
				this.nextFrame = this.activeAnimation.frames.get(0);
			} else if (this.activeAnimation.frames.containsKey(this.animationFrame + 1)) { // next
				this.nextFrame = this.activeAnimation.frames.get(this.animationFrame + 1);
			} else if (this.activeAnimation.isEdit) {
				if (this.animationFrame == this.activeAnimation.frames.size() - 1) {
					this.nextFrame = this.activeAnimation.frames.get(this.animationFrame);
				} else {
					this.animationFrame = 0;
					this.nextFrame = this.activeAnimation.frames.get(this.animationFrame + 1);
				}
				this.animationFrame = -1;
				this.startAnimationTime = 0;
			} else if (this.activeAnimation.repeatLast > 0 || this.activeAnimation.type == AnimationKind.DIES) { // repeat end
				int f = this.activeAnimation.repeatLast <= 0 ? 1 : this.activeAnimation.repeatLast;
				this.animationFrame = this.activeAnimation.frames.size() - f;
				if (this.animationFrame < 0) {
					this.animationFrame = 0;
				}
				this.nextFrame = this.activeAnimation.frames.containsKey(this.animationFrame + 1) ? this.activeAnimation.frames.get(this.animationFrame + 1) : this.currentFrame;
			}
		} else {
			this.nextFrame = AnimationFrameConfig.EMPTY_PART;
		}
		if (this.currentFrame == null || this.nextFrame == null) {
			if (this.activeAnimation != null) {
				this.updateClient(1, this.activeAnimation.type.get(), this.activeAnimation.id);
			}
			this.activeAnimation = null;
			for (EnumParts type : this.showParts.keySet()) {
				this.showParts.put(type, true);
			}
			this.stopAnimation();
			return;
		}
		for (PartConfig part : this.currentFrame.parts.values()) {
			this.showParts.put(part.getEnumType(), part.isShow());
		}
		int speed = this.currentFrame.getSpeed();
		
		if (this.activeAnimation != null && this.activeAnimation.type.isMoving()) {
			double sp = this.npc.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
			speed = (int) ((double) speed * 0.25d / sp);
		}
		if (isNewStart && this.currentFrame != null && !this.currentFrame.equals(this.nextFrame) && this.currentFrame.sound != null && this.npc.world.loadedEntityList.contains(this.npc)) {
			if (this.npc.world.isRemote && (this.activeAnimation == null || !this.activeAnimation.isEdit) && !MusicController.Instance.isPlaying(this.currentFrame.sound.toString())) {
				MusicController.Instance.playSound(SoundCategory.AMBIENT, this.currentFrame.sound.toString(), (float) this.npc.posX, (float) this.npc.posY, (float) this.npc.posZ, 1.0f, 1.0f);
			}
			this.animationSound = this.currentFrame.sound;
		}

		this.rots.clear();
		for (int partId = 0; partId < this.currentFrame.parts.size(); partId++) {
			Float[] values = new Float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f };
			PartConfig part0 = this.currentFrame.parts.get(partId);
			PartConfig part1 = this.nextFrame.parts.get(partId);
			if (part0.isDisable() || part1 == null) {
				this.rots.put(part0.id, null);
				continue;
			}
			for (int t = 0; t < 3; t++) { // 0:rotations, 1:offsets, 2:scales
				for (int a = 0; a < 3; a++) { // x, y, z
					float value_0;
					float value_1;
					switch (t) {
						case 1: {
							value_0 = 10.0f * part0.offset[a] - 5.0f;
							value_1 = 10.0f * part1.offset[a] - 5.0f;
							break;
						}
						case 2: {
							value_0 = part0.scale[a] * 5.0f;
							value_1 = part1.scale[a] * 5.0f;
							break;
						}
						default: {
							value_0 = part0.rotation[a];
							value_1 = part1.rotation[a];
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
		if (ticks >= speed + this.nextFrame.getEndDelay()) {
			this.animationFrame++;
			this.currentFrame = this.nextFrame;
			if (this.currentFrame.equals(AnimationFrameConfig.EMPTY_PART)) { this.currentFrame = null; }
			this.startAnimationTime = this.npc.world.getTotalWorldTime();
			this.isCompleteAnimation = this.activeAnimation == null || this.animationFrame >= this.activeAnimation.frames.size() - 1;
			AnimationEvent event;
			if (this.isCompleteAnimation) {
				this.animationFrame = 0;
				event = new AnimationEvent.StopEvent(this.npc, this.activeAnimation);
			} else {
				event = new AnimationEvent.NextFrameEvent(this.npc, this.activeAnimation);
				AnimationFrameConfig frame = this.activeAnimation == null || this.activeAnimation.frames.containsKey(this.animationFrame) ? this.activeAnimation.frames.get(this.animationFrame) : AnimationFrameConfig.EMPTY_PART;
				if (frame != null && frame.sound != null && this.npc.world.loadedEntityList.contains(this.npc)) {
					if (this.npc.world.isRemote && !MusicController.Instance.isPlaying(frame.sound.toString())) {
						MusicController.Instance.playSound(SoundCategory.AMBIENT, frame.sound.toString(), (float) this.npc.posX, (float) this.npc.posY, (float) this.npc.posZ, 1.0f, 1.0f);
					}
					this.animationSound = frame.sound;
				}
			}
			EventHooks.onEvent(ScriptController.Instance.clientScripts, event.nameEvent, event);
			if (this.isCompleteAnimation) {
				if (this.activeAnimation != null && this.activeAnimation.repeatLast > 0) {
					int f = this.activeAnimation.repeatLast <= 0 ? 1 : this.activeAnimation.repeatLast;
					this.animationFrame = this.activeAnimation.frames.size() - f;
					if (this.animationFrame < 0) {
						this.animationFrame = 0;
					}
				} else if (this.activeAnimation != null && !this.activeAnimation.isEdit) {
					this.stopAnimation();
				}
			}
		}
		this.isAnimated = this.currentFrame != null;
	}

	@Override
	public void startAnimation(int animationType) {
		if (animationType < 0 || animationType >= AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: " + animationType);
		}
		List<AnimationConfig> list = AnimationController.getInstance().getAnimations(data.get(AnimationKind.get(animationType)));
		if (list.size() == 0) {
			return;
		}
		int variant = this.rnd.nextInt(list.size());
		if (this.npc.world == null || this.npc.world.isRemote) {
			this.activeAnimation = list.get(variant);
			this.isCompleteAnimation = false;
		} else {
			this.updateClient(2, animationType, variant);
		}
	}

	@Override
	public void startAnimation(int animationType, int variant) {
		if (variant < 0) {
			this.startAnimation(animationType);
			return;
		}
		if (animationType < 0 || animationType >= AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: " + animationType);
		}
		List<AnimationConfig> list = AnimationController.getInstance().getAnimations(data.get(AnimationKind.get(animationType)));
		if (variant >= list.size()) {
			if (!this.npc.world.isRemote) { throw new CustomNPCsException("Variant must be between 0 and " + list.size() + " You have: " + variant); }
			return;
		}
		if (this.npc.world == null || this.npc.world.isRemote) {
			this.activeAnimation = list.get(variant);
			this.isCompleteAnimation = false;
		} else {
			this.updateClient(2, animationType, variant);
		}
	}

	@Override
	public void stopAnimation() {
		if (this.activeAnimation != null) {
			this.updateClient(1, this.activeAnimation.type.get(), this.activeAnimation.id);
			this.activeAnimation = null;
		}
		this.val = 0.0f;
		this.valNext = 0.0f;
		this.startAnimationTime = 0;
		this.isCompleteAnimation = false;
		if (this.animationSound != null && (this.npc.world == null || this.npc.world.isRemote)) {
			MusicController.Instance.stopSound(this.animationSound.toString(), SoundCategory.AMBIENT);
		}
		this.animationSound = null;
	}

	public boolean hasAnim() {
		if (this.activeAnimation != null || this.currentFrame != null) { return true; }
		for (List<Integer> list : data.values()) {
			if (!list.isEmpty()) { return true; }
		}
		return false;
	}
	
	public boolean hasAnim(AnimationKind type) { return data.containsKey(type) && !data.get(type).isEmpty(); }
	
	// Emotion
	@Override
	public IEmotion getEmotion() { return this.activeEmotion; }
	
	public Map<Integer, Float[]> getEmotionValues(EntityNPCInterface npc, float pt) {
		if (this.activeEmotion == null || this.activeEmotion.frames.isEmpty() || this.oldEmotionFrame == null) {
			this.activeEmotion = null;
			return null;
		}
		if (this.startEmotionTime <= 0) { this.startEmotionTime = npc.world.getTotalWorldTime(); }
		int ticks = (int) (npc.world.getTotalWorldTime() - this.startEmotionTime);
		EmotionFrame frame_0 = null, frame_1 = null;
		if (this.emotionFrame == -1) {
			this.emotionFrame = 0;
			this.isCompleteEmotion = false;
		}
		if (this.emotionFrame == -1) { // start
			if (this.oldEmotionFrame != null) { frame_0 = this.oldEmotionFrame; }
			else {
				frame_0 = EmotionFrame.EMPTY_PART;
				this.oldEmotionFrame = EmotionFrame.EMPTY_PART;
			}
			frame_1 = this.activeEmotion != null ? this.activeEmotion.frames.get(0) : EmotionFrame.EMPTY_PART;
		} else if (this.activeEmotion != null) {
			if (this.activeEmotion.frames.size() == 1) { // simple
				frame_0 = this.activeEmotion.frames.get(0);
				frame_1 = this.activeEmotion.frames.get(0);
				if (this.oldEmotionFrame == null) {
					this.oldEmotionFrame = EmotionFrame.EMPTY_PART;
				}
			} else if (this.activeEmotion.frames.containsKey(this.emotionFrame + 1)) { // next
				if (this.activeEmotion.frames.containsKey(this.emotionFrame)) {
					frame_0 = this.activeEmotion.frames.get(this.emotionFrame);
				} else {
					frame_0 = EmotionFrame.EMPTY_PART;
				}
				frame_1 = this.activeEmotion.frames.get(this.emotionFrame + 1);
			} else if (this.activeEmotion.repeatLast > 0) { // repeat end
				int f = this.activeEmotion.repeatLast <= 0 ? 1 : this.activeEmotion.repeatLast;
				this.emotionFrame = this.activeEmotion.frames.size() - f;
				if (this.emotionFrame < 0) {
					this.emotionFrame = 0;
				}
				frame_0 = this.activeEmotion.frames.get(this.emotionFrame);
				frame_1 = this.activeEmotion.frames.containsKey(this.emotionFrame + 1) ? this.activeEmotion.frames.get(this.emotionFrame + 1) : frame_0;
			}
		}
		int speed = frame_0.getSpeed();
		Map<Integer, Float[]> map = Maps.<Integer, Float[]>newTreeMap();
		for (int part = 0; part < 6; part++) { // 0:eyeRight, 1:eyeLeft, 2:pupilRight, 3:pupilLeft, 4:browRight, 5:browLeft
			
			Float[] values = new Float[] { 0.0f, 0.0f, 0.0f, 1.0f, 1.0f }; // ofsX, ofsY, scX, scY, rot

			for (int t = 0; t < 3; t++) { // 0:offsets, 1:scales, 2:rotations
				for (int a = 0; a < 2; a++) { // x, y
					if (t == 2 && a == 1) { continue; }
					float value_0;
					float value_1;
					switch (t) {
						case 1: { // scales
							switch (part) {
								case 1: { // eyeLeft
									value_0 = frame_0.scaleEye[a + 2] + 0.5f;
									value_1 = frame_1.scaleEye[a + 2] + 0.5f;
									break;
								}
								case 2: { // pupilRight
									value_0 = frame_0.scalePupil[a] + 0.5f;
									value_1 = frame_1.scalePupil[a] + 0.5f;
									break;
								}
								case 3: { // pupilLeft
									value_0 = frame_0.scalePupil[a + 2] + 0.5f;
									value_1 = frame_1.scalePupil[a + 2] + 0.5f;
									break;
								}
								case 4: { // browRight
									value_0 = frame_0.scaleBrow[a] + 0.5f;
									value_1 = frame_1.scaleBrow[a] + 0.5f;
									break;
								}
								case 5: { // browLeft
									value_0 = frame_0.scaleBrow[a + 2] + 0.5f;
									value_1 = frame_1.scaleBrow[a + 2] + 0.5f;
									break;
								}
								default: { // eyeRight
									value_0 = frame_0.scaleEye[a] + 0.5f;
									value_1 = frame_1.scaleEye[a] + 0.5f;
									break;
								}
							}
							break;
						}
						case 2: { // rotations
							switch (part) {
								case 1: { // eyeLeft
									value_0 = (frame_0.rotEye[1] - 0.5f) * 360.0f;
									value_1 = (frame_1.rotEye[1] - 0.5f) * 360.0f;
									break;
								}
								case 2: { // pupilRight
									value_0 = (frame_0.rotPupil[0] - 0.5f) * 360.0f;
									value_1 = (frame_1.rotPupil[0] - 0.5f) * 360.0f;
									break;
								}
								case 3: { // pupilLeft
									value_0 = (frame_0.rotPupil[1] - 0.5f) * 360.0f;
									value_1 = (frame_1.rotPupil[1] - 0.5f) * 360.0f;
									break;
								}
								case 4: { // browRight
									value_0 = (frame_0.rotBrow[0] - 0.5f) * 360.0f;
									value_1 = (frame_1.rotBrow[0] - 0.5f) * 360.0f;
									break;
								}
								case 5: { // browLeft
									value_0 = (frame_0.rotBrow[1] - 0.5f) * 360.0f;
									value_1 = (frame_1.rotBrow[1] - 0.5f) * 360.0f;
									break;
								}
								default: { // eyeRight
									value_0 = (frame_0.rotEye[0] - 0.5f) * 360.0f;
									value_1 = (frame_1.rotEye[0] - 0.5f) * 360.0f;
									break;
								}
							}
							break;
						}
						default: { // offsets
							switch (part) {
								case 1: { // eyeLeft
									value_0 = (frame_0.offsetEye[a + 2] - 0.5f) * 2.0f;
									value_1 = (frame_1.offsetEye[a + 2] - 0.5f) * 2.0f;
									break;
								}
								case 2: { // pupilRight
									value_0 = (frame_0.offsetPupil[a] - 0.5f) * 2.0f;
									value_1 = (frame_1.offsetPupil[a] - 0.5f) * 2.0f;
									break;
								}
								case 3: { // pupilLeft
									value_0 = (frame_0.offsetPupil[a + 2] - 0.5f) * 2.0f;
									value_1 = (frame_1.offsetPupil[a + 2] - 0.5f) * 2.0f;
									break;
								}
								case 4: { // browRight
									value_0 = (frame_0.offsetBrow[a] - 0.5f) * 2.0f;
									value_1 = (frame_1.offsetBrow[a] - 0.5f) * 2.0f;
									break;
								}
								case 5: { // browLeft
									value_0 = (frame_0.offsetBrow[a + 2] - 0.5f) * 2.0f;
									value_1 = (frame_1.offsetBrow[a + 2] - 0.5f) * 2.0f;
									break;
								}
								default: { // eyeRight
									value_0 = (frame_0.offsetEye[a] - 0.5f) * 2.0f;
									value_1 = (frame_1.offsetEye[a] - 0.5f) * 2.0f;
									break;
								}
							}
							break;
						}
					}
					values[t * 2 + a] = this.calcValue(value_0, value_1, speed, frame_0.isSmooth(), ticks, pt);
					if (t != 0) { values[t * 2 + a] /= 2 * (float) Math.PI; }
				}
			}
			map.put(part, values);
		}
		if (ticks >= speed + frame_1.getEndDelay()) {
			this.animationFrame++;
			this.startEmotionTime = npc.world.getTotalWorldTime();
			this.oldEmotionFrame = frame_1;
			this.isCompleteEmotion = this.activeEmotion != null ? this.animationFrame >= this.activeEmotion.frames.size() - 1 : true;
			if (this.isCompleteEmotion && this.activeEmotion != null && this.activeEmotion.repeatLast > 0) {
				int f = this.activeEmotion.repeatLast <= 0 ? 1 : this.activeEmotion.repeatLast;
				this.animationFrame = this.activeEmotion.frames.size() - f;
				if (this.animationFrame < 0) { this.animationFrame = 0; }
			}
		}
		
		return map;
	}
	
	@Override
	public void startEmotion(int emotionId) {
		IEmotion emotion = AnimationController.getInstance().getEmotion(emotionId);
		if (emotion == null) { return; }
		if (this.npc.world == null || this.npc.world.isRemote) {
			this.activeEmotion = (EmotionConfig) emotion;
			this.emotionFrame = 0;
			this.startEmotionTime = 0;
			this.isCompleteEmotion = false;
		} else {
			this.updateClient(4, emotion.getId());
		}
	}
	
	@Override
	public void stopEmotion() {
		if (this.activeEmotion != null) {
			this.updateClient(3, this.activeEmotion.id);
			this.oldEmotionFrame = this.activeEmotion.frames.get(this.emotionFrame);
			this.activeEmotion = null;
		}
		else { this.oldEmotionFrame = EmotionFrame.EMPTY_PART; }
		this.val = 0.0f;
		this.valNext = 0.0f;
		this.startEmotionTime = 0;
		this.isCompleteEmotion = false;
	}
	
	@Override
	public INbt getNbt() {
		return NpcAPI.Instance().getINbt(this.save(new NBTTagCompound()));
	}

	public void load(NBTTagCompound compound) {
		if (compound.hasKey("BaseEmotionId", 3)) { this.baseEmotionId = compound.getInteger("BaseEmotionId"); }
		data.clear();
		AnimationController aData = AnimationController.getInstance();
		boolean stopAnim = true;
		int aA = this.activeAnimation != null ? this.activeAnimation.id : -1;
		if (compound.hasKey("AllAnimations", 9)) {
			for (int c = 0; c < compound.getTagList("AllAnimations", 10).tagCount(); c++) {
				NBTTagCompound nbtCategory = compound.getTagList("AllAnimations", 10).getCompoundTagAt(c);
				int t = nbtCategory.getInteger("Category");
				if (t < 0) {
					t *= -1;
				}
				AnimationKind type = AnimationKind.get(t % AnimationKind.values().length);
				if (type == null) {
					LogWriter.warn("Try load AnimationKind ID:"+t+" - "+type+". Missed.");
					continue;
				}
				List<Integer> list = Lists.<Integer>newArrayList();
				int tagType = nbtCategory.getTag("Animations").getId();
				if (tagType == 11) {
					for (int id : nbtCategory.getIntArray("Animations")) {
						if (!list.contains(id)) {
							list.add(id);
							if (id == aA) { stopAnim = false; }
						}
					}
				} else if (tagType == 9) {
					int listType = ((NBTTagList) nbtCategory.getTag("Animations")).getTagType();
					if (listType == 10 && npc != null && npc.world != null && !npc.world.isRemote) {
						for (int i = 0; i < nbtCategory.getTagList("Animations", 10).tagCount(); i++) {
							NBTTagCompound nbt = nbtCategory.getTagList("Animations", 10).getCompoundTagAt(i);
							int id = nbt.getInteger("ID");
							String name = npc.getName() + "_" + nbt.getString("Name");
							AnimationConfig anim = (AnimationConfig) aData.getAnimation(id);
							if (anim == null || !anim.getName().equals(name)) {
								anim = (AnimationConfig) aData.createNewAnim();
							}
							if (anim != null) {
								id = anim.id;
								anim.readFromNBT(nbt);
								anim.name = name;
								anim.id = id;
								Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, EnumSync.AnimationData, anim.writeToNBT(new NBTTagCompound()));
							}
							if (!list.contains(id)) {
								list.add(id);
								if (id == aA) { stopAnim = false; }
							}
						}
					} else if (listType == 3) {
						for (int i = 0; i < nbtCategory.getTagList("Animations", 3).tagCount(); i++) {
							int id = nbtCategory.getTagList("Animations", 3).getIntAt(i);
							if (!list.contains(id)) {
								list.add(id);
								if (id == aA) { stopAnim = false; }
							}
						}
					}
				}
				Collections.sort(list);
				data.put(type, list);
			}
		}
		if (stopAnim) { this.stopAnimation(); }
		for (AnimationKind eat : AnimationKind.values()) {
			if (!data.containsKey(eat)) {
				data.put(eat, Lists.<Integer>newArrayList());
			}
		}
	}

	@Override
	public void reset() {
		this.stopAnimation();
		this.stopEmotion();
	}

	public NBTTagCompound save(NBTTagCompound compound) {
		this.cheakData();
		NBTTagList allAnimations = new NBTTagList();
		NBTTagList allEmotions = new NBTTagList();
		for (AnimationKind type : data.keySet()) {
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
		if (this.npc.world == null || this.npc.world.isRemote) {
			if (type == 1) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.StopNPCAnimation, this.npc.getEntityId(), var[0], var[1]);
			}
			return;
		}
		NBTTagCompound compound = this.save(new NBTTagCompound());
		compound.setInteger("EntityId", this.npc.getEntityId());
		if (var != null && var.length > 0) {
			compound.setIntArray("Vars", var);
		}
		Server.sendAssociatedData(this.npc, EnumPacketClient.UPDATE_NPC_ANIMATION, type, compound);
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

	public boolean isAnimated() { return this.isAnimated; }

	public boolean isAnimated(AnimationKind type) {
		return this.activeAnimation == null ? false : this.activeAnimation.type == type;
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

	public void startAnimation(AnimationConfig ac) { this.activeAnimation = ac; }

}
