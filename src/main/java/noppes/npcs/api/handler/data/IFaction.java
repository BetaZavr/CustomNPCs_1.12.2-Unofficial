package noppes.npcs.api.handler.data;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IPlayer;

@SuppressWarnings("all")
public interface IFaction {

	void addHostile(@ParamName("factionId") int factionId);

	boolean getAttackedByMobs();

	int getColor();

	int getDefaultPoints();

	String getDescription();

	String getFlag();

	int[] getHostileList();

	int getId();

	boolean getIsHidden();

	String getName();

	boolean hasHostile(@ParamName("factionId") int id);

	boolean hostileToFaction(@ParamName("factionId") int factionId);

	boolean hostileToNpc(@ParamName("npc") ICustomNpc<?> npc);

	int playerStatus(@ParamName("player") IPlayer<?> player);

	void removeHostile(@ParamName("factionId") int factionId);

	void save();

	void setAttackedByMobs(@ParamName("bo") boolean bo);

	void setDefaultPoints(@ParamName("points") int points);

	void setDescription(@ParamName("description") String description);

	void setFlag(@ParamName("flagPath") String flagPath);

	void setIsHidden(@ParamName("bo") boolean bo);

}
