package noppes.npcs.api.gui;

public interface IButton extends ICustomGuiComponent {

	int getHeight();

	String getLabel();

	String getTexture();

	int getTextureX();

	int getTextureY();

	int getWidth();

	boolean hasTexture();

	IButton setLabel(String lable);

	IButton setSize(int width, int height);

	IButton setTexture(String texture);

	IButton setTextureOffset(int textureX, int textureY);

}
