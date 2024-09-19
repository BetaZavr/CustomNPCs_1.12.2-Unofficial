package noppes.npcs.mixin.api.entity.player;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntityPlayer.class)
public interface EntityPlayerAPIMixin {

    @Accessor(value="spawnPos")
    @Mutable
    void npcs$setSpawnPos(BlockPos newSpawnPos);

}
