package noppes.npcs.api.handler.data;

import noppes.npcs.api.item.IItemStack;

public interface IQuestObjective {
	int getAreaRange();

	IItemStack getItem();

	int getMaxProgress();

	int getProgress();

	int getTargetID();

	String getTargetName();

	String getText();

	// New
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

}
