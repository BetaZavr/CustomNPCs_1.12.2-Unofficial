package noppes.npcs.client.model.animation;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.Server;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.entity.data.IAnimationFrame;
import noppes.npcs.client.model.part.ModelDataShared;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class AnimationConfig
implements IAnimation {

	public String name;
	public int repeatLast;
	public boolean isEdit;
	public final Map<Integer, AnimationFrameConfig> frames; // {Frame, setting Frame]}

	public int id;
	public AnimationKind type;

	public AnimationConfig() {
		this.frames = Maps.<Integer, AnimationFrameConfig>newTreeMap();
		this.frames.put(0, new AnimationFrameConfig());
		this.id = 0;
		this.name = "Default Animation";
		this.repeatLast = 0;
		this.isEdit = false;
	}

	@Override
	public int getId() { return this.id; }

	@Override
	public IAnimationFrame[] getFrames() {
		IAnimationFrame[] frames = new IAnimationFrame[this.frames.size()];
		for (int id : this.frames.keySet()) { frames[id] = this.frames.get(id); }
		return frames;
	}
	
	@Override
	public boolean hasFrame(int frame) { return this.frames.containsKey(frame); }
	
	@Override
	public IAnimationFrame getFrame(int frame) {
		if (!this.frames.containsKey(frame)) {
			throw new CustomNPCsException("Unknown frame " + frame);
		}
		return this.frames.get(frame);
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.frames.clear();
		for (int i=0; i<compound.getTagList("FrameConfigs", 10).tagCount(); i++) {
			AnimationFrameConfig afc = new AnimationFrameConfig();
			afc.readNBT(compound.getTagList("FrameConfigs", 10).getCompoundTagAt(i));
			afc.id = i;
			this.frames.put(i, afc);
		}
		if (this.frames.size()==0) { this.frames.put(0, new AnimationFrameConfig()); }
		this.id = compound.getInteger("ID");
		this.name = compound.getString("Name");
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for (AnimationFrameConfig afc : this.frames.values()) { list.appendTag(afc.writeNBT()); }
		compound.setTag("FrameConfigs", list);
		compound.setInteger("ID", this.id);
		compound.setString("Name", this.name);
		return compound;
	}

	@Override
	public IAnimationFrame addFrame() {
		int f = this.frames.size();
		this.frames.put(f, new AnimationFrameConfig());
		this.frames.get(f).id = f;
		return this.frames.get(f);
	}

	@Override
	public IAnimationFrame addFrame(IAnimationFrame frame) {
		if (frame==null) { return this.addFrame(); }
		int f = this.frames.size();
		this.frames.put(f, ((AnimationFrameConfig) frame).copy());
		this.frames.get(f).id = f;
		return this.frames.get(f);
	}

	@Override
	public boolean removeFrame(int frameId) {
		if (this.frames.size()<=1) { return false; }
		if (!this.frames.containsKey(frameId)) {
			throw new CustomNPCsException("Unknown frame ID:" + frameId);
		}
		Map<Integer, AnimationFrameConfig> newData = Maps.<Integer, AnimationFrameConfig>newTreeMap();
		int i = 0;
		boolean isDel = false;
		for (int f : this.frames.keySet()) {
			if (f == frameId) { isDel= true; continue; }
			newData.put(i, this.frames.get(f).copy());
			newData.get(i).id = i;
			i++;
		}
		if (isDel) {
			this.frames.clear();
			if (newData.size()==0) {
				newData.put(0, new AnimationFrameConfig());
			}
			this.frames.putAll(newData);
		}
		return isDel;
	}

	@Override
	public boolean removeFrame(IAnimationFrame frame) {
		if (frame==null || this.frames.size() <= 1) { return false; }
		for (int f : this.frames.keySet()) {
			if (this.frames.get(f).equals(frame)) {
				this.removeFrame(f);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String getName() { return this.name; }
	
	@Override
	public void setName(String name) {
		if (name==null || name.isEmpty()) { name = "Default Animation"; }
		this.name = name;
	}

	@Override
	public INbt getNbt() { return NpcAPI.Instance().getINbt(this.writeToNBT(new NBTTagCompound())); }
	
	@Override
	public void setNbt(INbt nbt) { this.readFromNBT(nbt.getMCNBT()); }

	@Override
	public void startToNpc(ICustomNpc<?> npc) {
		if (npc==null || !(npc.getMCEntity() instanceof EntityCustomNpc)) {
			throw new CustomNPCsException("NPC must not be null");
		}
		this.startToNpc((EntityCustomNpc) npc.getMCEntity());
	}
	
	public void startToNpc(EntityCustomNpc npcEntity) {
		if (npcEntity==null || !(npcEntity.modelData instanceof ModelDataShared) || ((ModelDataShared) npcEntity.modelData).entityClass!=null) { return; }
		((EntityNPCInterface) npcEntity).animation.activeAnim = this;
		if (((EntityNPCInterface) npcEntity).world==null || ((EntityNPCInterface) npcEntity).world.isRemote) { return; }
		NBTTagCompound compound = this.writeToNBT(new NBTTagCompound());
		compound.setInteger("EntityId", npcEntity.getEntityId());
		compound.setTag("CustomAnim", this.writeToNBT(new NBTTagCompound()));
		Server.sendAssociatedData((EntityNPCInterface) npcEntity, EnumPacketClient.UPDATE_NPC_ANIMATION, 3, compound);
	}

	@Override
	public int getRepeatLast() { return this.repeatLast; }
	
	@Override
	public void setRepeatLast(int frames) {
		if (frames < 0) { frames = 0; }
		if (frames > this.frames.size()) { frames = this.frames.size(); }
		this.repeatLast = frames;
	}

	public AnimationConfig copy() {
		AnimationConfig ac = new AnimationConfig();
		ac.readFromNBT(this.writeToNBT(new NBTTagCompound()));
		return ac;
	}
	
	
	public String getSettingName() {
		String c = "" + ((char) 167);
		return c+"7"+this.id+": "+c+"r"+this.name;
	}

}