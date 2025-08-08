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

	@Override
	public IDialog create() {
		return new Dialog(this);
	}

	@Override
	public IDialog[] dialogs() {
		return dialogs.values().toArray(new IDialog[0]);
	}

	@Override
	public String getName() {
		return title;
	}

	public void load(NBTTagCompound compound) {
		id = compound.getInteger("Slot");
		title = compound.getString("Title");
		NBTTagList dialogsList = compound.getTagList("Dialogs", 10);
        for (int i = 0; i < dialogsList.tagCount(); ++i) {
            Dialog dialog = new Dialog(this);
            NBTTagCompound comp = dialogsList.getCompoundTagAt(i);
            dialog.load(comp);
            dialog.id = comp.getInteger("DialogId");
            dialogs.put(dialog.id, dialog);
        }
    }

	public NBTTagCompound save(NBTTagCompound compound) {
		compound.setInteger("Slot", id);
		compound.setString("Title", title);
		NBTTagList list = new NBTTagList();
		for (Dialog dialog : dialogs.values()) { list.appendTag(dialog.save(new NBTTagCompound())); }
		compound.setTag("Dialogs", list);
		return compound;
	}

	// New from Unofficial (BetaZavr)
	public DialogCategory copy() {
		DialogCategory newCat = new DialogCategory();
		newCat.load(save(new NBTTagCompound()));
		return newCat;
	}

}
