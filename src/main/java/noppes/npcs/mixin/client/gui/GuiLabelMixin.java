package noppes.npcs.mixin.client.gui;

import net.minecraft.client.gui.GuiLabel;
import noppes.npcs.mixin.api.client.gui.GuiLabelAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = GuiLabel.class)
public class GuiLabelMixin implements GuiLabelAPIMixin {

    @Final
    @Shadow(aliases = "labels")
    private List<String> labels;

    @Shadow(aliases = "centered")
    private boolean centered;

    @Shadow(aliases = "border")
    private int border;

    @Override
    public int npcs$getBorder() { return border; }

    @Override
    public boolean npcs$getCentered() { return centered; }

    @Override
    public List<String> npcs$getLabels() { return labels; }

}
