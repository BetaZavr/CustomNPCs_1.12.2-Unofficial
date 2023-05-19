package noppes.npcs.client.model.animation;

import java.util.Map;

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
import noppes.npcs.api.entity.data.IAnimationFrame;
import noppes.npcs.constants.EnumAnimationType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class AnimationConfig
implements IAnimation {

	public static final AnimationFrameConfig EMPTY_PART = new AnimationFrameConfig();
	static final float PI = (float) Math.PI;

	public String name;
	public int frame, repeatLast;
	public boolean disable;
	public final Map<Integer, AnimationFrameConfig> frames; // {Frame, setting Frame]}
	public EnumAnimationType type;

	private long startTick;
	private int animMaxFrame, animDelay, animSpeed;
	private float val, valNext;
	private Map<Integer, Map<Integer, Float[]>> dismplayMap; // tick, patr values

	public AnimationConfig(int type) {
		this.dismplayMap = Maps.<Integer, Map<Integer, Float[]>>newTreeMap();
		this.frames = Maps.<Integer, AnimationFrameConfig>newTreeMap();
		this.frames.put(0, new AnimationFrameConfig());
		this.reset();
		
		this.name = "Default Animation";
		this.disable = false;
		this.repeatLast = 0;

		if (type<0) {type *= -1; }
		type %= EnumAnimationType.values().length;
		this.type = EnumAnimationType.values()[type];
		
	}

	public void reset() {
		this.val = 0.0f;
		this.valNext = 0.0f;
		this.frame = 0;
		this.startTick = 0;
		this.animMaxFrame = -1;
		this.animDelay = 0;
		this.animSpeed = 0;
		this.frames.clear();
	}

	@Override
	public boolean isDisable() { return this.disable; }

	@Override
	public void setDisable(boolean bo) { this.disable = bo; }

	@Override
	public IAnimationFrame[] getFrames() { return this.frames.values().toArray(new IAnimationFrame[this.frames.size()]); }

	@Override
	public IAnimationFrame getFrame(int frame) {
		if (!this.frames.containsKey(frame)) {
			throw new CustomNPCsException("Unknown frame " + frame);
		}
		return this.frames.get(frame);
	}

	@Override
	public int getType() { return this.type.ordinal(); }

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
	public Map<Integer, Float[]> getValues(float partialTicks, EntityNPCInterface npc) {
		if (this.frames.size()==0 || !this.frames.containsKey(this.frame)) { return null; }
		if (this.startTick<=0) { this.startTick = npc.world.getTotalWorldTime(); }
		
		int ticks = (int) (npc.world.getTotalWorldTime() - this.startTick);
		
		Map<Integer, Float[]> map;
		
		if (this.dismplayMap.containsKey(ticks)) { map = this.dismplayMap.get(ticks); }
		else if (this.dismplayMap.containsKey(this.animMaxFrame)) { map = this.dismplayMap.get(this.animMaxFrame); }
		else {
			AnimationFrameConfig frame_0 = this.frames.get(this.frame);
			this.animMaxFrame = frame_0.getSpeed();
			this.animDelay = frame_0.getEndDelay();
			this.animSpeed = frame_0.getEndDelay();
			AnimationFrameConfig frame_1;
			if (this.frames.containsKey(this.frame+1)) { frame_1 = this.frames.get(this.frame + 1); }
			else {
				if (this.type.isCyclical()) {
					if (this.repeatLast>0 && this.frames.containsKey(this.frame - this.repeatLast)) {
						frame_1 = this.frames.get(this.frame - this.repeatLast);
					} else {
						frame_1 = this.frames.get(0);
					}
				}
				else { return null; }
			}
			frame_0.setNpc(npc);
			frame_1.setNpc(npc);
			
			map = Maps.<Integer, Float[]>newTreeMap();
			for (int part=0; part<6; part++) {
				Float[] values = new Float[] { 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.2f, 0.2f, 0.2f }; // rotX, rotY, rotZ, ofsX, ofsY, ofsZ, scX, scY, scZ
				if ( frame_0.parts[part].isDisable()) {
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
						values[t*3+t] = this.calcValue(value_0, value_1, frame_0.getSpeed(), frame_0.isSmooth(), ticks, partialTicks);
						if (t!=0) { values[t*3+t] /= 2 * AnimationConfig.PI; } // offsets, scales - correction
					}
				}
				map.put(part, values);
			}
			return map;
		}
		
		if (ticks >= this.animSpeed + this.animDelay) {
			this.frame++;
			if (!this.frames.containsKey(this.frame)) {
				if (this.type.isCyclical()) { this.frame = 0; }
				else { npc.animation.stopAnimation(); }
			}
			this.startTick = npc.world.getTotalWorldTime();
		}
		return map;
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.frames.clear();
		for (int i=0; i<compound.getTagList("FrameConfigs", 10).tagCount(); i++) {
			AnimationFrameConfig afc = new AnimationFrameConfig();
			afc.readNBT(compound.getTagList("FrameConfigs", 10).getCompoundTagAt(i));
			afc.id = i;
			this.frames.put(i, afc);
		}
		int t = compound.getInteger("Type");
		if (t<0) { t *= -1; }
		this.type = EnumAnimationType.values()[t % EnumAnimationType.values().length];
		this.name = compound.getString("Name");
		this.disable = compound.getBoolean("IsDisable");
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for (AnimationFrameConfig afc : this.frames.values()) { list.appendTag(afc.writeNBT()); }
		compound.setTag("FrameConfigs", list);
		
		compound.setInteger("Type", this.type.ordinal());
		compound.setString("Name", this.name);
		compound.setBoolean("IsDisable", this.disable);
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
	public boolean removeFrame(int frame) {
		if (!this.frames.containsKey(frame)) {
			throw new CustomNPCsException("Unknown frame " + frame);
		}
		if (this.frames.size()<=1) {
			this.frames.get(0).clear();
			return true;
		}
		Map<Integer, AnimationFrameConfig> newData = Maps.<Integer, AnimationFrameConfig>newTreeMap();
		int i = 0;
		boolean isDel = false;
		for (int f : this.frames.keySet()) {
			if (f==frame) { isDel= true; continue; }
			newData.put(i, this.frames.get(f).copy());
			newData.get(i).id = i;
			i++;
		}
		if (isDel) {
			this.frames.clear();
			this.frames.putAll(newData);
		}
		return isDel;
	}

	@Override
	public boolean removeFrame(IAnimationFrame frame) {
		for (int f : this.frames.keySet()) {
			if (this.frames.get(f).equals(frame)) { return this.removeFrame(f); }
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
		if (npc==null || (npc.getMCEntity() instanceof EntityCustomNpc)) {
			throw new CustomNPCsException("NPC must not be null");
		}
		EntityCustomNpc npcEntity = (EntityCustomNpc) npc.getMCEntity();
		if (npcEntity.modelData instanceof ModelDataShared && ((ModelDataShared) npcEntity.modelData).entityClass==null) {
			((EntityNPCInterface) npcEntity).animation.activeAnim = this;
			if (((EntityNPCInterface) npcEntity).world==null || ((EntityNPCInterface) npcEntity).world.isRemote) { return; }
			NBTTagCompound compound = this.writeToNBT(new NBTTagCompound());
			compound.setInteger("EntityId", npc.getMCEntity().getEntityId());
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
		AnimationConfig ac = new AnimationConfig(0);
		ac.readFromNBT(this.writeToNBT(new NBTTagCompound()));
		return ac;
	}

}