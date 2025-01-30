package noppes.npcs.api.mixin.client.gui;

import net.minecraft.client.gui.GuiButton;

import java.util.List;

public interface IGuiScreenMixin {

    int npcs$getEventButton();

    List<GuiButton> npcs$getButtonList();

}
