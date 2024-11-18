package noppes.npcs.api.mixin.client.renderer;

import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.ChestRenderer;

public interface IBlockRendererDispatcherMixin {

    BlockModelRenderer npcs$getBlockModelRenderer();

    ChestRenderer npcs$getChestRenderer();

}
