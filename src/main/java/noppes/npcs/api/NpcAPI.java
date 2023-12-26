package noppes.npcs.api;

import java.io.File;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import noppes.npcs.CustomNpcs;
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
import noppes.npcs.api.wrapper.WrapperNpcAPI;

public abstract class NpcAPI {
	
	private static NpcAPI instance = null;

	public static NpcAPI Instance() {
		if (NpcAPI.instance != null) { return NpcAPI.instance; }
		if (!IsAvailable()) { return null; }
		try { NpcAPI.instance =  WrapperNpcAPI.Instance(); }
		catch (Exception e) { e.printStackTrace(); }
		return NpcAPI.instance;
	}

	public static boolean IsAvailable() {
		return Loader.isModLoaded(CustomNpcs.MODID);
	}

	public abstract ICustomGui createCustomGui(int id, int width, int height, boolean pauseGame);

	public abstract IPlayerMail createMail(String sender, String subject);

	public abstract ICustomNpc<?> createNPC(World world);

	public abstract EventBus events();

	public abstract String executeCommand(IWorld world, String command);

	public abstract ICloneHandler getClones();

	public abstract IDialogHandler getDialogs();

	public abstract IFactionHandler getFactions();

	public abstract File getGlobalDir();

	public abstract IBlock getIBlock(World world, BlockPos pos);

	public abstract IContainer getIContainer(Container container);

	public abstract IContainer getIContainer(IInventory inventory);

	public abstract IDamageSource getIDamageSource(DamageSource source);
	
	public abstract IEntityDamageSource getIDamageSource(String name, IEntity<?> entity);

	public abstract IEntity<?> getIEntity(Entity entity);

	public abstract IItemStack getIItemStack(ItemStack stack);

	public abstract INbt getINbt(NBTTagCompound nbt);

	public abstract IPos getIPos(double x, double y, double z);

	public abstract IPos getIPos(BlockPos pos);

	public abstract IWorld getIWorld(int dimensionId);

	public abstract IWorld getIWorld(World world);

	public abstract IWorld[] getIWorlds();

	public abstract IQuestHandler getQuests();

	public abstract String getRandomName(int dictionary, int gender);

	public abstract INbt getRawPlayerData(String uuid, String name);

	public abstract IRecipeHandler getRecipes();

	public abstract File getWorldDir();

	public abstract boolean hasPermissionNode(String permission);

	public abstract void registerCommand(CommandNoppesBase command);

	public abstract void registerPermissionNode(String permission, int defaultType);

	public abstract ICustomNpc<?> spawnNPC(World world, int x, int y, int z);

	public abstract INbt stringToNbt(String str);
	
	public abstract IPlayer<?> getIPlayer(String nameOrUUID);
	
	public abstract IPlayer<?>[] getAllPlayers();

	public abstract IBorderHandler getBorders();
	
	public abstract IAnimationHandler getAnimations();

	public abstract IMetods getMetods();

	public abstract IKeyBinding getIKeyBinding();

	public abstract INpcAttribute getIAttribute(IAttributeInstance mcattribute);
	
	public abstract IDimensionHandler getCustomDimention();

	public abstract IMarcetHandler getMarkets();
	
	
}
