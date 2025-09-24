package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.gui.IButton;
import noppes.npcs.api.gui.ICustomGuiComponent;

public class CustomGuiButtonWrapper extends CustomGuiComponentWrapper implements IButton {

	protected String label;
	protected String texture;
	protected int textureX;
	protected int textureY = -1;
	protected int height = -1;
	protected int width;

	public CustomGuiButtonWrapper() { }

	public CustomGuiButtonWrapper(int id, String label, int x, int y) {
		setId(id);
		setLabel(label);
		setPos(x, y);
	}

	public CustomGuiButtonWrapper(int id, String label, int x, int y, int width, int height) {
		this(id, label, x, y);
		setSize(width, height);
	}

	public CustomGuiButtonWrapper(int id, String label, int x, int y, int width, int height, String texture) {
		this(id, label, x, y, width, height);
		textureY = 0;
		setTexture(texture);
	}

	public CustomGuiButtonWrapper(int id, String label, int x, int y, int width, int height, String texture, int textureX, int textureY) {
		this(id, label, x, y, width, height, texture);
		setTextureOffset(textureX, textureY);
	}

	@Override
	public CustomGuiComponentWrapper fromNBT(NBTTagCompound nbt) {
		super.fromNBT(nbt);
		if (nbt.hasKey("size")) { setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]); }
		setLabel(nbt.getString("label"));
		if (nbt.hasKey("texture")) { setTexture(nbt.getString("texture")); }
		if (nbt.hasKey("texPos")) { setTextureOffset(nbt.getIntArray("texPos")[0], nbt.getIntArray("texPos")[1]); }
		return this;
	}

	@Override
	public int getHeight() { return height; }

	@Override
	public int getId() { return id; }

	@Override
	public String getLabel() { return label; }

	@Override
	public int getPosX() { return posX; }

	@Override
	public int getPosY() { return posY; }

	@Override
	public String getTexture() { return texture; }

	@Override
	public int getTextureX() { return textureX; }

	@Override
	public int getTextureY() { return textureY; }

	@Override
	public int getType() { return GuiComponentType.BUTTON.get(); }

	@Override
	public int getWidth() { return width; }

	@Override
	public boolean hasTexture() { return texture != null; }

	@Override
	public ICustomGuiComponent setId(int idIn) {
		id = idIn;
		return this;
	}

	@Override
	public IButton setLabel(String labelIn) {
		label = labelIn;
		return this;
	}

	@Override
	public IButton setSize(int widthIn, int heightIn) {
		if (widthIn <= 0 || heightIn <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + widthIn + ", " + heightIn + "]");
		}
		width = widthIn;
		height = heightIn;
		return this;
	}

	@Override
	public IButton setTexture(String textureIn) {
		texture = textureIn;
		return this;
	}

	@Override
	public IButton setTextureOffset(int textureXIn, int textureYIn) {
		textureX = textureXIn;
		textureY = textureYIn;
		return this;
	}

	@Override
	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		super.toNBT(nbt);
		if (width > 0 && height > 0) { nbt.setIntArray("size", new int[] { width, height }); }
		nbt.setString("label", label);
		if (hasTexture()) { nbt.setString("texture", texture); }
		if (textureX >= 0 && textureY >= 0) { nbt.setIntArray("texPos", new int[] { textureX, textureY }); }
		return nbt;
	}

}
