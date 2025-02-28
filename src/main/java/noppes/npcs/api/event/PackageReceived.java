package noppes.npcs.api.event;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PackageReceived extends CustomNPCsEvent {

	public Packet<?> message;

	public PackageReceived(Packet<?> msg) {
		message = msg;
	}

}
