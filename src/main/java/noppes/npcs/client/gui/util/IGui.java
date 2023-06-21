package noppes.npcs.client.gui.util;

public interface IGui {
	
	void drawScreen(int x, int y);

	int getID();

	boolean isActive();

	void updateScreen();
	
}
