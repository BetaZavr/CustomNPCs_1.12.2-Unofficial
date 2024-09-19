package noppes.npcs.mixin.client.network;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import noppes.npcs.mixin.api.client.network.NetHandlerPlayClientAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.UUID;

@Mixin(value = NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin implements NetHandlerPlayClientAPIMixin {

    @Final
    @Shadow(aliases = "playerInfoMap")
    private Map<UUID, NetworkPlayerInfo> playerInfoMap;

    @Override
    public Map<UUID, NetworkPlayerInfo> npcs$getplayerInfoMap() { return playerInfoMap; }

}
