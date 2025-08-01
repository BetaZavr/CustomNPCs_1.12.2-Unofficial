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
import noppes.npcs.util.Util;

public class NPCAttackSelector implements Predicate<EntityLivingBase> {

	private final EntityNPCInterface npc;

	public NPCAttackSelector(EntityNPCInterface npcIn) {
		npc = npcIn;
	}

	public boolean apply(EntityLivingBase entity) {
		if (entity == null ||
				!entity.isEntityAlive() ||
				entity == npc ||
				!npc.isInRange(entity, npc.stats.aggroRange) ||
				entity.getHealth() <= 0.1f ||
				!Util.instance.npcCanSeeTarget(npc, entity, false, true)
		)
		{ return false; }
		if (!npc.isFollower() && npc.ais.shouldReturnHome()) {
			int allowedDistance = npc.stats.aggroRange * 2;
			if (npc.ais.getMovingType() == 1) {
				allowedDistance += npc.ais.walkingRange;
			}
			double distance = entity.getDistanceSq(npc.getStartXPos(), npc.getStartYPos(),
					npc.getStartZPos());
			if (npc.ais.getMovingType() == 2) {
				int[] arr = npc.ais.getCurrentMovingPath();
				distance = entity.getDistanceSq(arr[0], arr[1], arr[2]);
			}
			if (distance > allowedDistance * allowedDistance) { return false; }
		}
		if (npc.advanced.jobInterface instanceof JobGuard && ((JobGuard) npc.advanced.jobInterface).isEntityApplicable(entity)) { return true; }
		if (npc.advanced.roleInterface instanceof RoleCompanion) {
			RoleCompanion role = (RoleCompanion) npc.advanced.roleInterface;
			if (role.job == EnumCompanionJobs.GUARD && ((CompanionGuard) role.jobInterface).isEntityApplicable(entity)) { return true; }
		}
		if (entity instanceof EntityPlayerMP) {
			EntityPlayerMP player = (EntityPlayerMP) entity;
			return npc.advanced.isAggressiveToPlayer((EntityPlayer) entity) && !player.capabilities.disableDamage;
		}
		if (entity instanceof EntityNPCInterface) {
			if (((EntityNPCInterface) entity).isKilled()) { return false; }
			return npc.advanced.isAggressiveToNpc((EntityNPCInterface) entity);
		}
		return false;
	}

}
