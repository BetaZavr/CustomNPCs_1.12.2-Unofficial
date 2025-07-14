package noppes.npcs.mixin.server;

import net.minecraft.server.MinecraftServer;
import noppes.npcs.CustomNpcs;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MinecraftServer.class, priority = 499)
public class MinecraftServerMixin {

    @Final
    @Shadow
    private static Logger LOGGER;

    @Inject(method = "stopServer", at = @At("HEAD"))
    private void npcs$stopServer(CallbackInfo ci) {
        LOGGER.info("CustomNPCs: Debug data showing...");
        CustomNpcs.debugData.logging(LOGGER);
    }

}
