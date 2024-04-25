package noppes.npcs.api.gui;

public interface ICustomGuiComponent {

	String[] getHoverText();

	int getId();

	int getPosX();

	int getPosY();

	boolean hasHoverText();

	void offSet(int type);

	ICustomGuiComponent setHoverText(String hover);

	ICustomGuiComponent setHoverText(String[] hovers);

	ICustomGuiComponent setId(int id);

	ICustomGuiComponent setPos(int x, int y);

}
