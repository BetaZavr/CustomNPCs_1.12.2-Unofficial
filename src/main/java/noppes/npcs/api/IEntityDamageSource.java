package noppes.npcs.api;

import noppes.npcs.api.entity.IEntity;

@SuppressWarnings("all")
public interface IEntityDamageSource {

	String getDeadMessage();

	IEntity<?> getIImmediateSource();

	IEntity<?> getITrueSource();

	String getType();

	void setDeadMessage(@ParamName("message") String message);

	void setImmediateSource(@ParamName("entity") IEntity<?> entity);

	void setTrueSource(@ParamName("entity") IEntity<?> entity);

	void setType(@ParamName("damageType") String damageType);

}
