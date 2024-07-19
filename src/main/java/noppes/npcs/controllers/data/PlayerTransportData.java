package noppes.npcs.controllers.data;

import java.util.HashSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class PlayerTransportData {

	public HashSet<Integer> transports;

	public PlayerTransportData() {
		this.transports = new HashSet<>();
	}

	public void loadNBTData(NBTTagCompound compound) {
		HashSet<Integer> dialogsRead = new HashSet<>();
		if (compound == null) {
			return;
		}
		NBTTagList list = compound.getTagList("TransportData", 10);
        for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
			dialogsRead.add(nbttagcompound.getInteger("Transport"));
		}
		this.transports = dialogsRead;
	}

	public void saveNBTData(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for (int dia : this.transports) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("Transport", dia);
			list.appendTag(nbttagcompound);
		}
		compound.setTag("TransportData", list);
	}
}
