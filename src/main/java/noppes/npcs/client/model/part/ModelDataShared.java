package noppes.npcs.client.model.part;

import java.util.*;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.LogWriter;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.ModelPartData;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.constants.EnumParts;

import javax.annotation.Nonnull;

public class ModelDataShared {

	protected ModelPartConfig arm1 = new ModelPartConfig();
	protected ModelPartConfig arm2 = new ModelPartConfig();
	protected ModelPartConfig arm3 = new ModelPartConfig();
	protected ModelPartConfig arm4 = new ModelPartConfig();
	protected ModelPartConfig body = new ModelPartConfig();
	protected ModelPartConfig head = new ModelPartConfig();
	protected ModelPartConfig leg1 = new ModelPartConfig();
	protected ModelPartConfig leg2 = new ModelPartConfig();
	protected ModelPartConfig leg3 = new ModelPartConfig();
	protected ModelPartConfig leg4 = new ModelPartConfig();
	protected ModelPartData legParts = new ModelPartData("legs");
	protected HashMap<EnumParts, ModelPartData> parts = new HashMap<>();

	protected final Map<EnumParts, List<LayerModel>> layersData = new TreeMap<>();

	public EntityLivingBase entity;
	public Class<? extends EntityLivingBase> entityClass;
	public NBTTagCompound extra = new NBTTagCompound();
	public ModelEyeData eyes = new ModelEyeData();

	public void clearEntity() { entity = null; }

	public float getBodyX() { return (1.0f - Math.max(body.scale[0], body.scale[2])) * 0.75f + getLegsX(); }

	public float getBodyY() { return (1.0f - body.scale[1]) * 0.75f + getLegsY(); }

	public Class<? extends EntityLivingBase> getEntityClass() { return entityClass; }

	public float getLegsX() {
		ModelPartConfig legs = leg1;
		if (leg2.notShared) {
			float s0 = Math.max(leg1.scale[0], leg1.scale[2]);
			float s1 = Math.max(leg2.scale[0], leg2.scale[2]);
			if (s1 > s0) { legs = leg2; }
		}
		return (1.0f - Math.max(legs.scale[0], legs.scale[2])) * 0.75f;
	}

	public float getLegsY() {
		ModelPartConfig legs = leg1;
		if (leg2.notShared && leg2.scale[1] > leg1.scale[1]) {
			legs = leg2;
		}
		return (1.0f - legs.scale[1]) * 0.75f;
	}

	public ModelPartData getOrCreatePart(EnumParts type) {
		if (type == null) { return null; }
		if (type == EnumParts.EYES) { return eyes; }
		ModelPartData part = getPartData(type);
		if (part == null) { parts.put(type, part = new ModelPartData(type.name)); }
		return part;
	}

	public ModelPartConfig getPartConfig(EnumParts type) {
		switch(type) {
			case HEAD: return head;
			case BODY: return body;
			case ARM_LEFT: return arm1;
			case ARM_RIGHT: return arm2;
			case LEG_LEFT: return leg1;
			case LEG_RIGHT: return leg2;
			case WRIST_LEFT: return arm3;
			case WRIST_RIGHT: return arm4;
			case FOOT_LEFT: return leg3;
			case FOOT_RIGHT: return leg4;
			default: return null;
		}
	}

	public ModelPartData getPartData(EnumParts type) {
		if (type == EnumParts.LEGS) { return legParts; }
		if (type == EnumParts.EYES) { return eyes; }
		return parts.get(type);
	}

	public void load(NBTTagCompound compound) {
		setEntityName(compound.getString("EntityClass"));
		arm1.load(compound.getCompoundTag("ArmsConfig"));
		arm2.load(compound.getCompoundTag("ArmsConfig" + (arm1.notShared ? "2" : "")));
		if (compound.hasKey("ArmsConfig3", 10)) {
			arm3.load(compound.getCompoundTag("ArmsConfig3"));
			arm4.load(compound.getCompoundTag("ArmsConfig" + (arm1.notShared ? "4" : "3")));
		}
		body.load(compound.getCompoundTag("BodyConfig"));
		leg1.load(compound.getCompoundTag("LegsConfig"));
		leg2.load(compound.getCompoundTag("LegsConfig" + (leg1.notShared ? "2" : "")));
		if (compound.hasKey("LegsConfig3", 10)) {
			leg3.load(compound.getCompoundTag("LegsConfig3"));
			leg4.load(compound.getCompoundTag("LegsConfig" + (leg1.notShared ? "4" : "3")));
		}
		head.load(compound.getCompoundTag("HeadConfig"));
		legParts.load(compound.getCompoundTag("LegParts"));
		eyes.load(compound.getCompoundTag("Eyes"));
		extra = compound.getCompoundTag("ExtraData");
		parts.clear();
		NBTTagList list = compound.getTagList("Parts", 10);
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound item = list.getCompoundTagAt(i);
			String name = item.getString("PartName");
			ModelPartData part = new ModelPartData(name);
			part.load(item);
			EnumParts e = EnumParts.FromName(name);
			if (e != null) { parts.put(e, part); }
		}
		layersData.clear();
		if (compound.hasKey("LayersData", 9)) {
			NBTTagList listLD = compound.getTagList("LayersData", 10);
			for (int i = 0; i < listLD.tagCount(); i++) {
				NBTTagCompound nbt = listLD.getCompoundTagAt(i);
				EnumParts part = EnumParts.getMainModel(nbt.getInteger("Part"));
				if (!layersData.containsKey(part)) { layersData.put(part, new ArrayList<>()); }
				NBTTagList listLM = nbt.getTagList("Layers", 10);
				for (int j = 0; j < listLM.tagCount(); j++) {
					layersData.get(part).add(new LayerModel(listLM.getCompoundTagAt(j)));
				}
			}
		}
		updateTranslate();
	}

	public void removePart(EnumParts type) { parts.remove(type); }

	public void setEntityClass(Class<? extends EntityLivingBase> entityClassIn) {
		entityClass = entityClassIn;
		entity = null;
		extra = new NBTTagCompound();
	}

	public void setEntityName(String string) {
		entityClass = null;
		entity = null;
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			try {
				Class<? extends Entity> c = ent.getEntityClass();
				if (c.getCanonicalName().equals(string) && EntityLivingBase.class.isAssignableFrom(c)) {
					entityClass = c.asSubclass(EntityLivingBase.class);
					break;
				}
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
	}

	private void updateTranslate() {
		for (EnumParts part : EnumParts.values()) {
			ModelPartConfig config = getPartConfig(part);
			if (config == null) { continue; }
			switch (part) {
				case HEAD:
                case BODY: {
					config.setTranslate(0.0f, getBodyY(), 0.0f);
					break;
				}
				case ARM_LEFT: {
					ModelPartConfig body = getPartConfig(EnumParts.BODY);
					float x = (1.0f - body.scale[0]) * 0.25f + (1.0f - config.scale[0]) * 0.075f;
					float y = getBodyY() + (1.0f - config.scale[1]) * -0.1f;
					config.setTranslate(-x, y, 0.0f);
					if (!config.notShared) {
						ModelPartConfig arm = getPartConfig(EnumParts.ARM_RIGHT);
						arm.copyValues(config);
					}
					break;
				}
				case ARM_RIGHT: {
					ModelPartConfig body = getPartConfig(EnumParts.BODY);
					float x = (1.0f - body.scale[0]) * 0.25f + (1.0f - config.scale[0]) * 0.075f;
					float y = getBodyY() + (1.0f - config.scale[1]) * -0.1f;
					config.setTranslate(x, y, 0.0f);
					break;
				}
				case LEG_LEFT: {
					config.setTranslate(config.scale[0] * 0.125f - 0.113f, getLegsY(), 0.0f);
					if (!config.notShared) {
						ModelPartConfig leg = getPartConfig(EnumParts.LEG_RIGHT);
						leg.copyValues(config);
					}
					break;
				}
				case LEG_RIGHT: {
					config.setTranslate((1.0f - config.scale[0]) * 0.125f, getLegsY(), 0.0f);
					break;
				}
            }
		}
	}

	public NBTTagCompound save() {
		NBTTagCompound compound = new NBTTagCompound();
		if (entityClass != null) { compound.setString("EntityClass", entityClass.getCanonicalName()); }
		compound.setTag("ArmsConfig", arm1.save());
		compound.setTag("ArmsConfig2", arm2.save());
		compound.setTag("ArmsConfig3", arm3.save());
		compound.setTag("ArmsConfig4", arm4.save());
		compound.setTag("BodyConfig", body.save());
		compound.setTag("LegsConfig", leg1.save());
		compound.setTag("LegsConfig2", leg2.save());
		compound.setTag("LegsConfig3", leg3.save());
		compound.setTag("LegsConfig4", leg4.save());
		compound.setTag("HeadConfig", head.save());
		compound.setTag("LegParts", legParts.save());
		compound.setTag("Eyes", eyes.save());
		compound.setTag("ExtraData", extra);
		NBTTagList listP = new NBTTagList();
		for (EnumParts e : parts.keySet()) {
			NBTTagCompound item = parts.get(e).save();
			item.setString("PartName", e.name);
			listP.appendTag(item);
		}
		compound.setTag("Parts", listP);
		NBTTagList listLD = new NBTTagList();
		for (EnumParts part : new ArrayList<>(layersData.keySet())) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("Part", part.ordinal());
			NBTTagList listLM = new NBTTagList();
			for (LayerModel lm : new ArrayList<>(layersData.get(part))) { listLM.appendTag(lm.save()); }
			nbt.setTag("Layers", listLM);
			listLD.appendTag(nbt);
		}
		compound.setTag("LayersData", listLD);
		return compound;
	}

	public IInventory getLayerInventory() {
		int max = 0;
		for (EnumParts part : new ArrayList<>(layersData.keySet())) {
			for (LayerModel lm : new ArrayList<>(layersData.get(part))) {
				if (max < lm.slotID) { max = lm.slotID; }
			}
		}
		NpcMiscInventory layerInventory = new NpcMiscInventory(max + 1);
		for (EnumParts part : new ArrayList<>(layersData.keySet())) {
			for (LayerModel lm : new ArrayList<>(layersData.get(part))) {
				if (lm.getStack().isEmpty()) { continue; }
				layerInventory.setInventorySlotContents(lm.slotID, lm.getStack());
			}
		}
		return layerInventory;
	}

	public int addNewLayer(NpcMiscInventory layerInventory) {
		int slotID = layerInventory.getSizeInventory();
		layerInventory.setSize(slotID);
		if (!layersData.containsKey(EnumParts.HEAD)) { layersData.put(EnumParts.HEAD, new ArrayList<>()); }
		LayerModel lm = new LayerModel();
		lm.slotID = slotID;
		layersData.get(EnumParts.HEAD).add(lm);
		return slotID;
	}

	public void moveLayerTo(int slotID, EnumParts newPart) {
		LayerModel lModel = null;
		for (EnumParts part : new ArrayList<>(layersData.keySet())) {
			if (part.equals(newPart)) { continue; }
			for (LayerModel lm: new ArrayList<>(layersData.get(part))) {
				if (lm.slotID == slotID) {
					lModel = lm;
					layersData.get(part).remove(lm);
					if (layersData.get(part).isEmpty()) { layersData.remove(part); }
					break;
				}
			}
			if (lModel != null) { break; }
		}
		if (lModel != null) {
			if (!layersData.containsKey(newPart)) { layersData.put(newPart, new ArrayList<>()); }
			layersData.get(newPart).add(lModel);
		}
	}

	public int removeLayer(int slotID) {
		boolean isRemoved = false;
		for (EnumParts part : new ArrayList<>(layersData.keySet())) {
			for (LayerModel lm: new ArrayList<>(layersData.get(part))) {
				if (lm.slotID == slotID) {
					layersData.get(part).remove(lm);
					if (layersData.get(part).isEmpty()) { layersData.remove(part); }
					isRemoved = true;
				}
				if (lm.slotID > slotID) { lm.slotID--; }
			}
			if (isRemoved) { break; }
		}
		return isRemoved ? Math.max(-1, slotID - 1) : slotID;
	}

	public @Nonnull List<String> getLayerKeys() {
		Map <Integer, String> map = new TreeMap<>();
		for (EnumParts part : new ArrayList<>(layersData.keySet())) {
			for (LayerModel lm: new ArrayList<>(layersData.get(part))) {
				map.put(lm.slotID, (lm.slotID + 1) + ": " + lm.getStack().getDisplayName());
			}
		}
		return new ArrayList<>(map.values());
	}

	public @Nonnull LayerModel getLayerModel(int slotID) {
		for (EnumParts part : new ArrayList<>(layersData.keySet())) {
			for (LayerModel lm: new ArrayList<>(layersData.get(part))) {
				if (lm.slotID == slotID) {
					lm.part = part;
					return lm;
				}
			}
		}
		if (!layersData.containsKey(EnumParts.HEAD)) { layersData.put(EnumParts.HEAD, new ArrayList<>()); }
		LayerModel lm = new LayerModel();
		lm.slotID = slotID;
		layersData.get(EnumParts.HEAD).add(lm);
		lm.part = EnumParts.HEAD;
		return lm;
	}

	public Map<EnumParts, List<LayerModel>> getRenderLayers() { return layersData; }

}
