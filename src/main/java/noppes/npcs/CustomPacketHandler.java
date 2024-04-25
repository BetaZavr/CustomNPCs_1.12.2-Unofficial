package noppes.npcs;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import noppes.npcs.api.event.PackageReceived;

public class CustomPacketHandler extends ChannelInboundHandlerAdapter {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		PackageReceived ev = new PackageReceived(ctx, msg,
				ctx.name().toString().equals(CustomNpcs.MODID + ":custom_packet_handler_server"));
		EventHooks.onPackageReceived(ev);
		if (ev.isCanceled()) {
			return;
		}
		super.channelRead(ctx, msg);
	}

}
