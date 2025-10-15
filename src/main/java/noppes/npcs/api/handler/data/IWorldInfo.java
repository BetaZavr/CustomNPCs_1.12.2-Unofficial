package noppes.npcs.api.handler.data;

import noppes.npcs.api.INbt;
import noppes.npcs.api.ParamName;

public interface IWorldInfo {

	int getId();

	INbt getNbt();

	void setNbt(@ParamName("nbt") INbt nbt);

}
