package noppes.npcs.controllers.data;

import java.util.HashSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class PlayerDialogData {
	public HashSet<Integer> dialogsRead;

	public PlayerDialogData() {
		this.dialogsRead = new HashSet<>();
	}

	public void loadNBTData(NBTTagCompound compound) {
		HashSet<Integer> dialogsRead = new HashSet<>();
		if (compound == null) {
			return;
		}
		NBTTagList list = compound.getTagList("DialogData", 10);
        for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
			dialogsRead.add(nbttagcompound.getInteger("Dialog"));
		}
		this.dialogsRead = dialogsRead;
	}

	public void saveNBTData(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for (int dia : this.dialogsRead) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("Dialog", dia);
			list.appendTag(nbttagcompound);
		}
		compound.setTag("DialogData", list);
	}
}
