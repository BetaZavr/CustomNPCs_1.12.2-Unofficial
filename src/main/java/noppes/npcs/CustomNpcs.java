package noppes.npcs;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import net.minecraft.network.play.server.SPacketScoreboardObjective;
import net.minecraft.network.play.server.SPacketUpdateScore;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
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
import noppes.npcs.api.event.potion.AffectEntity;
import noppes.npcs.api.event.potion.EndEffect;
import noppes.npcs.api.event.potion.IsReadyEvent;
import noppes.npcs.api.event.potion.PerformEffect;
import noppes.npcs.api.handler.capability.IItemStackWrapperHandler;
import noppes.npcs.api.handler.capability.IMarkDataHandler;
import noppes.npcs.api.handler.capability.IPlayerDataHandler;
import noppes.npcs.api.handler.capability.IWrapperEntityDataHandler;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.api.wrapper.WrapperEntityData;
import noppes.npcs.api.wrapper.WrapperNpcAPI;
import noppes.npcs.capability.ItemStackWrapperStorage;
import noppes.npcs.capability.MarkDataStorage;
import noppes.npcs.capability.PlayerDataStorage;
import noppes.npcs.capability.WrapperEntityDataStorage;
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
	@ConfigProp(info = "Script password. Necessary for decrypting scripts")
	public static String ScriptPassword = UUID.randomUUID().toString().replace("-", "");
	@ConfigProp(info = "Enables CustomNpcs startup update message")
	public static boolean EnableUpdateChecker = true;
	@ConfigProp(info = "Maximum and minimum amount of experience dropped from the NPC for the minimum and maximum level (Elite x1.75; Boss x4.75)")
	public static int[] experience = new int[] { 2, 3, 100, 115 };
	@ConfigProp(info = "Font size for custom fonts (doesn't work with minecrafts font)")
	public static int FontSize = 18;

	@ConfigProp(info = "Main text color of elements in GUI modification")
	public static int mainColor = 0xFFFFFFFF;
	@ConfigProp(info = "Name text color in GUI modification")
	public static int lableColor = 0xFF404040;
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
	@ConfigProp(info = "Show description when hovering cursor on over GUI elements")
	public static boolean showDescriptions = true;
	@ConfigProp(info = "Show Debug")
	public static boolean VerboseDebug = false;
	@ConfigProp(info = "Enables Vine Growth")
	public static boolean VineGrowthEnabled = true;
	@ConfigProp(info = "Maximum blocks to install per second with the Builder item")
	public static int maxBuilderBlocks = 10000;
	@ConfigProp(info = "Color of Script code elements. "+((char) 167)+"[Numbers, Functions, Strings, Comments]")
	public static String[] charCodeColor = new String[] { "6", "9", "7", "2" };
	@ConfigProp(info = "Maximum number of items in one Drop group")
	public static int maxItemInDropsNPC = 32;
	@ConfigProp(info = "Cancel the creation of variables in each Forge event (saves FPS)")
	public static boolean simplifiedForgeEvents = false;
	@ConfigProp(info = "NPC scenes can be activated using special keys")
	public static boolean SceneButtonsEnabled = true;
	@ConfigProp(info="NPC speech can trigger a chat event")
	public static boolean NpcSpeachTriggersChatEvent = false;
	@ConfigProp(info = "Show faction, quest and compass tabs in player inventory")
	public static boolean InventoryGuiEnabled = true;
	@ConfigProp(info = "Used only when migrating from older versions of modification 1.12.2 and lower")
	public static boolean FixUpdateFromPre_1_12 = true;
	@ConfigProp(info = "Summon a new NPC with random custom eyes")
	public static boolean EnableDefaultEyes = true;
	@ConfigProp(info = "Time in real days when the letter will be deleted from the player (-1 = never, at least 1 day, max 60)")
	public static int mailTimeWhenLettersWillBeDeleted = 30;
	@ConfigProp(info = "Time in seconds when a player can receive a letter [min not less than 10, max not more than 3600]")
	public static int[] mailTimeWhenLettersWillBeReceived = new int[] { 120, 300 };
	@ConfigProp(info = "Cost for sending a letter in game currency. [base send, one page, one stack of item, percentage of currency, redemption percentage]")
	public static int[] mailCostSendingLetter = new int[] { 10, 5, 30, 2, 4 };
	@ConfigProp(info = "Can players send themselves letters?")
	public static boolean mailSendToYourself = false;
	@ConfigProp(info = "Position on the screen of the icon indicating the presence of new messages (-1 = do not show, then from 0 to 3)")
	public static int mailWindow = 1;
	@ConfigProp(info = "Maximum number of tabs for scripts (from 1 to 20) Recommended: 5")
	public static int scriptMaxTabs = 10;
	@ConfigProp(info = "The speed for dialogs that show individual letters. (number per second from 10 to 100)")
	public static int dialogShowFitsSpeed = 30;
	@ConfigProp(info = "123456")
	public static int colorAnimHoverPart = 0xFA7800;
	
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

	@Mod.EventHandler
	public void preload(FMLPreInitializationEvent ev) {
		CustomNpcs.debugData.startDebug("Common", "Mod", "CustomNpcs_preload");
		CustomNpcs.Channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("CustomNPCs");
		CustomNpcs.ChannelPlayer = NetworkRegistry.INSTANCE.newEventDrivenChannel("CustomNPCsPlayer");
		(CustomNpcs.Dir = new File(new File(ev.getModConfigurationDirectory(), ".."), "customnpcs")).mkdir();
		(CustomNpcs.Config = new ConfigLoader(this.getClass(), ev.getModConfigurationDirectory(), "CustomNpcs"))
				.loadConfig();
		if (CustomNpcs.NpcNavRange < 16) { CustomNpcs.NpcNavRange = 16; }
		CustomRegisters.load();
		// Capabilities
		CapabilityManager.INSTANCE.register(IPlayerDataHandler.class, new PlayerDataStorage(), PlayerData::new);
		CapabilityManager.INSTANCE.register(IMarkDataHandler.class, new MarkDataStorage(), MarkData::new);
		CapabilityManager.INSTANCE.register(IWrapperEntityDataHandler.class, new WrapperEntityDataStorage(), WrapperEntityData::new);
		CapabilityManager.INSTANCE.register(IItemStackWrapperHandler.class, new ItemStackWrapperStorage(), ItemStackWrapper::new);

		NetworkRegistry.INSTANCE.registerGuiHandler(this, CustomNpcs.proxy);
		MinecraftForge.EVENT_BUS.register(new ServerEventsHandler());
		MinecraftForge.EVENT_BUS.register(new ServerTickHandler());
		MinecraftForge.EVENT_BUS.register(CustomNpcs.proxy);
		NpcAPI.Instance().events().register(new AbilityEventHandler());
		ForgeChunkManager.setForcedChunkLoadingCallback(this, (ForgeChunkManager.LoadingCallback) new ChunkController());
		
		CustomNpcs.customDimensionType = DimensionType.register("CustomDimensions", "CustomNpcs", "CustomDimensions".hashCode(), CustomWorldProvider.class, false);

		CustomNpcs.proxy.preload();
		ObfuscationHelper.setValue(RangedAttribute.class, (RangedAttribute) SharedMonsterAttributes.MAX_HEALTH, Double.MAX_VALUE, 1);
		CustomNpcs.debugData.endDebug("Common", "Mod", "CustomNpcs_preload");
	}

	@Mod.EventHandler
	public void load(FMLInitializationEvent ev) {
		CustomNpcs.debugData.startDebug("Common", "Mod", "CustomNpcs_load");
		PixelmonHelper.load();
		ScriptController controller = new ScriptController();
		if (CustomNpcs.EnableScripting && controller.languages.size() > 0) {
			MinecraftForge.EVENT_BUS.register(controller);
			MinecraftForge.EVENT_BUS.register(new PlayerEventHandler().registerForgeEvents(ev.getSide()));
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
		CustomNpcs.debugData.endDebug("Common", "Mod", "CustomNpcs_load");
	}
	
	@Mod.EventHandler
	public static void postload(FMLPostInitializationEvent ev) { // New
		CustomNpcs.debugData.startDebug("Common", "Mod", "CustomNpcs_postload");
		if (maxLv < 1) { maxLv = 1; }
		else if (maxLv > 999) { maxLv = 999; }
		if (maxBuilderBlocks < 20) { maxBuilderBlocks = 20; }
		else if (maxBuilderBlocks > 25000) { maxBuilderBlocks = 25000; }
		if (maxItemInDropsNPC < 5) { maxItemInDropsNPC = 5; }
		try { CustomNpcs.charCurrencies = new String(Character.toChars(Integer.parseInt(CustomNpcs.charCurrencies))); }
		catch (Exception e) { if (charCurrencies.length()>=1) { charCurrencies = new String(Character.toChars(0x20AC)); } }
		if (damageBoss.length != 4) { damageBoss = new int[] { 8, 52, 6, 26 }; }
		if (damageElite.length != 4) { damageElite = new int[] { 6, 32, 3, 16 }; }
		if (damageNormal.length != 4) { damageNormal = new int[] { 4, 22, 2, 11 }; }
		if (experience.length != 4) { experience = new int[] { 2, 3, 100, 115 }; }
		if (chatNpcColors.length != 3) { chatNpcColors = new int[] { 0x000000, 0x000000, 0xFFFFFF }; }
		if (chatPlayerColors.length != 3) { chatPlayerColors = new int[] { 0x000000, 0x2C4C00, 0xE0FFB0 }; }
		if (healthBoss.length != 2) { healthBoss = new int[] { 250, 20000 }; }
		if (healthElite.length != 2) { healthElite = new int[] { 60, 1200 }; }
		if (healthNormal.length != 2) { healthNormal = new int[] { 20, 500 }; }
		if (modelRaritySize.length != 3) { modelRaritySize = new int[] { 5, 6, 7 }; }
		if (resistanceBoss.length != 4) { resistanceBoss = new int[] { 110, 125, 175, 195 }; }
		if (resistanceElite.length != 4) { resistanceElite = new int[] { 105, 110, 130, 150 }; }
		if (resistanceNormal.length != 4) { resistanceNormal = new int[] { 100, 100, 100, 110 }; }
		if (charCodeColor.length != 4) { charCodeColor = new String[] { "6", "9", "7", "2" }; }
		if (mailTimeWhenLettersWillBeDeleted < -1) { mailTimeWhenLettersWillBeDeleted = -1; }
		else if (mailTimeWhenLettersWillBeDeleted < 1) { mailTimeWhenLettersWillBeDeleted = 1; }
		else if (mailTimeWhenLettersWillBeDeleted > 60) { mailTimeWhenLettersWillBeDeleted = 60; }
		if (mailTimeWhenLettersWillBeReceived[0] > mailTimeWhenLettersWillBeReceived[1]) {
			int m = new Integer(mailTimeWhenLettersWillBeReceived[0]);
			mailTimeWhenLettersWillBeReceived[0] = mailTimeWhenLettersWillBeReceived[1];
			mailTimeWhenLettersWillBeReceived[1] = m;
		}
		if (mailTimeWhenLettersWillBeReceived[0] < 10) { mailTimeWhenLettersWillBeReceived[0] = 10; }
		if (mailTimeWhenLettersWillBeReceived[1] > 3600) { mailTimeWhenLettersWillBeReceived[1] = 3600; }
		if (mailWindow < -1) { mailWindow = -1; } else if (mailWindow > 3) { mailWindow = 3; }
		if (mailCostSendingLetter[0] < 0) { mailCostSendingLetter[0] = 0; }
		if (mailCostSendingLetter[1] < 0) { mailCostSendingLetter[1] = 0; }
		if (mailCostSendingLetter[2] < 0) { mailCostSendingLetter[2] = 0; }
		if (mailCostSendingLetter[3] < 0) { mailCostSendingLetter[3] = 0; }
		else if (mailCostSendingLetter[3] > 500) { mailCostSendingLetter[3] = 500; }
		if (mailCostSendingLetter[4] < 0) { mailCostSendingLetter[4] = 0; }
		else if (mailCostSendingLetter[4] > 500) { mailCostSendingLetter[4] = 500; }
		if (dialogShowFitsSpeed < 10) { dialogShowFitsSpeed = 10; }
		else if (dialogShowFitsSpeed > 100) { dialogShowFitsSpeed = 100; }
		CustomNpcs.proxy.postload();
		
		new AdditionalMethods();
		for (ModContainer mod : Loader.instance().getModList()) {
			if (mod.getModId().equals(CustomNpcs.MODID)) { CustomNpcs.mod = mod; }
		}
		forgeClientEventNames.put(IsReadyEvent.class, "customPotionIsReady");
		forgeClientEventNames.put(PerformEffect.class, "customPotionPerformEffect");
		forgeClientEventNames.put(AffectEntity.class, "customPotionAffectEntity");
		forgeClientEventNames.put(EndEffect.class, "customPotionEndEffect");
		LogWriter.info("Mod loaded ^_^ Have a good game!");
		CustomNpcs.debugData.endDebug("Common", "Mod", "CustomNpcs_postload");
	}

	@Mod.EventHandler
	public void serverstart(FMLServerStartingEvent event) {
		CustomNpcs.debugData.startDebug("Common", "Mod", "CustomNpcs_serverstart");
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
		CustomNpcs.debugData.endDebug("Common", "Mod", "CustomNpcs_serverstart");
	}

	@Mod.EventHandler
	public void setAboutToStart(FMLServerAboutToStartEvent event) {
		CustomNpcs.debugData.startDebug("Common", "Mod", "CustomNpcs_setAboutToStart");
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
		CustomNpcs.debugData.endDebug("Common", "Mod", "CustomNpcs_setAboutToStart");
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
		BankController.getInstance().update();
		if (CustomNpcs.VerboseDebug) { CustomNpcs.showDebugs(); }
		CustomNpcs.Server = null;
	}

	public static List<String> showDebugs() {
		List<String> list = Lists.newArrayList();
		String temp = "Debug information output:";
		list.add(temp);
		LogWriter.debug(temp);
		CustomNpcs.debugData.stopAll();
		boolean start = false;
		for (String side : CustomNpcs.debugData.data.keySet()) {
			if (start) {
				list.add("----   ----  ----");
				LogWriter.debug("");
			}
			temp = "Showing Monitoring results for \""+side+"\" side. |Number - EventName: { [Target name, Runs, Average time] }|:";
			list.add(temp);
			LogWriter.debug(temp);
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
				temp = "["+(i+1)+"/"+events.size()+"] - \""+eventName+"\": { "+log+" }";
				list.add(temp);
				LogWriter.debug(temp);
				i++;
			}
			temp = "\""+side+"\" a long time ["+maxName[0]+"]";
			list.add(temp);
			LogWriter.debug(temp);
			temp = "\""+side+"\" most often: ["+maxName[1]+"]";
			list.add(temp);
			LogWriter.debug(temp);
			start = true;
		}
		return list;
	}

}
