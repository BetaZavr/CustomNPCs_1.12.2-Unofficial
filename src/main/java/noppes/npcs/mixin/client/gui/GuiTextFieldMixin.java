package noppes.npcs.mixin.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import noppes.npcs.mixin.api.client.gui.GuiTextFieldAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GuiTextField.class)
public class GuiTextFieldMixin implements GuiTextFieldAPIMixin {

    @Final
    @Shadow(aliases = "fontRenderer")
    private FontRenderer fontRenderer;

    @Shadow(aliases = "enableBackgroundDrawing")
    private boolean enableBackgroundDrawing = true;

    @Shadow(aliases = "canLoseFocus")
    private boolean canLoseFocus = true;

    @Shadow(aliases = "lineScrollOffset")
    private int lineScrollOffset;

    @Mutable
    @Shadow(aliases = "cursorPosition")
    private int cursorPosition;

    @Mutable
    @Shadow(aliases = "selectionEnd")
    private int selectionEnd;

    @Override
    public boolean npcs$getEnableBackgroundDrawing() { return enableBackgroundDrawing; }

    @Override
    public boolean npcs$getCanLoseFocus() { return canLoseFocus; }

    @Override
    public FontRenderer npcs$getFontRenderer() { return fontRenderer; }

    @Override
    public int npcs$getLineScrollOffset() { return lineScrollOffset; }

    @Override
    public void npcs$setCursorPosition(int newCursorPosition) {
        if (newCursorPosition < 0) { newCursorPosition *= -1; }
        cursorPosition = newCursorPosition;
    }

    @Override
    public void npcs$setSelectionEnd(int newSelectionEnd) {
        if (newSelectionEnd < 0) { newSelectionEnd *= -1; }
        selectionEnd = newSelectionEnd;
    }
}
