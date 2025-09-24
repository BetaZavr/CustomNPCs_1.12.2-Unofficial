package noppes.npcs.client.gui.util;

public interface ICustomScrollListener {

	void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll);

	void scrollDoubleClicked(String select, GuiCustomScroll scroll);

}
