package noppes.npcs.api.gui;

public interface ITexturedRect
extends ICustomGuiComponent {
	
	int getHeight();

	float getScale();

	String getTexture();

	int getTextureX();

	int getTextureY();

	int getWidth();

	ITexturedRect setScale(float scale);

	ITexturedRect setSize(int width, int height);
	
	ITexturedRect setTexture(String texture);

	ITexturedRect setTextureOffset(int textureX, int textureY);
	
}
