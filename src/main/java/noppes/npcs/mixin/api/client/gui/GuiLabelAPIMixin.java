package noppes.npcs.mixin.api.client.gui;

import net.minecraft.client.gui.GuiLabel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = GuiLabel.class)
public interface GuiLabelAPIMixin {

    @Accessor(value="border")
    int npcs$getBorder();

    @Accessor(value="centered")
    boolean npcs$getCentered();

    @Accessor(value="labels")
    List<String> npcs$getLabels();

}
