package noppes.npcs.mixin.impl.client.renderer;

import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.ChestRenderer;
import noppes.npcs.mixin.client.renderer.IBlockRendererDispatcherMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = BlockRendererDispatcher.class)
public class BlockRendererDispatcherMixin implements IBlockRendererDispatcherMixin {

    @Final
    @Shadow(aliases = "blockModelRenderer")
    private BlockModelRenderer blockModelRenderer;

    @Final
    @Shadow(aliases = "chestRenderer")
    private ChestRenderer chestRenderer;

    @Override
    public BlockModelRenderer npcs$getBlockModelRenderer() { return blockModelRenderer; }

    @Override
    public ChestRenderer npcs$getChestRenderer() { return chestRenderer; }

}
