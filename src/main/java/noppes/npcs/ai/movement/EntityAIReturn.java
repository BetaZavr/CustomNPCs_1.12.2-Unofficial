package noppes.npcs.ai.movement;

import net.minecraft.command.CommandException;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

public class EntityAIReturn
extends EntityAIBase {
	
	public static int MaxTotalTicks = 100;
	private double endPosX;
	private double endPosY;
	private double endPosZ;
	private EntityNPCInterface npc;
	private double[] preAttackPos;
	private int stuckCount;
	private int stuckTicks;
	private int totalTicks;
	private boolean wasAttacked;

	public EntityAIReturn(EntityNPCInterface npc) {
		this.stuckTicks = 0;
		this.totalTicks = 0;
		this.wasAttacked = false;
		this.stuckCount = 0;
		this.npc = npc;
		this.setMutexBits(AiMutex.PASSIVE);
	}

	private boolean isTooFar() {
		if (this.npc.homeDimensionId != this.npc.world.provider.getDimension()) { return true; }
		int allowedDistance = this.npc.stats.aggroRange * 2;
		if (this.npc.ais.getMovingType() == 1) { allowedDistance += this.npc.ais.walkingRange; }
		double x = this.npc.posX - this.endPosX;
		double z = this.npc.posZ - this.endPosZ;
		return x * x + z * z > allowedDistance * allowedDistance;
	}

	private void navigate(boolean towards) {
		if (!this.wasAttacked) {
			this.endPosX = this.npc.getStartXPos();
			this.endPosY = this.npc.getStartYPos();
			this.endPosZ = this.npc.getStartZPos();
		} else {
			this.endPosX = this.preAttackPos[0];
			this.endPosY = this.preAttackPos[1];
			this.endPosZ = this.preAttackPos[2];
		}
		double posX = this.endPosX;
		double posY = this.endPosY;
		double posZ = this.endPosZ;
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
				Vec3d pos = RandomPositionGenerator.findRandomTargetBlockTowards((EntityCreature) this.npc, distance,
						(distance / 2 > 7) ? 7 : (distance / 2), start);
				if (pos != null) {
					posX = pos.x;
					posY = pos.y;
					posZ = pos.z;
				}
			}
		}
		this.npc.getNavigator().clearPath();
		if (this.npc.homeDimensionId != this.npc.world.provider.getDimension()) {
			try {
				AdditionalMethods.teleportEntity(this.npc.world.getMinecraftServer(), this.npc, this.npc.homeDimensionId, this.endPosX, this.endPosY, this.endPosZ);
			}
			catch (CommandException e) {
				e.printStackTrace();
				this.npc.getNavigator().tryMoveToXYZ(posX, posY, posZ, 1.0);
			}
		} else {
			this.npc.getNavigator().tryMoveToXYZ(posX, posY, posZ, 1.0);
		}
	}
	
	@Override
	public void resetTask() {
		this.wasAttacked = false;
		this.npc.getNavigator().clearPath();
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		return this.npc.getHealth() > 0 && !this.npc.isFollower() && !this.npc.isKilled() && !this.npc.isAttacking()
				&& !this.npc.isVeryNearAssignedPlace() && !this.npc.isInteracting() && !this.npc.isRiding()
				&& (!this.npc.getNavigator().noPath() || !this.wasAttacked || this.isTooFar())
				&& this.totalTicks <= EntityAIReturn.MaxTotalTicks;
	}
	
	@Override
	public boolean shouldExecute() {
		if (this.npc.hasOwner() || this.npc.isRiding() || !this.npc.ais.shouldReturnHome() || this.npc.isKilled() || !this.npc.getNavigator().noPath() || this.npc.isMoving() || this.npc.isInteracting()) {
			return false;
		}
		// AI Attack
		if (this.npc.aiOwnerNPC != null && !this.npc.getNavigator().noPath()) {
			this.totalTicks = 0;
			return false;
		}
		// AI Panic
		if (this.npc.ais.onAttack == 1) {
			if (this.npc.isBurning() || this.npc.getAttackTarget() != null) {
				this.totalTicks = 0;
				return false;
			}
		}
		// Shelter at Night
		if (this.npc.ais.findShelter == 0 && (!this.npc.world.isDaytime() || this.npc.world.isRaining()) && !this.npc.world.provider.hasSkyLight()) {
			BlockPos pos = new BlockPos(this.npc.getStartXPos(), this.npc.getStartYPos(), this.npc.getStartZPos());
			if (this.npc.world.canSeeSky(pos) || this.npc.world.getLight(pos) <= 8) {
				return false;
			}
		}
		// Shelter at Day
		else if (this.npc.ais.findShelter == 1 && this.npc.world.isDaytime()) {
			BlockPos pos = new BlockPos(this.npc.getStartXPos(), this.npc.getStartYPos(), this.npc.getStartZPos());
			if (this.npc.world.canSeeSky(pos)) {
				return false;
			}
		}
		if (this.npc.isAttacking()) {
			if (!this.wasAttacked) {
				this.wasAttacked = true;
				this.preAttackPos = new double[] { this.npc.posX, this.npc.posY, this.npc.posZ };
			}
			return false;
		}
		if (!this.npc.isAttacking() && this.wasAttacked) { return true; }
		
		if (this.npc.homeDimensionId != this.npc.world.provider.getDimension()) { return true; }
		switch(this.npc.ais.getMovingType()) {
			case 1: {
				return !this.npc.isInRange(this.npc.getStartXPos(), -1.0, this.npc.getStartZPos(), this.npc.ais.walkingRange);
			}
			case 2: {
				if (this.npc.ais.getDistanceSqToPathPoint() < CustomNpcs.NpcNavRange * CustomNpcs.NpcNavRange) { return false; }
				break;
			}
		}
		return !this.npc.isVeryNearAssignedPlace();
	}
	
	@Override
	public void startExecuting() {
		this.stuckTicks = 0;
		this.totalTicks = 0;
		this.stuckCount = 0;
		this.navigate(false);
	}
	
	@Override
	public void updateTask() {
		++this.totalTicks;
		if (this.totalTicks > EntityAIReturn.MaxTotalTicks) {
			this.npc.getNavigator().clearPath();
			if (this.npc.homeDimensionId != this.npc.world.provider.getDimension()) {
				try {
					AdditionalMethods.teleportEntity(this.npc.world.getMinecraftServer(), this.npc, this.npc.homeDimensionId, this.endPosX, this.endPosY, this.endPosZ);
				}
				catch (CommandException e) {
					e.printStackTrace();
					this.npc.setPosition(this.endPosX, this.endPosY, this.endPosZ);
					System.out.println("CNPCs: ");
				}
			} else {
				this.npc.setPosition(this.endPosX, this.endPosY, this.endPosZ);
				System.out.println("CNPCs: ");
			}
			return;
		}
		if (this.stuckTicks > 0) {
			--this.stuckTicks;
		} else if (this.npc.getNavigator().noPath()) {
			++this.stuckCount;
			this.stuckTicks = 10;
			if ((this.totalTicks > 30 && this.wasAttacked && this.isTooFar()) || this.stuckCount > 5) {
				this.npc.getNavigator().clearPath();
				if (this.npc.homeDimensionId != this.npc.world.provider.getDimension()) {
					try {
						AdditionalMethods.teleportEntity(this.npc.world.getMinecraftServer(), this.npc, this.npc.homeDimensionId, this.endPosX, this.endPosY, this.endPosZ);
					}
					catch (CommandException e) {
						e.printStackTrace();
						this.npc.setPosition(this.endPosX, this.endPosY, this.endPosZ);
					}
				} else {
					this.npc.setPosition(this.endPosX, this.endPosY, this.endPosZ);
				}
			} else {
				this.navigate(this.stuckCount % 2 == 1);
			}
		} else {
			this.stuckCount = 0;
		}
	}
}
