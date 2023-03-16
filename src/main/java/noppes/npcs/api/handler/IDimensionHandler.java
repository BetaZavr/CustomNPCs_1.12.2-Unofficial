package noppes.npcs.api.handler;

import noppes.npcs.api.INbt;
import noppes.npcs.api.handler.data.IWorldInfo;

public interface IDimensionHandler {

	IWorldInfo createDimension();

	void setNbt(INbt nbt);

	INbt getNbt();

	IWorldInfo getMCWorldInfo(int dimensionID);

	int[] getAllIDs();

	void deleteDimension(int dimensionID);

}
