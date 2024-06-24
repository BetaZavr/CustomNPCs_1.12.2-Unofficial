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

	void setAnimation(int type);

	void setAttackInvisible(boolean attack);

	void setAttackLOS(boolean enabled);

	void setAvoidsWater(boolean enabled);

	void setCanSwim(boolean canSwim);

	void setDoorInteract(int type);

	void setInteractWithNPCs(boolean interact);

	void setLeapAtTarget(boolean leap);

	void setMovingPathType(int type, boolean pauses);

	void setMovingType(int type);

	void setNavigationType(int type);

	void setRetaliateType(int type);

	void setReturnsHome(boolean bo);

	void setSheltersFrom(int type);

	void setStandingType(int type);

	void setStopOnInteract(boolean stopOnInteract);

	void setTacticalRange(int range);

	void setTacticalType(int type);

	void setWalkingSpeed(int speed);

	void setWanderingRange(int range);
	
	boolean isAIDisabled();
	
	void setIsAIDisabled(boolean aiDisabled);
	
}
