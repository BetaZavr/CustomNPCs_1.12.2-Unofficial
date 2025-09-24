package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.gui.IGuiTimer;
import noppes.npcs.util.Util;

public class CustomGuiTimerWrapper extends CustomGuiComponentWrapper implements IGuiTimer {

	protected float scale = 1.0f;
	protected int height;
	protected int width;
	protected int color = 0xFFFFFF;
	public long start = 0L;
	public long now = 0L;
	public long end = 0L;
	public boolean reverse;

	public CustomGuiTimerWrapper() { }

	public CustomGuiTimerWrapper(int id, long start, long end, int x, int y, int width, int height) {
		setId(id);
		setPos(x, y);
		setSize(width, height);
		setTime(start, end);
	}

	public CustomGuiTimerWrapper(int id, long start, long end, int x, int y, int width, int height, int color) {
		this(id, start, end, x, y, width, height);
		setColor(color);
	}

	@Override
	public CustomGuiComponentWrapper fromNBT(NBTTagCompound nbt) {
		super.fromNBT(nbt);
		setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
		setColor(nbt.getInteger("color"));
		setScale(nbt.getFloat("scale"));
		long s = nbt.getLong("start");
		long e = nbt.getLong("end");
		if (s != start || e != end || now == 0) { setTime(s, e); }
		return this;
	}

	@Override
	public int getColor() { return color; }

	@Override
	public int getHeight() { return height; }

	@Override
	public float getScale() { return scale; }

	@Override
	public String getText() {
		long time = reverse ? now - System.currentTimeMillis() : (System.currentTimeMillis() - now);
		time /= 50L;
		return Util.instance.ticksToElapsedTime(time, false, false, false);
	}

	@Override
	public int getType() {
		return GuiComponentType.TIMER.get();
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public IGuiTimer setColor(int colorIn) {
		color = colorIn;
		return this;
	}

	@Override
	public IGuiTimer setScale(float scaleIn) {
		scale = scaleIn;
		return this;
	}

	@Override
	public IGuiTimer setSize(int widthIn, int heightIn) {
		if (widthIn <= 0 || heightIn <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + widthIn + ", " + heightIn + "]");
		}
		width = widthIn;
		height = heightIn;
		return this;
	}

	@Override
	public void setTime(long startIn, long endIn) {
		start = startIn;
		end = endIn;
		now = System.currentTimeMillis();
		reverse = start > end;
	}

	@Override
	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		super.toNBT(nbt);
		nbt.setIntArray("size", new int[] { width, height });
		nbt.setInteger("color", color);
		nbt.setFloat("scale", scale);
		nbt.setLong("start", start);
		nbt.setLong("end", end);
		nbt.setBoolean("Reverse", reverse);
		return nbt;
	}

}
