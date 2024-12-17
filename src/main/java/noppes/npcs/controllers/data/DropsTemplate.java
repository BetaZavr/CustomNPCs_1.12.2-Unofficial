package noppes.npcs.controllers.data;

import java.util.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.LogWriter;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.entity.data.DropSet;

public class DropsTemplate {

	public final Map<Integer, Map<Integer, DropSet>> groups = new TreeMap<>(); // <id, <pos, drop>>
	private boolean allDropsFromGroup = false; // or random
	private final Random rnd = new Random();

	public DropsTemplate() {
		groups.put(0, new TreeMap<>());
	}

	public DropsTemplate(NBTTagCompound nbtTemplate) {
		this();
		this.load(nbtTemplate);
	}

	public void addDropItem(int id, IItemStack item, double chance) {
		if (!groups.containsKey(id)) {
			id = groups.size();
			groups.put(id, new TreeMap<>());
		}
		DropSet ds = new DropSet(null);
		ds.item = item;
		ds.setChance(chance);
		ds.pos = groups.get(id).size();
		groups.get(id).put(ds.pos, ds);
	}

	public List<DropSet> getDrops() {
		List<DropSet> allDrops = new ArrayList<>();
		for (int groupId : groups.keySet()) {
			ArrayList<DropSet> preList = new ArrayList<>(groups.get(groupId).values());
			if (preList.isEmpty()) { continue; }
			if (allDropsFromGroup) { allDrops.addAll(preList); }
			else { allDrops.add(preList.get(rnd.nextInt(preList.size()))); }
		}
		return allDrops;
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound nbtTemplate = new NBTTagCompound();
		nbtTemplate.setBoolean("DropType", allDropsFromGroup);
		for (int id : this.groups.keySet()) {
			NBTTagList list = new NBTTagList();
			for (DropSet ds : this.groups.get(id).values()) {
				list.appendTag(ds.getNBT());
			}
			nbtTemplate.setTag("Group_" + id, list);
		}
		return nbtTemplate;
	}

	public void load(NBTTagCompound nbtTemplate) {
		if (nbtTemplate.hasKey("DropType", 3)) {
			allDropsFromGroup = nbtTemplate.getInteger("DropType") == 3;
		} else if (nbtTemplate.hasKey("DropType", 1)) {
			allDropsFromGroup = nbtTemplate.getBoolean("DropType");
		}

		this.groups.clear();
		Set<String> keys = nbtTemplate.getKeySet();
		for (String groupId : keys) {
			if (groupId.indexOf("Group_") != 0) {
				continue;
			}
			int id = -1;
			try {
				id = Integer.parseInt(groupId.replace("Group_", ""));
			} catch (Exception e) { LogWriter.error("Error:", e); }
			if (id < 0) {
				continue;
			}
			for (int j = 0; j < nbtTemplate.getTagList(groupId, 10).tagCount(); j++) {
				DropSet ds = new DropSet(null);
				ds.load(nbtTemplate.getTagList(groupId, 10).getCompoundTagAt(j));
				if (!this.groups.containsKey(id)) {
					this.groups.put(id, new TreeMap<>());
				}
				this.groups.get(id).put(ds.pos, ds);
			}
		}
	}

	public void removeDrop(int groupId, int slot) {
		if (!this.groups.containsKey(groupId) || !this.groups.get(groupId).containsKey(slot)) {
			return;
		}
		Map<Integer, DropSet> newDrop = new TreeMap<>();
		int j = 0;
		for (int s : groups.keySet()) {
			if (s == slot) { continue; }
			newDrop.put(j, groups.get(groupId).get(s));
			newDrop.get(j).pos = j;
			j++;
		}
		this.groups.get(groupId).clear();
		this.groups.get(groupId).putAll(newDrop);
	}

	public void removeGroup(int groupId) {
		if (!this.groups.containsKey(groupId)) {
			return;
		}
		this.groups.remove(groupId);
		Map<Integer, Map<Integer, DropSet>> newGroups = new TreeMap<>();
		int j = 0;
		for (int gId : this.groups.keySet()) {
			if (gId == groupId) {
				continue;
			}
			newGroups.put(j, this.groups.get(gId));
			j++;
		}
		this.groups.clear();
		this.groups.putAll(newGroups);
	}
	
	public static DropsTemplate from(DropsTemplate dropTemplate) {
		DropsTemplate dt = new DropsTemplate();
		if (dropTemplate != null) {
			dt.load(dropTemplate.getNBT());
		}
		return dt;
	}

}
