package noppes.npcs.mixin.entity.item;

import net.minecraft.entity.item.EntityItem;
import noppes.npcs.api.mixin.entity.item.IEntityItemMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityItem.class)
public class EntityItemMixin implements IEntityItemMixin {

    @Mutable
    @Shadow
    private int age;

    @Override
    public int npcs$getAge() { return age; }

    @Override
    public void npcs$setAge(int newAge) { age = newAge; }

}
