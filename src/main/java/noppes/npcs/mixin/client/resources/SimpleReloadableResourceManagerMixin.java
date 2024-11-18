package noppes.npcs.mixin.client.resources;

import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import noppes.npcs.api.mixin.client.resources.ISimpleReloadableResourceManagerMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = SimpleReloadableResourceManager.class)
public class SimpleReloadableResourceManagerMixin implements ISimpleReloadableResourceManagerMixin {

    @Final
    @Shadow
    private Map<String, FallbackResourceManager> domainResourceManagers;

    @Override
    public Map<String, FallbackResourceManager> npcs$getDomainResourceManagers() { return domainResourceManagers; }

}
