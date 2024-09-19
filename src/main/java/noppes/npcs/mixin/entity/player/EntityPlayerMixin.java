package noppes.npcs.mixin.entity.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.mixin.api.entity.player.EntityPlayerAPIMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntityPlayer.class)
public abstract class EntityPlayerMixin implements EntityPlayerAPIMixin {

    @Mutable
    @Shadow(aliases = "spawnPos")
    protected BlockPos spawnPos;

    @Accessor(value = "bedLocation")
    public void npcs$setSpawnPos(BlockPos newSpawnPos) {
        spawnPos = newSpawnPos;
    }

}
