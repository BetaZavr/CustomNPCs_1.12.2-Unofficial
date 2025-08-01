package noppes.npcs.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.IPos;
import noppes.npcs.api.handler.IBorderHandler;
import noppes.npcs.api.handler.data.IBorder;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.Zone3D;

public class BorderController implements IBorderHandler {

	private static BorderController instance;
	public static BorderController getInstance() {
		if (BorderController.instance == null) {
			BorderController.instance = new BorderController();
		}
		return BorderController.instance;
	}
	public final Map<Integer, Zone3D> regions = new TreeMap<>();

	public BorderController() {
		BorderController.instance = this;
		load();
	}

	public Zone3D createNew(int dimensionID, BlockPos pos) {
		Zone3D reg = new Zone3D(getUnusedId(), dimensionID, pos.getX(), pos.getY(), pos.getZ());
		regions.put(reg.getId(), reg);
		return reg;
	}

	@Override
	public IBorder createNew(int dimensionID, IPos pos) {
		return createNew(dimensionID, pos.getMCBlockPos());
	}

	@Override
	public IBorder[] getAllRegions() {
		return regions.values().toArray(new IBorder[0]);
	}

	public NBTTagCompound getNBT() {
		NBTTagList list = new NBTTagList();
		for (Zone3D region : regions.values()) {
			NBTTagCompound nbtRegion = new NBTTagCompound();
			region.save(nbtRegion);
			list.appendTag(nbtRegion);
		}
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setTag("Data", list);
		return nbttagcompound;
	}

	public int getRegionID(int dimensionID, BlockPos pos) {
		for (Zone3D reg : regions.values()) {
			if (reg.contains(pos.getX(), pos.getY(), pos.getZ(), dimensionID)) {
				return reg.getId();
			}
		}
		return -1;
	}


	@Override
	public IBorder getRegion(int regionId) {
		return regions.get(regionId);
	}

	@Override
	public IBorder[] getRegions(int dimensionID) {
		List<IBorder> regs = new ArrayList<>();
		for (Zone3D reg : regions.values()) {
			if (reg.dimensionID == dimensionID) {
				regs.add(reg);
			}
		}
		return regs.toArray(new IBorder[0]);
	}

	public List<Zone3D> getRegionsInWorld(int dimensionID) {
		List<Zone3D> regs = new ArrayList<>();
		for (Zone3D reg : regions.values()) {
			if (reg.dimensionID == dimensionID) {
				regs.add(reg);
			}
		}
		return regs;
	}

	@Override
	public List<Zone3D> getNearestRegions(int dimensionID, double xPos, double yPos, double zPos, double distance) {
		AxisAlignedBB searchBox = new AxisAlignedBB(xPos - distance, 0.0d, zPos - distance, xPos + distance + 1.0d, 255.0d, zPos + distance + 1.0d);
		List<Zone3D> regionsIn = new ArrayList<>();
		for (Zone3D reg : regions.values()) {
			if (reg.dimensionID != dimensionID) { continue; }
			if (searchBox.intersects(reg.getAxisAlignedBB())) { regionsIn.add(reg); }
		}
		return regionsIn;
	}

	public int getUnusedId() {
		int id = 0;
		while (regions.containsKey(id)) { id++; }
		return id;
	}

	public Zone3D loadRegion(NBTTagCompound nbtRegion) {
		if (nbtRegion == null || !nbtRegion.hasKey("ID", 3) || nbtRegion.getInteger("ID") < 0) {
			return null;
		}
		int id = nbtRegion.getInteger("ID");
		if (regions.containsKey(id)) {
			regions.get(id).load(nbtRegion);
			return regions.get(id);
		}
		Zone3D region = new Zone3D();
		region.load(nbtRegion);
		regions.put(region.getId(), region);
		return regions.get(region.getId());
	}

	private void load() {
		CustomNpcs.debugData.start(null);
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if (saveDir == null) {
			CustomNpcs.debugData.end(null);
			return;
		}
		try {
			File file = new File(saveDir, "borders.dat");
			if (file.exists()) {
				loadRegions(file);
			}
		}
		catch (Exception e) {
			try {
				File file2 = new File(saveDir, "borders.dat_old");
				if (file2.exists()) {
					loadRegions(file2);
				}
			} catch (Exception ex) { LogWriter.error(ex); }
		}
		CustomNpcs.debugData.end(null);
	}

	private void loadRegions(File file) throws IOException {
		loadRegions(CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath())));
	}

	public void loadRegions(NBTTagCompound compound) {
		regions.clear();
		if (compound.hasKey("Data", 9)) {
			for (int i = 0; i < compound.getTagList("Data", 10).tagCount(); ++i) {
				loadRegion(compound.getTagList("Data", 10).getCompoundTagAt(i));
			}
		}
	}

	@Override
	public boolean removeRegion(int region) {
		if (region < 0 || regions.isEmpty()) {
			return false;
		}
		regions.remove(region);
		save();
		return true;
	}

	@SuppressWarnings("all")
	public void save() {
		CustomNpcs.debugData.start(null);
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			File file = new File(saveDir, "borders.dat_new");
			File file2 = new File(saveDir, "borders.dat_old");
			File file3 = new File(saveDir, "borders.dat");
			CompressedStreamTools.writeCompressed(getNBT(), Files.newOutputStream(file.toPath()));
			if (file2.exists()) {
				file2.delete();
			}
			file3.renameTo(file2);
			if (file3.exists()) {
				file3.delete();
			}
			file.renameTo(file3);
			if (file.exists()) {
				file.delete();
			}
		}
		catch (Exception e) { LogWriter.error(e); }
		CustomNpcs.debugData.end(null);
	}

	public void sendTo(EntityPlayerMP player) {
		Server.sendData(player, EnumPacketClient.BORDER_DATA, -1, new NBTTagCompound());
		for (int id : regions.keySet()) {
			if (id < 0 || regions.get(id).getId() < 0) {
				continue;
			}
			NBTTagCompound nbtRegion = new NBTTagCompound();
			regions.get(id).save(nbtRegion);
			Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, id, nbtRegion);
		}
		Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, -2);
	}

	public void update() {
		if (CustomNpcs.Server == null || CustomNpcs.Server.getPlayerList().getOnlinePlayerNames().length == 0 || regions.isEmpty()) {
			return;
		}
		for (Zone3D reg : regions.values()) {
			for (WorldServer w : CustomNpcs.Server.worlds) {
				reg.update(w);
			}
		}
	}

	public void update(int id) {
		if (CustomNpcs.Server == null || CustomNpcs.Server.getPlayerList().getOnlinePlayerNames().length == 0) {
			return;
		}
		if (id < 0) {
			for (int i : regions.keySet()) {
				NBTTagCompound nbtRegion = new NBTTagCompound();
				regions.get(i).save(nbtRegion);
				for (EntityPlayerMP player : CustomNpcs.Server.getPlayerList().getPlayers()) {
					Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, i, nbtRegion);
					Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, -2);
				}
			}
		} else if (regions.containsKey(id)) {
			NBTTagCompound nbtRegion = new NBTTagCompound();
			regions.get(id).save(nbtRegion);
			for (EntityPlayerMP player : CustomNpcs.Server.getPlayerList().getPlayers()) {
				Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, id, nbtRegion);
				Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, -2);
			}
		}
	}

}
