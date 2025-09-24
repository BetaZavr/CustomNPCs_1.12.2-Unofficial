package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.gui.ITextField;

public class CustomGuiTextFieldWrapper extends CustomGuiComponentWrapper implements ITextField {

	protected String defaultText;
	protected int height;
	protected int width;

	public CustomGuiTextFieldWrapper() { }

	public CustomGuiTextFieldWrapper(int id, int x, int y, int width, int height) {
		setId(id);
		setPos(x, y);
		setSize(width, height);
	}

	@Override
	public CustomGuiComponentWrapper fromNBT(NBTTagCompound nbt) {
		super.fromNBT(nbt);
		setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
		if (nbt.hasKey("default")) { setText(nbt.getString("default")); }
		return this;
	}

	@Override
	public int getHeight() { return height; }

	@Override
	public String getText() { return defaultText; }

	@Override
	public int getType() { return GuiComponentType.TEXT_FIELD.get(); }

	@Override
	public int getWidth() { return width; }

	@Override
	public ITextField setSize(int widthIn, int heightIn) {
		if (widthIn <= 0 || heightIn <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + widthIn + ", " + heightIn + "]");
		}
		width = widthIn;
		height = heightIn;
		return this;
	}

	@Override
	public ITextField setText(String defaultTextIn) {
		defaultText = defaultTextIn;
		return this;
	}

	@Override
	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		super.toNBT(nbt);
		nbt.setIntArray("size", new int[] { width, height });
		if (defaultText != null && !defaultText.isEmpty()) { nbt.setString("default", defaultText); }
		return nbt;
	}

}
