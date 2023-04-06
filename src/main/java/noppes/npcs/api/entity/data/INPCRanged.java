package noppes.npcs.api.entity.data;

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

	int getRange();

	boolean getRender3D();

	int getShotCount();

	int getSize();

	String getSound(int type);

	int getSpeed();

	boolean getSpins();

	boolean getSticks();

	int getStrength();

	void setAccelerate(boolean accelerate);

	void setAccuracy(int accuracy);

	void setBurst(int count);

	void setBurstDelay(int delay);

	void setDelay(int min, int max);

	void setEffect(int type, int strength, int time);

	void setExplodeSize(int size);

	void setFireType(int type);

	void setGlows(boolean glows);

	void setHasAimAnimation(boolean aim);

	void setHasGravity(boolean hasGravity);

	void setKnockback(int punch);

	void setMeleeRange(int range);

	void setParticle(int type);

	void setRange(int range);

	void setRender3D(boolean render3d);

	void setShotCount(int count);

	void setSize(int size);

	void setSound(int type, String sound);

	void setSpeed(int speed);

	void setSpins(boolean spins);

	void setSticks(boolean sticks);

	void setStrength(int strength);
	
}
