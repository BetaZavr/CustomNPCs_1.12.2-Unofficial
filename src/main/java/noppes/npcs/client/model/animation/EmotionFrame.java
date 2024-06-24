package noppes.npcs.client.model.animation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.entity.data.IEmotionPart;
import noppes.npcs.entity.EntityNPCInterface;

public class EmotionFrame
implements IEmotionPart {

	public static final EmotionFrame EMPTY_PART = new EmotionFrame(0);
	public int id = 0;
	public int speed = 20;
	public int delay = 0;
	public boolean smooth = true;
	public boolean disable = false;
	public boolean blink = false;
	public boolean endBlink = false;
	public float[] offsetEye = new float[] { 0.5f, 0.5f, 0.5f, 0.5f }; // [rightX, rightY, leftX, leftY]
	public float[] rotEye = new float[] { 0.5f, 0.5f }; // [right, left]
	public float[] scaleEye = new float[] { 0.5f, 0.5f, 0.5f, 0.5f }; // [rightX, rightY, leftX, leftY]
	
	public float[] offsetPupil = new float[] { 0.5f, 0.5f, 0.5f, 0.5f };
	public float[] rotPupil = new float[] { 0.5f, 0.5f };
	public float[] scalePupil = new float[] { 0.5f, 0.5f, 0.5f, 0.5f };
	
	public float[] offsetBrow = new float[] { 0.5f, 0.5f, 0.5f, 0.5f };
	public float[] rotBrow = new float[] { 0.5f, 0.5f };
	public float[] scaleBrow = new float[] { 0.5f, 0.5f, 0.5f, 0.5f };

	public float[] offsetMouth = new float[] { 0.5f, 0.5f};
	public float rotMouth = 0.5f;
	public float[] scaleMouth = new float[] { 0.5f, 0.5f };
	public boolean rndMouth = false;
	public boolean showMouth = false;

	public EntityNPCInterface npc;
	
	public EmotionFrame(int id) { this.id = id; }
	
	public EmotionFrame(EntityNPCInterface npc) {
		this.npc = npc;
	}

	@Override
	public boolean isBlink() { return blink; }
	
	@Override
	public boolean isEndBlink() { return endBlink; }

	@Override
	public void setBlink(boolean bo) { this.blink = bo; }
	@Override
	public void setEndBlink(boolean bo) { this.endBlink = bo; }
	
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.getKeySet().size() == 0) { return; }
		this.id = compound.getInteger("Part");
		this.setSpeed(compound.getInteger("Speed"));
		this.setEndDelay(compound.getInteger("EndDelay"));
		this.smooth = compound.getBoolean("IsSmooth");
		this.disable = compound.getBoolean("IsDisable");
		this.blink = compound.getBoolean("IsBlink");
		this.endBlink = compound.getBoolean("IsEndBlink");
		this.rndMouth = compound.getBoolean("IsRandomMouth");
		this.showMouth = compound.getBoolean("ShowMouth");
		
		this.rotMouth = compound.getFloat("RotationMouth");
		NBTTagList listRotEye = compound.getTagList("RotationEye", 5);
		NBTTagList listOffEye = compound.getTagList("OffsetEye", 5);
		NBTTagList listScEye = compound.getTagList("ScaleEye", 5);
		NBTTagList listRotBrow = compound.getTagList("RotationBrow", 5);
		NBTTagList listOffBrow = compound.getTagList("OffsetBrow", 5);
		NBTTagList listScBrow = compound.getTagList("ScaleBrow", 5);
		NBTTagList listRotPupil = compound.getTagList("RotationPupil", 5);
		NBTTagList listOffPupil = compound.getTagList("OffsetPupil", 5);
		NBTTagList listScPupil = compound.getTagList("ScalePupil", 5);
		NBTTagList listOffMouth = compound.getTagList("OffsetMouth", 5);
		NBTTagList listScMouth = compound.getTagList("ScaleMouth", 5);
		
		int max = listRotEye.tagCount();
		if (max < listOffEye.tagCount()) { max = listOffEye.tagCount(); }
		if (max < listScEye.tagCount()) { max = listScEye.tagCount(); }
		if (max < this.rotEye.length) { max = this.rotEye.length; }
		if (max < this.offsetEye.length) { max = this.offsetEye.length; }
		if (max < this.scaleEye.length) { max = this.scaleEye.length; }
		for (int i = 0; i < max; i++) {
			if (i < this.rotEye.length && i < listRotEye.tagCount()) { this.rotEye[i] = listRotEye.getFloatAt(i); }
			if (i < this.offsetEye.length && i < listOffEye.tagCount()) { this.offsetEye[i] = listOffEye.getFloatAt(i); }
			if (i < this.scaleEye.length && i < listScEye.tagCount()) { this.scaleEye[i] = listScEye.getFloatAt(i); }
			if (i < this.rotBrow.length && i < listRotBrow.tagCount()) { this.rotBrow[i] = listRotBrow.getFloatAt(i); }
			if (i < this.offsetBrow.length && i < listOffBrow.tagCount()) { this.offsetBrow[i] = listOffBrow.getFloatAt(i); }
			if (i < this.scaleBrow.length && i < listScBrow.tagCount()) { this.scaleBrow[i] = listScBrow.getFloatAt(i); }
			if (i < this.rotPupil.length && i < listRotPupil.tagCount()) { this.rotPupil[i] = listRotPupil.getFloatAt(i); }
			if (i < this.offsetPupil.length && i < listOffPupil.tagCount()) { this.offsetPupil[i] = listOffPupil.getFloatAt(i); }
			if (i < this.scalePupil.length && i < listScPupil.tagCount()) { this.scalePupil[i] = listScPupil.getFloatAt(i); }
			if (i < this.offsetMouth.length && i < listOffMouth.tagCount()) { this.offsetMouth[i] = listOffMouth.getFloatAt(i); }
			if (i < this.scaleMouth.length && i < listScMouth.tagCount()) { this.scaleMouth[i] = listScMouth.getFloatAt(i); }
		}
		
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("Part", this.id);
		compound.setInteger("Speed", this.speed);
		compound.setInteger("EndDelay", this.delay);
		compound.setBoolean("IsSmooth", this.smooth);
		compound.setBoolean("IsDisable", this.disable);
		compound.setBoolean("IsBlink", this.blink);
		compound.setBoolean("IsEndBlink", this.endBlink);
		compound.setBoolean("IsRandomMouth", this.rndMouth);
		compound.setBoolean("ShowMouth", this.showMouth);

		compound.setFloat("RotationMouth", this.rotMouth);
		NBTTagList listRotEye = new NBTTagList();
		NBTTagList listOffEye = new NBTTagList();
		NBTTagList listScEye = new NBTTagList();
		NBTTagList listRotBrow = new NBTTagList();
		NBTTagList listOffBrow = new NBTTagList();
		NBTTagList listScBrow = new NBTTagList();
		NBTTagList listRotPupil = new NBTTagList();
		NBTTagList listOffPupil = new NBTTagList();
		NBTTagList listScPupil = new NBTTagList();
		NBTTagList listRotMouth = new NBTTagList();
		NBTTagList listOffMouth = new NBTTagList();
		NBTTagList listScMouth = new NBTTagList();
		int max = this.rotEye.length;
		if (max < this.offsetEye.length) { max = this.offsetEye.length; }
		if (max < this.scaleEye.length) { max = this.scaleEye.length; }
		for (int i = 0; i < max; i++) {
			if (i < this.rotEye.length) { listRotEye.appendTag(new NBTTagFloat(this.rotEye[i])); }
			if (i < this.offsetEye.length) { listOffEye.appendTag(new NBTTagFloat(this.offsetEye[i])); }
			if (i < this.scaleEye.length) { listScEye.appendTag(new NBTTagFloat(this.scaleEye[i])); }
			if (i < this.rotBrow.length) { listRotBrow.appendTag(new NBTTagFloat(this.rotBrow[i])); }
			if (i < this.offsetBrow.length) { listOffBrow.appendTag(new NBTTagFloat(this.offsetBrow[i])); }
			if (i < this.scaleBrow.length) { listScBrow.appendTag(new NBTTagFloat(this.scaleBrow[i])); }
			if (i < this.rotPupil.length) { listRotPupil.appendTag(new NBTTagFloat(this.rotPupil[i])); }
			if (i < this.offsetPupil.length) { listOffPupil.appendTag(new NBTTagFloat(this.offsetPupil[i])); }
			if (i < this.scalePupil.length) { listScPupil.appendTag(new NBTTagFloat(this.scalePupil[i])); }
			if (i < this.offsetMouth.length) { listOffMouth.appendTag(new NBTTagFloat(this.offsetMouth[i])); }
			if (i < this.scaleMouth.length) { listScMouth.appendTag(new NBTTagFloat(this.scaleMouth[i])); }
		}
		compound.setTag("RotationEye", listRotEye);
		compound.setTag("OffsetEye", listOffEye);
		compound.setTag("ScaleEye", listScEye);
		compound.setTag("RotationBrow", listRotBrow);
		compound.setTag("OffsetBrow", listOffBrow);
		compound.setTag("ScaleBrow", listScBrow);
		compound.setTag("RotationPupil", listRotPupil);
		compound.setTag("OffsetPupil", listOffPupil);
		compound.setTag("ScalePupil", listScPupil);
		compound.setTag("RotationMouth", listRotMouth);
		compound.setTag("OffsetMouth", listOffMouth);
		compound.setTag("ScaleMouth", listScMouth);
		
		return compound;
	}

	@Override
	public int getSpeed() { return speed; }

	@Override
	public int getEndDelay() { return this.delay; }

	@Override
	public boolean isSmooth() { return this.smooth; }

	@Override
	public void setEndDelay(int ticks) {
		if (ticks < 0) { ticks *= -1; }
		if (ticks > 1200) { ticks = 1200; }
		this.delay = ticks;
	}

	@Override
	public void setSmooth(boolean isSmooth) { this.smooth = isSmooth; }

	@Override
	public void setSpeed(int ticks) {
		if (ticks < 0) { ticks *= -1; }
		if (ticks > 1200) { ticks = 1200; }
		this.speed = ticks;
	}

	@Override
	public boolean isDisabled() { return disable; }

	@Override
	public void setDisable(boolean bo) { this.disable = bo; }

	public boolean startBlink() { return blink; }

	public EmotionFrame copy() {
		EmotionFrame newEf = new EmotionFrame(0);
		newEf.readFromNBT(this.writeToNBT());
		return newEf;
	}

	public void clear() {
		this.speed = 20;
		this.delay = 0;
		this.smooth = true;
		this.disable = false;
		this.blink = false;
		this.endBlink = false;
		this.offsetEye = new float[] { 0.5f, 0.5f, 0.5f, 0.5f };
		this.rotEye = new float[] { 0.5f, 0.5f };
		this.scaleEye = new float[] { 0.5f, 0.5f, 0.5f, 0.5f };
		this.offsetPupil = new float[] { 0.5f, 0.5f, 0.5f, 0.5f };
		this.rotPupil = new float[] { 0.5f, 0.5f };
		this.scalePupil = new float[] { 0.5f, 0.5f, 0.5f, 0.5f };
		this.offsetBrow = new float[] { 0.5f, 0.5f, 0.5f, 0.5f };
		this.rotBrow = new float[] { 0.5f, 0.5f };
		this.scaleBrow = new float[] { 0.5f, 0.5f, 0.5f, 0.5f };
		this.offsetMouth = new float[] { 0.5f, 0.5f};
		this.rotMouth = 0.5f;
		this.scaleMouth = new float[] { 0.5f, 0.5f };
		this.rndMouth = false;
		this.showMouth = false;
	}
	
}
