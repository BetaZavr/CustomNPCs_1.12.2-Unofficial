package noppes.npcs.mixin.world;

import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = World.class, priority = 499)
public class WorldMixin {

    @Inject(method = "updateBlockTick", at = @At("HEAD"))
    public void npcs$updateBlockTickStart(CallbackInfo ci) { CustomNpcs.debugData.start(null); }

    @Inject(method = "updateBlockTick", at = @At("TAIL"))
    public void npcs$updateBlockTickEnd(CallbackInfo ci) { CustomNpcs.debugData.end(null); }

    @Inject(method = "updateEntities", at = @At("HEAD"))
    public void npcs$updateEntitiesStart(CallbackInfo ci) { CustomNpcs.debugData.start(null); }

    @Inject(method = "updateEntities", at = @At("TAIL"))
    public void npcs$updateEntitiesEnd(CallbackInfo ci) { CustomNpcs.debugData.end(null); }

    @Inject(method = "tickPlayers", at = @At("HEAD"))
    public void npcs$tickPlayersStart(CallbackInfo ci) { CustomNpcs.debugData.start(null); }

    @Inject(method = "tickPlayers", at = @At("TAIL"))
    public void npcs$tickPlayersEnd(CallbackInfo ci) { CustomNpcs.debugData.end(null); }

}
