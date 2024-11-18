package noppes.npcs.mixin.client.gui;

import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import noppes.npcs.api.mixin.client.gui.IGuiYesNoMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// Used by custom GUI
@Mixin(value = GuiYesNo.class)
public class GuiYesNoMixin implements IGuiYesNoMixin {

    @Shadow
    protected GuiYesNoCallback parentScreen;

    @Shadow
    protected int parentButtonClickedId;

    @Override
    public GuiYesNoCallback npcs$getParentScreen() { return parentScreen; }

    @Override
    public int npcs$getParentButtonClickedId() { return parentButtonClickedId; }

}
