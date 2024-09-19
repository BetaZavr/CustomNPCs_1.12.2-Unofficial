package noppes.npcs.mixin.client.gui;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.mixin.api.client.gui.GuiScreenAPIMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GuiScreen.class)
public class GuiScreenMixin implements GuiScreenAPIMixin {

    @Shadow(aliases = "eventButton")
    private int eventButton;

    @Override
    public int npcs$getEventButton() { return eventButton; }

}
