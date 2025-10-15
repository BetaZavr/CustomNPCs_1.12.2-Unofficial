package noppes.npcs.api.entity.data;

import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
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

    int getMaxHurtResistantTime();

    void setAnimation(@ParamName("type") int type);

	void setAttackInvisible(@ParamName("attack") boolean attack);

	void setAttackLOS(@ParamName("enabled") boolean enabled);

	void setAvoidsWater(@ParamName("enabled") boolean enabled);

	void setCanSwim(@ParamName("canSwim") boolean canSwim);

	void setDoorInteract(@ParamName("type") int type);

	void setInteractWithNPCs(@ParamName("interact") boolean interact);

	void setLeapAtTarget(@ParamName("leap") boolean leap);

	void setMovingPathType(@ParamName("type") int type, @ParamName("pauses") boolean pauses);

	void setMovingType(@ParamName("type") int type);

	void setNavigationType(@ParamName("type") int type);

	void setRetaliateType(@ParamName("type") int type);

	void setReturnsHome(@ParamName("bo") boolean bo);

	void setSheltersFrom(@ParamName("type") int type);

	void setStandingType(@ParamName("type") int type);

	void setStopOnInteract(@ParamName("stopOnInteract") boolean stopOnInteract);

	void setTacticalRange(@ParamName("range") int range);

	void setTacticalType(@ParamName("type") int type);

	void setWalkingSpeed(@ParamName("speed") int speed);

	void setWanderingRange(@ParamName("range") int range);

	void setMaxHurtResistantTime(@ParamName("ticks") int ticks);

	boolean isAIDisabled();
	
	void setIsAIDisabled(@ParamName("aiDisabled") boolean aiDisabled);

    boolean canBeCollide();

	void setCanBeCollide(@ParamName("bo") boolean bo);
}
