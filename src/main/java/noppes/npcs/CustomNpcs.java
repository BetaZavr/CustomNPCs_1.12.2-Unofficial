package noppes.npcs;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockVine;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommand;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.network.play.server.SPacketScoreboardObjective;
import net.minecraft.network.play.server.SPacketUpdateScore;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import nikedemos.markovnames.generators.MarkovAncientGreek;
import nikedemos.markovnames.generators.MarkovAztec;
import nikedemos.markovnames.generators.MarkovCustomNPCsClassic;
import nikedemos.markovnames.generators.MarkovGenerator;
import nikedemos.markovnames.generators.MarkovJapanese;
import nikedemos.markovnames.generators.MarkovOldNorse;
import nikedemos.markovnames.generators.MarkovRoman;
import nikedemos.markovnames.generators.MarkovSaami;
import nikedemos.markovnames.generators.MarkovSlavic;
import nikedemos.markovnames.generators.MarkovSpanish;
import nikedemos.markovnames.generators.MarkovWelsh;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.handler.capability.INbtHandler;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.api.wrapper.WrapperEntityData;
import noppes.npcs.api.wrapper.WrapperNpcAPI;
import noppes.npcs.capability.NbtStorage;
import noppes.npcs.command.CommandNoppes;
import noppes.npcs.config.ConfigLoader;
import noppes.npcs.config.ConfigProp;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.controllers.ChunkController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.DropController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.GlobalDataController;
import noppes.npcs.controllers.KeyController;
import noppes.npcs.controllers.LinkedNpcController;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.MassBlockController;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.SpawnController;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.VisibilityController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.dimensions.CustomWorldProvider;
import noppes.npcs.dimensions.DimensionHandler;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.DataDebug;
import noppes.npcs.util.DataDebug.Debug;
import noppes.npcs.util.ObfuscationHelper;

@Mod(modid = "customnpcs", name = "CustomNpcs", version = "1.12", acceptedMinecraftVersions = "1.12, 1.12.1, 1.12.2")
public class CustomNpcs {
	
	@ConfigProp(info = "Currency symbol displayed in stores (unicode)")
	public static String charCurrencies = "20AC";
	@ConfigProp(info = "Number of chunk loading npcs that can be active at the same time")
	public static int ChuckLoaders = 20;
	@ConfigProp(info = "Minimum and maximum melle and range Damage of NPCs for 1 and Maximum level, respectively (rarity Boss)")
	public static int[] damageBoss = new int[] { 8, 52, 6, 26 };
	@ConfigProp(info = "Minimum and maximum melle and range Damage of NPCs for 1 and Maximum level, respectively (rarity Elite)")
	public static int[] damageElite = new int[] { 6, 32, 3, 16 };
	@ConfigProp(info = "Minimum and maximum melle and range Damage of NPCs for 1 and Maximum level, respectively (rarity Normal)")
	public static int[] damageNormal = new int[] { 4, 22, 2, 11 };
	@ConfigProp(info = "Default interact line. Leave empty to not have one")
	public static String DefaultInteractLine = "Hello @p";
	@ConfigProp(info = "If you are running sponge and you want to disable the permissions set this to true")
	public static boolean DisablePermissions = false;
	@ConfigProp(info = "Enable Chat Bubbles from npcs")
	public static boolean EnableChatBubbles = true;
	@ConfigProp(info = "Enable chat bubbles from players")
	public static boolean EnablePlayerChatBubbles = true;
	@ConfigProp(info = "For some it works, for others it doesnt, so Im disabling by default")
	public static boolean EnableInvisibleNpcs = false;
	@ConfigProp(info = "Whether scripting is enabled or not")
	public static boolean EnableScripting = true;
	@ConfigProp(info = "Enables CustomNpcs startup update message")
	public static boolean EnableUpdateChecker = true;
	@ConfigProp(info = "Maximum and minimum amount of experience dropped from the NPC for the minimum and maximum level (Elite x1.75; Boss x4.75)")
	public static int[] experience = new int[] { 2, 3, 100, 115 };
	@ConfigProp(info = "Font size for custom fonts (doesn't work with minecrafts font)")
	public static int FontSize = 18;

	@ConfigProp(info = "Main text color of elements in GUI modification")
	public static int mainColor = 0xFFFFFFFF;
	@ConfigProp(info = "Name text color in GUI modification")
	public static int lableColor = 0xFFE0E0E0;
	@ConfigProp(info = "Text color for inactive elements in modification GUI")
	public static int notEnableColor = 0xFFA0A0A0;
	@ConfigProp(info = "Text color of elements in modification GUI when the element is held down by the mouse cursor")
	public static int hoverColor = 0xFFFFFFA0;
	@ConfigProp(info = "Text Color for GUI Quest Log")
	public static int questLogColor = 0xFF404060;
	@ConfigProp(info = "Color of message bubbles above NPC head [text, frame, base]")
	public static int[] chatNpcColors = new int[] { 0x000000, 0x000000, 0xFFFFFF };
	@ConfigProp(info = "Color of message bubbles above Player head [text, frame, base]")
	public static int[] chatPlayerColors = new int[] { 0x000000, 0x2C4C00, 0xE0FFB0 };
	
	@ConfigProp(info = "When set to Minecraft it will use minecrafts font, when Default it will use OpenSans. Can only use fonts installed on your PC")
	public static String FontType = "Default";
	@ConfigProp(info = "Type 0 = Normal, Type 1 = Solid")
	public static int HeadWearType = 1;
	@ConfigProp(info = "Minimum and maximum health of NPCs for 1 and Maximum level, respectively (rarity Boss)")
	public static int[] healthBoss = new int[] { 250, 20000 };
	@ConfigProp(info = "Minimum and maximum health of NPCs for 1 and Maximum level, respectively (rarity Elite)")
	public static int[] healthElite = new int[] { 60, 1200 };
	@ConfigProp(info = "Minimum and maximum health of NPCs for 1 and Maximum level, respectively (rarity Normal)")
	public static int[] healthNormal = new int[] { 20, 500 };
	@ConfigProp(info = "Mod API only, or all methods. Attention! - loads the system")
	public static boolean helpAllMetods = false;
	@ConfigProp(info = "Enables Ice Melting")
	public static boolean IceMeltsEnabled = true;
	@ConfigProp(info = "Enables leaves decay")
	public static boolean LeavesDecayEnabled = true;
	@ConfigProp(info = "Maximum NPC level (45 recommended)")
	public static int maxLv = 45;
	@ConfigProp(info = "Resizes the model for rarity. (Normal, Elite, Boss)")
	public static int[] modelRaritySize = new int[] { 5, 6, 7 };
	@ConfigProp(info = "Arguments given to the Nashorn scripting library")
	public static String NashorArguments = "-strict";
	@ConfigProp(info = "Navigation search range for NPCs. Not recommended to increase if you have a slow pc or on a server")
	public static int NpcNavRange = 32;
	@ConfigProp(info = "Set to true if you want the dialog command option to be able to use op commands like tp etc")
	public static boolean NpcUseOpCommands = false;
	@ConfigProp(info = "Only ops can create and edit npcs")
	public static boolean OpsOnly = false;
	@SidedProxy(clientSide = "noppes.npcs.client.ClientProxy", serverSide = "noppes.npcs.CommonProxy")
	public static CommonProxy proxy;
	@ConfigProp(info = "Whether to recalculate Stats when setting Level and Rarity")
	public static boolean recalculateLR = true;
	@ConfigProp(info = "Parameters for calculating NPC Resistances (0=-100%, 1=0%, 2=100% [melee, arrow, explosion, knockback] rarity Boss)")
	public static int[] resistanceBoss = new int[] { 110, 125, 175, 195 };
	@ConfigProp(info = "Parameters for calculating NPC Resistances (0=-100%, 1=0%, 2=100% [melee, arrow, explosion, knockback] rarity Elite)")
	public static int[] resistanceElite = new int[] { 105, 110, 130, 150 };
	@ConfigProp(info = "Parameters for calculating NPC Resistances (0=-100%, 1=0%, 2=100% [melee, arrow, explosion, knockback] rarity Normal)")
	public static int[] resistanceNormal = new int[] { 100, 100, 100, 110 };
	@ConfigProp(info = "Whether to display Level and Rarity. If 1 then it will be installed on all clients")
	public static boolean showLR = true;
	@ConfigProp(info = "Display player balance in inventory")
	public static boolean showMoney = true;
	@ConfigProp(info = "Display player Quest Compass")
	public static boolean showQuestCompass = true;
	@ConfigProp(info = "Normal players can use soulstone on animals")
	public static boolean SoulStoneAnimals = true;
	@ConfigProp(info = "Normal players can use soulstone on all npcs")
	public static boolean SoulStoneNPCs = false;
	public static long ticks;
	@ConfigProp(info = "Enable Script Helper (on Client)")
	public static boolean useScriptHelper = true;
	@ConfigProp(info = "Script Helper also shows private values (on Client)")
	public static boolean scriptHelperForPro = true;
	@ConfigProp(info = "Script Helper also uses obfuscation (on Client)")
	public static boolean scriptHelperObfuscations = false;
	@ConfigProp(info = "Show description when hovering cursor on over GUI elements")
	public static boolean showDescriptions = true;
	@ConfigProp(info = "Show Debug")
	public static boolean VerboseDebug = false;
	@ConfigProp(info = "Enables Vine Growth")
	public static boolean VineGrowthEnabled = true;
	@ConfigProp(info = "Maximum blocks to install per second with the Builder item")
	public static int maxBuilderBlocks = 1000;
	@ConfigProp(info = "Color of Script code elements. "+((char) 167)+"[Numbers, Functions, Strings, Comments]")
	public static String[] charCodeColor = new String[] { "6", "9", "7", "2" };
	@ConfigProp(info = "Maximum number of items in one Drop group")
	public static int maxItemInDropsNPC = 32;
	@ConfigProp(info = "Cancel the creation of variables in each Forge event (saves FPS)")
	public static boolean simplifiedForgeEvents = false;
	@ConfigProp
	public static boolean SceneButtonsEnabled = true;
	@ConfigProp
	public static boolean NpcSpeachTriggersChatEvent = false;
	@ConfigProp
	public static boolean InventoryGuiEnabled = true;
	@ConfigProp
	public static boolean FixUpdateFromPre_1_12 = true;
	@ConfigProp
	public static boolean EnableDefaultEyes = true;

	public static String MODID = "customnpcs";
	public static FMLEventChannel Channel;
	public static FMLEventChannel ChannelPlayer;
	public static CustomNpcs instance;
	public static CommandNoppes NoppesCommand = new CommandNoppes();
	public static MarkovGenerator[] MARKOV_GENERATOR = new MarkovGenerator[10];
	public static MinecraftServer Server;
	public static DataDebug debugData = new DataDebug();
	public static final Map<Class<?>, String> forgeEventNames = new HashMap<Class<?>, String>();
	public static final Map<Class<?>, String> forgeClientEventNames = new HashMap<Class<?>, String>();
	public static boolean FreezeNPCs = false, showServerQuestCompass = true;
	public static File Dir;
	public static ConfigLoader Config;
	public static ITextComponent prefix = new TextComponentString(((char) 167)+"e["+((char) 167)+"2CustomNpcs"+((char) 167)+"e]"+((char) 167)+"r: ");
	private static String preSound = "";
	public static DimensionType customDimensionType;
	public static ModContainer mod;
	
	static { FluidRegistry.enableUniversalBucket(); }

	public CustomNpcs() { CustomNpcs.instance = this; }
	
	public static File getWorldSaveDirectory() { return getWorldSaveDirectory(null); }

	public static File getWorldSaveDirectory(String s) { // Changed
		try {
			File dir = new File(".");
			if (CustomNpcs.Server!=null) {
				if (!CustomNpcs.Server.isDedicatedServer()) {
					dir = new File(Minecraft.getMinecraft().mcDataDir, "saves");
				}
				dir = new File(new File(dir, CustomNpcs.Server.getFolderName()), CustomNpcs.MODID);
			}
			if (s != null) {
				dir = new File(dir, s);
			}
			if (!dir.exists()) {
				dir.mkdirs();
			}
			return dir;
		} catch (Exception e) {
			LogWriter.error("Error getting worldsave", e);
			return null;
		}
	}

	@SuppressWarnings({ "unchecked", "deprecation", "rawtypes" })
	@Mod.EventHandler
	public void preload(FMLPreInitializationEvent ev) {
		CustomNpcs.Channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("CustomNPCs");
		CustomNpcs.ChannelPlayer = NetworkRegistry.INSTANCE.newEventDrivenChannel("CustomNPCsPlayer");
		(CustomNpcs.Dir = new File(new File(ev.getModConfigurationDirectory(), ".."), "customnpcs")).mkdir();
		(CustomNpcs.Config = new ConfigLoader(this.getClass(), ev.getModConfigurationDirectory(), "CustomNpcs"))
				.loadConfig();
		if (CustomNpcs.NpcNavRange < 16) {
			CustomNpcs.NpcNavRange = 16;
		}
		CustomItems.load();
		// OLD Metods Capability
		CapabilityManager.INSTANCE.register(PlayerData.class, new Capability.IStorage() {
			public void readNBT(Capability capability, Object instance, EnumFacing side, NBTBase nbt) {
			}

			public NBTBase writeNBT(Capability capability, Object instance, EnumFacing side) {
				return null;
			}
		}, PlayerData.class);
		CapabilityManager.INSTANCE.register(WrapperEntityData.class, new Capability.IStorage() {
			public void readNBT(Capability capability, Object instance, EnumFacing side, NBTBase nbt) {
			}

			public NBTBase writeNBT(Capability capability, Object instance, EnumFacing side) {
				return null;
			}
		}, WrapperEntityData.class);
		CapabilityManager.INSTANCE.register(ItemStackWrapper.class, new Capability.IStorage<ItemStackWrapper>() {
			public void readNBT(Capability capability, ItemStackWrapper instance, EnumFacing side, NBTBase nbt) {
			}

			public NBTBase writeNBT(Capability capability, ItemStackWrapper instance, EnumFacing side) {
				return null;
			}
		}, () -> null);
		/*CapabilityManager.INSTANCE.register(MarkData.class, new Capability.IStorage() {
			public void readNBT(Capability capability, Object instance, EnumFacing side, NBTBase nbt) {
			}

			public NBTBase writeNBT(Capability capability, Object instance, EnumFacing side) {
				return null;
			}
		}, MarkData.class);*/
		CapabilityManager.INSTANCE.register(INbtHandler.class, new NbtStorage(), MarkData::new);

		NetworkRegistry.INSTANCE.registerGuiHandler(this, CustomNpcs.proxy);
		MinecraftForge.EVENT_BUS.register(new ServerEventsHandler());
		MinecraftForge.EVENT_BUS.register(new ServerTickHandler());
		MinecraftForge.EVENT_BUS.register(new CustomEntities());
		MinecraftForge.EVENT_BUS.register(CustomNpcs.proxy);
		NpcAPI.Instance().events().register(new AbilityEventHandler());
		ForgeChunkManager.setForcedChunkLoadingCallback(this, (ForgeChunkManager.LoadingCallback) new ChunkController());
		
		CustomNpcs.customDimensionType = DimensionType.register("CustomDimensions", "CustomNpcs", "CustomDimensions".hashCode(), CustomWorldProvider.class, false);

		CustomNpcs.proxy.preload();
		ObfuscationHelper.setValue(RangedAttribute.class, (RangedAttribute) SharedMonsterAttributes.MAX_HEALTH, Double.MAX_VALUE, 1);
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent ev) {
		PixelmonHelper.load();
		ScriptController controller = new ScriptController();
		if (CustomNpcs.EnableScripting && controller.languages.size() > 0) {
			MinecraftForge.EVENT_BUS.register(controller);
			MinecraftForge.EVENT_BUS.register(new ScriptPlayerEventHandler().registerForgeEvents());
			MinecraftForge.EVENT_BUS.register(new ScriptItemEventHandler());
		}
		ForgeModContainer.fullBoundingBoxLadders = true;
		new CustomNpcsPermissions();
		CustomNpcs.MARKOV_GENERATOR[0] = new MarkovRoman(3);
		CustomNpcs.MARKOV_GENERATOR[1] = new MarkovJapanese(4);
		CustomNpcs.MARKOV_GENERATOR[2] = new MarkovSlavic(3);
		CustomNpcs.MARKOV_GENERATOR[3] = new MarkovWelsh(3);
		CustomNpcs.MARKOV_GENERATOR[4] = new MarkovSaami(3);
		CustomNpcs.MARKOV_GENERATOR[5] = new MarkovOldNorse(4);
		CustomNpcs.MARKOV_GENERATOR[6] = new MarkovAncientGreek(3);
		CustomNpcs.MARKOV_GENERATOR[7] = new MarkovAztec(3);
		CustomNpcs.MARKOV_GENERATOR[8] = new MarkovCustomNPCsClassic(3);
		CustomNpcs.MARKOV_GENERATOR[9] = new MarkovSpanish(3);
		CustomNpcs.proxy.load();
	}
	
	@Mod.EventHandler
	public static void postload(FMLPostInitializationEvent ev) { // New
		if (CustomNpcs.maxLv < 1) { CustomNpcs.maxLv = 1; }
		else if (CustomNpcs.maxLv > 999) { CustomNpcs.maxLv = 999; }
		if (CustomNpcs.maxBuilderBlocks < 20) { CustomNpcs.maxBuilderBlocks = 20; }
		else if (CustomNpcs.maxBuilderBlocks > 25000) { CustomNpcs.maxBuilderBlocks = 25000; }
		if (CustomNpcs.maxItemInDropsNPC < 5) { CustomNpcs.maxItemInDropsNPC = 5; }
		try {
			CustomNpcs.charCurrencies = new String(Character.toChars(Integer.parseInt(CustomNpcs.charCurrencies)));
		} catch (Exception e) {
			CustomNpcs.charCurrencies = new String(Character.toChars(0x20AC));
		}
		CustomNpcs.proxy.postload();
		new AdditionalMethods();
		for (ModContainer mod : Loader.instance().getModList()) {
			if (mod.getModId().equals(CustomNpcs.MODID)) {
				CustomNpcs.mod = mod;
				break;
			}
		}
		LogWriter.info("Mod loaded ^_^ Have a good game!");
	}

	@SideOnly(Side.CLIENT)
	public static void stopPreviousSound(String newSound) {
		if (CustomNpcs.preSound != null && !CustomNpcs.preSound.isEmpty()) {
			Minecraft.getMinecraft().getSoundHandler().stop(CustomNpcs.preSound, SoundCategory.VOICE);
		}
		CustomNpcs.preSound = newSound;
	}

	@Mod.EventHandler
	public void serverstart(FMLServerStartingEvent event) {
		event.registerServerCommand((ICommand) CustomNpcs.NoppesCommand);
		EntityNPCInterface.ChatEventPlayer = new FakePlayer(event.getServer().getWorld(0), (GameProfile) EntityNPCInterface.ChatEventProfile);
		EntityNPCInterface.CommandPlayer = new FakePlayer(event.getServer().getWorld(0), (GameProfile) EntityNPCInterface.CommandProfile);
		EntityNPCInterface.GenericPlayer = new FakePlayer(event.getServer().getWorld(0), (GameProfile) EntityNPCInterface.GenericProfile);
		for (WorldServer world : CustomNpcs.Server.worlds) {
			ServerScoreboard board = (ServerScoreboard) world.getScoreboard();
			board.addDirtyRunnable(() -> {
				Iterator<String> iterator = Availability.scores.iterator();
				while (iterator.hasNext()) {
					ScoreObjective so = board.getObjective(iterator.next());
					if (so != null) {
						Iterator<EntityPlayerMP> iterator2 = CustomNpcs.Server.getPlayerList().getPlayers().iterator();
						while (iterator2.hasNext()) {
							EntityPlayerMP player = iterator2.next();
							if (!board.entityHasObjective(player.getName(), so) && board.getObjectiveDisplaySlotCount(so) == 0) {
								player.connection.sendPacket(new SPacketScoreboardObjective(so, 0));
							}
							player.connection.sendPacket(new SPacketUpdateScore(board.getOrCreateScore(player.getName(), so)));
						}
					}
				}
				return;
			});
			board.addDirtyRunnable(() -> {
				Iterator<EntityPlayerMP> itrPlayer = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayers().iterator();
				while (itrPlayer.hasNext()) {
					EntityPlayerMP player = itrPlayer.next();
					PlayerData data = PlayerData.get(player);
					if (data!=null) {
						VisibilityController.onUpdate(player);
					}
				}
				return;
			});
		}
		DimensionHandler.getInstance().loadDimensions();
	}

	@Mod.EventHandler
	public void setAboutToStart(FMLServerAboutToStartEvent event) {
		CustomNpcs.Server = event.getServer();
		ChunkController.instance.clear();
		FactionController.instance.load();
		ScriptController.Instance.load();
		
		new DropController();
		new RecipeController();
		new AnimationController();
		new KeyController();
		new TransportController();
		
		new PlayerDataController();
		new GlobalDataController();
		new SpawnController();
		new LinkedNpcController();
		new MassBlockController();;
		new VisibilityController();
		
		WrapperNpcAPI.clearCache();
		Set<ResourceLocation> names = Block.REGISTRY.getKeys();
		for (ResourceLocation name : names) {
			Block block = Block.REGISTRY.getObject(name);
			if (block instanceof BlockLeaves) {
				block.setTickRandomly(CustomNpcs.LeavesDecayEnabled);
			}
			if (block instanceof BlockVine) {
				block.setTickRandomly(CustomNpcs.VineGrowthEnabled);
			}
			if (block instanceof BlockIce) {
				block.setTickRandomly(CustomNpcs.IceMeltsEnabled);
			}
		}
	}

	@Mod.EventHandler
	public void started(FMLServerStartedEvent event) {
		new BankController();
		new MarcetController();
		new BorderController();
		DialogController.instance.load();
		QuestController.instance.load();
		ScriptController.HasStart = true;
		ServerCloneController.Instance = new ServerCloneController();
		ScriptController.Instance.loadItemTextures();
	}

	@Mod.EventHandler
	public void stopped(FMLServerStoppedEvent event) {
		ServerCloneController.Instance = null;
		MarcetController.getInstance().saveMarcets();
		RecipeController.getInstance().save();
		AnimationController.getInstance().save();
		KeyController.getInstance().save();
		DropController.getInstance().save();
		ScriptController.Instance.saveItemTextures();
		ItemScripted.Resources.clear();
		CustomNpcs.Server = null;
	}

	public static void showDebugs() {
		if (!CustomNpcs.VerboseDebug) { return; }
		LogWriter.debug("Debug Server Datas Event: { [Target name, Runs, Average time] }");
		CustomNpcs.debugData.stopAll();
		for (String side : CustomNpcs.debugData.data.keySet()) {
			LogWriter.debug("Showing Monitoring results for the "+side+" side. |Number - EventName: { [Target name, Runs, Average time] }|:");
			List<String> events = Lists.newArrayList(CustomNpcs.debugData.data.get(side).times.keySet());
			Collections.sort(events);
			int i = 0;
			long max = Long.MIN_VALUE;
			String[] maxName = new String[] { "", "" };
			for (String eventName : events) {
				Debug dd = CustomNpcs.debugData.data.get(side);
				List<String> targets = Lists.newArrayList(CustomNpcs.debugData.data.get(side).times.get(eventName).keySet());
				Collections.sort(targets);
				String log = "";
				for (String target : targets) {
					Long[] time = CustomNpcs.debugData.data.get(side).times.get(eventName).get(target);
					if (log.length()>0) { log += "; "; }
					if (time[0]<=0) { time[0] = 1L; }
					log += "["+target+", "+time[0]+", "+AdditionalMethods.ticksToElapsedTime(time[1], true, false, false)+"]";
					if (time[1]==dd.max) { maxName[0] = "\""+eventName+"|"+target+"\": "+AdditionalMethods.ticksToElapsedTime(dd.max, true, false, false); }
					if (max<time[0]) { max = time[0]; maxName[1] = "\""+eventName+"|"+target+"\": "+time[0]+" runs"; }
				}
				if (!log.isEmpty()) { LogWriter.debug("["+(i+1)+"/"+targets.size()+"] - \""+eventName+"\": { "+log+" }"); }
				i++;
			}
			LogWriter.debug(side+" a long time ["+maxName[0]+"]; Most often: ["+maxName[1]+"]");
		}
	}

}
