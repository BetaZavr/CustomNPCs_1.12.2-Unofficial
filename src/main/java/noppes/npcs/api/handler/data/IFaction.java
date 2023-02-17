package noppes.npcs.api.handler.data;

import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IPlayer;

public interface IFaction {
	void addHostile(int p0);

	boolean getAttackedByMobs();

	int getColor();

	int getDefaultPoints();

	int[] getHostileList();

	int getId();

	boolean getIsHidden();

	String getName();

	boolean hasHostile(int p0);

	boolean hostileToFaction(int p0);

	boolean hostileToNpc(ICustomNpc<?> p0);

	int playerStatus(IPlayer<?> p0);

	void removeHostile(int p0);

	void save();

	void setAttackedByMobs(boolean p0);

	void setDefaultPoints(int p0);

	void setIsHidden(boolean p0);
}
