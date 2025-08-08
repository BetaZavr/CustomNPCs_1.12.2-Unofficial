package noppes.npcs.controllers;

import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

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
import noppes.npcs.constants.EnumSync;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.util.Util;
import noppes.npcs.util.NBTJsonUtil;

public class QuestController implements IQuestHandler {

	public static QuestController instance = new QuestController();
	public final TreeMap<Integer, QuestCategory> categories = new TreeMap<>();
	public final TreeMap<Integer, QuestCategory> categoriesSync = new TreeMap<>();
	public final TreeMap<Integer, Quest> quests = new TreeMap<>();
	private int lastUsedCatID = 0;
	private int lastUsedQuestID = 0;

	public QuestController() { instance = this; }

	@Override
	public IQuestCategory[] categories() {
		return categories.values().toArray(new IQuestCategory[0]);
	}

	public boolean containsCategoryName(QuestCategory category) {
		for (QuestCategory cat : categories.values()) {
			if (cat.id == category.id && cat.title.equalsIgnoreCase(category.title)) {
				return true;
			}
		}
		return false;
	}

	public boolean containsQuestName(QuestCategory category, Quest quest) {
		for (Quest q : category.quests.values()) {
			if (q.id != quest.id && q.getName().equalsIgnoreCase(quest.getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IQuest get(int id) {
		return quests.get(id);
	}

	private File getDir() {
		return new File(CustomNpcs.getWorldSaveDirectory(), "quests");
	}

	@SuppressWarnings("all")
	public void load() {
		CustomNpcs.debugData.start(null);
		categories.clear();
		quests.clear();
		lastUsedCatID = 0;
		lastUsedQuestID = 0;
		try {
			File file = new File(CustomNpcs.getWorldSaveDirectory(), "quests.dat");
			if (file.exists()) {
				loadCategoriesOld(file);
				file.delete();
				file = new File(CustomNpcs.getWorldSaveDirectory(), "quests.dat_old");
				if (file.exists()) {
					file.delete();
				}
				CustomNpcs.debugData.end(null);
				return;
			}
		} catch (Exception e) { LogWriter.error(e); }
		File dir = getDir();
		if (!dir.exists()) {
			dir.mkdir();
		} else {
			for (File file2 : Objects.requireNonNull(dir.listFiles())) {
				if (file2.isDirectory()) {
					QuestCategory category = loadCategoryDir(file2);
					Iterator<Integer> ite = category.quests.keySet().iterator();
					while (ite.hasNext()) {
						int id = ite.next();
						if (id > lastUsedQuestID) {
							lastUsedQuestID = id;
						}
						Quest quest = category.quests.get(id);
						if (quests.containsKey(id)) {
							LogWriter.error("Duplicate id " + quest.id + " from category " + category.title);
							ite.remove();
						} else {
							quests.put(id, quest);
						}
					}
					++lastUsedCatID;
					category.id = lastUsedCatID;
					categories.put(category.id, category);
				}
			}
		}
		CustomNpcs.debugData.end(null);
	}

	private void loadCategoriesOld(File file) throws Exception {
		NBTTagCompound compound = CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath()));
		lastUsedCatID = compound.getInteger("lastID");
		lastUsedQuestID = compound.getInteger("lastQuestID");
		NBTTagList list = compound.getTagList("Data", 10);
        for (int i = 0; i < list.tagCount(); ++i) {
            QuestCategory category = new QuestCategory();
            category.load(list.getCompoundTagAt(i));
            categories.put(category.id, category);
            saveCategory(category);
            Iterator<Map.Entry<Integer, Quest>> ita = category.quests.entrySet().iterator();
            while (ita.hasNext()) {
                Map.Entry<Integer, Quest> entry = ita.next();
                Quest quest = entry.getValue();
                quest.id = entry.getKey();
                if (quests.containsKey(quest.id)) {
                    ita.remove();
                } else {
                    saveQuest(category, quest);
                }
            }
        }
    }

	private QuestCategory loadCategoryDir(File dir) {
		QuestCategory category = new QuestCategory();
		category.title = dir.getName();
		for (File file : Objects.requireNonNull(dir.listFiles())) {
			if (file.isFile()) {
				if (file.getName().endsWith(".json")) {
					try {
						Quest quest = new Quest(category);
						quest.id = Integer.parseInt(file.getName().substring(0, file.getName().length() - 5));
						quest.loadPartial(NBTJsonUtil.LoadFile(file));
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
		QuestCategory cat = categories.get(category);
		if (cat == null) {
			return;
		}
		File dir = new File(getDir(), cat.title);
		if (!Util.instance.removeFile(dir)) {
			LogWriter.error("Error delete " + dir + "; no access or file not uploaded!");
			return;
		}
		for (Integer qId : cat.quests.keySet()) {
			quests.remove(qId);
		}
		categories.remove(category);
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_REMOVE, EnumSync.QuestCategoriesData, category);
	}

	@SuppressWarnings("all")
	public void removeQuest(Quest quest) {
		File file = new File(new File(getDir(), quest.category.title), quest.id + ".json");
		if (file.exists()) {
			file.delete();
		}
		quests.remove(quest.id);
		quest.category.quests.remove(quest.id);
		for (QuestCategory cat : categories.values()) {
            cat.quests.remove(quest.id);
		}
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_REMOVE, EnumSync.QuestData, quest.id);
	}

	@SuppressWarnings("all")
	public void saveCategory(QuestCategory category) {
		CustomNpcs.debugData.start(null);
		category.title = NoppesStringUtils.cleanFileName(category.title);
		if (category.title.isEmpty()) {
			category.title = "default";
			while (containsCategoryName(category)) {
				category.title += "_";
			}
		}
		if (categories.containsKey(category.id)) {
			QuestCategory currentCategory = categories.get(category.id);
			File newdir = new File(getDir(), category.title);
			File olddir = new File(getDir(), currentCategory.title);
			while (containsCategoryName(category)) {
				category.title += "_";
			}
			if (newdir.exists() || !olddir.renameTo(newdir)) {
				CustomNpcs.debugData.end(null);
				return;
			}
			category.quests.clear();
			category.quests.putAll(currentCategory.quests);
		} else {
			if (category.id < 0) {
				++lastUsedCatID;
				category.id = lastUsedCatID;
			}
			while (containsCategoryName(category)) {
				category.title += "_";
			}
			File dir = new File(getDir(), category.title);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
		categories.put(category.id, category);
		for (Quest quest : quests.values()) {
			if (quest.category.id == category.id) {
				quest.category = category;
			}
		}
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, EnumSync.QuestCategoriesData, category.save(new NBTTagCompound()));
		CustomNpcs.debugData.end(null);
	}

	@SuppressWarnings("all")
	public void saveQuest(QuestCategory category, Quest quest) {
		if (category == null) { return; }
		CustomNpcs.debugData.start(null);
		while (containsQuestName(quest.category, quest)) {
			quest.setName(quest.getName() + "_");
		}
		if (quest.id < 0) {
			++lastUsedQuestID;
			quest.id = lastUsedQuestID;
		}
		quests.put(quest.id, quest);
		category.quests.put(quest.id, quest);
		File dir = new File(getDir(), category.title);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir, quest.id + ".json_new");
		File file2 = new File(dir, quest.id + ".json");
		try {
			Util.instance.saveFile(file, quest.saveToPartial(new NBTTagCompound()));
			if (file2.exists()) {
				file2.delete();
			}
			file.renameTo(file2);
			Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, EnumSync.QuestData, quest.save(new NBTTagCompound()), category.id);
		} catch (Exception e) { LogWriter.error(e); }
		CustomNpcs.debugData.end(null);
	}

}
