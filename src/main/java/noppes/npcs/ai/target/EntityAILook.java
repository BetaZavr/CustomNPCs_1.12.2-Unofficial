package noppes.npcs.ai.target;

import java.util.Iterator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.util.IRayTraceRotate;
import noppes.npcs.constants.AiMutex;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class EntityAILook extends EntityAIBase {

	private boolean forced;
	private Entity forcedEntity;
	private int idle;
	private double lookX;
	private double lookY;
	private double lookZ;
	private final EntityNPCInterface npc;
	boolean rotateBody;
	public boolean fastRotation = false;

	public EntityAILook(EntityNPCInterface npcIn) {
		idle = 0;
		forced = false;
		forcedEntity = null;
		npc = npcIn;
		setMutexBits(AiMutex.LOOK);
	}

	public void resetTask() {
		rotateBody = false;
		forced = false;
		forcedEntity = null;
	}

	public void rotate(Entity entity) {
		forced = true;
		forcedEntity = entity;
	}

	public void rotate(int degrees) {
		forced = true;
		npc.renderYawOffset = degrees;
		npc.rotationYaw = degrees;
		npc.rotationYawHead = degrees;
	}

	public boolean shouldExecute() {
		return !npc.isAttacking() && npc.getNavigator().noPath() && !npc.isPlayerSleeping()
				&& npc.isEntityAlive() && (!CustomNpcs.ShowCustomAnimation || !npc.animation.isAnimated(AnimationKind.ATTACKING, AnimationKind.INIT, AnimationKind.INTERACT, AnimationKind.DIES));
	}

	public void startExecuting() {
		rotateBody = (npc.ais.getStandingType() == 0 || npc.ais.getStandingType() == 3);
	}

	public void updateTask() {
		CustomNpcs.debugData.start(npc);
		Entity lookat = null;
		// has Target Entity
		if (forced && forcedEntity != null) { lookat = forcedEntity; }
		else if (npc.isInteracting()) {
			Iterator<EntityLivingBase> ita = npc.interactingEntities.iterator();
			double closestDistance = 12.0;
			while (ita.hasNext()) {
				EntityLivingBase entity = ita.next();
				double distance = entity.getDistance(npc);
				if (distance < closestDistance) {
					closestDistance = entity.getDistance(npc);
					lookat = entity;
				} else {
					if (distance <= 12.0) {
						continue;
					}
					ita.remove();
				}
			}
		}
		else if (npc.ais.getStandingType() == 2 || npc.ais.getStandingType() == 4) {
			lookat = npc.world.getClosestPlayerToEntity(npc, 16.0);
		}
		// looking at someone
		if (lookat != null) {
			npc.updateLook = npc.lookAt == null || !npc.lookAt.equals(lookat);
			npc.lookAt = lookat;
			double posY;
			if (lookat instanceof EntityLivingBase) { posY = lookat.posY + (double) lookat.getEyeHeight(); }
			else { posY = (lookat.getEntityBoundingBox().minY + lookat.getEntityBoundingBox().maxY) / 2.0D; }
			setLookPosition(lookat.posX, posY, lookat.posZ, npc.getVerticalFaceSpeed());
			CustomNpcs.debugData.end(npc);
			return;
		}
		// looks in a random direction
		npc.updateLook = npc.lookAt != null;
		npc.lookAt = null;
		if (rotateBody) {
			if (idle == 0 && npc.getRNG().nextFloat() < 0.004f) {
				double d0 = Math.PI * npc.getRNG().nextDouble() * 2.0;
				if (npc.ais.getStandingType() == 3) { // only head
					d0 = 0.017453292519943295 * npc.ais.orientation + Math.PI / 5.0 + 1.8849555921538759 * npc.getRNG().nextDouble();
				}
				lookX = Math.cos(d0);
				lookY = (npc.getRNG().nextFloat() - 0.5f) * 0.85f;
				lookZ = Math.sin(d0);

				IRayTraceRotate data = Util.instance.getAngles3D(npc.posX, npc.posY, npc.posZ, lookX, lookY, lookZ);
				npc.lookPos[0] = (float) data.getYaw();
				npc.lookPos[1] = (float) data.getPitch();
				npc.updateClient();
				idle = 20 + npc.getRNG().nextInt(20);
			} else if (npc.ais.getStandingType() == 3 || npc.ais.getStandingType() == 0) {
				if (lookX != 0.0f && lookY != 0.0f && lookZ != 0.0f) {
					setLookPosition(npc.posX + lookX, npc.posY + npc.getEyeHeight() + lookY, npc.posZ + lookZ, npc.getVerticalFaceSpeed());
				}
			}
			if (idle > 0) {
				--idle;
				setLookPosition(npc.posX + lookX, npc.posY + npc.getEyeHeight() + lookY, npc.posZ + lookZ, npc.getVerticalFaceSpeed());
			}
		}
		// doesn't look at anyone
		if ((npc.ais.getStandingType() == 1 || npc.ais.getStandingType() == 4) && !forced) {
			npc.renderYawOffset = npc.ais.orientation;
			npc.rotationYaw = npc.ais.orientation;
			npc.rotationYawHead = npc.ais.orientation;
		}
		CustomNpcs.debugData.end(npc);
	}

	private void setLookPosition(double x, double y, double z, int verticalFaceSpeed) {
		if (!CustomNpcs.ShowCustomAnimation || !npc.animation.isAnimated(AnimationKind.ATTACKING, AnimationKind.INIT, AnimationKind.INTERACT, AnimationKind.DIES)) {
			npc.getLookHelper().setLookPosition(x, y, z, 10.0f, verticalFaceSpeed);
		}
	}

}
