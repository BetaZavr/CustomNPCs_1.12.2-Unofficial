package noppes.npcs.api.handler.data;

import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;

public interface IAvailability {

	int[] getDaytime();

	int getHealth();

	int getHealthType();

	int getMinPlayerLevel();

	String[] getPlayerNames();

	String getStoredDataValue(String key);

	boolean hasDialog(int id);

	boolean hasFaction(int id);

	boolean hasPlayerName(String name);

	boolean hasQuest(int id);

	boolean hasScoreboard(String objective);

	boolean hasStoredData(String key, String value);

	boolean isAvailable(IPlayer<?> player);

	void removeDialog(int id);

	void removeFaction(int id);

	void removePlayerName(String name);

	void removeQuest(int id);

	void removeScoreboard(String objective);

	void removeStoredData(String key);

	void setDaytime(int type);

	void setDaytime(int minHour, int maxHour);

	void setDialog(int id, int type);

	void setFaction(int id, int type, int stance);

	void setHealth(int value, int type);

	void setMinPlayerLevel(int level);

	void setPlayerName(String name, int type);

	void setQuest(int id, int type);

	void setScoreboard(String objective, int type, int value);

	void setStoredData(String key, String value, int type);

	boolean getGMOnly();

	void setGMOnly(boolean gmOnly);

	IAvailabilityStack getAvailabilityStack(int id);

	IAvailabilityStack[] getAvailabilityStacks();

	IAvailabilityStack addIItemStack(IItemStack item);

	void removeIItemStack(int id);

}
