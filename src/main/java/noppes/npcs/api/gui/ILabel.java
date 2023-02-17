package noppes.npcs.api.gui;

public interface ILabel extends ICustomGuiComponent {
	int getColor();

	int getHeight();

	float getScale();

	String getText();

	int getWidth();

	ILabel setColor(int p0);

	ILabel setScale(float p0);

	ILabel setSize(int p0, int p1);

	ILabel setText(String p0);
}
