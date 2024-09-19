package noppes.npcs.mixin.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Block.class)
public interface BlockAPIMixin {

    @Mutable
    @Accessor(value="blockState")
    void npcs$setBlockState(BlockStateContainer newBlockState);

}
