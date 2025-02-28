package noppes.npcs.client.gui.util;

public interface IComponentGui {

	int getId();

	int[] getCenter();

	void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks);

	void setHoverText(String srt, Object ... args);

	int getLeft();

	int getTop();

	void setLeft(int left);

	void setTop(int top);

	int getWidth();

	int getHeight();

	void customKeyTyped(char c, int id);

	void customMouseClicked(int mouseX, int mouseY, int mouseButton);

	void customMouseReleased(int mouseX, int mouseY, int mouseButton);

	boolean isVisible();

	void setVisible(boolean bo);

	boolean isEnabled();

	void setEnabled(boolean bo);

	boolean isMouseOver();

}
