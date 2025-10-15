package noppes.npcs.api;

import java.io.File;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
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
import noppes.npcs.api.wrapper.WrapperNpcAPI;
import noppes.npcs.client.util.ResourceData;

@SuppressWarnings("all")
public abstract class NpcAPI {

	private static NpcAPI instance = null;

	public static NpcAPI Instance() {
		if (NpcAPI.instance != null) {
			return NpcAPI.instance;
		}
		if (!IsAvailable()) {
			return null;
		}
		try {
			NpcAPI.instance = WrapperNpcAPI.Instance();
		}
		catch (Exception e) { LogWriter.error(e); }
		return NpcAPI.instance;
	}

	public static boolean IsAvailable() {
		return Loader.isModLoaded(CustomNpcs.MODID);
	}

	public abstract ICustomGui createCustomGui(@ParamName("id") int id, @ParamName("width") int width, @ParamName("height") int height, @ParamName("pauseGame") boolean pauseGame);

	public abstract IPlayerMail createMail(@ParamName("sender") String sender, @ParamName("title") String title);

	public abstract ICustomNpc<?> createNPC(@ParamName("worldMC") World worldMC);

	public abstract EventBus events();

	public abstract String executeCommand(@ParamName("world") IWorld world, @ParamName("command") String command);

	public abstract IPlayer<?>[] getAllPlayers();

	public abstract IAnimationHandler getAnimations();

	public abstract IBorderHandler getBorders();

	public abstract ICloneHandler getClones();

	public abstract IDimensionHandler getCustomDimension();

	public abstract IDialogHandler getDialogs();

	public abstract IFactionHandler getFactions();

	public abstract File getGlobalDir();

	public abstract INpcAttribute getIAttribute(@ParamName("attributeMC") IAttributeInstance attributeMC);

	public abstract IBlock getIBlock(@ParamName("worldMC") World worldMC, @ParamName("posMC") BlockPos posMC);

	public abstract IContainer getIContainer(@ParamName("containerMC") Container containerMC);

	public abstract IContainer getIContainer(@ParamName("inventoryMC") IInventory inventoryMC);

	public abstract IDamageSource getIDamageSource(@ParamName("damageMC") DamageSource damageMC);

	public abstract IEntityDamageSource getIDamageSource(@ParamName("name") String name, @ParamName("entity") IEntity<?> entity);

	public abstract IEntity<?> getIEntity(@ParamName("entityMC") Entity entityMC);

	public abstract IItemStack getIItemStack(@ParamName("stackMC") ItemStack stackMC);

	public abstract IKeyBinding getIKeyBinding();

	public abstract INbt getINbt(@ParamName("nbtMC") NBTTagCompound nbtMC);

	public abstract IPlayer<?> getIPlayer(@ParamName("nameOrUUID") String nameOrUUID);

	public abstract IPos getIPos(@ParamName("posMC") BlockPos posMC);

	public abstract IPos getIPos(@ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z);

	public abstract IWorld getIWorld(@ParamName("dimensionName") String dimensionName);

	public abstract IWorld getIWorld(@ParamName("dimensionId") int dimensionId);

	public abstract IWorld getIWorld(@ParamName("worldMC") World worldMC);

	public abstract IWorld[] getIWorlds();

	public abstract IMarcetHandler getMarkets();

	public abstract IMethods getMethods();

	public abstract IQuestHandler getQuests();

	public abstract String getRandomName(@ParamName("dictionary") int dictionary, @ParamName("gender") int gender);

	public abstract INbt getRawPlayerData(@ParamName("uuid") String uuid, @ParamName("name") String name);

	public abstract IRecipeHandler getRecipes();

	public abstract File getWorldDir();

	public abstract boolean hasPermissionNode(@ParamName("permission") String permission);

	public abstract void registerCommand(@ParamName("command") CommandNoppesBase command);

	public abstract void registerPermissionNode(@ParamName("permission") String permission, @ParamName("defaultType") int defaultType);

	public abstract ICustomNpc<?> spawnNPC(@ParamName("worldMC") World worldMC, @ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	public abstract INbt stringToNbt(@ParamName("str") String str);

	public abstract ICustomPlayerData getPlayerData(@ParamName("player") IPlayer<?> player);

	public abstract ResourceData getResourceData(@ParamName("texture") ResourceLocation texture, @ParamName("u") int u, @ParamName("v") int v, @ParamName("width") int width, @ParamName("height") int height);

	public abstract IData getTempdata();

	public abstract IData getStoreddata();

}
