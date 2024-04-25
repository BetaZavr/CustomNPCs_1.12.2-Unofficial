package noppes.npcs.api.handler.data;

public interface IQuestCategory {

	IQuest create();

	String getName();

	IQuest[] quests();

}
