package noppes.npcs.api.handler;

import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.handler.data.IBorder;

public interface IBorderHandler {

	IBorder getRegion(int regionId);

	INbt getNbt();

	boolean removeRegion(int regionId);

	IBorder[] getRegions(int dimensionID);

	IBorder[] getAllRegions();

	void sendToAll(int id);

	IBorder createNew(int imensionID, IPos pos);

}
