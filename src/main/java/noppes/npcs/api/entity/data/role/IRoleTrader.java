package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.entity.data.INPCRole;
import noppes.npcs.api.handler.data.IMarcet;

public interface IRoleTrader
extends INPCRole {
	
	int getMarketID();
	
	IMarcet getMarket();

	void setMarket(int id);
	
	void setMarket(IMarcet marcet);
	
}
