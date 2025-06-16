package noppes.npcs.api.gui;

import noppes.npcs.api.item.IItemStack;

public interface ICustomGuiComponent {

	String[] getHoverText();

	IItemStack getHoverStack();

	int getId();

	int getPosX();

	int getPosY();

	boolean hasHoverText();

	void offSet(int type);

	@SuppressWarnings("all")
	ICustomGuiComponent setHoverStack(IItemStack item);

	ICustomGuiComponent setHoverText(String hover);

	ICustomGuiComponent setHoverText(String[] hovers);

	ICustomGuiComponent setId(int id);

	ICustomGuiComponent setPos(int x, int y);

}
