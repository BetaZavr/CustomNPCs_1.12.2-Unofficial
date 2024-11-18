package noppes.npcs.mixin.network;

import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import noppes.npcs.EventHooks;
import noppes.npcs.api.event.PackageReceived;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = NetworkManager.class)
public class NetworkManagerMixin {

    // Packets not allowed to be processed:
    @Unique
    private static List<String> npcs$notAllowed = Lists.newArrayList("SPacketTabComplete", "CPacketSpectate", "CPacketKeepAlive");

    @Final
    @Shadow
    private EnumPacketDirection direction;

    @Shadow
    private Channel channel;

    @Inject(method = "channelRead0*", at = @At("HEAD"), cancellable = true)
    private void npcs$channelRead0(ChannelHandlerContext context, Packet<?> packet, CallbackInfo ci) {
        if (channel.isOpen() && !npcs$notAllowed.contains(packet.getClass().getSimpleName())) {
            PackageReceived event = new PackageReceived(packet);
            EventHooks.onPackageReceived(event, direction == EnumPacketDirection.SERVERBOUND);
            if (event.isCanceled()) { ci.cancel(); }
        }
    }

}
