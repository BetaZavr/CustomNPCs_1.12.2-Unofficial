package noppes.npcs.api.handler.data;

import noppes.npcs.api.entity.IPlayer;

public interface IAvailability {
	
	int[] getDaytime();

	int getMinPlayerLevel();

	boolean hasDialog(int id);

	boolean hasFaction(int id);

	boolean hasQuest(int id);

	boolean hasScoreboard(String objective);

	boolean isAvailable(IPlayer<?> player);

	void removeDialog(int id);

	void removeFaction(int id);

	void removeQuest(int id);

	void removeScoreboard(String objective);

	void setDaytime(int type);

	void setDaytime(int minHour, int maxHour);

	void setDialog(int id, int type);

	void setFaction(int id, int type, int stance);

	void setMinPlayerLevel(int level);

	void setQuest(int id, int type);

	void setScoreboard(String objective, int type, int value);

	int getHealth();

	int getHealthType();

	void setHealth(int value, int type);
	
	String[] getPlayerNames();

	boolean hasPlayerName(String name);

	void removePlayerName(String name);

	void setPlayerName(String name, int type);
	
}
