package noppes.npcs;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class ModelPartData {

	private static final Map<String, ResourceLocation> resources = new HashMap<>();

	public int color;
	public int colorPattern;
	private ResourceLocation location;
	public String name;
	public byte pattern;
	public boolean playerTexture;
	public byte type;

	public ModelPartData(String nameIn) {
		color = 0xFFFFFFFF;
		colorPattern = 0xFFFFFFFF;
		type = 0;
		pattern = 0;
		playerTexture = false;
		name = nameIn;
	}

	public String getColor() {
		StringBuilder str = new StringBuilder(Integer.toHexString(color));
		while (str.length() < 6) { str.insert(0, "0"); }
		return str.toString();
	}

	public ResourceLocation getResource() {
		if (location != null) { return location; }
		String texture = name + "/" + type;
		if ((location = ModelPartData.resources.get(texture)) != null) {
			return location;
		}
		location = new ResourceLocation("moreplayermodels:textures/" + texture + ".png");
		ModelPartData.resources.put(texture, location);
		return location;
	}

	public void load(NBTTagCompound compound) {
		if (!compound.hasKey("Type")) {
			type = -1;
			return;
		}
		type = compound.getByte("Type");
		color = compound.getInteger("Color");
		playerTexture = compound.getBoolean("PlayerTexture");
		pattern = compound.getByte("Pattern");
		location = null;
	}

	public void setType(int typeIn) {
		type = (byte) typeIn;
		location = null;
	}

	@Override
	public String toString() {
		return "ModelPartData: {Color: " + color + "; Type: " + type + "; Location: " + location + "}";
	}

	public NBTTagCompound save() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setByte("Type", type);
		compound.setInteger("Color", color);
		compound.setBoolean("PlayerTexture", playerTexture);
		compound.setByte("Pattern", pattern);
		return compound;
	}

}
