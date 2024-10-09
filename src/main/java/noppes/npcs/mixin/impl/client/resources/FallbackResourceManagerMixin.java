package noppes.npcs.mixin.impl.client.resources;

import net.minecraft.client.resources.FallbackResourceManager;
import net.minecraft.client.resources.IResourcePack;
import noppes.npcs.mixin.client.resources.IFallbackResourceManagerMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = FallbackResourceManager.class)
public class FallbackResourceManagerMixin implements IFallbackResourceManagerMixin {

    @Final
    @Shadow
    protected List<IResourcePack> resourcePacks;

    @Override
    public List<IResourcePack> npcs$getResourcePacks() { return resourcePacks; }

}
