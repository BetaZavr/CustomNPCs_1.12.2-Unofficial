package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface IEditNPC {

    int getEventButton();

	boolean hasSubGui();

	void addLine(int sX, int sY, int eX, int eY, int color, int size);

	void closeMiniWindow(GuiNpcMiniWindow miniWindow);

	void setBackground(String texture);

	void putHoverText(@Nullable List<String> hoverText);

	void putHoverText(@Nullable String text, Object ... args);

	boolean hasArea();

	void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton);

	List<String> getHoverText();

	IComponentGui get(int id, Class<?> classType);

    void onClosed();

    void drawNpc(Entity entity, int x, int y, float zoomed, int rotation, int vertical, int mouseFocus);

	void drawNpc(int x, int y);

	void setSubGui(SubGuiInterface gui);

	void drawHoverText(String text, Object... args);

	boolean hasHoverText();

    boolean keyCnpcsPressed(char typedChar, int keyCode);

	boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton);

	boolean mouseCnpcsReleased(int mouseX, int mouseY, int state);

	void save();

	void subGuiClosed(GuiScreen subGui);

	void elementClicked();

	SubGuiInterface getSubGui();

}
