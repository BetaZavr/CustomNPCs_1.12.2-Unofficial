package noppes.npcs.api.handler;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IDialogCategory;

public interface IDialogHandler {

	IDialogCategory[] categories();

	IDialog get(@ParamName("id") int id);

}
