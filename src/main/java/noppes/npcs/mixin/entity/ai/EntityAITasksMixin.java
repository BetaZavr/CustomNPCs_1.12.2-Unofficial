package noppes.npcs.mixin.entity.ai;

import net.minecraft.entity.ai.EntityAITasks;
import noppes.npcs.mixin.api.entity.ai.EntityAITasksAPIMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityAITasks.class)
public class EntityAITasksMixin implements EntityAITasksAPIMixin {

    @Shadow(aliases = "tickRate")
    private int tickRate;

    @Override
    public int npcs$getTickRate() { return tickRate; }

}
