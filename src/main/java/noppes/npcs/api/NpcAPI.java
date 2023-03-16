package noppes.npcs.api;

import java.io.File;

import net.minecraft.entity.Entity;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.ICloneHandler;
import noppes.npcs.api.handler.IDialogHandler;
import noppes.npcs.api.handler.IFactionHandler;
import noppes.npcs.api.handler.IQuestHandler;
import noppes.npcs.api.handler.IRecipeHandler;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.WrapperNpcAPI;

public abstract class NpcAPI {
	
	private static NpcAPI instance = null;

	public static NpcAPI Instance() {
		if (NpcAPI.instance != null) { return NpcAPI.instance; }
		if (!IsAvailable()) { return null; }
		try {
			NpcAPI.instance =  WrapperNpcAPI.Instance();
			//Class<?> c = Class.forName("noppes.npcs.api.wrapper.WrapperNpcAPI");
			//NpcAPI.instance = (NpcAPI) c.getMethod("Instance", (Class[]) new Class[0]).invoke(null, new Object[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	public abstract IEntityDamageSource getIDamageSource(String name, IEntity<?> entity); // New

	public abstract IEntity<?> getIEntity(Entity entity);

	public abstract IItemStack getIItemStack(ItemStack stack);

	public abstract INbt getINbt(NBTTagCompound nbt);

	public abstract IPos getIPos(double x, double y, double z);

	public abstract IWorld getIWorld(int dimensionId);

	public abstract IWorld getIWorld(WorldServer world);

	public abstract IWorld[] getIWorlds();

	public abstract IQuestHandler getQuests();

	public abstract String getRandomName(int dictionary, int gender);

	public abstract INbt getRawPlayerData(String uuid);

	public abstract IRecipeHandler getRecipes();

	public abstract File getWorldDir();

	public abstract boolean hasPermissionNode(String permission);

	public abstract void registerCommand(CommandNoppesBase command);

	public abstract void registerPermissionNode(String permission, int defaultType);

	public abstract ICustomNpc<?> spawnNPC(World world, int x, int y, int z);

	public abstract INbt stringToNbt(String str);
	
}
