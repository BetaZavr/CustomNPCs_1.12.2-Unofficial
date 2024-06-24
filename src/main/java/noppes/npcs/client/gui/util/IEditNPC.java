package noppes.npcs.client.gui.util;

import noppes.npcs.entity.EntityNPCInterface;

public interface IEditNPC {

	int getEventButton();

	EntityNPCInterface getNPC();

	boolean hasSubGui();

	void buttonEvent(GuiNpcButton button);

	void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll);

	void scrollDoubleClicked(String select, GuiCustomScroll scroll);

	void mouseDragged(GuiNpcSlider slider);

	void mousePressed(GuiNpcSlider slider);

	void mouseReleased(GuiNpcSlider slider);

	void unFocused(GuiNpcTextField textField);
	
	void addLine(int sX, int sY, int eX, int eY, int color, int size);

	void closeMiniWindow(GuiNpcMiniWindow miniWindow);

	void setMiniHoverText(int id, IComponentGui component);
	
}
