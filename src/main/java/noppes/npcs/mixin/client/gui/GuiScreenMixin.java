package noppes.npcs.mixin.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.api.mixin.client.gui.IGuiScreenMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

// Used by NPC GUI
@Mixin(value = GuiScreen.class)
public class GuiScreenMixin implements IGuiScreenMixin {

    @Shadow
    public List<GuiButton> buttonList;

    @Shadow
    private int eventButton;

    @Override
    public int npcs$getEventButton() { return eventButton; }

    @Override
    public List<GuiButton> npcs$getButtonList() { return buttonList; }

}
