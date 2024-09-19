package noppes.npcs.mixin.tileentity;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import noppes.npcs.LogWriter;
import noppes.npcs.blocks.tiles.TileEntityCustomBanner;
import noppes.npcs.mixin.api.tileentity.TileEntityAPIMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TileEntity.class)
public class TileEntityMixin implements TileEntityAPIMixin {

    @Mutable
    @Shadow(aliases = "blockMetadata")
    private int blockMetadata;

    @Mutable
    @Shadow(aliases = "blockType")
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

    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void npcs$register(String id, Class<? extends TileEntity> clazz, CallbackInfo ci) {
        if (id != null && id.equals("banner") && clazz == TileEntityBanner.class) {
            LogWriter.info("Register Custom Tile Banner");
            TileEntity.register(id, TileEntityCustomBanner.class); // Register your banner class
            ci.cancel(); // Cancel vanilla registration
        }
    }

}
