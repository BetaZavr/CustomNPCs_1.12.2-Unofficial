package noppes.npcs.mixin.api.client.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiEditArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiEditArray.class, remap = false)
public interface GuiEditArrayAPIMixin {

    @Accessor(value="parentScreen")
    GuiScreen npcs$getParentScreen();

    @Accessor(value="slotIndex")
    int npcs$getSlotIndex();

    @Accessor(value="enabled")
    boolean npcs$getEnabled();

}
