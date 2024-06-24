package noppes.npcs.client.model.animation;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.api.entity.data.IAnimationFrame;
import noppes.npcs.api.entity.data.IAnimationPart;
import noppes.npcs.constants.EnumParts;

public class AnimationFrameConfig
implements IAnimationFrame {

	public static final AnimationFrameConfig EMPTY_PART = new AnimationFrameConfig();
	public boolean smooth;
	public int speed = 10;
	public int delay = 0;
	public int id = 0;
	
	/* 0:head
	 * 1:left arm
	 * 2:right arm
	 * 3:body
	 * 4:left leg
	 * 5:right leg
	 * 6:left stack
	 * 7:right stack
	 */
	public final Map<Integer, PartConfig> parts = Maps.<Integer, PartConfig>newTreeMap();
	public ResourceLocation sound = null;
	public int emotionId = -1;
	private int version = 1;

	public AnimationFrameConfig() {
		this.parts.clear();
		for (int i = 0; i < 8; i++) {
			PartConfig pc = new PartConfig(i, AnimationFrameConfig.getPartType(i));
			this.parts.put(i, pc);
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
			for (int p = 0; p < 8; p++) {
				if (!this.parts.containsKey(p)) {
					this.parts.put(p, new PartConfig(p, AnimationFrameConfig.getPartType(p)));
				}
			}
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
		if (compound.hasKey("StartSound", 8)) { this.setStartSound(compound.getString("StartSound")); }
		if (compound.hasKey("EmotionID", 3)) { this.setStartEmotion(compound.getInteger("EmotionID")); }
		this.parts.clear();
		for (int i = 0; i < compound.getTagList("PartConfigs", 10).tagCount(); i++) {
			NBTTagCompound nbt = compound.getTagList("PartConfigs", 10).getCompoundTagAt(i);
			PartConfig pc;
			if (nbt.hasKey("Part", 3) && this.parts.containsKey(nbt.getInteger("Part"))) { pc = this.parts.get(nbt.getInteger("Part")); }
			else { pc = new PartConfig(i, AnimationFrameConfig.getPartType(i)); }
			pc.readNBT(nbt);
			if (pc.type == EnumParts.WRIST_RIGHT || pc.type == EnumParts.WRIST_LEFT || pc.type == EnumParts.FOOT_RIGHT || pc.type == EnumParts.FOOT_LEFT) { continue; }
			this.parts.put(pc.id, pc);
if (i == 7) { break; }
		}
		for (int p = 0; p < 8; p++) {
			if (!this.parts.containsKey(p)) {
				this.parts.put(p, new PartConfig(p, AnimationFrameConfig.getPartType(p)));
			}
		}
		fixParts();
	}

	public static EnumParts getPartType(int id) {
		switch(id) {
			case 0: return EnumParts.ARM_LEFT;
			case 1: return EnumParts.ARM_LEFT;
			case 2: return EnumParts.ARM_RIGHT;
			case 3: return EnumParts.BODY;
			case 4: return EnumParts.LEG_LEFT;
			case 5: return EnumParts.LEG_RIGHT;
			case 6: return EnumParts.LEFT_STACK;
			case 7: return EnumParts.RIGHT_STACK;
			default: return EnumParts.CUSTOM;
		}
	}

	public boolean removePart(PartConfig part) {
		if (part == null || this.parts.size() <= 8) {
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
		compound.setString("StartSound", this.getStartSound());
		compound.setInteger("EmotionID", this.emotionId);
		compound.setInteger("Version", this.version);
		return compound;
	}

	@Override
	public String getStartSound() { return this.sound == null ? "" : this.sound.toString(); }

	@Override
	public void setStartSound(String sound) {
		if (sound == null || sound.isEmpty()) { this.sound = null; }
		this.sound = new ResourceLocation(sound);
		if (this.sound.getResourcePath().isEmpty() || this.sound.getResourceDomain().isEmpty()) { this.sound = null; }
	}
	
	public void setStartSound(ResourceLocation resource) { this.sound = resource; }

	@Override
	public int getStartEmotion() { return emotionId; }

	@Override
	public void setStartEmotion(int id) { this.emotionId = id; }

	
}
