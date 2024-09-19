package noppes.npcs.mixin.api.client.resources;

import net.minecraft.client.resources.AbstractResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(value = AbstractResourcePack.class)
public interface AbstractResourcePackAPIMixin {

    @Accessor(value="resourcePackFile")
    File npcs$getResourcePackFile();

}
