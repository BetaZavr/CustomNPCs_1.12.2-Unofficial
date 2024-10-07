package noppes.npcs.mixin.client.gui;

import net.minecraft.client.gui.FontRenderer;

public interface IGuiTextFieldMixin {

    boolean npcs$getEnableBackgroundDrawing();

    boolean npcs$getCanLoseFocus();

    FontRenderer npcs$getFontRenderer();

    int npcs$getLineScrollOffset();

    void npcs$setCursorPosition(int newCursorPosition);

    void npcs$setSelectionEnd(int newSelectionEnd);

}
