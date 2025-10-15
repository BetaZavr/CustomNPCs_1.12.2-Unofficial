package noppes.npcs.api.wrapper;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IEntityDamageSource;
import noppes.npcs.api.IMethods;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.entity.data.INpcAttribute;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.*;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.data.AttributeWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.client.util.ResourceData;
import noppes.npcs.containers.ContainerNpcInterface;
import noppes.npcs.controllers.*;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.dimensions.DimensionHandler;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.LRUHashMap;
import noppes.npcs.util.Util;
import noppes.npcs.util.NBTJsonUtil;

public class WrapperNpcAPI extends NpcAPI {

	public static volatile LRUHashMap<Integer, WorldWrapper> worldCache = new LRUHashMap<>(300);
	private static NpcAPI instance = null;
	private static final Comparator<World> sorter = (w_0, w_1) -> {
        String dimName0 = w_0.provider.getDimensionType().getName();
        String dimName1 = w_1.provider.getDimensionType().getName();
        if ("overworld".equals(dimName0)) { return -1; }
        if ("overworld".equals(dimName1)) { return 1; }
        return Integer.compare(w_0.provider.getDimension(), w_1.provider.getDimension());
    };

	public static EventBus EVENT_BUS = new EventBus();
	private final List<World> worlds = Lists.newArrayList();

	public static void clearCache() {
		WrapperNpcAPI.worldCache.clear();
		BlockWrapper.clearCache();
	}

	public static NpcAPI Instance() {
		if (WrapperNpcAPI.instance == null) {
			WrapperNpcAPI.instance = new WrapperNpcAPI();
		}
		return WrapperNpcAPI.instance;
	}

	public static void resetScriptControllerData(NBTTagCompound compound) {
		WorldWrapper.getStoredData().setNbt(new NBTWrapper(compound));
	}

	private void checkWorld() {
		if (CustomNpcs.Server != null && CustomNpcs.Server.isServerStopped()) {
			throw new CustomNPCsException("No world is loaded right now");
		}
	}

	@Override
	public ICustomGui createCustomGui(int id, int width, int height, boolean pauseGame) {
		return new CustomGuiWrapper(id, width, height, pauseGame, null);
	}

	@Override
	public IPlayerMail createMail(String sender, String title) {
		PlayerMail mail = new PlayerMail();
		mail.sender = sender;
		mail.title = title;
		return mail;
	}

	@Override
	public ICustomNpc<?> createNPC(World worldMC) {
		if (worldMC.isRemote) {
			return null;
		}
		EntityCustomNpc npc = new EntityCustomNpc(worldMC);
		return npc.wrappedNPC;
	}

	@Override
	public EventBus events() {
		return WrapperNpcAPI.EVENT_BUS;
	}

	@Override
	public String executeCommand(IWorld world, String command) {
		FakePlayer player = EntityNPCInterface.CommandPlayer;
		player.setWorld(world.getMCWorld());
		player.setPosition(0.0, 0.0, 0.0);
		return NoppesUtilServer.runCommand(world.getMCWorld(), BlockPos.ORIGIN, "API", command, null, player);
	}

	@Override
	public IPlayer<?>[] getAllPlayers() {
		List<IPlayer<?>> list = Lists.newArrayList();
		if (CustomNpcs.Server != null) {
			for (EntityPlayerMP player : CustomNpcs.Server.getPlayerList().getPlayers()) {
				if (player == null) {
					continue;
				}
				list.add((IPlayer<?>) this.getIEntity(player));
			}
		}
		return list.toArray(new IPlayer<?>[0]);
	}

	@Override
	public IAnimationHandler getAnimations() {
		return AnimationController.getInstance();
	}

	@Override
	public IBorderHandler getBorders() {
		return BorderController.getInstance();
	}

	@Override
	public ICloneHandler getClones() {
		return ServerCloneController.Instance;
	}

	@Override
	public IDimensionHandler getCustomDimension() {
		return DimensionHandler.getInstance();
	}

	@Override
	public IDialogHandler getDialogs() {
		return DialogController.instance;
	}

	@Override
	public IFactionHandler getFactions() {
		this.checkWorld();
		return FactionController.instance;
	}

	@Override
	public File getGlobalDir() {
		return CustomNpcs.Dir;
	}

	@Override
	public INpcAttribute getIAttribute(IAttributeInstance attributeMC) {
		return new AttributeWrapper(attributeMC);
	}

	@Override
	public IBlock getIBlock(World worldMC, BlockPos posMC) {
		if (worldMC == null) { return null; }
		return BlockWrapper.createNew(worldMC, posMC, worldMC.getBlockState(posMC));
	}

	@Override
	public IContainer getIContainer(Container containerMC) {
		if (containerMC instanceof ContainerNpcInterface) {
			return ContainerNpcInterface.getOrCreateIContainer((ContainerNpcInterface) containerMC);
		}
		return new ContainerWrapper(containerMC);
	}

	@Override
	public IContainer getIContainer(IInventory inventoryMC) {
		return new ContainerWrapper(inventoryMC);
	}

	@Override
	public IDamageSource getIDamageSource(DamageSource damageMC) {
		return new DamageSourceWrapper(damageMC);
	}

	@Override
	public IEntityDamageSource getIDamageSource(String name, IEntity<?> entity) {
		return new NpcEntityDamageSource(name, entity);
	}

	@Override
	public IEntity<?> getIEntity(Entity entityMC) {
		if (entityMC == null || entityMC.world == null) { return null; }
		if (entityMC instanceof EntityNPCInterface) { return ((EntityNPCInterface) entityMC).wrappedNPC; }
		return WrapperEntityData.get(entityMC);
	}

	@Override
	public IItemStack getIItemStack(ItemStack stackMC) {
		if (stackMC == null || stackMC.isEmpty()) { return ItemStackWrapper.AIR; }
		return (IItemStack) stackMC.getCapability(ItemStackWrapper.ITEM_SCRIPTED_DATA_CAPABILITY, null);
	}

	@Override
	public IKeyBinding getIKeyBinding() {
		return KeyController.getInstance();
	}

	@Override
	public INbt getINbt(NBTTagCompound nbtMC) {
		if (nbtMC == null) {
			return new NBTWrapper(new NBTTagCompound());
		}
		return new NBTWrapper(nbtMC);
	}

	@Override
	public IPlayer<?> getIPlayer(String nameOrUUID) {
		IPlayer<?>[] iPlayers = getAllPlayers();
		for (IPlayer<?> iPlayer : iPlayers) {
			if (iPlayer.getName().equals(nameOrUUID) || iPlayer.getUUID().equals(nameOrUUID)) { return iPlayer; }
		}
		return null;
	}

	@Override
	public IPos getIPos(BlockPos posMC) {
		return new BlockPosWrapper(posMC);
	}

	@Override
	public IPos getIPos(double x, double y, double z) {
		return new BlockPosWrapper(x, y, z);
	}

	@Override
	public IWorld getIWorld(String dimension) {
		resetWorlds();
		for (World world : worlds) {
			if (world.provider.getDimensionType().getName().equals(dimension)) {
				return getIWorld(world);
			}
		}
		if (!Thread.currentThread().getName().toLowerCase().contains("client")) {
			throw new CustomNPCsException("Unknown dimension: \"" + dimension + "\"");
		}
		return null;
	}

	@Override
	public IWorld getIWorld(int dimensionId) {
		resetWorlds();
		for (World world : worlds) {
			if (world.provider.getDimension() == dimensionId) {
				return getIWorld(world);
			}
		}
		if (!Thread.currentThread().getName().toLowerCase().contains("client")) {
			throw new CustomNPCsException("Unknown dimension: \"" + dimensionId + "\"");
		}
		return null;
	}

	private void resetWorlds() {
		checkWorld();
		if (CustomNpcs.Server != null) {
			worlds.clear();
			worlds.addAll(Arrays.asList(CustomNpcs.Server.worlds));
			worlds.sort(sorter);
		}
		EntityPlayer player = CustomNpcs.proxy.getPlayer();
		if (player != null && !worlds.contains(player.world)) {
			worlds.add(player.world);
			worlds.sort(sorter);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public IWorld getIWorld(World worldMC) {
		WorldWrapper w = WrapperNpcAPI.worldCache.get(worldMC.provider.getDimension());
		if (w != null) {
			if (w.world == null) {
				w.world = worldMC;
			}
		} else {
			WrapperNpcAPI.worldCache.put(worldMC.provider.getDimension(), w = WorldWrapper.createNew(worldMC));
		}
		return w;
	}

	@Override
	public IWorld[] getIWorlds() {
		resetWorlds();
		IWorld[] iWorlds = new IWorld[worlds.size()];
		for (int i = 0; i < worlds.size(); i++) { iWorlds[i] = getIWorld(worlds.get(i)); }
		return iWorlds;
	}

	@Override
	public IMarcetHandler getMarkets() {
		return MarcetController.getInstance();
	}

	@Override
	public IMethods getMethods() {
		return Util.instance;
	}

	@Override
	public IQuestHandler getQuests() {
		this.checkWorld();
		return QuestController.instance;
	}

	@Override
	public String getRandomName(int dictionary, int gender) {
		return CustomNpcs.MARKOV_GENERATOR[dictionary].fetch(gender);
	}

	@Override
	@SuppressWarnings("all")
	public INbt getRawPlayerData(String uuid, String name) {
		if  (CustomNpcs.Server != null) {
			UUID uuidMC;
			try { uuidMC = UUID.fromString(uuid); }
			catch (Exception e) { throw new CustomNPCsException("Invalid UUID string: \"" + uuid + "\""); }
            EntityPlayerMP player = CustomNpcs.Server.getPlayerList().getPlayerByUUID(uuidMC);
            if (player != null && player.getName().equals(name)) {
                PlayerData data = CustomNpcs.proxy.getPlayerData(player);
                if (data != null) {
                    return getINbt(data.getNBT());
                }
            }
        }
		return getINbt(PlayerData.loadPlayerData(uuid, name));
	}

	@Override
	public IRecipeHandler getRecipes() {
		this.checkWorld();
		return null;
	}

	@Override
	public File getWorldDir() {
		return CustomNpcs.getWorldSaveDirectory();
	}

	@Override
	public boolean hasPermissionNode(String permission) {
		return PermissionAPI.getPermissionHandler().getRegisteredNodes().contains(permission);
	}

	@Override
	public void registerCommand(CommandNoppesBase command) {
		CustomNpcs.NoppesCommand.registerCommand(command);
	}

	@Override
	public void registerPermissionNode(String permission, int defaultType) {
		if (defaultType < 0 || defaultType > 2) {
			throw new CustomNPCsException("Default type cant be smaller than 0 or larger than 2");
		}
		if (this.hasPermissionNode(permission)) {
			throw new CustomNPCsException("Permission already exists");
		}
		DefaultPermissionLevel level = DefaultPermissionLevel.values()[defaultType];
		PermissionAPI.registerNode(permission, level, permission);
	}

	@Override
	public ICustomNpc<?> spawnNPC(World worldMC, int x, int y, int z) {
		if (worldMC.isRemote) { return null; }
		EntityCustomNpc npc = new EntityCustomNpc(worldMC);
		npc.setPositionAndRotation(x + 0.5, y, z + 0.5, 0.0f, 0.0f);
		npc.ais.setStartPos(x, y, z);
		npc.setHealth(npc.getMaxHealth());
		worldMC.spawnEntity(npc);
		return npc.wrappedNPC;
	}

	@Override
	public INbt stringToNbt(String str) {
		if (str == null || str.isEmpty()) {
			throw new CustomNPCsException("Cant cast empty string to nbt");
		}
		try {
			return this.getINbt(NBTJsonUtil.Convert(str));
		} catch (NBTJsonUtil.JsonException e) {
			throw new CustomNPCsException(e, "Failed converting " + str);
		}
	}

	@Override
	public ICustomPlayerData getPlayerData(IPlayer<?> player) {
		if (player == null) { return null; }
		return PlayerDataController.instance.getDataFromUsername(CustomNpcs.Server, player.getName());
	}

	@Override
	public ResourceData getResourceData(ResourceLocation texture, int u, int v, int width, int height) {
		return new ResourceData(texture, u, v, width, height);
	}

	@Override
	public IData getTempdata() { return WorldWrapper.getTempData(); }

	@Override
	public IData getStoreddata() { return WorldWrapper.getStoredData(); }

}
