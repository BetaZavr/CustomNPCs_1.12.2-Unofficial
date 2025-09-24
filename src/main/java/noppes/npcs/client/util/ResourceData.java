package noppes.npcs.client.util;

import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ResourceData {

	public static final ResourceData EMPTY = new ResourceData(null, 0, 0, 0,0);

	public ResourceLocation resource;
	public int u;
	public int v;
	public int width;
	public int height;
	public float tW = 0.0f;
	public float tH = 0.0f;
	public float tD = 0.0f;
	public float scaleX = 0.0f;
	public float scaleY = 0.0f;
	public float scaleZ = 0.0f;
	// OBJ
	public Map<String, String> materialTextures = new HashMap<>();
	public boolean isOBJ;
	public float rotateX = 0.0f;
	public float rotateY = 0.0f;
	public float rotateZ = 0.0f;

	public ResourceData(ResourceLocation texture, int uIn, int vIn, int widthIn, int heightIn) {
		resource = texture;
		isOBJ = texture != null && texture.getResourcePath().toLowerCase().endsWith(".obj");
		width = widthIn;
		height = heightIn;
		u = uIn;
		v = vIn;
	}

	public boolean isOBJ() { return isOBJ; }

}
