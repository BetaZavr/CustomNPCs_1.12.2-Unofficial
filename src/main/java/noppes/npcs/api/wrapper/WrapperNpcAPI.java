package noppes.npcs.api.wrapper;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
import noppes.npcs.LogWriter;
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
import noppes.npcs.api.entity.data.INpcAttribute;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.IAnimationHandler;
import noppes.npcs.api.handler.IBorderHandler;
import noppes.npcs.api.handler.ICloneHandler;
import noppes.npcs.api.handler.IDialogHandler;
import noppes.npcs.api.handler.IDimensionHandler;
import noppes.npcs.api.handler.IFactionHandler;
import noppes.npcs.api.handler.IKeyBinding;
import noppes.npcs.api.handler.IMarcetHandler;
import noppes.npcs.api.handler.IQuestHandler;
import noppes.npcs.api.handler.IRecipeHandler;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.data.AttributeWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.containers.ContainerNpcInterface;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.KeyController;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.dimensions.DimensionHandler;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;
import noppes.npcs.util.LRUHashMap;
import noppes.npcs.util.NBTJsonUtil;

public class WrapperNpcAPI extends NpcAPI {

	public static EventBus EVENT_BUS = new EventBus();
	private static NpcAPI instance = null;
	static Map<Integer, WorldWrapper> worldCache = new LRUHashMap<>(10);
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

	private void checkWorld() {
		if (CustomNpcs.Server == null || CustomNpcs.Server.isServerStopped()) {
			throw new CustomNPCsException("No world is loaded right now");
		}
	}

	@Override
	public ICustomGui createCustomGui(int id, int width, int height, boolean pauseGame) {
		return new CustomGuiWrapper(id, width, height, pauseGame, null); // Changed
	}

	@Override
	public IPlayerMail createMail(String sender, String subject) {
		PlayerMail mail = new PlayerMail();
		mail.sender = sender;
		mail.title = subject;
		return mail;
	}

	@Override
	public ICustomNpc<?> createNPC(World world) {
		if (world.isRemote) {
			return null;
		}
		EntityCustomNpc npc = new EntityCustomNpc(world);
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
	public INpcAttribute getIAttribute(IAttributeInstance mcAttribute) {
		return new AttributeWrapper(mcAttribute);
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlock getIBlock(World world, BlockPos pos) {
		if (world == null) {
			return null;
		}
		try {
			return BlockWrapper.createNew(world, pos, world.getBlockState(pos));
		}
		catch (Exception e) { LogWriter.error("Error:", e); }
		return null;
	}

	@Override
	public IContainer getIContainer(Container container) {
		if (container instanceof ContainerNpcInterface) {
			return ContainerNpcInterface.getOrCreateIContainer((ContainerNpcInterface) container);
		}
		return new ContainerWrapper(container);
	}

	@Override
	public IContainer getIContainer(IInventory inventory) {
		return new ContainerWrapper(inventory);
	}

	@Override
	public IDamageSource getIDamageSource(DamageSource damagesource) {
		return new DamageSourceWrapper(damagesource);
	}

	@Override
	public IEntityDamageSource getIDamageSource(String name, IEntity<?> entity) { // new
		return new NpcEntityDamageSource(name, entity);
	}

	@Override
	public IEntity<?> getIEntity(Entity entity) {
		if (entity == null || entity.world == null) {
			return null;
		}
		if (entity instanceof EntityNPCInterface) {
			return ((EntityNPCInterface) entity).wrappedNPC;
		}
		return WrapperEntityData.get(entity);
	}

	@Override
	public IItemStack getIItemStack(ItemStack itemstack) {
		if (itemstack == null || itemstack.isEmpty()) {
			return ItemStackWrapper.AIR;
		}
		return (IItemStack) itemstack.getCapability(ItemStackWrapper.ITEM_SCRIPTED_DATA_CAPABILITY, null);
	}

	@Override
	public IKeyBinding getIKeyBinding() {
		return KeyController.getInstance();
	}

	@Override
	public INbt getINbt(NBTTagCompound compound) {
		if (compound == null) {
			return new NBTWrapper(new NBTTagCompound());
		}
		return new NBTWrapper(compound);
	}

	@Override
	public IPlayer<?> getIPlayer(String name) {
		EntityPlayerMP player = CustomNpcs.Server.getPlayerList().getPlayerByUsername(name);
		if (player == null) {
			try {
				player = CustomNpcs.Server.getPlayerList().getPlayerByUUID(UUID.fromString(name));
			}
			catch (Exception e) { LogWriter.error("Error:", e); }
		}
		if (player == null) {
			for (EntityPlayerMP p : CustomNpcs.Server.getPlayerList().getPlayers()) {
				if (p.getName().equalsIgnoreCase(name)) {
					player = p;
					break;
				}
			}
		}
		return player == null ? null : (IPlayer<?>) this.getIEntity(player);
	}

	@Override
	public IPos getIPos(BlockPos pos) {
		return new BlockPosWrapper(pos);
	}

	@Override
	public IPos getIPos(double x, double y, double z) {
		return new BlockPosWrapper(x, y, z);
	}

	@Override
	public IWorld getIWorld(String dimension) {
		if (CustomNpcs.Server == null) {
			EntityPlayer player = CustomNpcs.proxy.getPlayer();
			if (!this.worlds.contains(player.world)) { this.worlds.add(player.world); }
		} else {
			this.worlds.clear();
			this.worlds.addAll(Arrays.asList(CustomNpcs.Server.worlds));
		}
		ResourceLocation loc = new ResourceLocation(dimension);
		for (World world : this.worlds) {
			if (world.provider.getDimensionType().getName().equals(loc.getResourcePath())) {
				return this.getIWorld(world);
			}
		}
		throw new CustomNPCsException("Unknown dimension: \"" + dimension + "\"");
	}

	@Override
	public IWorld getIWorld(int dimensionId) {
		if (CustomNpcs.Server != null) {
			worlds.clear();
            worlds.addAll(Arrays.asList(CustomNpcs.Server.worlds));
		}
		EntityPlayer player = CustomNpcs.proxy.getPlayer();
		if (player != null && !worlds.contains(player.world)) { worlds.add(player.world); }
		for (World world : worlds) {
			if (world.provider.getDimension() == dimensionId) {
				return this.getIWorld(world);
			}
		}
		throw new CustomNPCsException("Unknown dimension id: " + dimensionId);
	}

	@SuppressWarnings("deprecation")
	@Override
	public IWorld getIWorld(World world) {
		WorldWrapper w = WrapperNpcAPI.worldCache.get(world.provider.getDimension());
		if (w != null) {
			if (w.world == null) {
				w.world = world;
			}
		} else {
			WrapperNpcAPI.worldCache.put(world.provider.getDimension(), w = WorldWrapper.createNew(world));
		}
		return w;
	}

	@Override
	public IWorld[] getIWorlds() {
		this.checkWorld();
		IWorld[] worlds = new IWorld[CustomNpcs.Server.worlds.length];
		for (int i = 0; i < CustomNpcs.Server.worlds.length; ++i) {
			worlds[i] = this.getIWorld(CustomNpcs.Server.worlds[i]);
		}
		return worlds;
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
	public ICustomNpc<?> spawnNPC(World world, int x, int y, int z) {
		if (world.isRemote) {
			return null;
		}
		EntityCustomNpc npc = new EntityCustomNpc(world);
		npc.setPositionAndRotation(x + 0.5, y, z + 0.5, 0.0f, 0.0f);
		npc.ais.setStartPos(x, y, z);
		npc.setHealth(npc.getMaxHealth());
		world.spawnEntity(npc);
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

}
