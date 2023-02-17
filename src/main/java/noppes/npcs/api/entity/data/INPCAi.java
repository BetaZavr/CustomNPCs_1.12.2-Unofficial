package noppes.npcs.api.entity.data;

public interface INPCAi {
	int getAnimation();

	boolean getAttackInvisible();

	boolean getAttackLOS();

	boolean getAvoidsWater();

	boolean getCanSwim();

	int getCurrentAnimation();

	int getDoorInteract();

	boolean getInteractWithNPCs();

	boolean getLeapAtTarget();

	boolean getMovingPathPauses();

	int getMovingPathType();

	int getMovingType();

	int getNavigationType();

	int getRetaliateType();

	boolean getReturnsHome();

	int getSheltersFrom();

	int getStandingType();

	boolean getStopOnInteract();

	int getTacticalRange();

	int getTacticalType();

	int getWalkingSpeed();

	int getWanderingRange();

	void setAnimation(int p0);

	void setAttackInvisible(boolean p0);

	void setAttackLOS(boolean p0);

	void setAvoidsWater(boolean p0);

	void setCanSwim(boolean p0);

	void setDoorInteract(int p0);

	void setInteractWithNPCs(boolean p0);

	void setLeapAtTarget(boolean p0);

	void setMovingPathType(int p0, boolean p1);

	void setMovingType(int p0);

	void setNavigationType(int p0);

	void setRetaliateType(int p0);

	void setReturnsHome(boolean p0);

	void setSheltersFrom(int p0);

	void setStandingType(int p0);

	void setStopOnInteract(boolean p0);

	void setTacticalRange(int p0);

	void setTacticalType(int p0);

	void setWalkingSpeed(int p0);

	void setWanderingRange(int p0);
}
