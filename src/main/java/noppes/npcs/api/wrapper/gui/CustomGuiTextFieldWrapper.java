package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.gui.ITextField;

public class CustomGuiTextFieldWrapper
extends CustomGuiComponentWrapper
implements ITextField {
	
	String defaultText;
	int height;
	int width;

	public CustomGuiTextFieldWrapper() {
	}

	public CustomGuiTextFieldWrapper(int id, int x, int y, int width, int height) {
		this.setId(id);
		this.setPos(x, y);
		this.setSize(width, height);
	}

	@Override
	public CustomGuiComponentWrapper fromNBT(NBTTagCompound nbt) {
		super.fromNBT(nbt);
		this.setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
		if (nbt.hasKey("default")) {
			this.setText(nbt.getString("default"));
		}
		return this;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public String getText() {
		return this.defaultText;
	}

	@Override
	public int getType() {
		return GuiComponentType.TEXT_FIELD.get();
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public ITextField setSize(int width, int height) {
		if (width  <= 0 || height <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + width + ", " + height + "]");
		}
		this.width = width;
		this.height = height;
		return this;
	}

	@Override
	public ITextField setText(String defaultText) {
		this.defaultText = defaultText;
		return this;
	}

	@Override
	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		super.toNBT(nbt);
		nbt.setIntArray("size", new int[] { this.width, this.height });
		if (this.defaultText != null && !this.defaultText.isEmpty()) {
			nbt.setString("default", this.defaultText);
		}
		return nbt;
	}
}
