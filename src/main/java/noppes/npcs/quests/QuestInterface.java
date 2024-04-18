package noppes.npcs.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.LogWriter;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.util.AdditionalMethods;

// Global Changed
public class QuestInterface {
	
	private int id = 0;
	public NpcMiscInventory items = new NpcMiscInventory(1);
	public QuestObjective[] tasks;

	public QuestInterface() {
		this.items = new NpcMiscInventory(1);
		this.tasks = new QuestObjective[0];
	}

	public QuestObjective addTask(EnumQuestTask type) {
		if (this.tasks.length >= 9) {
			return null;
		}
		QuestObjective[] ts = new QuestObjective[this.tasks.length + 1];
		for (int i = 0; i < this.tasks.length; i++) {
			ts[i] = this.tasks[i];
		}
		ts[this.tasks.length] = new QuestObjective(this.id, type);
		this.tasks = ts;
		fix();
		return this.tasks[this.tasks.length - 1];
	}

	public void downPos(QuestObjective task) {
		QuestObjective[] ts = new QuestObjective[this.tasks.length];
		try {
			for (int i = 0, j = 0; i < this.tasks.length; i++) {
				if (this.tasks[i] == task) {
					continue;
				}
				ts[j] = this.tasks[i];
				j++;
				if ((i - 1) >= 0 && this.tasks[i - 1] == task) {
					ts[j] = this.tasks[i - 1];
					j++;
				}
			}
			this.tasks = ts;
			fix();
		} catch (Exception e) {
			LogWriter.error("CNPCs Error ", e);
		}
	}

	public void fix() {
		List<QuestObjective> tsl = new ArrayList<QuestObjective>();
		Map<Integer, ItemStack> stacks = Maps.<Integer, ItemStack>newTreeMap();
		for (int i = 0; i < this.tasks.length; i++) {
			if (this.tasks[i] == null) { continue; }
			QuestObjective to = this.tasks[i];
			if (to.getMaxProgress() <= 0) { to.setMaxProgress(1); }
			stacks.put(i, ItemStack.EMPTY);
			if ((to.getEnumType() == EnumQuestTask.ITEM || to.getEnumType() == EnumQuestTask.CRAFT)) {
				stacks.put(i, to.getItemStack());
			} else if (to.getEnumType() == EnumQuestTask.AREAKILL) {
				if (to.getAreaRange() < 3) {
					to.setAreaRange(3);
				} else if (to.getAreaRange() > 32) {
					to.setAreaRange(24);
				}
			}
			tsl.add(to);
		}
		QuestObjective[] ts = new QuestObjective[tsl.size()];
		for (int i = 0; i < tsl.size(); i++) {
			ts[i] = tsl.get(i);
		}
		this.tasks = ts;
		this.items = new NpcMiscInventory(stacks.size());
		for (int i = 0; i < stacks.size(); i++) {
			this.items.setInventorySlotContents(i, stacks.get(i));
		}
	}

	public boolean getFound(QuestData data, QuestObjective object) {
		boolean isFound = false;
		for (NBTBase dataNBT : data.extraData.getTagList("Locations", 10)) {
			if (object.getTargetName().equalsIgnoreCase(((NBTTagCompound) dataNBT).getString("Location")) && ((NBTTagCompound) dataNBT).getBoolean("Found")) {
				isFound = true;
				break;
			}
		}
		return isFound;
	}

	public int getId() {
		return this.id;
	}

	public Map<String, QuestObjective> getKeys() {
		Map<String, QuestObjective> keys = new HashMap<String, QuestObjective>();
		String chr = ""+((char) 167);
		for (int i = 0; i < this.tasks.length; i++) {
			QuestObjective to = this.tasks[i];
			String key = (i + 1) + "-";
			String name = "";
			switch (to.getEnumType()) {
			case DIALOG: {
				name = new TextComponentTranslation("quest.has.false").getFormattedText();
				Dialog d = DialogController.instance.dialogs.get(to.getTargetID());
				if (d != null) {
					name = chr + "8"
							+ AdditionalMethods.instance.deleteColor(new TextComponentTranslation(d.category.getName()).getFormattedText())
							+ "/" + chr + "r" + new TextComponentTranslation(d.getName()).getFormattedText();
				}
				key += "[" + chr + "bDr" + chr + "r] " + name;
				keys.put(key, to);
				break;
			}
			case KILL: {
				name = new TextComponentTranslation("entity." + to.getTargetName() + ".name", new Object[0])
						.getFormattedText();
				if (to.getTargetName().isEmpty()) {
					name = new TextComponentTranslation("quest.has.false").getFormattedText();
				} else if (name.indexOf("entity.") == 0 && name.indexOf(".name") > 0) {
					name = to.getTargetName();
				}
				key += "[" + chr + "cK" + chr + "r] " + name + " = " + to.getMaxProgress();
				keys.put(key, to);
				break;
			}
			case AREAKILL: {
				name = new TextComponentTranslation("entity." + to.getTargetName() + ".name", new Object[0])
						.getFormattedText();
				if (to.getTargetName().isEmpty()) {
					name = new TextComponentTranslation("quest.has.false").getFormattedText();
				} else if (name.indexOf("entity.") == 0 && name.indexOf(".name") > 0) {
					name = to.getTargetName();
				}
				key += "[" + chr + "4AK" + chr + "r] " + name + " = " + to.getMaxProgress();
				keys.put(key, to);
				break;
			}
			case LOCATION: {
				name = to.getTargetName();
				if (to.getTargetName().isEmpty()) {
					name = new TextComponentTranslation("quest.has.false").getFormattedText();
				}
				key += "[" + chr + "2L" + chr + "r] " + name;
				keys.put(key, to);
				break;
			}
			case MANUAL: {
				name = to.getTargetName();
				if (to.getTargetName().isEmpty()) {
					name = new TextComponentTranslation("quest.has.false").getFormattedText();
				}
				key += "[" + chr + "dM" + chr + "r] " + name + " = " + to.getMaxProgress();
				keys.put(key, to);
				break;
			}
			case CRAFT: {
				name = to.getItemStack().getDisplayName();
				if (to.getItemStack().isEmpty()) {
					name = new TextComponentTranslation("quest.has.false").getFormattedText();
				}
				key += "[" + chr + "eIc" + chr + "r] " + name + " = " + to.getMaxProgress();
				keys.put(key, to);
				break;
			}
			default: { // ITEM
				name = to.getItemStack().getDisplayName();
				if (to.getItemStack().isEmpty()) {
					name = new TextComponentTranslation("quest.has.false").getFormattedText();
				}
				key += "[" + chr + "6If" + chr + "r] " + name + " = " + to.getMaxProgress();
				keys.put(key, to);
			}
			}
		}
		return keys;
	}

	public IQuestObjective[] getObjectives(EntityPlayer player) {
		IQuestObjective[] array = new IQuestObjective[this.tasks.length];
		for (int i = 0; i < this.tasks.length; i++) {
			array[i] = this.tasks[i].copyToPlayer(player);
		}
		return array;
	}

	public int getPos(QuestObjective task) {
		for (int i = 0; i < this.tasks.length; i++) {
			if (this.tasks[i] == task) {
				return i;
			}
		}
		return -1;
	}

	public Map<ItemStack, Integer> getProgressSet(EntityPlayer player) {
		HashMap<ItemStack, Integer> map = new HashMap<ItemStack, Integer>();
		List<QuestObjective> mapTO = new ArrayList<QuestObjective>();
		for (QuestObjective to : this.tasks) {
			if (to.getEnumType() != EnumQuestTask.ITEM || to.getEnumType() != EnumQuestTask.CRAFT) {
				continue;
			}
			if (NoppesUtilServer.IsItemStackNull(to.getItemStack())) {
				continue;
			}
			mapTO.add(to);
		}
		for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
			ItemStack item = player.inventory.getStackInSlot(i);
			if (!NoppesUtilServer.IsItemStackNull(item)) {
				for (QuestObjective to : mapTO) {
					if (NoppesUtilPlayer.compareItems(to.getItemStack(), item, to.isIgnoreDamage(),
							to.isItemIgnoreNBT())) {
						int count = 0;
						if (map.containsKey(to.getItemStack())) {
							count = map.get(to.getItemStack());
						}
						map.put(to.getItemStack(), count + item.getCount());
					}
				}
			}
		}
		return map;
	}

	public void handleComplete(EntityPlayer player) {
		boolean bo = false;
		for (QuestObjective to : this.tasks) {
			if (to.getEnumType() != EnumQuestTask.ITEM || to.getItemStack().isEmpty() || !to.isItemLeave()) {
				continue;
			}
			int stacksize = to.getMaxProgress();
			for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
				ItemStack stack = player.inventory.getStackInSlot(i);
				if (!NoppesUtilServer.IsItemStackNull(stack) && NoppesUtilPlayer.compareItems(stack, to.getItemStack(),
						to.isIgnoreDamage(), to.isItemIgnoreNBT())) {
					bo = true;
					int size = stack.getCount();
					if (stacksize - size >= 0) {
						player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
						stack.splitStack(size);
					} else {
						stack.splitStack(stacksize);
					}
					stacksize -= size;
					if (stacksize <= 0) {
						break;
					}
				}
			}
		}
		if (bo) {
			player.inventoryContainer.detectAndSendChanges();
		}
	}

	public boolean isCompleted(EntityPlayer player) {
		PlayerData playerdata = PlayerData.get(player);
		QuestData data = playerdata.questData.activeQuests.get(this.id);
		if (data==null) { return false; }
		boolean complete = true;
		for (QuestObjective to : this.tasks) {
			switch (to.getEnumType()) {
				case DIALOG: {
					complete = playerdata.dialogData.dialogsRead.contains(to.getTargetID());
					break;
				}
				case LOCATION: {
					complete = this.getFound(data, to);
					break;
				}
				case KILL: {
					HashMap<String, Integer> killed = to.getKilled(data);
					if (killed.size() == 0) { complete = false; }
					for (String entity : killed.keySet()) {
						if (entity.equalsIgnoreCase(to.getTargetName())) {
							if (killed.get(entity) < to.getMaxProgress()) {
								complete = false;
							}
							break;
						}
					}
					break;
				}
				case AREAKILL: {
					HashMap<String, Integer> killed = to.getKilled(data);
					if (killed.size() == 0) {
						complete = false;
					}
					for (String entity : killed.keySet()) {
						if (entity.equalsIgnoreCase(to.getTargetName())) {
							if (killed.get(entity) < to.getMaxProgress()) {
								complete = false;
							}
							break;
						}
					}
					break;
				}
				case MANUAL: {
					HashMap<String, Integer> manual = to.getKilled(data);
					if (manual.size() == 0) {
						complete = false;
					}
					for (String entity : manual.keySet()) {
						if (entity.equalsIgnoreCase(to.getTargetName())) {
							if (manual.get(entity) < to.getMaxProgress()) {
								complete = false;
							}
							break;
						}
					}
					break;
				}
				case CRAFT: {
					HashMap<ItemStack, Integer> crafted = to.getCrafted(data);
					if (crafted.size() == 0) {
						complete = false;
					}
					for (ItemStack item : crafted.keySet()) {
						if (NoppesUtilPlayer.compareItems(to.getItemStack(), item, to.isIgnoreDamage(),
								to.isItemIgnoreNBT())) {
							if (crafted.get(item) < to.getMaxProgress()) {
								complete = false;
							}
							break;
						}
					}
					break;
				}
				default: { // ITEM
					complete = NoppesUtilPlayer.compareItems(player, to.getItemStack(), to.isIgnoreDamage(), to.isItemIgnoreNBT(), to.getMaxProgress());
				}
			}
			if (data!=null && data.quest!=null) {
				if (!complete && data.quest.step != 2) { return false; }
				if (complete && data.quest.step == 2) { return true; }
			}
		}
		return complete;
	}

	public void readEntityFromNBT(NBTTagCompound compound, int id) {
		this.id = id;
		if (!compound.hasKey("Tasks", 9)) { // Old versions
			List<QuestObjective> oldTasks = new ArrayList<QuestObjective>();
			if (compound.getInteger("Type") == 0) { // Item
				this.items = new NpcMiscInventory(compound.getCompoundTag("Items").getTagList("NpcMiscInv", 10).tagCount());
				this.items.setFromNBT(compound.getCompoundTag("Items"));
				for (ItemStack item : this.items.items) {
					QuestObjective to = new QuestObjective(this.id, EnumQuestTask.ITEM);
					to.setItem(item);
					to.setItemLeave(compound.getBoolean("LeaveItems"));
					to.setItemIgnoreDamage(compound.getBoolean("IgnoreDamage"));
					to.setItemIgnoreNBT(compound.getBoolean("IgnoreNBT"));
					oldTasks.add(to);
				}
			} else if (compound.getInteger("Type") == 1) { // Dialogs
				HashMap<Integer, Integer> dialogs = NBTTags.getIntegerIntegerMap(compound.getTagList("QuestDialogs", 10));
				for (int dId : dialogs.values()) {
					QuestObjective to = new QuestObjective(this.id, EnumQuestTask.DIALOG);
					to.setTargetID(dId); // DialogID
					oldTasks.add(to);
				}
			} else if (compound.getInteger("Type") == 2 || compound.getInteger("Type") == 4) { // Kill or Area Kill
				TreeMap<String, Integer> targets = new TreeMap<String, Integer>(NBTTags.getStringIntegerMap(compound.getTagList("QuestDialogs", 10)));
				for (String name : targets.keySet()) {
					QuestObjective to = new QuestObjective(this.id, EnumQuestTask.values()[compound.getInteger("Type")]);
					to.setTargetName(name);
					to.setMaxProgress(targets.get(name));
					oldTasks.add(to);
				}
			} else if (compound.getInteger("Type") == 3) { // Location
				if (compound.hasKey("QuestLocation", 8)) {
					QuestObjective t0 = new QuestObjective(this.id, EnumQuestTask.LOCATION);
					t0.setTargetName(compound.getString("QuestLocation"));
					oldTasks.add(t0);
				}
				if (compound.hasKey("QuestLocation2", 8)) {
					QuestObjective t1 = new QuestObjective(this.id, EnumQuestTask.LOCATION);
					t1.setTargetName(compound.getString("QuestLocation2"));
					oldTasks.add(t1);
				}
				if (compound.hasKey("QuestLocation3", 8)) {
					QuestObjective t2 = new QuestObjective(this.id, EnumQuestTask.LOCATION);
					t2.setTargetName(compound.getString("QuestLocation3"));
					oldTasks.add(t2);
				}

			} else { // Manual
				TreeMap<String, Integer> manuals = new TreeMap<String, Integer>(NBTTags.getStringIntegerMap(compound.getTagList("QuestManual", 10)));
				for (String name : manuals.keySet()) {
					QuestObjective to = new QuestObjective(this.id, EnumQuestTask.MANUAL);
					to.setTargetName(name);
					to.setMaxProgress(manuals.get(name));
					oldTasks.add(to);
				}
			}
			this.tasks = oldTasks.toArray(new QuestObjective[oldTasks.size() > 9 ? 9 : oldTasks.size()]);
		} else { // New
			this.tasks = new QuestObjective[compound.getTagList("Tasks", 10).tagCount()];
			Map<Integer, ItemStack> stacks = Maps.<Integer, ItemStack>newTreeMap();
			for (int i = 0; i < compound.getTagList("Tasks", 10).tagCount(); i++) {
				QuestObjective to = new QuestObjective(this.id, EnumQuestTask.ITEM);
				to.load(compound.getTagList("Tasks", 10).getCompoundTagAt(i));
				if ((to.getEnumType() == EnumQuestTask.ITEM || to.getEnumType() == EnumQuestTask.CRAFT) && !to.getItemStack().isEmpty()) {
					stacks.put(i, to.getItemStack());
				}
				else { stacks.put(i, ItemStack.EMPTY);}
				this.tasks[i] = to;
			}
			this.items = new NpcMiscInventory(stacks.size());
			for (int i = 0; i < stacks.size(); i++) {
				this.items.items.set(i, stacks.get(i));
			}
		}
		fix();
	}

	public boolean removeTask(QuestObjective task) {
		if (task == null) {
			return false;
		}
		QuestObjective[] ts = new QuestObjective[this.tasks.length - 1];
		boolean del = false;
		for (int i = 0, j = 0; i < this.tasks.length; i++) {
			if (this.tasks[i] == task) {
				del = true;
				continue;
			}
			ts[j] = this.tasks[i];
			j++;
		}
		if (del) {
			this.tasks = ts;
			fix();
		}
		return del;
	}

	public boolean setFound(QuestData data, String location) { // Changed
		if (data == null || data.quest.id != this.id) {
			return false;
		}
		for (QuestObjective to : this.tasks) {
			if (to.getEnumType() != EnumQuestTask.LOCATION || !location.equalsIgnoreCase(to.getTargetName())) {
				continue;
			}
			NBTTagCompound dataNBT = new NBTTagCompound();
			dataNBT.setString("Location", to.getTargetName());
			dataNBT.setBoolean("Found", true);
			
			if (data.extraData.getTagList("Locations", 10).tagCount() == 0) {
				NBTTagList list = new NBTTagList();
				list.appendTag(dataNBT);
				data.extraData.setTag("Locations", list);
				return true;
			}
			boolean found = false;
			for (int i = 0; i < data.extraData.getTagList("Locations", 10).tagCount(); i++) {
				NBTTagCompound dataLoc = data.extraData.getTagList("Locations", 10).getCompoundTagAt(i);
				if (location.equalsIgnoreCase(dataLoc.getString("Location"))) {
					if (!dataLoc.getBoolean("Found")) {
						data.extraData.getTagList("Locations", 10).getCompoundTagAt(i).setBoolean("Found", true);
					}
					else {
						return false;
					}
					found = true;
					break;
				}
			}
			if (!found) {
				data.extraData.getTagList("Locations", 10).appendTag(dataNBT);
				return true;
			}
		}
		return false;
	}

	public void upPos(QuestObjective task) {
		QuestObjective[] ts = new QuestObjective[this.tasks.length];
		try {
			for (int i = 0, j = 0; i < this.tasks.length; i++) {
				if (this.tasks[i] == task) {
					continue;
				}
				if ((i + 1) < this.tasks.length && this.tasks[i + 1] == task) {
					ts[j] = this.tasks[i + 1];
					j++;
				}
				ts[j] = this.tasks[i];
				j++;
			}
			this.tasks = ts;
			fix();
		} catch (Exception e) {
			LogWriter.error("CNPCs Error ", e);
		}
	}

	public void writeEntityToNBT(NBTTagCompound compound) {
		fix();
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < this.tasks.length; i++) {
			list.appendTag(this.tasks[i].getNBT());
		}
		compound.setTag("Tasks", list);
	}

}
