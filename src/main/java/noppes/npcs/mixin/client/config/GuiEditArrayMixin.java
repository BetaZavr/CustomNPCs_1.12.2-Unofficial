package noppes.npcs.mixin.client.config;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.config.GuiEditArray;
import noppes.npcs.api.mixin.client.config.IGuiEditArrayMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = GuiEditArray.class, remap = false)
public class GuiEditArrayMixin implements IGuiEditArrayMixin {

    @Shadow
    protected GuiScreen parentScreen;

    @Shadow
    protected int slotIndex;

    @Shadow
    protected boolean enabled;

    @Override
    public GuiScreen npcs$getParentScreen() { return parentScreen; }

    @Override
    public int npcs$getSlotIndex() { return slotIndex; }

    @Override
    public boolean npcs$getEnabled() { return enabled; }

}
