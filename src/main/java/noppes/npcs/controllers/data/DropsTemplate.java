package noppes.npcs.controllers.data;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.entity.data.DropSet;
import noppes.npcs.util.ValueUtil;

public class DropsTemplate {

	public final Map<Integer, Map<Integer, DropSet>> groups; // <id, <pos, drop>>
	private int dropType; // 0-only rnd, 1-only min, 2-only max, 3-all
	private Random rnd;

	public DropsTemplate() {
		this.groups = Maps.<Integer, Map<Integer, DropSet>>newTreeMap();
		this.dropType = 0;
		this.rnd = new Random();
	}
	
	public DropsTemplate(NBTTagCompound nbtTemplate) {
		this();
		this.load(nbtTemplate);
	}

	public void load(NBTTagCompound nbtTemplate) {
		this.dropType = nbtTemplate.getInteger("DropType");
		this.groups.clear();
		Set<String> keys = nbtTemplate.getKeySet();
		for (String groupId : keys) {
			if (groupId.indexOf("Group_")!=0) { continue; }
			int id = -1;
			try { id = Integer.parseInt(groupId.replace("Group_", "")); }
			catch (NumberFormatException ex) { }
			if (id<0) { continue; }
			for (int j = 0; j < nbtTemplate.getTagList(groupId, 10).tagCount(); j++) {
				DropSet ds = new DropSet(null);
				ds.load(nbtTemplate.getTagList(groupId, 10).getCompoundTagAt(j));
				if (!this.groups.containsKey(id)) { this.groups.put(id, Maps.<Integer, DropSet>newTreeMap()); }
				this.groups.get(id).put(ds.pos, ds);
			}
		}
	}

	public NBTBase getNBT() {
		NBTTagCompound nbtTemplate = new NBTTagCompound();
		nbtTemplate.setInteger("DropType", this.dropType);
		for (int id : this.groups.keySet()) {
			NBTTagList list = new NBTTagList();
			for (DropSet ds : this.groups.get(id).values()) {
				list.appendTag(ds.getNBT());
			}
			nbtTemplate.setTag("Group_"+id, list);
		}
		return nbtTemplate;
	}

	public List<IItemStack> createDrops(double ch, boolean isLooted, EntityLivingBase attacking) {
		List<IItemStack> list = Lists.<IItemStack>newArrayList();
		for (int groupId : this.groups.keySet()) {
			float r = this.rnd.nextFloat();
			Map<IItemStack, Double> preMap = Maps.<IItemStack, Double>newHashMap();
			for (DropSet ds : this.groups.get(groupId).values()) {
				double c = ds.chance * ch / 100.0d;
				if (this.dropType==3) { r = this.rnd.nextFloat(); }
				if (ds.item == null || ds.item.isEmpty() || isLooted == ds.lootMode || (c<1.0d && c > r)) { continue; }
				boolean needAdd = true;
				if (ds.getQuestID() > 0) {
					if (attacking instanceof EntityPlayer) {
						IPlayer<?> player = (IPlayer<?>) NpcAPI.Instance().getIEntity(attacking);
						for (IQuest q : player.getActiveQuests()) {
							if (q.getId() == ds.getQuestID()) {
								for (IQuestObjective objQ : q.getObjectives(player)) {
									if (!objQ.isCompleted()) {
										needAdd = false;
										break;
									}
								}
								break;
							}
						}
					}
				}
				if (needAdd && !(ds.amount[0]==0 && ds.amount[1]==0)) { preMap.put(ds.createLoot(ch), ds.chance); }
			}
			if (preMap.isEmpty()) { continue; }
			int p = this.rnd.nextInt(preMap.size());
			if (p==preMap.size()) { p = preMap.size() - 1; }
			else if (p<0) { p = 0; }
			int i = 0;
			IItemStack hStack = null; 
			double h = this.dropType==1 ? 2.0d : -1.0d;
			for (IItemStack stack : preMap.keySet()) {
				if (this.dropType==3) { // all
					list.add(stack);
					continue;
				}
				if (this.dropType==0) { // rnd
					if (i!=p) { i++; continue; }
					list.add(stack);
					break;
				}
				double c = preMap.get(stack);
				if (this.dropType==1) { // min
					if (h>=c) {
						h = c;
						hStack = stack;
					}
				} else { // max
					if (h<=c) {
						h = c;
						hStack = stack;
					}
				}
			}
			if (hStack!=null) { list.add(hStack); }
		}
		return list;
	}

	public ICustomDrop addDropItem(int id, IItemStack item, double chance) {
		if (this.groups.containsKey(id)) {
			id = this.groups.size();
			this.groups.put(id, Maps.<Integer, DropSet>newTreeMap());
		}
		chance = ValueUtil.correctDouble(chance, 0.0001d, 100.0d);
		DropSet ds = new DropSet(null);
		ds.item = item;
		ds.chance = chance;
		ds.pos = this.groups.get(id).size();
		this.groups.get(id).put(ds.pos, ds);
		return (ICustomDrop) ds;
	}

	public void removeDrop(int groupId, int slot) {
		if (!this.groups.containsKey(groupId) || !this.groups.get(groupId).containsKey(slot)) { return; }
		this.groups.get(groupId).remove(groupId);
		Map<Integer, DropSet> newDrop = Maps.newTreeMap();
		int j = 0;
		for (int s : this.groups.keySet()) {
			if (s==slot) { continue; }
			newDrop.put(j, this.groups.get(groupId).get(s));
			newDrop.get(j).pos = j;
			j++;
		}
		this.groups.get(groupId).clear();
		this.groups.get(groupId).putAll(newDrop);
	}

	public void removeGroup(int groupId) {
		if (!this.groups.containsKey(groupId)) { return; }
		this.groups.get(groupId).remove(groupId);
		Map<Integer, Map<Integer, DropSet>> newGroups = Maps.<Integer, Map<Integer, DropSet>>newTreeMap();
		int j = 0;
		for (int gId : this.groups.keySet()) {
			if (gId==groupId) { continue; }
			newGroups.put(j, this.groups.get(gId));
			j++;
		}
		this.groups.clear();
		this.groups.putAll(newGroups);
	}

}
