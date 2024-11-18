package noppes.npcs.mixin.client.resources;

import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourceIndex;
import noppes.npcs.api.mixin.client.resources.IDefaultResourcePackMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = DefaultResourcePack.class)
public class DefaultResourcePackMixin implements IDefaultResourcePackMixin {

    @Final
    @Shadow
    private ResourceIndex resourceIndex;

    @Override
    public ResourceIndex npcs$getResourceIndex() { return resourceIndex; }

}
