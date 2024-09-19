package noppes.npcs.mixin.client.gui;

import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import noppes.npcs.mixin.api.client.gui.GuiYesNoAPIMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GuiYesNo.class)
public class GuiYesNoMixin implements GuiYesNoAPIMixin {

    @Shadow(aliases = "parentScreen")
    protected GuiYesNoCallback parentScreen;

    @Shadow(aliases = "parentScreen")
    protected int parentButtonClickedId;

    @Override
    public GuiYesNoCallback npcs$getParentScreen() { return parentScreen; }

    @Override
    public int npcs$getParentButtonClickedId() { return parentButtonClickedId; }

}
