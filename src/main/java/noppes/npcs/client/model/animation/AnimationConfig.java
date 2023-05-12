package noppes.npcs.client.model.animation;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.data.role.IJobPuppet;
import noppes.npcs.constants.EnumAnimationType;
import noppes.npcs.constants.EnumNpcJob;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class AnimationConfig
implements IJobPuppet {

	public int startTick, prevTicks, speed;
	public float val, valNext;
	public EnumAnimationType type;
	public Map<Integer, PartConfig> data;
	private EntityNPCInterface npc;

	public AnimationConfig(EntityNPCInterface npc, int type) {
		this.npc = npc;
		this.data = Maps.<Integer, PartConfig>newTreeMap();
		for (int i=0; i<6; i++) { this.data.put(i, new PartConfig(npc)); }
		if (type<0) {type *= -1; }
		type %= EnumAnimationType.values().length;
		this.type = EnumAnimationType.values()[type];
		this.prevTicks = 0;
		this.startTick = 0;
		this.val = 0.0f;
		this.valNext = 0.0f;
		this.speed = 40;
	}

	@Override
	public IJobPuppetPart getPart(int part) {
		if (!this.data.containsKey(part)) {
			throw new CustomNPCsException("Unknown part " + part, new Object[0]);
		}
		return this.data.get(part);
	}

	@Override
	public int getType() { return EnumNpcJob.values().length; }

	public void reset() {
		this.val = 0.0f;
		this.valNext = 0.0f;
		this.prevTicks = 0;
		this.startTick = this.npc.ticksExisted;
	}

	private float calcRotation(float r, float r2, float partialTicks) {
		if (this.prevTicks != this.npc.ticksExisted) {
			int ticks = this.npc.ticksExisted - this.startTick;
			this.val = 1.0f - (MathHelper.cos(ticks / this.speed * (float) Math.PI / 2.0f) + 1.0f) / 2.0f;
			this.valNext = 1.0f - (MathHelper.cos((ticks + 1) / this.speed * (float) Math.PI / 2.0f) + 1.0f) / 2.0f;
			this.prevTicks = this.npc.ticksExisted;
		}
		float f = this.val + (this.valNext - this.val) * partialTicks;
		return r + (r2 - r) * f;
	}

	public int getAnimationSpeed() { return (int) this.speed; }

	public float getRotation(PartConfig part1, PartConfig part2, int axis, float partialTicks) {
		return this.calcRotation(part1.rotation[axis], part2.rotation[axis], partialTicks);
	}
	public void setAnimationSpeed(int speed) {
		this.speed = ValueUtil.correctInt(speed, 0, 600);
		this.npc.updateClient = true;
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.data.clear();
		for (int i=0; i<6; i++) {
			PartConfig pc = new PartConfig(npc);
			pc.readNBT(compound.getTagList("PartConfigs", 10).getCompoundTagAt(i));
			this.data.put(i, pc);
		}
		int t = compound.getInteger("Type");
		if (t<0) { t *= -1; }
		this.type = EnumAnimationType.values()[t % EnumAnimationType.values().length];
		this.speed = compound.getInteger("TickSpeed");
		if (this.speed<0) { this.speed *= -1; }
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for (PartConfig pc : this.data.values()) { list.appendTag(pc.writeNBT()); }
		compound.setTag("PartConfigs", list);
		compound.setInteger("Type", this.type.ordinal());
		compound.setInteger("TickSpeed", this.speed);
		return compound;
	}
	
}