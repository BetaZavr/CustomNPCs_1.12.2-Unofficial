package noppes.npcs.mixin.server;

import net.minecraft.server.MinecraftServer;
import noppes.npcs.CustomNpcs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinecraftServer.class, priority = 499)
public class MinecraftServerMixin {

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void npcs$stopServer(CallbackInfo ci) { CustomNpcs.debugData.logging(); }

    @Inject(method = "tick", at = @At("HEAD"))
    public void npcs$tickStart(CallbackInfo ci) { CustomNpcs.debugData.start(null); }

    @Inject(method = "tick", at = @At("TAIL"))
    public void npcs$tickEnd(CallbackInfo ci) { CustomNpcs.debugData.end(null); }

}
