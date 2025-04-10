package noppes.npcs.mixin.fml.common.eventhandler;

import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.InitNoiseGensEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.api.event.CustomNPCsEvent;
import noppes.npcs.api.event.ForgeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EventBus.class, remap = false)
public class EventBusMixin {

    @Shadow
    private boolean shutdown;

    @Inject(method = "post", at = @At("RETURN"), cancellable = true)
    public void cnpcs$post(Event event, CallbackInfoReturnable<Boolean> cir) {
        if (shutdown || !CustomNpcs.isLoaded ||
                event instanceof CustomNPCsEvent ||
                event instanceof GenericEvent ||
                event instanceof InitNoiseGensEvent ||
                event instanceof InitMapGenEvent ||
                event instanceof EntityEvent.EntityConstructing ||
                event instanceof WorldEvent.PotentialSpawns) { return; }
        EventHooks.onForgeEvent(new ForgeEvent(event));
        if (event.isCancelable()) {
            cir.setReturnValue(event.isCanceled());
        }
    }

}
