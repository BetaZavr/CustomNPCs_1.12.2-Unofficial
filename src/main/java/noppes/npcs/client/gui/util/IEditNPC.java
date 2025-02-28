package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.INpc;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface IEditNPC {


    static IEditNPC getCustomGui(INpc npc, Container container) {
        return new GuiContainerNPCInterface(npc, container);
    }

    static IEditNPC getCustomGui(INpc npc, boolean isContainer) {
		if (isContainer) { return getCustomGui(npc, null); }
        return new GuiNPCInterface(npc);
    }

    int getEventButton();

	ISubGuiInterface getSubGui();

	IGuiNpcTextField getTextField(int id);

	IGuiMenuTopButton getTopButton(int id);

	IGuiNpcMiniWindow getMiniWindow(int id);

	boolean hasSubGui();

	void drawHoverText(String text, Object... args);

	void mouseDragged(IGuiNpcSlider slider);

	void mousePressed(IGuiNpcSlider slider);

	void mouseReleased(IGuiNpcSlider slider);

	void unFocused(IGuiNpcTextField textField);
	
	void addLine(int sX, int sY, int eX, int eY, int color, int size);

	void closeMiniWindow(IGuiNpcMiniWindow miniWindow);

	void setMiniHoverText(int id, IComponentGui component);

	void setHoverText(@Nullable String text, Object ... args);

	void setBackground(String texture);

	void setHoverText(@Nullable List<String> hoverText);

	void close();

	void closeSubGui(ISubGuiInterface gui);

	boolean hasArea();

	void buttonEvent(IGuiNpcButton button);

	void buttonEvent(@Nonnull IGuiNpcButton button, int mouseButton);

	void add(IComponentGui component);

	IComponentGui get(int id);

	void addButton(IGuiNpcButton button);

	IGuiNpcButton addButton(int id, int x, int y, int width, int height, int textureX, int textureY, ResourceLocation texture);

	IGuiNpcButton addButton(int id, int x, int y, int width, int height, int val, String... display);

	IGuiNpcButton addButton(int id, int x, int y, int width, int height, String label);

	IGuiNpcButton addButton(int id, int x, int y, int width, int height, String label, boolean enabled);

	IGuiNpcButton addButton(int id, int x, int y, int width, int height, String[] display, int val);

	IGuiNpcButton addButton(int id, int x, int y, String label);

	IGuiNpcButton addButton(int id, int x, int y, String[] display, int val);

	void drawMainScreen(int mouseX, int mouseY, float partialTicks);

	void drawWait();

	IGuiTextArea getTextArea(int id);

	IGuiNpcButton getButton(int id);

	IGuiNpcCheckBox addCheckBox(int id, int x, int y, int width, int height, String trueLabel, String falseLabel);

	IGuiNpcCheckBox addCheckBox(int id, int x, int y, int width, int height, String trueLabel, String falseLabel, boolean select);

	IGuiNpcCheckBox addCheckBox(int id, int x, int y, String trueLabel, String falseLabel, boolean select);

	void addLabel(IGuiNpcLabel label);

	IGuiNpcLabel getLabel(int id);

	IGuiNpcLabel addLabel(int id, Object label, int x, int y, int color);

	IGuiNpcLabel addLabel(int id, Object label, int x, int y);

	void addScroll(IGuiCustomScroll scroll);

	ResourceLocation getResource(String texture);

	IGuiCustomScroll getScroll(int id);

	IGuiCustomScroll addScroll(ICustomScrollListener parent, int scrollId);

	IGuiCustomScroll addScroll(ICustomScrollListener parent, boolean setSearch, int id);

	IGuiCustomScroll addScroll(ICustomScrollListener parent, int id, boolean isMultipleSelection);

	void addSideButton(IGuiMenuSideButton slider);

	IGuiMenuSideButton getSideButton(int id);

	void addSlider(IGuiNpcSlider slider);

	IGuiNpcSlider getSlider(int id);

	void addTextField(IGuiNpcTextField textField);

	void addTopButton(IGuiMenuTopButton button);

	void addMiniWindow(IGuiNpcMiniWindow miniwindows);

	void drawNpc(Entity entity, int x, int y, float zoomed, int rotation, int vertical, int mouseFocus);

	void drawNpc(int x, int y);

	void initGui();

	INpc getNpc();

	void setNpc(INpc iNpc);

	void setSubGui(ISubGuiInterface gui);

	boolean hasHoverText();

	void setWorldAndResolution(Minecraft mc, int width, int height);

	boolean isMouseHover(int mX, int mY, int px, int py, int pwidth, int pheight);

	void keyTyped(char c, int id);

	void mouseClicked(int mouseX, int mouseY, int mouseButton);

	void updateScreen();

	void save();

}
