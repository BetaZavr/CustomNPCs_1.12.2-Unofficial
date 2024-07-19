package noppes.npcs.controllers.data;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.roles.JobItemGiver;

public class PlayerItemGiverData {
	private HashMap<Integer, Integer> chained;
	private HashMap<Integer, Long> itemgivers;

	public PlayerItemGiverData() {
		this.itemgivers = new HashMap<>();
		this.chained = new HashMap<>();
	}

	public int getItemIndex(JobItemGiver jobItemGiver) {
		if (this.chained.containsKey(jobItemGiver.itemGiverId)) {
			return this.chained.get(jobItemGiver.itemGiverId);
		}
		return 0;
	}

	public long getTime(JobItemGiver jobItemGiver) {
		return this.itemgivers.get(jobItemGiver.itemGiverId);
	}

	public boolean notInteractedBefore(JobItemGiver jobItemGiver) {
		return !this.itemgivers.containsKey(jobItemGiver.itemGiverId);
	}

	public void loadNBTData(NBTTagCompound compound) {
		this.chained = NBTTags.getIntegerIntegerMap(compound.getTagList("ItemGiverChained", 10));
		this.itemgivers = NBTTags.getIntegerLongMap(compound.getTagList("ItemGiversList", 10));
	}

	public void saveNBTData(NBTTagCompound compound) {
		compound.setTag("ItemGiverChained", NBTTags.nbtIntegerIntegerMap(this.chained));
		compound.setTag("ItemGiversList", NBTTags.nbtIntegerLongMap(this.itemgivers));
	}

	public void setItemIndex(JobItemGiver jobItemGiver, int i) {
		this.chained.put(jobItemGiver.itemGiverId, i);
	}

	public void setTime(JobItemGiver jobItemGiver, long day) {
		this.itemgivers.put(jobItemGiver.itemGiverId, day);
	}
}
