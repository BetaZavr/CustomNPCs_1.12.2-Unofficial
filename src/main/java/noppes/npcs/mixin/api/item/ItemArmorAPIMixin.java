package noppes.npcs.mixin.api.item;

import net.minecraft.item.ItemArmor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ItemArmor.class)
public interface ItemArmorAPIMixin {

    @Mutable
    @Accessor(value="damageReduceAmount")
    void npcs$setDamageReduceAmount(int newDamageReduceAmount);

    @Mutable
    @Accessor(value="toughness")
    void npcs$setToughness(float newToughness);

}
