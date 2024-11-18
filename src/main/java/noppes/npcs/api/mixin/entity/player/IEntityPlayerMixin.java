package noppes.npcs.api.mixin.entity.player;

import net.minecraft.util.math.BlockPos;

public interface IEntityPlayerMixin {

    void npcs$setSpawnPos(BlockPos newSpawnPos);

}
