package noppes.npcs.mixin.api.client.resources;

import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = FallbackResourceManager.class)
public interface FallbackResourceManagerAPIMixin {

    @Accessor(value="resourcePacks")
    List<IResourcePack> npcs$getResourcePacks();

}
