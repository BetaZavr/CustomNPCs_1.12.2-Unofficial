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

	String getSound(int p0);

	int getSpeed();

	boolean getSpins();

	boolean getSticks();

	int getStrength();

	void setAccelerate(boolean p0);

	void setAccuracy(int p0);

	void setBurst(int p0);

	void setBurstDelay(int p0);

	void setDelay(int p0, int p1);

	void setEffect(int p0, int p1, int p2);

	void setExplodeSize(int p0);

	void setFireType(int p0);

	void setGlows(boolean p0);

	void setHasAimAnimation(boolean p0);

	void setHasGravity(boolean p0);

	void setKnockback(int p0);

	void setMeleeRange(int p0);

	void setParticle(int p0);

	void setRange(int p0);

	void setRender3D(boolean p0);

	void setShotCount(int p0);

	void setSize(int p0);

	void setSound(int p0, String p1);

	void setSpeed(int p0);

	void setSpins(boolean p0);

	void setSticks(boolean p0);

	void setStrength(int p0);
}
