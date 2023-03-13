package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.Zone3D;
import noppes.npcs.util.NBTJsonUtil;

public class BorderController {
	
	private static BorderController instance;
	public HashMap<Integer, Zone3D> regions;
	private String filePath;

	public BorderController() {
		this.filePath = "";
		BorderController.instance = this;
		this.regions = Maps.<Integer, Zone3D>newHashMap();
		this.loadRegions();
	}
	
	public static BorderController getInstance() {
		if (newInstance()) { BorderController.instance = new BorderController(); }
		return BorderController.instance;
	}

	private static boolean newInstance() {
		if (BorderController.instance == null) { return true; }
		File file = CustomNpcs.getWorldSaveDirectory();
		return file != null && !BorderController.instance.filePath.equals(file.getAbsolutePath());
	}

	public Zone3D getRegion(int regionId) {
		return this.regions.get(regionId);
	}

	public NBTTagCompound getNBT() {
		NBTTagList list = new NBTTagList();
		for (Zone3D region : this.regions.values()) {
			NBTTagCompound nbtRegion = new NBTTagCompound();
			region.writeToNBT(nbtRegion);
			list.appendTag(nbtRegion);
		}
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setTag("Data", list);
		return nbttagcompound;
	}

	public int getUnusedId() {
		int id;
		for (id = 0; this.regions.containsKey(id); ++id) { }
		return id;
	}

	private void loadRegions() {
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if (saveDir == null) { return; }
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
			} catch (Exception ex) {
			}
		}
	}

	private void loadRegions(File file) throws IOException {
		this.loadRegions(CompressedStreamTools.readCompressed(new FileInputStream(file)));
	}

	public void loadRegions(NBTTagCompound compound) throws IOException {
		if (this.regions!=null) { this.regions.clear(); }
		else { this.regions = Maps.<Integer, Zone3D>newHashMap(); }
		if (compound.hasKey("Data", 9)) {
			for (int i = 0; i < compound.getTagList("Data", 10).tagCount(); ++i) {
				this.loadRegion(compound.getTagList("Data", 10).getCompoundTagAt(i));
			}
		}
	}

	public Zone3D loadRegion(NBTTagCompound nbtRegion) {
		if (nbtRegion==null || !nbtRegion.hasKey("ID", 3) || nbtRegion.getInteger("ID")<0) { return null; }
		if (this.regions.containsKey(nbtRegion.getInteger("ID"))) {
			this.regions.get(nbtRegion.getInteger("ID")).readFromNBT(nbtRegion);
			return this.regions.get(nbtRegion.getInteger("ID"));
		}
		Zone3D region = new Zone3D();
		region.readFromNBT(nbtRegion);
		this.regions.put(region.id, region);
		return this.regions.get(region.id);
	}
	
	public boolean removeRegion(int region) {
		if (region < 0 || this.regions.size() == 0) { return false; }
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
			CompressedStreamTools.writeCompressed(this.getNBT(), (OutputStream) new FileOutputStream(file));
			if (file2.exists()) { file2.delete(); }
			file3.renameTo(file2);
			if (file3.exists()) { file3.delete(); }
			file.renameTo(file3);
			if (file.exists()) { file.delete(); }
			if (CustomNpcs.VerboseDebug) {
				NBTJsonUtil.SaveFile(new File(saveDir, "borders.json"), this.getNBT());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<Zone3D> getRegionsInWorld(int dimensionID) {
		List<Zone3D> regs = Lists.<Zone3D>newArrayList();
		for (Zone3D reg : this.regions.values()) {
			if (reg.dimensionID==dimensionID) { regs.add(reg); }
		}
		return regs;
	}
	
	public void sendTo(EntityPlayerMP player) {
		Server.sendData(player, EnumPacketClient.BORDER_DATA, -1, new NBTTagCompound());
		for (int id : this.regions.keySet()) {
			if (id<0 || this.regions.get(id).id<0) { continue; }
			NBTTagCompound nbtRegion = new NBTTagCompound();
			this.regions.get(id).writeToNBT(nbtRegion);
			Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, id, nbtRegion);
		}
		Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, -2);
	}

	public void sendToAll(int id) {
		if (id<0 || !this.regions.containsKey(id)) { return; }
		if (CustomNpcs.Server==null || CustomNpcs.Server.getPlayerList().getOnlinePlayerNames().length==0) { return; }
		NBTTagCompound nbtRegion = new NBTTagCompound();
		this.regions.get(id).writeToNBT(nbtRegion);
		for (EntityPlayerMP player : CustomNpcs.Server.getPlayerList().getPlayers()) {
			Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, id, nbtRegion);
			Server.sendDataDelayed(player, EnumPacketClient.BORDER_DATA, 10, -2);
		}
	}

	public Zone3D createNew(int imensionID, BlockPos pos) {
		if (this.regions==null) { this.regions = Maps.<Integer, Zone3D>newHashMap(); }
		Zone3D reg = new Zone3D(this.getUnusedId(), imensionID, pos.getX(), pos.getY(), pos.getZ());
		this.regions.put(reg.id, reg);
		return reg;
	}

	public void update() { // <- ServerTickHandler.onServerTick(event)
		if (CustomNpcs.Server==null || this.regions.size()==0) { return; }
		for (int i = 0; i < CustomNpcs.Server.worlds.length; ++i) {
			for (Zone3D reg : this.regions.values()) {
				if (reg.dimensionID==CustomNpcs.Server.worlds[i].provider.getDimension()) { reg.update(CustomNpcs.Server.worlds[i]); }
			}
		}
	}
	
}
