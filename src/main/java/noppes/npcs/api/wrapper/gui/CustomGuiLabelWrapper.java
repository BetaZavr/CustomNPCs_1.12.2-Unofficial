package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.gui.ILabel;

public class CustomGuiLabelWrapper extends CustomGuiComponentWrapper implements ILabel {
	int color;
	int height;
	String label;
	float scale;
	int width;

	public CustomGuiLabelWrapper() {
		this.color = 16777215;
		this.scale = 1.0f;
	}

	public CustomGuiLabelWrapper(int id, String label, int x, int y, int width, int height) {
		this.color = 16777215;
		this.scale = 1.0f;
		this.setID(id);
		this.setText(label);
		this.setPos(x, y);
		this.setSize(width, height);
	}

	public CustomGuiLabelWrapper(int id, String label, int x, int y, int width, int height, int color) {
		this(id, label, x, y, width, height);
		this.setColor(color);
	}

	@Override
	public CustomGuiComponentWrapper fromNBT(NBTTagCompound nbt) {
		super.fromNBT(nbt);
		this.setText(nbt.getString("label"));
		this.setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
		this.setColor(nbt.getInteger("color"));
		this.setScale(nbt.getFloat("scale"));
		return this;
	}

	@Override
	public int getColor() {
		return this.color;
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
	public String getText() {
		return this.label;
	}

	@Override
	public int getType() {
		return 1;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public ILabel setColor(int color) {
		this.color = color;
		return this;
	}

	@Override
	public ILabel setScale(float scale) {
		this.scale = scale;
		return this;
	}

	@Override
	public ILabel setSize(int width, int height) {
		this.width = width;
		this.height = height;
		return this;
	}

	@Override
	public ILabel setText(String label) {
		this.label = label;
		return this;
	}

	@Override
	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		super.toNBT(nbt);
		nbt.setString("label", this.label);
		nbt.setIntArray("size", new int[] { this.width, this.height });
		nbt.setInteger("color", this.color);
		nbt.setFloat("scale", this.scale);
		return nbt;
	}
}
