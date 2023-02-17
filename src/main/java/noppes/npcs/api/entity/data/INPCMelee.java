package noppes.npcs.api.entity.data;

public interface INPCMelee {
	int getDelay();

	int getEffectStrength();

	int getEffectTime();

	int getEffectType();

	int getKnockback();

	int getRange();

	int getStrength();

	void setDelay(int p0);

	void setEffect(int p0, int p1, int p2);

	void setKnockback(int p0);

	void setRange(int p0);

	void setStrength(int p0);
}
