package noppes.npcs.client.controllers;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.constants.EnumParts;

public class Preset {
	public static void FillDefault(HashMap<String, Preset> presets) {
		ModelData data = new ModelData();
		Preset preset = new Preset();
		preset.name = "Elf Male";
		preset.data = data;
		data.getPartConfig(EnumParts.LEG_LEFT).setScale(0.85f, 1.15f);
		data.getPartConfig(EnumParts.ARM_LEFT).setScale(0.85f, 1.15f);
		data.getPartConfig(EnumParts.BODY).setScale(0.85f, 1.15f);
		data.getPartConfig(EnumParts.HEAD).setScale(0.85f, 0.95f);
		presets.put("elf male", preset);
		data = new ModelData();
		preset = new Preset();
		preset.name = "Elf Female";
		preset.data = data;
		data.getOrCreatePart(EnumParts.BREASTS).type = 2;
		data.getPartConfig(EnumParts.LEG_LEFT).setScale(0.8f, 1.05f);
		data.getPartConfig(EnumParts.ARM_LEFT).setScale(0.8f, 1.05f);
		data.getPartConfig(EnumParts.BODY).setScale(0.8f, 1.05f);
		data.getPartConfig(EnumParts.HEAD).setScale(0.8f, 0.85f);
		presets.put("elf female", preset);
		data = new ModelData();
		preset = new Preset();
		preset.name = "Dwarf Male";
		preset.data = data;
		data.getPartConfig(EnumParts.LEG_LEFT).setScale(1.1f, 0.7f, 0.9f);
		data.getPartConfig(EnumParts.ARM_LEFT).setScale(0.9f, 0.7f);
		data.getPartConfig(EnumParts.BODY).setScale(1.2f, 0.7f, 1.5f);
		data.getPartConfig(EnumParts.HEAD).setScale(0.85f, 0.85f);
		presets.put("dwarf male", preset);
		data = new ModelData();
		preset = new Preset();
		preset.name = "Dwarf Female";
		preset.data = data;
		data.getOrCreatePart(EnumParts.BREASTS).type = 2;
		data.getPartConfig(EnumParts.LEG_LEFT).setScale(0.9f, 0.65f);
		data.getPartConfig(EnumParts.ARM_LEFT).setScale(0.9f, 0.65f);
		data.getPartConfig(EnumParts.BODY).setScale(1.0f, 0.65f, 1.1f);
		data.getPartConfig(EnumParts.HEAD).setScale(0.85f, 0.85f);
		presets.put("dwarf female", preset);
		data = new ModelData();
		preset = new Preset();
		preset.name = "Orc Male";
		preset.data = data;
		data.getPartConfig(EnumParts.LEG_LEFT).setScale(1.2f, 1.05f);
		data.getPartConfig(EnumParts.ARM_LEFT).setScale(1.2f, 1.05f);
		data.getPartConfig(EnumParts.BODY).setScale(1.4f, 1.1f, 1.5f);
		data.getPartConfig(EnumParts.HEAD).setScale(1.2f, 1.1f);
		presets.put("orc male", preset);
		data = new ModelData();
		preset = new Preset();
		preset.name = "Orc Female";
		preset.data = data;
		data.getOrCreatePart(EnumParts.BREASTS).type = 2;
		data.getPartConfig(EnumParts.LEG_LEFT).setScale(1.1f, 1.0f);
		data.getPartConfig(EnumParts.ARM_LEFT).setScale(1.1f, 1.0f);
		data.getPartConfig(EnumParts.BODY).setScale(1.1f, 1.0f, 1.25f);
		presets.put("orc female", preset);
		data = new ModelData();
		preset = new Preset();
		preset.name = "Human Male";
		preset.data = data;
		presets.put("human male", preset);
		data = new ModelData();
		preset = new Preset();
		preset.name = "Human Female";
		preset.data = data;
		data.getOrCreatePart(EnumParts.BREASTS).type = 2;
		data.getPartConfig(EnumParts.LEG_LEFT).setScale(0.92f, 0.92f);
		data.getPartConfig(EnumParts.HEAD).setScale(0.95f, 0.95f);
		data.getPartConfig(EnumParts.ARM_LEFT).setScale(0.8f, 0.92f);
		data.getPartConfig(EnumParts.BODY).setScale(0.92f, 0.92f);
		presets.put("human female", preset);
		data = new ModelData();
		preset = new Preset();
		preset.name = "Cat Male";
		preset.data = data;
		ModelPartData ears = data.getOrCreatePart(EnumParts.EARS);
		ears.type = 0;
		ears.color = 14263886;
		ModelPartData snout = data.getOrCreatePart(EnumParts.SNOUT);
		snout.type = 0;
		snout.color = 14263886;
		ModelPartData tail = data.getOrCreatePart(EnumParts.TAIL);
		tail.type = 0;
		tail.color = 14263886;
		presets.put("cat male", preset);
		data = new ModelData();
		preset = new Preset();
		preset.name = "Cat Female";
		preset.data = data;
		ears = data.getOrCreatePart(EnumParts.EARS);
		ears.type = 0;
		ears.color = 14263886;
		snout = data.getOrCreatePart(EnumParts.SNOUT);
		snout.type = 0;
		snout.color = 14263886;
		tail = data.getOrCreatePart(EnumParts.TAIL);
		tail.type = 0;
		tail.color = 14263886;
		data.getOrCreatePart(EnumParts.BREASTS).type = 2;
		data.getPartConfig(EnumParts.HEAD).setScale(0.95f, 0.95f);
		data.getPartConfig(EnumParts.LEG_LEFT).setScale(0.92f, 0.92f);
		data.getPartConfig(EnumParts.ARM_LEFT).setScale(0.8f, 0.92f);
		data.getPartConfig(EnumParts.BODY).setScale(0.92f, 0.92f);
		presets.put("cat female", preset);
		data = new ModelData();
		preset = new Preset();
		preset.name = "Wolf Male";
		preset.data = data;
		ears = data.getOrCreatePart(EnumParts.EARS);
		ears.type = 0;
		ears.color = 6182997;
		snout = data.getOrCreatePart(EnumParts.SNOUT);
		snout.type = 2;
		snout.color = 6182997;
		tail = data.getOrCreatePart(EnumParts.TAIL);
		tail.type = 0;
		tail.color = 6182997;
		presets.put("wolf male", preset);
		data = new ModelData();
		preset = new Preset();
		preset.name = "Wolf Female";
		preset.data = data;
		ears = data.getOrCreatePart(EnumParts.EARS);
		ears.type = 0;
		ears.color = 6182997;
		snout = data.getOrCreatePart(EnumParts.SNOUT);
		snout.type = 2;
		snout.color = 6182997;
		tail = data.getOrCreatePart(EnumParts.TAIL);
		tail.type = 0;
		tail.color = 6182997;
		data.getOrCreatePart(EnumParts.BREASTS).type = 2;
		data.getPartConfig(EnumParts.HEAD).setScale(0.95f, 0.95f);
		data.getPartConfig(EnumParts.LEG_LEFT).setScale(0.92f, 0.92f);
		data.getPartConfig(EnumParts.ARM_LEFT).setScale(0.8f, 0.92f);
		data.getPartConfig(EnumParts.BODY).setScale(0.92f, 0.92f);
		presets.put("wolf female", preset);
		data = new ModelData();
		preset = new Preset();
		preset.name = "Enderchibi";
		preset.data = data;
		data.getPartConfig(EnumParts.LEG_LEFT).setScale(0.65f, 0.75f);
		data.getPartConfig(EnumParts.ARM_LEFT).setScale(0.5f, 1.45f);
		ModelPartData part = data.getOrCreatePart(EnumParts.PARTICLES);
		part.type = 1;
		part.color = 16711680;
		presets.put("enderchibi", preset);
	}

	public ModelData data;

	public String name;

	public Preset() {
		this.data = new ModelData();
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.name = compound.getString("PresetName");
		this.data.readFromNBT(compound.getCompoundTag("PresetData"));
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("PresetName", this.name);
		compound.setTag("PresetData", this.data.writeToNBT());
		return compound;
	}
}
