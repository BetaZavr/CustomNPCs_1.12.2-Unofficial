package noppes.npcs.api.handler;

import noppes.npcs.api.IPos;
import noppes.npcs.api.handler.data.IBorder;

public interface IBorderHandler {

	IBorder createNew(int dimID, IPos pos);

	IBorder[] getAllRegions();

	IBorder getRegion(int regionId);

	IBorder[] getRegions(int dimID);

	boolean removeRegion(int regionId);

}
