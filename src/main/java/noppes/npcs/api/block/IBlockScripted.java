package noppes.npcs.api.block;

import noppes.npcs.api.ILayerModel;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.item.IItemStack;

public interface IBlockScripted
extends IBlock {
	
	String executeCommand(String command);

	float getHardness();

	boolean getIsLadder();

	boolean getIsPassible();

	int getLight();

	IItemStack getModel();

	int getRedstonePower();

	float getResistance();

	int getRotationX();

	int getRotationY();

	int getRotationZ();

	float getScaleX();

	float getScaleY();

	float getScaleZ();

	ITextPlane getTextPlane();

	ITextPlane getTextPlane2();

	ITextPlane getTextPlane3();

	ITextPlane getTextPlane4();

	ITextPlane getTextPlane5();

	ITextPlane getTextPlane6();

	ITimers getTimers();

	void setHardness(float hardness);

	void setIsLadder(boolean enabled);

	void setIsPassible(boolean passible);

	void setLight(int value);

	void setModel(IItemStack item);

	void setModel(String name);

	void setModel(String blockName, int meta);
	
	void setModel(IBlock iblock);

	void setRedstonePower(int power);

	void setResistance(float resistance);

	void setRotation(int x, int y, int z);

	void setScale(float x, float y, float z);

	void trigger(int id, Object ... arguments);
	
	ILayerModel[] getLayerModels();
	
	ILayerModel createLayerModel();

	void updateModel();

	boolean removeLayerModel(int id);

	boolean removeLayerModel(ILayerModel layer);
	
}
