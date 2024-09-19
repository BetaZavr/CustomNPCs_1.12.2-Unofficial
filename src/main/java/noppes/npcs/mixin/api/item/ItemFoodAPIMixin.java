package noppes.npcs.mixin.api.item;

import net.minecraft.item.ItemFood;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ItemFood.class)
public interface ItemFoodAPIMixin {

    @Mutable
    @Accessor(value="itemUseDuration")
    void npcs$setItemUseDuration(int newItemUseDuration);

}
