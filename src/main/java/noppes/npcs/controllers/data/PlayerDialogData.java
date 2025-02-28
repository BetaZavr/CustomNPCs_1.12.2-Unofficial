package noppes.npcs.controllers.data;

import java.util.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class PlayerDialogData {

	public final Map<Integer, Set<Integer>> dialogsRead = new TreeMap<>();

	public void loadNBTData(NBTTagCompound compound) {
		if (compound == null) {
			return;
		}
		dialogsRead.clear();
		NBTTagList dialogs = compound.getTagList("DialogData", 10);
        for (int i = 0; i < dialogs.tagCount(); ++i) {
			NBTTagCompound nbtDialog = dialogs.getCompoundTagAt(i);
			Set<Integer> set = new TreeSet<>();
			for (int id : nbtDialog.getIntArray("OptionRead")) { set.add(id); }
			dialogsRead.put(nbtDialog.getInteger("Dialog"), set);
		}
	}

	public void saveNBTData(NBTTagCompound compound) {
		NBTTagList dialogs = new NBTTagList();
		for (int dialogId : dialogsRead.keySet()) {
			NBTTagCompound nbtDialog = new NBTTagCompound();
			nbtDialog.setInteger("Dialog", dialogId);
			int[] set = new int[dialogsRead.get(dialogId).size()];
			int i = 0;
			for (int id :dialogsRead.get(dialogId)) { set[i++] = id; }
			nbtDialog.setIntArray("OptionRead", set);
			dialogs.appendTag(nbtDialog);
		}
		compound.setTag("DialogData", dialogs);
	}

	public boolean has(int dialogId) { return dialogsRead.containsKey(dialogId); }

	public void read(int dialogId) {
		if (has(dialogId)) { return; }
		dialogsRead.put(dialogId, new TreeSet<>());
	}

	public void option(int dialogId, int optionId) {
		if (dialogsRead.containsKey(dialogId)) { dialogsRead.put(dialogId, new TreeSet<>());; }
		dialogsRead.get(dialogId).add(optionId);
	}


}
