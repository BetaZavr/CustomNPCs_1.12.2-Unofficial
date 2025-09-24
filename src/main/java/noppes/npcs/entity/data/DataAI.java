package noppes.npcs.entity.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NBTTags;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IPos;
import noppes.npcs.api.entity.data.INPCAi;
import noppes.npcs.api.wrapper.BlockPosWrapper;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobBuilder;
import noppes.npcs.roles.JobFarmer;

public class DataAI
		implements INPCAi {

	protected final EntityNPCInterface npc;
	protected List<int[]> movingPath = new ArrayList<>();
	protected BlockPos startPos = null;
	protected int maxHurtResistantTime = CustomNpcs.DefaultHurtResistantTime * 2;
	protected int standingType = 0; // 0:NoRotation, 1:RotateBody, 2:Stalking, 3:HeadRotation, 4:EyeRotation
	protected int movingType = 0; // 0:Standing, 1:Wandering, 2:MovingPath -> EntityAIMovingPath
	protected int tacticalRadius = 8;
	protected int moveSpeed = 5;

	public boolean attackInvisible = false;
	public boolean avoidsSun = false;
	public boolean avoidsWater = false;
	public boolean canLeap = false; // can jump to target
	public boolean canSprint = false;
	public boolean canSwim = true;
	public boolean directLOS = true;
	public boolean movingPause = true; // -> EntityAIMovingPath
	public boolean npcInteracting = true;
	public boolean reactsToFire = false;
	public boolean returnToStart = true;
	public boolean stopAndInteract = true;
	public boolean aiDisabled = false;
	public boolean canBeCollide = true;

	public int animationType = 0;
	public int doorInteract = 2;
	public int findShelter = 2; // 0:Night, 1:Day, 2:Disable
	public int movementType = 0; // 0:Ground, 1:Flying, 2:Swimming
	public int movingPattern = 0; // -> EntityAIMovingPath
	public int movingPos = 0; // -> EntityAIMovingPath
	public int onAttack = 0; // 0:Normal, 1:Panic, 2:Retreat, 3:Nothing
	public int orientation = 0;
	public int tacticalVariant = 0;
	public int walkingRange = 10;

	public float bodyOffsetX = 5.0f;
	public float bodyOffsetY = 5.0f;
	public float bodyOffsetZ = 5.0f;
	public float stepheight = 0.6f;

	public DataAI(EntityNPCInterface npcIn) { npc = npcIn; }

	@SuppressWarnings("all")
	public void appendMovingPath(int[] pos) { movingPath.add(pos); }

	@SuppressWarnings("all")
	public void clearMovingPath() {
		movingPath.clear();
		movingPos = 0;
	}

	public void decreaseMovingPath() {
		List<int[]> list = getMovingPath();
		if (list.size() == 1) {
			movingPos = 0;
			return;
		}
		--movingPos;
		if (movingPos < 0) {
			if (movingPattern == 0) { movingPos = list.size() - 1; }
			else if (movingPattern == 1) { movingPos = list.size() * 2 - 2; }
		}
	}

	@Override
	public int getAnimation() { return animationType; }

	@Override
	public boolean getAttackInvisible() { return attackInvisible; }

	@Override
	public boolean getAttackLOS() { return directLOS; }

	@Override
	public boolean getAvoidsWater() { return avoidsWater; }

	@Override
	public boolean getCanSwim() { return canSwim; }

	@Override
	public int getCurrentAnimation() { return npc.currentAnimation; }

	public int[] getCurrentMovingPath() {
		List<int[]> list = getMovingPath();
		int size = list.size();
		if (size == 1) { return list.get(0); }
		int pos = movingPos;
		if (movingPattern == 0 && pos >= size) { pos = movingPos = 0; }
		else if (movingPattern == 1) {
			int size2 = size * 2 - 1;
			if (pos >= size2) { pos = movingPos = 0; }
			else if (pos >= size) { pos = size2 - pos; }
		}
		return list.get(pos);
	}

	public double getDistanceSqToPathPoint() {
		int[] pos = getCurrentMovingPath();
		return npc.getDistanceSq(pos[0] + 0.5, pos[1], pos[2] + 0.5);
	}

	@Override
	public int getDoorInteract() { return doorInteract; }

	@Override
	public boolean getInteractWithNPCs() { return npcInteracting; }

	@Override
	public boolean getLeapAtTarget() { return canLeap; }

	public List<int[]> getMovingPath() {
		if (startPos != null) {
			if (movingPath.isEmpty()) { movingPath.add(getStartArray()); }
			else {
				int[] arr = movingPath.get(0);
				if (arr[0] != startPos.getX() || arr[1] != startPos.getY() || arr[2] != startPos.getZ()) {
					movingPath.remove(0);
					movingPath.add(0, getStartArray());
				}
			}
		}
		return movingPath;
	}

	@Override
	public boolean getMovingPathPauses() { return movingPause; }

	@SuppressWarnings("all")
	public int[] getMovingPathPos(int m_pos) { return movingPath.get(m_pos); }

	@SuppressWarnings("all")
	public int getMovingPathSize() { return movingPath.size(); }

	@Override
	public int getMovingPathType() { return movingPattern; }

	@SuppressWarnings("all")
	public int getMovingPos() { return movingPos; }

	/**
	 * @return
	 * 		0: Standing
	 * 		1: Wandering
	 * 		2: MovingPath -> EntityAIMovingPath
	 */
	@Override
	public int getMovingType() { return movingType; }

	/**
	 * @return 0:Ground, 1:Flying, 2:Swimming
	 */
	@Override
	public int getNavigationType() { return movementType; }

	/**
	 * @return 0:Normal, 1:Panic, 2:Retreat, 3:Nothing
	 */
	@Override
	public int getRetaliateType() { return onAttack; }

	@Override
	public boolean getReturnsHome() { return returnToStart; }

	/**
	 * 0:Night, 1:Day, 2:Disable
	 */
	@Override
	public int getSheltersFrom() { return findShelter; }

	/**
	 * 0:NoRotation, 1:RotateBody, 2:Stalking, 3:HeadRotation, 4:EyeRotation
	 */
	@Override
	public int getStandingType() { return standingType; }

	public int[] getStartArray() {
		BlockPos pos = startPos();
		return new int[] { pos.getX(), pos.getY(), pos.getZ() };
	}

	public IPos getStartPos() { return new BlockPosWrapper(startPos()); }

	@Override
	public boolean getStopOnInteract() { return stopAndInteract; }

	@Override
	public int getTacticalRange() { return tacticalRadius; }

	@Override
	public int getTacticalType() { return tacticalVariant; }

	@Override
	public int getWalkingSpeed() { return moveSpeed; }

	@Override
	public int getWanderingRange() { return walkingRange; }

	@Override
	public int getMaxHurtResistantTime() { return maxHurtResistantTime; }

	public void incrementMovingPath() {
		List<int[]> list = getMovingPath();
		if (list.size() == 1) {
			movingPos = 0;
			return;
		}
		++movingPos;
		if (movingPattern == 0) { movingPos %= list.size(); }
		else if (movingPattern == 1) {
			int size = list.size() * 2 - 1;
			movingPos %= size;
		}
	}

	public void readToNBT(NBTTagCompound compound) {
		canSwim = compound.getBoolean("CanSwim");
		reactsToFire = compound.getBoolean("ReactsToFire");
		setAvoidsWater(compound.getBoolean("AvoidsWater"));
		avoidsSun = compound.getBoolean("AvoidsSun");
		returnToStart = compound.getBoolean("ReturnToStart");
		onAttack = compound.getInteger("OnAttack");
		doorInteract = compound.getInteger("DoorInteract");
		findShelter = compound.getInteger("FindShelter");
		directLOS = compound.getBoolean("DirectLOS");
		canLeap = compound.getBoolean("CanLeap");
		aiDisabled = compound.getBoolean("AIDisabled");
		canSprint = compound.getBoolean("CanSprint");
		if (compound.hasKey("CanBeCollide", 1)) { canBeCollide = compound.getBoolean("CanBeCollide"); }
		tacticalRadius = compound.getInteger("TacticalRadius");
		movingPause = compound.getBoolean("MovingPause");
		npcInteracting = compound.getBoolean("npcInteracting");
		stopAndInteract = compound.getBoolean("stopAndInteract");
		movementType = compound.getInteger("MovementType");
		animationType = compound.getInteger("MoveState");
		standingType = compound.getInteger("StandingState");
		movingType = compound.getInteger("MovingState");
		tacticalVariant = compound.getInteger("TacticalVariant");
		orientation = compound.getInteger("Orientation");
		bodyOffsetY = compound.getFloat("PositionOffsetY");
		bodyOffsetZ = compound.getFloat("PositionOffsetZ");
		bodyOffsetX = compound.getFloat("PositionOffsetX");
		stepheight = compound.getFloat("StepHeight");
		walkingRange = compound.getInteger("WalkingRange");
		setWalkingSpeed(compound.getInteger("MoveSpeed"));
		setMovingPath(NBTTags.getIntegerArraySet(compound.getTagList("MovingPathNew", 10)));
		movingPos = compound.getInteger("MovingPos");
		movingPattern = compound.getInteger("MovingPatern");
		attackInvisible = compound.getBoolean("AttackInvisible");
		if (compound.hasKey("StartPosNew")) {
			int[] pos = compound.getIntArray("StartPosNew");
			startPos = new BlockPos(pos[0], pos[1], pos[2]);
		}
		npc.stepHeight = stepheight;
		if (standingType != 0 && standingType != 2) {
			npc.setRotationYawHead(orientation);
		}

		if (compound.hasKey("MaxHurtResistantTime", 3)) { maxHurtResistantTime = compound.getInteger("MaxHurtResistantTime"); }
		npc.maxHurtResistantTime = maxHurtResistantTime;
	}

	@Override
	public void setAnimation(int type) { animationType = type; }

	@Override
	public void setAttackInvisible(boolean attack) { attackInvisible = attack; }

	@Override
	public void setAttackLOS(boolean enabled) {
		directLOS = enabled;
		npc.updateAI = true;
	}

	@Override
	public void setAvoidsWater(boolean enabled) {
		if (npc.getNavigator() instanceof PathNavigateGround) { npc.setPathPriority(PathNodeType.WATER, enabled ? PathNodeType.WATER.getPriority() : 0.0f); }
		avoidsWater = enabled;
	}

	@Override
	public void setCanSwim(boolean canSwimIn) { canSwim = canSwimIn; }

	@Override
	public void setDoorInteract(int type) {
		doorInteract = type;
		npc.updateAI = true;
	}

	@Override
	public void setInteractWithNPCs(boolean interact) { npcInteracting = interact; }

	@Override
	public void setLeapAtTarget(boolean leap) {
		canLeap = leap;
		npc.updateAI = true;
	}

	public void setMovingPath(List<int[]> list) {
		movingPath = list;
		if (!movingPath.isEmpty()) {
			int[] pos = movingPath.get(0);
			startPos = new BlockPos(pos[0], pos[1], pos[2]);
		}
	}

	@SuppressWarnings("all")
	public void setMovingPathPos(int m_pos, int[] pos) {
		if (m_pos < 0) { m_pos = 0; }
		movingPath.set(m_pos, pos);
	}

	@Override
	public void setMovingPathType(int type, boolean pauses) {
		if (type != 0 && type != 1) { throw new CustomNPCsException("Moving path type: " + type); }
		movingPattern = type;
		movingPause = pauses;
	}

	@SuppressWarnings("all")
	public void setMovingPos(int pos) { movingPos = pos; }

	@Override
	public void setMovingType(int type) {
		if (type < 0 || type > 2) { throw new CustomNPCsException("Unknown moving type: " + type); }
		movingType = type;
		npc.updateAI = true;
	}

	@Override
	public void setNavigationType(int type) { movementType = type; }

	@Override
	public void setRetaliateType(int type) {
		if (type < 0 || type > 3) { throw new CustomNPCsException("[0 / 3] ]Unknown retaliation type: " + type); }
		onAttack = type;
		npc.updateAI = true;
	}

	@Override
	public void setReturnsHome(boolean bo) { returnToStart = bo; }

	@Override
	public void setSheltersFrom(int type) {
		findShelter = type;
		npc.updateAI = true;
	}

	@Override
	public void setStandingType(int type) {
		if (type < 0 || type > 4) { throw new CustomNPCsException("Unknown standing type: " + type); }
		standingType = type;
		npc.updateAI = true;
	}

	public void setStartPos(BlockPos pos) { startPos = pos; }

	public void setStartPos(double x, double y, double z) {
		startPos = new BlockPos(x, y, z);
	}

	public void setStartPos(IPos pos) { startPos = pos.getMCBlockPos(); }

	@Override
	public void setStopOnInteract(boolean stopOnInteract) {
		stopAndInteract = stopOnInteract;
	}

	@Override
	public void setTacticalRange(int range) { tacticalRadius = range; }

	@Override
	public void setTacticalType(int type) {
		tacticalVariant = type;
		npc.updateAI = true;
	}

	@Override
	public void setWalkingSpeed(int speed) {
		if (speed < 0 || speed > 10) { throw new CustomNPCsException("Wrong speed: " + speed); }
		moveSpeed = speed;
		npc.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(npc.getSpeed());
		npc.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue((npc.getSpeed() * 2.0f));
	}

	@Override
	public void setWanderingRange(int range) {
		if (range < 1 || range > 50) { throw new CustomNPCsException("Bad wandering range: " + range + " (1 - 50)"); }
		walkingRange = range;
	}

	@Override
	public void setMaxHurtResistantTime(int ticks) {
		if (ticks < 0) { ticks *= -1; }
		if (ticks > 1200) { ticks = 1200; }
		maxHurtResistantTime = ticks;
	}

	public boolean shouldReturnHome() {
		return (!(npc.advanced.jobInterface instanceof JobBuilder) || !((JobBuilder) npc.advanced.jobInterface).isBuilding()) &&
				(!(npc.advanced.jobInterface instanceof JobFarmer) || !((JobFarmer) npc.advanced.jobInterface).isPlucking()) &&
				returnToStart;
	}

	public BlockPos startPos() {
		if (startPos == null) { startPos = new BlockPos(npc); }
		return startPos;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		setAvoidsWater(avoidsWater);
		compound.setBoolean("CanSwim", canSwim);
		compound.setBoolean("ReactsToFire", reactsToFire);
		compound.setBoolean("AvoidsWater", avoidsWater);
		compound.setBoolean("AvoidsSun", avoidsSun);
		compound.setBoolean("ReturnToStart", returnToStart);
		compound.setInteger("OnAttack", onAttack);
		compound.setInteger("DoorInteract", doorInteract);
		compound.setInteger("FindShelter", findShelter);
		compound.setBoolean("DirectLOS", directLOS);
		compound.setBoolean("CanLeap", canLeap);
		compound.setBoolean("AIDisabled", aiDisabled);
		compound.setBoolean("CanSprint", canSprint);
		compound.setBoolean("CanBeCollide", canBeCollide);
		compound.setInteger("TacticalRadius", tacticalRadius);
		compound.setBoolean("MovingPause", movingPause);
		compound.setBoolean("npcInteracting", npcInteracting);
		compound.setBoolean("stopAndInteract", stopAndInteract);
		compound.setInteger("MoveState", animationType);
		compound.setInteger("StandingState", standingType);
		compound.setInteger("MovingState", movingType);
		compound.setInteger("TacticalVariant", tacticalVariant);
		compound.setInteger("MovementType", movementType);
		compound.setInteger("Orientation", orientation);
		compound.setFloat("PositionOffsetX", bodyOffsetX);
		compound.setFloat("PositionOffsetY", bodyOffsetY);
		compound.setFloat("PositionOffsetZ", bodyOffsetZ);
		compound.setFloat("StepHeight", stepheight);
		compound.setInteger("WalkingRange", walkingRange);
		compound.setInteger("MoveSpeed", moveSpeed);
		compound.setTag("MovingPathNew", NBTTags.nbtIntegerArraySet(movingPath));
		compound.setInteger("MovingPos", movingPos);
		compound.setInteger("MovingPatern", movingPattern);
		compound.setIntArray("StartPosNew", getStartArray());
		compound.setBoolean("AttackInvisible", attackInvisible);
		compound.setInteger("MaxHurtResistantTime", maxHurtResistantTime);
		return compound;
	}

	@Override
	public boolean isAIDisabled() { return aiDisabled; }

	@Override
	public void setIsAIDisabled(boolean bo) { aiDisabled = bo; }

	@Override
	public boolean canBeCollide() { return canBeCollide; }

	@Override
	public void setCanBeCollide(boolean bo) { canBeCollide = bo; }

}
