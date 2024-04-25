package noppes.npcs.api;

import noppes.npcs.api.entity.IEntity;

public interface IEntityDamageSource {

	String getDeadMessage();

	IEntity<?> getIImmediateSource();

	IEntity<?> getITrueSource();

	String getType();

	void setDeadMessage(String message);

	void setImmediateSource(IEntity<?> entity);

	void setTrueSource(IEntity<?> entity);

	void setType(String damageType);

}
