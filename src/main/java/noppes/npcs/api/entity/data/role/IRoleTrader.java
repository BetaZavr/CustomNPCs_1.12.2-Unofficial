package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.entity.data.INPCRole;
import noppes.npcs.api.handler.data.IMarcet;

public interface IRoleTrader extends INPCRole {

	IMarcet getMarket();

	int getMarketID();

	void setMarket(IMarcet marcet);

	void setMarket(int id);

}
