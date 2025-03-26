package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandom;
import noppes.npcs.NBTTags;

public class SpawnData extends WeightedRandom.Item {
	
	public List<String> biomes = new ArrayList<>();
	public NBTTagCompound compoundEntity = new NBTTagCompound();
	public int id = -1;
	public boolean liquid = false;
	public String name = "";
	public int type = 0;
	public int group = 4;
	public int range = 8;

	public SpawnData() { super(10); }

	public void readNBT(NBTTagCompound compound) {
		this.id = compound.getInteger("SpawnId");
		this.name = compound.getString("SpawnName");
		this.itemWeight = compound.getInteger("SpawnWeight");
		if (this.itemWeight == 0) {
			this.itemWeight = 1;
		}
		this.biomes = NBTTags.getStringList(compound.getTagList("SpawnBiomes", 10));
		this.compoundEntity = compound.getCompoundTag("SpawnCompound1");
		this.type = compound.getInteger("SpawnType");
		if (compound.hasKey("MaxInGroup", 3)) { this.group = compound.getInteger("MaxInGroup"); }
		if (compound.hasKey("GroupInRange", 3)) { this.range = compound.getInteger("GroupInRange"); }
	}

	public NBTTagCompound writeNBT(NBTTagCompound compound) {
		compound.setInteger("SpawnId", this.id);
		compound.setString("SpawnName", this.name);
		compound.setInteger("SpawnWeight", this.itemWeight);
		compound.setTag("SpawnBiomes", NBTTags.nbtStringList(this.biomes));
		compound.setTag("SpawnCompound1", this.compoundEntity);
		compound.setInteger("SpawnType", this.type);
		compound.setInteger("MaxInGroup", this.group);
		compound.setInteger("GroupInRange", this.range);
		return compound;
	}

}
