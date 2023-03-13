package noppes.npcs.api.block;

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

	void setRedstonePower(int p0);

	void setResistance(float p0);

	void setRotation(int p0, int p1, int p2);

	void setScale(float p0, float p1, float p2);

	void setModel(String blockName, int meta); // New
	
	void setModel(IBlock iblock); // New

}
