package noppes.npcs.api;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.item.IItemStack;

public interface ILayerModel {

	int getPos();
	
	public NBTTagCompound getNbt();
	
	void setNbt(NBTTagCompound nbt);

	float getOffset(int axis);

	void setOffset(float x, float y, float z);

	IItemStack getModel();

	void setModel(IItemStack stack);

	String getOBJModel();

	void setOBJModel(String path);

	float getRotate(int axis);

	void setRotate(float x, float y, float z);

	boolean isRotate(int axis);

	void setIsRotate(boolean x, boolean y, boolean z);

	float getScale(int axis);

	void setScale(float x, float y, float z);
	
	int getRotateSpeed();
	
	void setRotateSpeed(int speed);
	
}
