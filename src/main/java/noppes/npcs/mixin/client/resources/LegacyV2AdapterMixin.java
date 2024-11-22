package noppes.npcs.mixin.client.resources;

import net.minecraft.client.resources.LegacyV2Adapter;
import net.minecraft.client.resources.IResourcePack;
import noppes.npcs.api.mixin.client.resources.ILegacyV2AdapterMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = LegacyV2Adapter.class)
public class LegacyV2AdapterMixin implements ILegacyV2AdapterMixin {

    @Final
    @Shadow
    private IResourcePack pack;

    @Override
    public IResourcePack npcs$getIResourcePack() { return pack; }

}
