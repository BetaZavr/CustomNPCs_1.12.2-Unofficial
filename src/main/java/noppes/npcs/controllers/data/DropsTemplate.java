package noppes.npcs.controllers.data;

import java.util.*;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.entity.data.DropSet;

public class DropsTemplate {

	public final Map<Integer, Map<Integer, DropSet>> groups = new TreeMap<>(); // <id, <pos, drop>>
	private int dropType = 0; // 0-only rnd, 1-only min, 2-only max, 3-all
	private final Random rnd = new Random();

	public DropsTemplate() {
		this.groups.put(0, new TreeMap<>());
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

	public List<IItemStack> createDrops(double ch, boolean isLooted, EntityLivingBase attacking) {
		List<IItemStack> list = new ArrayList<>();
		
		for (int groupId : this.groups.keySet()) {
			float r = this.rnd.nextFloat();
			Map<IItemStack, Double> preMap = new HashMap<>();
			HashMap<Integer, QuestData> activeQuests = null;
			if (attacking instanceof EntityPlayer) {
				activeQuests = CustomNpcs.proxy.getPlayerData((EntityPlayer) attacking).questData.activeQuests;
			}
			for (DropSet ds : this.groups.get(groupId).values()) {
				double c = ds.chance * ch / 100.0d;
				if (this.dropType == 3) {
					r = this.rnd.nextFloat();
				}
				if (ds.item == null || ds.item.isEmpty() || isLooted == ds.lootMode || (c < 1.0d && c < r)) {
					continue;
				}
				boolean needAdd = true;
				if (ds.getQuestID() > 0) {
                    needAdd = false;
                    if (activeQuests != null) {
                        QuestData qData = activeQuests.get(ds.getQuestID());
                        if (qData != null) {
                            for (IQuestObjective objQ : qData.quest.getObjectives((EntityPlayer) attacking)) {
                                if (!objQ.isCompleted()) {
                                    needAdd = true;
                                    break;
                                }
                            }
                        }
                    }
				}
				if (needAdd && !(ds.amount[0] == 0 && ds.amount[1] == 0)) {
					preMap.put(ds.createLoot(ch), ds.chance);
				}
			}
			if (preMap.isEmpty()) {
				continue;
			}
			int p = this.rnd.nextInt(preMap.size());
			if (p == preMap.size()) {
				p = preMap.size() - 1;
			}
            int i = 0;
			IItemStack hStack = null;
			double h = this.dropType == 1 ? 2.0d : -1.0d;
			for (IItemStack stack : preMap.keySet()) {
				if (this.dropType == 3) { // all
					list.add(stack);
					continue;
				}
				if (this.dropType == 0) { // rnd
					if (i != p) {
						i++;
						continue;
					}
					list.add(stack);
					break;
				}
				double c = preMap.get(stack);
				if (this.dropType == 1) { // min
					if (h >= c) {
						h = c;
						hStack = stack;
					}
				} else { // max
					if (h <= c) {
						h = c;
						hStack = stack;
					}
				}
			}
			if (hStack != null) {
				list.add(hStack);
			}
		}
		return list;
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound nbtTemplate = new NBTTagCompound();
		nbtTemplate.setInteger("DropType", this.dropType);
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
		this.dropType = nbtTemplate.getInteger("DropType");
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
