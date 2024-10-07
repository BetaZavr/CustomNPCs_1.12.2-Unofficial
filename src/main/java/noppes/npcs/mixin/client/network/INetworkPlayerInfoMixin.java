package noppes.npcs.mixin.client.network;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.util.ResourceLocation;

import java.util.Map;

public interface INetworkPlayerInfoMixin {

    Map<MinecraftProfileTexture.Type, ResourceLocation> npcs$getPlayerTextures();

}
