package noppes.npcs.api.gui;

public interface IButton extends ICustomGuiComponent {
	int getHeight();

	String getLabel();

	String getTexture();

	int getTextureX();

	int getTextureY();

	int getWidth();

	boolean hasTexture();

	IButton setLabel(String p0);

	IButton setSize(int p0, int p1);

	IButton setTexture(String p0);

	IButton setTextureOffset(int p0, int p1);
}
