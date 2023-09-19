package noppes.npcs;

import net.minecraft.entity.Entity;

public interface IChatMessages {
	
	void addMessage(String message, Entity npc);

	void renderMessages(double x, double y, double z, float height, boolean inRange);
}
