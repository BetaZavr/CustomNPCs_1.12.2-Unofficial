package noppes.npcs.api.wrapper;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.EntityType;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.entity.EntityProjectile;

@SuppressWarnings("rawtypes")
public class ProjectileWrapper<T extends EntityProjectile>
extends ThrowableWrapper<T>
implements IProjectile {
	
	public ProjectileWrapper(T entity) {
		super(entity);
	}

	@Override
	public void enableEvents() {
		if (ScriptContainer.Current == null) {
			throw new CustomNPCsException("Can only be called during scripts", new Object[0]);
		}
		if (!this.entity.scripts.contains(ScriptContainer.Current)) {
			this.entity.scripts.add(ScriptContainer.Current);
		}
	}

	@Override
	public int getAccuracy() {
		return this.entity.accuracy;
	}

	@Override
	public boolean getHasGravity() {
		return this.entity.hasGravity();
	}

	@Override
	public IItemStack getItem() {
		return NpcAPI.Instance().getIItemStack(this.entity.getItemDisplay());
	}

	@Override
	public int getType() {
		return EntityType.PROJECTILE.get();
	}

	@Override
	public void setAccuracy(int accuracy) {
		this.entity.accuracy = accuracy;
	}

	@Override
	public void setHasGravity(boolean bo) {
		this.entity.setHasGravity(bo);
	}

	@Override
	public void setHeading(double x, double y, double z) {
		x -= this.entity.posX;
		y -= this.entity.posY;
		z -= this.entity.posZ;
		float varF = this.entity.hasGravity() ? MathHelper.sqrt(x * x + z * z) : 0.0f;
		float angle = this.entity.getAngleForXYZ(x, y, z, varF, false);
		float acc = 20.0f - MathHelper.floor(this.entity.accuracy / 5.0f);
		this.entity.shoot(x, y, z, angle, acc);
	}

	@Override
	public void setHeading(float yaw, float pitch) {
		EntityProjectile entityProjectile = this.entity;
		this.entity.rotationYaw = yaw;
		entityProjectile.prevRotationYaw = yaw;
		EntityProjectile entityProjectile2 = this.entity;
		this.entity.rotationPitch = pitch;
		entityProjectile2.prevRotationPitch = pitch;
		double varX = -MathHelper.sin(yaw / 180.0f * 3.1415927f) * MathHelper.cos(pitch / 180.0f * 3.1415927f);
		double varZ = MathHelper.cos(yaw / 180.0f * 3.1415927f) * MathHelper.cos(pitch / 180.0f * 3.1415927f);
		double varY = -MathHelper.sin(pitch / 180.0f * 3.1415927f);
		float acc = 20.0f - MathHelper.floor(this.entity.accuracy / 5.0f);
		this.entity.shoot(varX, varY, varZ, -pitch, acc);
	}

	@Override
	public void setHeading(IEntity entity) {
		this.setHeading(entity.getX(), entity.getMCEntity().getEntityBoundingBox().minY + entity.getHeight() / 2.0f,
				entity.getZ());
	}

	@Override
	public void setItem(IItemStack item) {
		if (item == null) {
			this.entity.setThrownItem(ItemStack.EMPTY);
		} else {
			this.entity.setThrownItem(item.getMCItemStack());
		}
	}

	@Override
	public boolean typeOf(int type) {
		return type == EntityType.PROJECTILE.get() || super.typeOf(type);
	}
}
