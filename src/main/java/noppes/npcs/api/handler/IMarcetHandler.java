package noppes.npcs.api.handler;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.handler.data.IDeal;
import noppes.npcs.api.handler.data.IMarcet;

@SuppressWarnings("all")
public interface IMarcetHandler {

	IDeal addDeal();

	IMarcet addMarcet();

	IDeal getDeal(@ParamName("dealId") int dealId);

	int[] getDealIDs();

	IMarcet getMarcet(@ParamName("marcetId") int marcetId);

	IMarcet getMarcet(@ParamName("name") String name);

	int[] getMarketIDs();

	void removeDeal(@ParamName("dealId") int dealId);

	void removeMarcet(@ParamName("marcetId") int marcetId);

}
