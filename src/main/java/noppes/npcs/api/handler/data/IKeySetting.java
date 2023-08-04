package noppes.npcs.api.handler.data;

import noppes.npcs.api.INbt;

public interface IKeySetting {
	
	String getName();
	
	void setName(String name);
	
	String getCategory();
	
	void setCategory(String name);

	int getId();
	
	int getKeyId();
	
	int getModiferType();
	
	void setKeyId(int keyId);
	
	void setModiferType(int type);

	INbt getNbt();

	void setNbt(INbt nbt);
	
}
