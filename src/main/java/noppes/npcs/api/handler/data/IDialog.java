package noppes.npcs.api.handler.data;

import java.util.List;

public interface IDialog {
	IAvailability getAvailability();

	IDialogCategory getCategory();

	String getCommand();

	int getId();

	String getName();

	IDialogOption getOption(int p0);

	List<IDialogOption> getOptions();

	IQuest getQuest();

	String getText();

	void save();

	void setCommand(String p0);

	void setName(String p0);

	void setQuest(IQuest p0);

	void setText(String p0);
}
