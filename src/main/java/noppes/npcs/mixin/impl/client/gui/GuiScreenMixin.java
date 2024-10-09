package noppes.npcs.mixin.impl.client.gui;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.mixin.client.gui.IGuiScreenMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

// Used by NPC GUI
@Mixin(value = GuiScreen.class)
public class GuiScreenMixin implements IGuiScreenMixin {

    @Shadow
    private int eventButton;

    @Override
    public int npcs$getEventButton() { return eventButton; }

}
