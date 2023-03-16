package noppes.npcs.api.handler.data;

import noppes.npcs.api.INbt;

public interface IWorldInfo {
	
	int getID();
	
	INbt getNbt();

	void setNbt(INbt nbt);
	
}
