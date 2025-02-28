package noppes.npcs.client.util;

import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.gui.util.IResourceData;

public class ResourceData implements IResourceData {

	public ResourceLocation resource;
	public int u, v, width, height;
	public float tH;
	public float scaleX = 0.0f;
	public float scaleY = 0.0f;

	public ResourceData(ResourceLocation texture, int u, int v, int width, int height) {
		this.resource = texture;
		this.u = u;
		this.v = v;
		this.width = width;
		this.height = height;
		tH = 0.0f;
	}

	@Override
	public ResourceLocation getResource() { return resource; }

	@Override
	public int getWidth() { return width; }

	@Override
	public int getHeight() { return height; }

	@Override
	public float getTextureHeight() { return tH; }

	@Override
	public float getScaleX() { return scaleX; }

	@Override
	public float getScaleY() { return scaleY; }

	@Override
	public int getU() { return u; }

	@Override
	public int getV() { return v; }

}
