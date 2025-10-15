package noppes.npcs.api.gui;

import noppes.npcs.api.ParamName;

public interface IGuiTimer extends ICustomGuiComponent {

	int getColor();

	int getHeight();

	float getScale();

	String getText();

	int getWidth();

	IGuiTimer setColor(@ParamName("color") int color);

	IGuiTimer setScale(@ParamName("scale") float scale);

	IGuiTimer setSize(@ParamName("width") int width, @ParamName("height") int height);

	void setTime(@ParamName("start") long start, @ParamName("end") long end);

}
