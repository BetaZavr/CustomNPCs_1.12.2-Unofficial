package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.ParamName;

public interface ITransportLocation {

	int getDimension();

	int getId();

	String getName();

	int getType();

	int getX();

	int getY();

	int getZ();

	void setPos(@ParamName("dimensionId") int dimensionId,
				@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	void setType(@ParamName("type") int type);

}
