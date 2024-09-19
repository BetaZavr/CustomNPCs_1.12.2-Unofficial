package noppes.npcs.mixin.api.entity.passive;

import net.minecraft.entity.passive.EntityVillager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntityVillager.class)
public interface EntityVillagerAPIMixin {

    @Accessor(value="careerId")
    int npcs$getCareerID();

}
