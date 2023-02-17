package noppes.npcs.api.handler.data;

import java.util.List;

public interface IDialogCategory {
	IDialog create();

	List<IDialog> dialogs();

	String getName();
}
