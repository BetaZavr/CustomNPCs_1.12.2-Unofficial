package noppes.npcs.ai.selector;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.constants.EnumCompanionJobs;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobGuard;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.companion.CompanionGuard;
import noppes.npcs.util.AdditionalMethods;

public class NPCAttackSelector implements Predicate<EntityLivingBase> {
	private EntityNPCInterface npc;

	// EntityAIClosestTarget <- EntityNPCInterface.updateTasks()
	
	public NPCAttackSelector(EntityNPCInterface npc) {
		this.npc = npc;
	}

	public boolean apply(EntityLivingBase ob) {
		return this.isEntityApplicable(ob);
	}

	public boolean isEntityApplicable(EntityLivingBase entity) {
		if (!entity.isEntityAlive() || entity == this.npc || this.npc.isRunHome || !this.npc.isInRange(entity, this.npc.stats.aggroRange) || entity.getHealth() < 1.0f) {
			return false;
		}
		if (!AdditionalMethods.npcCanSeeTarget(this.npc, entity, false)) {
			return false;
		}
		if (!this.npc.isFollower() && this.npc.ais.shouldReturnHome()) {
			int allowedDistance = this.npc.stats.aggroRange * 2;
			if (this.npc.ais.getMovingType() == 1) {
				allowedDistance += this.npc.ais.walkingRange;
			}
			double distance = entity.getDistanceSq(this.npc.getStartXPos(), this.npc.getStartYPos(),
					this.npc.getStartZPos());
			if (this.npc.ais.getMovingType() == 2) {
				int[] arr = this.npc.ais.getCurrentMovingPath();
				distance = entity.getDistanceSq(arr[0], arr[1], arr[2]);
			}
			if (distance > allowedDistance * allowedDistance) {
				return false;
			}
		}
		if (this.npc.advanced.jobInterface instanceof JobGuard && ((JobGuard) this.npc.advanced.jobInterface).isEntityApplicable(entity)) {
			return true;
		}
		if (this.npc.advanced.roleInterface instanceof RoleCompanion) {
			RoleCompanion role = (RoleCompanion) this.npc.advanced.roleInterface;
			if (role.job == EnumCompanionJobs.GUARD && ((CompanionGuard) role.jobInterface).isEntityApplicable(entity)) {
				return true;
			}
		}
		if (entity instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) entity;
			return this.npc.advanced.isAggressiveToPlayer((EntityPlayer) entity) && !player.capabilities.disableDamage;
		}
		if (entity instanceof EntityNPCInterface) {
			if (((EntityNPCInterface) entity).isKilled()) { return false; }
			return this.npc.advanced.isAggressiveToNpc((EntityNPCInterface) entity);
		}
		return false;
	}
}
