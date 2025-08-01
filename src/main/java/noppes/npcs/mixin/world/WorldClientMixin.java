package noppes.npcs.mixin.world;

import net.minecraft.client.multiplayer.WorldClient;
import noppes.npcs.CustomNpcs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WorldClient.class, priority = 499)
public class WorldClientMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    public void npcs$tickStart(CallbackInfo ci) { CustomNpcs.debugData.start(null); }

    @Inject(method = "tick", at = @At("TAIL"))
    public void npcs$tickEnd(CallbackInfo ci) { CustomNpcs.debugData.end(null); }

}
