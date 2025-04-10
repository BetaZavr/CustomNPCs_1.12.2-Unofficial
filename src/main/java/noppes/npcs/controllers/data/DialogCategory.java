package noppes.npcs.controllers.data;

import java.util.TreeMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IDialogCategory;

public class DialogCategory implements IDialogCategory {

	public final TreeMap<Integer, Dialog> dialogs = new TreeMap<>();
	public int id = -1;
	public String title = "";

	public DialogCategory copy() {
		DialogCategory newCat = new DialogCategory();
		newCat.readNBT(this.writeNBT(new NBTTagCompound()));
		return null;
	}

	@Override
	public IDialog create() {
		return new Dialog(this);
	}

	@Override
	public IDialog[] dialogs() {
		return this.dialogs.values().toArray(new IDialog[0]);
	}

	@Override
	public String getName() {
		return this.title;
	}

	public void readNBT(NBTTagCompound compound) {
		this.id = compound.getInteger("Slot");
		this.title = compound.getString("Title");
		NBTTagList dialogsList = compound.getTagList("Dialogs", 10);
        for (int ii = 0; ii < dialogsList.tagCount(); ++ii) {
            Dialog dialog = new Dialog(this);
            NBTTagCompound comp = dialogsList.getCompoundTagAt(ii);
            dialog.readNBT(comp);
            dialog.id = comp.getInteger("DialogId");
            this.dialogs.put(dialog.id, dialog);
        }
    }

	public NBTTagCompound writeNBT(NBTTagCompound compound) {
		compound.setInteger("Slot", this.id);
		compound.setString("Title", this.title);
		NBTTagList dialogs = new NBTTagList();
		for (Dialog dialog : this.dialogs.values()) {
			dialogs.appendTag(dialog.writeToNBT(new NBTTagCompound()));
		}
		compound.setTag("Dialogs", dialogs);
		return compound;
	}
}
