package noppes.npcs.api.event;

import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PackageReceived extends CustomNPCsEvent {

	public Object message;

	public PackageReceived(Object msg) {
		this.message = msg;
		CPacketAnimation j;
	}

}
