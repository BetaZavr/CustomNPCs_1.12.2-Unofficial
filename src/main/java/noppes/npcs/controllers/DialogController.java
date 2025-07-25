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
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.Server;
import noppes.npcs.api.constants.OptionType;
import noppes.npcs.api.handler.IDialogHandler;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IDialogCategory;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.DialogGuiSettings;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.util.Util;
import noppes.npcs.util.NBTJsonUtil;

public class DialogController implements IDialogHandler {

	public static DialogController instance = new DialogController();
	public final TreeMap<Integer, DialogCategory> categories = new TreeMap<>();
	public final TreeMap<Integer, DialogCategory> categoriesSync = new TreeMap<>();
	public final TreeMap<Integer, Dialog> dialogs = new TreeMap<>();
	private int lastUsedCatID = 0;
	private int lastUsedDialogID = 0;

	private final DialogGuiSettings guiSettings = new DialogGuiSettings();

	public DialogController() {
		DialogController.instance = this;
	}

	@Override
	public IDialogCategory[] categories() {
		return this.categories.values().toArray(new IDialogCategory[0]);
	}

	public boolean containsCategoryName(DialogCategory category) {
		for (DialogCategory cat : this.categories.values()) {
			if (category.id != cat.id && cat.title.equalsIgnoreCase(category.title)) {
				return true;
			}
		}
		return false;
	}

	public boolean containsDialogName(DialogCategory category, Dialog dialog) {
		for (Dialog dia : category.dialogs.values()) {
			if (dia.id != dialog.id && dia.title.equalsIgnoreCase(dialog.title)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public IDialog get(int id) {
		return this.dialogs.get(id);
	}

	private File getDir() {
		return new File(CustomNpcs.getWorldSaveDirectory(), "dialogs");
	}

	public boolean hasDialog(int dialogId) {
		return this.dialogs.containsKey(dialogId);
	}

	public void load() {
		CustomNpcs.debugData.start(null);
		LogWriter.info("Loading Dialogs");
		this.loadCategories();
		try {
			File file = new File(CustomNpcs.getWorldSaveDirectory(), "dialog_gui_settings.dat");
			if (file.exists()) { guiSettings.load(CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath()))); }
			else { saveSettings(); }
		} catch (Exception e) { LogWriter.except(e); }
		LogWriter.info("Done loading Dialogs");
		CustomNpcs.debugData.end(null);
	}

	@SuppressWarnings("all")
	private void loadCategories() {
		this.categories.clear();
		this.dialogs.clear();
		this.lastUsedCatID = 0;
		this.lastUsedDialogID = 0;
		try {
			File file = new File(CustomNpcs.getWorldSaveDirectory(), "dialog.dat");
			if (file.exists()) {
				this.loadCategoriesOld(file);
				file.delete();
				file = new File(CustomNpcs.getWorldSaveDirectory(), "dialog.dat_old");
				if (file.exists()) {
					file.delete();
				}
				return;
			}
		} catch (Exception e) { LogWriter.except(e); }
		File dir = this.getDir();
		if (!dir.exists()) {
			dir.mkdir();
			this.loadDefaultDialogs();
		}
		else {
			for (File file2 : Objects.requireNonNull(dir.listFiles())) {
				if (file2.isDirectory()) {
					DialogCategory category = this.loadCategoryDir(file2);
					Iterator<Map.Entry<Integer, Dialog>> ite = category.dialogs.entrySet().iterator();
					while (ite.hasNext()) {
						Map.Entry<Integer, Dialog> entry = ite.next();
						int id = entry.getKey();
						if (id > this.lastUsedDialogID) {
							this.lastUsedDialogID = id;
						}
						Dialog dialog = entry.getValue();
						if (this.dialogs.containsKey(id)) {
							LogWriter.error("Duplicate id " + dialog.id + " from category " + category.title);
							ite.remove();
						} else {
							this.dialogs.put(id, dialog);
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
		NBTTagCompound compound = CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath()));
		NBTTagList list = compound.getTagList("Data", 10);
        for (int i = 0; i < list.tagCount(); ++i) {
			DialogCategory category = new DialogCategory();
			category.readNBT(list.getCompoundTagAt(i));
			this.saveCategory(category);
			Iterator<Map.Entry<Integer, Dialog>> ita = category.dialogs.entrySet().iterator();
			while (ita.hasNext()) {
				Map.Entry<Integer, Dialog> entry = ita.next();
				Dialog dialog = entry.getValue();
				dialog.id = entry.getKey();
				if (this.dialogs.containsKey(dialog.id)) {
					ita.remove();
				} else {
					this.saveDialog(category, dialog);
				}
			}
		}
	}

	private DialogCategory loadCategoryDir(File dir) {
		DialogCategory category = new DialogCategory();
		category.title = dir.getName();
		for (File file : Objects.requireNonNull(dir.listFiles())) {
			if (file.isFile()) {
				if (file.getName().endsWith(".json")) {
					try {
						Dialog dialog = new Dialog(category);
						dialog.id = Integer.parseInt(file.getName().substring(0, file.getName().length() - 5));
						dialog.readNBTPartial(NBTJsonUtil.LoadFile(file));
						category.dialogs.put(dialog.id, dialog);
					} catch (Exception e) {
						LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
					}
				}
			}
		}
		return category;
	}

	private void loadDefaultDialogs() {
		DialogCategory cat = new DialogCategory();
		cat.id = this.lastUsedCatID++;
		cat.title = "Villager";
		Dialog dia1 = new Dialog(cat);
		dia1.id = 1;
		dia1.title = "Start";
		dia1.text = "dialog.base.1.text";
		Dialog dia2 = new Dialog(cat);
		dia2.id = 2;
		dia2.title = "Ask about village";
		dia2.text = Util.instance
				.deleteColor(new TextComponentTranslation("dialog.base.2.text").getFormattedText());
		Dialog dia3 = new Dialog(cat);
		dia3.id = 3;
		dia3.title = "Who are you";
		dia3.text = Util.instance
				.deleteColor(new TextComponentTranslation("dialog.base.3.text").getFormattedText());
		cat.dialogs.put(dia1.id, dia1);
		cat.dialogs.put(dia2.id, dia2);
		cat.dialogs.put(dia3.id, dia3);
		DialogOption option = new DialogOption();
		option.title = "dialog.base.1.option.0";
		option.addDialog(2);
		option.optionType = OptionType.DIALOG_OPTION;
		DialogOption option2 = new DialogOption();
		option2.title = "dialog.base.1.option.1";
		option2.addDialog(3);
		option2.optionType = OptionType.DIALOG_OPTION;
		DialogOption option3 = new DialogOption();
		option3.title = "dialog.base.1.option.2";
		option3.optionType = OptionType.QUIT_OPTION;
		dia1.options.put(0, option2);
		dia1.options.put(1, option);
		dia1.options.put(2, option3);
		DialogOption option4 = new DialogOption();
		option4.title = Util.instance.deleteColor(new TextComponentTranslation("dialog.base.2.option.0").getFormattedText());
		option4.addDialog(1);
		dia2.options.put(1, option4);
		dia3.options.put(1, option4);
		this.lastUsedDialogID = 3;
		this.lastUsedCatID = 1;
		this.saveCategory(cat);
		this.saveDialog(cat, dia1);
		this.saveDialog(cat, dia2);
		this.saveDialog(cat, dia3);
	}

	public void removeCategory(int category) {
		DialogCategory cat = this.categories.get(category);
		if (cat == null) {
			return;
		}
		File dir = new File(this.getDir(), cat.title);
		if (!Util.instance.removeFile(dir)) {
			LogWriter.error("Error delete " + dir + "; no access or file not uploaded!");
		}
		for (int dia : cat.dialogs.keySet()) {
			this.dialogs.remove(dia);
		}
		this.categories.remove(category);
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_REMOVE, EnumSync.DialogCategoriesData, category);
	}

	public void removeDialog(Dialog dialog) {
		DialogCategory category = dialog.category;
		File file = new File(new File(this.getDir(), category.title), dialog.id + ".json");
		if (!file.delete()) {
			return;
		}
		category.dialogs.remove(dialog.id);
		this.dialogs.remove(dialog.id);
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_REMOVE, EnumSync.DialogData, dialog.id);
	}

	@SuppressWarnings("all")
	public void saveCategory(DialogCategory category) {
		CustomNpcs.debugData.start(null);
		category.title = NoppesStringUtils.cleanFileName(category.title);
		if (category.title.isEmpty()) {
			category.title = "default";
			while (this.containsCategoryName(category)) { category.title += "_"; }
		}
		if (this.categories.containsKey(category.id)) {
			DialogCategory currentCategory = this.categories.get(category.id);
			if (!currentCategory.title.equals(category.title)) {
				while (this.containsCategoryName(category)) { category.title += "_"; }
				File newDir = new File(this.getDir(), category.title);
				File oldDir = new File(this.getDir(), currentCategory.title);
				if (newDir.exists() || !oldDir.renameTo(newDir)) {
					CustomNpcs.debugData.end(null);
					return;
				}
			}
			category.dialogs.clear();
			category.dialogs.putAll(currentCategory.dialogs);
		} else {
			if (category.id < 0) {
				++this.lastUsedCatID;
				category.id = this.lastUsedCatID;
			}
			while (this.containsCategoryName(category)) {
				category.title += "_";
			}
			File dir = new File(this.getDir(), category.title);
			if (!dir.exists()) {
				dir.mkdirs();
			}
		}
		this.categories.put(category.id, category);
		for (Dialog dialog : dialogs.values()) {
			if (dialog.category.id == category.id) {
				dialog.category = category;
			}
		}
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, EnumSync.DialogCategoriesData, category.writeNBT(new NBTTagCompound()));
		CustomNpcs.debugData.end(null);
	}

	@SuppressWarnings("all")
	public void saveDialog(DialogCategory category, Dialog dialog) {
		if (category == null) { return; }
		CustomNpcs.debugData.start(null);
		StringBuilder title = new StringBuilder(dialog.title);
		while (this.containsDialogName(dialog.category, dialog)) {
			title.append("_");
		}
		dialog.title = title.toString();
		if (dialog.id < 0) {
			++this.lastUsedDialogID;
			dialog.id = this.lastUsedDialogID;
		}
		this.dialogs.put(dialog.id, dialog);
		category.dialogs.put(dialog.id, dialog);
		File dir = new File(this.getDir(), category.title);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir, dialog.id + ".json_new");
		File file2 = new File(dir, dialog.id + ".json");
		try {
			NBTTagCompound compound = dialog.writeToNBT(new NBTTagCompound());
			Util.instance.saveFile(file, compound);
			if (file2.exists()) {
				file2.delete();
			}
			file.renameTo(file2);
			Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, EnumSync.DialogData, compound,
					category.id);
		}
		catch (Exception e) { LogWriter.except(e);}
		CustomNpcs.debugData.end(null);
	}

	@SuppressWarnings("all")
	public void saveSettings() {
		CustomNpcs.debugData.start(null);
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			File file = new File(saveDir, "dialog_gui_settings.dat_new");
			File file2 = new File(saveDir, "dialog_gui_settings.dat_old");
			File file3 = new File(saveDir, "dialog_gui_settings.dat");
			CompressedStreamTools.writeCompressed(guiSettings.save(), Files.newOutputStream(file.toPath()));
			if (file2.exists()) { file2.delete(); }
			file3.renameTo(file2);
			if (file3.exists()) { file3.delete(); }
			file.renameTo(file3);
			if (file.exists()) { file.delete(); }
		} catch (Exception e) { LogWriter.error("Error:", e); }
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, EnumSync.DialogGuiSettings, guiSettings.save());
		CustomNpcs.debugData.end(null);
	}

	public DialogGuiSettings getGuiSettings() { return guiSettings; }

}
