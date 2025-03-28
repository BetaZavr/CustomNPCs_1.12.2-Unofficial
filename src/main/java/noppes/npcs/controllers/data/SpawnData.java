package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.WeightedRandom;
import noppes.npcs.NBTTags;
import noppes.npcs.util.ValueUtil;

public class SpawnData extends WeightedRandom.Item {
	
	public List<String> biomes = new ArrayList<>();
	public NBTTagCompound compoundEntity = new NBTTagCompound();
	public int id = -1;
	public boolean liquid = false;
	public boolean canSeeSummon = true;
	public String name = "";
	public int type = 0;
	public int group = 4;
	public int range = 8;
	public int maxNearPlayer = 10;

	public SpawnData() { super(10); }

	public void readNBT(NBTTagCompound compound) {
		id = compound.getInteger("SpawnId");
		name = compound.getString("SpawnName");
		itemWeight = ValueUtil.correctInt(compound.getInteger("SpawnWeight"), 1, 100);
		biomes = NBTTags.getStringList(compound.getTagList("SpawnBiomes", 10));
		compoundEntity = compound.getCompoundTag("SpawnCompound1");
		type = compound.getInteger("SpawnType");
		if (compound.hasKey("MaxInGroup", 3)) { group = compound.getInteger("MaxInGroup"); }
		if (compound.hasKey("GroupInRange", 3)) { range = compound.getInteger("GroupInRange"); }
		if (compound.hasKey("MaximumNearPlayer", 3)) { maxNearPlayer = compound.getInteger("MaximumNearPlayer"); }
		canSeeSummon = compound.getBoolean("PlayerCanSeeSummon");
	}

	public NBTTagCompound writeNBT(NBTTagCompound compound) {
		compound.setInteger("SpawnId", id);
		compound.setString("SpawnName", name);
		compound.setInteger("SpawnWeight", itemWeight);
		compound.setTag("SpawnBiomes", NBTTags.nbtStringList(biomes));
		compound.setTag("SpawnCompound1", compoundEntity);
		compound.setInteger("SpawnType", type);
		compound.setInteger("MaxInGroup", group);
		compound.setInteger("GroupInRange", range);
		compound.setInteger("MaximumNearPlayer", maxNearPlayer);
		compound.setBoolean("PlayerCanSeeSummon", canSeeSummon);
		return compound;
	}

}
