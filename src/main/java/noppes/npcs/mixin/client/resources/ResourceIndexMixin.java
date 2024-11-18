package noppes.npcs.mixin.client.resources;

import net.minecraft.client.resources.ResourceIndex;
import noppes.npcs.api.mixin.client.resources.IResourceIndexMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.util.Map;

@Mixin(value = ResourceIndex.class)
public class ResourceIndexMixin implements IResourceIndexMixin {

    @Final
    @Shadow
    private Map<String, File> resourceMap;

    @Override
    public Map<String, File> npcs$getResourceMap() { return resourceMap; }

}
