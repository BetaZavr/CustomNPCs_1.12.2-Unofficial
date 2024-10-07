package noppes.npcs.mixin.client.resources;

import net.minecraft.client.resources.FallbackResourceManager;

import java.util.Map;

public interface ISimpleReloadableResourceManagerMixin {

    Map<String, FallbackResourceManager> npcs$getDomainResourceManagers();

}
