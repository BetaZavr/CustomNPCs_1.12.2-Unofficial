package noppes.npcs.api.handler;

import java.util.List;

import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IDialogCategory;

public interface IDialogHandler {
	List<IDialogCategory> categories();

	IDialog get(int p0);
}
