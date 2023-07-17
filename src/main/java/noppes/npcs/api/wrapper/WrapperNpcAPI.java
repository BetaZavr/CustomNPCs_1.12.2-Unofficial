package noppes.npcs.api.wrapper;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
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
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.IAnimationHandler;
import noppes.npcs.api.handler.IBorderHandler;
import noppes.npcs.api.handler.ICloneHandler;
import noppes.npcs.api.handler.IDialogHandler;
import noppes.npcs.api.handler.IFactionHandler;
import noppes.npcs.api.handler.IQuestHandler;
import noppes.npcs.api.handler.IRecipeHandler;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.containers.ContainerNpcInterface;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.LRUHashMap;
import noppes.npcs.util.NBTJsonUtil;

public class WrapperNpcAPI
extends NpcAPI {
	
	public static EventBus EVENT_BUS = new EventBus();
	private static NpcAPI instance = null;
	static Map<Integer, WorldWrapper> worldCache = new LRUHashMap<Integer, WorldWrapper>(10);

	public static void clearCache() {
		WrapperNpcAPI.worldCache.clear();
		BlockWrapper.clearCache();
	}

	public static NpcAPI Instance() {
		if (WrapperNpcAPI.instance == null) { WrapperNpcAPI.instance = new WrapperNpcAPI(); }
		return WrapperNpcAPI.instance;
	}

	private void checkWorld() {
		if (CustomNpcs.Server == null || CustomNpcs.Server.isServerStopped()) {
			throw new CustomNPCsException("No world is loaded right now", new Object[0]);
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
		mail.subject = subject;
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
	public ICloneHandler getClones() {
		return ServerCloneController.Instance;
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

	@SuppressWarnings("deprecation")
	@Override
	public IBlock getIBlock(World world, BlockPos pos) {
		return BlockWrapper.createNew(world, pos, world.getBlockState(pos));
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
		if (entity == null || entity.world==null) { return null; }
		if (entity instanceof EntityNPCInterface) { return ((EntityNPCInterface) entity).wrappedNPC; }
		return WrapperEntityData.get(entity);
	}

	@Override
	public IItemStack getIItemStack(ItemStack itemstack) {
		if (itemstack == null || itemstack.isEmpty()) {
			return ItemStackWrapper.AIR;
		}
		return (IItemStack) itemstack.getCapability(ItemStackWrapper.ITEMSCRIPTEDDATA_CAPABILITY, null);
	}

	@Override
	public INbt getINbt(NBTTagCompound compound) {
		if (compound == null) {
			return new NBTWrapper(new NBTTagCompound());
		}
		return new NBTWrapper(compound);
	}

	@Override
	public IPos getIPos(double x, double y, double z) {
		return new BlockPosWrapper(new BlockPos(x, y, z));
	}

	@Override
	public IWorld getIWorld(int dimensionId) {
		for (WorldServer world : CustomNpcs.Server.worlds) {
			if (world.provider.getDimension() == dimensionId) {
				return this.getIWorld(world);
			}
		}
		throw new CustomNPCsException("Unknown dimension id: " + dimensionId, new Object[0]);
	}

	@SuppressWarnings("deprecation")
	@Override
	public IWorld getIWorld(WorldServer world) {
		WorldWrapper w = WrapperNpcAPI.worldCache.get(world.provider.getDimension());
		if (w != null) {
			w.world = world;
			return w;
		}
		WrapperNpcAPI.worldCache.put(world.provider.getDimension(), w = WorldWrapper.createNew(world));
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
	public IQuestHandler getQuests() {
		this.checkWorld();
		return QuestController.instance;
	}

	@Override
	public String getRandomName(int dictionary, int gender) {
		return CustomNpcs.MARKOV_GENERATOR[dictionary].fetch(gender);
	}

	@Override
	public INbt getRawPlayerData(String uuid) {
		return this.getINbt(PlayerData.loadPlayerData(uuid));
	}

	@Override
	public IRecipeHandler getRecipes() {
		this.checkWorld();
		return (IRecipeHandler) RecipeController.getInstance();
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
			throw new CustomNPCsException("Default type cant be smaller than 0 or larger than 2", new Object[0]);
		}
		if (this.hasPermissionNode(permission)) {
			throw new CustomNPCsException("Permission already exists", new Object[0]);
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
			throw new CustomNPCsException("Cant cast empty string to nbt", new Object[0]);
		}
		try {
			return this.getINbt(NBTJsonUtil.Convert(str));
		} catch (NBTJsonUtil.JsonException e) {
			throw new CustomNPCsException(e, "Failed converting " + str, new Object[0]);
		}
	}

	@Override
	public IPlayer<?> getIPlayer(String name) {
		EntityPlayerMP player = CustomNpcs.Server.getPlayerList().getPlayerByUsername(name);
		if (player==null) {
			try { player = CustomNpcs.Server.getPlayerList().getPlayerByUUID(UUID.fromString(name)); }
			catch (Exception e) { }
		}
		if (player==null) {
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
	public IBorderHandler getBorders() { return BorderController.getInstance(); }

	@Override
	public IAnimationHandler getAnimations() { return AnimationController.getInstance(); }

}
