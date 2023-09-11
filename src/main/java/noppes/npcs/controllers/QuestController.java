package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Maps;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.Server;
import noppes.npcs.api.handler.IQuestHandler;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestCategory;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.NBTJsonUtil;

public class QuestController
implements IQuestHandler {
	
	public static QuestController instance = new QuestController();
	public final TreeMap<Integer, QuestCategory> categories;
	public final TreeMap<Integer, QuestCategory> categoriesSync;
	public final TreeMap<Integer, Quest> quests;
	private int lastUsedCatID;
	private int lastUsedQuestID;

	public QuestController() {
		this.categoriesSync = Maps.<Integer, QuestCategory>newTreeMap();
		this.categories = Maps.<Integer, QuestCategory>newTreeMap();
		this.quests = Maps.<Integer, Quest>newTreeMap();
		this.lastUsedCatID = 0;
		this.lastUsedQuestID = 0;
		QuestController.instance = this;
	}

	@Override
	public IQuestCategory[] categories() {
		return this.categories.values().toArray(new IQuestCategory[this.categories.size()]);
	}

	public boolean containsCategoryName(QuestCategory category) {
		for (QuestCategory cat : this.categories.values()) {
			if (cat.id != category.id && cat.title.equalsIgnoreCase(category.title)) {
				return true;
			}
		}
		return false;
	}

	public boolean containsQuestName(QuestCategory category, Quest quest) {
		for (Quest q : category.quests.values()) {
			if (q.id != quest.id && q.getName().equalsIgnoreCase(quest.getName())) { // Changed
				return true;
			}
		}
		return false;
	}

	@Override
	public IQuest get(int id) {
		return this.quests.get(id);
	}

	private File getDir() {
		return new File(CustomNpcs.getWorldSaveDirectory(), "quests");
	}

	public void load() {
		this.categories.clear();
		this.quests.clear();
		this.lastUsedCatID = 0;
		this.lastUsedQuestID = 0;
		try {
			File file = new File(CustomNpcs.getWorldSaveDirectory(), "quests.dat");
			if (file.exists()) {
				this.loadCategoriesOld(file);
				file.delete();
				file = new File(CustomNpcs.getWorldSaveDirectory(), "quests.dat_old");
				if (file.exists()) {
					file.delete();
				}
				return;
			}
		} catch (Exception ex) {
		}
		File dir = this.getDir();
		if (!dir.exists()) {
			dir.mkdir();
		} else {
			for (File file2 : dir.listFiles()) {
				if (file2.isDirectory()) {
					QuestCategory category = this.loadCategoryDir(file2);
					Iterator<Integer> ite = category.quests.keySet().iterator();
					while (ite.hasNext()) {
						int id = ite.next();
						if (id > this.lastUsedQuestID) {
							this.lastUsedQuestID = id;
						}
						Quest quest = category.quests.get(id);
						if (this.quests.containsKey(id)) {
							LogWriter.error("Duplicate id " + quest.id + " from category " + category.title);
							ite.remove();
						} else {
							this.quests.put(id, quest);
						}
					}
					++this.lastUsedCatID;
					category.id = this.lastUsedCatID;
					this.categories.put(category.id, category);
				}
			}
		}
	}

	private void loadCategoriesOld(File file) throws Exception {
		NBTTagCompound nbttagcompound1 = CompressedStreamTools.readCompressed(new FileInputStream(file));
		this.lastUsedCatID = nbttagcompound1.getInteger("lastID");
		this.lastUsedQuestID = nbttagcompound1.getInteger("lastQuestID");
		NBTTagList list = nbttagcompound1.getTagList("Data", 10);
		if (list != null) {
			for (int i = 0; i < list.tagCount(); ++i) {
				QuestCategory category = new QuestCategory();
				category.readNBT(list.getCompoundTagAt(i));
				this.categories.put(category.id, category);
				this.saveCategory(category);
				Iterator<Map.Entry<Integer, Quest>> ita = category.quests.entrySet().iterator();
				while (ita.hasNext()) {
					Map.Entry<Integer, Quest> entry = ita.next();
					Quest quest = entry.getValue();
					quest.id = entry.getKey();
					if (this.quests.containsKey(quest.id)) {
						ita.remove();
					} else {
						this.saveQuest(category, quest);
					}
				}
			}
		}
	}

	private QuestCategory loadCategoryDir(File dir) {
		QuestCategory category = new QuestCategory();
		category.title = dir.getName();
		for (File file : dir.listFiles()) {
			if (file.isFile()) {
				if (file.getName().endsWith(".json")) {
					try {
						Quest quest = new Quest(category);
						quest.id = Integer.parseInt(file.getName().substring(0, file.getName().length() - 5));
						quest.readNBTPartial(NBTJsonUtil.LoadFile(file));
						category.quests.put(quest.id, quest);
					} catch (Exception e) {
						LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
					}
				}
			}
		}
		return category;
	}

	public void removeCategory(int category) {
		QuestCategory cat = this.categories.get(category);
		if (cat == null) { return; }
		File dir = new File(this.getDir(), cat.title);
		// if (!dir.delete()) { return; } Changed
		// New
		if (!AdditionalMethods.remove(dir)) {
			LogWriter.error("Error delite " + dir + "; no access or file not uploaded!");
		}
		for (int dia : cat.quests.keySet()) {
			this.quests.remove(dia);
		}
		this.categories.remove(category);
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_REMOVE, 3, category);
	}

	public void removeQuest(Quest quest) {
		File file = new File(new File(this.getDir(), quest.category.title), quest.id + ".json");
		if (file.exists()) { file.delete(); }
		this.quests.remove(quest.id);
		quest.category.quests.remove(quest.id);
		for (QuestCategory cat : this.categories.values()) {
			if (cat.quests.containsKey(quest.id)) { cat.quests.remove(quest.id); }
		}
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_REMOVE, 2, quest.id);
	}

	public void saveCategory(QuestCategory category) {
		category.title = NoppesStringUtils.cleanFileName(category.title);
		if (this.categories.containsKey(category.id)) {
			QuestCategory currentCategory = this.categories.get(category.id);
			File newdir = new File(this.getDir(), category.title);
			File olddir = new File(this.getDir(), currentCategory.title);
			while (this.containsCategoryName(category)) { category.title += "_"; }
			if (newdir.exists()) { return; }
			if (!olddir.renameTo(newdir)) { return; }
			category.quests.clear();
			category.quests.putAll(currentCategory.quests);
		} else {
			if (category.id < 0) {
				++this.lastUsedCatID;
				category.id = this.lastUsedCatID;
			}
			while (this.containsCategoryName(category)) {
				category.title += "_";
			}
			File dir = new File(this.getDir(), category.title);
			if (!dir.exists()) { dir.mkdirs(); }
		}
		this.categories.put(category.id, category);
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, 3, category.writeNBT(new NBTTagCompound()));
	}

	public void saveQuest(QuestCategory category, Quest quest) {
		if (category == null) { return; }
		while (this.containsQuestName(quest.category, quest)) {
			quest.setName(quest.getName() + "_"); // Changed
		}
		if (quest.id < 0) {
			++this.lastUsedQuestID;
			quest.id = this.lastUsedQuestID;
		}
		this.quests.put(quest.id, quest);
		category.quests.put(quest.id, quest);
		File dir = new File(this.getDir(), category.title);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir, quest.id + ".json_new");
		File file2 = new File(dir, quest.id + ".json");
		try {
			NBTJsonUtil.SaveFile(file, quest.writeToNBTPartial(new NBTTagCompound()));
			if (file2.exists()) {
				file2.delete();
			}
			file.renameTo(file2);
			Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, 2, quest.writeToNBT(new NBTTagCompound()), category.id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
