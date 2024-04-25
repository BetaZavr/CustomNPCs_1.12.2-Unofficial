package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.IFaction;

public interface IFactionHandler {

	IFaction create(String name, int color);

	IFaction delete(int id);

	IFaction get(int id);

	IFaction[] list();

}
