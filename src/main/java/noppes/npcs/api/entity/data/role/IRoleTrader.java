package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.data.INPCRole;
import noppes.npcs.api.handler.data.IMarcet;

public interface IRoleTrader extends INPCRole {

	IMarcet getMarket();

	int getMarketID();

	void setMarket(@ParamName("marcet") IMarcet marcet);

	void setMarket(@ParamName("id") int id);

}
