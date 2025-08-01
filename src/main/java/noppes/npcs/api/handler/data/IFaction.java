package noppes.npcs.api.handler.data;

import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IPlayer;

@SuppressWarnings("all")
public interface IFaction {

	void addHostile(int id);

	boolean getAttackedByMobs();

	int getColor();

	int getDefaultPoints();

	String getDescription();

	String getFlag();

	int[] getHostileList();

	int getId();

	boolean getIsHidden();

	String getName();

	boolean hasHostile(int id);

	boolean hostileToFaction(int factionId);

	boolean hostileToNpc(ICustomNpc<?> npc);

	int playerStatus(IPlayer<?> player);

	void removeHostile(int id);

	void save();

	void setAttackedByMobs(boolean bo);

	void setDefaultPoints(int points);

	void setDescription(String description);

	void setFlag(String flagPath);

	void setIsHidden(boolean bo);

}
