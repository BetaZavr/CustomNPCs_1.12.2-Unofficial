package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.IDeal;
import noppes.npcs.api.handler.data.IMarcet;

public interface IMarcetHandler {

	IMarcet addMarcet();

	IMarcet getMarcet(String name);

	IMarcet getMarcet(int marcetId);

	int[] getMarketIDs();

	boolean removeMarcet(int marcetID);

	IDeal addDeal(int marcetID);

	IDeal getDeal(int dealID);
	
	boolean removeDeal(int dealID);

	int[] getDealIDs();

}
