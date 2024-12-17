package noppes.npcs.client.gui.util;

import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nullable;
import java.util.List;

public interface IEditNPC {

	int getEventButton();

	EntityNPCInterface getNPC();

	boolean hasSubGui();

	void buttonEvent(GuiNpcButton button);

	void drawHoverText(String text, Object... args);

	void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll);

	void scrollDoubleClicked(String select, GuiCustomScroll scroll);

	void mouseDragged(GuiNpcSlider slider);

	void mousePressed(GuiNpcSlider slider);

	void mouseReleased(GuiNpcSlider slider);

	void unFocused(GuiNpcTextField textField);
	
	void addLine(int sX, int sY, int eX, int eY, int color, int size);

	void closeMiniWindow(GuiNpcMiniWindow miniWindow);

	void setMiniHoverText(int id, IComponentGui component);

	void setHoverText(@Nullable String text, Object ... args);

	void setHoverText(@Nullable List<String> hoverText);

	void closeSubGui(SubGuiInterface gui);

	boolean hasArea();

}
