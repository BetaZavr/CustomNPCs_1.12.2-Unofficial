package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.INpc;
import noppes.npcs.entity.EntityNPCInterface;

public interface ISubGuiInterface extends IEditNPC {

    int getId();

    GuiScreen getParent();

    void setParent(GuiScreen gui);

    Object getObject();

    void setObject(Object obj);

    void drawScreen(int mouseX, int mouseY, float partialTicks);

    void elementClicked();

}
