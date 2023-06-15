package noppes.npcs.api.event;

import io.netty.channel.ChannelHandlerContext;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PackageReceived extends CustomNPCsEvent {
	
	public ChannelHandlerContext channel;
	public boolean side;
	public Object message;

	public PackageReceived(ChannelHandlerContext ctx, Object msg, boolean side) {
		this.channel = ctx;
		this.message = msg;
		this.side = side;
	}
	
}
