package noppes.npcs.api.gui;

public interface ITextField
extends ICustomGuiComponent {
	
	int getHeight();

	String getText();

	int getWidth();

	ITextField setSize(int width, int height);

	ITextField setText(String text);
	
}
