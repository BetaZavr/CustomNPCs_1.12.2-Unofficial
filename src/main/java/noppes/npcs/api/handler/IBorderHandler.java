package noppes.npcs.api.handler;

import noppes.npcs.api.IPos;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.handler.data.IBorder;
import noppes.npcs.controllers.data.Zone3D;

import java.util.List;

@SuppressWarnings("all")
public interface IBorderHandler {

	IBorder createNew(@ParamName("dimensionId") int dimensionId, @ParamName("pos") IPos pos);

	IBorder[] getAllRegions();

	IBorder getRegion(@ParamName("regionId") int regionId);

	IBorder[] getRegions(@ParamName("dimensionId") int dimensionId);

	boolean removeRegion(@ParamName("regionId") int regionId);

	List<Zone3D> getNearestRegions(@ParamName("dimensionId") int dimensionId,
								   @ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z,
								   @ParamName("distance") double distance);

}
