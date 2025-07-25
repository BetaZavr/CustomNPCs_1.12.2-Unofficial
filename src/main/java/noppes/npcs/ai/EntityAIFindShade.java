package noppes.npcs.ai;

import java.util.Random;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.AiMutex;

public class EntityAIFindShade extends EntityAIBase {

	private double shelterX;
	private double shelterY;
	private double shelterZ;
	private final EntityCreature theCreature;
	private final World world;

	public EntityAIFindShade(EntityCreature par1EntityCreature) {
		theCreature = par1EntityCreature;
		world = par1EntityCreature.world;
		setMutexBits(AiMutex.PASSIVE);
	}

	private Vec3d findPossibleShelter() {
		Random random = theCreature.getRNG();
		BlockPos blockpos = new BlockPos(theCreature.posX, theCreature.getEntityBoundingBox().minY, theCreature.posZ);
		for (int i = 0; i < 10; ++i) {
			BlockPos blockpos2 = blockpos.add(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
			if (!world.canSeeSky(blockpos2) && theCreature.getBlockPathWeight(blockpos2) < 0.0f) {
				return new Vec3d(blockpos2.getX(), blockpos2.getY(), blockpos2.getZ());
			}
		}
		return null;
	}

	public boolean shouldContinueExecuting() {
		return !theCreature.getNavigator().noPath();
	}

	public boolean shouldExecute() {
		CustomNpcs.debugData.start(theCreature);
		if (!world.isDaytime()) {
			CustomNpcs.debugData.end(theCreature);
			return false;
		}
		if (!world.canSeeSky(new BlockPos(theCreature.posX, theCreature.getEntityBoundingBox().minY, theCreature.posZ))) {
			CustomNpcs.debugData.end(theCreature);
			return false;
		}
		Vec3d var1 = findPossibleShelter();
		if (var1 == null) {
			CustomNpcs.debugData.end(theCreature);
			return false;
		}
		shelterX = var1.x;
		shelterY = var1.y;
		shelterZ = var1.z;
		CustomNpcs.debugData.end(theCreature);
		return true;
	}

	public void startExecuting() {
		theCreature.getNavigator().tryMoveToXYZ(shelterX, shelterY, shelterZ, 1.0);
	}
}
