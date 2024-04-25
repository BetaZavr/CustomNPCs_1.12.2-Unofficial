package noppes.npcs.client.util;

import net.minecraft.util.ResourceLocation;

public class ResourceData {

	public ResourceLocation resource;
	public int u, v, width, height;
	public float tH;

	public ResourceData(ResourceLocation texture, int u, int v, int width, int height) {
		this.resource = texture;
		this.u = u;
		this.v = v;
		this.width = width;
		this.height = height;
		this.tH = 0.0f;
	}

}
