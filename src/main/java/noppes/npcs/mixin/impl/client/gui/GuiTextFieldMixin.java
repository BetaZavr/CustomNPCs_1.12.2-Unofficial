package noppes.npcs.mixin.impl.client.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import noppes.npcs.mixin.client.gui.IGuiTextFieldMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

// Used by custom GUI
@Mixin(value = GuiTextField.class)
public class GuiTextFieldMixin implements IGuiTextFieldMixin {

    @Final
    @Shadow
    private FontRenderer fontRenderer;

    @Shadow
    private boolean enableBackgroundDrawing = true;

    @Shadow
    private boolean canLoseFocus = true;

    @Shadow
    private int lineScrollOffset;

    @Mutable
    @Shadow
    private int cursorPosition;

    @Mutable
    @Shadow
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
