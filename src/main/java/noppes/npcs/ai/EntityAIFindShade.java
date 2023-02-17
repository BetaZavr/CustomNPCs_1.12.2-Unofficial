package noppes.npcs.ai;

import java.util.Random;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import noppes.npcs.constants.AiMutex;

public class EntityAIFindShade extends EntityAIBase {
	private double shelterX;
	private double shelterY;
	private double shelterZ;
	private EntityCreature theCreature;
	private World world;

	public EntityAIFindShade(EntityCreature par1EntityCreature) {
		this.theCreature = par1EntityCreature;
		this.world = par1EntityCreature.world;
		this.setMutexBits(AiMutex.PASSIVE);
	}

	private Vec3d findPossibleShelter() {
		Random random = this.theCreature.getRNG();
		BlockPos blockpos = new BlockPos(this.theCreature.posX, this.theCreature.getEntityBoundingBox().minY,
				this.theCreature.posZ);
		for (int i = 0; i < 10; ++i) {
			BlockPos blockpos2 = blockpos.add(random.nextInt(20) - 10, random.nextInt(6) - 3, random.nextInt(20) - 10);
			if (!this.world.canSeeSky(blockpos2) && this.theCreature.getBlockPathWeight(blockpos2) < 0.0f) {
				return new Vec3d(blockpos2.getX(), blockpos2.getY(), blockpos2.getZ());
			}
		}
		return null;
	}

	public boolean shouldContinueExecuting() {
		return !this.theCreature.getNavigator().noPath();
	}

	public boolean shouldExecute() {
		if (!this.world.isDaytime()) {
			return false;
		}
		if (!this.world.canSeeSky(new BlockPos(this.theCreature.posX, this.theCreature.getEntityBoundingBox().minY,
				this.theCreature.posZ))) {
			return false;
		}
		Vec3d var1 = this.findPossibleShelter();
		if (var1 == null) {
			return false;
		}
		this.shelterX = var1.x;
		this.shelterY = var1.y;
		this.shelterZ = var1.z;
		return true;
	}

	public void startExecuting() {
		this.theCreature.getNavigator().tryMoveToXYZ(this.shelterX, this.shelterY, this.shelterZ, 1.0);
	}
}
