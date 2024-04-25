package noppes.npcs.controllers.data;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.Server;
import noppes.npcs.api.entity.data.IMiniMapData;
import noppes.npcs.api.entity.data.IPlayerMiniMap;
import noppes.npcs.constants.EnumPacketClient;

public class PlayerMiniMapData implements IPlayerMiniMap {

	public final Map<Integer, List<MiniMapData>> points = Maps.<Integer, List<MiniMapData>>newTreeMap();
	public String modName = "non";
	private boolean update;
	public Map<String, Object> addData = Maps.<String, Object>newHashMap();

	@Override
	public IMiniMapData addPoint(int dimentionId) {
		if (modName.equals("non")) {
			return new MiniMapData();
		}
		if (!points.containsKey(dimentionId)) {
			points.put(dimentionId, Lists.<MiniMapData>newArrayList());
		}
		MiniMapData mmd = new MiniMapData();
		mmd.dimIDs = new int[] { dimentionId };
		mmd.id = points.get(dimentionId).size();
		if (modName.equals("voxelmap")) {
			mmd.icon = "";
		}
		points.get(dimentionId).add(mmd);
		update = true;
		return mmd;
	}

	@Override
	public IMiniMapData[] getAllPoints() {
		List<IMiniMapData> list = Lists.<IMiniMapData>newArrayList();
		for (List<MiniMapData> l : points.values()) {
			list.addAll(l);
		}
		return list.toArray(new IMiniMapData[list.size()]);
	}

	@Override
	public String getModName() {
		return modName;
	}

	private NBTTagCompound getNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("ModName", modName);
		NBTTagList dim = new NBTTagList();
		for (int dimID : points.keySet()) {
			NBTTagCompound nbtList = new NBTTagCompound();
			nbtList.setInteger("DimentionID", dimID);
			NBTTagList pList = new NBTTagList();
			for (MiniMapData mmd : points.get(dimID)) {
				pList.appendTag(mmd.save());
			}
			nbtList.setTag("Points", pList);
			dim.appendTag(nbtList);
		}
		nbt.setTag("Data", dim);
		return nbt;
	}

	@Override
	public IMiniMapData getPoint(int dimentionId, int id) {
		if (!points.containsKey(dimentionId)) {
			return null;
		}
		return points.get(dimentionId).get(id);
	}

	@Override
	public IMiniMapData getPoint(int dimentionId, String name) {
		if (!points.containsKey(dimentionId)) {
			return null;
		}
		for (MiniMapData mmd : points.get(dimentionId)) {
			if (mmd.name.equals(name)) {
				return mmd;
			}
		}
		return null;
	}

	@Override
	public IMiniMapData[] getPoints(int dimentionId) {
		if (!points.containsKey(dimentionId)) {
			return new IMiniMapData[0];
		}
		List<MiniMapData> list = points.get(dimentionId);
		return list.toArray(new IMiniMapData[list.size()]);
	}

	public MiniMapData getQuestTask(int questId, int taskId, String name, int dimID) {
		for (List<MiniMapData> list : points.values()) {
			for (MiniMapData mmd : list) {
				if (mmd.isQuestTask(questId, taskId)) {
					return mmd;
				}
				if (mmd.name.equals(name)) {
					for (int id : mmd.dimIDs) {
						if (id == dimID) {
							return mmd;
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public String[] getSpecificKeys() {
		return addData.keySet().toArray(new String[addData.size()]);
	}

	@Override
	public Object getSpecificValue(String key) {
		return addData.get(key);
	}

	public void loadNBTData(NBTTagCompound compound) {
		if (!compound.hasKey("MiniMapData")) {
			return;
		}
		NBTTagCompound nbt = compound.getCompoundTag("MiniMapData");
		modName = nbt.getString("ModName");

		points.clear();
		NBTTagList list = nbt.getTagList("Data", 10);
		if (list.tagCount() != 0) {
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound nbtList = list.getCompoundTagAt(i);
				List<MiniMapData> pList = Lists.<MiniMapData>newArrayList();
				for (int j = 0; j < nbtList.getTagList("Points", 10).tagCount(); j++) {
					MiniMapData mmd = new MiniMapData();
					mmd.load(nbtList.getTagList("Points", 10).getCompoundTagAt(j));
					pList.add(mmd);
				}
				points.put(nbtList.getInteger("DimentionID"), pList);
			}
		}
	}

	@Override
	public boolean removePoint(int dimentionId, int id) {
		IMiniMapData mmd = getPoint(dimentionId, id);
		if (mmd == null) {
			return false;
		}
		points.get(dimentionId).remove(mmd);
		return true;
	}

	@Override
	public boolean removePoint(int dimentionId, String name) {
		IMiniMapData mmd = getPoint(dimentionId, name);
		if (mmd == null) {
			return false;
		}
		points.get(dimentionId).remove(mmd);
		return true;
	}

	public NBTTagCompound saveNBTData(NBTTagCompound compound) {
		compound.setTag("MiniMapData", this.getNBT());
		return compound;
	}

	public void update(EntityPlayerMP player) {
		boolean needSend = false;
		for (List<MiniMapData> list : points.values()) {
			for (MiniMapData mmd : list) {
				needSend = mmd.isUpdate();
				if (needSend) {
					break;
				}
			}
			if (needSend) {
				break;
			}
		}
		if (needSend || this.update) {
			Server.sendData(player, EnumPacketClient.MINIMAP_DATA, this.saveNBTData(new NBTTagCompound()));
		}
		this.update = false;
	}

}
