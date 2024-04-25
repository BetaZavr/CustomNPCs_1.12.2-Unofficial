package noppes.npcs.api.handler.data;

import noppes.npcs.api.INbt;

public interface IKeySetting {

	String getCategory();

	int getId();

	int getKeyId();

	int getModiferType();

	String getName();

	INbt getNbt();

	void setCategory(String name);

	void setKeyId(int keyId);

	void setModiferType(int type);

	void setName(String name);

	void setNbt(INbt nbt);

}
