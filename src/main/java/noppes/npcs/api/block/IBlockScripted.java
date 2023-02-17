package noppes.npcs.api.block;

import noppes.npcs.api.ITimers;
import noppes.npcs.api.item.IItemStack;

public interface IBlockScripted
extends IBlock {
	
	String executeCommand(String p0);

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

	void setHardness(float p0);

	void setIsLadder(boolean p0);

	void setIsPassible(boolean p0);

	void setLight(int p0);

	void setModel(IItemStack p0);

	void setModel(String p0);

	void setRedstonePower(int p0);

	void setResistance(float p0);

	void setRotation(int p0, int p1, int p2);

	void setScale(float p0, float p1, float p2);

	void setModel(String blockName, int meta); // New
	
	void setModel(IBlock iblock); // New

}
