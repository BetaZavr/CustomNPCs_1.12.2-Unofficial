package noppes.npcs.mixin.api.client.network;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = NetworkPlayerInfo.class)
public interface NetworkPlayerInfoAPIMixin {

    @Accessor(value="playerTextures")
    Map<MinecraftProfileTexture.Type, ResourceLocation> npcs$getPlayerTextures();

}
