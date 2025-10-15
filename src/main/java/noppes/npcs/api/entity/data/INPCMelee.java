package noppes.npcs.api.entity.data;

import noppes.npcs.api.ParamName;

public interface INPCMelee {

	int getDelay();

	int getEffectStrength();

	int getEffectTime();

	int getEffectType();

	int getKnockback();

	double getRange();

	int getStrength();

	void setDelay(@ParamName("speed") int speed);

	void setEffect(@ParamName("type") int type, @ParamName("strength") int strength, @ParamName("time") int time);

	void setKnockback(@ParamName("knockback") int knockback);

	void setRange(@ParamName("range") double range);

	void setStrength(@ParamName("strength") int strength);

}
