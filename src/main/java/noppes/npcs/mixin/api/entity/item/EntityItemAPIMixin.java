package noppes.npcs.mixin.api.entity.item;

import net.minecraft.entity.item.EntityItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntityItem.class)
public interface EntityItemAPIMixin {

    @Accessor(value="age")
    int npcs$getAge();

    @Accessor(value="age")
    void npcs$setAge(int newAge);

}
