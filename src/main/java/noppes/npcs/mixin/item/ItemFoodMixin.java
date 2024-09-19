package noppes.npcs.mixin.item;

import net.minecraft.item.ItemFood;
import noppes.npcs.mixin.api.item.ItemFoodAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ItemFood.class)
public class ItemFoodMixin implements ItemFoodAPIMixin {

    @Mutable
    @Final
    @Shadow(aliases = "itemUseDuration")
    public int itemUseDuration;

    @Override
    public void npcs$setItemUseDuration(int newItemUseDuration) {
        if (newItemUseDuration < 0) { newItemUseDuration *= -1; }
        if (newItemUseDuration < 1) { newItemUseDuration = 1; }
        itemUseDuration = newItemUseDuration;
    }

}
