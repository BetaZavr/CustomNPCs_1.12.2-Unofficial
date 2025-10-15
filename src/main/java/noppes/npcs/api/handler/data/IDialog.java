package noppes.npcs.api.handler.data;

import noppes.npcs.api.ParamName;

public interface IDialog {

	IAvailability getAvailability();

	IDialogCategory getCategory();

	String getCommand();

	int getId();

	String getName();

	IDialogOption getOption(@ParamName("slot") int slot);

	IDialogOption[] getOptions();

	IQuest getQuest();

	String getText();

	void save();

	void setCommand(@ParamName("command") String command);

	void setName(@ParamName("name") String name);

	void setQuest(@ParamName("quest") IQuest quest);

	void setText(@ParamName("text") String text);

}
