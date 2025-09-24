package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.gui.ILabel;

public class CustomGuiLabelWrapper extends CustomGuiComponentWrapper implements ILabel {

	protected String label;
	protected boolean showShadow = false;
	protected float scale = 1.0f;
	protected int height;
	protected int width;
	protected int color = CustomNpcs.LableColor.getRGB();

	public CustomGuiLabelWrapper() { }

	public CustomGuiLabelWrapper(int id, String label, int x, int y, int width, int height) {
		setId(id);
		setText(label);
		setPos(x, y);
		setSize(width, height);
	}

	public CustomGuiLabelWrapper(int id, String label, int x, int y, int width, int height, int color) {
		this(id, label, x, y, width, height);
		setColor(color);
	}

	@Override
	public CustomGuiComponentWrapper fromNBT(NBTTagCompound nbt) {
		super.fromNBT(nbt);
		setText(nbt.getString("label"));
		setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
		setColor(nbt.getInteger("color"));
		setScale(nbt.getFloat("scale"));
		showShadow = nbt.getBoolean("shadow");
		return this;
	}

	@Override
	public int getColor() { return color; }

	@Override
	public int getHeight() { return height; }

	@Override
	public float getScale() { return scale; }

	@Override
	public String getText() { return label; }

	@Override
	public int getType() { return GuiComponentType.LABEL.get(); }

	@Override
	public int getWidth() { return width; }

	@Override
	public boolean isShadow() { return showShadow; }

	@Override
	public ILabel setColor(int colorIn) {
		color = colorIn;
		return this;
	}

	@Override
	public ILabel setScale(float scaleIn) {
		scale = scaleIn;
		return this;
	}

	@Override
	public void setShadow(boolean showShadowIn) { showShadow = showShadowIn; }

	@Override
	public ILabel setSize(int widthIn, int heightIn) {
		if (widthIn <= 0 || heightIn <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + widthIn + ", " + heightIn + "]");
		}
		width = widthIn;
		height = heightIn;
		return this;
	}

	@Override
	public ILabel setText(String labelIn) {
		label = labelIn;
		return this;
	}

	@Override
	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		super.toNBT(nbt);
		nbt.setString("label", label);
		nbt.setIntArray("size", new int[] { width, height });
		nbt.setInteger("color", color);
		nbt.setFloat("scale", scale);
		nbt.setBoolean("shadow", showShadow);
		return nbt;
	}

}
