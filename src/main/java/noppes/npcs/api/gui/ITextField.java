package noppes.npcs.api.gui;

import noppes.npcs.api.ParamName;

public interface ITextField extends ICustomGuiComponent {

	int getHeight();

	String getText();

	int getWidth();

	ITextField setSize(@ParamName("width") int width, @ParamName("height") int height);

	ITextField setText(@ParamName("text") String text);

}
