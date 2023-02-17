package noppes.npcs.api.gui;

public interface ITexturedRect extends ICustomGuiComponent {
	int getHeight();

	float getScale();

	String getTexture();

	int getTextureX();

	int getTextureY();

	int getWidth();

	ITexturedRect setScale(float p0);

	ITexturedRect setSize(int p0, int p1);

	ITexturedRect setTexture(String p0);

	ITexturedRect setTextureOffset(int p0, int p1);
}
