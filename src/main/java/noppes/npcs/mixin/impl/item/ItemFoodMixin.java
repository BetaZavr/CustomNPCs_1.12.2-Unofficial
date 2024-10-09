package noppes.npcs.mixin.impl.item;

import net.minecraft.item.ItemFood;
import noppes.npcs.mixin.item.IItemFoodMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ItemFood.class)
public class ItemFoodMixin implements IItemFoodMixin {

    @Mutable
    @Final
    @Shadow
    public int itemUseDuration;

    @Override
    public void npcs$setItemUseDuration(int newItemUseDuration) {
        if (newItemUseDuration < 0) { newItemUseDuration *= -1; }
        if (newItemUseDuration < 1) { newItemUseDuration = 1; }
        itemUseDuration = newItemUseDuration;
    }

}
