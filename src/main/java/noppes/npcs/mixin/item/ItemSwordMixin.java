package noppes.npcs.mixin.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;
import noppes.npcs.api.mixin.item.IItemSwordMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = ItemSword.class)
public class ItemSwordMixin implements IItemSwordMixin {

    @Mutable
    @Final
    @Shadow
    private float attackDamage;

    @Final
    @Shadow
    private Item.ToolMaterial material;

    @Override
    public float npcs$getAttackDamage() { return attackDamage; }

    @Override
    public void npcs$setAttackDamage(float newAttackDamage) {
        if (newAttackDamage < 0.0f) { newAttackDamage = 0.0f; }
        attackDamage = newAttackDamage;
    }

    @Override
    public Item.ToolMaterial npcs$getMaterial() { return material; }

}
