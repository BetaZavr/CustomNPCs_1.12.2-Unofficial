package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.gui.ITexturedRect;

public class CustomGuiTexturedRectWrapper
extends CustomGuiComponentWrapper
implements ITexturedRect {
	
	int height;
	float scale;
	String texture;
	int textureX;
	int textureY;
	int width;

	public CustomGuiTexturedRectWrapper() {
		this.textureY = -1;
		this.scale = 1.0f;
	}

	public CustomGuiTexturedRectWrapper(int id, String texture, int x, int y, int width, int height) {
		this.textureY = -1;
		this.scale = 1.0f;
		this.setId(id);
		this.setTexture(texture);
		this.setPos(x, y);
		this.setSize(width, height);
	}

	public CustomGuiTexturedRectWrapper(int id, String texture, int x, int y, int width, int height, int textureX,
			int textureY) {
		this(id, texture, x, y, width, height);
		this.setTextureOffset(textureX, textureY);
	}

	@Override
	public CustomGuiComponentWrapper fromNBT(NBTTagCompound nbt) {
		super.fromNBT(nbt);
		this.setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
		this.setScale(nbt.getFloat("scale"));
		this.setTexture(nbt.getString("texture"));
		if (nbt.hasKey("texPos")) {
			this.setTextureOffset(nbt.getIntArray("texPos")[0], nbt.getIntArray("texPos")[1]);
		}
		return this;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public float getScale() {
		return this.scale;
	}

	@Override
	public String getTexture() {
		return this.texture;
	}

	@Override
	public int getTextureX() {
		return this.textureX;
	}

	@Override
	public int getTextureY() {
		return this.textureY;
	}

	@Override
	public int getType() {
		return GuiComponentType.TEXTURED_RECT.get();
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public ITexturedRect setScale(float scale) {
		this.scale = scale;
		return this;
	}

	@Override
	public ITexturedRect setSize(int width, int height) {
		if (width  <= 0 || height <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + width + ", " + height + "]");
		}
		this.width = width;
		this.height = height;
		return this;
	}

	@Override
	public ITexturedRect setTexture(String texture) {
		this.texture = texture;
		return this;
	}

	@Override
	public ITexturedRect setTextureOffset(int offsetX, int offsetY) {
		this.textureX = offsetX;
		this.textureY = offsetY;
		return this;
	}

	@Override
	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		super.toNBT(nbt);
		nbt.setIntArray("size", new int[] { this.width, this.height });
		nbt.setFloat("scale", this.scale);
		nbt.setString("texture", this.texture);
		if (this.textureX >= 0 && this.textureY >= 0) {
			nbt.setIntArray("texPos", new int[] { this.textureX, this.textureY });
		}
		return nbt;
	}
}
