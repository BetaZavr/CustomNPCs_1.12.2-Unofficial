package noppes.npcs.mixin.impl.client.gui;

import net.minecraft.client.gui.GuiLabel;
import noppes.npcs.mixin.client.gui.IGuiLabelMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

// Used by custom GUI
@Mixin(value = GuiLabel.class)
public class GuiLabelMixin implements IGuiLabelMixin {

    @Final
    @Shadow
    private List<String> labels;

    @Shadow
    private boolean centered;

    @Shadow
    private int border;

    @Override
    public int npcs$getBorder() { return border; }

    @Override
    public boolean npcs$getCentered() { return centered; }

    @Override
    public List<String> npcs$getLabels() { return labels; }

}
