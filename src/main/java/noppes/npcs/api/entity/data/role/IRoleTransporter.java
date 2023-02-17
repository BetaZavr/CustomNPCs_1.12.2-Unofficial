package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.entity.data.INPCRole;

public interface IRoleTransporter extends INPCRole {
	public interface ITransportLocation {
		int getDimension();

		int getId();

		String getName();

		int getType();

		int getX();

		int getY();

		int getZ();
	}

	ITransportLocation getLocation();
}
