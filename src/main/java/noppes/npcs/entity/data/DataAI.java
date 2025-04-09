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
	public int findShelter = 2;
	public int movementType = 0; // 0:Ground, 1:Flying, 2:Swimming
	private int moveSpeed = 5;
	public int movingPattern = 0; // -> EntityAIMovingPath
	public int movingPos = 0; // -> EntityAIMovingPath
	private int movingType = 0; // 0:Standing, 1:Wandering, 2:MovingPath -> EntityAIMovingPath
	public int onAttack = 0; // 0:Normal, 1:Panic, 2:Retreat, 3:Nothing
	public int orientation = 0;
	private int standingType = 0; // 0:NoRotation, 1:RotateBody, 2:Stalking, 3:HeadRotation, 4:EyeRotation
	private int tacticalRadius = 8;
	public int tacticalVariant = 0;
	public int walkingRange = 10;
	private int maxHurtResistantTime = CustomNpcs.DefaultHurtResistantTime * 2;

	public float bodyOffsetX = 5.0f;
	public float bodyOffsetY = 5.0f;
	public float bodyOffsetZ = 5.0f;
	public float stepheight = 0.6f;

	private BlockPos startPos = null;
	private List<int[]> movingPath = new ArrayList<>();
	private final EntityNPCInterface npc;

	public DataAI(EntityNPCInterface npc) {
		this.npc = npc;
	}

	public void appendMovingPath(int[] pos) {
		this.movingPath.add(pos);
	}

	public void clearMovingPath() {
		this.movingPath.clear();
		this.movingPos = 0;
	}

	public void decreaseMovingPath() {
		List<int[]> list = this.getMovingPath();
		if (list.size() == 1) {
			this.movingPos = 0;
			return;
		}
		--this.movingPos;
		if (this.movingPos < 0) {
			if (this.movingPattern == 0) {
				this.movingPos = list.size() - 1;
			} else if (this.movingPattern == 1) {
				this.movingPos = list.size() * 2 - 2;
			}
		}
	}

	@Override
	public int getAnimation() {
		return this.animationType;
	}

	@Override
	public boolean getAttackInvisible() {
		return this.attackInvisible;
	}

	@Override
	public boolean getAttackLOS() {
		return this.directLOS;
	}

	@Override
	public boolean getAvoidsWater() {
		return this.avoidsWater;
	}

	@Override
	public boolean getCanSwim() {
		return this.canSwim;
	}

	@Override
	public int getCurrentAnimation() {
		return this.npc.currentAnimation;
	}

	public int[] getCurrentMovingPath() {
		List<int[]> list = this.getMovingPath();
		int size = list.size();
		if (size == 1) {
			return list.get(0);
		}
		int pos = movingPos;
		if (movingPattern == 0 && pos >= size) {
			pos = movingPos = 0;
		} else if (movingPattern == 1) {
			int size2 = size * 2 - 1;
			if (pos >= size2) {
				pos = movingPos = 0;
			} else if (pos >= size) {
				pos = size2 - pos;
			}
		}
		return list.get(pos);
	}

	public double getDistanceSqToPathPoint() {
		int[] pos = this.getCurrentMovingPath();
		return this.npc.getDistanceSq(pos[0] + 0.5, pos[1], pos[2] + 0.5);
	}

	@Override
	public int getDoorInteract() {
		return this.doorInteract;
	}

	@Override
	public boolean getInteractWithNPCs() {
		return this.npcInteracting;
	}

	@Override
	public boolean getLeapAtTarget() {
		return this.canLeap;
	}

	public List<int[]> getMovingPath() {
		if (startPos != null) {
			if (movingPath.isEmpty()) {
				movingPath.add(getStartArray());
			} else {
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
	public boolean getMovingPathPauses() {
		return this.movingPause;
	}

	public int[] getMovingPathPos(int m_pos) {
		return this.movingPath.get(m_pos);
	}

	public int getMovingPathSize() {
		return this.movingPath.size();
	}

	@Override
	public int getMovingPathType() {
		return this.movingPattern;
	}

	public int getMovingPos() {
		return this.movingPos;
	}

	/**
	 * @return
	 * 		0: Standing
	 * 		1: Wandering
	 * 		2: MovingPath -> EntityAIMovingPath
	 */
	@Override
	public int getMovingType() {
		return this.movingType;
	}

	/**
	 * @return 0:Ground, 1:Flying, 2:Swimming
	 */
	@Override
	public int getNavigationType() {
		return this.movementType;
	}

	@Override
	public int getRetaliateType() {
		return this.onAttack;
	}

	@Override
	public boolean getReturnsHome() {
		return this.returnToStart;
	}

	@Override
	public int getSheltersFrom() {
		return this.findShelter;
	}

	/**
	 * 0:NoRotation, 1:RotateBody, 2:Stalking, 3:HeadRotation, 4:EyeRotation
	 */
	@Override
	public int getStandingType() {
		return this.standingType;
	}

	public int[] getStartArray() {
		BlockPos pos = startPos();
		return new int[] { pos.getX(), pos.getY(), pos.getZ() };
	}

	public IPos getStartPos() {
		return new BlockPosWrapper(startPos());
	}

	@Override
	public boolean getStopOnInteract() {
		return this.stopAndInteract;
	}

	@Override
	public int getTacticalRange() {
		return this.tacticalRadius;
	}

	@Override
	public int getTacticalType() {
		return this.tacticalVariant;
	}

	@Override
	public int getWalkingSpeed() {
		return this.moveSpeed;
	}

	@Override
	public int getWanderingRange() {
		return this.walkingRange;
	}

	@Override
	public int getMaxHurtResistantTime() {
		return this.maxHurtResistantTime;
	}


	public void incrementMovingPath() {
		List<int[]> list = this.getMovingPath();
		if (list.size() == 1) {
			this.movingPos = 0;
			return;
		}
		++this.movingPos;
		if (this.movingPattern == 0) {
			this.movingPos %= list.size();
		} else if (this.movingPattern == 1) {
			int size = list.size() * 2 - 1;
			this.movingPos %= size;
		}
	}

	public void readToNBT(NBTTagCompound compound) {
		this.canSwim = compound.getBoolean("CanSwim");
		this.reactsToFire = compound.getBoolean("ReactsToFire");
		this.setAvoidsWater(compound.getBoolean("AvoidsWater"));
		this.avoidsSun = compound.getBoolean("AvoidsSun");
		this.returnToStart = compound.getBoolean("ReturnToStart");
		this.onAttack = compound.getInteger("OnAttack");
		this.doorInteract = compound.getInteger("DoorInteract");
		this.findShelter = compound.getInteger("FindShelter");
		this.directLOS = compound.getBoolean("DirectLOS");
		this.canLeap = compound.getBoolean("CanLeap");
		this.aiDisabled = compound.getBoolean("AIDisabled");
		this.canSprint = compound.getBoolean("CanSprint");
		if (compound.hasKey("CanBeCollide", 1)) { this.canBeCollide = compound.getBoolean("CanBeCollide"); }
		this.tacticalRadius = compound.getInteger("TacticalRadius");
		this.movingPause = compound.getBoolean("MovingPause");
		this.npcInteracting = compound.getBoolean("npcInteracting");
		this.stopAndInteract = compound.getBoolean("stopAndInteract");
		this.movementType = compound.getInteger("MovementType");
		this.animationType = compound.getInteger("MoveState");
		this.standingType = compound.getInteger("StandingState");
		this.movingType = compound.getInteger("MovingState");
		this.tacticalVariant = compound.getInteger("TacticalVariant");
		this.orientation = compound.getInteger("Orientation");
		this.bodyOffsetY = compound.getFloat("PositionOffsetY");
		this.bodyOffsetZ = compound.getFloat("PositionOffsetZ");
		this.bodyOffsetX = compound.getFloat("PositionOffsetX");
		this.stepheight = compound.getFloat("StepHeight");
		this.walkingRange = compound.getInteger("WalkingRange");
		this.setWalkingSpeed(compound.getInteger("MoveSpeed"));
		this.setMovingPath(NBTTags.getIntegerArraySet(compound.getTagList("MovingPathNew", 10)));
		this.movingPos = compound.getInteger("MovingPos");
		this.movingPattern = compound.getInteger("MovingPatern");
		this.attackInvisible = compound.getBoolean("AttackInvisible");
		if (compound.hasKey("StartPosNew")) {
			int[] pos = compound.getIntArray("StartPosNew");
			startPos = new BlockPos(pos[0], pos[1], pos[2]);
		}
		npc.stepHeight = stepheight;
		if (standingType != 0 && standingType != 2) {
			npc.setRotationYawHead(orientation);
		}

		if (compound.hasKey("MaxHurtResistantTime", 3)) { maxHurtResistantTime = compound.getInteger("MaxHurtResistantTime"); }
		npc.maxHurtResistantTime = this.maxHurtResistantTime;
	}

	@Override
	public void setAnimation(int type) {
		this.animationType = type;
	}

	@Override
	public void setAttackInvisible(boolean attack) {
		this.attackInvisible = attack;
	}

	@Override
	public void setAttackLOS(boolean enabled) {
		this.directLOS = enabled;
		this.npc.updateAI = true;
	}

	@Override
	public void setAvoidsWater(boolean enabled) {
		if (this.npc.getNavigator() instanceof PathNavigateGround) {
			this.npc.setPathPriority(PathNodeType.WATER, enabled ? PathNodeType.WATER.getPriority() : 0.0f);
		}
		this.avoidsWater = enabled;
	}

	@Override
	public void setCanSwim(boolean canSwim) {
		this.canSwim = canSwim;
	}

	@Override
	public void setDoorInteract(int type) {
		this.doorInteract = type;
		this.npc.updateAI = true;
	}

	@Override
	public void setInteractWithNPCs(boolean interact) {
		this.npcInteracting = interact;
	}

	@Override
	public void setLeapAtTarget(boolean leap) {
		this.canLeap = leap;
		this.npc.updateAI = true;
	}

	public void setMovingPath(List<int[]> list) {
		movingPath = list;
		if (!movingPath.isEmpty()) {
			int[] pos = movingPath.get(0);
			startPos = new BlockPos(pos[0], pos[1], pos[2]);
		}
	}

	public void setMovingPathPos(int m_pos, int[] pos) {
		if (m_pos < 0) {
			m_pos = 0;
		}
		movingPath.set(m_pos, pos);
	}

	@Override
	public void setMovingPathType(int type, boolean pauses) {
		if (type != 0 && type != 1) {
			throw new CustomNPCsException("Moving path type: " + type);
		}
		this.movingPattern = type;
		this.movingPause = pauses;
	}

	public void setMovingPos(int pos) {
		this.movingPos = pos;
	}

	@Override
	public void setMovingType(int type) {
		if (type < 0 || type > 2) {
			throw new CustomNPCsException("Unknown moving type: " + type);
		}
		this.movingType = type;
		this.npc.updateAI = true;
	}

	@Override
	public void setNavigationType(int type) {
		this.movementType = type;
	}

	@Override
	public void setRetaliateType(int type) {
		if (type < 0 || type > 3) {
			throw new CustomNPCsException("[0 / 3] ]Unknown retaliation type: " + type);
		}
		this.onAttack = type;
		this.npc.updateAI = true;
	}

	@Override
	public void setReturnsHome(boolean bo) {
		this.returnToStart = bo;
	}

	@Override
	public void setSheltersFrom(int type) {
		this.findShelter = type;
		this.npc.updateAI = true;
	}

	@Override
	public void setStandingType(int type) {
		if (type < 0 || type > 4) {
			throw new CustomNPCsException("Unknown standing type: " + type);
		}
		this.standingType = type;
		this.npc.updateAI = true;
	}

	public void setStartPos(BlockPos pos) {
		startPos = pos;
	}

	public void setStartPos(double x, double y, double z) {
		startPos = new BlockPos(x, y, z);
	}

	public void setStartPos(IPos pos) {
		startPos = pos.getMCBlockPos();
	}

	@Override
	public void setStopOnInteract(boolean stopOnInteract) {
		stopAndInteract = stopOnInteract;
	}

	@Override
	public void setTacticalRange(int range) {
		tacticalRadius = range;
	}

	@Override
	public void setTacticalType(int type) {
		this.tacticalVariant = type;
		this.npc.updateAI = true;
	}

	@Override
	public void setWalkingSpeed(int speed) {
		if (speed < 0 || speed > 10) {
			throw new CustomNPCsException("Wrong speed: " + speed);
		}
		this.moveSpeed = speed;
		this.npc.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.npc.getSpeed());
		this.npc.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue((this.npc.getSpeed() * 2.0f));
	}

	@Override
	public void setWanderingRange(int range) {
		if (range < 1 || range > 50) {
			throw new CustomNPCsException("Bad wandering range: " + range + " (1 - 50)");
		}
		this.walkingRange = range;
	}

	@Override
	public void setMaxHurtResistantTime(int ticks) {
		if (ticks < 0) { ticks *= -1; }
		if (ticks > 1200) { ticks = 1200; }
		this.maxHurtResistantTime = ticks;
	}

	public boolean shouldReturnHome() {
		return (!(this.npc.advanced.jobInterface instanceof JobBuilder) || !((JobBuilder) this.npc.advanced.jobInterface).isBuilding()) && (!(this.npc.advanced.jobInterface instanceof JobFarmer) || !((JobFarmer) this.npc.advanced.jobInterface).isPlucking()) && this.returnToStart;
	}

	public BlockPos startPos() {
		if (startPos == null) { startPos = new BlockPos(npc); }
		return startPos;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		this.setAvoidsWater(this.avoidsWater);
		compound.setBoolean("CanSwim", this.canSwim);
		compound.setBoolean("ReactsToFire", this.reactsToFire);
		compound.setBoolean("AvoidsWater", this.avoidsWater);
		compound.setBoolean("AvoidsSun", this.avoidsSun);
		compound.setBoolean("ReturnToStart", this.returnToStart);
		compound.setInteger("OnAttack", this.onAttack);
		compound.setInteger("DoorInteract", this.doorInteract);
		compound.setInteger("FindShelter", this.findShelter);
		compound.setBoolean("DirectLOS", this.directLOS);
		compound.setBoolean("CanLeap", this.canLeap);
		compound.setBoolean("AIDisabled", this.aiDisabled);
		compound.setBoolean("CanSprint", this.canSprint);
		compound.setBoolean("CanBeCollide", this.canBeCollide);
		compound.setInteger("TacticalRadius", this.tacticalRadius);
		compound.setBoolean("MovingPause", this.movingPause);
		compound.setBoolean("npcInteracting", this.npcInteracting);
		compound.setBoolean("stopAndInteract", this.stopAndInteract);
		compound.setInteger("MoveState", this.animationType);
		compound.setInteger("StandingState", this.standingType);
		compound.setInteger("MovingState", this.movingType);
		compound.setInteger("TacticalVariant", this.tacticalVariant);
		compound.setInteger("MovementType", this.movementType);
		compound.setInteger("Orientation", this.orientation);
		compound.setFloat("PositionOffsetX", this.bodyOffsetX);
		compound.setFloat("PositionOffsetY", this.bodyOffsetY);
		compound.setFloat("PositionOffsetZ", this.bodyOffsetZ);
		compound.setFloat("StepHeight", this.stepheight);
		compound.setInteger("WalkingRange", this.walkingRange);
		compound.setInteger("MoveSpeed", this.moveSpeed);
		compound.setTag("MovingPathNew", NBTTags.nbtIntegerArraySet(this.movingPath));
		compound.setInteger("MovingPos", this.movingPos);
		compound.setInteger("MovingPatern", this.movingPattern);
		compound.setIntArray("StartPosNew", this.getStartArray());
		compound.setBoolean("AttackInvisible", this.attackInvisible);
		compound.setInteger("MaxHurtResistantTime", this.maxHurtResistantTime);
		return compound;
	}

	@Override
	public boolean isAIDisabled() { return aiDisabled; }

	@Override
	public void setIsAIDisabled(boolean bo) { this.aiDisabled = bo; }

	@Override
	public boolean canBeCollide() { return canBeCollide; }

	@Override
	public void setCanBeCollide(boolean bo) { this.canBeCollide = bo; }

}
