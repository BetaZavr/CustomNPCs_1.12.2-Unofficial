package noppes.npcs.mixin.api.client.resources;

import net.minecraft.client.resources.ResourceIndex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;
import java.util.Map;

@Mixin(value = ResourceIndex.class)
public interface ResourceIndexAPIMixin {

    @Accessor(value="resourceMap")
    Map<String, File> npcs$getResourceMap();

}
