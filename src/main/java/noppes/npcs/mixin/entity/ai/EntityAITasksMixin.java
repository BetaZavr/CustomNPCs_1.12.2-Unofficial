package noppes.npcs.mixin.entity.ai;

import net.minecraft.entity.ai.EntityAITasks;
import noppes.npcs.api.mixin.entity.ai.IEntityAITasksMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityAITasks.class)
public class EntityAITasksMixin implements IEntityAITasksMixin {

    @Shadow
    private int tickRate;

    @Override
    public int npcs$getTickRate() { return tickRate; }

}
