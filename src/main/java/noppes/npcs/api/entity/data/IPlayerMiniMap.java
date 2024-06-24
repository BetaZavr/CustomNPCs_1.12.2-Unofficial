package noppes.npcs.api.entity.data;

public interface IPlayerMiniMap {

	IMiniMapData addPoint(int dimentionId);

	IMiniMapData[] getAllPoints();

	String getModName();

	IMiniMapData getPoint(int id);

	IMiniMapData getPoint(String name);

	IMiniMapData[] getPoints(int dimentionId);

	String[] getSpecificKeys();

	Object getSpecificValue(String key);

	boolean removePoint(int id);

	boolean removePoint(String name);
	
	boolean removePoints(int dimentionId);

}
