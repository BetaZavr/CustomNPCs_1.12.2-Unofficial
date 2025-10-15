package noppes.npcs.api.gui;

import noppes.npcs.api.ParamName;

public interface ITexturedRect extends ICustomGuiComponent {

	int getColor();

	int getHeight();

	float getScale();

	String getTexture();

	int getTextureX();

	int getTextureY();

	int getWidth();

	ITexturedRect setColor(@ParamName("color") int color);

	ITexturedRect setScale(@ParamName("scale") float scale);

	ITexturedRect setSize(@ParamName("width") int width, @ParamName("height") int height);

	ITexturedRect setTexture(@ParamName("texture") String texture);

	ITexturedRect setTextureOffset(@ParamName("textureX") int textureX, @ParamName("textureY") int textureY);

}
