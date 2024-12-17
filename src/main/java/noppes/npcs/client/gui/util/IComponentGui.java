package noppes.npcs.client.gui.util;

public interface IComponentGui {

	int getId();

	int[] getCenter();

	void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks);

	void setHoverText(String srt, Object ... args);

	void updateScreen();

}
