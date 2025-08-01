package noppes.npcs.mixin.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiButtonImage;
import net.minecraft.client.gui.inventory.GuiCrafting;
import noppes.npcs.api.mixin.client.gui.IGuiScreenMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GuiCrafting.class, priority = 499)
public class GuiCraftingMixin {

    @Shadow
    private GuiButtonImage recipeButton;

    /**
     * @author BetaZavr
     * @reason Remove extra duplicate button
     */
    @Inject(method = "initGui", at = @At("TAIL"))
    public void npcs$initGui(CallbackInfo ci) {
        List<GuiButton> buttonList = ((IGuiScreenMixin) this).npcs$getButtonList();
        if (buttonList.size() > 1) {
            buttonList.clear();
            buttonList.add(recipeButton);
        }
    }

}
