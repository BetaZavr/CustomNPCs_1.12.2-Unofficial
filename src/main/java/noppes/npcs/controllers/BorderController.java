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
		if (newInstance()) {
			BorderController.instance = new BorderController();
		}
		return BorderController.instance;
	}
	private static boolean newInstance() {
		if (BorderController.instance == null) {
			return true;
		}
		File file = CustomNpcs.getWorldSaveDirectory();
		return file != null && !BorderController.instance.filePath.equals(file.getAbsolutePath());
	}

	public final Map<Integer, Zone3D> regions = new TreeMap<>();

	private String filePath;

	public BorderController() {
		BorderController.instance = this;
		this.filePath = CustomNpcs.getWorldSaveDirectory().getAbsolutePath();
		this.loadRegions();
	}

	public Zone3D createNew(int dimensionID, BlockPos pos) {
		Zone3D reg = new Zone3D(this.getUnusedId(), dimensionID, pos.getX(), pos.getY(), pos.getZ());
		regions.put(reg.getId(), reg);
		return reg;
	}

	@Override
	public IBorder createNew(int dimensionID, IPos pos) {
		return this.createNew(dimensionID, pos.getMCBlockPos());
	}

	@Override
	public IBorder[] getAllRegions() {
		return this.regions.values().toArray(new IBorder[0]);
	}

	public NBTTagCompound getNBT() {
		NBTTagList list = new NBTTagList();
		for (Zone3D region : this.regions.values()) {
			NBTTagCompound nbtRegion = new NBTTagCompound();
			region.save(nbtRegion);
			list.appendTag(nbtRegion);
		}
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setTag("Data", list);
		return nbttagcompound;
	}

	public int getRegionID(int dimensionID, BlockPos pos) {
		for (Zone3D reg : this.regions.values()) {
			if (reg.contains(pos.getX(), pos.getY(), pos.getZ(), dimensionID)) {
				return reg.getId();
			}
		}
		return -1;
	}


	@Override
	public IBorder getRegion(int regionId) {
		return this.regions.get(regionId);
	}

	@Override
	public IBorder[] getRegions(int dimensionID) {
		List<IBorder> regs = new ArrayList<>();
		for (Zone3D reg : this.regions.values()) {
			if (reg.dimensionID == dimensionID) {
				regs.add(reg);
			}
		}
		return regs.toArray(new IBorder[0]);
	}

	public List<Zone3D> getRegionsInWorld(int dimensionID) {
		List<Zone3D> regs = new ArrayList<>();
		for (Zone3D reg : this.regions.values()) {
			if (reg.dimensionID == dimensionID) {
				regs.add(reg);
			}
		}
		return regs;
	}

	@Override
	public List<Zone3D> getNearestRegions(int dimensionID, double xPos, double yPos, double zPos, double distance) {
		AxisAlignedBB searchBox = new AxisAlignedBB(xPos - distance, 0.0d, zPos - distance, xPos + distance + 1.0d, 255.0d, zPos + distance + 1.0d);
		List<Zone3D> regions = new ArrayList<>();
		for (Zone3D reg : this.regions.values()) {
			if (reg.dimensionID != dimensionID) { continue; }
			if (searchBox.intersects(reg.getAxisAlignedBB())) { regions.add(reg); }
		}
		return regions;
	}

	public int getUnusedId() {
		int id = 0;
		while (this.regions.containsKey(id)) { id++; }
		return id;
	}

	public Zone3D loadRegion(NBTTagCompound nbtRegion) {
		if (nbtRegion == null || !nbtRegion.hasKey("ID", 3) || nbtRegion.getInteger("ID") < 0) {
			return null;
		}
		int id = nbtRegion.getInteger("ID");
		if (this.regions.containsKey(id)) {
			this.regions.get(id).load(nbtRegion);
			return this.regions.get(id);
		}
		Zone3D region = new Zone3D();
		region.load(nbtRegion);
		this.regions.put(region.getId(), region);
		return this.regions.get(region.getId());
	}

	private void loadRegions() {
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if (saveDir == null) {
			return;
		}
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.startDebug("Common", null, "loadRegions");
		}
		this.filePath = saveDir.getAbsolutePath();
		try {
			File file = new File(saveDir, "borders.dat");
			if (file.exists()) {
				this.loadRegions(file);
			}
		} catch (Exception e) {
			try {
				File file2 = new File(saveDir, "borders.dat_old");
				if (file2.exists()) {
					this.loadRegions(file2);
				}
			} catch (Exception ex) { LogWriter.error("Error:", ex); }
		}
		CustomNpcs.debugData.endDebug("Common", null, "loadRegions");
	}

	private void loadRegions(File file) throws IOException {
		this.loadRegions(CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath())));
	}

	public void loadRegions(NBTTagCompound compound) {
		regions.clear();
		if (compound.hasKey("Data", 9)) {
			for (int i = 0; i < compound.getTagList("Data", 10).tagCount(); ++i) {
				this.loadRegion(compound.getTagList("Data", 10).getCompoundTagAt(i));
			}
		}
	}

	@Override
	public boolean removeRegion(int region) {
		if (region < 0 || this.regions.isEmpty()) {
			return false;
		}
		this.regions.remove(region);
		this.saveRegions();
		return true;
	}

	public void saveRegions() {
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			File file = new File(saveDir, "borders.dat_new");
			File file2 = new File(saveDir, "borders.dat_old");
			File file3 = new File(saveDir, "borders.dat");
			CompressedStreamTools.writeCompressed(this.getNBT(), Files.newOutputStream(file.toPath()));
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
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	public void sendTo(EntityPlayerMP player) {
		Server.sendData(player, EnumPacketClient.BORDER_DATA, -1, new NBTTagCompound());
		for (int id : this.regions.keySet()) {
			if (id < 0 || this.regions.get(id).getId() < 0) {
				continue;
			}
			NBTTagCompound nbtRegion = new NBTTagCompound();
			this.regions.get(id).save(nbtRegion);
			Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, id, nbtRegion);
		}
		Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, -2);
	}

	public void update() {
		if (CustomNpcs.Server == null || CustomNpcs.Server.getPlayerList().getOnlinePlayerNames().length == 0 || this.regions.isEmpty()) {
			return;
		}
		for (Zone3D reg : this.regions.values()) {
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
			for (int i : this.regions.keySet()) {
				NBTTagCompound nbtRegion = new NBTTagCompound();
				this.regions.get(i).save(nbtRegion);
				for (EntityPlayerMP player : CustomNpcs.Server.getPlayerList().getPlayers()) {
					Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, i, nbtRegion);
					Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, -2);
				}
			}
		} else if (this.regions.containsKey(id)) {
			NBTTagCompound nbtRegion = new NBTTagCompound();
			this.regions.get(id).save(nbtRegion);
			for (EntityPlayerMP player : CustomNpcs.Server.getPlayerList().getPlayers()) {
				Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, id, nbtRegion);
				Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, -2);
			}
		}
	}

}
