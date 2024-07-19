package noppes.npcs.api.block;

import noppes.npcs.api.ILayerModel;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.item.IItemStack;

public interface IBlockScripted extends IBlock {

	ILayerModel createLayerModel();

	String executeCommand(String command);

	float getHardness();

	boolean getIsLadder();

	boolean getIsPassable();

	ILayerModel[] getLayerModels();

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

	boolean removeLayerModel(ILayerModel layer);

	boolean removeLayerModel(int id);

	void setHardness(float hardness);

	void setIsLadder(boolean enabled);

	void setIsPassable(boolean passable);

	void setLight(int value);

	void setModel(IBlock iblock);

	void setModel(IItemStack item);

	void setModel(String name);

	void setModel(String blockName, int meta);

	void setRedstonePower(int power);

	void setResistance(float resistance);

	void setRotation(int x, int y, int z);

	void setScale(float x, float y, float z);

	void trigger(int id, Object... arguments);

	void updateModel();

}
