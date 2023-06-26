package noppes.npcs.entity.data;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.entity.data.INPCAnimation;
import noppes.npcs.api.wrapper.NPCWrapper;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.AnimationFrameConfig;
import noppes.npcs.client.model.animation.EmotionConfig;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.entity.EntityNPCInterface;

public class DataAnimation
implements INPCAnimation {

	public AnimationConfig activeAnim;
	public EmotionConfig activeEmtn;
	public final Map<AnimationKind, List<AnimationConfig>> data;
	public final List<EmotionConfig> emotion;
	private EntityNPCInterface npc;
	private Random rnd = new Random();
	
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
				AnimationKind eat = AnimationKind.values()[t];
				List<AnimationConfig> list = Lists.<AnimationConfig>newArrayList();
				for (int i=0; i<nbtCategory.getTagList("Animations", 10).tagCount(); i++) {
					AnimationConfig ac = new AnimationConfig(t);
					ac.readFromNBT(nbtCategory.getTagList("Animations", 10).getCompoundTagAt(i));
					list.add(ac);
				}
				this.data.put(eat, list);
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
		if (this.activeAnim!=null && this.activeAnim.type==type) { return this.activeAnim; }
		this.activeAnim = null;
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
		if (this.activeAnim!=null) { this.activeAnim.reset(); }
		return this.activeAnim;
	}

	private void updateClient(int type, int ... var) {
		if (this.npc.world==null || this.npc.world.isRemote) { return; }
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
			this.activeAnim.reset();
			this.activeAnim = null;
			this.updateClient(1);
		}
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
		List<AnimationConfig> list = this.data.get(AnimationKind.values()[animationType]);
		return list.toArray(new IAnimation[list.size()]);
	}

	@Override
	public IAnimation getAnimation(int animationType, int variant) {
		if (animationType<0 || animationType>=AnimationKind.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + AnimationKind.values().length + " You have: "+animationType);
		}
		List<AnimationConfig> list = this.data.get(AnimationKind.values()[animationType]);
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
		List<AnimationConfig> list = this.data.get(AnimationKind.values()[animationType]);
		if (list.size()==0) { return; }
		if (this.npc.world==null || this.npc.world.isRemote) {
			this.activeAnim = list.get(this.rnd.nextInt(list.size()));
		} else {
			this.updateClient(2, animationType, this.rnd.nextInt(list.size()));
		}
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
		List<AnimationConfig> list = this.data.get(AnimationKind.values()[animationType]);
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
		AnimationKind t = AnimationKind.values()[animationType];
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
		this.data.get(AnimationKind.values()[animationType]).clear();
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
	
}
