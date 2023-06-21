package noppes.npcs;

import noppes.npcs.entity.EntityNPCInterface;

public interface IChatMessages {
	
	void addMessage(String message, EntityNPCInterface npc);

	void renderMessages(double x, double y, double z, float height, boolean inRange);
}
