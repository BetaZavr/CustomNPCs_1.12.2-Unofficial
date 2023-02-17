package noppes.npcs.api.gui;

public interface ITextField extends ICustomGuiComponent {
	int getHeight();

	String getText();

	int getWidth();

	ITextField setSize(int p0, int p1);

	ITextField setText(String p0);
}
