package noppes.npcs.api.handler;

import java.util.List;

import noppes.npcs.api.handler.data.IFaction;

public interface IFactionHandler {
	IFaction create(String p0, int p1);

	IFaction delete(int p0);

	IFaction get(int p0);

	List<IFaction> list();
}
