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

	public ModelPartData(String name) {
		this.color = 0xFFFFFFFF;
		this.colorPattern = 0xFFFFFFFF;
		this.type = 0;
		this.pattern = 0;
		this.playerTexture = false;
		this.name = name;
	}

	public String getColor() {
		StringBuilder str = new StringBuilder(Integer.toHexString(this.color));
		while (str.length() < 6) { str.insert(0, "0"); }
		return str.toString();
	}

	public ResourceLocation getResource() {
		if (this.location != null) { return this.location; }
		String texture = this.name + "/" + this.type;
		if ((this.location = ModelPartData.resources.get(texture)) != null) {
			return this.location;
		}
		this.location = new ResourceLocation("moreplayermodels:textures/" + texture + ".png");
		ModelPartData.resources.put(texture, this.location);
		return this.location;
	}

	public void readFromNBT(NBTTagCompound compound) {
		if (!compound.hasKey("Type")) {
			this.type = -1;
			return;
		}
		this.type = compound.getByte("Type");
		this.color = compound.getInteger("Color");
		this.playerTexture = compound.getBoolean("PlayerTexture");
		this.pattern = compound.getByte("Pattern");
		this.location = null;
	}

	public void setType(int type) {
		this.type = (byte) type;
		this.location = null;
	}

	@Override
	public String toString() {
		return "ModelPartData: {Color: " + this.color + "; Type: " + this.type + "; Location: " + location + "}";
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setByte("Type", this.type);
		compound.setInteger("Color", this.color);
		compound.setBoolean("PlayerTexture", this.playerTexture);
		compound.setByte("Pattern", this.pattern);
		return compound;
	}

}
