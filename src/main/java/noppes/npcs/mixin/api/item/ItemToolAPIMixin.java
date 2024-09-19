package noppes.npcs.mixin.api.item;

import net.minecraft.item.ItemTool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ItemTool.class, remap = false)
public interface ItemToolAPIMixin {

    @Mutable
    @Accessor(value="toolClass")
    void npcs$setToolClass(String newToolClass);

}
