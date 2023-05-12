package noppes.npcs.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class EntityNPCFlying
extends EntityNPCInterface {
	
	public EntityNPCFlying(World world) {
		super(world);
	}

	@Override
	public boolean canFly() {
		return this.ais.movementType > 0;
	}

	@Override
	public void fall(float distance, float damageMultiplier) {
		if (!this.canFly()) {
			super.fall(distance, damageMultiplier);
		}
	}

	public boolean isOnLadder() {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void travel(float par1, float par2, float par3) {
		if (!this.canFly()) {
			super.travel(par1, par2, par3);
			return;
		}
		if (!this.isInWater() && this.ais.movementType == 2) {
			this.motionY = -0.15;
		}
		if (this.isInWater() && this.ais.movementType == 1) {
			this.moveRelative(par1, par2, par3, 0.02f);
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.800000011920929;
			this.motionY *= 0.800000011920929;
			this.motionZ *= 0.800000011920929;
		} else if (this.isInLava()) {
			this.moveRelative(par1, par2, par3, 0.02f);
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.5;
			this.motionY *= 0.5;
			this.motionZ *= 0.5;
		} else {
			float f2 = 0.91f;
			if (this.onGround) {
				f2 = this.world
						.getBlockState(new BlockPos(this.posX, this.getEntityBoundingBox().minY - 1.0, this.posZ))
						.getBlock().slipperiness * 0.91f;
			}
			float f3 = 0.16277136f / (f2 * f2 * f2);
			this.moveRelative(par1, par2, par3, this.onGround ? (0.1f * f3) : 0.02f);
			f2 = 0.91f;
			if (this.onGround) {
				f2 = this.world
						.getBlockState(new BlockPos(this.posX, this.getEntityBoundingBox().minY - 1.0, this.posZ))
						.getBlock().slipperiness * 0.91f;
			}
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= f2;
			this.motionY *= f2;
			this.motionZ *= f2;
		}
		this.prevLimbSwingAmount = this.limbSwingAmount;
		double d1 = this.posX - this.prevPosX;
		double d2 = this.posZ - this.prevPosZ;
		float f4 = MathHelper.sqrt(d1 * d1 + d2 * d2) * 4.0f;
		if (f4 > 1.0f) {
			f4 = 1.0f;
		}
		this.limbSwingAmount += (f4 - this.limbSwingAmount) * 0.4f;
		this.limbSwing += this.limbSwingAmount;
	}

	protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
		if (!this.canFly()) {
			super.updateFallState(y, onGroundIn, state, pos);
		}
	}
}
