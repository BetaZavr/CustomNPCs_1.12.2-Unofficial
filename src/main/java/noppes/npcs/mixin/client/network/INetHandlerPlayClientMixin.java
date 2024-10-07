package noppes.npcs.mixin.client.network;

import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.Map;
import java.util.UUID;

public interface INetHandlerPlayClientMixin {

    Map<UUID, NetworkPlayerInfo> npcs$getplayerInfoMap();

}
