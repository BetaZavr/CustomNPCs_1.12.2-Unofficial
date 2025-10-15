package noppes.npcs.api.block;

import noppes.npcs.api.ILayerModel;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface IBlockScripted extends IBlock {

	ILayerModel createLayerModel();

	String executeCommand(@ParamName("command") String command);

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

	boolean removeLayerModel(@ParamName("layer") ILayerModel layer);

	boolean removeLayerModel(@ParamName("id") int id);

	void setHardness(@ParamName("hardness") float hardness);

	void setIsLadder(@ParamName("enabled") boolean enabled);

	void setIsPassible(@ParamName("passable") boolean passable);

	void setLight(@ParamName("value") int value);

	void setModel(@ParamName("block") IBlock block);

	void setModel(@ParamName("item") IItemStack item);

	void setModel(@ParamName("name") String name);

	void setModel(@ParamName("blockName") String blockName, @ParamName("meta") int meta);

	void setRedstonePower(@ParamName("power") int power);

	void setResistance(@ParamName("resistance") float resistance);

	void setRotation(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	void setScale(@ParamName("x") float x, @ParamName("y") float y, @ParamName("z") float z);

	void trigger(@ParamName("id") int id, @ParamName("arguments") Object... arguments);

	void updateModel();

}
