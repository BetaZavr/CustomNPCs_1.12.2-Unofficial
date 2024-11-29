package noppes.npcs.controllers.data;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.ICompatibilty;
import noppes.npcs.VersionCompatibility;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.OptionType;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IDialogCategory;
import noppes.npcs.api.handler.data.IDialogOption;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.QuestController;

public class Dialog implements ICompatibilty, IDialog {

	public Availability availability;
	public DialogCategory category;
	public PlayerMail mail;
	public FactionOptions factionOptions;
	public final Map<Integer, DialogOption> options = new TreeMap<>();
	public boolean disableEsc, hideNPC, showWheel, stopSound, showFits;
	public int id, quest, version, delay;
	public String command, sound, text, title, texture;

	public Dialog(DialogCategory category) {
		this.version = VersionCompatibility.ModRev;
		this.id = -1;
		this.title = "";
		this.text = "";
		this.texture = "";
		this.quest = -1;
		this.availability = new Availability();
		this.factionOptions = new FactionOptions();
		this.command = "";
		this.mail = new PlayerMail();
		this.hideNPC = false;
		this.showWheel = false;
		this.showFits = true;
		this.disableEsc = false;
		this.stopSound = true;
		this.category = category;
		this.delay = 0;
	}

	public Dialog copy() {
		Dialog dialog = new Dialog(this.category);
		NBTTagCompound compound = new NBTTagCompound();
		this.writeToNBT(compound);
		dialog.readNBT(compound);
		return dialog;
	}

	public Dialog copy(EntityPlayer player) {
		Dialog dialog = new Dialog(this.category);
		dialog.id = this.id;
		dialog.text = this.text;
		dialog.title = this.title;
		dialog.quest = this.quest;
		dialog.sound = this.sound;
		dialog.texture = this.texture;
		dialog.mail = this.mail;
		dialog.command = this.command;
		dialog.hideNPC = this.hideNPC;
		dialog.showWheel = this.showWheel;
		dialog.showFits = this.showFits;
		dialog.disableEsc = this.disableEsc;
		dialog.stopSound = this.stopSound;
		for (int slot : this.options.keySet()) {
			DialogOption option = this.options.get(slot);
			if (option.optionType == OptionType.DISABLED || player != null && !option.isAvailable(player)) {
				continue;
			}
			if (option.optionType == OptionType.DIALOG_OPTION && !option.hasDialogs()) {
				continue;
			}
			dialog.options.put(slot, option);
		}
		dialog.delay = this.delay;
		return dialog;
	}

	public void downPos(int optionId) {
		if (!this.options.containsKey(optionId) || optionId < 0 || optionId >= this.options.size() - 1) {
			return;
		}
		Map<Integer, DialogOption> newOptions = new TreeMap<>();
		for (int id : this.options.keySet()) {
			DialogOption option = this.options.get(id);
			if (id == optionId) {
				option.slot = id + 1;
				newOptions.put(id + 1, this.options.get(id));
				continue;
			} else if (id == optionId + 1) {
				option.slot = id - 1;
				newOptions.put(id - 1, this.options.get(id));
				continue;
			}
			newOptions.put(id, this.options.get(id));
		}
		this.options.clear();
		this.options.putAll(newOptions);
	}

	@Override
	public IAvailability getAvailability() {
		return this.availability;
	}

	@Override
	public IDialogCategory getCategory() {
		return this.category;
	}

	@Override
	public String getCommand() {
		return this.command;
	}

	@Override
	public int getId() {
		return this.id;
	}

	public String getKey() {
		char c = ((char) 167);
		return c + "7ID:" + this.id + c + "8 " + this.category.title + "/" + c + "r" + this.title;
	}

	@Override
	public String getName() {
		return this.title;
	}

	@Override
	public IDialogOption getOption(int slot) {
		IDialogOption option = this.options.get(slot);
		if (option == null) {
			throw new CustomNPCsException("There is no DialogOption for slot: " + slot);
		}
		return option;
	}

	@Override
	public IDialogOption[] getOptions() {
		return this.options.values().toArray(new IDialogOption[0]);
	}

	@Override
	public Quest getQuest() {
		if (QuestController.instance == null) {
			return null;
		}
		return QuestController.instance.quests.get(this.quest);
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public int getVersion() {
		return this.version;
	}

	public boolean hasDialogs(EntityPlayer player) {
		for (DialogOption option : this.options.values()) {
			if (option != null && option.optionType == OptionType.DIALOG_OPTION && option.hasDialogs()
					&& option.isAvailable(player)) {
				return true;
			}
		}
		return false;
	}

	public boolean notHasOtherOptions() {
		for (DialogOption option : this.options.values()) {
			if (option != null && option.optionType != OptionType.DISABLED) {
				return false;
			}
		}
		return true;
	}

	public boolean hasQuest() {
		return this.getQuest() != null;
	}

	public void readNBT(NBTTagCompound compound) {
		this.id = compound.getInteger("DialogId");
		this.readNBTPartial(compound);
	}

	public void readNBTPartial(NBTTagCompound compound) {
		this.version = compound.getInteger("ModRev");
		VersionCompatibility.CheckAvailabilityCompatibility(this, compound);
		this.title = compound.getString("DialogTitle");
		this.text = compound.getString("DialogText");
		this.quest = compound.getInteger("DialogQuest");
		this.sound = compound.getString("DialogSound");
		this.texture = compound.getString("DialogTexture");
		this.command = compound.getString("DialogCommand");
		this.mail.readNBT(compound.getCompoundTag("DialogMail"));
		this.hideNPC = compound.getBoolean("DialogHideNPC");
		this.showWheel = compound.getBoolean("DialogShowWheel");
		if (compound.hasKey("DialogShowFits", 1)) {
			this.showFits = compound.getBoolean("DialogShowFits");
		}
		this.disableEsc = compound.getBoolean("DialogDisableEsc");
		if (compound.hasKey("DialogStopSound", 1)) {
			this.stopSound = compound.getBoolean("DialogStopSound");
		}
		NBTTagList options = compound.getTagList("Options", 10);
		Map<Integer, DialogOption> newoptions = new HashMap<>();
		for (int i = 0; i < options.tagCount(); ++i) {
			NBTTagCompound option = options.getCompoundTagAt(i);
			int opslot = option.getInteger("OptionSlot");
			DialogOption dia = new DialogOption();
			dia.readNBT(option.getCompoundTag("Option"));
			newoptions.put(opslot, dia);
			dia.slot = opslot;
		}
		this.options.clear();
		this.options.putAll(newoptions);
		this.availability.readFromNBT(compound);
		this.factionOptions.readFromNBT(compound);
		this.delay = compound.getInteger("ResponseDelay");
		if (this.delay < 0) {
			this.delay = 0;
		} else if (this.delay > 1200) {
			this.delay = 1200;
		}
	}

	@Override
	public void save() {
		DialogController.instance.saveDialog(this.category, this);
	}

	@Override
	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public void setName(String name) {
		this.title = name;
	}

	@Override
	public void setQuest(IQuest quest) {
		if (quest == null) {
			this.quest = -1;
		} else {
			if (quest.getId() < 0) {
				throw new CustomNPCsException("Quest id is lower than 0");
			}
			this.quest = quest.getId();
		}
	}

	@Override
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void setVersion(int version) {
		this.version = version;
	}

	public void upPos(int optionId) {
		if (!this.options.containsKey(optionId) || optionId <= 0) {
			return;
		}
		Map<Integer, DialogOption> newOptions = new TreeMap<>();
		for (int id : this.options.keySet()) {
			DialogOption option = this.options.get(id);
			if (id == optionId - 1) {
				option.slot = id + 1;
				newOptions.put(id + 1, option);
				continue;
			} else if (id == optionId) {
				option.slot = id - 1;
				newOptions.put(id - 1, option);
				continue;
			}
			newOptions.put(id, option);
		}
		this.options.clear();
		this.options.putAll(newOptions);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("DialogId", this.id);
		return this.writeToNBTPartial(compound);
	}

	public NBTTagCompound writeToNBTPartial(NBTTagCompound compound) {
		compound.setString("DialogTitle", this.title);
		compound.setString("DialogText", this.text);
		compound.setInteger("DialogQuest", this.quest);
		compound.setString("DialogCommand", this.command);
		compound.setTag("DialogMail", this.mail.writeNBT());
		compound.setBoolean("DialogHideNPC", this.hideNPC);
		compound.setBoolean("DialogShowWheel", this.showWheel);
		compound.setBoolean("DialogShowFits", this.showFits);
		compound.setBoolean("DialogDisableEsc", this.disableEsc);
		compound.setBoolean("DialogStopSound", this.stopSound);
		if (this.sound != null && !this.sound.isEmpty()) {
			compound.setString("DialogSound", this.sound);
		}
		if (this.texture != null && !this.texture.isEmpty()) {
			compound.setString("DialogTexture", this.texture);
		}
		NBTTagList options = new NBTTagList();
		for (int opslot : this.options.keySet()) {
			NBTTagCompound listcompound = new NBTTagCompound();
			listcompound.setInteger("OptionSlot", opslot);
			listcompound.setTag("Option", this.options.get(opslot).writeNBT());
			options.appendTag(listcompound);
		}
		compound.setTag("Options", options);
		this.availability.writeToNBT(compound);
		this.factionOptions.writeToNBT(compound);
		compound.setInteger("ModRev", this.version);
		compound.setInteger("ResponseDelay", this.delay);
		return compound;
	}

}
