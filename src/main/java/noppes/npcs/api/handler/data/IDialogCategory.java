package noppes.npcs.api.handler.data;

public interface IDialogCategory {
	
	IDialog create();

	IDialog[] dialogs();

	String getName();
	
}
