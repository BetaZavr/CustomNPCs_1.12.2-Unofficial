package noppes.npcs.client.model.part;

import java.util.*;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.LogWriter;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.ModelPartData;
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
	protected final Map<Integer, LayerModel> layersData = new TreeMap<>();
	private final List<String> disableLayers = new ArrayList<>();

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
		NBTTagList listP = compound.getTagList("Parts", 10);
		for (int i = 0; i < listP.tagCount(); ++i) {
			NBTTagCompound item = listP.getCompoundTagAt(i);
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
				LayerModel lm = new LayerModel(listLD.getCompoundTagAt(i));
				layersData.put(lm.slotID, lm);
			}
		}
		disableLayers.clear();
		if (compound.hasKey("DisableLayers", 9)) {
			NBTTagList listDL = compound.getTagList("DisableLayers", 8);
			for (int i = 0; i < listDL.tagCount(); i++) { disableLayers.add(listDL.getStringTagAt(i)); }
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
			} catch (Exception e) { LogWriter.error(e); }
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
		for (LayerModel lm : new ArrayList<>(layersData.values())) { listLD.appendTag(lm.save()); }
		compound.setTag("LayersData", listLD);
		NBTTagList listDL = new NBTTagList();
		for (String layer : disableLayers) { listDL.appendTag(new NBTTagString(layer)); }
		compound.setTag("DisableLayers", listDL);
		return compound;
	}

	public int addNewLayer() {
		int slotID = layersData.size();
		LayerModel lm = new LayerModel();
		lm.slotID = slotID;
		layersData.put(slotID, lm);
		return slotID;
	}

	public int removeLayer(int slotID) {
		Map<Integer, LayerModel> newLayersData = new TreeMap<>();
		int id = 0;
		for (LayerModel lm : new ArrayList<>(layersData.values())) {
			if (lm.slotID == slotID) { continue; }
			lm.slotID = id;
			newLayersData.put(lm.slotID, lm);
			id++;
		}
		if (newLayersData.size() != layersData.size()) {
			layersData.clear();
			layersData.putAll(newLayersData);
			return Math.max(-1, slotID - 1);
		}
		return slotID;
	}

	public @Nonnull List<String> getLayerKeys() {
		List<String> list = new ArrayList<>();
		for (LayerModel lm: new ArrayList<>(layersData.values())) {
			String key = (lm.slotID + 1) + ": ";
			if (lm.getOBJ() != null) {
				String k = lm.getOBJ().getResourcePath();
				if (k.lastIndexOf("/") != -1) {
					key += k.substring(k.lastIndexOf("/")+1);
				} else if (k.lastIndexOf(":") != -1) {
					key += k.substring(k.lastIndexOf(":")+1);
				} else {
					key += k;
				}
			}
			else { key += lm.getStack().getDisplayName(); }
			list.add(key);
		}
		return list;
	}

	public LayerModel getLayerModel(int slotID) {
		if (layersData.containsKey(slotID)) { return layersData.get(slotID); }
		if (slotID == layersData.size()) { return layersData.get(addNewLayer()); }
		return null;
	}

	public List<LayerModel> getRenderLayers(EnumParts part) {
		List<LayerModel> list = new ArrayList<>();
		for (LayerModel lm : new ArrayList<>(layersData.values())) {
			if (lm.part == part) { list.add(lm); }
		}
		return list;
	}

	public boolean isNoEmptyLayer() {
		for (LayerModel lm : new ArrayList<>(layersData.values())) {
			if (lm.getStack().isEmpty() && lm.getOBJ() == null) { return false; }
		}
		return true;
	}

	public boolean isDisableLayer(String layerName) {
		return disableLayers.contains(layerName);
	}

	public String[] getDisableLayers() {
		return disableLayers.toArray(new String[0]);
	}

	public void setDisableLayers(String[] newLayers) {
		disableLayers.clear();
		if (newLayers != null && newLayers.length != 0) { disableLayers.addAll(Arrays.asList(newLayers)); }
	}

	public boolean hasDisableLayers() { return !disableLayers.isEmpty(); }

}
