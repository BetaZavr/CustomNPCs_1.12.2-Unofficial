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
import net.minecraft.util.math.MathHelper;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.Server;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.entity.data.INPCAnimation;
import noppes.npcs.api.event.AnimationEvent;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.client.model.animation.EmotionConfig;
import noppes.npcs.client.model.animation.PartConfig;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;

public class DataAnimation
implements INPCAnimation {

	public AnimationConfig activeAnim = null;
	public EmotionConfig activeEmtn = null;
	public final Map<AnimationKind, List<Integer>> data = Maps.<AnimationKind, List<Integer>>newHashMap();
	public final List<EmotionConfig> emotion = Lists.<EmotionConfig>newArrayList();
	private EntityNPCInterface npc;
	private Random rnd = new Random();

	private AnimationConfig oldAnim;
	public boolean isComplete;
	public int frame;
	public long startFrameTick;
	private float val, valNext;
	public boolean isAnimated;
	public final List<Boolean> showParts = Lists.<Boolean>newArrayList(new Boolean[] { true, true, true, true, true, true } );
	
	public DataAnimation(EntityNPCInterface npc) {
		this.npc = npc;
		for (AnimationKind eak : AnimationKind.values()) { data.put(eak, Lists.<Integer>newArrayList()); }
		this.clear();
	}
	
	public void load(NBTTagCompound compound) {
		data.clear();
		this.emotion.clear();
		AnimationController aData = AnimationController.getInstance();
		if (compound.hasKey("AllAnimations", 9)) {
			for (int c=0; c<compound.getTagList("AllAnimations", 10).tagCount(); c++) {
				NBTTagCompound nbtCategory = compound.getTagList("AllAnimations", 10).getCompoundTagAt(c);
				int t = nbtCategory.getInteger("Category");
				if (t < 0) { t *= -1; }
				AnimationKind type = AnimationKind.get(t % AnimationKind.values().length);
				List<Integer> list = Lists.<Integer>newArrayList();
				for (int i = 0; i<nbtCategory.getTagList("Animations", 3).tagCount(); i++) {
					int id = nbtCategory.getTagList("Animations", 3).getIntAt(i);
					if (!list.contains(id)) { list.add(id); }
				}
				for (int i = 0; i<nbtCategory.getTagList("Animations", 10).tagCount(); i++) {
					NBTTagCompound nbt = nbtCategory.getTagList("Animations", 10).getCompoundTagAt(i);
					int id = nbt.getInteger("ID");
					String name = npc.getName() + "_" + nbt.getString("Name");
					AnimationConfig anim = (AnimationConfig) aData.getAnimation(id);
					if (anim == null || !anim.getName().equals(name)) { anim = (AnimationConfig) aData.createNew(); }
					if (anim != null) {
						id = anim.id;
						anim.readFromNBT(nbt);
						anim.name = name;
						anim.id = id;
					}
					if (!list.contains(id)) { list.add(id); }
				}
				Collections.sort(list);
				data.put(type, list);
			}
		}
		for (AnimationKind eat : AnimationKind.values()) {
			if (!data.containsKey(eat)) { data.put(eat, Lists.<Integer>newArrayList()); }
		}
		
		for (int c=0; c<compound.getTagList("AllEmotions", 10).tagCount(); c++) {
			
		}
	}

	public NBTTagCompound save(NBTTagCompound compound) {
		NBTTagList allAnimations = new NBTTagList();
		NBTTagList allEmotions = new NBTTagList();
		for (AnimationKind eat : data.keySet()) {
			NBTTagCompound nbtCategory = new NBTTagCompound();
			nbtCategory.setInteger("Category", eat.get());
			NBTTagList animations = new NBTTagList();
			for (int id : data.get(eat)) { animations.appendTag(new NBTTagInt(id)); }
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
		if (this.activeAnim!=null && this.activeAnim.type == type) {
			if (this.frame < this.activeAnim.frames.size() || this.activeAnim.isEdit) {
				if (this.frame >= this.activeAnim.frames.size() && this.activeAnim.isEdit) {
					this.frame = -1;
				}
				return this.activeAnim;
			}
		}
		if (this.activeAnim != null) { this.updateClient(1, this.activeAnim.type.get(), this.activeAnim.id); }
		this.activeAnim = null;
		this.val = 0.0f;
		this.valNext = 0.0f;
		this.startFrameTick = 0;
		List<Integer> ids = data.get(type);
		if (ids==null) { data.put(type, ids = Lists.<Integer>newArrayList()); }

		AnimationController aData = AnimationController.getInstance();
		List<AnimationConfig> list = aData.getAnimations(data.get(type));
		if (list.size()>0) {
			List<AnimationConfig> selectList = Lists.<AnimationConfig>newArrayList();
			for (AnimationConfig ac : list) { selectList.add(ac); }
			if (selectList.size()>0) {
				this.activeAnim = selectList.get(this.rnd.nextInt(selectList.size()));
			}
		}
		if (this.activeAnim!=null) {
			this.frame = -1;
			this.activeAnim.type = type;
		}
		return this.activeAnim;
	}

	public void updateClient(int type, int ... var) {
		if (this.npc.world==null || this.npc.world.isRemote) {
			if (type==1) { NoppesUtilPlayer.sendData(EnumPlayerPacket.StopNPCAnimation, this.npc.getEntityId(), var[0], var[1]); }
			return;
		}
		NBTTagCompound compound = this.save(new NBTTagCompound());
		compound.setInteger("EntityId", this.npc.getEntityId());
		if (var!=null && var.length>0) { compound.setIntArray("Vars", var); }
		Server.sendAssociatedData(this.npc, EnumPacketClient.UPDATE_NPC_ANIMATION, type, compound);
	}
	
	@Override
	public void reset() {
		this.stopAnimation();
		this.stopEmotion();
	}
	
	@Override
	public void clear() {
		this.isComplete = false;
		this.activeAnim = null;
		this.activeEmtn = null;
		data.clear();
		this.emotion.clear();
		for (List<Integer> ids : data.values()) { ids.clear(); }
		this.updateClient(0);
	}

	@Override
	public void update() { this.updateClient(0); }
	
	@Override
	public void stopAnimation() {
		if (this.activeAnim!=null) {
			this.updateClient(1, this.activeAnim.type.get(), this.activeAnim.id);
			this.activeAnim = null;
		}
		this.oldAnim = null;
		this.val = 0.0f;
		this.valNext = 0.0f;
		this.startFrameTick = 0;
		this.isComplete = false;
	}

	@Override
	public void stopEmotion() {
		if (this.activeEmtn!=null) {
			this.activeEmtn.reset();
			this.activeEmtn = null;
			this.updateClient(3);
		}
	}

	@Override
	public IAnimation[] getAnimations(int animationType) {
		if (animationType<0 || animationType>=AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: "+animationType);
		}
		List<AnimationConfig> list = AnimationController.getInstance().getAnimations(data.get(AnimationKind.get(animationType)));
		return list.toArray(new IAnimation[list.size()]);
	}

	@Override
	public IAnimation getAnimation(int animationType, int variant) {
		if (animationType<0 || animationType>=AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: "+animationType);
		}
		List<AnimationConfig> list = AnimationController.getInstance().getAnimations(data.get(AnimationKind.get(animationType)));
		if (list.isEmpty()) { return null; }
		if (variant >= list.size()) {
			throw new CustomNPCsException("Variant must be between 0 and " + (list.size()-1) + " You have: "+variant);
		}
		return list.get(variant);
	}

	@Override
	public void startAnimation(int animationType) {
		if (animationType<0 || animationType>=AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: "+animationType);
		}
		List<AnimationConfig> list = AnimationController.getInstance().getAnimations(data.get(AnimationKind.get(animationType)));
		if (list.size()==0) { return; }
		int variant = this.rnd.nextInt(list.size());
		if (this.npc.world==null || this.npc.world.isRemote) {
			this.activeAnim = list.get(variant);
			this.isComplete = false;
		}
		else { this.updateClient(2, animationType, variant); }
	}

	@Override
	public void startAnimation(int animationType, int variant) {
		if (variant<0) {
			this.startAnimation(animationType);
			return;
		}
		if (animationType<0 || animationType>=AnimationKind.values().length) { throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: "+animationType); }
		List<AnimationConfig> list = AnimationController.getInstance().getAnimations(data.get(AnimationKind.get(animationType)));
		if (variant >= list.size()) {
			throw new CustomNPCsException("Variant must be between 0 and " + list.size() + " You have: "+variant);
		}
		if (this.npc.world==null || this.npc.world.isRemote) {
			this.activeAnim = list.get(variant);
			this.isComplete = false;
		} else {
			this.updateClient(2, animationType, variant);
		}
	}
	
	@Override
	public INbt getNbt() { return NpcAPI.Instance().getINbt(this.save(new NBTTagCompound())); }

	@Override
	public void setNbt(INbt nbt) { this.load(nbt.getMCNBT()); }

	@Override
	public boolean removeAnimation(int animationType, int animationId) {
		if (animationType < 0 || animationType>=AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: "+animationType);
		}
		AnimationKind type = AnimationKind.get(animationType);
		if (!data.containsKey(type)) { data.put(type, Lists.<Integer>newArrayList()); }
		for (Integer id : data.get(type)) {
			if (id == animationId) { return data.get(type).remove(id); }
		}
		return false;
	}
	
	@Override
	public void removeAnimations(int animationType) {
		if (animationType<0 || animationType>=AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: "+animationType);
		}
		AnimationKind type = AnimationKind.get(animationType);
		if (!data.containsKey(type)) { data.put(type, Lists.<Integer>newArrayList()); }
		data.get(type).clear();
	}
	
	public Map<Integer, Float[]> getValues(EntityNPCInterface npc, AnimationConfig anim, float pt) {
		if (anim==null || anim.frames.isEmpty()) { return null; }
		if (this.startFrameTick<=0) { this.startFrameTick = npc.world.getTotalWorldTime(); }
		int ticks = (int) (npc.world.getTotalWorldTime() - this.startFrameTick);
		AnimationFrameConfig frame_0 = null, frame_1 = null;
		AnimationEvent event;
		if (this.frame==-1 && (anim.type==AnimationKind.ATTACKING ||
				anim.type==AnimationKind.DIES ||
				anim.type==AnimationKind.JUMP ||
				anim.type==AnimationKind.INIT)) {
			this.frame = 0;
			this.isComplete = false;
			event = new AnimationEvent.StartEvent(npc, anim);
			EventHooks.onEvent(ScriptController.Instance.clientScripts, event.nameEvent, event);
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
			if (this.frame == anim.frames.size() - 1) {
				frame_0 = anim.frames.get(anim.frames.size()-1);
				frame_1 = anim.frames.get(this.frame);
			} else {
				this.frame = 0;
				frame_0 = anim.frames.get(this.frame);
				frame_1 = anim.frames.get(this.frame + 1);
			}
			this.frame = -1;
			this.startFrameTick = 0;
		}
		else if (anim.repeatLast>0 || anim.type==AnimationKind.DIES) { // repeat end
			int f = anim.repeatLast<=0 ? 1 : anim.repeatLast;
			this.frame = anim.frames.size() - f;
			if (this.frame<0) { this.frame = 0; }
			frame_0 = anim.frames.get(this.frame);
			frame_1 = anim.frames.containsKey(this.frame + 1) ? anim.frames.get(this.frame + 1) : frame_0;
		}
		this.showParts.clear();
		if (frame_0 == null || frame_1 == null) {
			if (this.activeAnim!=null) { this.updateClient(1, this.activeAnim.type.get(), this.activeAnim.id); }
			this.activeAnim = null;
			for (int i = 0; i < 6; i++) { this.showParts.add(true); }
			this.frame = -1;
			this.startFrameTick = 0;
			return null;
		}
		for (PartConfig part : frame_0.parts.values()) { this.showParts.add(part.isShow()); }
		
		
		int speed = frame_0.getSpeed();
		if (anim.type.isMoving()) {
			double sp = npc.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
			speed = (int) ((double) speed * 0.25d / sp);
		}
		
		Map<Integer, Float[]> map = Maps.<Integer, Float[]>newTreeMap();
		for (int part=0; part<6; part++) { // 0:head, 1:left arm, 2:right arm, 3:body, 4:left leg, 5:right leg
			Float[] values = new Float[] { 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f }; // rotX, rotY, rotZ, ofsX, ofsY, ofsZ, scX, scY, scZ
			if (frame_0.parts.get(part).isDisable()) {
				map.put(part, null);
				continue;
			}
			for (int t=0; t<3; t++) { // 0:rotations, 1:offsets, 2:scales
				for (int a=0; a<3; a++) { // x, y, z
					float value_0;
					float value_1;
					switch(t) {
						case 1: {
							value_0 = 10.0f * frame_0.parts.get(part).offset[a] - 5.0f;
							value_1 = 10.0f * frame_1.parts.get(part).offset[a] - 5.0f;
							break;
						}
						case 2: {
							value_0 = frame_0.parts.get(part).scale[a] * 5.0f;
							value_1 = frame_1.parts.get(part).scale[a] * 5.0f;
							break;
						}
						default: {
							value_0 = frame_0.parts.get(part).rotation[a];
							value_1 = frame_1.parts.get(part).rotation[a];
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
					values[t * 3 + a] = this.calcValue(value_0, value_1, speed, frame_0.isSmooth(), ticks, pt);
					if (t!=0) { values[t * 3 + a] /= 2 * (float) Math.PI; } // offsets, scales - correction
				}
			}
			map.put(part, values);
		}
		if (ticks >= speed + frame_1.getEndDelay()) {
			this.frame++;
			this.startFrameTick = npc.world.getTotalWorldTime();
			this.oldAnim = anim;
			this.isComplete = this.frame >= anim.frames.size()-1;
			if (this.isComplete) { event = new AnimationEvent.StopEvent(npc, anim); }
			else { event = new AnimationEvent.NextFrameEvent(npc, anim); }
			EventHooks.onEvent(ScriptController.Instance.clientScripts, event.nameEvent, event);
			if (this.isComplete && (anim.repeatLast>0 || anim.type==AnimationKind.DIES)) {
				int f = anim.repeatLast<=0 ? 1 : anim.repeatLast;
				this.frame = anim.frames.size() - f;
				if (this.frame<0) { this.frame = 0; }
			}
		}
		return map;
	}
	
	private float calcValue(float value_0, float value_1, int speed, boolean isSmooth, float ticks, float pt) {
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
