package noppes.npcs.mixin.client.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiEditArray;
import noppes.npcs.mixin.api.client.config.GuiEditArrayAPIMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GuiEditArray.class, remap = false)
public class GuiEditArrayMixin implements GuiEditArrayAPIMixin {

    @Shadow(aliases = "parentScreen")
    protected GuiScreen parentScreen;

    @Shadow(aliases = "slotIndex")
    protected int slotIndex;

    @Shadow(aliases = "enabled")
    protected boolean enabled;

    @Override
    public GuiScreen npcs$getParentScreen() { return parentScreen; }

    @Override
    public int npcs$getSlotIndex() { return slotIndex; }

    @Override
    public boolean npcs$getEnabled() { return enabled; }

}
