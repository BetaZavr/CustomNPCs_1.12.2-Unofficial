package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.Server;
import noppes.npcs.api.entity.data.IMiniMapData;
import noppes.npcs.api.entity.data.IPlayerMiniMap;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.util.Util;

public class PlayerMiniMapData
implements IPlayerMiniMap {

	public final List<MiniMapData> points = new ArrayList<>();
	public String modName = "non";
	private boolean update;
	public Map<String, Object> addData = new HashMap<>();

	@Override
	public IMiniMapData addPoint(int dimensionId) {
		if (modName.equals("non")) {
			return new MiniMapData();
		}
		MiniMapData mmd = new MiniMapData();
		mmd.dimIDs = new int[] { dimensionId };
		mmd.id = points.size();
		if (modName.equals("voxelmap")) { mmd.icon = ""; }
		points.add(mmd);
		update = true;
		return mmd;
	}

	@Override
	public IMiniMapData[] getAllPoints() { return points.toArray(new IMiniMapData[0]); }

	@Override
	public String getModName() { return modName; }

	private NBTTagCompound getNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("ModName", modName);
		NBTTagList pList = new NBTTagList();
		for (MiniMapData mmd : points) { pList.appendTag(mmd.save()); }
		nbt.setTag("Data", pList);
		return nbt;
	}

	@Override
	public IMiniMapData getPoint(int id) {
		if (id < 0 || id >= points.size()) { return null; }
		MiniMapData mmd = points.get(id);
		if (mmd != null && mmd.id == id) { return mmd; }
		for (MiniMapData mmdc : points) {
			if (mmdc.id == id) { return mmdc; }
		}
		return null;
	}

	@Override
	public IMiniMapData getPoint(String name) {
		for (MiniMapData mmd : points) {
			if (mmd.name.equals(name)) { return mmd; }
		}
		return null;
	}

	@Override
	public IMiniMapData[] getPoints(int dimensionId) {
		List<MiniMapData> list = new ArrayList<>();
		for (MiniMapData mmd : points) {
			for (int id : mmd.dimIDs) {
				if (id == dimensionId) {
					list.add(mmd);
					break;
				}
			}
		}
		return list.toArray(new IMiniMapData[0]);
	}

	public MiniMapData getQuestTask(int questId, int taskId, String questName, int dimID) {
		questName = Util.instance.deleteColor(questName);
		for (MiniMapData mmd : points) {
			if (mmd.isQuestTask(questId, taskId)) { return mmd; }
			if (mmd.name.equals(questName)) {
				for (int id : mmd.dimIDs) {
					if (id == dimID) { return mmd; }
				}
			}
		}
		return null;
	}

	@Override
	public String[] getSpecificKeys() {
		return addData.keySet().toArray(new String[0]);
	}

	@Override
	public Object getSpecificValue(String key) {
		return addData.get(key);
	}

	public void loadNBTData(NBTTagCompound compound) {
		if (!compound.hasKey("MiniMapData")) { return; }
		NBTTagCompound nbt = compound.getCompoundTag("MiniMapData");
		modName = nbt.getString("ModName");
		points.clear();
		NBTTagList list = nbt.getTagList("Data", 10);
		if (list.tagCount() != 0) {
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound nbtList = list.getCompoundTagAt(i);
				if (nbtList.hasKey("Points", 9) || nbtList.hasKey("DimensionID", 3)) { // OLD
					for (int j = 0; j < nbtList.getTagList("Points", 10).tagCount(); j++) {
						MiniMapData mmd = new MiniMapData();
						mmd.load(nbtList.getTagList("Points", 10).getCompoundTagAt(j));
						points.add(mmd);
					}
				} else {
					MiniMapData mmd = new MiniMapData();
					mmd.load(nbtList);
					points.add(mmd);
				}
			}
		}
	}
	
	public void removeQuestPoints(int questId) {
		boolean remove = false;
		List<MiniMapData> tempList = new ArrayList<>(points);
		for (MiniMapData mmd : tempList) {
			if (mmd.isQuestTask(questId, -1)) {
				points.remove(mmd);
				remove = true;
			}
		}
		if (remove) { update = true; }
	}
	
	@Override
	public boolean removePoint(int id) {
		if (id < 0 || id >= points.size()) { return false; }
		MiniMapData mmd = points.get(id);
		boolean remove = false;
		if (mmd != null && mmd.id == id) { remove = points.remove(mmd); }
		if (!remove) {
			for (MiniMapData mmds : points) {
				if (mmds.id == id) { remove = points.remove(mmds); }
				if (remove) { break; }
			}
		}
		if (remove) { update = true; }
		return true;
	}

	@Override
	public boolean removePoint(String name) {
		name = Util.instance.deleteColor(name);
		for (MiniMapData mmd : points) {
			if (mmd.name.equals(name) && points.remove(mmd)) {
				update = true;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean removePoints(int dimensionId) {
		boolean remove = false;
		List<MiniMapData> tempList = new ArrayList<>(points);
		for (MiniMapData mmd : tempList) {
			for (int id : mmd.dimIDs) {
				if (id == dimensionId && points.remove(mmd)) {
					remove = true;
					break;
				}
			}
		}
		if (remove) { update = true; }
		return remove;
	}

	public NBTTagCompound saveNBTData(NBTTagCompound compound) {
		compound.setTag("MiniMapData", this.getNBT());
		return compound;
	}

	public void update(EntityPlayerMP player) {
		boolean needSend = this.update;
		if (!needSend) {
			for (MiniMapData mmd : points) {
				if (mmd.isUpdate()) {
					needSend = true;
					break;
				}
			}
		}
		if (needSend) {
			this.update = false;
			Server.sendData(player, EnumPacketClient.MINIMAP_DATA, this.saveNBTData(new NBTTagCompound()));
		}
	}

	public MiniMapData get(MiniMapData mmd) {
		for (MiniMapData mmp : points) {
			boolean equalDimIDs = mmp.dimIDs.length == mmd.dimIDs.length;
			if (equalDimIDs) {
				int eq = 0;
				for (int idp : mmp.dimIDs) {
					for (int idd : mmd.dimIDs) {
						if (idp == idd) { eq ++; break; }
					}
				}
				equalDimIDs = mmp.dimIDs.length == eq;
			}
			if (equalDimIDs && mmp.name.equals(mmd.name) && mmp.type.equals(mmd.type) && mmp.pos.getMCBlockPos().equals(mmd.pos.getMCBlockPos())) {
				return mmp;
			}
		}
		return null;
	}

}
