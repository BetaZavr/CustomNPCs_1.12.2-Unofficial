package noppes.npcs.api.handler;

import noppes.npcs.api.INbt;
import noppes.npcs.api.handler.data.IWorldInfo;

public interface IDimensionHandler {

	IWorldInfo createDimension();

	void deleteDimension(int dimensionID);

	int[] getAllIDs();

	IWorldInfo getMCWorldInfo(int dimensionID);

	INbt getNbt();

	void setNbt(INbt nbt);

}
