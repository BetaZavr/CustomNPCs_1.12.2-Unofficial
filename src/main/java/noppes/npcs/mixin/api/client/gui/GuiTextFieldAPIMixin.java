package noppes.npcs.mixin.api.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiTextField.class)
public interface GuiTextFieldAPIMixin {

    @Accessor(value="enableBackgroundDrawing")
    boolean npcs$getEnableBackgroundDrawing();

    @Accessor(value="canLoseFocus")
    boolean npcs$getCanLoseFocus();

    @Accessor(value="fontRenderer")
    FontRenderer npcs$getFontRenderer();

    @Accessor(value="lineScrollOffset")
    int npcs$getLineScrollOffset();

    @Mutable
    @Accessor(value="lineScrollOffset")
    void npcs$setCursorPosition(int newCursorPosition);

    @Mutable
    @Accessor(value="selectionEnd")
    void npcs$setSelectionEnd(int newSelectionEnd);

}
