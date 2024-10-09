package noppes.npcs.mixin.impl.entity.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.mixin.entity.player.IEntityPlayerMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityPlayer.class)
public abstract class EntityPlayerMixin implements IEntityPlayerMixin {

    @Mutable
    @Shadow
    protected BlockPos spawnPos;

    @Override
    public void npcs$setSpawnPos(BlockPos newSpawnPos) {
        spawnPos = newSpawnPos;
    }

}
