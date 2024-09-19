package noppes.npcs.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBanner;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespacedDefaultedByKey;
import noppes.npcs.LogWriter;
import noppes.npcs.blocks.BlockCustomBanner;
import noppes.npcs.mixin.api.block.BlockAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Block.class)
public abstract class BlockMixin implements BlockAPIMixin {

    @Mutable
    @Final
    @Shadow(aliases = "blockState")
    protected BlockStateContainer blockState;

    @Final
    @Shadow(aliases = "blockState")
    public static RegistryNamespacedDefaultedByKey<ResourceLocation, Block> REGISTRY;

    @Override
    public void npcs$setBlockState(BlockStateContainer newBlockState) {
        if (blockState == null) { return; }
        blockState = newBlockState;
    }

    @Inject(method = "registerBlock*", at = @At("HEAD"), cancellable = true)
    private void npcs$registerBlock(int id, ResourceLocation textualID, Block block_, CallbackInfo ci) {
        if (textualID != null) {
            if (textualID.getResourcePath().equals("standing_banner") && block_ instanceof BlockBanner.BlockBannerStanding) {
                LogWriter.info("Register Custom Block Banner Standing");
                REGISTRY.register(id, textualID, new BlockCustomBanner.BlockBannerStanding());
                ci.cancel();
            }
            else if (textualID.getResourcePath().equals("wall_banner") && block_ instanceof BlockBanner.BlockBannerHanging) {
                LogWriter.info("Register Custom Block Banner Wall");
                REGISTRY.register(id, textualID, new BlockCustomBanner.BlockBannerHanging());
                ci.cancel();
            }
        }
    }

}
