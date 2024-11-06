package noppes.npcs.ai;

import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAILook
		extends EntityAIBase {

	private boolean forced;
	private Entity forcedEntity;
	private int idle;
	private double lookX;
	private double lookZ;
	private final EntityNPCInterface npc;
	boolean rotatebody;
	public boolean fastRotation = false;

	public EntityAILook(EntityNPCInterface npc) {
		this.idle = 0;
		this.forced = false;
		this.forcedEntity = null;
		this.npc = npc;
		this.setMutexBits(AiMutex.LOOK);
	}

	public void resetTask() {
		this.rotatebody = false;
		this.forced = false;
		this.forcedEntity = null;
	}

	public void rotate(Entity entity) {
		this.forced = true;
		this.forcedEntity = entity;
	}

	public void rotate(int degrees) {
		this.forced = true;
		this.npc.renderYawOffset = degrees;
		this.npc.rotationYaw = degrees;
		this.npc.rotationYawHead = degrees;
	}

	public boolean shouldExecute() {
		return !this.npc.isAttacking() && this.npc.getNavigator().noPath() && !this.npc.isPlayerSleeping()
				&& this.npc.isEntityAlive() && (!CustomNpcs.ShowCustomAnimation || !this.npc.animation.isAnimated(AnimationKind.ATTACKING, AnimationKind.INIT, AnimationKind.INTERACT, AnimationKind.DIES));
	}

	public void startExecuting() {
		this.rotatebody = (this.npc.ais.getStandingType() == 0 || this.npc.ais.getStandingType() == 3 || this.npc.ais.getStandingType() == 4);
	}

	public void updateTask() {
		Entity lookat = null;
		// has Target Entity
		if (this.forced && this.forcedEntity != null) {
			lookat = this.forcedEntity;
		}
		else if (this.npc.isInteracting()) {
			Iterator<EntityLivingBase> ita = this.npc.interactingEntities.iterator();
			double closestDistance = 12.0;
			while (ita.hasNext()) {
				EntityLivingBase entity = ita.next();
				double distance = entity.getDistance(this.npc);
				if (distance < closestDistance) {
					closestDistance = entity.getDistance(this.npc);
					lookat = entity;
				} else {
					if (distance <= 12.0) {
						continue;
					}
					ita.remove();
				}
			}
		} else if (this.npc.ais.getStandingType() == 2 || this.npc.ais.getStandingType() == 4) {
			lookat = this.npc.world.getClosestPlayerToEntity(this.npc, 16.0);
		}
		if (lookat != null) {
			this.npc.updateLook = this.npc.lookAt == null || !this.npc.lookAt.equals(lookat);
			this.npc.lookAt = lookat;
			double posY;
			if (lookat instanceof EntityLivingBase) { posY = lookat.posY + (double)lookat.getEyeHeight(); }
			else { posY = (lookat.getEntityBoundingBox().minY + lookat.getEntityBoundingBox().maxY) / 2.0D; }
			this.setLookPosition(lookat.posX, posY, lookat.posZ, this.npc.getVerticalFaceSpeed());
			return;
		}

		// any Look
		this.npc.updateLook = this.npc.lookAt != null;
		this.npc.lookAt = null;
		if (this.rotatebody) {
			if (this.idle == 0 && this.npc.getRNG().nextFloat() < 0.004f) {
				double var1 = 6.283185307179586 * this.npc.getRNG().nextDouble();
				if (this.npc.ais.getStandingType() == 3) {
					var1 = 0.017453292519943295 * this.npc.ais.orientation + 0.6283185307179586 + 1.8849555921538759 * this.npc.getRNG().nextDouble();
				}
				this.lookX = Math.cos(var1);
				this.lookZ = Math.sin(var1);
				this.idle = 20 + this.npc.getRNG().nextInt(20);
			}
			if (this.idle > 0) {
				--this.idle;
				this.setLookPosition(this.npc.posX + this.lookX, this.npc.posY + this.npc.getEyeHeight(), this.npc.posZ + this.lookZ, this.npc.getVerticalFaceSpeed());
			}
		}
		if ((this.npc.ais.getStandingType() == 1 || this.npc.ais.getStandingType() == 4) && !this.forced) {
			this.npc.renderYawOffset = this.npc.ais.orientation;
			this.npc.rotationYaw = this.npc.ais.orientation;
			this.npc.rotationYawHead = this.npc.ais.orientation;
		}
	}

	private void setLookPosition(double x, double y, double z, int verticalFaceSpeed) {
		if (!CustomNpcs.ShowCustomAnimation || !this.npc.animation.isAnimated(AnimationKind.ATTACKING, AnimationKind.INIT, AnimationKind.INTERACT, AnimationKind.DIES)) {
			this.npc.getLookHelper().setLookPosition(x, y, z, 10.0f, verticalFaceSpeed);
		}
	}

}
