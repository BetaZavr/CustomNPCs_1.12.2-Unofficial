package noppes.npcs.api.mixin.client.gui;

import net.minecraft.client.gui.GuiYesNoCallback;

public interface IGuiYesNoMixin {

    GuiYesNoCallback npcs$getParentScreen();

    int npcs$getParentButtonClickedId();

}
