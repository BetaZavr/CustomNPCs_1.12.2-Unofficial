package noppes.npcs.api;

import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface ILayerModel {

	IItemStack getModel();

	INbt getNbt();

	String getOBJModel();

	float getOffset(@ParamName("axis") int axis);

	int getPos();

	float getRotate(@ParamName("axis") int axis);

	int getRotateSpeed();

	float getScale(@ParamName("axis") int axis);

	boolean isRotate(@ParamName("axis") int axis);

	void setIsRotate(@ParamName("x") boolean x, @ParamName("y") boolean y, @ParamName("z") boolean z);

	void setModel(@ParamName("stack") IItemStack stack);

	void setNbt(@ParamName("nbt") INbt nbt);

	void setOBJModel(@ParamName("path") String path);

	void setOffset(@ParamName("x") float x, @ParamName("y") float y, @ParamName("z") float z);

	void setRotate(@ParamName("x") float x, @ParamName("y") float y, @ParamName("z") float z);

	void setRotateSpeed(@ParamName("speed") int speed);

	void setScale(@ParamName("x") float x, @ParamName("y") float y, @ParamName("z") float z);

}
