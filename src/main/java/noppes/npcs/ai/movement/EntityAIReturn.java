package noppes.npcs.ai.movement;

import net.minecraft.command.CommandException;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class EntityAIReturn extends EntityAIBase {

	public static int MaxTotalTicks = 100;
	private double endPosX;
	private double endPosY;
	private double endPosZ;
	private final EntityNPCInterface npc;
	private double[] preAttackPos;
	private int stuckCount;
	private int stuckTicks;
	private int totalTicks;
	private boolean wasAttacked;

	public EntityAIReturn(EntityNPCInterface npc) {
		stuckTicks = 0;
		totalTicks = 0;
		wasAttacked = false;
		stuckCount = 0;
		this.npc = npc;
		setMutexBits(AiMutex.PASSIVE);
	}

	private boolean isTooFar() {
		if (npc.homeDimensionId != npc.world.provider.getDimension()) { return true; }
		int allowedDistance = npc.stats.aggroRange * 2;
		if (npc.ais.getMovingType() == 1) { allowedDistance += npc.ais.walkingRange; }
		return Util.instance.distanceTo(npc.posX, npc.posY, npc.posZ, endPosX, endPosY, endPosZ) > allowedDistance;
	}

	private void navigate(boolean towards) {
		if (!wasAttacked) {
			endPosX = npc.getStartXPos();
			endPosY = npc.getStartYPos();
			endPosZ = npc.getStartZPos();
		} else {
			endPosX = preAttackPos[0];
			endPosY = preAttackPos[1];
			endPosZ = preAttackPos[2];
		}
		double posX = endPosX;
		double posY = endPosY;
		double posZ = endPosZ;
		double range = this.npc.getDistance(posX, posY, posZ);
		if (range > CustomNpcs.NpcNavRange || towards) {
			int distance = (int) range;
			if (distance > CustomNpcs.NpcNavRange) {
				distance = CustomNpcs.NpcNavRange / 2;
			} else {
				distance /= 2;
			}
			if (distance > 2) {
				Vec3d start = new Vec3d(posX, posY, posZ);
				Vec3d pos = RandomPositionGenerator.findRandomTargetBlockTowards(npc, distance, Math.min(distance / 2, 7), start);
				if (pos != null) {
					posX = pos.x;
					posY = pos.y;
					posZ = pos.z;
				}
			}
		}
		tryBackHome(posX, posY, posZ);
	}

	private void tryBackHome(double endPosX, double endPosY, double endPosZ) {
		if (wasAttacked) { npc.setAttackTarget(null); }
		npc.getNavigator().clearPath();
		if (npc.homeDimensionId != this.npc.world.provider.getDimension()) {
			try {
				Util.instance.teleportEntity(npc.world.getMinecraftServer(), npc, npc.homeDimensionId, npc.getHomePosition());
			} catch (CommandException e) {
				LogWriter.error("Error teleport back home: ", e);
				npc.getNavigator().tryMoveToXYZ(endPosX, endPosY, endPosZ, 1.3);
			}
		} else {
			npc.getNavigator().tryMoveToXYZ(endPosX, endPosY, endPosZ, 1.3);
		}
	}

	@Override
	public void resetTask() {
		wasAttacked = false;
		npc.setAttackTarget(null);
	}

	@Override
	public boolean shouldContinueExecuting() {
		boolean bo = true;
		if (npc.ais.onAttack == 2) {
			double dist = Util.instance.distanceTo(npc.posX, npc.posY, npc.posZ, npc.getStartXPos(), npc.getStartYPos(), npc.getStartZPos());
			bo = dist > npc.stats.aggroRange;
		}
		return npc.getHealth() > 0 && !npc.isFollower() && !npc.isKilled() && !npc.isAttacking() && !npc.advanced.jobInterface.isWorking()
				&& !npc.isVeryNearAssignedPlace() && !npc.isInteracting() && !npc.isRiding()
				&& (!npc.getNavigator().noPath() || !wasAttacked || isTooFar())
				&& totalTicks <= EntityAIReturn.MaxTotalTicks && bo;
	}

	@Override
	public boolean shouldExecute() {
		CustomNpcs.debugData.start(npc, this, "shouldExecute");
		if (npc.hasOwner() || npc.isRiding() || !npc.ais.shouldReturnHome() || npc.isKilled() || !npc.getNavigator().noPath() || npc.isMoving() || npc.isInteracting()) {
			CustomNpcs.debugData.end(npc, this, "shouldExecute");
			return false;
		}
		// AI Attack
		if (npc.aiOwnerNPC != null && !npc.getNavigator().noPath()) {
			totalTicks = 0;
			CustomNpcs.debugData.end(npc, this, "shouldExecute");
			return false;
		}
		// AI Panic
		if (npc.ais.onAttack == 1) {
			if (npc.isBurning() || npc.getAttackTarget() != null) {
				totalTicks = 0;
				CustomNpcs.debugData.end(npc, this, "shouldExecute");
				return false;
			}
		}
		// Shelter at Night
		if (npc.ais.findShelter == 0 && (!npc.world.isDaytime() || npc.world.isRaining()) && npc.world.provider.hasSkyLight()) {
			if (npc.world.getLight(npc.getPosition()) < 10) {
				CustomNpcs.debugData.end(npc, this, "shouldExecute");
				return true;
			}
		}
		// Shelter at Day
		else if (npc.ais.findShelter == 1 && npc.world.isDaytime() && npc.world.canSeeSky(npc.getPosition())) {
			CustomNpcs.debugData.end(npc, this, "shouldExecute");
			return true;
		}
		if (npc.isAttacking()) {
			if (!wasAttacked) {
				wasAttacked = true;
				preAttackPos = new double[] { npc.posX, npc.posY, npc.posZ };
			}
			CustomNpcs.debugData.end(npc, this, "shouldExecute");
			return isTooFar();
		}
		if (!npc.isAttacking() && wasAttacked) {
			CustomNpcs.debugData.end(npc, this, "shouldExecute");
			return true;
		}
		if (npc.homeDimensionId != npc.world.provider.getDimension()) {
			CustomNpcs.debugData.end(npc, this, "shouldExecute");
			return true;
		}
		switch (npc.ais.getMovingType()) {
			case 1: {
				CustomNpcs.debugData.end(npc, this, "shouldExecute");
				return !npc.isInRange(npc.getStartXPos(), -1.0, npc.getStartZPos(),
						npc.ais.walkingRange);
			}
			case 2: {
				if (npc.ais.getDistanceSqToPathPoint() < CustomNpcs.NpcNavRange * CustomNpcs.NpcNavRange) {
					CustomNpcs.debugData.end(npc, this, "shouldExecute");
					return false;
				}
				break;
			}
		}
		CustomNpcs.debugData.end(npc, this, "shouldExecute");
		return !npc.isVeryNearAssignedPlace();
	}

	@Override
	public void startExecuting() {
		stuckTicks = 0;
		totalTicks = 0;
		stuckCount = 0;
		navigate(false);
	}

	@Override
	public void updateTask() {
		CustomNpcs.debugData.start(npc, this, "updateTask");
		++totalTicks;
		if (totalTicks > EntityAIReturn.MaxTotalTicks) {
			tryBackHome(endPosX, endPosY, endPosZ);
			CustomNpcs.debugData.end(npc, this, "updateTask");
			return;
		}
		if (stuckTicks > 0) { --stuckTicks; }
		else if (npc.getNavigator().noPath()) {
			++stuckCount;
			stuckTicks = 10;
			if ((totalTicks > 30 && wasAttacked && isTooFar()) || stuckCount > 5) {
				tryBackHome(endPosX, endPosY, endPosZ);
			} else {
				navigate(this.stuckCount % 2 == 1);
			}
		} else {
			stuckCount = 0;
		}
		CustomNpcs.debugData.end(npc, this, "updateTask");
	}

	@SuppressWarnings("all")
	public BlockPos getEndPositions() {
		if (!wasAttacked) {
			return new BlockPos(npc.getStartXPos(), npc.getStartYPos(), npc.getStartZPos());
		}
		return new BlockPos(preAttackPos[0], preAttackPos[1], preAttackPos[2]);
	}

}
