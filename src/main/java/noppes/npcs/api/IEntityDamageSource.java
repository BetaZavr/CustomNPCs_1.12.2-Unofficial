package noppes.npcs.api;

import noppes.npcs.api.entity.IEntity;

public interface IEntityDamageSource {

	String getType();
	
	void setType(String damageType);
	
	IEntity<?> getITrueSource();
	
	void setTrueSource(IEntity<?> entity);
	
	IEntity<?> getIImmediateSource();
	
	void setImmediateSource(IEntity<?> entity);
	
	String getDeadMessage();
	
	void setDeadMessage(String message);
	
	//IEntityDamageSource setIsThornsDamage(); // base in EntityDamageSource
	
	//boolean getIsThornsDamage(); // base in EntityDamageSource
	
}
