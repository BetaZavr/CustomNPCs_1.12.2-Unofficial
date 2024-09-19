package noppes.npcs.mixin.client.network;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.mixin.api.client.network.NetworkPlayerInfoAPIMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = NetworkPlayerInfo.class)
public class NetworkPlayerInfoMixin implements NetworkPlayerInfoAPIMixin {

    @Shadow(aliases = "playerTextures")
    Map<MinecraftProfileTexture.Type, ResourceLocation> playerTextures;

    @Override
    public Map<MinecraftProfileTexture.Type, ResourceLocation> npcs$getPlayerTextures() { return playerTextures; }

}
