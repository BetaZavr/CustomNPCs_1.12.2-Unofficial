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
			dialogId = id;
			availability = new Availability();
		}

		public OptionDialogID(NBTTagCompound compound) {
			dialogId = compound.getInteger("DialogId");
			availability = new Availability();
			availability.load(compound);
		}

		public NBTTagCompound getNBT() {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setInteger("DialogId", dialogId);
			availability.save(compound);
			return compound;
		}

		public String toString() {
			return "OptionDialogID: " + dialogId + "; " + availability.toString();
		}

	}

	public String command = "";
	public int optionColor = 0xE0E0E0;
	public int slot = -1;
	public int iconId = 0;
	public OptionType optionType = OptionType.DIALOG_OPTION;
	public String title = "Talk";
	public final List<OptionDialogID> dialogs = new ArrayList<>();

	public Dialog getDialog(EntityPlayer player) {
		if (!hasDialogs() || player == null) {
			return null;
		}
		DialogController dData = DialogController.instance;
		for (OptionDialogID od : dialogs) {
			if (!dData.hasDialog(od.dialogId)) {
				continue;
			}
			if (od.availability.isAvailable(player)) {
				return dData.get(od.dialogId);
			}
		}
		return null;
	}

	@Override
	public String getName() {
		return title;
	}

	@Override
	public int getSlot() {
		return slot;
	}

	@Override
	public int getType() {
		return optionType.get();
	}

	public boolean hasDialogs() {
        return !dialogs.isEmpty() && optionType == OptionType.DIALOG_OPTION;
    }

	public boolean isAvailable(EntityPlayer player) {
		if (optionType == OptionType.DISABLED) {
			return false;
		}
		if (optionType != OptionType.DIALOG_OPTION) {
			return true;
		}
		Dialog dialog = getDialog(player);
		return dialog != null && dialog.availability.isAvailable(player);
	}

	public void load(NBTTagCompound compound) {
		if (compound != null) {
			title = compound.getString("Title");
			optionColor = compound.getInteger("DialogColor");
			iconId = compound.getInteger("IconId");
			optionType = OptionType.get(compound.getInteger("OptionType"));
			command = compound.getString("DialogCommand");
			if (optionColor == 0) {
				optionColor = 14737632;
			}
			dialogs.clear();
			if (compound.hasKey("Dialog", 3)) { // OLD
				dialogs.add(new OptionDialogID(compound.getInteger("Dialog")));
			} else if (compound.hasKey("Dialogs", 9)) {
				for (int i = 0; i < compound.getTagList("Dialogs", 10).tagCount(); i++) {
					dialogs.add(new OptionDialogID(compound.getTagList("Dialogs", 10).getCompoundTagAt(i)));
				}
			}
		}
	}

	public NBTTagCompound save() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("Title", title);
		compound.setInteger("OptionType", optionType.get());
		compound.setInteger("DialogColor", optionColor);
		compound.setInteger("IconId", iconId);
		compound.setString("DialogCommand", command);

		NBTTagList list = new NBTTagList();
		for (OptionDialogID od : dialogs) {
			list.appendTag(od.getNBT());
		}
		compound.setTag("Dialogs", list);
		return compound;
	}

	// New from BetaZavr
	public void replaceDialogIDs(int oldId, int newId) {
		List<OptionDialogID> newDialogs = new ArrayList<>();
		boolean added = false;
		for (OptionDialogID od : dialogs) {
			if (od.dialogId == oldId) {
				od.dialogId = newId;
				added = true;
			}
			newDialogs.add(od);
		}
		if (added) {
			dialogs.clear();
			dialogs.addAll(newDialogs);
		}
	}

	public void upPos(int dialogId) {
		List<OptionDialogID> newDialogs = new ArrayList<>();
		boolean added = false;
		OptionDialogID old = null;
		for (OptionDialogID od : dialogs) {
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
			dialogs.clear();
			dialogs.addAll(newDialogs);
		}
	}

	public void addDialog(int dialogId) {
		OptionDialogID od = new OptionDialogID(dialogId);
		dialogs.add(od);
	}

	public DialogOption copy() {
		DialogOption newDO = new DialogOption();
		newDO.load(save());
		return newDO;
	}

	public void downPos(int dialogId) {
		List<OptionDialogID> newDialogs = new ArrayList<>();
		boolean added = false;
		OptionDialogID found = null;
		for (OptionDialogID od : dialogs) {
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
			dialogs.clear();
			dialogs.addAll(newDialogs);
		}
	}

}
