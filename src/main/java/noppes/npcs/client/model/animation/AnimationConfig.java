package noppes.npcs.client.model.animation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.ModelDataShared;
import noppes.npcs.Server;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.constants.EnumAnimationType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class AnimationConfig
implements IAnimation {

	public static final PartConfig EMPTY_PART = new PartConfig(null);
	static final float PI = (float) Math.PI;

	public String name;
	public int id, frame, repeatLast;
	public boolean disable;
	private long startTick;
	private float val, valNext;
	public final Map<Integer, PartConfig[]> frames; // {Frame, 0:head, 1:left arm, 2:right arm, 3:body, 4:left leg, 5:right leg]}
	public EnumAnimationType type;
	private EntityNPCInterface npc;

	public AnimationConfig(EntityNPCInterface npc, int type) {
		this.npc = npc;
		this.frames = Maps.<Integer, PartConfig[]>newTreeMap();
		this.frames.put(0, new PartConfig[] { new PartConfig(npc), new PartConfig(npc), new PartConfig(npc), new PartConfig(npc), new PartConfig(npc), new PartConfig(npc) });
		if (type<0) {type *= -1; }
		type %= EnumAnimationType.values().length;
		this.type = EnumAnimationType.values()[type];
		this.startTick = 0L;
		this.val = 0.0f;
		this.valNext = 0.0f;
		this.frame = 0;
		this.name = "Default Animation";
		this.id = 0;
		this.disable = false;
		this.repeatLast = 0;
	}

	@Override
	public boolean isDisable() { return this.disable; }
	
	@Override
	public void setDisable(boolean bo) { this.disable = bo; }
	
	@Override
	public IAnimationPart[] getParts(int frame) { return this.frames.get(frame); }
	
	@Override
	public IAnimationPart getPart(int frame, int part) {
		if (!this.frames.containsKey(frame)) {
			throw new CustomNPCsException("Unknown frame " + frame);
		}
		PartConfig[] pcs = this.frames.get(frame);
		if (part<0 || part>=pcs.length) {
			throw new CustomNPCsException("Unknown part " + part + " in frame "+ frame);
		}
		return pcs[part];
	}

	@Override
	public int getType() { return this.type.ordinal(); }

	public void reset() {
		this.val = 0.0f;
		this.valNext = 0.0f;
		this.frame = 0;
		this.startTick = this.npc.world.getTotalWorldTime();
	}

	private float calcValue(float value_0, float value1, int speed, boolean isSmooth, float ticks, float partialTicks) {
		if (ticks >= speed - 1) { ticks = speed - 1; }
		if (isSmooth) {
			this.val = -0.5f * MathHelper.cos((float) ticks / (float) speed * AnimationConfig.PI) + 0.5f;
			this.valNext = -0.5f * MathHelper.cos((float) (ticks+1) / (float) speed * AnimationConfig.PI) + 0.5f;
		} else {
			this.val = (float) ticks / (float) speed;
			this.valNext = (float) (ticks + 1) / (float) speed;
		}
		float f = this.val + (this.valNext - this.val) * partialTicks;
		float value = (value_0 + (value1 - value_0) * f) * 2.0f * AnimationConfig.PI;
		return value;
	}
	/**
	 * Return asix values
	 * @param partType - 0:head, 1:left arm, 2:right arm, 3:body, 4:left leg, 5:right leg
	 * @param valueType - 0:rotations, 1:offsets, 2:scales
	 * @param isCyclical
	 * @param partialTicks
	 * @param npc
	 * @return values float[ x, y, z ]
	 */
	public float[] getValues(int partType, int valueType, boolean isCyclical, float partialTicks, EntityNPCInterface npc) {
		if (this.frames.size()==0 || !this.frames.containsKey(this.frame)) { return null; }
		this.npc = npc;
		PartConfig part_1;
		PartConfig part_0 = (PartConfig) this.frames.get(this.frame)[partType];
		if (this.frames.containsKey(this.frame+1)) { part_1 = (PartConfig) this.frames.get(this.frame + 1)[partType]; }
		else {
			if (this.type==EnumAnimationType.dies) {
				if (this.repeatLast>0 && this.frames.containsKey(this.frame - this.repeatLast)) {
					part_1 = (PartConfig) this.frames.get(this.frame - this.repeatLast)[partType];
				} else {
					part_1 = (PartConfig) this.frames.get(0)[partType];
				}
			}
			else if (this.type.isCyclical()) {
				part_1 = (PartConfig) this.frames.get(0)[partType];
			}
			else { return null; }
		}
		if (part_0.isDisabled()) { part_0 = AnimationConfig.EMPTY_PART; }
		if (part_1.isDisabled()) { part_1 = AnimationConfig.EMPTY_PART; }
		long ticks = this.npc.world.getTotalWorldTime() - this.startTick;
		
		float[] values = new float[] { 0.0f, 0.0f, 0.0f};
		for (int i=0; i<3; i++) {
			float value_0 = part_0.rotation[i] - 0.5f;
			float value_1 = part_1.rotation[i] - 0.5f;
			if (valueType==1) {
				value_0 = 10.0f * part_0.offset[i] - 5.0f;
				value_1 = 10.0f * part_1.offset[i] - 5.0f;
			}
			else if (valueType==2) {
				value_0 = part_0.scale[i] * 5.0f;
				value_1 = part_1.scale[i] * 5.0f;
			}
			values[i] = this.calcValue(value_0, value_1, part_0.speed, part_0.isSmooth, ticks, partialTicks);
			if (valueType!=0) {
				values[i] /= 2 * AnimationConfig.PI;
			}
		}
		if (ticks + part_0.delay  + 1>=part_0.speed) {
			this.frame++;
			if (!this.frames.containsKey(this.frame)) {
				if (this.type.isCyclical()) { this.frame = 0; }
				else { npc.animation.stopAnimation(); }
			}
			this.startTick = this.npc.world.getTotalWorldTime();
		}
		return values;
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.frames.clear();
		for (int i=0; i<compound.getTagList("Frames", 10).tagCount(); i++) {
			NBTTagCompound nbtPart = compound.getTagList("Frames", 10).getCompoundTagAt(i);
			List<PartConfig> pcs = Lists.<PartConfig>newArrayList();
			for (int j=0; j<nbtPart.getTagList("PartConfigs", 10).tagCount(); j++) {
				PartConfig pc = new PartConfig(this.npc);
				pc.readNBT(nbtPart.getTagList("PartConfigs", 10).getCompoundTagAt(j));
				pcs.add(pc);
			}
			this.frames.put(nbtPart.getInteger("FrameID"), pcs.toArray(new PartConfig[pcs.size()]));
		}
		int t = compound.getInteger("Type");
		if (t<0) { t *= -1; }
		this.type = EnumAnimationType.values()[t % EnumAnimationType.values().length];
		this.name = compound.getString("Name");
		this.id = compound.getInteger("ID");
		this.disable = compound.getBoolean("IsDisable");
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		int i = 0;
		for (PartConfig[] pcs : this.frames.values()) {
			NBTTagCompound nbtPart = new NBTTagCompound();
			nbtPart.setInteger("FrameID", i);
			NBTTagList pcsList = new NBTTagList();
			for (PartConfig pc : pcs) { pcsList.appendTag(pc.writeNBT()); }
			nbtPart.setTag("PartConfigs", pcsList);
			list.appendTag(nbtPart);
			i++;
		}
		compound.setTag("Frames", list);
		compound.setInteger("Type", this.type.ordinal());
		compound.setString("Name", this.name);
		compound.setInteger("ID", this.id);
		compound.setBoolean("IsDisable", this.disable);
		return compound;
	}

	@Override
	public int addFrame() {
		int endFrame = this.frames.size();
		this.frames.put(this.frames.size(), new PartConfig[] { new PartConfig(this.npc), new PartConfig(this.npc), new PartConfig(this.npc), new PartConfig(this.npc), new PartConfig(this.npc), new PartConfig(this.npc) });
		return endFrame;
	}

	@Override
	public int addFrame(IAnimationPart[] parts) {
		if (parts==null || parts.length!=6) { return this.addFrame(); }
		int endFrame = this.frames.size();
		PartConfig[] newParts = new PartConfig[] { new PartConfig(this.npc), new PartConfig(this.npc), new PartConfig(this.npc), new PartConfig(this.npc), new PartConfig(this.npc), new PartConfig(this.npc) };
		for (int i=0; i<6; i++) { newParts[i].readNBT(((PartConfig) parts[i]).writeNBT()); }
		this.frames.put(this.frames.size(), newParts);
		return endFrame;
	}
	
	public boolean removeFrame(int frame) {
		if (!this.frames.containsKey(frame)) { return false; }
		Map<Integer, PartConfig[]> newData = Maps.<Integer, PartConfig[]>newTreeMap();
		int i = 0;
		boolean isDel = false;
		for (int f : this.frames.keySet()) {
			if (f==frame) { isDel= true; continue; }
			newData.put(i, this.frames.get(f));
			f++;
		}
		if (isDel) {
			this.frames.clear();
			this.frames.putAll(newData);
		}
		return isDel;
	}
	
	public boolean removePart(int frame, int part) {
		if (!this.frames.containsKey(frame) || part<0 || part>=this.frames.get(frame).length) { return false; }
		this.frames.get(frame)[part].clear();
		return true;
	}

	@Override
	public int getId() { return this.id; }
	
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
		if (npc==null || (npc.getMCEntity() instanceof EntityCustomNpc)) {
			throw new CustomNPCsException("NPC must not be null");
		}
		EntityCustomNpc npcEntity = (EntityCustomNpc) npc.getMCEntity();
		if (npcEntity.modelData instanceof ModelDataShared && ((ModelDataShared) npcEntity.modelData).entityClass==null) {
			((EntityNPCInterface) npcEntity).animation.activeAnim = this;
			if (((EntityNPCInterface) npcEntity).world==null || ((EntityNPCInterface) npcEntity).world.isRemote) { return; }
			NBTTagCompound compound = this.writeToNBT(new NBTTagCompound());
			compound.setInteger("EntityId", this.npc.getEntityId());
			compound.setTag("CustomAnim", this.writeToNBT(new NBTTagCompound()));
			Server.sendAssociatedData((EntityNPCInterface) npcEntity, EnumPacketClient.UPDATE_NPC_ANIMATION, 3, compound);
		}
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
		AnimationConfig ac = new AnimationConfig(this.npc, 0);
		ac.readFromNBT(this.writeToNBT(new NBTTagCompound()));
		return ac;
	}
	
}