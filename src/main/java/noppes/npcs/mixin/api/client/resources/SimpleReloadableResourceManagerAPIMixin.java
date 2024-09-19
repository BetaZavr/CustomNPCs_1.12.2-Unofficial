package noppes.npcs.mixin.api.client.resources;

import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = SimpleReloadableResourceManager.class)
public interface SimpleReloadableResourceManagerAPIMixin {

    @Accessor(value="domainResourceManagers")
    Map<String, FallbackResourceManager> npcs$getDomainResourceManagers();

}
