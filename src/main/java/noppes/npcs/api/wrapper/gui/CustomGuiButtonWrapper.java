package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.gui.IButton;
import noppes.npcs.api.gui.ICustomGuiComponent;

public class CustomGuiButtonWrapper
extends CustomGuiComponentWrapper
implements IButton {
	
	int height;
	String label;
	String texture;
	int textureX;
	int textureY;
	int width;

	public CustomGuiButtonWrapper() {
		this.height = -1;
		this.textureY = -1;
	}

	public CustomGuiButtonWrapper(int id, String label, int x, int y) {
		this.height = -1;
		this.textureY = -1;
		this.setId(id);
		this.setLabel(label);
		this.setPos(x, y);
	}

	public CustomGuiButtonWrapper(int id, String label, int x, int y, int width, int height) {
		this(id, label, x, y);
		this.setSize(width, height);
	}

	public CustomGuiButtonWrapper(int id, String label, int x, int y, int width, int height, String texture) {
		this(id, label, x, y, width, height);
		this.textureY = 0;
		this.setTexture(texture);
	}

	public CustomGuiButtonWrapper(int id, String label, int x, int y, int width, int height, String texture, int textureX, int textureY) {
		this(id, label, x, y, width, height, texture);
		this.setTextureOffset(textureX, textureY);
	}

	@Override
	public CustomGuiComponentWrapper fromNBT(NBTTagCompound nbt) {
		super.fromNBT(nbt);
		if (nbt.hasKey("size")) {
			this.setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
		}
		this.setLabel(nbt.getString("label"));
		if (nbt.hasKey("texture")) {
			this.setTexture(nbt.getString("texture"));
		}
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
	public int getId() {
		return this.id;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public int getPosX() {
		return this.posX;
	}

	@Override
	public int getPosY() {
		return this.posY;
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
		return GuiComponentType.BUTTON.get();
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public boolean hasTexture() {
		return this.texture != null;
	}

	@Override
	public ICustomGuiComponent setId(int id) {
		this.id = id;
		return this;
	}

	@Override
	public IButton setLabel(String label) {
		this.label = label;
		return this;
	}

	@Override
	public IButton setSize(int width, int height) {
		if (width  <= 0 || height <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + width + ", " + height + "]");
		}
		this.width = width;
		this.height = height;
		return this;
	}

	@Override
	public IButton setTexture(String texture) {
		this.texture = texture;
		return this;
	}

	@Override
	public IButton setTextureOffset(int textureX, int textureY) {
		this.textureX = textureX;
		this.textureY = textureY;
		return this;
	}

	@Override
	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		super.toNBT(nbt);
		if (this.width > 0 && this.height > 0) {
			nbt.setIntArray("size", new int[] { this.width, this.height });
		}
		nbt.setString("label", this.label);
		if (this.hasTexture()) {
			nbt.setString("texture", this.texture);
		}
		if (this.textureX >= 0 && this.textureY >= 0) {
			nbt.setIntArray("texPos", new int[] { this.textureX, this.textureY });
		}
		return nbt;
	}
}
