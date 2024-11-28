package noppes.npcs.api.mixin.entity.player;

import net.minecraft.util.math.BlockPos;
import noppes.npcs.entity.data.DataAnimation;

public interface IEntityPlayerMixin {

    void npcs$setSpawnPos(BlockPos newSpawnPos);

    DataAnimation npcs$getAnimation();

}
