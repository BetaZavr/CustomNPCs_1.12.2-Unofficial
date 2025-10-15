package noppes.npcs.api.handler.data;

import noppes.npcs.api.INbt;
import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface IKeySetting {

	String getCategory();

	int getId();

	int getKeyId();

	int getModiferType();

	String getName();

	INbt getNbt();

	void setCategory(@ParamName("name") String name);

	void setKeyId(@ParamName("keyId") int keyId);

	void setModiferType(@ParamName("type") int type);

	void setName(@ParamName("name") String name);

	void setNbt(@ParamName("nbt") INbt nbt);

}
