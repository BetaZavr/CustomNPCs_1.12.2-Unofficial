package noppes.npcs.api.constants;

public enum GuiComponentType {

	BUTTON(0), ITEM_SLOT(5), LABEL(1), SCROLL(4), TEXT_FIELD(3), TEXTURED_RECT(2), TIMER(6), ENTITY(7);

	final int type;

	GuiComponentType(int t) {
		this.type = t;
	}

	public int get() {
		return this.type;
	}

}
