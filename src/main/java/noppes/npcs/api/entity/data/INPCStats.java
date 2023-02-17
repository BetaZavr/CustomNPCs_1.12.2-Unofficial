package noppes.npcs.api.entity.data;

public interface INPCStats {
	int getAggroRange();

	int getCombatRegen();

	int getCreatureType(); // (0=Normal, 1=Undead, 2=Arthropod) Only used for damage calculations with
							// enchants

	int getHealthRegen();

	boolean getHideDeadBody();

	boolean getImmune(int type);

	// New
	int getLevel();

	int getMaxHealth();

	INPCMelee getMelee();

	INPCRanged getRanged();

	int getRarity();

	String getRarityTitle();

	float getResistance(int type);

	int getRespawnTime();

	int getRespawnType();

	boolean isCalmdown();

	void setAggroRange(int regen);

	void setCalmdown(boolean bo);

	void setCombatRegen(int regen);

	void setCreatureType(int type); // (0=Normal, 1=Undead, 2=Arthropod) Only used for damage calculations with
									// enchants

	void setHealthRegen(int regen);

	void setHideDeadBody(boolean hide);

	void setImmune(int type, boolean bo);

	void setLevel(int level);

	void setMaxHealth(int maxHealth);

	void setRarity(int rarity);

	void setRarityTitle(String rarity);

	void setResistance(int type, float value);

	void setRespawnTime(int seconds);

	void setRespawnType(int type);

}
