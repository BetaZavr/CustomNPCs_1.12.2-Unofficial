package noppes.npcs.api.entity.data;

public interface INPCMelee {
	
	int getDelay();

	int getEffectStrength();

	int getEffectTime();

	int getEffectType();

	int getKnockback();

	int getRange();

	int getStrength();

	void setDelay(int speed);

	void setEffect(int type, int strength, int time);

	void setKnockback(int knockback);

	void setRange(int range);

	void setStrength(int strength);
	
}

