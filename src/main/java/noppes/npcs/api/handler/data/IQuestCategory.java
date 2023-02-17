package noppes.npcs.api.handler.data;

import java.util.List;

public interface IQuestCategory {
	IQuest create();

	String getName();

	List<IQuest> quests();
}
