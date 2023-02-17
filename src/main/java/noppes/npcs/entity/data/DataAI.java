package noppes.npcs.entity.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.util.math.BlockPos;
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
	
	public int animationType;
	public boolean attackInvisible;
	public boolean avoidsSun;
	public boolean avoidsWater;
	public float bodyOffsetX;
	public float bodyOffsetY;
	public float bodyOffsetZ;
	public boolean canLeap;
	public boolean canSprint;
	public boolean canSwim;
	public boolean directLOS;
	public int doorInteract;
	public int findShelter;
	public int movementType;
	private int moveSpeed;
	private List<int[]> movingPath;
	public int movingPattern;
	public boolean movingPause;
	public int movingPos;
	private int movingType;
	private EntityNPCInterface npc;
	public boolean npcInteracting;
	public int onAttack;
	public int orientation;
	public boolean reactsToFire;
	public boolean returnToStart;
	private int standingType;
	private BlockPos startPos;
	public boolean stopAndInteract;
	private int tacticalRadius;
	public int tacticalVariant;
	public int walkingRange;

	public DataAI(EntityNPCInterface npc) {
		this.onAttack = 0;
		this.doorInteract = 2;
		this.findShelter = 2;
		this.canSwim = true;
		this.reactsToFire = false;
		this.avoidsWater = false;
		this.avoidsSun = false;
		this.returnToStart = true;
		this.directLOS = true;
		this.canLeap = false;
		this.canSprint = false;
		this.stopAndInteract = true;
		this.attackInvisible = false;
		this.tacticalVariant = 0;
		this.tacticalRadius = 8;
		this.movementType = 0;
		this.animationType = 0;
		this.standingType = 0;
		this.movingType = 0;
		this.npcInteracting = true;
		this.orientation = 0;
		this.bodyOffsetX = 5.0f;
		this.bodyOffsetY = 5.0f;
		this.bodyOffsetZ = 5.0f;
		this.walkingRange = 10;
		this.moveSpeed = 5;
		this.movingPath = new ArrayList<int[]>();
		this.startPos = null;
		this.movingPos = 0;
		this.movingPattern = 0;
		this.movingPause = true;
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
		int pos = this.movingPos;
		if (this.movingPattern == 0 && pos >= size) {
			boolean movingPos = false;
			this.movingPos = (movingPos ? 1 : 0);
			pos = (movingPos ? 1 : 0);
		}
		if (this.movingPattern == 1) {
			int size2 = size * 2 - 1;
			if (pos >= size2) {
				boolean movingPos2 = false;
				this.movingPos = (movingPos2 ? 1 : 0);
				pos = (movingPos2 ? 1 : 0);
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
		if (this.movingPath.isEmpty() && this.startPos != null) {
			this.movingPath.add(this.getStartArray());
		}
		return this.movingPath;
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

	@Override
	public int getMovingType() {
		return this.movingType;
	}

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

	@Override
	public int getStandingType() {
		return this.standingType;
	}

	public int[] getStartArray() {
		BlockPos pos = this.startPos();
		return new int[] { pos.getX(), pos.getY(), pos.getZ() };
	}

	public IPos getStartPos() {
		return new BlockPosWrapper(this.startPos());
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
		this.canSprint = compound.getBoolean("CanSprint");
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
		this.walkingRange = compound.getInteger("WalkingRange");
		this.setWalkingSpeed(compound.getInteger("MoveSpeed"));
		this.setMovingPath(NBTTags.getIntegerArraySet(compound.getTagList("MovingPathNew", 10)));
		this.movingPos = compound.getInteger("MovingPos");
		this.movingPattern = compound.getInteger("MovingPatern");
		this.attackInvisible = compound.getBoolean("AttackInvisible");
		if (compound.hasKey("StartPosNew")) {
			int[] startPos = compound.getIntArray("StartPosNew");
			this.startPos = new BlockPos(startPos[0], startPos[1], startPos[2]);
		}
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
		this.movingPath = list;
		if (!this.movingPath.isEmpty()) {
			int[] startPos = this.movingPath.get(0);
			this.startPos = new BlockPos(startPos[0], startPos[1], startPos[2]);
		}
	}

	public void setMovingPathPos(int m_pos, int[] pos) {
		if (m_pos < 0) {
			m_pos = 0;
		}
		this.movingPath.set(m_pos, pos);
	}

	@Override
	public void setMovingPathType(int type, boolean pauses) {
		if (type < 0 && type > 1) {
			throw new CustomNPCsException("Moving path type: " + type, new Object[0]);
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
			throw new CustomNPCsException("Unknown moving type: " + type, new Object[0]);
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
			throw new CustomNPCsException("[0 / 3] ]Unknown retaliation type: " + type, new Object[0]);
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
		if (type < 0 || type > 3) {
			throw new CustomNPCsException("Unknown standing type: " + type, new Object[0]);
		}
		this.standingType = type;
		this.npc.updateAI = true;
	}

	public void setStartPos(BlockPos pos) {
		this.startPos = pos;
	}

	public void setStartPos(double x, double y, double z) {
		this.startPos = new BlockPos(x, y, z);
	}

	public void setStartPos(IPos pos) {
		this.startPos = pos.getMCBlockPos();
	}

	@Override
	public void setStopOnInteract(boolean stopOnInteract) {
		this.stopAndInteract = stopOnInteract;
	}

	@Override
	public void setTacticalRange(int range) {
		this.tacticalRadius = range;
	}

	@Override
	public void setTacticalType(int type) {
		this.tacticalVariant = type;
		this.npc.updateAI = true;
	}

	@Override
	public void setWalkingSpeed(int speed) {
		if (speed < 0 || speed > 10) {
			throw new CustomNPCsException("Wrong speed: " + speed, new Object[0]);
		}
		this.moveSpeed = speed;
		this.npc.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(this.npc.getSpeed());
		this.npc.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue((this.npc.getSpeed() * 2.0f));
	}

	@Override
	public void setWanderingRange(int range) {
		if (range < 1 || range > 50) {
			throw new CustomNPCsException("Bad wandering range: " + range, new Object[0]);
		}
		this.walkingRange = range;
	}

	public boolean shouldReturnHome() {
		return (this.npc.advanced.job != 10 || !((JobBuilder) this.npc.jobInterface).isBuilding())
				&& (this.npc.advanced.job != 11 || !((JobFarmer) this.npc.jobInterface).isPlucking())
				&& this.returnToStart;
	}

	public BlockPos startPos() {
		if (this.startPos == null) {
			this.startPos = new BlockPos(this.npc);
		}
		return this.startPos;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
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
		compound.setBoolean("CanSprint", this.canSprint);
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
		compound.setInteger("WalkingRange", this.walkingRange);
		compound.setInteger("MoveSpeed", this.moveSpeed);
		compound.setTag("MovingPathNew", NBTTags.nbtIntegerArraySet(this.movingPath));
		compound.setInteger("MovingPos", this.movingPos);
		compound.setInteger("MovingPatern", this.movingPattern);
		this.setAvoidsWater(this.avoidsWater);
		compound.setIntArray("StartPosNew", this.getStartArray());
		compound.setBoolean("AttackInvisible", this.attackInvisible);
		return compound;
	}
}
