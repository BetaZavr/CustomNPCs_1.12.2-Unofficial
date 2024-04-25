package noppes.npcs.client.gui.util;

import noppes.npcs.entity.EntityNPCInterface;

public interface IEditNPC {

	int getEventButton();

	EntityNPCInterface getNPC();

	boolean hasSubGui();

}
