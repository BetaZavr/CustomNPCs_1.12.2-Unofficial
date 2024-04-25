package noppes.npcs.client.gui.util;

public interface ICustomScrollListener {

	void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll);

	void scrollDoubleClicked(String select, GuiCustomScroll scroll);
}
