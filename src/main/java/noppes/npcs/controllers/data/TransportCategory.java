package noppes.npcs.controllers.data;

import java.util.Map;
import java.util.Vector;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class TransportCategory {
	
	public int id;
	public Map<Integer, TransportLocation> locations;
	public String title;

	public TransportCategory() {
		this.id = -1;
		this.title = "";
		this.locations = Maps.<Integer, TransportLocation>newTreeMap();
	}

	public Vector<TransportLocation> getDefaultLocations() {
		Vector<TransportLocation> list = new Vector<TransportLocation>();
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
		if (this.title.isEmpty()) { this.title = "Default"; }
		NBTTagList locs = compound.getTagList("CategoryLocations", 10);
		if (locs == null || locs.tagCount() == 0) { return; }
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
