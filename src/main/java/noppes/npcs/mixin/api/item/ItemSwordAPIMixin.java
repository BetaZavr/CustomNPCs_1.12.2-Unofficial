package noppes.npcs.mixin.api.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ItemSword.class)
public interface ItemSwordAPIMixin {

    @Accessor(value="attackDamage")
    float npcs$getAttackDamage();

    @Mutable
    @Accessor(value="attackDamage")
    void npcs$setAttackDamage(float newAttackDamage);

    @Accessor(value="material")
    Item.ToolMaterial npcs$getMaterial();

}
