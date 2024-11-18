package noppes.npcs.api.mixin.item;

import net.minecraft.item.Item;

public interface IItemSwordMixin {

    float npcs$getAttackDamage();

    void npcs$setAttackDamage(float newAttackDamage);

    Item.ToolMaterial npcs$getMaterial();

}
