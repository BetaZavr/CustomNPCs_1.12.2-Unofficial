package noppes.npcs.api.handler.data;

public interface IDialog {

	IAvailability getAvailability();

	IDialogCategory getCategory();

	String getCommand();

	int getId();

	String getName();

	IDialogOption getOption(int slot);

	IDialogOption[] getOptions();

	IQuest getQuest();

	String getText();

	void save();

	void setCommand(String command);

	void setName(String name);

	void setQuest(IQuest quest);

	void setText(String text);

}
