package noppes.npcs.api.gui;

public interface ITexturedButton extends IButton {
	String getTexture();

	int getTextureX();

	int getTextureY();

	ITexturedButton setTexture(String p0);

	ITexturedButton setTextureOffset(int p0, int p1);
}
