package noppes.npcs.controllers.data;

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class TransportCategory {

	public int id = -1;
	public Map<Integer, TransportLocation> locations = new TreeMap<>();
	public String title = "";

	public Vector<TransportLocation> getDefaultLocations() {
		Vector<TransportLocation> list = new Vector<>();
		for (TransportLocation loc : this.locations.values()) {
			if (loc.isDefault()) {
				list.add(loc);
			}
		}
		return list;
	}

	public void readNBT(NBTTagCompound compound) {
		this.id = compound.getInteger("CategoryId");
		this.title = compound.getString("CategoryTitle");
		if (this.title.isEmpty()) {
			this.title = "Default";
		}
		NBTTagList locs = compound.getTagList("CategoryLocations", 10);
		if (locs.tagCount() == 0) {
			return;
		}
		for (int ii = 0; ii < locs.tagCount(); ++ii) {
			TransportLocation location = new TransportLocation();
			location.readNBT(locs.getCompoundTagAt(ii));
			location.category = this;
			this.locations.put(location.id, location);
		}
	}

	public void writeNBT(NBTTagCompound compound) {
		compound.setInteger("CategoryId", this.id);
		compound.setString("CategoryTitle", this.title);
		NBTTagList locs = new NBTTagList();
		for (TransportLocation location : this.locations.values()) {
			locs.appendTag(location.writeNBT());
		}
		compound.setTag("CategoryLocations", locs);
	}
}
