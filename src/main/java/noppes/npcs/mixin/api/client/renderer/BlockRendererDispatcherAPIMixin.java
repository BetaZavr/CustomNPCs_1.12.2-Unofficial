package noppes.npcs.mixin.api.client.renderer;

import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.ChestRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = BlockRendererDispatcher.class)
public interface BlockRendererDispatcherAPIMixin {

    @Accessor(value="blockModelRenderer")
    BlockModelRenderer npcs$getBlockModelRenderer();

    @Accessor(value="chestRenderer")
    ChestRenderer npcs$getChestRenderer();

}
