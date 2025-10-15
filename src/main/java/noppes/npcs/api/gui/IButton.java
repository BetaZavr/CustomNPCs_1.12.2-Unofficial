package noppes.npcs.api.gui;

import noppes.npcs.api.ParamName;

public interface IButton extends ICustomGuiComponent {

	int getHeight();

	String getLabel();

	String getTexture();

	int getTextureX();

	int getTextureY();

	int getWidth();

	boolean hasTexture();

	IButton setLabel(@ParamName("label") String label);

	IButton setSize(@ParamName("width") int width, @ParamName("height") int height);

	IButton setTexture(@ParamName("texture") String texture);

	IButton setTextureOffset(@ParamName("textureX") int textureX, @ParamName("textureY") int textureY);

}
