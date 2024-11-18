package noppes.npcs.api.mixin.tileentity;

import net.minecraft.block.Block;

public interface ITileEntityMixin {

    void npcs$setBlockMetadata(int newBlockMetadata);

    void npcs$setBlockType(Block newBlockType);

}
