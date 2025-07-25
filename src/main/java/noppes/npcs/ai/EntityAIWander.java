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

	private final EntityNPCInterface npc;
	private EntityNPCInterface nearbyNPC;
	public Predicate<? super Entity> selector;
	private double x;
	private double y;
	private double zPosition;

	public EntityAIWander(EntityNPCInterface npcIn) {
		npc = npcIn;
		setMutexBits(AiMutex.PASSIVE);
		selector = new NPCInteractSelector(npc);
	}

	private EntityNPCInterface getNearbyNPC() {
		List<Entity> list = npc.world.getEntitiesInAABBexcluding(npc,
				npc.getEntityBoundingBox().grow(npc.ais.walkingRange,
						(npc.ais.walkingRange > 7) ? 7.0 : npc.ais.walkingRange,
						npc.ais.walkingRange),
				selector);
		Iterator<Entity> ita = list.iterator();
		while (ita.hasNext()) {
			EntityNPCInterface npc = (EntityNPCInterface) ita.next();
			if (!npc.ais.stopAndInteract || npc.isAttacking() || !npc.isEntityAlive()
					|| npc.faction.isAggressiveToNpc(npc)) {
				ita.remove();
			}
		}
		if (list.isEmpty()) {
			return null;
		}
		return (EntityNPCInterface) list.get(npc.getRNG().nextInt(list.size()));
	}

	private Vec3d getVec() {
		if (npc.ais.walkingRange <= 0) { return RandomPositionGenerator.findRandomTarget(npc, CustomNpcs.NpcNavRange, 7); }
		BlockPos start = new BlockPos(npc.getStartXPos(), npc.getStartYPos(), npc.getStartZPos());
		int distance = (int) MathHelper.sqrt(npc.getDistanceSq(start));
		int range = npc.ais.walkingRange - distance;
		if (range > CustomNpcs.NpcNavRange) { range = CustomNpcs.NpcNavRange; }
		if (range < 3) {
			if (distance >  npc.ais.walkingRange) {
				distance =  npc.ais.walkingRange;
			}
			if (distance > CustomNpcs.NpcNavRange) {
				distance = CustomNpcs.NpcNavRange;
			}
			Vec3d vec;
			for (int i = 0; i < 10; i++) {
				vec = RandomPositionGenerator.findRandomTarget(npc, distance, distance);
				if (vec == null) { continue; }
				double d0 = (double)start.getX() - vec.x;
				double d1 = (double)start.getY() - vec.y;
				double d2 = (double)start.getZ() - vec.z;
				int dist = (int) MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
				if (dist > npc.ais.walkingRange) { continue; }
				return vec;
			}
			return new Vec3d(npc.getStartXPos(), npc.getStartYPos(), npc.getStartZPos());
		}
		return RandomPositionGenerator.findRandomTarget(npc, range, Math.min(range, 7));
	}

	public void resetTask() {
		CustomNpcs.debugData.start(npc);
		if (nearbyNPC != null && npc.isInRange(nearbyNPC, 3.5)) {
			EntityNPCInterface talk = npc;
			if (npc.getRNG().nextBoolean()) { talk = nearbyNPC; }
			Line line = talk.advanced.getNPCInteractLine();
			if (line == null) { line = new Line("... ... ..."); }
			line.setShowText(false);
			if (talk.isEntityAlive()) { talk.saySurrounding(line); }
			npc.addInteract(nearbyNPC);
			nearbyNPC.addInteract(npc);
		}
		nearbyNPC = null;
		CustomNpcs.debugData.end(npc);
	}

	public boolean shouldContinueExecuting() {
		return (nearbyNPC == null
				|| (selector.apply(nearbyNPC) && !npc.isInRange(nearbyNPC, npc.width)))
				&& !npc.getNavigator().noPath() && npc.isEntityAlive() && !npc.isInteracting();
	}

	public boolean shouldExecute() {
		CustomNpcs.debugData.start(npc);
		if (npc.getIdleTime() >= 100 || !npc.getNavigator().noPath() || npc.isInteracting()
				|| npc.isRiding() || (npc.ais.movingPause && npc.getRNG().nextInt(80) != 0)) {
			CustomNpcs.debugData.end(npc);
			return false;
		}
		if (npc.ais.npcInteracting && npc.getRNG().nextInt(npc.ais.movingPause ? 6 : 16) == 1) {
			nearbyNPC = getNearbyNPC();
		}
		if (nearbyNPC != null) {
			x = MathHelper.floor(nearbyNPC.posX);
			y = MathHelper.floor(nearbyNPC.posY);
			zPosition = MathHelper.floor(nearbyNPC.posZ);
			nearbyNPC.addInteract(npc);
		}
		else {
			Vec3d vec = getVec();
			if (vec == null) {
				CustomNpcs.debugData.end(npc);
				return false;
			}
			x = vec.x;
			y = vec.y;
			if (npc.ais.movementType == 1) { y = npc.getStartYPos() + npc.getRNG().nextFloat() * 0.75 * npc.ais.walkingRange; }
			zPosition = vec.z;
		}
		CustomNpcs.debugData.end(npc);
		return true;
	}

	public void startExecuting() {
		npc.getNavigator().tryMoveToXYZ(x, y, zPosition, 1.0);
	}

	public void updateTask() {
		if (nearbyNPC != null) {
			nearbyNPC.getNavigator().clearPath();
		}
	}
}
