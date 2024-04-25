package noppes.npcs.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.client.model.part.ModelData;
import noppes.npcs.constants.EnumParts;

public class EntityCustomNpc extends EntityNPCFlying {

	public ModelData modelData;

	public EntityCustomNpc(World world) {
		super(world);
		this.modelData = new ModelData();
		if (!CustomNpcs.EnableDefaultEyes) {
			this.modelData.eyes.type = -1;
		}
	}

	@Override
	public void onUpdate() {
		super.onUpdate();
		if (this.isRemote()) {
			ModelPartData particles = this.modelData.getPartData(EnumParts.PARTICLES);
			if (particles != null && !this.isKilled()) {
				CustomNpcs.proxy.spawnParticle(this, "ModelData", this.modelData, particles);
			}
			EntityLivingBase entity = this.modelData.getEntity(this);
			if (entity != null) {
				try {
					entity.onUpdate();
				} catch (Exception ex) {
				}
				EntityUtil.Copy(this, entity);
			}
		}
		this.modelData.eyes.update(this);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("NpcModelData")) {
			this.modelData.readFromNBT(compound.getCompoundTag("NpcModelData"));
		}
		super.readEntityFromNBT(compound);
	}

	public boolean startRiding(Entity par1Entity, boolean force) {
		boolean b = super.startRiding(par1Entity, force);
		this.updateHitbox();
		return b;
	}

	@Override
	public void updateHitbox() {
		Entity entity = this.modelData.getEntity(this);
		if (this.modelData == null || entity == null) {
			this.baseHeight = 1.9f - this.modelData.getBodyY()
					+ (this.modelData.getPartConfig(EnumParts.HEAD).scaleBase[1] - 1.0f) / 2.0f;
			super.updateHitbox();
		} else {
			if (entity instanceof EntityNPCInterface) {
				((EntityNPCInterface) entity).updateHitbox();
			}
			this.width = entity.width / 5.0f * this.display.getSize();
			this.height = entity.height / 5.0f * this.display.getSize();
			if (this.width < 0.1f) {
				this.width = 0.1f;
			}
			if (this.height < 0.1f) {
				this.height = 0.1f;
			}
			if (!this.display.getHasHitbox() || (this.isKilled() && this.stats.hideKilledBody)) {
				this.width = 1.0E-5f;
			}
			double n = this.width / 2.0f;
			if (n > World.MAX_ENTITY_RADIUS) {
				World.MAX_ENTITY_RADIUS = this.width / 2.0f;
			}
			this.setPosition(this.posX, this.posY, this.posZ);
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound compound) {
		super.writeEntityToNBT(compound);
		compound.setTag("NpcModelData", this.modelData.writeToNBT());
	}

	public boolean writeToNBTAtomically(NBTTagCompound compound) {
		boolean bo = super.writeToNBTAtomically(compound);
		if (bo) {
			String s = this.getEntityString();
			if (s.equals("minecraft:customnpcs.customnpc") || s.equals("minecraft:customnpcs:customnpc")) {
				compound.setString("id", CustomNpcs.MODID + ":customnpc");
			}
		}
		return bo;
	}

	public boolean writeToNBTOptional(NBTTagCompound compound) {
		boolean bo = super.writeToNBTAtomically(compound);
		if (bo) {
			String s = this.getEntityString();
			if (s.equals("minecraft:customnpcs.customnpc") || s.equals("minecraft:customnpcs:customnpc")) {
				compound.setString("id", CustomNpcs.MODID + ":customnpc");
			}
		}
		return bo;
	}

}
