package noppes.npcs.api.gui;

public interface ICustomGuiComponent {
	String[] getHoverText();

	int getID();

	int getPosX();

	int getPosY();

	boolean hasHoverText();

	ICustomGuiComponent setHoverText(String hover);

	ICustomGuiComponent setHoverText(String[] hovers);

	ICustomGuiComponent setID(int id);

	ICustomGuiComponent setPos(int u, int v);
}
