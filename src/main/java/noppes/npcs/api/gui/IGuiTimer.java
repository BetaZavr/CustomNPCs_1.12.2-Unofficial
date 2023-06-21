package noppes.npcs.api.gui;

public interface IGuiTimer
extends ICustomGuiComponent {

	void setTime(long start, long end);

	int getColor();

	int getHeight();

	float getScale();

	String getText();

	int getWidth();

	IGuiTimer setColor(int color);

	IGuiTimer setScale(float scale);

	IGuiTimer setSize(int width, int height);

}
