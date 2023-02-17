package noppes.npcs.api.handler.data;

import noppes.npcs.api.entity.IPlayer;

public interface IAvailability {
	
	int[] getDaytime();

	int getMinPlayerLevel();

	boolean hasDialog(int id);

	boolean hasFaction(int id);

	boolean hasQuest(int id);

	boolean hasScoreboard(String objective);

	// int getDialog(int pos); Changed

	// int getQuest(int pos); Changed

	boolean isAvailable(IPlayer<?> player);

	void removeDialog(int id); // Changed

	void removeFaction(int id); // Changed

	void removeQuest(int id); // Changed

	void removeScoreboard(String objective);

	void setDaytime(int type);

	void setDaytime(int minHour, int maxHour);

	void setDialog(int id, int type); // Changed

	void setFaction(int id, int type, int stance); // Changed

	void setMinPlayerLevel(int level);

	void setQuest(int id, int type); // Changed

	void setScoreboard(String objective, int type, int value); // Changed
	
	int getHealth();
	
	int getHealthType();
	
	void setHealth(int value, int type);

}
