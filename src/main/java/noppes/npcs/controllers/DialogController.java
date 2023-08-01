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
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.Server;
import noppes.npcs.api.handler.IDialogHandler;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IDialogCategory;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.NBTJsonUtil;

public class DialogController
implements IDialogHandler {
	
	public static DialogController instance = new DialogController();
	public final TreeMap<Integer, DialogCategory> categories;
	public final TreeMap<Integer, DialogCategory> categoriesSync;
	public final TreeMap<Integer, Dialog> dialogs;
	private int lastUsedCatID;
	private int lastUsedDialogID;

	public DialogController() {
		this.categoriesSync = Maps.<Integer, DialogCategory>newTreeMap();
		this.categories = Maps.<Integer, DialogCategory>newTreeMap();
		this.dialogs = Maps.<Integer, Dialog>newTreeMap();
		this.lastUsedDialogID = 0;
		this.lastUsedCatID = 0;
		DialogController.instance = this;
	}

	@Override
	public IDialogCategory[] categories() {
		return this.categories.values().toArray(new IDialogCategory[this.categories.size()]);
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
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.startDebug("Common", null, "loadDialogs");
		}
		LogWriter.info("Loading Dialogs");
		this.loadCategories();
		LogWriter.info("Done loading Dialogs");
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.endDebug("Common", null, "loadDialogs");
		}
	}

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
		} catch (Exception e) {
			LogWriter.except(e);
		}
		File dir = this.getDir();
		if (!dir.exists()) {
			dir.mkdir();
			this.loadDefaultDialogs();
		} else {
			for (File file2 : dir.listFiles()) {
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
		NBTTagCompound nbttagcompound1 = CompressedStreamTools.readCompressed(new FileInputStream(file));
		NBTTagList list = nbttagcompound1.getTagList("Data", 10);
		if (list == null) {
			return;
		}
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
		for (File file : dir.listFiles()) {
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
		dia2.text = AdditionalMethods.instance.deleteColor(new TextComponentTranslation("dialog.base.2.text").getFormattedText());
		Dialog dia3 = new Dialog(cat);
		dia3.id = 3;
		dia3.title = "Who are you";
		dia3.text = AdditionalMethods.instance.deleteColor(new TextComponentTranslation("dialog.base.3.text").getFormattedText());
		cat.dialogs.put(dia1.id, dia1);
		cat.dialogs.put(dia2.id, dia2);
		cat.dialogs.put(dia3.id, dia3);
		DialogOption option = new DialogOption();
		option.title = "dialog.base.1.option.1";
		option.addDialog(2);
		option.optionType = 1;
		DialogOption option2 = new DialogOption();
		option2.title = AdditionalMethods.instance.deleteColor(new TextComponentTranslation("dialog.base.1.option.0").getFormattedText());
		option2.addDialog(3);
		option2.optionType = 1;
		DialogOption option3 = new DialogOption();
		option3.title = AdditionalMethods.instance.deleteColor(new TextComponentTranslation("dialog.base.1.option.2").getFormattedText());
		option3.optionType = 0;
		dia1.options.put(0, option2);
		dia1.options.put(1, option);
		dia1.options.put(2, option3);
		DialogOption option4 = new DialogOption();
		option4.title = AdditionalMethods.instance.deleteColor(new TextComponentTranslation("dialog.base.2.option.0").getFormattedText());
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
		// if (!dir.delete()) { return; } Changed
		// New
		if (!AdditionalMethods.remove(dir)) {
			LogWriter.error("Error delite " + dir + "; no access or file not uploaded!");
		}
		for (int dia : cat.dialogs.keySet()) {
			this.dialogs.remove(dia);
		}
		this.categories.remove(category);
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_REMOVE, 5, category);
	}

	public void removeDialog(Dialog dialog) {
		DialogCategory category = dialog.category;
		File file = new File(new File(this.getDir(), category.title), dialog.id + ".json");
		if (!file.delete()) {
			return;
		}
		category.dialogs.remove(dialog.id);
		this.dialogs.remove(dialog.id);
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_REMOVE, 4, dialog.id);
	}

	public void saveCategory(DialogCategory category) {
		category.title = NoppesStringUtils.cleanFileName(category.title);
		if (this.categories.containsKey(category.id)) {
			DialogCategory currentCategory = this.categories.get(category.id);
			if (!currentCategory.title.equals(category.title)) {
				while (this.containsCategoryName(category)) {
					category.title += "_";
				}
				File newdir = new File(this.getDir(), category.title);
				File olddir = new File(this.getDir(), currentCategory.title);
				if (newdir.exists()) {
					return;
				}
				if (!olddir.renameTo(newdir)) {
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
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, 5, category.writeNBT(new NBTTagCompound()));
	}

	public Dialog saveDialog(DialogCategory category, Dialog dialog) {
		if (category == null) {
			return dialog;
		}
		while (this.containsDialogName(dialog.category, dialog)) {
			dialog.title += "_";
		}
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
			NBTJsonUtil.SaveFile(file, compound);
			if (file2.exists()) {
				file2.delete();
			}
			file.renameTo(file2);
			Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, 4, compound, category.id);
		} catch (Exception e) {
			LogWriter.except(e);
		}
		return dialog;
	}

}
