package noppes.npcs;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.util.ValueUtil;

public class ModelPartConfig {

	public boolean notShared = false;
	public final float[] scale = new float[] { 1.0f, 1.0f, 1.0f };
	public final float[] offset = new float[] { 0.0f, 0.0f, 0.0f };

	public void copyValues(ModelPartConfig config) {
		for (int i = 0; i < 3; i++) {
			scale[i] = config.scale[i];
			offset[i] = config.offset[i];
		}
	}

	public void load(NBTTagCompound compound) {
		scale[0] = ValueUtil.correctFloat(compound.getFloat("ScaleX"), 0.5f, 1.5f);
		scale[1] = ValueUtil.correctFloat(compound.getFloat("ScaleY"), 0.5f, 1.5f);
		scale[2] = ValueUtil.correctFloat(compound.getFloat("ScaleZ"), 0.5f, 1.5f);
		offset[0] = ValueUtil.correctFloat(compound.getFloat("TransX"), -1.0f, 1.0f);
		offset[1] = ValueUtil.correctFloat(compound.getFloat("TransY"), -1.0f, 1.0f);
		offset[2] = ValueUtil.correctFloat(compound.getFloat("TransZ"), -1.0f, 1.0f);
		notShared = compound.getBoolean("NotShared");
	}

	public void setScale(float x, float y) {
		scale[0] = ValueUtil.correctFloat(x, 0.5f, 1.5f);
		scale[1] = ValueUtil.correctFloat(x, 0.5f, 1.5f);
		scale[2] = ValueUtil.correctFloat(y, 0.5f, 1.5f);
	}

	public void setScale(float x, float y, float z) {
		scale[0] = ValueUtil.correctFloat(x, 0.5f, 1.5f);
		scale[1] = ValueUtil.correctFloat(y, 0.5f, 1.5f);
		scale[2] = ValueUtil.correctFloat(z, 0.5f, 1.5f);
	}

	public void setTranslate(float transX, float transY, float transZ) {
		offset[0] = ValueUtil.correctFloat(transX, -1.0f, 1.0f);
		offset[1] = ValueUtil.correctFloat(transY, -1.0f, 1.0f);
		offset[2] = ValueUtil.correctFloat(transZ, -1.0f, 1.0f);
	}

	public NBTTagCompound save() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setFloat("ScaleX", scale[0]);
		compound.setFloat("ScaleY", scale[1]);
		compound.setFloat("ScaleZ", scale[2]);
		compound.setFloat("TransX", offset[0]);
		compound.setFloat("TransY", offset[1]);
		compound.setFloat("TransZ", offset[2]);
		compound.setBoolean("NotShared", notShared);
		return compound;
	}

}
