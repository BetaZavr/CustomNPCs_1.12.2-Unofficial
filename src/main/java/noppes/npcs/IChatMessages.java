package noppes.npcs;

import noppes.npcs.entity.EntityNPCInterface;

public interface IChatMessages {
	void addMessage(String p0, EntityNPCInterface p1);

	void renderMessages(double p0, double p1, double p2, float p3, boolean p4);
}
