package noppes.npcs.mixin.client.resources;

import net.minecraft.client.resources.AbstractResourcePack;
import noppes.npcs.mixin.api.client.resources.AbstractResourcePackAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;

@Mixin(value = AbstractResourcePack.class)
public class AbstractResourcePackMixin implements AbstractResourcePackAPIMixin {

    @Final
    @Shadow(aliases = "resourcePackFile")
    protected File resourcePackFile;

    @Override
    public File npcs$getResourcePackFile() { return resourcePackFile; }

}
