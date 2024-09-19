package noppes.npcs.mixin.api.client.gui;

import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiYesNo.class)
public interface GuiYesNoAPIMixin {

    @Accessor(value="parentScreen")
    GuiYesNoCallback npcs$getParentScreen();

    @Accessor(value="parentButtonClickedId")
    int npcs$getParentButtonClickedId();
}
