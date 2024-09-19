package noppes.npcs.mixin.api.entity.ai;

import net.minecraft.entity.ai.EntityAITasks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntityAITasks.class)
public interface EntityAITasksAPIMixin {

    @Accessor(value="tickRate")
    int npcs$getTickRate();

}
