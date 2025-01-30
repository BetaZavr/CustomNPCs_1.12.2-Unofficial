package noppes.npcs.api.handler;

import noppes.npcs.api.IPos;
import noppes.npcs.api.handler.data.IBorder;
import noppes.npcs.controllers.data.Zone3D;

import java.util.List;

public interface IBorderHandler {

	IBorder createNew(int dimID, IPos pos);

	IBorder[] getAllRegions();

	IBorder getRegion(int regionId);

	IBorder[] getRegions(int dimID);

	boolean removeRegion(int regionId);

	List<Zone3D> getNearestRegions(int dimensionID, double xPos, double yPos, double zPos, double distance);

}
