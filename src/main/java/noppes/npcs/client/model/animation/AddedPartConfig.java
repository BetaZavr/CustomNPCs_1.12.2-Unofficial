package noppes.npcs.client.model.animation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.util.ValueUtil;

public class AddedPartConfig extends PartConfig {

	public ResourceLocation location;
	public ResourceLocation obj;
	public int parentPart;
	
	public int textureU, textureV;
	public boolean isNormal;
	public final float[] pos = new float[] { 0.0f, 0.0f, 0.0f }; // start pos [x, y, z]
	public final float[] rot = new float[] { 0.0f, 0.0f, 0.0f }; // start rot [x, y, z]
	public final float[] size = new float[] { 4.0f, 5.5f, 3.5f, 3.0f, 4.0f }; // end pos [dx, dy0, dy1, dy2, dz]

	public AddedPartConfig(int id) {
		super(id, EnumParts.CUSTOM);
	}

	@Override
	public void clear() {
		super.clear();
		location = new ResourceLocation(CustomNpcs.MODID, "textures/gui/animation/default_part.png");
		obj = null;
		parentPart = -1;
	}

	public void readNBT(NBTTagCompound compound) {
		super.readNBT(compound);
		this.location = new ResourceLocation(compound.getString("Location"));
		this.parentPart = compound.getInteger("ParentPart");
		if (compound.hasKey("OBJLocation", 8)) {
			this.obj = new ResourceLocation(compound.getString("LOBJLocationocation"));
		}
		for (int i = 0; i < 3; i++) {
			try { this.pos[i] = ValueUtil.correctFloat(compound.getTagList("BasePosition", 5).getFloatAt(i), -1.0f, 1.0f); } catch (Exception e) { LogWriter.error("Error:", e); }
			try { this.rot[i] = ValueUtil.correctFloat(compound.getTagList("BaseRotation", 5).getFloatAt(i), -5.0f, 5.0f); } catch (Exception e) { LogWriter.error("Error:", e); }
		}
		for (int i = 0; i < 5; i++) {
			try { this.size[i] = ValueUtil.correctFloat(compound.getTagList("BaseSize", 5).getFloatAt(i), -1.0f, 1.0f); } catch (Exception e) { LogWriter.error("Error:", e); }
		}
	}

	public NBTTagCompound writeNBT() {
		NBTTagCompound compound = super.writeNBT();
		compound.setString("Location", this.location.toString());
		compound.setInteger("ParentPart", this.parentPart);
		if (this.obj != null) {
			compound.setString("OBJLocation", this.obj.toString());
		}
		NBTTagList listPos = new NBTTagList();
		NBTTagList listRot = new NBTTagList();
		for (int i = 0; i < 3; i++) {
			listPos.appendTag(new NBTTagFloat(this.pos[i]));
			listRot.appendTag(new NBTTagFloat(this.rot[i]));
		}
		compound.setTag("BasePosition", listPos);
		compound.setTag("BaseRotation", listRot);

		NBTTagList listSize = new NBTTagList();
		for (int i = 0; i < 5; i++) {
			listSize.appendTag(new NBTTagFloat(this.size[i]));
		}
		compound.setTag("BaseSize", listSize);
		return compound;
	}
}
