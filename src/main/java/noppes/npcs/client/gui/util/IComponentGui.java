package noppes.npcs.client.gui.util;

import java.util.List;

public interface IComponentGui {

	List<String> getHoversText();

	boolean isHovered();

	int getID();

	int[] getCenter();

	void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks);

	boolean keyCnpcsPressed(char typedChar, int keyCode);

	boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton);

	boolean mouseCnpcsReleased(int mouseX, int mouseY, int state);

	IComponentGui setHoverText(String srt, Object ... args);

	IComponentGui setIsEnable(boolean isEnable);

	IComponentGui setIsVisible(boolean isVisible);

	void moveTo(int addX, int addY);

	void updateCnpcsScreen();

}
