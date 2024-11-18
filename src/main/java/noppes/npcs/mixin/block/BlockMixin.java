package noppes.npcs.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespacedDefaultedByKey;
import noppes.npcs.api.mixin.block.IBlockMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Block.class)
public abstract class BlockMixin implements IBlockMixin {

    @Mutable
    @Final
    @Shadow
    protected BlockStateContainer blockState;

    @Final
    @Shadow
    public static RegistryNamespacedDefaultedByKey<ResourceLocation, Block> REGISTRY;

    @Override
    public void npcs$setBlockState(BlockStateContainer newBlockState) {
        if (blockState == null) { return; }
        blockState = newBlockState;
    }

}
