package noppes.npcs.client.model.part;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.util.ValueUtil;

public class ModelPartConfig {
	
	public boolean notShared;
	public float[] scaleBase, scaleAnimation;
	public float[] offsetBase, offsetAnimation;
	public float[] rotateAnimation;
	public float scaleY;

	public ModelPartConfig() {
		this.scaleBase = new float[] { 1.0f, 1.0f, 1.0f };
		this.scaleAnimation = new float[] { 1.0f, 1.0f, 1.0f };
		this.offsetBase = new float[] { 0.0f, 0.0f, 0.0f };
		this.offsetAnimation = new float[] { 0.0f, 0.0f, 0.0f };
		this.rotateAnimation = new float[] { 0.0f, 0.0f, 0.0f };
		this.notShared = false;
	}

	public float checkValue(float given, float min, float max) {
		if (given < min) { return min; }
		if (given > max) { return max; }
		return given;
	}

	public void copyValues(ModelPartConfig config) {
		for (int i=0; i<3; i++) {
			this.scaleBase[i] = config.scaleBase[i];
			this.offsetBase[i] = config.offsetBase[i];
		}
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.scaleBase[0] = this.checkValue(compound.getFloat("ScaleX"), 0.5f, 1.5f);
		this.scaleBase[1] = this.checkValue(compound.getFloat("ScaleY"), 0.5f, 1.5f);
		this.scaleBase[2] = this.checkValue(compound.getFloat("ScaleZ"), 0.5f, 1.5f);
		this.offsetBase[0] = this.checkValue(compound.getFloat("TransX"), -1.0f, 1.0f);
		this.offsetBase[1] = this.checkValue(compound.getFloat("TransY"), -1.0f, 1.0f);
		this.offsetBase[2] = this.checkValue(compound.getFloat("TransZ"), -1.0f, 1.0f);
		this.notShared = compound.getBoolean("NotShared");
	}

	public void setScale(float x, float y) {
		this.scaleBase[0] = x;
		this.scaleBase[1] = x;
		this.scaleBase[2] = y;
	}

	public void setScale(float x, float y, float z) {
		this.scaleBase[0] = ValueUtil.correctFloat(x, 0.5f, 1.5f);
		this.scaleBase[1] = ValueUtil.correctFloat(y, 0.5f, 1.5f);
		this.scaleBase[2] = ValueUtil.correctFloat(z, 0.5f, 1.5f);
	}

	public void setTranslate(float transX, float transY, float transZ) {
		this.offsetBase[0] = transX;
		this.offsetBase[1] = transY;
		this.offsetBase[2] = transZ;
	}

	@Override
	public String toString() {
		return "ScaleXYZ: " + this.scaleBase[0] + ", " + this.scaleBase[1] + ", " + this.scaleBase[2]+"]";
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setFloat("ScaleX", this.scaleBase[0]);
		compound.setFloat("ScaleY", this.scaleBase[1]);
		compound.setFloat("ScaleZ", this.scaleBase[2]);
		compound.setFloat("TransX", this.offsetBase[0]);
		compound.setFloat("TransY", this.offsetBase[1]);
		compound.setFloat("TransZ", this.offsetBase[2]);
		compound.setBoolean("NotShared", this.notShared);
		return compound;
	}
}
