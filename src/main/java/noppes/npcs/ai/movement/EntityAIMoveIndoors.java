package noppes.npcs.ai.movement;

import java.util.Random;

import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIMoveIndoors extends EntityAIBase {

	private double shelterX;
	private double shelterY;
	private double shelterZ;
	private final EntityNPCInterface npc;
	private final World world;

	public EntityAIMoveIndoors(EntityNPCInterface npcIn) {
		npc = npcIn;
		world = npc.world;
		setMutexBits(AiMutex.PASSIVE);
	}

	private Vec3d findPossibleShelter() {
		Random random = npc.getRNG();
		BlockPos blockpos = new BlockPos(npc.posX, npc.getEntityBoundingBox().minY, npc.posZ);
		for (int i = 0; i < 10; ++i) {
			BlockPos blockpos2 = blockpos.add(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
			if (!world.canSeeSky(blockpos2) && npc.getBlockPathWeight(blockpos2) < 0.0f) {
				return new Vec3d(blockpos2.getX(), blockpos2.getY(), blockpos2.getZ());
			}
		}
		return null;
	}

	public boolean shouldContinueExecuting() {
		return !npc.getNavigator().noPath();
	}

	public boolean shouldExecute() {
		CustomNpcs.debugData.start(npc);
		if ((npc.world.isDaytime() && !npc.world.isRaining()) || npc.world.provider.hasSkyLight()) {
			CustomNpcs.debugData.end(npc);
			return false;
		}
		BlockPos pos = new BlockPos(npc.posX, npc.getEntityBoundingBox().minY, npc.posZ);
		if (!world.canSeeSky(pos) && world.getLight(pos) > 8) {
			CustomNpcs.debugData.end(npc);
			return false;
		}
		Vec3d var1 = findPossibleShelter();
		if (var1 == null) {
			CustomNpcs.debugData.end(npc);
			return false;
		}
		shelterX = var1.x;
		shelterY = var1.y;
		shelterZ = var1.z;
		CustomNpcs.debugData.end(npc);
		return true;
	}

	public void startExecuting() {
		CustomNpcs.debugData.start(npc);
		npc.getNavigator().tryMoveToXYZ(shelterX, shelterY, shelterZ, 1.0);
		CustomNpcs.debugData.end(npc);
	}
}
