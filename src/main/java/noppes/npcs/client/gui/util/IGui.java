package noppes.npcs.client.gui.util;

public interface IGui {
	void drawScreen(int p0, int p1);

	int getID();

	boolean isActive();

	void updateScreen();
}
