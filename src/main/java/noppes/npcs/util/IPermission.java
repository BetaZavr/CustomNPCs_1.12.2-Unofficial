package noppes.npcs.util;

import noppes.npcs.constants.EnumPacketServer;

public interface IPermission {

	boolean isAllowed(EnumPacketServer enumPacket);

}
