package noppes.npcs.client.model.part;

import java.util.HashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.LogWriter;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.ModelPartData;
import noppes.npcs.constants.EnumParts;

public class ModelDataShared {

	protected ModelPartConfig arm1 = new ModelPartConfig();
	protected ModelPartConfig arm2 = new ModelPartConfig();
	protected ModelPartConfig arm3 = new ModelPartConfig();
	protected ModelPartConfig arm4 = new ModelPartConfig();
	protected ModelPartConfig body = new ModelPartConfig();
	public EntityLivingBase entity;
	public Class<? extends EntityLivingBase> entityClass;
	public NBTTagCompound extra = new NBTTagCompound();
	public ModelEyeData eyes = new ModelEyeData();
	protected ModelPartConfig head = new ModelPartConfig();
	protected ModelPartConfig leg1 = new ModelPartConfig();
	protected ModelPartConfig leg2 = new ModelPartConfig();
	protected ModelPartConfig leg3 = new ModelPartConfig();
	protected ModelPartConfig leg4 = new ModelPartConfig();
	protected ModelPartData legParts = new ModelPartData("legs");
	protected HashMap<EnumParts, ModelPartData> parts = new HashMap<>();

	public ModelDataShared() {
	}

	public void clearEntity() {
		this.entity = null;
	}

	public float getBodyX() {
		return (1.0f - Math.max(body.scale[0], body.scale[2])) * 0.75f + getLegsX();
	}

	public float getBodyY() {
		return (1.0f - body.scale[1]) * 0.75f + getLegsY();
	}

	public Class<? extends EntityLivingBase> getEntityClass() {
		return this.entityClass;
	}

	public float getLegsX() {
		ModelPartConfig legs = this.leg1;
		if (leg2.notShared) {
			float s0 = Math.max(leg1.scale[0], leg1.scale[2]);
			float s1 = Math.max(leg2.scale[0], leg2.scale[2]);
			if (s1 > s0) { legs = leg2; }
		}
		return (1.0f - Math.max(legs.scale[0], legs.scale[2])) * 0.75f;
	}

	public float getLegsY() {
		ModelPartConfig legs = this.leg1;
		if (this.leg2.notShared && this.leg2.scale[1] > this.leg1.scale[1]) {
			legs = this.leg2;
		}
		return (1.0f - legs.scale[1]) * 0.75f;
	}

	public ModelPartData getOrCreatePart(EnumParts type) {
		if (type == null) {
			return null;
		}
		if (type == EnumParts.EYES) {
			return this.eyes;
		}
		ModelPartData part = this.getPartData(type);
		if (part == null) {
			this.parts.put(type, part = new ModelPartData(type.name));
		}
		return part;
	}

	public ModelPartConfig getPartConfig(EnumParts type) {
		switch(type) {
			case HEAD: return this.head;
			case BODY: return this.body;
			case ARM_LEFT: return this.arm1;
			case ARM_RIGHT: return this.arm2;
			case LEG_LEFT: return this.leg1;
			case LEG_RIGHT: return this.leg2;
			case WRIST_LEFT: return this.arm3;
			case WRIST_RIGHT: return this.arm4;
			case FOOT_LEFT: return this.leg3;
			case FOOT_RIGHT: return this.leg4;
			default: return null;
		}
	}

	public ModelPartData getPartData(EnumParts type) {
		if (type == EnumParts.LEGS) {
			return this.legParts;
		}
		if (type == EnumParts.EYES) {
			return this.eyes;
		}
		return this.parts.get(type);
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.setEntityName(compound.getString("EntityClass"));
		this.arm1.readFromNBT(compound.getCompoundTag("ArmsConfig"));
		this.arm2.readFromNBT(compound.getCompoundTag("ArmsConfig" + (this.arm1.notShared ? "2" : "")));
		if (compound.hasKey("ArmsConfig3", 10)) {
			this.arm3.readFromNBT(compound.getCompoundTag("ArmsConfig3"));
			this.arm4.readFromNBT(compound.getCompoundTag("ArmsConfig" + (this.arm1.notShared ? "4" : "3")));
		}
		this.body.readFromNBT(compound.getCompoundTag("BodyConfig"));
		this.leg1.readFromNBT(compound.getCompoundTag("LegsConfig"));
		this.leg2.readFromNBT(compound.getCompoundTag("LegsConfig" + (this.leg1.notShared ? "2" : "")));
		if (compound.hasKey("LegsConfig3", 10)) {
			this.leg3.readFromNBT(compound.getCompoundTag("LegsConfig3"));
			this.leg4.readFromNBT(compound.getCompoundTag("LegsConfig" + (this.leg1.notShared ? "4" : "3")));
		}
		this.head.readFromNBT(compound.getCompoundTag("HeadConfig"));
		this.legParts.readFromNBT(compound.getCompoundTag("LegParts"));
		this.eyes.readFromNBT(compound.getCompoundTag("Eyes"));
		this.extra = compound.getCompoundTag("ExtraData");
		HashMap<EnumParts, ModelPartData> parts = new HashMap<>();
		NBTTagList list = compound.getTagList("Parts", 10);
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound item = list.getCompoundTagAt(i);
			String name = item.getString("PartName");
			ModelPartData part = new ModelPartData(name);
			part.readFromNBT(item);
			EnumParts e = EnumParts.FromName(name);
			if (e != null) {
				parts.put(e, part);
			}
		}
		this.parts = parts;
		this.updateTranslate();
	}

	public void removePart(EnumParts type) {
		this.parts.remove(type);
	}

	public void setEntityClass(Class<? extends EntityLivingBase> entityClass) {
		this.entityClass = entityClass;
		this.entity = null;
		this.extra = new NBTTagCompound();
	}

	public void setEntityName(String string) {
		this.entityClass = null;
		this.entity = null;
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			try {
				Class<? extends Entity> c = ent.getEntityClass();
				if (c.getCanonicalName().equals(string) && EntityLivingBase.class.isAssignableFrom(c)) {
					this.entityClass = c.asSubclass(EntityLivingBase.class);
					break;
				}
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
	}

	private void updateTranslate() {
		for (EnumParts part : EnumParts.values()) {
			ModelPartConfig config = this.getPartConfig(part);
			if (config != null) {
				if (part == EnumParts.HEAD) {
					config.setTranslate(0.0f, this.getBodyY(), 0.0f);
				} else if (part == EnumParts.ARM_LEFT) {
					ModelPartConfig body = this.getPartConfig(EnumParts.BODY);
					float x = (1.0f - body.scale[0]) * 0.25f + (1.0f - config.scale[0]) * 0.075f;
					float y = this.getBodyY() + (1.0f - config.scale[1]) * -0.1f;
					config.setTranslate(-x, y, 0.0f);
					if (!config.notShared) {
						ModelPartConfig arm = this.getPartConfig(EnumParts.ARM_RIGHT);
						arm.copyValues(config);
					}
				} else if (part == EnumParts.ARM_RIGHT) {
					ModelPartConfig body = this.getPartConfig(EnumParts.BODY);
					float x = (1.0f - body.scale[0]) * 0.25f + (1.0f - config.scale[0]) * 0.075f;
					float y = this.getBodyY() + (1.0f - config.scale[1]) * -0.1f;
					config.setTranslate(x, y, 0.0f);
				} else if (part == EnumParts.LEG_LEFT) {
					config.setTranslate(config.scale[0] * 0.125f - 0.113f, this.getLegsY(), 0.0f);
					if (!config.notShared) {
						ModelPartConfig leg = this.getPartConfig(EnumParts.LEG_RIGHT);
						leg.copyValues(config);
					}
				} else if (part == EnumParts.LEG_RIGHT) {
					config.setTranslate((1.0f - config.scale[0]) * 0.125f, this.getLegsY(), 0.0f);
				} else if (part == EnumParts.BODY) {
					config.setTranslate(0.0f, this.getBodyY(), 0.0f);
				}
			}
		}
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		if (this.entityClass != null) {
			compound.setString("EntityClass", this.entityClass.getCanonicalName());
		}
		compound.setTag("ArmsConfig", this.arm1.writeToNBT());
		compound.setTag("ArmsConfig2", this.arm2.writeToNBT());
		compound.setTag("ArmsConfig3", this.arm3.writeToNBT());
		compound.setTag("ArmsConfig4", this.arm4.writeToNBT());
		compound.setTag("BodyConfig", this.body.writeToNBT());
		compound.setTag("LegsConfig", this.leg1.writeToNBT());
		compound.setTag("LegsConfig2", this.leg2.writeToNBT());
		compound.setTag("LegsConfig3", this.leg3.writeToNBT());
		compound.setTag("LegsConfig4", this.leg4.writeToNBT());
		compound.setTag("HeadConfig", this.head.writeToNBT());
		compound.setTag("LegParts", this.legParts.writeToNBT());
		compound.setTag("Eyes", this.eyes.writeToNBT());
		compound.setTag("ExtraData", this.extra);
		NBTTagList list = new NBTTagList();
		for (EnumParts e : this.parts.keySet()) {
			NBTTagCompound item = this.parts.get(e).writeToNBT();
			item.setString("PartName", e.name);
			list.appendTag(item);
		}
		compound.setTag("Parts", list);
		return compound;
	}
}
