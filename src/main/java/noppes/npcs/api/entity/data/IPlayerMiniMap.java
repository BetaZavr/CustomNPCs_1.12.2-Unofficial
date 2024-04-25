package noppes.npcs.api.entity.data;

public interface IPlayerMiniMap {

	IMiniMapData addPoint(int dimentionId);

	IMiniMapData[] getAllPoints();

	String getModName();

	IMiniMapData getPoint(int dimentionId, int id);

	IMiniMapData getPoint(int dimentionId, String name);

	IMiniMapData[] getPoints(int dimentionId);

	String[] getSpecificKeys();

	Object getSpecificValue(String key);

	boolean removePoint(int dimentionId, int id);

	boolean removePoint(int dimentionId, String name);

}
