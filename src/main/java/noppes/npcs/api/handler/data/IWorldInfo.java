package noppes.npcs.api.handler.data;

import noppes.npcs.api.INbt;

public interface IWorldInfo {

	int getId();

	INbt getNbt();

	void setNbt(INbt nbt);

}
