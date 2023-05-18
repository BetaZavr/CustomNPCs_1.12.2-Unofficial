package noppes.npcs.entity.data;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.Server;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.entity.data.INPCAnimation;
import noppes.npcs.api.wrapper.NPCWrapper;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.constants.EnumAnimationType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.entity.EntityNPCInterface;

public class DataAnimation
implements INPCAnimation {

	public AnimationConfig activeAnim;
	public final Map<EnumAnimationType, List<AnimationConfig>> data;
	private EntityNPCInterface npc;
	private Random rnd = new Random();
	
	public DataAnimation(EntityNPCInterface npc) {
		this.npc = npc;
		this.data = Maps.<EnumAnimationType, List<AnimationConfig>>newHashMap();
		this.clear();
	}
	
	public void readFromNBT(NBTTagCompound compound) {
		this.data.clear();
		for (int c=0; c<compound.getTagList("AllAnimations", 10).tagCount(); c++) {
			NBTTagCompound nbtCategory = compound.getTagList("AllAnimations", 10).getCompoundTagAt(c);
			int t = nbtCategory.getInteger("Category");
			if (t<0) { t *= -1; }
			t %= EnumAnimationType.values().length;
			EnumAnimationType eat = EnumAnimationType.values()[t];
			List<AnimationConfig> list = Lists.<AnimationConfig>newArrayList();
			for (int i=0; i<nbtCategory.getTagList("Animations", 10).tagCount(); i++) {
				AnimationConfig ac = new AnimationConfig(this.npc, t);
				ac.readFromNBT(nbtCategory.getTagList("Animations", 10).getCompoundTagAt(i));
				ac.id = i;
				list.add(ac);
			}
			this.data.put(eat, list);
		}
		for (EnumAnimationType eat : EnumAnimationType.values()) {
			if (!this.data.containsKey(eat)) { this.data.put(eat, Lists.<AnimationConfig>newArrayList()); }
		}
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList allAnimations = new NBTTagList();
		for (EnumAnimationType eat : this.data.keySet()) {
			NBTTagCompound nbtCategory = new NBTTagCompound();
			nbtCategory.setInteger("Category", eat.ordinal());
			NBTTagList animations = new NBTTagList();
			for (AnimationConfig ac : this.data.get(eat)) {
				animations.appendTag(ac.writeToNBT(new NBTTagCompound()));
			}
			nbtCategory.setTag("Animations", animations);
			allAnimations.appendTag(nbtCategory);
		}
		compound.setTag("AllAnimations", allAnimations);
		return compound;
	}

	public AnimationConfig getActive(EnumAnimationType type) {
		if (this.activeAnim!=null && this.activeAnim.type==type) { return this.activeAnim; }
		this.activeAnim = null;
		List<AnimationConfig> list = this.data.get(type);
		if (list==null) { this.data.put(type, list = Lists.<AnimationConfig>newArrayList()); }
		if (list.size()>0) {
			this.activeAnim = list.get(this.rnd.nextInt(list.size()));
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
		this.stop();
	}
	
	@Override
	public void clear() {
		this.activeAnim = null;
		this.data.clear();
		for (EnumAnimationType eat : EnumAnimationType.values()) {
			this.data.put(eat, Lists.<AnimationConfig>newArrayList());
		}
		this.updateClient(0);
	}

	@Override
	public void update() { this.updateClient(0); }
	
	@Override
	public void stop() {
		if (this.activeAnim!=null) {
			this.activeAnim.reset();
			this.activeAnim = null;
			this.updateClient(1);
		}
	}

	@Override
	public IAnimation[] getAnimations(int animationType) {
		if (animationType<0 || animationType>=EnumAnimationType.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + EnumAnimationType.values().length + " You have: "+animationType);
		}
		List<AnimationConfig> list = this.data.get(EnumAnimationType.values()[animationType]);
		return list.toArray(new IAnimation[list.size()]);
	}

	@Override
	public IAnimation getAnimation(int animationType, int variant) {
		if (animationType<0 || animationType>=EnumAnimationType.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + EnumAnimationType.values().length + " You have: "+animationType);
		}
		List<AnimationConfig> list = this.data.get(EnumAnimationType.values()[animationType]);
		if (variant>=list.size()) {
			throw new CustomNPCsException("Variant must be between 0 and " + list.size() + " You have: "+variant);
		}
		return list.get(variant);
	}

	@Override
	public void start(int animationType) {
		if (animationType<0 || animationType>=EnumAnimationType.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + EnumAnimationType.values().length + " You have: "+animationType);
		}
		List<AnimationConfig> list = this.data.get(EnumAnimationType.values()[animationType]);
		if (list.size()==0) { return; }
		if (this.npc.world==null || this.npc.world.isRemote) {
			this.activeAnim = list.get(this.rnd.nextInt(list.size()));
		} else {
			this.updateClient(2, animationType, this.rnd.nextInt(list.size()));
		}
	}

	@Override
	public void start(int animationType, int variant) {
		if (variant<0) {
			this.start(animationType);
			return;
		}
		if (animationType<0 || animationType>=EnumAnimationType.values().length) {
			throw new CustomNPCsException("Animation Type must be between 0 and " + EnumAnimationType.values().length + " You have: "+animationType);
		}
		List<AnimationConfig> list = this.data.get(EnumAnimationType.values()[animationType]);
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
	public void startFromSaved(int animationId) {
		IAnimation anim = AnimationController.getInstance().getAnimation(animationId);
		if (anim!=null) { anim.startToNpc(new NPCWrapper<EntityNPCInterface>(this.npc));}
	}
	
	@Override
	public void startFromSaved(String animationName) {
		IAnimation anim = AnimationController.getInstance().getAnimation(animationName);
		if (anim!=null) { anim.startToNpc(new NPCWrapper<EntityNPCInterface>(this.npc));}
	}
	
	@Override
	public INbt getNbt() { return NpcAPI.Instance().getINbt(this.writeToNBT(new NBTTagCompound())); }

	@Override
	public void setNbt(INbt nbt) { this.readFromNBT(nbt.getMCNBT()); }
	
}
