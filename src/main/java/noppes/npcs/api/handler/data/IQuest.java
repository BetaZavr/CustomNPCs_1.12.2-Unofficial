package noppes.npcs.api.handler.data;

import noppes.npcs.api.IContainer;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IPlayer;

@SuppressWarnings("all")
public interface IQuest {

	IQuestObjective addTask();

	IQuestCategory getCategory();

	ICustomNpc<?> getCompleterNpc();

	String getCompleteText();

	int getExtraButton();

	String getExtraButtonText();

	int[] getForgetDialogues();

	int[] getForgetQuests();

	int getId();

	boolean getIsRepeatable();

	int getLevel();

	String getLogText();

	String getName();

	IQuest getNextQuest();

	IQuestObjective[] getObjectives(@ParamName("player") IPlayer<?> player);

	IContainer getRewards();

	int getRewardType();

	String getTitle();

	boolean isCancelable();

	boolean isSetUp();

	boolean removeTask(@ParamName("task") IQuestObjective task);

	void save();

	void sendChangeToAll();

	void setCancelable(@ParamName("cancelable") boolean cancelable);

	void setCompleterNpc(@ParamName("npc") ICustomNpc<?> npc);

	void setCompleteText(@ParamName("text") String text);

	void setExtraButton(@ParamName("type") int type);

	void setExtraButtonText(@ParamName("hover") String hover);

	void setForgetDialogues(@ParamName("forget") int[] forget);

	void setForgetQuests(@ParamName("forget") int[] forget);

	void setLevel(@ParamName("level") int level);

	void setLogText(@ParamName("text") String text);

	void setName(@ParamName("name") String name);

	void setNextQuest(@ParamName("quest") IQuest quest);

	void setRewardText(@ParamName("text") String text);

	void setRewardType(@ParamName("type") int type);

}
