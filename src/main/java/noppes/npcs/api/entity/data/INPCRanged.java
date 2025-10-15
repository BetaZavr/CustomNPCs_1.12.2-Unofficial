package noppes.npcs.api.entity.data;

import noppes.npcs.api.ParamName;

public interface INPCRanged {

	boolean getAccelerate();

	int getAccuracy();

	int getBurst();

	int getBurstDelay();

	int getDelayMax();

	int getDelayMin();

	int getDelayRNG();

	int getEffectStrength();

	int getEffectTime();

	int getEffectType();

	int getExplodeSize();

	int getFireType();

	boolean getGlows();

	boolean getHasAimAnimation();

	boolean getHasGravity();

	int getKnockback();

	int getMeleeRange();

	int getParticle();

	double getRange();

	boolean getRender3D();

	int getShotCount();

	int getSize();

	String getSound(@ParamName("type") int type);

	int getSpeed();

	boolean getSpins();

	boolean getSticks();

	int getStrength();

	void setAccelerate(@ParamName("accelerate") boolean accelerate);

	void setAccuracy(@ParamName("accuracy") int accuracy);

	void setBurst(@ParamName("count") int count);

	void setBurstDelay(@ParamName("delay") int delay);

	void setDelay(@ParamName("min") int min, @ParamName("max") int max);

	void setEffect(@ParamName("type") int type, @ParamName("strength") int strength, @ParamName("time") int time);

	void setExplodeSize(@ParamName("size") int size);

	void setFireType(@ParamName("type") int type);

	void setGlows(@ParamName("glows") boolean glows);

	void setHasAimAnimation(@ParamName("aim") boolean aim);

	void setHasGravity(@ParamName("hasGravity") boolean hasGravity);

	void setKnockback(@ParamName("punch") int punch);

	void setMeleeRange(@ParamName("range") int range);

	void setParticle(@ParamName("type") int type);

	void setRange(@ParamName("range") double range);

	void setRender3D(@ParamName("render3d") boolean render3d);

	void setShotCount(@ParamName("count") int count);

	void setSize(@ParamName("size") int size);

	void setSound(@ParamName("type") int type, @ParamName("sound") String sound);

	void setSpeed(@ParamName("speed") int speed);

	void setSpins(@ParamName("spins") boolean spins);

	void setSticks(@ParamName("sticks") boolean sticks);

	void setStrength(@ParamName("strength") int strength);

}
