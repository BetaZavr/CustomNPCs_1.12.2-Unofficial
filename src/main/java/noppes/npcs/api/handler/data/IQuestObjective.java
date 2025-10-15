package noppes.npcs.api.handler.data;

import noppes.npcs.api.IPos;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
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

	void setAndTitle(@ParamName("andTitle") boolean andTitle);

	void setAreaRange(@ParamName("nbt") int range);

	void setCompassDimension(@ParamName("dimensionId") int dimensionId);

	void setCompassPos(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	void setCompassPos(@ParamName("pos") IPos pos);

	void setCompassRange(@ParamName("range") int range);

	void setItem(@ParamName("item") IItemStack item);

	void setItemIgnoreDamage(@ParamName("bo") boolean bo);

	void setItemIgnoreNBT(@ParamName("bo") boolean bo);

	void setItemLeave(@ParamName("bo") boolean bo);

	void setMaxProgress(@ParamName("value") int value);

	void setNotShowLogEntity(@ParamName("notShowLogEntity") boolean notShowLogEntity);

	void setOrientationEntityName(@ParamName("name") String name);

	void setPartName(@ParamName("isPart") boolean isPart);

	void setPointOnMiniMap(@ParamName("bo") boolean bo);

	void setProgress(@ParamName("value") int value);

	void setTargetID(@ParamName("id") int id);

	void setTargetName(@ParamName("name") String name);

	void setType(@ParamName("type") int type);

}
