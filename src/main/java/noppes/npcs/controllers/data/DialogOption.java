package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.constants.OptionType;
import noppes.npcs.api.handler.data.IDialogOption;
import noppes.npcs.controllers.DialogController;

public class DialogOption implements IDialogOption {

	public static class OptionDialogID {

		public int dialogId;
		public Availability availability;

		public OptionDialogID(int id) {
			this.dialogId = id;
			this.availability = new Availability();
		}

		public OptionDialogID(NBTTagCompound compound) {
			this.dialogId = compound.getInteger("DialogId");
			this.availability = new Availability();
			this.availability.readFromNBT(compound);
		}

		public NBTTagCompound getNBT() {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setInteger("DialogId", this.dialogId);
			this.availability.writeToNBT(compound);
			return compound;
		}

		public String toString() {
			return "OptionDialogID: " + this.dialogId + "; " + this.availability.toString();
		}

	}

	public String command;
	public int optionColor, slot, iconId;
	public OptionType optionType;
	public String title;
	public final List<OptionDialogID> dialogs;

	public DialogOption() {
		this.title = "Talk";
		this.optionType = OptionType.DIALOG_OPTION;
		this.optionColor = 14737632;
		this.command = "";
		this.slot = -1;
		this.iconId = 0;
		this.dialogs = new ArrayList<>();
	}

	public void addDialog(int dialogId) {
		OptionDialogID od = new OptionDialogID(dialogId);
		this.dialogs.add(od);
	}

	public DialogOption copy() {
		DialogOption newDO = new DialogOption();
		NBTTagCompound compound = this.writeNBT();
		newDO.readNBT(compound);
		return newDO;
	}

	public void downPos(int dialogId) {
		List<OptionDialogID> newDialogs = new ArrayList<>();
		boolean added = false;
		OptionDialogID found = null;
		for (OptionDialogID od : this.dialogs) {
			if (od.dialogId == dialogId && found == null) {
				found = od;
				continue;
			}
			newDialogs.add(od);
			if (found != null && !added) {
				newDialogs.add(found);
				added = true;
			}
		}
		if (found != null && !added) {
			newDialogs.add(found);
			added = true;
		}
		if (added) {
			this.dialogs.clear();
			this.dialogs.addAll(newDialogs);
		}
	}

	public Dialog getDialog(EntityPlayer player) {
		if (!this.hasDialogs() || player == null) {
			return null;
		}
		DialogController dData = DialogController.instance;
		for (OptionDialogID od : this.dialogs) {
			if (!dData.hasDialog(od.dialogId)) {
				continue;
			}
			if (od.availability.isAvailable(player)) {
				return (Dialog) dData.get(od.dialogId);
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return this.title;
	}

	@Override
	public int getSlot() {
		return this.slot;
	}

	@Override
	public int getType() {
		return this.optionType.get();
	}

	public boolean hasDialogs() {
        return !this.dialogs.isEmpty() && this.optionType == OptionType.DIALOG_OPTION;
    }

	public boolean isAvailable(EntityPlayer player) {
		if (this.optionType == OptionType.DISABLED) {
			return false;
		}
		if (this.optionType != OptionType.DIALOG_OPTION) {
			return true;
		}
		Dialog dialog = this.getDialog(player);
		return dialog != null && dialog.availability.isAvailable(player);
	}

	public void readNBT(NBTTagCompound compound) {
		if (compound == null) {
			return;
		}
		this.title = compound.getString("Title");
		this.optionColor = compound.getInteger("DialogColor");
		this.iconId = compound.getInteger("IconId");
		this.optionType = OptionType.get(compound.getInteger("OptionType"));
		this.command = compound.getString("DialogCommand");
		if (this.optionColor == 0) {
			this.optionColor = 14737632;
		}
		this.dialogs.clear();
		if (compound.hasKey("Dialog", 3)) { // OLD
			this.dialogs.add(new OptionDialogID(compound.getInteger("Dialog")));
		} else if (compound.hasKey("Dialogs", 9)) {
			for (int i = 0; i < compound.getTagList("Dialogs", 10).tagCount(); i++) {
				this.dialogs.add(new OptionDialogID(compound.getTagList("Dialogs", 10).getCompoundTagAt(i)));
			}
		}
	}

	public void replaceDialogIDs(int oldId, int newId) {
		List<OptionDialogID> newDialogs = new ArrayList<>();
		boolean added = false;
		for (OptionDialogID od : this.dialogs) {
			if (od.dialogId == oldId) {
				od.dialogId = newId;
				added = true;
			}
			newDialogs.add(od);
		}
		if (added) {
			this.dialogs.clear();
			this.dialogs.addAll(newDialogs);
		}
	}

	public void upPos(int dialogId) {
		List<OptionDialogID> newDialogs = new ArrayList<>();
		boolean added = false;
		OptionDialogID old = null;
		for (OptionDialogID od : this.dialogs) {
			if (od.dialogId == dialogId && old != null) {
				newDialogs.remove(old);
				newDialogs.add(od);
				newDialogs.add(old);
				added = true;
				continue;
			}
			old = od;
			newDialogs.add(od);
		}
		if (added) {
			this.dialogs.clear();
			this.dialogs.addAll(newDialogs);
		}
	}

	public NBTTagCompound writeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("Title", this.title);
		compound.setInteger("OptionType", this.optionType.get());
		compound.setInteger("DialogColor", this.optionColor);
		compound.setInteger("IconId", this.iconId);
		compound.setString("DialogCommand", this.command);
		NBTTagList list = new NBTTagList();
		for (OptionDialogID od : this.dialogs) {
			list.appendTag(od.getNBT());
		}
		compound.setTag("Dialogs", list);
		return compound;
	}

}
