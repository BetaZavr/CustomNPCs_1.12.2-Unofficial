package noppes.npcs.dimensions;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import com.google.common.collect.Lists;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.ServerWorldEventHandler;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.Server;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.handler.IDimensionHandler;
import noppes.npcs.api.handler.data.IWorldInfo;
import noppes.npcs.constants.EnumPacketClient;

public class DimensionHandler
extends WorldSavedData
implements IDimensionHandler
{
	static String NAME = "CustomNpcsHandler";

	HashMap<Integer, CustomWorldInfo> dimensionInfo;
	HashMap<Integer, UUID> toBeDeleted;

	public DimensionHandler(String name) {
		super(name);
		this.dimensionInfo = new HashMap<Integer, CustomWorldInfo>();
		this.toBeDeleted = new HashMap<Integer, UUID>();
	}

	public DimensionHandler() {
		super(DimensionHandler.NAME);
		this.dimensionInfo = new HashMap<Integer, CustomWorldInfo>();
		this.toBeDeleted = new HashMap<Integer, UUID>();
	}
	
	@Override
	public boolean isDirty() { return true; }

	public void createDimension(EntityPlayerMP playerEntity, CustomWorldInfo worldInfo) {
		worldInfo.id = findFreeDimensionID();
		this.dimensionInfo.put(worldInfo.id, worldInfo);
		DimensionManager.registerDimension(worldInfo.id, CustomNpcs.customDimensionType);
		loadDimension(worldInfo.id, worldInfo);
		if (playerEntity!=null) { playerEntity.sendMessage(new TextComponentTranslation("message.dimensions.created", worldInfo.getWorldName(), ""+worldInfo.id)); }
		syncWithClients();
	}

	private int findFreeDimensionID() {
		int currentID = 100;
		while (this.dimensionInfo.containsKey(currentID)) { currentID++; }
		return currentID;
	}

	public ITextComponent generateList() {
		StringBuilder stringBuilder = new StringBuilder();
		if (this.dimensionInfo.isEmpty()) { return new TextComponentTranslation("dimensions.nodimensions"); }
		else {
			int counter = 0;
			for (Entry<Integer, CustomWorldInfo> entry : this.dimensionInfo.entrySet()) {
				stringBuilder.append(String.format("%s %s", "DIM " + entry.getKey(), "(" + entry.getValue().getWorldName() + ")"));
				counter++;
				if (counter < dimensionInfo.size()) { stringBuilder.append("\n"); }
			}
			return new TextComponentString(stringBuilder.toString());
		}
	}

	public static DimensionHandler getInstance() {
		DimensionHandler INSTANCE = (DimensionHandler) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getMapStorage().getOrLoadData(DimensionHandler.class, DimensionHandler.NAME);
		if (INSTANCE == null) {
			INSTANCE = new DimensionHandler();
			FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getMapStorage().setData(DimensionHandler.NAME, INSTANCE);
		}
		return INSTANCE;
	}

	public String getDimensionName(int dimensionId) { return this.dimensionInfo.get(dimensionId).getWorldName(); }

	public HashMap<Integer, CustomWorldInfo> getDimensionInfo() { return this.dimensionInfo; }

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagList nbtList = nbt.getTagList("dimensionInfo", 10);
		for (int i = 0; i < nbtList.tagCount(); i++) {
			NBTTagCompound compound = nbtList.getCompoundTagAt(i);
			this.dimensionInfo.put(compound.getInteger("dimensionID"), new CustomWorldInfo(compound.getCompoundTag("worldInfo")));
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTTagList nbtList = new NBTTagList();
		for (Entry<Integer, CustomWorldInfo> entry : dimensionInfo.entrySet()) {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setInteger("dimensionID", entry.getKey());
			compound.setTag("worldInfo", entry.getValue().cloneNBTCompound(null));
			nbtList.appendTag(compound);
		}
		nbt.setTag("dimensionInfo", nbtList);
		return nbt;
	}

	public void loadDimensions() {
		for (Entry<Integer, CustomWorldInfo> entry : this.dimensionInfo.entrySet()) {
			int dimensionID = entry.getKey();
			WorldInfo worldInfo = entry.getValue();
			DimensionManager.registerDimension(dimensionID, CustomNpcs.customDimensionType);
			loadDimension(dimensionID, worldInfo);
		}
	}

	private void loadDimension(int dimensionID, WorldInfo worldInfo) {
		WorldServer overworld = (WorldServer) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
		if (overworld == null) { throw new RuntimeException("Cannot Hotload Dim: Overworld is not Loaded!"); }
		try { DimensionManager.getProviderType(dimensionID); }
		catch (Exception e) {
			LogWriter.error("Cannot Hotload Dim: " + e);
			return;
		}
		MinecraftServer mcServer = overworld.getMinecraftServer();
		ISaveHandler savehandler = overworld.getSaveHandler();
		EnumDifficulty difficulty = mcServer.getEntityWorld().getDifficulty();
		WorldServer world = (WorldServer) (new WorldCustom(worldInfo, mcServer, savehandler, dimensionID, overworld, mcServer.profiler).init());
		world.addEventListener(new ServerWorldEventHandler(mcServer, world));
		MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));
		if (!mcServer.isSinglePlayer()) { world.getWorldInfo().setGameType(mcServer.getGameType()); }
		mcServer.setDifficultyForAllWorlds(difficulty);
	}

	public void deleteDimension(ICommandSender sender, int dimensionID) {
		if (dimensionID<100 || !this.dimensionInfo.containsKey(dimensionID)) {
			if (sender!=null) {
				if (this.toBeDeleted.containsKey(dimensionID)) { sender.sendMessage(new TextComponentTranslation("message.dimensions.err.del")); }
				else if (dimensionID>=100) { sender.sendMessage(new TextComponentTranslation("message.dimensions.err.notmod")); }
			}
			return;
		}
		World worldObj = DimensionManager.getWorld(dimensionID);
		if (worldObj.playerEntities.size() > 0) {
			WorldServer world = sender.getServer().getWorld(0);
			BlockPos coords = world.getSpawnCoordinate();
			if (coords == null) {
				coords = world.getSpawnPoint();
				if (!world.isAirBlock(coords)) {
					coords = world.getTopSolidOrLiquidBlock(coords);
				} else {
					while (world.isAirBlock(coords) && coords.getY() > 0) {
						coords = coords.down();
					}
					if (coords.getY() == 0) {
						coords = world.getTopSolidOrLiquidBlock(coords);
					}
				}
			}
			List<EntityPlayerMP> players = Lists.<EntityPlayerMP>newArrayList();
			for (EntityPlayer player : worldObj.playerEntities) {
				if (!(player instanceof EntityPlayerMP)) { continue; }
				player.sendMessage(new TextComponentTranslation("message.dimensions.tp.isdelete"));
				players.add((EntityPlayerMP) player);
			}
			for (EntityPlayerMP player : players) {
				NoppesUtilPlayer.teleportPlayer(player, coords.getX(), coords.getY(), coords.getZ(), 0);
			}
		}
		Entity entitySender = null;
		if (sender!=null) { entitySender = sender.getCommandSenderEntity(); }
		this.toBeDeleted.put(dimensionID, entitySender != null ? entitySender.getUniqueID() : null);
		DimensionManager.unloadWorld(dimensionID);
	}

	public void unload(World world, int dimensionID) {
		if (this.dimensionInfo.containsKey(dimensionID)) { DimensionManager.unregisterDimension(dimensionID); }
		if (this.toBeDeleted.containsKey(dimensionID)) {
			UUID uniqueID = this.toBeDeleted.get(dimensionID);
			this.toBeDeleted.remove(dimensionID);
			this.dimensionInfo.remove(dimensionID);
			((WorldServer) world).flush();
			File dimensionFolder = new File(DimensionManager.getCurrentSaveRootDirectory(), "DIM" + dimensionID);
			EntityPlayerMP player = null;
			if (uniqueID != null) { player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(uniqueID); }
			try { FileUtils.deleteDirectory(dimensionFolder); }
			catch (IOException e) {
				e.printStackTrace();
				if (player != null) { player.sendMessage(new TextComponentTranslation("message.dimensions.err.notmod", ""+dimensionID)); }
			}
			finally {
				if (player != null) { player.sendMessage(new TextComponentTranslation("message.dimensions.del.foder", ""+dimensionID)); }
			}
			syncWithClients();
		}
	}

	private void syncWithClients() {
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.DIMENSIOS_IDS, this.getIDs());
	}

	public int[] getIDs() {
		int[] a = new int[this.dimensionInfo.size()];
		int j = 0;
		for (int i : this.dimensionInfo.keySet()) {
			a[j] = i;
			j++;
		}
		return a;
	}

	@Override
	public IWorldInfo getMCWorldInfo(int id) {
		if (id<100) { return null; } 
		return this.dimensionInfo.get(id);
	}

	public Map<String, Integer> getMapDimensionsIDs() {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		for (int id : DimensionManager.getStaticDimensionIDs()) {
			WorldProvider provider = DimensionManager.createProviderFor(id);
			String key = (DimensionManager.getWorld(id)!=null)+"&"+provider.getDimensionType().getName();
			if (this.dimensionInfo.containsKey(id)) {
				key = (this.toBeDeleted.containsKey(id) ? "delete" : DimensionManager.getWorld(id)!=null ) + "&" + this.dimensionInfo.get(id).getWorldName();
			}
			key += "&"+provider.getDimensionType().getSuffix();
			map.put(key, id);
		}
		return map;
	}

	@Override
	public void setNbt(INbt nbt) {
		this.readFromNBT(nbt.getMCNBT());
	}
	

	@Override
	public INbt getNbt() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return NpcAPI.Instance().getINbt(nbt);
	}
	

	@Override
	public int[] getAllIDs() {
		int[] arr = new int[this.dimensionInfo.size()];
		int j = 0;
		for (int i : this.dimensionInfo.keySet()) {
			arr[j] = i;
			j++;
		}
		return arr;
	}

	@Override
	public void deleteDimension(int dimensionID) {
		this.deleteDimension(null, dimensionID);
	}

	@Override
	public IWorldInfo createDimension() {
		CustomWorldInfo cwi = new CustomWorldInfo(new NBTTagCompound());
		this.createDimension(null, cwi);
		return cwi;
	}
	
}