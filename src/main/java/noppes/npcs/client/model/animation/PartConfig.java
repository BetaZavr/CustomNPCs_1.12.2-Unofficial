package noppes.npcs.client.model.animation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.LogWriter;
import noppes.npcs.api.entity.data.IAnimationPart;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.util.ValueUtil;

public class PartConfig implements IAnimationPart {

	public float[] rotation = new float[] { 0.5f, 0.5f, 0.5f, 0.5f, 0.5f }; // [x, y, z, x1, y1 ] 0.0 = 0; 1.0 = 360;
	public float[] offset = new float[] { 0.5f, 0.5f, 0.5f }; // [x, y, z] 0.0 = -5; 1.0 = 5
	public float[] scale = new float[] { 0.2f, 0.2f, 0.2f }; // [x, y, z] 0.0 = 0; 1.0 = 5
	public int id = 0;
	public boolean disable = false;
	public boolean show = true;
	public String name;
	protected EnumParts type = EnumParts.HEAD;

	public PartConfig() {
		this.name = "part_" + id;
		this.clear();
	}

	public PartConfig(int id, EnumParts type) {
		this();
		this.id = id;
		this.type = type;
		this.setMainName();
	}

	private void setMainName() {
		if (this.id < 8 && !this.name.startsWith("part_")) { return; }
		switch (this.id) {
			case 0: this.name = "model.head"; break;
			case 1: this.name = "model.larm"; break;
			case 2: this.name = "model.rarm"; break;
			case 3: this.name = "model.body"; break;
			case 4: this.name = "model.lleg"; break;
			case 5: this.name = "model.rleg"; break;
			case 6: this.name = "model.lstack"; break;
			case 7: this.name = "model.rstack"; break;
			default: {
				if (this.name == null || this.name.isEmpty()) { this.name = "part_" + id; }
				break;
			}
		}
	}

	@Override
	public int getType() { return this.type.ordinal(); }
	
	public EnumParts getEnumType() { return this.type; }
	
	@Override
	public void clear() {
		for (int i = 0; i < 3; i++) {
			this.rotation[i] = 0.5f;
			this.offset[i] = 0.5f;
			this.scale[i] = 0.2f;
		}
		this.rotation[3] = 0.5f;
		this.rotation[4] = 0.5f;
	}

	public PartConfig copy() {
		PartConfig pc = new PartConfig();
		pc.readNBT(this.writeNBT());
		return pc;
	}

	@Override
	public float[] getOffset() {
		return new float[] { 10.0f * this.offset[0] - 5.0f, 10.0f * this.offset[1] - 5.0f,
				10.0f * this.offset[2] - 5.0f };
	}

	@Override
	public float[] getRotation() {
		return new float[] { this.rotation[0] * 360.0f, this.rotation[1] * 360.0f, this.rotation[2] * 360.0f };
	}
	
	@Override
	public float[] getRotationPart() {
		return new float[] { this.rotation[3] * 360.0f, this.rotation[4] * 360.0f };
	}

	@Override
	public float[] getScale() {
		return new float[] { this.scale[0] * 5.0f, this.scale[1] * 5.0f, this.scale[2] * 5.0f };
	}

	@Override
	public boolean isDisable() {
		return this.disable;
	}

	@Override
	public boolean isShow() {
		return this.show;
	}

	public void readNBT(NBTTagCompound compound) {
		for (int i = 0; i < 3; i++) {
			try { this.rotation[i] = ValueUtil.correctFloat(compound.getTagList("Rotation", 5).getFloatAt(i), -1.0f, 1.0f); } catch (Exception e) { LogWriter.error("Error:", e); }
			try { this.offset[i] = ValueUtil.correctFloat(compound.getTagList("Offset", 5).getFloatAt(i), -5.0f, 5.0f); } catch (Exception e) { LogWriter.error("Error:", e); }
			try { this.scale[i] = ValueUtil.correctFloat(compound.getTagList("Scale", 5).getFloatAt(i), 0.0f, 5.0f); } catch (Exception e) { LogWriter.error("Error:", e); }
		}
		if (compound.getTagList("Rotation", 5).tagCount() >= 5) {
			try { this.rotation[3] = ValueUtil.correctFloat(compound.getTagList("Rotation", 5).getFloatAt(3), -1.0f, 1.0f); } catch (Exception e) { LogWriter.error("Error:", e); }
			try { this.rotation[4] = ValueUtil.correctFloat(compound.getTagList("Rotation", 5).getFloatAt(4), -1.0f, 1.0f); } catch (Exception e) { LogWriter.error("Error:", e); }
		}
		this.id = compound.getInteger("Part");
		this.disable = compound.getBoolean("Disabled");
		if (compound.hasKey("Show", 1)) {
			this.show = compound.getBoolean("Show");
		}
		if (compound.hasKey("Name", 8)) {
			this.name = compound.getString("Name");
		}
		this.setMainName();
		if (compound.hasKey("Type", 3)) {
			this.type = EnumParts.values()[compound.getInteger("Type")];
		}
	}

	@Override
	public void setDisable(boolean bo) {
		this.disable = bo;
	}

	@Override
	public void setOffset(float x, float y, float z) {
		x %= 5.0f;
		y %= 5.0f;
		z %= 5.0f;
		this.offset[0] = ValueUtil.correctFloat(x / 5.0f, 0.0f, 1.0f);
		this.offset[1] = ValueUtil.correctFloat(y / 5.0f, 0.0f, 1.0f);
		this.offset[2] = ValueUtil.correctFloat(z / 5.0f, 0.0f, 1.0f);
	}

	@Override
	public void setRotation(float x, float y, float z) {
		x %= 360.0f;
		y %= 360.0f;
		z %= 360.0f;
		if (x < 0.0f) { x += 360.0f; }
		if (y < 0.0f) { y += 360.0f; }
		if (z < 0.0f) { z += 360.0f; }
		this.rotation[0] = ValueUtil.correctFloat(x / 360.0f, 0.0f, 1.0f);
		this.rotation[1] = ValueUtil.correctFloat(y / 360.0f, 0.0f, 1.0f);
		this.rotation[2] = ValueUtil.correctFloat(z / 360.0f, 0.0f, 1.0f);
	}

	@Override
	public void setRotation(float x1, float y1) {
		x1 %= 360.0f;
		y1 %= 360.0f;
		if (x1 < 0.0f) { x1 += 360.0f; }
		if (y1 < 0.0f) { y1 += 360.0f; }
		this.rotation[3] = ValueUtil.correctFloat(x1 / 360.0f, 0.0f, 1.0f);
		this.rotation[4] = ValueUtil.correctFloat(y1 / 360.0f, 0.0f, 1.0f);
	}

	@Override
	public void setScale(float x, float y, float z) {
		x %= 5.0f;
		y %= 5.0f;
		z %= 5.0f;
		this.scale[0] = ValueUtil.correctFloat(x / 5.0f, 0.0f, 1.0f);
		this.scale[1] = ValueUtil.correctFloat(y / 5.0f, 0.0f, 1.0f);
		this.scale[2] = ValueUtil.correctFloat(z / 5.0f, 0.0f, 1.0f);
	}

	@Override
	public void setShow(boolean bo) {
		this.show = bo;
	}

	public NBTTagCompound writeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList listRot = new NBTTagList();
		NBTTagList listOff = new NBTTagList();
		NBTTagList listSc = new NBTTagList();
		for (int i = 0; i < 3; i++) {
			listRot.appendTag(new NBTTagFloat(this.rotation[i]));
			listOff.appendTag(new NBTTagFloat(this.offset[i]));
			listSc.appendTag(new NBTTagFloat(this.scale[i]));
		}
		listRot.appendTag(new NBTTagFloat(this.rotation[3]));
		listRot.appendTag(new NBTTagFloat(this.rotation[4]));
		compound.setTag("Rotation", listRot);
		compound.setTag("Offset", listOff);
		compound.setTag("Scale", listSc);
		
		compound.setInteger("Part", this.id);
		compound.setBoolean("Disabled", this.disable);
		compound.setBoolean("Show", this.show);
		compound.setString("Name", this.name);
		compound.setInteger("Type", this.type.ordinal());
		
		return compound;
	}

}
