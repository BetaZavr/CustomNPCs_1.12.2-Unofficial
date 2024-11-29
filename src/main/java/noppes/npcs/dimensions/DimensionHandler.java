package noppes.npcs.dimensions;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
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

import javax.annotation.Nonnull;

public class DimensionHandler extends WorldSavedData implements IDimensionHandler {

	static String NAME = "CustomNpcsHandler";
	
	public DimensionHandler(String mapName) {
		super(mapName);
	}
	
	public static DimensionHandler getInstance() {
		DimensionHandler INSTANCE = (DimensionHandler) Objects.requireNonNull(FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getMapStorage()).getOrLoadData(DimensionHandler.class, DimensionHandler.NAME);
		if (INSTANCE == null) {
			INSTANCE = new DimensionHandler();
			FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld().getMapStorage().setData(DimensionHandler.NAME, INSTANCE);
		}
		return INSTANCE;
	}
	private final Map<Integer, CustomWorldInfo> dimensionInfo = new TreeMap<>();

	private final Map<Integer, UUID> toBeDeleted = new TreeMap<>();

	public DimensionHandler() {
		super(DimensionHandler.NAME);
	}

	@Override
	public IWorldInfo createDimension() {
		CustomWorldInfo cwi = new CustomWorldInfo(new NBTTagCompound());
		this.createDimension(null, cwi);
		return cwi;
	}

	public void createDimension(EntityPlayerMP playerEntity, CustomWorldInfo worldInfo) {
		worldInfo.id = findFreeDimensionID();
		this.dimensionInfo.put(worldInfo.id, worldInfo);
		DimensionManager.registerDimension(worldInfo.id, CustomNpcs.customDimensionType);
		loadDimension(worldInfo.id, worldInfo);
		if (playerEntity != null) {
			playerEntity.sendMessage(new TextComponentTranslation("message.dimensions.created",
					worldInfo.getWorldName(), "" + worldInfo.id));
		}
		syncWithClients();
	}

	public void deleteDimension(ICommandSender sender, int dimensionID) {
		if (dimensionID < 100 || !this.dimensionInfo.containsKey(dimensionID)) {
			if (sender != null) {
				if (this.toBeDeleted.containsKey(dimensionID)) {
					sender.sendMessage(new TextComponentTranslation("message.dimensions.err.del"));
				} else if (dimensionID >= 100) {
					sender.sendMessage(new TextComponentTranslation("message.dimensions.err.notmod"));
				}
			}
			return;
		}
		World worldObj = DimensionManager.getWorld(dimensionID);
		if (!worldObj.playerEntities.isEmpty()) {
			WorldServer world = Objects.requireNonNull(sender.getServer()).getWorld(0);
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
			List<EntityPlayerMP> players = new ArrayList<>();
			for (EntityPlayer player : worldObj.playerEntities) {
				if (!(player instanceof EntityPlayerMP)) {
					continue;
				}
				player.sendMessage(new TextComponentTranslation("message.dimensions.tp.isdelete"));
				players.add((EntityPlayerMP) player);
			}
			for (EntityPlayerMP player : players) {
				NoppesUtilPlayer.teleportPlayer(player, coords.getX(), coords.getY(), coords.getZ(), 0,
						player.rotationYaw, player.rotationPitch);
			}
		}
		Entity entitySender = null;
		if (sender != null) {
			entitySender = sender.getCommandSenderEntity();
		}
		this.toBeDeleted.put(dimensionID, entitySender != null ? entitySender.getUniqueID() : null);
		DimensionManager.unloadWorld(dimensionID);
		List<WorldServer> list = new ArrayList<>();
		for (WorldServer w : CustomNpcs.Server.worlds) {
			if (w.provider.getDimension() != dimensionID) {
				list.add(w);
			}
		}
		if (CustomNpcs.Server.worlds.length != list.size()) {
			CustomNpcs.Server.worlds = list.toArray(new WorldServer[0]);
		}
	}

	@Override
	public void deleteDimension(int dimensionID) {
		this.deleteDimension(null, dimensionID);
	}

	private int findFreeDimensionID() {
		int currentID = 100;
		while (this.dimensionInfo.containsKey(currentID)) {
			currentID++;
		}
		return currentID;
	}

	@Override
	public int[] getAllIDs() {
		int[] arr = new int[this.dimensionInfo.size()];
		int j = 0;
		for (int i : this.dimensionInfo.keySet()) {
			if (i < 100 || this.toBeDeleted.containsKey(i)) {
				continue;
			}
			arr[j] = i;
			j++;
		}
		return arr;
	}

	public Map<String, Integer> getMapDimensionsIDs() {
		HashMap<String, Integer> map = new HashMap<>();
		for (int id : DimensionManager.getStaticDimensionIDs()) {
			WorldProvider provider = DimensionManager.createProviderFor(id);
			StringBuilder key = new StringBuilder((DimensionManager.getWorld(id) != null) + "&" + provider.getDimensionType().getName());
			if (this.dimensionInfo.containsKey(id)) {
				if (this.toBeDeleted.containsKey(id)) {
					continue;
				}
				key = new StringBuilder((DimensionManager.getWorld(id) != null) + "&" + this.dimensionInfo.get(id).getWorldName());
			}
			while (map.containsKey(key + "&" + provider.getDimensionType().getSuffix())) {
				key.append("_");
			}
			key.append("&").append(provider.getDimensionType().getSuffix());
			map.put(key.toString(), id);
		}
		return map;
	}

	@Override
	public IWorldInfo getMCWorldInfo(int id) {
		return this.dimensionInfo.get(id);
	}

	@Override
	public INbt getNbt() {
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(nbt);
	}

	public boolean isDelete(int id) {
		return this.toBeDeleted.containsKey(id);
	}

	@Override
	public boolean isDirty() {
		return true;
	}

	private void loadDimension(int dimensionID, WorldInfo worldInfo) {
		WorldServer overworld = (WorldServer) FMLCommonHandler.instance().getMinecraftServerInstance().getEntityWorld();
        try {
			DimensionManager.getProviderType(dimensionID);
		} catch (Exception e) {
			LogWriter.error("Cannot Hotload Dim: " + e);
			return;
		}
		MinecraftServer mcServer = overworld.getMinecraftServer();
		ISaveHandler savehandler = overworld.getSaveHandler();
        assert mcServer != null;
        EnumDifficulty difficulty = mcServer.getEntityWorld().getDifficulty();
		WorldServer world = (WorldServer) (new WorldCustom(worldInfo, mcServer, savehandler, dimensionID, overworld,
				mcServer.profiler).init());
		world.addEventListener(new ServerWorldEventHandler(mcServer, world));
		LogWriter.debug("Try Load World: " + dimensionID + "; world = " + world);
		try {
			Class.forName("org.orecruncher.dsurround.server.services.AtmosphereService");
        } catch (ClassNotFoundException e) {
			MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));
		}
		if (!mcServer.isSinglePlayer()) {
			world.getWorldInfo().setGameType(mcServer.getGameType());
		}
		mcServer.setDifficultyForAllWorlds(difficulty);
	}

	public void loadDimensions() {
		for (Entry<Integer, CustomWorldInfo> entry : this.dimensionInfo.entrySet()) {
			int dimensionID = entry.getKey();
			try {
				DimensionManager.registerDimension(dimensionID, CustomNpcs.customDimensionType);
				loadDimension(dimensionID, entry.getValue());
			} catch (Exception e) {
				LogWriter.error("Error Load Custom Dimensions [" + dimensionID + "]: ", e);
            }
		}
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound nbt) {
		NBTTagList nbtList = nbt.getTagList("dimensionInfo", 10);
		for (int i = 0; i < nbtList.tagCount(); i++) {
			NBTTagCompound compound = nbtList.getCompoundTagAt(i);
			this.dimensionInfo.put(compound.getInteger("dimensionID"),
					new CustomWorldInfo(compound.getCompoundTag("worldInfo")));
		}
	}

	@Override
	public void setNbt(INbt nbt) {
		this.readFromNBT(nbt.getMCNBT());
	}

	private void syncWithClients() {
		Object ids = this.getAllIDs();
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.DIMENSION_IDS, ids);
	}

	public void unload(World world, int dimensionID) {
		if (this.dimensionInfo.containsKey(dimensionID)) {
			DimensionManager.unregisterDimension(dimensionID);
		}
		if (this.toBeDeleted.containsKey(dimensionID)) {
			UUID uniqueID = this.toBeDeleted.get(dimensionID);
			this.toBeDeleted.remove(dimensionID);
			this.dimensionInfo.remove(dimensionID);
			((WorldServer) world).flush();
			File dimensionFolder = new File(DimensionManager.getCurrentSaveRootDirectory(), "DIM" + dimensionID);
			EntityPlayerMP player = null;
			if (uniqueID != null) {
				player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList()
						.getPlayerByUUID(uniqueID);
			}
			try {
				FileUtils.deleteDirectory(dimensionFolder);
			} catch (IOException e) {
				LogWriter.error("Error:", e);
				if (player != null) {
					player.sendMessage(new TextComponentTranslation("message.dimensions.err.notmod", "" + dimensionID));
				}
			} finally {
				if (player != null) {
					player.sendMessage(new TextComponentTranslation("message.dimensions.del.folder", "" + dimensionID));
				}
			}
			syncWithClients();
		}
	}

	@Override
	public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound nbt) {
		NBTTagList nbtList = new NBTTagList();
		for (Entry<Integer, CustomWorldInfo> entry : this.dimensionInfo.entrySet()) {
			if (this.toBeDeleted.containsKey(entry.getKey())) {
				continue;
			}
			NBTTagCompound compound = new NBTTagCompound();
			compound.setInteger("dimensionID", entry.getKey());
			compound.setTag("worldInfo", entry.getValue().cloneNBTCompound(null));
			nbtList.appendTag(compound);
		}
		nbt.setTag("dimensionInfo", nbtList);
		return nbt;
	}

}