package noppes.npcs.mixin.client.network;

import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import noppes.npcs.api.mixin.client.network.INetHandlerPlayClientMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.UUID;

@Mixin(value = NetHandlerPlayClient.class, priority = 499)
public class NetHandlerPlayClientMixin implements INetHandlerPlayClientMixin {

    @Final
    @Shadow
    private Map<UUID, NetworkPlayerInfo> playerInfoMap;

    @Override
    public Map<UUID, NetworkPlayerInfo> npcs$getplayerInfoMap() { return playerInfoMap; }

}
