package noppes.npcs.mixin.api.client.network;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

@Mixin(value = NetHandlerPlayClient.class)
public interface NetHandlerPlayClientAPIMixin {

    @Accessor(value="playerInfoMap")
    Map<UUID, NetworkPlayerInfo> npcs$getplayerInfoMap();

}
