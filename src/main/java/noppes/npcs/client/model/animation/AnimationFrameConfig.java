package noppes.npcs.client.model.animation;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.entity.data.IAnimationFrame;
import noppes.npcs.api.entity.data.IAnimationPart;

public class AnimationFrameConfig implements IAnimationFrame {

	public static final AnimationFrameConfig EMPTY_PART = new AnimationFrameConfig();
	public boolean smooth;
	public int speed, delay;
	public final Map<Integer, PartConfig> parts; // 0:head, 1:left arm, 2:right arm, 3:body, 4:left leg, 5:right leg
	public int id;

	public AnimationFrameConfig() {
		this.parts = Maps.<Integer, PartConfig>newTreeMap();
		for (int i = 0; i < 6; i++) {
			this.parts.put(i, new PartConfig(i));
		}
		this.id = 0;
		this.clear();
	}

	public void clear() {
		this.smooth = false;
		this.speed = 10;
		this.delay = 0;
	}

	public AnimationFrameConfig copy() {
		AnimationFrameConfig newAfc = new AnimationFrameConfig();
		newAfc.readNBT(this.writeNBT());
		return newAfc;
	}

	private void fixParts() {
		int i = 0;
		Map<Integer, PartConfig> newParts = Maps.<Integer, PartConfig>newTreeMap();
		boolean change = false;
		for (Integer id : this.parts.keySet()) {
			PartConfig ps = this.parts.get(id);
			if (id != i || ps.id != i) {
				change = true;
			}
			ps.id = i;
			newParts.put(i, ps);
			i++;
		}
		if (change) {
			this.parts.clear();
			this.parts.putAll(newParts);
		}
	}

	@Override
	public int getEndDelay() {
		if (this.delay < 0) {
			this.delay *= -1;
		}
		if (this.delay > 1200) {
			this.delay = -1200;
		}
		return this.delay;
	}

	@Override
	public IAnimationPart getPart(int id) {
		if (id < 0) {
			id *= -1;
		}
		if (id > this.parts.size()) {
			id %= this.parts.size();
		}
		return this.parts.get(id);
	}

	@Override
	public int getSpeed() {
		if (this.speed < 0) {
			this.speed *= -1;
		}
		if (this.speed > 1200) {
			this.speed = 1200;
		}
		return this.speed;
	}

	@Override
	public boolean isSmooth() {
		return this.smooth;
	}

	public void readNBT(NBTTagCompound compound) {
		this.id = compound.getInteger("ID");
		this.setSmooth(compound.getBoolean("IsSmooth"));
		this.setSpeed(compound.getInteger("Speed"));
		this.setEndDelay(compound.getInteger("EndDelay"));
		this.parts.clear();
		for (int i = 0; i < compound.getTagList("PartConfigs", 10).tagCount(); i++) {
			NBTTagCompound nbt = compound.getTagList("PartConfigs", 10).getCompoundTagAt(i);
			PartConfig pc;
			if (nbt.hasKey("", 8)) {
				pc = new AddedPartConfig(i);
			} else {
				pc = new PartConfig(i);
			}
			pc.readNBT(nbt);
			this.parts.put(pc.id, pc);
		}
		fixParts();
	}

	public boolean removePart(PartConfig part) {
		if (part == null || this.parts.size() <= 6) {
			return false;
		}
		for (Integer id : this.parts.keySet()) {
			PartConfig p = this.parts.get(id);
			if (p.equals(part) || p.id == part.id) {
				this.parts.remove(id);
				fixParts();
				return true;
			}
		}
		return false;
	}

	@Override
	public void setEndDelay(int ticks) {
		if (ticks < 0) {
			ticks *= -1;
		}
		if (ticks > 1200) {
			ticks = 1200;
		}
		this.delay = ticks;
	}

	@Override
	public void setSmooth(boolean isSmooth) {
		this.smooth = isSmooth;
	}

	@Override
	public void setSpeed(int ticks) {
		if (ticks < 0) {
			ticks *= -1;
		}
		if (ticks > 1200) {
			ticks = 1200;
		}
		this.speed = ticks;
	}

	public NBTTagCompound writeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("IsSmooth", this.smooth);
		compound.setInteger("ID", this.id);
		compound.setInteger("Speed", this.speed);
		compound.setInteger("EndDelay", this.delay);
		NBTTagList list = new NBTTagList();
		for (int id : this.parts.keySet()) {
			list.appendTag(this.parts.get(id).writeNBT());
		}
		compound.setTag("PartConfigs", list);

		return compound;
	}

}
