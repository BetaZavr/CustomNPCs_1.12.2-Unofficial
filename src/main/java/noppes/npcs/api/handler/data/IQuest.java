package noppes.npcs.api.handler.data;

import noppes.npcs.api.IContainer;
import noppes.npcs.api.entity.IPlayer;

public interface IQuest {
	
	IQuestObjective addTask();

	IQuestCategory getCategory();

	String getCompleteText();

	int[] getForgetDialogues();

	int[] getForgetQuests();

	int getId();

	boolean getIsRepeatable();

	// new
	int getLevel();

	String getLogText();

	String getName();

	IQuest getNextQuest();

	String getNpcName();

	IQuestObjective[] getObjectives(IPlayer<?> p0);

	IContainer getRewards();

	int getRewardType();

	String getTitle();

	boolean isCancelable();

	boolean isSetUp();

	boolean removeTask(IQuestObjective task);

	void save();

	void sendChangeToAll();

	void setCancelable(boolean cancelable);

	void setCompleteText(String p0);

	void setForgetDialogues(int[] forget);

	void setForgetQuests(int[] forget);

	void setLevel(int level);

	void setLogText(String p0);

	void setName(String p0);

	void setNextQuest(IQuest p0);

	void setNpcName(String p0);

	void setRewardText(String text);

	void setRewardType(int type);

}
