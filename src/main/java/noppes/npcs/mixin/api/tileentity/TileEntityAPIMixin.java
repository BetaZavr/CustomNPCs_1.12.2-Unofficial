package noppes.npcs.mixin.api.tileentity;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = TileEntity.class)
public interface TileEntityAPIMixin {

    @Mutable
    @Accessor(value="blockMetadata")
    void npcs$setBlockMetadata(int newBlockMetadata);

    @Mutable
    @Accessor(value="blockType")
    void npcs$setBlockType(Block newBlockType);

}
