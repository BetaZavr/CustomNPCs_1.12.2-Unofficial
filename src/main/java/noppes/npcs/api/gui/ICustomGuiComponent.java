package noppes.npcs.api.gui;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.item.IItemStack;

public interface ICustomGuiComponent {

	String[] getHoverText();

	IItemStack getHoverStack();

	int getId();

	int getPosX();

	int getPosY();

	boolean hasHoverText();

	void offSet(@ParamName("type") int type);

	@SuppressWarnings("all")
	ICustomGuiComponent setHoverStack(@ParamName("item") IItemStack item);

	ICustomGuiComponent setHoverText(@ParamName("hover") String hover);

	ICustomGuiComponent setHoverText(@ParamName("hovers") String[] hovers);

	ICustomGuiComponent setId(@ParamName("id") int id);

	ICustomGuiComponent setPos(@ParamName("x") int x, @ParamName("y") int y);

}
