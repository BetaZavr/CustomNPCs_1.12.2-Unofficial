package noppes.npcs.mixin.impl.tileentity;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import noppes.npcs.mixin.tileentity.ITileEntityMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = TileEntity.class)
public class TileEntityMixin implements ITileEntityMixin {

    @Mutable
    @Shadow
    private int blockMetadata;

    @Mutable
    @Shadow
    protected Block blockType;

    @Override
    public void npcs$setBlockMetadata(int newBlockMetadata) {
        if (newBlockMetadata < 0) { newBlockMetadata *= -1; }
        blockMetadata = newBlockMetadata;
    }

    @Override
    public void npcs$setBlockType(Block newBlockType) {
        if (newBlockType == null) { return; }
        blockType = newBlockType;
    }

}
