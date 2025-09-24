package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.gui.ITexturedRect;
import noppes.npcs.util.ValueUtil;

public class CustomGuiTexturedRectWrapper extends CustomGuiComponentWrapper implements ITexturedRect {

	protected String texture;
	protected float scale = 1.0f;
	protected int textureX;
	protected int textureY = -1;
	protected int width;
	protected int height;
	protected int color = 0xFFFFFFFF;

	public CustomGuiTexturedRectWrapper() { }

	public CustomGuiTexturedRectWrapper(int id, String texture, int x, int y, int width, int height) {
		setId(id);
		setTexture(texture);
		setPos(x, y);
		setSize(width, height);
	}

	public CustomGuiTexturedRectWrapper(int id, String texture, int x, int y, int width, int height, int textureX, int textureY) {
		this(id, texture, x, y, width, height);
		setTextureOffset(textureX, textureY);
	}

	@Override
	public CustomGuiComponentWrapper fromNBT(NBTTagCompound nbt) {
		super.fromNBT(nbt);
		setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
		setScale(nbt.getFloat("scale"));
		setTexture(nbt.getString("texture"));
		if (nbt.hasKey("texPos")) { setTextureOffset(nbt.getIntArray("texPos")[0], nbt.getIntArray("texPos")[1]); }
		return this;
	}

	@Override
	public int getColor() { return color; }

	@Override
	public int getHeight() { return height; }

	@Override
	public float getScale() { return scale; }

	@Override
	public String getTexture() { return texture; }

	@Override
	public int getTextureX() { return textureX; }

	@Override
	public int getTextureY() { return textureY; }

	@Override
	public int getType() { return GuiComponentType.TEXTURED_RECT.get(); }

	@Override
	public int getWidth() { return width; }

	@Override
	public ITexturedRect setColor(int colorIn) {
		color = colorIn;
		return this;
	}

	@Override
	public ITexturedRect setScale(float scaleIn) {
		scale = ValueUtil.correctFloat(scaleIn, 0.0f, 10.0f);
		return this;
	}

	@Override
	public ITexturedRect setSize(int widthIn, int heightIn) {
		if (widthIn <= 0 || heightIn <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + widthIn + ", " + heightIn + "]");
		}
		width = widthIn;
		height = heightIn;
		return this;
	}

	@Override
	public ITexturedRect setTexture(String textureIn) {
		texture = textureIn;
		return this;
	}

	@Override
	public ITexturedRect setTextureOffset(int offsetX, int offsetY) {
		textureX = Math.max(0, offsetX);
		textureY = Math.max(0, offsetY);
		return this;
	}

	@Override
	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		super.toNBT(nbt);
		nbt.setIntArray("size", new int[] { width, height });
		nbt.setFloat("scale", scale);
		nbt.setInteger("color", color);
		nbt.setString("texture", texture);
		if (textureX >= 0 && textureY >= 0) {
			nbt.setIntArray("texPos", new int[] { textureX, textureY });
		}
		return nbt;
	}

}
