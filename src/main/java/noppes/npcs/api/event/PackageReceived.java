package noppes.npcs.api.event;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.EventName;
import noppes.npcs.constants.EnumScriptType;

@Cancelable
@EventName(EnumScriptType.PACKAGE_RECEIVED)
public class PackageReceived extends CustomNPCsEvent {

	public Packet<?> message;

	public PackageReceived(Packet<?> msg) { message = msg; }

}
