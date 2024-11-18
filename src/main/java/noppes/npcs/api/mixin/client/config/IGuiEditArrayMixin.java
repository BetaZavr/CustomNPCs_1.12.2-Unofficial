package noppes.npcs.api.mixin.client.config;

import net.minecraft.client.gui.GuiScreen;

public interface IGuiEditArrayMixin {

    GuiScreen npcs$getParentScreen();

    int npcs$getSlotIndex();

    boolean npcs$getEnabled();

}
