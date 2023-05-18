package noppes.npcs;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.util.ValueUtil;

public class ModelPartConfig {
	
	public boolean notShared;
	public float scaleX;
	public float scaleY;
	public float scaleZ;
	public float transX;
	public float transY;
	public float transZ;

	public ModelPartConfig() {
		this.scaleX = 1.0f;
		this.scaleY = 1.0f;
		this.scaleZ = 1.0f;
		this.transX = 0.0f;
		this.transY = 0.0f;
		this.transZ = 0.0f;
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
		this.scaleX = config.scaleX;
		this.scaleY = config.scaleY;
		this.scaleZ = config.scaleZ;
		this.transX = config.transX;
		this.transY = config.transY;
		this.transZ = config.transZ;
	}

	public float getScaleX() {
		return this.scaleX;
	}

	public float getScaleY() {
		return this.scaleY;
	}

	public float getScaleZ() {
		return this.scaleZ;
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.scaleX = this.checkValue(compound.getFloat("ScaleX"), 0.5f, 1.5f);
		this.scaleY = this.checkValue(compound.getFloat("ScaleY"), 0.5f, 1.5f);
		this.scaleZ = this.checkValue(compound.getFloat("ScaleZ"), 0.5f, 1.5f);
		this.transX = this.checkValue(compound.getFloat("TransX"), -1.0f, 1.0f);
		this.transY = this.checkValue(compound.getFloat("TransY"), -1.0f, 1.0f);
		this.transZ = this.checkValue(compound.getFloat("TransZ"), -1.0f, 1.0f);
		this.notShared = compound.getBoolean("NotShared");
	}

	public void setScale(float x, float y) {
		this.scaleX = x;
		this.scaleZ = x;
		this.scaleY = y;
	}

	public void setScale(float x, float y, float z) {
		this.scaleX = ValueUtil.correctFloat(x, 0.5f, 1.5f);
		this.scaleY = ValueUtil.correctFloat(y, 0.5f, 1.5f);
		this.scaleZ = ValueUtil.correctFloat(z, 0.5f, 1.5f);
	}

	public void setTranslate(float transX, float transY, float transZ) {
		this.transX = transX;
		this.transY = transY;
		this.transZ = transZ;
	}

	@Override
	public String toString() {
		return "ScaleX: " + this.scaleX + " - ScaleY: " + this.scaleY + " - ScaleZ: " + this.scaleZ;
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setFloat("ScaleX", this.scaleX);
		compound.setFloat("ScaleY", this.scaleY);
		compound.setFloat("ScaleZ", this.scaleZ);
		compound.setFloat("TransX", this.transX);
		compound.setFloat("TransY", this.transY);
		compound.setFloat("TransZ", this.transZ);
		compound.setBoolean("NotShared", this.notShared);
		return compound;
	}
}
