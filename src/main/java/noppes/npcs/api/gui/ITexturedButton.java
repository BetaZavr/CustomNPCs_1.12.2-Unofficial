package noppes.npcs.api.gui;

import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface ITexturedButton extends IButton {

	String getTexture();

	int getTextureX();

	int getTextureY();

	ITexturedButton setTexture(@ParamName("texture") String texture);

	ITexturedButton setTextureOffset(@ParamName("textureX") int textureX, @ParamName("textureY") int textureY);

}
