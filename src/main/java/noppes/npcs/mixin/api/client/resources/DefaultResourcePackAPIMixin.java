package noppes.npcs.mixin.api.client.resources;

import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourceIndex;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = DefaultResourcePack.class)
public interface DefaultResourcePackAPIMixin {

    @Accessor(value="resourceIndex")
    ResourceIndex npcs$getResourceIndex();

}
