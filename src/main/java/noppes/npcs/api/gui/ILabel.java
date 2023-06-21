package noppes.npcs.api.gui;

public interface ILabel
extends ICustomGuiComponent {
	
	int getColor();

	int getHeight();

	float getScale();

	String getText();

	int getWidth();

	ILabel setColor(int color);

	ILabel setScale(float scale);

	ILabel setSize(int width, int height);

	ILabel setText(String label);
	
}
