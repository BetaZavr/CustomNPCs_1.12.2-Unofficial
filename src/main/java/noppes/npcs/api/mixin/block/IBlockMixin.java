package noppes.npcs.api.mixin.block;

import net.minecraft.block.state.BlockStateContainer;

public interface IBlockMixin {

    void npcs$setBlockState(BlockStateContainer newBlockState);

}
