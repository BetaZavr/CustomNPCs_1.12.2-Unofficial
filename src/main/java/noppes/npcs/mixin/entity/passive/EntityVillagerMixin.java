package noppes.npcs.mixin.entity.passive;

import net.minecraft.entity.passive.EntityVillager;
import noppes.npcs.api.mixin.entity.passive.IEntityVillagerMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityVillager.class)
public class EntityVillagerMixin implements IEntityVillagerMixin {

    @Shadow
    private int careerId;

    @Override
    public int npcs$getCareerID() { return careerId; }

}
