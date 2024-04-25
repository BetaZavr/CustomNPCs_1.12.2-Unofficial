package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.IDeal;
import noppes.npcs.api.handler.data.IMarcet;

public interface IMarcetHandler {

	IDeal addDeal();

	IMarcet addMarcet();

	IDeal getDeal(int dealID);

	int[] getDealIDs();

	IMarcet getMarcet(int marcetId);

	IMarcet getMarcet(String name);

	int[] getMarketIDs();

	boolean removeDeal(int dealID);

	boolean removeMarcet(int marcetID);

}
