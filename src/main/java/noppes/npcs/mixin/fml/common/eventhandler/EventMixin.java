package noppes.npcs.mixin.fml.common.eventhandler;

import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.api.NpcAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = Event.class, remap = false, priority = 499)
public class EventMixin {

    @Unique
    @SuppressWarnings("all")
    public NpcAPI API = NpcAPI.Instance();

}
