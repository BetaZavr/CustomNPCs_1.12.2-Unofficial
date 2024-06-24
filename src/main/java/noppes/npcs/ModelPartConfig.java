package noppes.npcs;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.util.ValueUtil;

public class ModelPartConfig {

	public boolean notShared;
	public float[] scale;
	public float[] offset;
	public float scaleY;

	public ModelPartConfig() {
		this.scale = new float[] { 1.0f, 1.0f, 1.0f };
		this.offset = new float[] { 0.0f, 0.0f, 0.0f };
		this.notShared = false;
	}

	public float checkValue(float given, float min, float max) {
		if (given < min) {
			return min;
		}
		if (given > max) {
			return max;
		}
		return given;
	}

	public void copyValues(ModelPartConfig config) {
		for (int i = 0; i < 3; i++) {
			this.scale[i] = config.scale[i];
			this.offset[i] = config.offset[i];
		}
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.scale[0] = this.checkValue(compound.getFloat("ScaleX"), 0.5f, 1.5f);
		this.scale[1] = this.checkValue(compound.getFloat("ScaleY"), 0.5f, 1.5f);
		this.scale[2] = this.checkValue(compound.getFloat("ScaleZ"), 0.5f, 1.5f);
		this.offset[0] = this.checkValue(compound.getFloat("TransX"), -1.0f, 1.0f);
		this.offset[1] = this.checkValue(compound.getFloat("TransY"), -1.0f, 1.0f);
		this.offset[2] = this.checkValue(compound.getFloat("TransZ"), -1.0f, 1.0f);
		this.notShared = compound.getBoolean("NotShared");
	}

	public void setScale(float x, float y) {
		this.scale[0] = x;
		this.scale[1] = x;
		this.scale[2] = y;
	}

	public void setScale(float x, float y, float z) {
		this.scale[0] = ValueUtil.correctFloat(x, 0.5f, 1.5f);
		this.scale[1] = ValueUtil.correctFloat(y, 0.5f, 1.5f);
		this.scale[2] = ValueUtil.correctFloat(z, 0.5f, 1.5f);
	}

	public void setTranslate(float transX, float transY, float transZ) {
		this.offset[0] = transX;
		this.offset[1] = transY;
		this.offset[2] = transZ;
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setFloat("ScaleX", this.scale[0]);
		compound.setFloat("ScaleY", this.scale[1]);
		compound.setFloat("ScaleZ", this.scale[2]);
		compound.setFloat("TransX", this.offset[0]);
		compound.setFloat("TransY", this.offset[1]);
		compound.setFloat("TransZ", this.offset[2]);
		compound.setBoolean("NotShared", this.notShared);
		return compound;
	}
}
