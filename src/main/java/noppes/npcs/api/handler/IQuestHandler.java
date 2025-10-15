package noppes.npcs.api.handler;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestCategory;

public interface IQuestHandler {

	IQuestCategory[] categories();

	IQuest get(@ParamName("id") int id);

}
