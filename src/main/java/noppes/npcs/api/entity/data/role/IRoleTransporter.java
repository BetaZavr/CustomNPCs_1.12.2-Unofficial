package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.entity.data.INPCRole;

public interface IRoleTransporter
extends INPCRole {

	ITransportLocation getLocation();
}
