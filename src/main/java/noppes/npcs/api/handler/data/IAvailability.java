package noppes.npcs.api.handler.data;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface IAvailability {

	int[] getDaytime();

	int getHealth();

	int getHealthType();

	int getMinPlayerLevel();

	String[] getPlayerNames();

	String getStoredDataValue(@ParamName("key") String key);

	boolean hasDialog(@ParamName("id") int id);

	boolean hasFaction(@ParamName("id") int id);

	boolean hasPlayerName(@ParamName("name") String name);

	boolean hasQuest(@ParamName("id") int id);

	boolean hasScoreboard(@ParamName("objective") String objective);

	boolean hasStoredData(@ParamName("key") String key, @ParamName("value") String value);

	boolean isAvailable(@ParamName("player") IPlayer<?> player);

	void removeDialog(@ParamName("id") int id);

	void removeFaction(@ParamName("id") int id);

	void removePlayerName(@ParamName("name") String name);

	void removeQuest(@ParamName("id") int id);

	void removeScoreboard(@ParamName("objective") String objective);

	void removeStoredData(@ParamName("key") String key);

	void setDaytime(@ParamName("type") int type);

	void setDaytime(@ParamName("minHour") int minHour, @ParamName("maxHour") int maxHour);

	void setDialog(@ParamName("id") int id, @ParamName("type") int type);

	void setFaction(@ParamName("id") int id, @ParamName("type") int type, @ParamName("stance") int stance);

	void setHealth(@ParamName("value") int value, @ParamName("type") int type);

	void setMinPlayerLevel(@ParamName("level") int level);

	void setPlayerName(@ParamName("id") String name, @ParamName("type") int type);

	void setQuest(@ParamName("id") int id, @ParamName("type") int type);

	void setScoreboard(@ParamName("objective") String objective, @ParamName("type") int type, @ParamName("value") int value);

	void setStoredData(@ParamName("key") String key, @ParamName("value") String value, @ParamName("type") int type);

	boolean getGMOnly();

	void setGMOnly(@ParamName("gmOnly") boolean gmOnly);

	IItemStack getIItemStack(@ParamName("slotId") int slotId);

	IItemStack[] getIItemStacks();

	void setIItemStack(@ParamName("slotId") int slotId, @ParamName("item") IItemStack item);

	void removeIItemStack(@ParamName("slotId") int slotId);

}
