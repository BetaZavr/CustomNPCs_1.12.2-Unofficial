package noppes.npcs.api.handler.data;

import noppes.npcs.api.IPos;
import noppes.npcs.api.item.IItemStack;

public interface IQuestObjective { // QuestObjective

	int getAreaRange();

	int getCompassDimension();

	IPos getCompassPos();

	int getCompassRange();

	IItemStack getItem();

	int getMaxProgress();

	String getOrientationEntityName();

	int getProgress();

	int getTargetID();

	String getTargetName();

	String getText();

	int getType();

	boolean isAndTitle();

	boolean isCompleted();

	boolean isIgnoreDamage();

	boolean isItemIgnoreNBT();

	boolean isItemLeave();

	boolean isNotShowLogEntity();

	boolean isPartName();

	boolean isSetPointOnMiniMap();

	void setAndTitle(boolean andTitle);

	void setAreaRange(int range);

	void setCompassDimension(int dimensionID);

	void setCompassPos(int x, int y, int z);

	void setCompassPos(IPos pos);

	void setCompassRange(int range);

	void setItem(IItemStack item);

	void setItemIgnoreDamage(boolean bo);

	void setItemIgnoreNBT(boolean bo);

	void setItemLeave(boolean bo);

	void setMaxProgress(int value);

	void setNotShowLogEntity(boolean notShowLogEntity);

	void setOrientationEntityName(String name);

	void setPartName(boolean isPart);

	void setPointOnMiniMap(boolean bo);

	void setProgress(int value);

	void setTargetID(int id);

	void setTargetName(String name);

	void setType(int type);

}
