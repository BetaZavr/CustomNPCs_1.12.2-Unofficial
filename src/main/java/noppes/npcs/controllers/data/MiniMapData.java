package noppes.npcs.controllers.data;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.api.IPos;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.data.IMiniMapData;

public class MiniMapData implements IMiniMapData {

	private int questId = -1;
	private int taskId = -1;
	private boolean update = false;

	public int id = 0;
	public int color;
	public int[] dimIDs = new int[] { 0 };
	public String name = "default map point";
	public String type = "Normal";
	public String icon = "icon.png";
	public IPos pos = NpcAPI.Instance().getIPos(BlockPos.ORIGIN);
	public boolean isEnable = true;
	public Map<String, String> gsonData = Maps.<String, String>newTreeMap();

	public MiniMapData() {
		this.color = (int) ((double) 0xFF000000 + Math.random() * (double) 0xFFFFFF);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MiniMapData)) {
			return false;
		}
		MiniMapData mmd = (MiniMapData) obj;
		if (this.dimIDs.length == mmd.dimIDs.length) {
			return false;
		}
		for (int i = 0; i < this.dimIDs.length; i++) {
			if (this.dimIDs[i] != mmd.dimIDs[i]) {
				return false;
			}
		}
		return this.id == mmd.id && this.color == mmd.color && this.name.equals(name) && this.type.equals(type)
				&& this.icon.equals(icon) && this.pos.getMCBlockPos().equals(mmd.pos.getMCBlockPos())
				&& this.isEnable == mmd.isEnable;
	}

	@Override
	public int getColor() {
		return color;
	}

	@Override
	public int[] getDimentions() {
		return this.dimIDs;
	}

	@Override
	public String getIcon() {
		return type;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IPos getPos() {
		return pos;
	}

	@Override
	public String[] getSpecificKeys() {
		return gsonData.keySet().toArray(new String[gsonData.size()]);
	}

	@Override
	public String getSpecificValue(String key) {
		return gsonData.get(key);
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public boolean isEnable() {
		return isEnable;
	}

	public boolean isQuestTask(int questId, int taskId) {
		return this.questId == questId && this.taskId == taskId;
	}

	public boolean isUpdate() {
		if (update) {
			update = false;
			return true;
		}
		return false;
	}

	public void load(NBTTagCompound compound) {
		isEnable = compound.getBoolean("IsEnable");
		questId = compound.getInteger("QuestID");
		taskId = compound.getInteger("TaskID");
		dimIDs = compound.getIntArray("DimentionID");
		color = compound.getInteger("Color");
		id = compound.getInteger("ID");
		type = compound.getString("Type");
		name = compound.getString("Name");
		icon = compound.getString("Icon");
		pos = NpcAPI.Instance().getIPos(BlockPos.fromLong(compound.getLong("Pos")));

		gsonData.clear();
		for (int i = 0; i < compound.getTagList("GsonData", 10).tagCount(); i++) {
			NBTTagCompound gsonNBT = compound.getTagList("GsonData", 10).getCompoundTagAt(i);
			gsonData.put(gsonNBT.getString("K"), gsonNBT.getString("V"));
		}

		update = false;
	}

	public NBTTagCompound save() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("IsEnable", isEnable);
		compound.setInteger("QuestID", questId);
		compound.setInteger("TaskID", taskId);
		compound.setIntArray("DimentionID", dimIDs);
		compound.setInteger("Color", color);
		compound.setInteger("ID", id);
		compound.setString("Type", type);
		compound.setString("Name", name);
		compound.setString("Icon", icon);
		compound.setLong("Pos", pos.getMCBlockPos().toLong());

		NBTTagList gsonList = new NBTTagList();
		for (String key : gsonData.keySet()) {
			NBTTagCompound gsonNBT = new NBTTagCompound();
			gsonNBT.setString("K", key);
			gsonNBT.setString("V", gsonData.get(key));
			gsonList.appendTag(gsonNBT);
		}
		compound.setTag("GsonData", gsonList);

		return compound;
	}

	@Override
	public void setColor(int newColor) {
		if (color == newColor) {
			return;
		}
		color = newColor;
		update = true;
	}

	@Override
	public void setDimentions(int[] dims) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIcon(String newIcon) {
		if (newIcon == null) {
			newIcon = "";
		}
		if (icon.equals(newIcon)) {
			return;
		}
		icon = newIcon;
		update = true;
	}

	@Override
	public void setName(String newName) {
		if (newName == null) {
			newName = "";
		}
		if (name.equals(newName)) {
			return;
		}
		name = newName;
		update = true;
	}

	@Override
	public void setPos(int x, int y, int z) {
		BlockPos newPos = new BlockPos(x, y, z);
		if (pos.getMCBlockPos().equals(newPos)) {
			return;
		}
		pos = NpcAPI.Instance().getIPos(newPos);
		update = true;
	}

	@Override
	public void setPos(IPos newPos) {
		if (newPos == null) {
			newPos = NpcAPI.Instance().getIPos(BlockPos.ORIGIN);
		}
		if (pos.getMCBlockPos().equals(newPos.getMCBlockPos())) {
			return;
		}
		pos = NpcAPI.Instance().getIPos(newPos.getMCBlockPos());
		update = true;
	}

	public void setQuest(MiniMapData parent) {
		this.questId = parent.questId;
		this.taskId = parent.taskId;
	}

	@Override
	public void setType(String newType) {
		if (newType == null) {
			newType = "";
		}
		if (type.equals(newType)) {
			return;
		}
		type = newType;
		update = true;
	}

	@Override
	public String toString() {
		String gs = "empty";
		if (!gsonData.isEmpty()) {
			gs = "";
			for (String k : gsonData.keySet()) {
				if (!gs.isEmpty()) {
					gs += ", ";
				}
				gs += "(" + k + "=" + gsonData.get(k) + ")";
			}
			gs = "[" + gs + "]";
		}
		String ds = "";
		for (int id : this.dimIDs) {
			if (!ds.isEmpty()) {
				ds += ", ";
			}
			ds += id;
		}
		ds = "[" + ds + "]";
		return "Point Data: {ID: " + this.id + ", Name: " + this.name + ", Type: " + this.type + ", Icon: " + this.icon
				+ ", Color: " + this.color + ", Pos: " + this.pos.getMCBlockPos() + ", DimensionIDs: " + ds
				+ ", IsEnable: " + this.isEnable + ", GsonData: " + gs + "}";
	}

}
