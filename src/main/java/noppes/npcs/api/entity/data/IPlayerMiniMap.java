package noppes.npcs.api.entity.data;

public interface IPlayerMiniMap {

	IMiniMapData addPoint(int dimensionId);

	IMiniMapData[] getAllPoints();

	String getModName();

	IMiniMapData getPoint(int id);

	IMiniMapData getPoint(String name);

	IMiniMapData[] getPoints(int dimensionId);

	String[] getSpecificKeys();

	Object getSpecificValue(String key);

	boolean removePoint(int id);

	boolean removePoint(String name);
	
	boolean removePoints(int dimensionId);

}
