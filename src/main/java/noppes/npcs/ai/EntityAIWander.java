package noppes.npcs.ai;

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ai.selector.NPCInteractSelector;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIWander extends EntityAIBase {

	private final EntityNPCInterface entity;
	private EntityNPCInterface nearbyNPC;
	public Predicate<? super Entity> selector;
	private double x;
	private double y;
	private double zPosition;

	public EntityAIWander(EntityNPCInterface npc) {
		entity = npc;
		setMutexBits(AiMutex.PASSIVE);
		selector = new NPCInteractSelector(npc);
	}

	private EntityNPCInterface getNearbyNPC() {
		List<Entity> list = entity.world.getEntitiesInAABBexcluding(entity,
				entity.getEntityBoundingBox().grow(entity.ais.walkingRange,
						(entity.ais.walkingRange > 7) ? 7.0 : entity.ais.walkingRange,
						entity.ais.walkingRange),
				selector);
		Iterator<Entity> ita = list.iterator();
		while (ita.hasNext()) {
			EntityNPCInterface npc = (EntityNPCInterface) ita.next();
			if (!npc.ais.stopAndInteract || npc.isAttacking() || !npc.isEntityAlive()
					|| entity.faction.isAggressiveToNpc(npc)) {
				ita.remove();
			}
		}
		if (list.isEmpty()) {
			return null;
		}
		return (EntityNPCInterface) list.get(entity.getRNG().nextInt(list.size()));
	}

	private Vec3d getVec() {
		if (entity.ais.walkingRange <= 0) { return RandomPositionGenerator.findRandomTarget(entity, CustomNpcs.NpcNavRange, 7); }
		BlockPos start = new BlockPos(entity.getStartXPos(), entity.getStartYPos(), entity.getStartZPos());
		int distance = (int) MathHelper.sqrt(entity.getDistanceSq(start));
		int range = entity.ais.walkingRange - distance;
		if (range > CustomNpcs.NpcNavRange) { range = CustomNpcs.NpcNavRange; }
		if (range < 3) {
			if (distance >  entity.ais.walkingRange) {
				distance =  entity.ais.walkingRange;
			}
			if (distance > CustomNpcs.NpcNavRange) {
				distance = CustomNpcs.NpcNavRange;
			}
			Vec3d vec;
			for (int i = 0; i < 10; i++) {
				vec = RandomPositionGenerator.findRandomTarget(entity, distance, distance);
				if (vec == null) { continue; }
				double d0 = (double)start.getX() - vec.x;
				double d1 = (double)start.getY() - vec.y;
				double d2 = (double)start.getZ() - vec.z;
				int dist = (int) MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
				if (dist > entity.ais.walkingRange) { continue; }
				return vec;
			}
			return new Vec3d(entity.getStartXPos(), entity.getStartYPos(), entity.getStartZPos());
		}
		return RandomPositionGenerator.findRandomTarget(entity, range, Math.min(range, 7));
	}

	public void resetTask() {
		if (nearbyNPC != null && entity.isInRange(nearbyNPC, 3.5)) {
			EntityNPCInterface talk = entity;
			if (entity.getRNG().nextBoolean()) { talk = nearbyNPC; }
			Line line = talk.advanced.getNPCInteractLine();
			if (line == null) { line = new Line("... ... ..."); }
			line.setShowText(false);
			if (talk.isEntityAlive()) { talk.saySurrounding(line); }
			entity.addInteract(nearbyNPC);
			nearbyNPC.addInteract(entity);
		}
		nearbyNPC = null;
	}

	public boolean shouldContinueExecuting() {
		return (nearbyNPC == null
				|| (selector.apply(nearbyNPC) && !entity.isInRange(nearbyNPC, entity.width)))
				&& !entity.getNavigator().noPath() && entity.isEntityAlive() && !entity.isInteracting();
	}

	public boolean shouldExecute() {
		CustomNpcs.debugData.start(entity, this, "shouldExecute");
		if (entity.getIdleTime() >= 100 || !entity.getNavigator().noPath() || entity.isInteracting()
				|| entity.isRiding() || (entity.ais.movingPause && entity.getRNG().nextInt(80) != 0)) {
			CustomNpcs.debugData.end(entity, this, "shouldExecute");
			return false;
		}
		if (entity.ais.npcInteracting && entity.getRNG().nextInt(entity.ais.movingPause ? 6 : 16) == 1) {
			nearbyNPC = getNearbyNPC();
		}
		if (nearbyNPC != null) {
			x = MathHelper.floor(nearbyNPC.posX);
			y = MathHelper.floor(nearbyNPC.posY);
			zPosition = MathHelper.floor(nearbyNPC.posZ);
			nearbyNPC.addInteract(entity);
		}
		else {
			Vec3d vec = getVec();
			if (vec == null) {
				CustomNpcs.debugData.end(entity, this, "shouldExecute");
				return false;
			}
			x = vec.x;
			y = vec.y;
			if (entity.ais.movementType == 1) { y = entity.getStartYPos() + entity.getRNG().nextFloat() * 0.75 * entity.ais.walkingRange; }
			zPosition = vec.z;
		}
		CustomNpcs.debugData.end(entity, this, "shouldExecute");
		return true;
	}

	public void startExecuting() {
		entity.getNavigator().tryMoveToXYZ(x, y, zPosition, 1.0);
	}

	public void updateTask() {
		if (nearbyNPC != null) {
			nearbyNPC.getNavigator().clearPath();
		}
	}
}
