package noppes.npcs.api.handler.data;

import noppes.npcs.api.INbt;

public interface IScriptData {

	String getName();

	INbt getNBT();

	Object getObject();

	int getType();

	String getValue();

	void setNBT(INbt nbt);

}
