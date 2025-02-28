package noppes.npcs.client.gui.util;

public interface ICustomScrollListener {

	void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll);

	void scrollDoubleClicked(String select, IGuiCustomScroll scroll);

	boolean hasSubGui();

}
