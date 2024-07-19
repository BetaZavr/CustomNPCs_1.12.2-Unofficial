package noppes.npcs.client.model.animation;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.data.IEmotion;
import noppes.npcs.api.entity.data.IEmotionPart;

public class EmotionConfig
		implements IEmotion {

	public final Map<Integer, EmotionFrame> frames;
	public int id = 0;
	public int repeatLast = 0;
	public String name = "Default Emotion";
	public boolean canBlink = true;

	public boolean immutable = false;

	public EmotionConfig() {
		this.frames = Maps.newTreeMap();
		this.frames.put(0, new EmotionFrame(0));
	}

	public void read(NBTTagCompound nbtEmotion) {
		this.frames.clear();
		for (int i = 0; i < nbtEmotion.getTagList("FrameConfigs", 10).tagCount(); i++) {
			EmotionFrame ef = new EmotionFrame(i);
			ef.readFromNBT(nbtEmotion.getTagList("FrameConfigs", 10).getCompoundTagAt(i));
			ef.id = i;
			this.frames.put(i, ef);
		}
		if (this.frames.isEmpty()) {
			this.frames.put(0, new EmotionFrame(0));
		}
		this.id = nbtEmotion.getInteger("ID");
		this.name = nbtEmotion.getString("Name");
		this.repeatLast = nbtEmotion.getInteger("EmotionRepeat");
		this.canBlink = nbtEmotion.getBoolean("CanBlink");
	}

	public NBTTagCompound save() {
		NBTTagCompound nbtEmotion = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (EmotionFrame ef : this.frames.values()) { list.appendTag(ef.writeToNBT()); }
		nbtEmotion.setTag("FrameConfigs", list);
		nbtEmotion.setInteger("ID", this.id);
		nbtEmotion.setString("Name", this.name);
		nbtEmotion.setInteger("EmotionRepeat", this.repeatLast);
		nbtEmotion.setBoolean("CanBlink", this.canBlink);
		if (this.repeatLast < 0) { this.repeatLast = this.frames.size() - 1; }
		return nbtEmotion;
	}

	public String getName() { return name; }

	public EmotionConfig copy() {
		EmotionConfig ec = new EmotionConfig();
		ec.read(this.save());
		return ec;
	}

	public String getSettingName() {
		String c = "" + ((char) 167);
		return c + "7" + this.id + ": " + c + "r" + this.name;
	}

	@Override
	public int getId() { return this.id; }

	@Override
	public boolean canBlink() { return canBlink; }

	@Override
	public void setCanBlink(boolean bo) { canBlink = bo; }

	@Override
	public IEmotionPart addFrame() {
		int f = this.frames.size();
		this.frames.put(f, new EmotionFrame(f));
		return this.frames.get(f);
	}

	@Override
	public IEmotionPart addFrame(IEmotionPart frame) {
		if (frame == null) { return this.addFrame(); }
		int f = this.frames.size();
		this.frames.put(f, ((EmotionFrame) frame).copy());
		this.frames.get(f).id = f;
		return this.frames.get(f);
	}

	@Override
	public boolean removeFrame(IEmotionPart frame) {
		if (frame == null || this.frames.size() <= 1) {
			return false;
		}
		for (int f : this.frames.keySet()) {
			if (this.frames.get(f).equals(frame)) {
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
		Map<Integer, EmotionFrame> newData = Maps.newTreeMap();
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
			if (newData.isEmpty()) {
				newData.put(0, new EmotionFrame(0));
			}
			this.frames.putAll(newData);
		}
		return isDel;
	}

}
