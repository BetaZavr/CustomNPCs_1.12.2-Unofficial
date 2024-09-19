package noppes.npcs.mixin.api.client.gui;

import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiScreen.class)
public interface GuiScreenAPIMixin {

    @Accessor(value="eventButton")
    int npcs$getEventButton();

}
