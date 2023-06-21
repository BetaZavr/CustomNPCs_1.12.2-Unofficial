package noppes.npcs.api.handler.data;

import noppes.npcs.api.IPos;
import noppes.npcs.api.item.IItemStack;

public interface IQuestObjective {
	
	int getAreaRange();

	IItemStack getItem();

	int getMaxProgress();

	int getProgress();

	int getTargetID();

	String getTargetName();

	String getText();

	int getType();

	boolean isCompleted();

	boolean isIgnoreDamage();

	boolean isItemIgnoreNBT();

	boolean isItemLeave();

	void setAreaRange(int range);

	void setItem(IItemStack item);

	void setItemIgnoreDamage(boolean bo);

	void setItemIgnoreNBT(boolean bo);

	void setItemLeave(boolean bo);

	void setMaxProgress(int value);

	void setProgress(int value);

	void setTargetID(int id);

	void setTargetName(String name);

	void setType(int type);

	IPos getCompassPos();

	void setCompassPos(IPos pos);

	void setCompassPos(int x, int y, int z);
	
	int getCompassDimension();
	
	void setCompassDimension(int dimensionID);
	
	int getCompassRange();
	
	void setCompassRange(int range);

	String getOrientationEntityName();

	void setOrientationEntityName(String name);
	
}
