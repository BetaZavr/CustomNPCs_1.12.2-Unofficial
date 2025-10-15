package noppes.npcs.api.gui;

import noppes.npcs.api.ParamName;

public interface ILabel extends ICustomGuiComponent {

	int getColor();

	int getHeight();

	float getScale();

	String getText();

	int getWidth();

	boolean isShadow();

	ILabel setColor(@ParamName("color") int color);

	ILabel setScale(@ParamName("scale") float scale);

	void setShadow(@ParamName("showShadow") boolean showShadow);

	ILabel setSize(@ParamName("width") int width, @ParamName("height") int height);

	ILabel setText(@ParamName("label") String label);

}
