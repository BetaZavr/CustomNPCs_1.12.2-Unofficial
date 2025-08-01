package noppes.npcs.client.model.animation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.util.ValueUtil;

public class AddedPartConfig {

	public ResourceLocation location;
	public ResourceLocation objUp;
	public ResourceLocation objDown;
	public int parentPart = -1;
	public int id;
	public int textureU = 40;
	public int textureV = 16;
	public boolean isNormal;
	public final float[] pos = new float[] { 0.0f, 0.0f, 0.0f }; // offset position relative to parent [x, y, z]
	public final float[] rot = new float[] { 0.0f, 0.0f, 0.0f }; // base rotation relative to parent [x, y, z]
	public final float[] size = new float[] { 4.0f, 5.5f, 3.5f, 3.0f, 4.0f }; // cuboid size [dx, dy0, dy1, dy2, dz]

	public AddedPartConfig() {
		clear();
	}

	@SuppressWarnings("all")
	public AddedPartConfig(int parentPartId) {
		parentPart = parentPartId;
		clear();
	}

	public void clear() {
		for (int i = 0; i < 3; i++) {
			pos[i] = 0.0f;
			rot[i] = 0.0f;
		}
		size[0] = 4.0f;
		size[1] = 5.5f;
		size[2] = 3.5f;
		size[3] = 3.0f;
		size[4] = 4.0f;
		isNormal = true;
		textureU = 40;
		textureV = 16;
		location = new ResourceLocation(CustomNpcs.MODID, "textures/entity/humanmale/steve.png");
		objUp = null;
		objDown = null;
	}

	public void load(NBTTagCompound compound) {
		parentPart = compound.getInteger("ParentPart");
		location = new ResourceLocation(compound.getString("Location"));
		if (compound.hasKey("OBJUpLocation", 8)) { objUp = new ResourceLocation(compound.getString("OBJUpLocation")); }
		if (compound.hasKey("OBJDownLocation", 8)) { objUp = new ResourceLocation(compound.getString("OBJDownLocation")); }
		for (int i = 0; i < 5; i++) {
			try { size[i] = ValueUtil.correctFloat(compound.getTagList("BaseSize", 5).getFloatAt(i), -1.0f, 1.0f); } catch (Exception e) { LogWriter.error(e); }
			if (i > 2) { continue; }
			try { pos[i] = ValueUtil.correctFloat(compound.getTagList("BasePosition", 5).getFloatAt(i), -1.0f, 1.0f); } catch (Exception e) { LogWriter.error(e); }
			try { rot[i] = ValueUtil.correctFloat(compound.getTagList("BaseRotation", 5).getFloatAt(i), -5.0f, 5.0f); } catch (Exception e) { LogWriter.error(e); }
		}
	}

	public NBTTagCompound save() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("ParentPart", parentPart);
		compound.setString("Location", location.toString());
		if (objUp != null) { compound.setString("OBJUpLocation", objUp.toString()); }
		if (objDown != null) { compound.setString("OBJDownLocation", objDown.toString()); }
		NBTTagList listPos = new NBTTagList();
		NBTTagList listRot = new NBTTagList();
		NBTTagList listSize = new NBTTagList();
		for (int i = 0; i < 5; i++) {
			listSize.appendTag(new NBTTagFloat(size[i]));
			if (i > 2) { continue; }
			listPos.appendTag(new NBTTagFloat(pos[i]));
			listRot.appendTag(new NBTTagFloat(rot[i]));
		}
		compound.setTag("BasePosition", listPos);
		compound.setTag("BaseRotation", listRot);
		compound.setTag("BaseSize", listSize);

		return compound;
	}

}
