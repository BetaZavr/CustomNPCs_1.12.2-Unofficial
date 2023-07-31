package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.gui.IGuiTimer;
import noppes.npcs.api.wrapper.gui.CustomGuiComponentWrapper;
import noppes.npcs.util.AdditionalMethods;

public class CustomGuiTimerWrapper
extends CustomGuiComponentWrapper
implements IGuiTimer {
	
	int color;
	public long start, now, end;
	float scale;
	int width, height;
	public boolean reverse;

	public CustomGuiTimerWrapper() {
		this.color = 16777215;
		this.scale = 1.0f;
		this.start = 0;
		this.now = 0;
		this.end = 0;
	}

	public CustomGuiTimerWrapper(int id, long start, long end, int x, int y, int width, int height) {
		this();
		this.setID(id);
		this.setPos(x, y);
		this.setSize(width, height);
		this.setTime(start, end);
	}

	public CustomGuiTimerWrapper(int id, long start, long end, int x, int y, int width, int height, int color) {
		this(id, start, end, x, y, width, height);
		this.setColor(color);
	}

	@Override
	public CustomGuiComponentWrapper fromNBT(NBTTagCompound nbt) {
		super.fromNBT(nbt);
		this.setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
		this.setColor(nbt.getInteger("color"));
		this.setScale(nbt.getFloat("scale"));
		long s = nbt.getLong("start");
		long e = nbt.getLong("end");
		if (s!=this.start || e!=this.end || this.now==0) {
			this.setTime(s, e);
		}
		return this;
	}

	@Override
	public  void setTime(long start, long end) {
		this.start = start;
		this.end = end;
		this.now = System.currentTimeMillis();
		this.reverse = start > end;
	}

	@Override
	public int getColor() { return this.color; }

	@Override
	public int getHeight() { return this.height; }

	@Override
	public float getScale() { return this.scale; }

	@Override
	public String getText() {
		long time = this.reverse ? this.now-System.currentTimeMillis() : (System.currentTimeMillis()-this.now);
		time /= 50L;
		return AdditionalMethods.ticksToElapsedTime(time, false, false, false);
	}

	@Override
	public int getType() { return 6; }

	@Override
	public int getWidth() { return this.width; }

	@Override
	public IGuiTimer setColor(int color) {
		this.color = color;
		return this;
	}

	@Override
	public IGuiTimer setScale(float scale) {
		this.scale = scale;
		return this;
	}

	@Override
	public IGuiTimer setSize(int width, int height) {
		if (width  <= 0 || height <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + width + ", " + height + "]");
		}
		this.width = width;
		this.height = height;
		return this;
	}

	@Override
	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		super.toNBT(nbt);
		nbt.setIntArray("size", new int[] { this.width, this.height });
		nbt.setInteger("color", this.color);
		nbt.setFloat("scale", this.scale);
		nbt.setLong("start", this.start);
		nbt.setLong("end", this.end);
		nbt.setBoolean("Reverse", this.reverse);
		return nbt;
	}
	
}

