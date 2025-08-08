package noppes.npcs.api.constants;

public enum GuiComponentType {

	BUTTON(0),
	LABEL(1),
	TEXTURED_RECT(2),
	TEXT_FIELD(3),
	SCROLL(4),
	ITEM_SLOT(5),
	TIMER(6),
	ENTITY(7);

	final int type;

	GuiComponentType(int t) { type = t; }

	public int get() { return type; }

}
