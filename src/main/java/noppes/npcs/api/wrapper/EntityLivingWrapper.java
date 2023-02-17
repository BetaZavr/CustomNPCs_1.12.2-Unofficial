package noppes.npcs.api.wrapper;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.api.IPos;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.entity.EntityNPCInterface;

@SuppressWarnings("rawtypes")
public class EntityLivingWrapper<T extends EntityLiving> extends EntityLivingBaseWrapper<T> implements IEntityLiving {
	public EntityLivingWrapper(T entity) {
		super(entity);
	}

	@Override
	public boolean canSeeEntity(IEntity entity) {
		return this.entity.getEntitySenses().canSee(entity.getMCEntity());
	}

	@Override
	public void clearNavigation() {
		this.entity.getNavigator().clearPath();
		if (this.entity instanceof EntityNPCInterface) {
			((EntityNPCInterface) this.entity).resetBackPos();
		} // New
	}

	@Override
	public IEntityLivingBase getAttackTarget() {
		IEntityLivingBase base = (IEntityLivingBase) NpcAPI.Instance().getIEntity(this.entity.getAttackTarget());
		return (base != null) ? base : super.getAttackTarget();
	}

	@Override
	public IPos getNavigationPath() {
		if (!this.isNavigating()) {
			return null;
		}
		PathPoint point = this.entity.getNavigator().getPath().getFinalPathPoint();
		if (point == null) {
			return null;
		}
		return new BlockPosWrapper(new BlockPos(point.x, point.y, point.z));
	}

	@Override
	public boolean isAttacking() {
		return super.isAttacking() || this.entity.getAttackTarget() != null;
	}

	@Override
	public boolean isNavigating() {
		return !this.entity.getNavigator().noPath();
	}

	@Override
	public void jump() {
		this.entity.getJumpHelper().setJumping();
	}

	@Override
	public void navigateTo(double x, double y, double z, double speed) {
		this.entity.getNavigator().clearPath();
		this.entity.getNavigator().tryMoveToXYZ(x, y, z, speed * 0.7);
		if (this.entity instanceof EntityNPCInterface) {
			((EntityNPCInterface) this.entity).resetBackPos();
		} // New
	}

	@Override
	public void setAttackTarget(IEntityLivingBase living) {
		if (living == null) {
			this.entity.setAttackTarget(null);
		} else {
			this.entity.setAttackTarget(living.getMCEntity());
		}
		super.setAttackTarget(living);
	}
}
