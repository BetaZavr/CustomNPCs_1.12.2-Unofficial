package noppes.npcs.api.gui;

public interface ITexturedRect extends ICustomGuiComponent {

	int getColor();

	int getHeight();

	float getScale();

	String getTexture();

	int getTextureX();

	int getTextureY();

	int getWidth();

	ITexturedRect setColor(int color);

	ITexturedRect setScale(float scale);

	ITexturedRect setSize(int width, int height);

	ITexturedRect setTexture(String texture);

	ITexturedRect setTextureOffset(int textureX, int textureY);

}
