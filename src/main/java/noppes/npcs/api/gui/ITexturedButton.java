package noppes.npcs.api.gui;

public interface ITexturedButton extends IButton {

	String getTexture();

	int getTextureX();

	int getTextureY();

	ITexturedButton setTexture(String texture);

	ITexturedButton setTextureOffset(int textureX, int textureY);

}
