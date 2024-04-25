package moe.plushie.armourers_workshop.api.common.skin;

import moe.plushie.armourers_workshop.api.common.IRectangle3D;

public class Rectangle3D implements IRectangle3D {

	private int x;
	private int y;
	private int z;
	private int width;
	private int height;
	private int depth;

	public Rectangle3D(int x, int y, int z, int width, int height, int depth) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.width = width;
		this.height = height;
		this.depth = depth;
	}

	@Override
	public int getDepth() {
		return this.depth;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getX() {
		return this.x;
	}

	@Override
	public int getY() {
		return this.y;
	}

	@Override
	public int getZ() {
		return this.z;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setZ(int z) {
		this.z = z;
	}

	@Override
	public String toString() {
		return "Rectangle3D [x=" + x + ", y=" + y + ", z=" + z + ", width=" + width + ", height=" + height + ", depth="
				+ depth + "]";
	}
}
