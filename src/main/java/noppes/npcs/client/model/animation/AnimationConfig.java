package noppes.npcs.client.model.animation;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.data.role.IJobPuppet;
import noppes.npcs.constants.EnumAnimationType;
import noppes.npcs.constants.EnumNpcJob;
import noppes.npcs.entity.EntityNPCInterface;

public class AnimationConfig
implements IJobPuppet {

	public int startTick, prevTicks, step;
	public float val, valNext;
	public EnumAnimationType type;
	public Map<Integer, PartConfig[]> data; // 0:head, 1:left arm, 2:right arm, 3:body, 4:left leg, 5:right leg
	private EntityNPCInterface npc;

	public AnimationConfig(EntityNPCInterface npc, int type) {
		this.npc = npc;
		this.data = Maps.<Integer, PartConfig[]>newTreeMap();
		for (int i=0; i<6; i++) { this.data.put(i, new PartConfig[] { new PartConfig(npc) }); }
		if (type<0) {type *= -1; }
		type %= EnumAnimationType.values().length;
		this.type = EnumAnimationType.values()[type];
		this.prevTicks = 0;
		this.startTick = 0;
		this.val = 0.0f;
		this.valNext = 0.0f;
		this.step = 0;
	}
	
	public PartConfig[] getParts(int part) { return this.data.get(part); }
	
	@Override
	public IJobPuppetPart getPart(int part, int step) {
		if (!this.data.containsKey(part)) {
			throw new CustomNPCsException("Unknown part " + part);
		}
		PartConfig[] pcs = this.data.get(part);
		if (step<0 || step>=pcs.length) {
			throw new CustomNPCsException("Unknown step " + step + " in part "+ part);
		}
		return pcs[step];
	}

	@Override
	public int getType() { return EnumNpcJob.values().length; }

	public void reset() {
		this.val = 0.0f;
		this.valNext = 0.0f;
		this.prevTicks = 0;
		this.step = 0;
		this.startTick = this.npc.ticksExisted;
	}

	private float calcRotation(float r, float r2, float speed, float partialTicks) {
		if (this.prevTicks != this.npc.ticksExisted) {
			int ticks = this.npc.ticksExisted - this.startTick;
			this.val = 1.0f - (MathHelper.cos(ticks / speed * (float) Math.PI / 2.0f) + 1.0f) / 2.0f;
			this.valNext = 1.0f - (MathHelper.cos((ticks + 1) / speed * (float) Math.PI / 2.0f) + 1.0f) / 2.0f;
			this.prevTicks = this.npc.ticksExisted;
		}
		float f = this.val + (this.valNext - this.val) * partialTicks;
		return r + (r2 - r) * f;
	}

	public float getRotation(PartConfig[] pcs, int axis, float partialTicks) {
		if (pcs==null) { return 0; }
		PartConfig part1 = pcs[this.step];
		PartConfig part2;
		if (this.step+1>=pcs.length) { part2 = pcs[this.step + 1]; }
		else { part2 = pcs[0]; }
		return this.calcRotation(part1.rotation[axis], part2.rotation[axis], part2.speed, partialTicks);
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.data.clear();
		for (int i=0; i<6 && i<compound.getTagList("PartSettings", 10).tagCount(); i++) {
			NBTTagCompound nbtPart = compound.getTagList("PartSettings", 10).getCompoundTagAt(i);
			List<PartConfig> pcs = Lists.<PartConfig>newArrayList();
			for (int j=0; j<nbtPart.getTagList("PartConfigs", 10).tagCount(); j++) {
				PartConfig pc = new PartConfig(this.npc);
				pc.readNBT(nbtPart.getTagList("PartConfigs", 10).getCompoundTagAt(i));
				pcs.add(pc);
			}
			this.data.put(nbtPart.getInteger("Part"), pcs.toArray(new PartConfig[pcs.size()]));
		}
		int t = compound.getInteger("Type");
		if (t<0) { t *= -1; }
		this.type = EnumAnimationType.values()[t % EnumAnimationType.values().length];
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for (int part : this.data.keySet()) {
			NBTTagCompound nbtPart = new NBTTagCompound();
			nbtPart.setInteger("Part", part);
			NBTTagList pcs = new NBTTagList();
			for (PartConfig pc : this.data.get(part)) {
				pcs.appendTag(pc.writeNBT());
			}
			nbtPart.setTag("PartConfigs", pcs);
			list.appendTag(nbtPart);
		}
		compound.setTag("PartSettings", list);
		compound.setInteger("Type", this.type.ordinal());
		return compound;
	}
	
}