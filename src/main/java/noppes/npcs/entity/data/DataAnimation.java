package noppes.npcs.entity.data;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.entity.data.INPCAnimation;
import noppes.npcs.api.wrapper.NPCWrapper;
import noppes.npcs.client.Client;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.client.model.animation.EmotionConfig;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class DataAnimation
implements INPCAnimation {

	public AnimationConfig activeAnim;
	public EmotionConfig activeEmtn;
	public final Map<AnimationKind, List<AnimationConfig>> data;
	public final List<EmotionConfig> emotion;
	private EntityNPCInterface npc;
	private Random rnd = new Random();

	private AnimationConfig oldAnim;
	private int frame;
	private long startFrameTick;
	private float val, valNext;
	
	public DataAnimation(EntityNPCInterface npc) {
		this.npc = npc;
		this.data = Maps.<AnimationKind, List<AnimationConfig>>newHashMap();
		this.emotion = Lists.<EmotionConfig>newArrayList();
		this.clear();
	}
	
	public void readFromNBT(NBTTagCompound compound) {
		this.data.clear();
		this.emotion.clear();
		if (!compound.hasKey("AllAnimations", 9) && CustomNpcs.FixUpdateFromPre_1_12) { // OLD
			AnimationKind type = compound.getBoolean("PuppetMoving") ? AnimationKind.WALKING : compound.getBoolean("PuppetAttacking") ? AnimationKind.ATTACKING : AnimationKind.STANDING;
			int speed;
			switch(compound.getInteger("PuppetAnimationSpeed")) {
				case 0: speed = 80; break;
				case 1: speed = 48; break;
				case 2: speed = 26; break;
				case 3: speed = 20; break;
				case 5: speed = 8; break;
				case 6: speed = 6; break;
				case 7: speed = 4; break;
				default: speed = 14; break;
			}
			boolean isAnim = compound.getBoolean("PuppetAnimate");
			List<AnimationConfig> list = Lists.<AnimationConfig>newArrayList();
			AnimationConfig ac = new AnimationConfig(type.get());
			AnimationFrameConfig f0 = ac.frames.get(0), f1 = null;
			ac.name = "Loaded from old version";
			if (isAnim) {
				f1 = (AnimationFrameConfig) ac.addFrame();
				f1.setSpeed(speed);
			}
			f0.setSpeed(speed);
			for (int i=0; i < (isAnim ? 12 : 6); i++) {
				String n;
				switch(i) {
					case 1: n = "PuppetLArm"; break;
					case 2: n = "PuppetRArm"; break;
					case 3: n = "PuppetBody"; break;
					case 4: n = "PuppetLLeg"; break;
					case 5: n = "PuppetRLeg"; break;
					case 6: n = "PuppetHead2"; break;
					case 7: n = "PuppetLArm2"; break;
					case 8: n = "PuppetRArm2"; break;
					case 9: n = "PuppetBody2"; break;
					case 10: n = "PuppetLLeg2"; break;
					case 11: n = "PuppetRLeg2"; break;
					default: n = "PuppetHead"; break;
				}
				if (i<6) {
					f0.parts[i].rotation[0] = 0.5f * compound.getCompoundTag(n).getFloat("RotationX") + 0.5f;
					f0.parts[i].rotation[1] = 0.5f * compound.getCompoundTag(n).getFloat("RotationY") + 0.5f;
					f0.parts[i].rotation[2] = 0.5f * compound.getCompoundTag(n).getFloat("RotationZ") + 0.5f;
					f0.parts[i].disable = compound.getCompoundTag(n).getBoolean("Disabled");
				} else {
					f1.parts[i%6].rotation[0] = 0.5f * compound.getCompoundTag(n).getFloat("RotationX") + 0.5f;
					f1.parts[i%6].rotation[1] = 0.5f * compound.getCompoundTag(n).getFloat("RotationY") + 0.5f;
					f1.parts[i%6].rotation[2] = 0.5f * compound.getCompoundTag(n).getFloat("RotationZ") + 0.5f;
					f1.parts[i%6].disable = compound.getCompoundTag(n).getBoolean("Disabled");
				}
			}
			
			list.add(ac);
			this.data.put(type, list);
			
		} else {
			for (int c=0; c<compound.getTagList("AllAnimations", 10).tagCount(); c++) {
				NBTTagCompound nbtCategory = compound.getTagList("AllAnimations", 10).getCompoundTagAt(c);
				int t = nbtCategory.getInteger("Category");
				if (t<0) { t *= -1; }
				t %= AnimationKind.values().length;
				AnimationKind type = AnimationKind.get(t);
				List<AnimationConfig> list = Lists.<AnimationConfig>newArrayList();
				for (int i=0; i<nbtCategory.getTagList("Animations", 10).tagCount(); i++) {
					AnimationConfig ac = new AnimationConfig(t);
					ac.readFromNBT(nbtCategory.getTagList("Animations", 10).getCompoundTagAt(i));
					list.add(ac);
				}
				this.data.put(type, list);
			}
		}
		for (AnimationKind eat : AnimationKind.values()) {
			if (!this.data.containsKey(eat)) { this.data.put(eat, Lists.<AnimationConfig>newArrayList()); }
		}
		for (int c=0; c<compound.getTagList("AllEmotions", 10).tagCount(); c++) {
			
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList allAnimations = new NBTTagList();
		NBTTagList allEmotions = new NBTTagList();
		for (AnimationKind eat : this.data.keySet()) {
			NBTTagCompound nbtCategory = new NBTTagCompound();
			nbtCategory.setInteger("Category", eat.get());
			NBTTagList animations = new NBTTagList();
			for (AnimationConfig ac : this.data.get(eat)) {
				animations.appendTag(ac.writeToNBT(new NBTTagCompound()));
			}
			nbtCategory.setTag("Animations", animations);
			allAnimations.appendTag(nbtCategory);
		}
		compound.setTag("AllAnimations", allAnimations);
		
		
		compound.setTag("AllEmotions", allEmotions);
		
		return compound;
	}

	public EmotionConfig getActiveEmotion() {
		if (this.activeEmtn!=null) { return this.activeEmtn; }
		this.activeEmtn = null;
		if (this.emotion.size()>0) {
			this.activeEmtn = this.emotion.get(this.rnd.nextInt(this.emotion.size()));
		}
		if (this.activeEmtn!=null) { this.activeEmtn.reset(); }
		return this.activeEmtn;
	}
	
	public AnimationConfig getActiveAnimation(AnimationKind type) {
		if (this.activeAnim!=null && this.activeAnim.type==type) {
			if (this.frame < this.activeAnim.frames.size() || this.activeAnim.isEdit) {
				if (this.frame >= this.activeAnim.frames.size() && this.activeAnim.isEdit) {
					this.frame = -1;
				}
				return this.activeAnim;
			}
		}
		if (this.activeAnim!=null) { this.updateClient(1, this.activeAnim.getType(), this.activeAnim.id); }
		this.activeAnim = null;
		this.frame = -1;
		this.val = 0.0f;
		this.valNext = 0.0f;
		this.startFrameTick = 0;
		List<AnimationConfig> list = this.data.get(type);
		if (list==null) { this.data.put(type, list = Lists.<AnimationConfig>newArrayList()); }
		if (list.size()==0 && (type==AnimationKind.FLY_STAND || type==AnimationKind.WATER_STAND)) {
			list = this.data.get(AnimationKind.STANDING);
		}
		else if (list.size()==0 && (type==AnimationKind.FLY_WALK || type==AnimationKind.WATER_WALK)) {
			list = this.data.get(AnimationKind.WALKING);
		}
		if (list.size()>0) {
			List<AnimationConfig> selectList = Lists.<AnimationConfig>newArrayList();
			for (AnimationConfig ac : list) {
				if (ac.isDisable()) { continue; }
				selectList.add(ac);
			}
			if (selectList.size()>0) {
				this.activeAnim = selectList.get(this.rnd.nextInt(selectList.size()));
			}
		}
		return this.activeAnim;
	}

	private void updateClient(int type, int ... var) {
		if (this.npc.world==null || this.npc.world.isRemote) {
			if (type==1) { Client.sendDataDelayCheck(EnumPlayerPacket.StopNPCAnimation, this, 0, this.npc.getEntityId(), var[0], var[1]); }
			return;
		}
		NBTTagCompound compound = this.writeToNBT(new NBTTagCompound());
		compound.setInteger("EntityId", this.npc.getEntityId());
		if (var!=null && var.length>0) { compound.setIntArray("Vars", var); }
		Server.sendAssociatedData(this.npc, EnumPacketClient.UPDATE_NPC_ANIMATION, type, compound);
	}
	
	@Override
	public void reset() {
		this.stopAnimation();
	}
	
	@Override
	public void clear() {
		this.activeAnim = null;
		this.activeEmtn = null;
		this.data.clear();
		this.emotion.clear();
		for (AnimationKind eat : AnimationKind.values()) {
			this.data.put(eat, Lists.<AnimationConfig>newArrayList());
		}
		this.updateClient(0);
	}

	@Override
	public void update() { this.updateClient(0); }
	
	@Override
	public void stopAnimation() {
		if (this.activeAnim!=null) {
			this.updateClient(1, this.activeAnim.getType(), this.activeAnim.id);
			this.activeAnim = null;
		}
		this.oldAnim = null;
		this.frame = -1;
		this.val = 0.0f;
		this.valNext = 0.0f;
		this.startFrameTick = 0;
	}

	@Override
	public void stopEmotion() {
		if (this.activeEmtn!=null) {
			this.activeEmtn.reset();
			this.activeEmtn = null;
			this.updateClient(1);
		}
	}

	@Override
	public IAnimation[] getAnimations(int animationType) {
		if (animationType<0 || animationType>=AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: "+animationType);
		}
		List<AnimationConfig> list = this.data.get(AnimationKind.get(animationType));
		return list.toArray(new IAnimation[list.size()]);
	}

	@Override
	public IAnimation getAnimation(int animationType, int variant) {
		if (animationType<0 || animationType>=AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: "+animationType);
		}
		List<AnimationConfig> list = this.data.get(AnimationKind.get(animationType));
		if (variant>=list.size()) {
			throw new CustomNPCsException("Variant must be between 0 and " + list.size() + " You have: "+variant);
		}
		return list.get(variant);
	}

	@Override
	public void startAnimation(int animationType) {
		if (animationType<0 || animationType>=AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: "+animationType);
		}
		List<AnimationConfig> list = this.data.get(AnimationKind.get(animationType));
		if (list.size()==0) { return; }
		int variant = this.rnd.nextInt(list.size());
		if (this.npc.world==null || this.npc.world.isRemote) { this.activeAnim = list.get(variant); }
		else { this.updateClient(2, animationType, variant); }
	}

	@Override
	public void startAnimation(int animationType, int variant) {
		if (variant<0) {
			this.startAnimation(animationType);
			return;
		}
		if (animationType<0 || animationType>=AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: "+animationType);
		}
		List<AnimationConfig> list = this.data.get(AnimationKind.get(animationType));
		if (variant>=list.size()) {
			throw new CustomNPCsException("Variant must be between 0 and " + list.size() + " You have: "+variant);
		}
		if (this.npc.world==null || this.npc.world.isRemote) {
			this.activeAnim = list.get(variant);
		} else {
			this.updateClient(2, animationType, variant);
		}
	}

	@Override
	public void startAnimationFromSaved(int animationId) {
		IAnimation anim = AnimationController.getInstance().getAnimation(animationId);
		if (anim!=null) { anim.startToNpc(new NPCWrapper<EntityNPCInterface>(this.npc));}
	}
	
	@Override
	public void startAnimationFromSaved(String animationName) {
		IAnimation anim = AnimationController.getInstance().getAnimation(animationName);
		if (anim!=null) { anim.startToNpc(new NPCWrapper<EntityNPCInterface>(this.npc));}
	}
	
	@Override
	public INbt getNbt() { return NpcAPI.Instance().getINbt(this.writeToNBT(new NBTTagCompound())); }

	@Override
	public void setNbt(INbt nbt) { this.readFromNBT(nbt.getMCNBT()); }

	@Override
	public boolean removeAnimation(int animationType, String animationName) {
		if (animationType<0 || animationType>=AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: "+animationType);
		}
		AnimationKind t = AnimationKind.get(animationType);
		for (AnimationConfig ac : this.data.get(t)) {
			if (ac.name.equalsIgnoreCase(animationName)) {
				this.data.get(t).remove(ac);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void removeAnimations(int animationType) {
		if (animationType<0 || animationType>=AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: "+animationType);
		}
		this.data.get(AnimationKind.get(animationType)).clear();
	}

	@Override
	public AnimationConfig createAnimation(int animationType) {
		if (animationType<0 || animationType>=AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: "+animationType);
		}
		AnimationConfig ac = new AnimationConfig(animationType);
		ac.id = this.data.get(ac.type).size();
		this.data.get(ac.type).add(ac);
		return ac;
	}
	
	public Map<Integer, Float[]> getValues(EntityCustomNpc npc, AnimationConfig anim) {
		if (anim==null || anim.frames.isEmpty()) {
			return null;
		}
		if (this.startFrameTick<=0) { this.startFrameTick = npc.world.getTotalWorldTime(); }
		int ticks = (int) (npc.world.getTotalWorldTime() - this.startFrameTick);
		AnimationFrameConfig frame_0 = null, frame_1 = null;
		if (this.frame==-1 && (anim.type==AnimationKind.ATTACKING ||
				anim.type==AnimationKind.DIES ||
				anim.type==AnimationKind.JUMP ||
				anim.type==AnimationKind.INIT)) {
			this.frame = 0;
		}
		if (this.frame==-1) { // start
			if (this.oldAnim!=null && !this.oldAnim.frames.isEmpty()) {
				frame_0 = this.oldAnim.frames.get(this.oldAnim.frames.size()-1);
			} else {
				frame_0 = AnimationFrameConfig.EMPTY_PART;
				this.oldAnim = anim;
			}
			frame_1 = anim.frames.get(0);
		}
		else if (anim.frames.size()==1) { // simple
			frame_0 = anim.frames.get(0);
			frame_1 = anim.frames.get(0);
			if (this.oldAnim==null) { this.oldAnim = anim; }
		}
		else if (anim.frames.containsKey(this.frame+1)) { // next
			if (anim.frames.containsKey(this.frame)) { frame_0 = anim.frames.get(this.frame); }
			else { frame_0 = AnimationFrameConfig.EMPTY_PART; }
			frame_1 = anim.frames.get(this.frame + 1);
		}
		else if (anim.isEdit) {
			this.frame = 0;
			frame_0 = anim.frames.get(anim.frames.size()-1);
			frame_1 = anim.frames.get(this.frame);
		}
		else if (anim.repeatLast>0 || anim.type==AnimationKind.DIES) { // repeat end
			int f = anim.repeatLast<=0 ? 1 : anim.repeatLast;
			this.frame = anim.frames.size() - f;
			if (this.frame<0) { this.frame = 0; }
			frame_0 = anim.frames.get(this.frame);
			frame_1 = anim.frames.containsKey(this.frame + 1) ? anim.frames.get(this.frame + 1) : frame_0;
		}
		if (frame_0 == null || frame_1 == null) {
			if (this.activeAnim!=null) { this.updateClient(1, this.activeAnim.getType(), this.activeAnim.id); }
			this.activeAnim = null;
			this.frame = -1;
			this.startFrameTick = 0;
			return null;
		} // end
		
		int speed = frame_0.getSpeed();
		if (anim.type.isMoving()) {
			double sp = npc.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
			speed = (int) ((double) speed * 0.25d / sp);
		}
		
		Map<Integer, Float[]> map = Maps.<Integer, Float[]>newTreeMap();
		for (int part=0; part<6; part++) { // 0:head, 1:left arm, 2:right arm, 3:body, 4:left leg, 5:right leg
			Float[] values = new Float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f }; // rotX, rotY, rotZ, ofsX, ofsY, ofsZ, scX, scY, scZ
			if (frame_0.parts[part].isDisable()) {
				map.put(part, null);
				continue;
			}
			for (int t=0; t<3; t++) { // 0:rotations, 1:offsets, 2:scales
				for (int a=0; a<3; a++) { // x, y, z
					float value_0;
					float value_1;
					switch(t) {
						case 1: {
							value_0 = 10.0f * frame_0.parts[part].offset[a] - 5.0f;
							value_1 = 10.0f * frame_1.parts[part].offset[a] - 5.0f;
							break;
						}
						case 2: {
							value_0 = frame_0.parts[part].scale[a] * 5.0f;
							value_1 = frame_1.parts[part].scale[a] * 5.0f;
							break;
						}
						default: {
							value_0 = frame_0.parts[part].rotation[a];
							value_1 = frame_1.parts[part].rotation[a];
							if (value_0 < 0.5f && Math.abs(value_0 + 1.0f - value_1) < Math.abs(value_0 - value_1)) {
								value_0 += 1.0f;
							}
							else if (value_1 < 0.5f && Math.abs(value_1 + 1.0f - value_0) < Math.abs(value_0 - value_1)) {
								value_1 += 1.0f;
							}
							value_0 -= 0.5f;
							value_1 -= 0.5f;
							break;
						}
					}
					values[t * 3 + a] = this.calcValue(value_0, value_1, speed, frame_0.isSmooth(), ticks);
					if (t!=0) { values[t * 3 + a] /= 2 * (float) Math.PI; } // offsets, scales - correction
				}
			}
			map.put(part, values);
		}
		if (ticks >= speed + frame_1.getEndDelay()) {
			this.frame++;
			this.startFrameTick = npc.world.getTotalWorldTime();
			this.oldAnim = anim;
			if (this.frame>=anim.frames.size()-1 && (anim.repeatLast>0 || anim.type==AnimationKind.DIES)) {
				int f = anim.repeatLast<=0 ? 1 : anim.repeatLast;
				this.frame = anim.frames.size() - f;
				if (this.frame<0) { this.frame = 0; }
			}
		}
		return map;
	}
	
	private float calcValue(float value_0, float value_1, int speed, boolean isSmooth, float ticks) {
		float pt = Minecraft.getMinecraft().getRenderPartialTicks();
		if (ticks > speed) { ticks = speed; pt = 1.0f; }
		float pi = (float) Math.PI;
		if (isSmooth) {
			this.val = -0.5f * MathHelper.cos((float) ticks / (float) speed * pi) + 0.5f;
			this.valNext = -0.5f * MathHelper.cos((float) (ticks+1) / (float) speed * pi) + 0.5f;
		} else {
			this.val = (float) ticks / (float) speed;
			this.valNext = (float) (ticks + 1) / (float) speed;
		}
		float f = this.val + (this.valNext - this.val) * pt;
		float value = (value_0 + (value_1 - value_0) * f) * 2.0f * pi;
		return value;
	}
	
}
