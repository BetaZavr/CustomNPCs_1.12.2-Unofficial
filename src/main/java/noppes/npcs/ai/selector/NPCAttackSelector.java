package noppes.npcs.ai.selector;

import com.google.common.base.Predicate;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import noppes.npcs.constants.EnumCompanionJobs;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobGuard;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.companion.CompanionGuard;

public class NPCAttackSelector implements Predicate<EntityLivingBase> {
	private EntityNPCInterface npc;

	public NPCAttackSelector(EntityNPCInterface npc) {
		this.npc = npc;
	}

	public boolean apply(EntityLivingBase ob) {
		return this.isEntityApplicable(ob);
	}

	public boolean isEntityApplicable(EntityLivingBase entity) {
		if (!entity.isEntityAlive() || entity == this.npc || !this.npc.isInRange(entity, this.npc.stats.aggroRange)
				|| entity.getHealth() < 1.0f) {
			return false;
		}
		if (this.npc.ais.directLOS && !this.npc.getEntitySenses().canSee(entity)) {
			return false;
		}
		if (!this.npc.ais.attackInvisible && entity.isPotionActive(MobEffects.INVISIBILITY)
				&& !this.npc.isInRange(entity, 3.0)) {
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
		if (this.npc.advanced.job == 3 && ((JobGuard) this.npc.jobInterface).isEntityApplicable(entity)) {
			return true;
		}
		if (this.npc.advanced.role == 6) {
			RoleCompanion role = (RoleCompanion) this.npc.roleInterface;
			if (role.job == EnumCompanionJobs.GUARD
					&& ((CompanionGuard) role.jobInterface).isEntityApplicable(entity)) {
				return true;
			}
		}
		if (entity instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) entity;
			return this.npc.faction.isAggressiveToPlayer((EntityPlayer) player) && !player.capabilities.disableDamage;
		}
		if (entity instanceof EntityNPCInterface) {
			if (((EntityNPCInterface) entity).isKilled()) {
				return false;
			}
			if (this.npc.advanced.attackOtherFactions) {
				return this.npc.faction.isAggressiveToNpc((EntityNPCInterface) entity);
			}
		}
		return false;
	}
}
