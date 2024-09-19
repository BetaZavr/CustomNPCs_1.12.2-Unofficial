package noppes.npcs.mixin.client.resources;

import net.minecraft.client.resources.ResourceIndex;
import noppes.npcs.mixin.api.client.resources.ResourceIndexAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.util.Map;

@Mixin(value = ResourceIndex.class)
public class ResourceIndexMixin implements ResourceIndexAPIMixin {

    @Final
    @Shadow(aliases = "resourceMap")
    private Map<String, File> resourceMap;

    @Override
    public Map<String, File> npcs$getResourceMap() { return resourceMap; }

}
