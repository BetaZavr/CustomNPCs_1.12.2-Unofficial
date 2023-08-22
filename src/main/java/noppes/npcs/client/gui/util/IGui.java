package noppes.npcs.client.gui.util;

public interface IGui {
	
	void drawScreen(int x, int y);

	int getId();

	boolean isActive();

	void updateScreen();
	
}
