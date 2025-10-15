package noppes.npcs.api.entity.data;

import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface INPCStats {

	int getAggroRange();

	int getCombatRegen();

	int getCreatureType();

	int getHealthRegen();

	boolean getHideDeadBody();

	boolean getImmune(@ParamName("type") int type);

	int getLevel();

	double getMaxHealth();

	INPCMelee getMelee();

	INPCRanged getRanged();

	int getRarity();

	String getRarityTitle();

	float getResistance(@ParamName("damageName") String damageName);
	
	String[] getResistanceKeys();

	int getRespawnTime();

	int getRespawnType();

	boolean isCalmdown();

	void setAggroRange(@ParamName("range") int range);

	void setCalmdown(@ParamName("bo") boolean bo);

	void setCombatRegen(@ParamName("range") int regen);

	void setCreatureType(@ParamName("type") int type);

	void setHealthRegen(@ParamName("regen") int regen);

	void setHideDeadBody(@ParamName("hide") boolean hide);

	void setImmune(@ParamName("type") int type, @ParamName("bo") boolean bo);

	void setLevel(@ParamName("level") int level);

	void setMaxHealth(@ParamName("maxHealth") double maxHealth);

	void setRarity(@ParamName("rarity") int rarity);

	void setRarityTitle(@ParamName("rarity") String rarity);

	void setResistance(@ParamName("damageName") String damageName, @ParamName("value") float value);

	void setRespawnTime(@ParamName("seconds") int seconds);

	void setRespawnType(@ParamName("type") int type);

    float getChanceBlockDamage();

	void setChanceBlockDamage(@ParamName("chance") float chance);
}
