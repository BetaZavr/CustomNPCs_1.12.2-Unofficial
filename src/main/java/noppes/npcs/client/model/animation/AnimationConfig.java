package noppes.npcs.client.model.animation;

import java.util.Iterator;
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

public class AnimationConfig implements IAnimation {

	public String name = "Default Animation";
	public int repeatLast = 0;
	public boolean isEdit = false;
	public final Map<Integer, AnimationFrameConfig> frames = Maps.<Integer, AnimationFrameConfig>newTreeMap(); // {Frame, setting Frame]}

	public int id = 0;
	public AnimationKind type = AnimationKind.STANDING;

	public AnimationConfig() { this.frames.put(0, new AnimationFrameConfig()); }

	@Override
	public IAnimationFrame addFrame() {
		int f = this.frames.size();
		this.frames.put(f, new AnimationFrameConfig());
		this.frames.get(f).id = f;
		return this.frames.get(f);
	}

	@Override
	public IAnimationFrame addFrame(int frameId, IAnimationFrame frame) {
		if (frame == null) { return this.addFrame(); }
		if (frameId < 0) {
			frameId = this.frames.size();
			this.frames.put(frameId, ((AnimationFrameConfig) frame).copy());
			this.frames.get(frameId).id = frameId;
		} else {
			Map<Integer, AnimationFrameConfig> newFrames = Maps.<Integer, AnimationFrameConfig>newTreeMap();
			int j = 0;
			for (int i : this.frames.keySet()) {
				if (i == frameId) {
					newFrames.put(j, ((AnimationFrameConfig) frame).copy());
					j++;
				}
				newFrames.put(j, ((AnimationFrameConfig) frame).copy());
				j++;
			}
			this.frames.clear();
			this.frames.putAll(newFrames);
		}
		return this.frames.get(frameId);
	}

	public AnimationConfig copy() {
		AnimationConfig ac = new AnimationConfig();
		ac.readFromNBT(this.writeToNBT(new NBTTagCompound()));
		return ac;
	}

	@Override
	public IAnimationFrame getFrame(int frameId) {
		if (!this.frames.containsKey(frameId)) {
			throw new CustomNPCsException("Unknown frame " + frameId);
		}
		return this.frames.get(frameId);
	}

	@Override
	public IAnimationFrame[] getFrames() {
		IAnimationFrame[] frames = new IAnimationFrame[this.frames.size()];
		for (int id : this.frames.keySet()) {
			frames[id] = this.frames.get(id);
		}
		return frames;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public INbt getNbt() {
		return NpcAPI.Instance().getINbt(this.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public int getRepeatLast() {
		return this.repeatLast;
	}

	public String getSettingName() {
		String c = "" + ((char) 167);
		return c + "7" + this.id + ": " + c + "r" + this.name;
	}

	@Override
	public boolean hasFrame(int frameId) {
		return this.frames.containsKey(frameId);
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.frames.clear();
		for (int i = 0; i < compound.getTagList("FrameConfigs", 10).tagCount(); i++) {
			AnimationFrameConfig afc = new AnimationFrameConfig();
			afc.readNBT(compound.getTagList("FrameConfigs", 10).getCompoundTagAt(i));
			afc.id = i;
			this.frames.put(i, afc);
		}
		if (this.frames.size() == 0) {
			this.frames.put(0, new AnimationFrameConfig());
		}
		this.id = compound.getInteger("ID");
		this.name = compound.getString("Name");
	}

	@Override
	public boolean removeFrame(IAnimationFrame frameId) {
		if (frameId == null || this.frames.size() <= 1) {
			return false;
		}
		for (int f : this.frames.keySet()) {
			if (this.frames.get(f).equals(frameId)) {
				this.removeFrame(f);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removeFrame(int frameId) {
		if (this.frames.size() <= 1) {
			return false;
		}
		if (!this.frames.containsKey(frameId)) {
			throw new CustomNPCsException("Unknown frame ID:" + frameId);
		}
		Map<Integer, AnimationFrameConfig> newData = Maps.<Integer, AnimationFrameConfig>newTreeMap();
		int i = 0;
		boolean isDel = false;
		for (int f : this.frames.keySet()) {
			if (f == frameId) {
				isDel = true;
				continue;
			}
			newData.put(i, this.frames.get(f).copy());
			newData.get(i).id = i;
			i++;
		}
		if (isDel) {
			this.frames.clear();
			if (newData.size() == 0) {
				newData.put(0, new AnimationFrameConfig());
			}
			this.frames.putAll(newData);
		}
		return isDel;
	}

	@Override
	public void setName(String name) {
		if (name == null || name.isEmpty()) {
			name = "Default Animation";
		}
		this.name = name;
	}

	@Override
	public void setNbt(INbt nbt) {
		this.readFromNBT(nbt.getMCNBT());
	}

	@Override
	public void setRepeatLast(int frames) {
		if (frames < 0) {
			frames = 0;
		}
		if (frames > this.frames.size()) {
			frames = this.frames.size();
		}
		this.repeatLast = frames;
	}

	public void startToNpc(EntityCustomNpc npcEntity) {
		if (npcEntity == null || !(npcEntity.modelData instanceof ModelDataShared)
				|| ((ModelDataShared) npcEntity.modelData).entityClass != null) {
			return;
		}
		((EntityNPCInterface) npcEntity).animation.startAnimation(this);
		if (((EntityNPCInterface) npcEntity).world == null || ((EntityNPCInterface) npcEntity).world.isRemote) {
			return;
		}
		NBTTagCompound compound = this.writeToNBT(new NBTTagCompound());
		compound.setInteger("EntityId", npcEntity.getEntityId());
		compound.setTag("CustomAnim", this.writeToNBT(new NBTTagCompound()));
		Server.sendAssociatedData((EntityNPCInterface) npcEntity, EnumPacketClient.UPDATE_NPC_ANIMATION, 3, compound);
	}

	@Override
	public void startToNpc(ICustomNpc<?> npc) {
		if (npc == null || !(npc.getMCEntity() instanceof EntityCustomNpc)) {
			throw new CustomNPCsException("NPC must not be null");
		}
		this.startToNpc((EntityCustomNpc) npc.getMCEntity());
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		Iterator<AnimationFrameConfig> setss = this.frames.values().iterator();
		while(setss != null && setss.hasNext()) {
			try {
				AnimationFrameConfig afc = setss.next();
				list.appendTag(afc.writeNBT());
			}
			catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}
		
		compound.setTag("FrameConfigs", list);
		compound.setInteger("ID", this.id);
		compound.setString("Name", this.name);
		return compound;
	}

}