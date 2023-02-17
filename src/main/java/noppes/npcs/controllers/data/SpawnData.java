package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandom;
import noppes.npcs.NBTTags;

public class SpawnData extends WeightedRandom.Item {
	public List<String> biomes;
	public NBTTagCompound compound1;
	public int id;
	public boolean liquid;
	public String name;
	public int type;

	public SpawnData() {
		super(10);
		this.biomes = new ArrayList<String>();
		this.id = -1;
		this.name = "";
		this.compound1 = new NBTTagCompound();
		this.liquid = false;
		this.type = 0;
	}

	public void readNBT(NBTTagCompound compound) {
		this.id = compound.getInteger("SpawnId");
		this.name = compound.getString("SpawnName");
		this.itemWeight = compound.getInteger("SpawnWeight");
		if (this.itemWeight == 0) {
			this.itemWeight = 1;
		}
		this.biomes = NBTTags.getStringList(compound.getTagList("SpawnBiomes", 10));
		this.compound1 = compound.getCompoundTag("SpawnCompound1");
		this.type = compound.getInteger("SpawnType");
	}

	public NBTTagCompound writeNBT(NBTTagCompound compound) {
		compound.setInteger("SpawnId", this.id);
		compound.setString("SpawnName", this.name);
		compound.setInteger("SpawnWeight", this.itemWeight);
		compound.setTag("SpawnBiomes", NBTTags.nbtStringList(this.biomes));
		compound.setTag("SpawnCompound1", this.compound1);
		compound.setInteger("SpawnType", this.type);
		return compound;
	}
}
