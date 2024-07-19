package noppes.npcs.api.entity.data.role;

public interface ITransportLocation {

	int getDimension();

	int getId();

	String getName();

	int getType();

	int getX();

	int getY();

	int getZ();

	void setPos(int dimensionID, int x, int y, int z);

	void setType(int type);

}
