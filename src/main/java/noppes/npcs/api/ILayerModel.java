package noppes.npcs.api;

import noppes.npcs.api.item.IItemStack;

public interface ILayerModel {

	IItemStack getModel();

	INbt getNbt();

	String getOBJModel();

	float getOffset(int axis);

	int getPos();

	float getRotate(int axis);

	int getRotateSpeed();

	float getScale(int axis);

	boolean isRotate(int axis);

	void setIsRotate(boolean x, boolean y, boolean z);

	void setModel(IItemStack stack);

	void setNbt(INbt nbt);

	void setOBJModel(String path);

	void setOffset(float x, float y, float z);

	void setRotate(float x, float y, float z);

	void setRotateSpeed(int speed);

	void setScale(float x, float y, float z);

}
