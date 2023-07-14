package noppes.npcs.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;

import javax.imageio.ImageIO;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import micdoodle8.mods.galacticraft.api.client.tabs.InventoryTabFactions;
import micdoodle8.mods.galacticraft.api.client.tabs.InventoryTabQuests;
import micdoodle8.mods.galacticraft.api.client.tabs.InventoryTabVanilla;
import micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFlame;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.Locale;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraft.world.World;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import noppes.npcs.CommonProxy;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartData;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.PacketHandlerPlayer;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.api.item.IItemScripted;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.blocks.CustomBlock;
import noppes.npcs.blocks.CustomBlockPortal;
import noppes.npcs.blocks.CustomBlockSlab;
import noppes.npcs.blocks.CustomBlockStairs;
import noppes.npcs.blocks.CustomLiquid;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.controllers.PresetController;
import noppes.npcs.client.fx.EntityEnderFX;
import noppes.npcs.client.gui.GuiBlockBuilder;
import noppes.npcs.client.gui.GuiBlockCopy;
import noppes.npcs.client.gui.GuiBorderBlock;
import noppes.npcs.client.gui.GuiBoundarySetting;
import noppes.npcs.client.gui.GuiBuilderSetting;
import noppes.npcs.client.gui.GuiHelpBook;
import noppes.npcs.client.gui.GuiMerchantAdd;
import noppes.npcs.client.gui.GuiNbtBook;
import noppes.npcs.client.gui.GuiNpcDimension;
import noppes.npcs.client.gui.GuiNpcMobSpawner;
import noppes.npcs.client.gui.GuiNpcMobSpawnerMounter;
import noppes.npcs.client.gui.GuiNpcPather;
import noppes.npcs.client.gui.GuiNpcRedstoneBlock;
import noppes.npcs.client.gui.GuiNpcRemoteEditor;
import noppes.npcs.client.gui.GuiNpcWaypoint;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.dimentions.GuiCreateDimension;
import noppes.npcs.client.gui.global.GuiNPCManageBanks;
import noppes.npcs.client.gui.global.GuiNPCManageDialogs;
import noppes.npcs.client.gui.global.GuiNPCManageFactions;
import noppes.npcs.client.gui.global.GuiNPCManageLinkedNpc;
import noppes.npcs.client.gui.global.GuiNPCManageMarcets;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.global.GuiNPCManageRecipes;
import noppes.npcs.client.gui.global.GuiNPCManageTransporters;
import noppes.npcs.client.gui.global.GuiNpcQuestReward;
import noppes.npcs.client.gui.mainmenu.GuiDropEdit;
import noppes.npcs.client.gui.mainmenu.GuiNPCGlobalMainMenu;
import noppes.npcs.client.gui.mainmenu.GuiNPCInv;
import noppes.npcs.client.gui.mainmenu.GuiNpcAI;
import noppes.npcs.client.gui.mainmenu.GuiNpcAdvanced;
import noppes.npcs.client.gui.mainmenu.GuiNpcDisplay;
import noppes.npcs.client.gui.mainmenu.GuiNpcStats;
import noppes.npcs.client.gui.player.GuiCustomChest;
import noppes.npcs.client.gui.player.GuiMailbox;
import noppes.npcs.client.gui.player.GuiMailmanWrite;
import noppes.npcs.client.gui.player.GuiNPCBankChest;
import noppes.npcs.client.gui.player.GuiNPCTrader;
import noppes.npcs.client.gui.player.GuiNpcCarpentryBench;
import noppes.npcs.client.gui.player.GuiNpcFollower;
import noppes.npcs.client.gui.player.GuiNpcFollowerHire;
import noppes.npcs.client.gui.player.GuiNpcQuestRewardItem;
import noppes.npcs.client.gui.player.GuiTransportSelection;
import noppes.npcs.client.gui.player.companion.GuiNpcCompanionInv;
import noppes.npcs.client.gui.player.companion.GuiNpcCompanionStats;
import noppes.npcs.client.gui.player.companion.GuiNpcCompanionTalents;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeItem;
import noppes.npcs.client.gui.roles.GuiNpcBankSetup;
import noppes.npcs.client.gui.roles.GuiNpcFollowerSetup;
import noppes.npcs.client.gui.roles.GuiNpcItemGiver;
import noppes.npcs.client.gui.roles.GuiNpcTransporter;
import noppes.npcs.client.gui.script.GuiScript;
import noppes.npcs.client.gui.script.GuiScriptBlock;
import noppes.npcs.client.gui.script.GuiScriptDoor;
import noppes.npcs.client.gui.script.GuiScriptGlobal;
import noppes.npcs.client.gui.script.GuiScriptItem;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.client.model.ModelBipedAlt;
import noppes.npcs.client.model.ModelClassicPlayer;
import noppes.npcs.client.model.ModelNPCGolem;
import noppes.npcs.client.model.ModelNpcCrystal;
import noppes.npcs.client.model.ModelNpcDragon;
import noppes.npcs.client.model.ModelNpcSlime;
import noppes.npcs.client.model.ModelPlayerAlt;
import noppes.npcs.client.renderer.RenderCustomNpc;
import noppes.npcs.client.renderer.RenderNPCInterface;
import noppes.npcs.client.renderer.RenderNPCPony;
import noppes.npcs.client.renderer.RenderNpcCrystal;
import noppes.npcs.client.renderer.RenderNpcDragon;
import noppes.npcs.client.renderer.RenderNpcSlime;
import noppes.npcs.client.renderer.RenderProjectile;
import noppes.npcs.config.TrueTypeFont;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.containers.ContainerBuilderSettings;
import noppes.npcs.containers.ContainerCarpentryBench;
import noppes.npcs.containers.ContainerCustomChest;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.containers.ContainerManageBanks;
import noppes.npcs.containers.ContainerManageRecipes;
import noppes.npcs.containers.ContainerNPCBankInterface;
import noppes.npcs.containers.ContainerNPCCompanion;
import noppes.npcs.containers.ContainerNPCDropSetup;
import noppes.npcs.containers.ContainerNPCFollower;
import noppes.npcs.containers.ContainerNPCFollowerHire;
import noppes.npcs.containers.ContainerNPCFollowerSetup;
import noppes.npcs.containers.ContainerNPCInv;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.containers.ContainerNPCTraderSetup;
import noppes.npcs.containers.ContainerNpcItemGiver;
import noppes.npcs.containers.ContainerNpcQuestReward;
import noppes.npcs.containers.ContainerNpcQuestRewardItem;
import noppes.npcs.containers.ContainerNpcQuestTypeItem;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerGameData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPC64x32;
import noppes.npcs.entity.EntityNPCGolem;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityNpcAlex;
import noppes.npcs.entity.EntityNpcClassicPlayer;
import noppes.npcs.entity.EntityNpcCrystal;
import noppes.npcs.entity.EntityNpcDragon;
import noppes.npcs.entity.EntityNpcPony;
import noppes.npcs.entity.EntityNpcSlime;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.items.CustomArmor;
import noppes.npcs.items.CustomBow;
import noppes.npcs.items.CustomFishingRod;
import noppes.npcs.items.CustomFood;
import noppes.npcs.items.CustomShield;
import noppes.npcs.items.CustomTool;
import noppes.npcs.items.CustomWeapon;
import noppes.npcs.particles.CustomParticle;
import noppes.npcs.util.ObfuscationHelper;

@SuppressWarnings("deprecation")
public class ClientProxy
extends CommonProxy {
	
	public static final Map<CreativeTabs, List<RecipeList>> MOD_RECIPES_BY_TAB = Maps.<CreativeTabs, List<RecipeList>>newHashMap();
	public static KeyBinding frontButton = (KeyBinding) ObfuscationHelper.getValue(GameSettings.class, Minecraft.getMinecraft().gameSettings, 55); // w
	public static KeyBinding leftButton = (KeyBinding) ObfuscationHelper.getValue(GameSettings.class, Minecraft.getMinecraft().gameSettings, 56); // a
	public static KeyBinding backButton = (KeyBinding) ObfuscationHelper.getValue(GameSettings.class, Minecraft.getMinecraft().gameSettings, 57); // s
	public static KeyBinding rightButton = (KeyBinding) ObfuscationHelper.getValue(GameSettings.class, Minecraft.getMinecraft().gameSettings, 58); // d
	public static KeyBinding jumpButton = (KeyBinding) ObfuscationHelper.getValue(GameSettings.class, Minecraft.getMinecraft().gameSettings, 59); // space
	public static Map<Integer, List<UUID>> notVisibleNPC = Maps.<Integer, List<UUID>>newHashMap();
	public static FontContainer Font;
	public static PlayerData playerData = new PlayerData();
	public static KeyBinding QuestLog;
	public static String recipeGroup, recipeName;
	public static KeyBinding Scene1;
	public static KeyBinding Scene2;
	public static KeyBinding Scene3;
	public static KeyBinding SceneReset;
	public static Map<String, Map<String, TreeMap<ResourceLocation, Long>>> texturesData = Maps.<String, Map<String, TreeMap<ResourceLocation, Long>>>newHashMap();
	
	public static class FontContainer {
		private TrueTypeFont textFont;
		public boolean useCustomFont;

		private FontContainer() {
			this.textFont = null;
			this.useCustomFont = true;
		}

		public FontContainer(String fontType, int fontSize) {
			this.textFont = null;
			this.useCustomFont = true;
			this.textFont = new TrueTypeFont(new Font(fontType, 0, fontSize), 1.0f);
			this.useCustomFont = !fontType.equalsIgnoreCase("minecraft");
			try {
				if (!this.useCustomFont || fontType.isEmpty() || fontType.equalsIgnoreCase("default")) {
					this.textFont = new TrueTypeFont(new ResourceLocation(CustomNpcs.MODID, "opensans.ttf"), fontSize,
							1.0f);
				}
			} catch (Exception e) {
				LogWriter.info("Failed loading font so using Arial");
			}
		}

		public void clear() {
			if (this.textFont != null) {
				this.textFont.dispose();
			}
		}

		public FontContainer copy() {
			FontContainer font = new FontContainer();
			font.textFont = this.textFont;
			font.useCustomFont = this.useCustomFont;
			return font;
		}

		public void drawString(String text, int x, int y, int color) {
			if (this.useCustomFont) {
				this.textFont.draw(text, x, y, color);
			} else {
				Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(text, x, y, color);
			}
		}

		public String getName() {
			if (!this.useCustomFont) {
				return "Minecraft";
			}
			return this.textFont.getFontName();
		}

		public int height(String text) {
			if (this.useCustomFont) {
				return this.textFont.height(text);
			}
			return Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT;
		}

		public int width(String text) {
			if (this.useCustomFont) {
				return this.textFont.width(text);
			}
			return Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
		}
	}

	public static void bindTexture(ResourceLocation location) {
		try {
			if (location == null) {
				return;
			}
			TextureManager manager = Minecraft.getMinecraft().getTextureManager();
			ITextureObject ob = manager.getTexture(location);
			if (ob == null) {
				ob = new SimpleTexture(location);
				manager.loadTexture(location, ob);
			}
			GlStateManager.bindTexture(ob.getGlTextureId());
		} catch (NullPointerException | ReportedException ex) { }
	}

	private void createFolders() {
		File file = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID);
		if (!file.exists()) {
			file.mkdirs();
		}
		File check = new File(file, "sounds");
		if (!check.exists()) {
			check.mkdir();
		}
		File json = new File(file, "sounds.json");
		if (!json.exists()) {
			try {
				json.createNewFile();
				BufferedWriter writer = new BufferedWriter(new FileWriter(json));
				writer.write("{\n\n}");
				writer.close();
			} catch (IOException ex) { }
		}
		check = new File(file, "textures");
		if (!check.exists()) {
			check.mkdir();
		}
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID > EnumGuiType.values().length) {
			return null;
		}
		EnumGuiType gui = EnumGuiType.values()[ID];
		EntityNPCInterface npc = NoppesUtil.getLastNpc();
		Container container = this.getContainer(gui, player, x, y, z, npc);
		return this.getGui(npc, gui, container, x, y, z);
	}

	private GuiScreen getGui(EntityNPCInterface npc, EnumGuiType gui, Container container, int x, int y, int z) { // Changed
		switch (gui) {
			case CustomChest: { return new GuiCustomChest((ContainerCustomChest) container); }
			case MainMenuDisplay: {
				if (npc != null) {
					return new GuiNpcDisplay(npc);
				}
				this.getPlayer().sendMessage(new TextComponentString("Unable to find npc"));
			}
			case MainMenuStats: { return new GuiNpcStats(npc); }
			case MainMenuInv: { return new GuiNPCInv(npc, (ContainerNPCInv) container); }
			case MainMenuInvDrop: { return new GuiDropEdit(npc, (ContainerNPCDropSetup) container, (GuiContainer) Minecraft.getMinecraft().currentScreen, x, y, z); } // New
			case MainMenuAdvanced: { return new GuiNpcAdvanced(npc); }
			case QuestReward: { return new GuiNpcQuestReward(npc, (ContainerNpcQuestReward) container); }
			case QuestTypeItem: { // New
				Quest quest = NoppesUtilServer.getEditingQuest(getPlayer());
				if (quest != null && quest.questInterface.tasks[x].getEnumType() == EnumQuestTask.ITEM
						|| quest.questInterface.tasks[x].getEnumType() == EnumQuestTask.CRAFT) {
					return new GuiNpcQuestTypeItem(npc, (ContainerNpcQuestTypeItem) container,
							quest.questInterface.tasks[x]);
				}
				return null;
			}
			case QuestRewardItem: { return new GuiNpcQuestRewardItem((ContainerNpcQuestRewardItem) container, x); }
			case MovingPath: { return new GuiNpcPather(npc); }
			case ManageFactions: { return new GuiNPCManageFactions(npc); }
			case ManageLinked: { return new GuiNPCManageLinkedNpc(npc); }
			case BuilderBlock: { return new GuiBlockBuilder(x, y, z); }
			case ManageTransport: { return new GuiNPCManageTransporters(npc); }
			case ManageRecipes: { return new GuiNPCManageRecipes(npc, (ContainerManageRecipes) container); }
			case ManageDialogs: { return new GuiNPCManageDialogs(npc); }
			case ManageQuests: { return new GuiNPCManageQuest(npc); }
			case ManageBanks: { return new GuiNPCManageBanks(npc, (ContainerManageBanks) container); 	}
			case MainMenuGlobal: { return new GuiNPCGlobalMainMenu(npc); }
			case MainMenuAI: { return new GuiNpcAI(npc); }
			case PlayerAnvil: { return new GuiNpcCarpentryBench((ContainerCarpentryBench) container); }
			case PlayerFollowerHire: { return new GuiNpcFollowerHire(npc, (ContainerNPCFollowerHire) container); }
			case PlayerFollower: { return new GuiNpcFollower(npc, (ContainerNPCFollower) container); }
			case PlayerTrader: { return new GuiNPCTrader(npc, (ContainerNPCTrader) container); }
			case PlayerBankSmall: { return new GuiNPCBankChest(npc, (ContainerNPCBankInterface) container); }
			case PlayerBankUnlock: { return new GuiNPCBankChest(npc, (ContainerNPCBankInterface) container); }
			case PlayerBankUprade: { return new GuiNPCBankChest(npc, (ContainerNPCBankInterface) container); }
			case PlayerBankLarge: { return new GuiNPCBankChest(npc, (ContainerNPCBankInterface) container); }
			case PlayerTransporter: { return new GuiTransportSelection(npc); }
			case Script: { return new GuiScript(npc); }
			case ScriptBlock: { return new GuiScriptBlock(x, y, z); }
			case ScriptItem: { return new GuiScriptItem(getPlayer()); }
			case ScriptDoor: { return new GuiScriptDoor(x, y, z); }
			case ScriptPlayers: { return new GuiScriptGlobal(); }
			case SetupFollower: { return new GuiNpcFollowerSetup(npc, (ContainerNPCFollowerSetup) container); }
			case SetupItemGiver: { return new GuiNpcItemGiver(npc, (ContainerNpcItemGiver) container); }
			case SetupTrader: { return new GuiNPCManageMarcets(npc, (ContainerNPCTraderSetup) container); }
			case SetupTransporter: { return new GuiNpcTransporter(npc); }
			case SetupBank: { return new GuiNpcBankSetup(npc); }
			case NpcRemote: {
				if (Minecraft.getMinecraft().currentScreen == null) { return new GuiNpcRemoteEditor(); }
				return null;
			}
			case PlayerMailman: { return new GuiMailmanWrite((ContainerMail) container, x == 1, y == 1); }
			case PlayerMailbox: { return new GuiMailbox(); }
			case MerchantAdd: { return new GuiMerchantAdd(); }
			case NpcDimensions: { return new GuiNpcDimension(); }
			case Border: { return new GuiBorderBlock(x, y, z); }
			case RedstoneBlock: { return new GuiNpcRedstoneBlock(x, y, z); }
			case MobSpawner: { return new GuiNpcMobSpawner(x, y, z); }
			case CopyBlock: { return new GuiBlockCopy(x, y, z); }
			case MobSpawnerMounter: { return new GuiNpcMobSpawnerMounter(x, y, z); }
			case Waypoint: { return new GuiNpcWaypoint(x, y, z); }
			case Companion: { return new GuiNpcCompanionStats(npc); }
			case CompanionTalent: { return new GuiNpcCompanionTalents(npc); }
			case CompanionInv: { return new GuiNpcCompanionInv(npc, (ContainerNPCCompanion) container); }
			case NbtBook: { return new GuiNbtBook(x, y, z); }
			case CustomGui: { return new GuiCustom((ContainerCustomGui) container); }
			case HelpBook: { return new GuiHelpBook(); }
			case BoundarySetting: { return new GuiBoundarySetting(x, y); }
			case BuilderSetting: { return new GuiBuilderSetting((ContainerBuilderSettings) container, x); }
			case DimentionSetting: { return new GuiCreateDimension(x); }
			default: { return null; }
		}
	}

	@Override
	public EntityPlayer getPlayer() {
		return Minecraft.getMinecraft().player;
	}

	@Override
	public PlayerData getPlayerData(EntityPlayer player) {
		if (ClientProxy.playerData.player != player) { ClientProxy.playerData.player = player; }
		return ClientProxy.playerData;
	}

	@Override
	public boolean hasClient() {
		return true;
	}

	@Override
	public void preload() {
		ClientProxy.Font = new FontContainer(CustomNpcs.FontType, CustomNpcs.FontSize);
		this.createFolders();
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager())
				.registerReloadListener((IResourceManagerReloadListener) new CustomNpcResourceListener());
		CustomNpcs.Channel.register(new PacketHandlerClient());
		CustomNpcs.ChannelPlayer.register(new PacketHandlerPlayer());
		new MusicController();
		MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
		MinecraftForge.EVENT_BUS.register(new ClientGuiEventHandler());
		Minecraft mc = Minecraft.getMinecraft();
		ClientProxy.QuestLog = new KeyBinding("Quest Log", 38, "key.categories.gameplay");
		if (CustomNpcs.SceneButtonsEnabled) {
			ClientProxy.Scene1 = new KeyBinding("Scene1 start/pause", 79, "key.categories.gameplay");
			ClientProxy.Scene2 = new KeyBinding("Scene2 start/pause", 80, "key.categories.gameplay");
			ClientProxy.Scene3 = new KeyBinding("Scene3 start/pause", 81, "key.categories.gameplay");
			ClientProxy.SceneReset = new KeyBinding("Scene reset", 82, "key.categories.gameplay");
			ClientRegistry.registerKeyBinding(ClientProxy.Scene1);
			ClientRegistry.registerKeyBinding(ClientProxy.Scene2);
			ClientRegistry.registerKeyBinding(ClientProxy.Scene3);
			ClientRegistry.registerKeyBinding(ClientProxy.SceneReset);
		}
		ClientRegistry.registerKeyBinding(ClientProxy.QuestLog);
		mc.gameSettings.loadOptions();
		new PresetController(CustomNpcs.Dir);
		if (CustomNpcs.EnableUpdateChecker) {
			VersionChecker checker = new VersionChecker();
			checker.start();
		}
		PixelmonHelper.loadClient();
		OBJLoader.INSTANCE.addDomain(CustomNpcs.MODID);
	}

	@Override
	public void openGui(EntityNPCInterface npc, EnumGuiType gui) {
		this.openGui(npc, gui, 0, 0, 0);
	}

	@Override
	public void openGui(EntityNPCInterface npc, EnumGuiType gui, int x, int y, int z) {
		Minecraft minecraft = Minecraft.getMinecraft();
		Container container = this.getContainer(gui, (EntityPlayer) minecraft.player, x, y, z, npc);
		GuiScreen guiscreen = this.getGui(npc, gui, container, x, y, z);
		if (guiscreen != null) {
			minecraft.displayGuiScreen(guiscreen);
		}
	}

	@Override
	public void openGui(EntityPlayer player, Object guiscreen) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (!player.world.isRemote || !(guiscreen instanceof GuiScreen)) {
			return;
		}
		if (guiscreen != null) {
			minecraft.displayGuiScreen((GuiScreen) guiscreen);
		}
	}

	@Override
	public void openGui(int i, int j, int k, EnumGuiType gui, EntityPlayer player) {
		Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft.player != player) {
			return;
		}
		GuiScreen guiscreen = this.getGui(null, gui, null, i, j, k);
		if (guiscreen != null) {
			minecraft.displayGuiScreen(guiscreen);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void load() {
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
		if (CustomNpcs.InventoryGuiEnabled) {
			MinecraftForge.EVENT_BUS.register(new TabRegistry());
			if (TabRegistry.getTabList().isEmpty()) {
				TabRegistry.registerTab(new InventoryTabVanilla());
				TabRegistry.registerTab(new InventoryTabFactions());
				TabRegistry.registerTab(new InventoryTabQuests());
			}
		}
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcPony.class, (Render) new RenderNPCPony());
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcCrystal.class, new RenderNpcCrystal(new ModelNpcCrystal(0.5f)));
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcDragon.class, new RenderNpcDragon(new ModelNpcDragon(0.0f), 0.5f));
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcSlime.class, new RenderNpcSlime(new ModelNpcSlime(16), new ModelNpcSlime(0), 0.25f));
		RenderingRegistry.registerEntityRenderingHandler(EntityProjectile.class, (Render) new RenderProjectile());
		RenderingRegistry.registerEntityRenderingHandler(EntityCustomNpc.class, new RenderCustomNpc((ModelBiped) new ModelPlayerAlt(0.0f, false)));
		RenderingRegistry.registerEntityRenderingHandler(EntityNPC64x32.class, new RenderCustomNpc(new ModelBipedAlt(0.0f)));
		RenderingRegistry.registerEntityRenderingHandler(EntityNPCGolem.class, new RenderNPCInterface((ModelBase) new ModelNPCGolem(0.0f), 0.0f));
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcAlex.class, new RenderCustomNpc((ModelBiped) new ModelPlayerAlt(0.0f, true)));
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcClassicPlayer.class, new RenderCustomNpc((ModelBiped) new ModelClassicPlayer(0.0f)));
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack, tintIndex) -> 9127187, new Item[] { CustomItems.mount, CustomItems.cloner, CustomItems.moving, CustomItems.scripter, CustomItems.wand, CustomItems.teleporter });
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack, tintIndex) -> {
			IItemStack item = NpcAPI.Instance().getIItemStack(stack);
			if (stack.getItem() == CustomItems.scripted_item) {
				return ((IItemScripted) item).getColor();
			}
			return -1;
		}, new Item[] { CustomItems.scripted_item });
		ClientProxy.checkLocalization();
		new GuiTextureSelection(null, "", "png", 0);
		
		Map<Integer, IParticleFactory> map = ObfuscationHelper.getValue(ParticleManager.class, Minecraft.getMinecraft().effectRenderer, Map.class);
		for (int id : CustomItems.customparticles.keySet()) {
			if (map.containsKey(id)) { continue; }
			map.put(id, new IParticleFactory() {
				public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... parametrs) {
					CustomParticle p = CustomItems.customparticles.get(particleID);
					if (p==null) { return new CustomParticle(new NBTTagCompound(), Minecraft.getMinecraft().getTextureManager(), worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, parametrs);}
					return new CustomParticle(p.nbtData, Minecraft.getMinecraft().getTextureManager(), worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, parametrs);
				}
			});
		}
	}
	
	@Override
	public void postload() {
		
	}

	@Override
	public void spawnParticle(EntityLivingBase player, String string, Object... ob) {
		if (string.equals("Block")) {
			BlockPos pos = (BlockPos) ob[0];
			int id = (int) ob[1];
			Block block = Block.getBlockById(id & 0xFFF);
			Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(pos,
					block.getStateFromMeta(id >> 12 & 0xFF));
		} else if (string.equals("ModelData")) {
			ModelData data = (ModelData) ob[0];
			ModelPartData particles = (ModelPartData) ob[1];
			EntityCustomNpc npc = (EntityCustomNpc) player;
			Minecraft minecraft = Minecraft.getMinecraft();
			double height = npc.getYOffset() + data.getBodyY();
			Random rand = npc.getRNG();
			for (int i = 0; i < 2; ++i) {
				EntityEnderFX fx = new EntityEnderFX(npc, (rand.nextDouble() - 0.5) * player.width,
						rand.nextDouble() * player.height - height - 0.25, (rand.nextDouble() - 0.5) * player.width,
						(rand.nextDouble() - 0.5) * 2.0, -rand.nextDouble(), (rand.nextDouble() - 0.5) * 2.0,
						particles);
				minecraft.effectRenderer.addEffect((Particle) fx);
			}
		}
	}

	@Override
	public void spawnParticle(EnumParticleTypes particle, double x, double y, double z, double motionX, double motionY,
			double motionZ, float scale) {
		Minecraft mc = Minecraft.getMinecraft();
		double xx = mc.getRenderViewEntity().posX - x;
		double yy = mc.getRenderViewEntity().posY - y;
		double zz = mc.getRenderViewEntity().posZ - z;
		if (xx * xx + yy * yy + zz * zz > 256.0) {
			return;
		}
		Particle fx = mc.effectRenderer.spawnEffectParticle(particle.getParticleID(), x, y, z, motionX, motionY,
				motionZ, new int[0]);
		if (fx == null) {
			return;
		}
		if (particle == EnumParticleTypes.FLAME) {
			ObfuscationHelper.setValue(ParticleFlame.class, (ParticleFlame) fx, scale, 0);
		} else if (particle == EnumParticleTypes.SMOKE_NORMAL) {
			ObfuscationHelper.setValue(ParticleSmokeNormal.class, (ParticleSmokeNormal) fx, scale, 0);
		}
	}

	public void updateGUI() {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if (gui == null) { return; }
		gui.initGui();
	}
	
	/**
	 * RecipeBookClient
	 * @param recipe
	 * @param needSend
	 * @param delete
	 */
	public void updateRecipes(INpcRecipe recipe, boolean needSend, boolean delete, String debug) {
		super.updateRecipes(recipe, false, delete, "ClientProxy.updateRecipes()");
		/** Create Base Data Global */
		if (!RecipeBookClient.RECIPES_BY_TAB.containsKey(CustomItems.tab)) {
			RecipeList recipelist = new RecipeList();
			RecipeBookClient.ALL_RECIPES.add(recipelist);
			(RecipeBookClient.RECIPES_BY_TAB.computeIfAbsent(CustomItems.tab, (p_194085_0_) -> {
				return new ArrayList<RecipeList>();
			})).add(recipelist);
			(RecipeBookClient.RECIPES_BY_TAB.computeIfAbsent(CreativeTabs.SEARCH, (p_194083_0_) -> {
				return new ArrayList<RecipeList>();
			})).add(recipelist);
		}
		/** Create Base Data Mod */
		if (!ClientProxy.MOD_RECIPES_BY_TAB.containsKey(CustomItems.tab)) {
			RecipeList recipelist = new RecipeList();
			(ClientProxy.MOD_RECIPES_BY_TAB.computeIfAbsent(CustomItems.tab, (p_194085_0_) -> {
				return new ArrayList<RecipeList>();
			})).add(recipelist);
			(ClientProxy.MOD_RECIPES_BY_TAB.computeIfAbsent(CreativeTabs.SEARCH, (p_194083_0_) -> {
				return new ArrayList<RecipeList>();
			})).add(recipelist);
		}
		
		/** Update Recipe */
		if (recipe != null) {
			List<RecipeList> lists = recipe.isGlobal() ? RecipeBookClient.RECIPES_BY_TAB.get(CustomItems.tab) : ClientProxy.MOD_RECIPES_BY_TAB.get(CustomItems.tab);
			for (RecipeList list : lists) {
				for (IRecipe rec : list.getRecipes()) {
					if (rec instanceof INpcRecipe && ((INpcRecipe) rec).equal(recipe)) {
						if (delete) {
							list.getRecipes().remove(rec);
						} else {
							((INpcRecipe) rec).copy(recipe);
						}
						break;
					}
				}
			}
		}
		
		/** Update All Recipes */
		// Delete Old
		for (int i = 0; i < 2; i++) { // Lists
			List<RecipeList> delList = Lists.<RecipeList>newArrayList();
			List<RecipeList> lists = (i == 0 ? RecipeBookClient.RECIPES_BY_TAB.get(CustomItems.tab)
					: ClientProxy.MOD_RECIPES_BY_TAB.get(CustomItems.tab));
			for (RecipeList list : lists) { // Recipes
				List<IRecipe> del = Lists.<IRecipe>newArrayList();
				for (IRecipe rec : list.getRecipes()) {
					if (!(rec instanceof INpcRecipe)) { continue; }
					INpcRecipe r = RecipeController.getInstance().getRecipe(rec.getRegistryName());
					if (r==null || !r.isValid() || (r.isGlobal() && i==1) || (!r.isGlobal() && i==0)) {
						del.add(rec);
					}
				}
				if (del.size() > 0) {
					for (IRecipe rec : del) {
						list.getRecipes().remove(rec);
					}
				}
				if (list.getRecipes().size()==0) { delList.add(list); }
			}
			if (delList.size() > 0) {
				for (RecipeList list : delList) {
					if (i == 0) {
						RecipeBookClient.ALL_RECIPES.remove(list);
						RecipeBookClient.RECIPES_BY_TAB.get(CustomItems.tab).remove(list);
						RecipeBookClient.RECIPES_BY_TAB.get(CreativeTabs.SEARCH).remove(list);
					} else {
						ClientProxy.MOD_RECIPES_BY_TAB.get(CustomItems.tab).remove(list);
						ClientProxy.MOD_RECIPES_BY_TAB.get(CreativeTabs.SEARCH).remove(list);
					}
				}
			}
		}
		if (recipe != null) {
			this.updateRecipeBook((EntityPlayerSP) this.getPlayer());
			return;
		}

		/** Adds */
		for (int i = 0; i < 2; i++) {
			Map<String, List<INpcRecipe>> map = (i == 0 ? RecipeController.getInstance().globalList : RecipeController.getInstance().modList);
			for (String group : map.keySet()) {
				RecipeList parent = null;
				for (RecipeList list : (i == 0 ? RecipeBookClient.RECIPES_BY_TAB.get(CustomItems.tab) : ClientProxy.MOD_RECIPES_BY_TAB.get(CustomItems.tab))) {
					if (list!=null && list.getRecipes().size()==0) {
						parent = list;
						break;
					}
					if (list.getRecipes().get(0) instanceof INpcRecipe && ((INpcRecipe) list.getRecipes().get(0)).getNpcGroup().equals(group)) {
						parent = list;
						break;
					}
				}
				if (parent == null) {
					RecipeList newList = new RecipeList();
					for (INpcRecipe rec : map.get(group)) {
						if (!rec.isValid()) { continue; }
						newList.add((IRecipe) rec);
					}
					if (i == 0) {
						RecipeBookClient.ALL_RECIPES.add(newList);
						RecipeBookClient.RECIPES_BY_TAB.get(CreativeTabs.SEARCH).add(newList);
						RecipeBookClient.RECIPES_BY_TAB.get(CustomItems.tab).add(newList);
					} else {
						ClientProxy.MOD_RECIPES_BY_TAB.get(CreativeTabs.SEARCH).add(newList);
						ClientProxy.MOD_RECIPES_BY_TAB.get(CustomItems.tab).add(newList);
					}
				} else {
					for (INpcRecipe rec : map.get(group)) {
						if (!rec.isValid()) { continue; }
						boolean added = true;
						for (IRecipe r : parent.getRecipes()) {
							if (r instanceof INpcRecipe && ((INpcRecipe) r).equal(rec)) {
								added = false;
								((INpcRecipe) r).copy(rec);
								break;
							}
						}
						if (added) {
							parent.add((IRecipe) rec);
						}
					}
				}
			}
		}
		this.updateRecipeBook((EntityPlayerSP) this.getPlayer());
	}
	
	@Override
	public void updateRecipeBook(EntityPlayer player) {
		if (player instanceof EntityPlayerMP) {
			super.updateRecipeBook(player);
			return;
		}
		if (!(player instanceof EntityPlayerSP)) { return; }
		RecipeBook book = ((EntityPlayerSP) player).getRecipeBook();
		if (book == null) { return; }
		BitSet recipes = ObfuscationHelper.getValue(RecipeBook.class, book, 0);
		BitSet newRecipes = ObfuscationHelper.getValue(RecipeBook.class, book, 1);
		List<Integer> delIDs = Lists.<Integer>newArrayList();
		for (int id = recipes.nextSetBit(0); id >= 0; id = recipes.nextSetBit(id + 1)) {
			if (CraftingManager.REGISTRY.getObjectById(id) == null) {
				delIDs.add(id);
			}
		}
		if (delIDs.size() > 0) {
			for (int id : delIDs) {
				recipes.clear(id);
			}
		}
		delIDs.clear();
		for (int id = newRecipes.nextSetBit(0); id >= 0; id = newRecipes.nextSetBit(id + 1)) {
			if (CraftingManager.REGISTRY.getObjectById(id) == null) {
				delIDs.add(id);
			}
		}
		if (delIDs.size() > 0) {
			for (int id : delIDs) {
				newRecipes.clear(id);
			}
		}
		ObfuscationHelper.setValue(RecipeBook.class, book, recipes, 0);
		ObfuscationHelper.setValue(RecipeBook.class, book, newRecipes, 1);
		player.unlockRecipes(RecipeController.getInstance().getKnownRecipes());
	}
	
	@SuppressWarnings("resource")
	@Override
	public void checkBlockFiles(ICustomElement customblock) {
		super.checkBlockFiles(customblock);
		String name = customblock.getCustomName();
		String fileName = ((Block) customblock).getRegistryName().getResourcePath();
		String n =	name.equals("blockexample") ? "Example Custom Block" :
					name.equals("liquidexample") ? "Example Custom Fluid" :
					name.equals("stairsexample") ? "Example Custom Stairs" :
					name.equals("slabexample") ? "Example Custom Slab" :
					name.equals("fasingblockexample") ? "Example Custom Fasing Block" :
					name.equals("portalexample") ? "Example Custom Portal Block" :
					name;
		while(n.indexOf('_')!=-1) { n = n.replace('_', ' '); }
		this.setLocalization("tile."+fileName+".name", n);
		if (customblock instanceof CustomLiquid) {
			this.setLocalization("fluid."+fileName, n);
		}
		
		if (customblock instanceof CustomBlockSlab.CustomBlockSlabDouble) { return; }
		
		File texturesDir = new File(CustomNpcs.Dir, "assets/"+CustomNpcs.MODID+"/textures/"+(customblock instanceof CustomLiquid ? "fluids" : customblock instanceof CustomBlockPortal ? "environment" : "blocks")); 
		if (!texturesDir.exists()) { texturesDir.mkdirs(); }
		File texture = new File(texturesDir, name.toLowerCase()+".png");
		IResource baseTexrure;
		if (!texture.exists()) {
			boolean has = false;
			try {
				if (customblock instanceof CustomBlock && ((CustomBlock) customblock).hasProperty()) {
					if (((CustomBlock) customblock).INT!=null) {
						
					}
					else if (((CustomBlock) customblock).FACING!=null) {
						texture = new File(texturesDir, name.toLowerCase()+"_bottom.png");
						if (!texture.exists()) {
							baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/brewing_stand_base.png"));
							if (baseTexrure!=null) { ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); }
						}
						texture = new File(texturesDir, name.toLowerCase()+"_top.png");
						if (!texture.exists()) {
							baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/endframe_top.png"));
							if (baseTexrure!=null) { ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); }
						}
						texture = new File(texturesDir, name.toLowerCase()+"_front.png");
						if (!texture.exists()) {
							baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/furnace_front_off.png"));
							if (baseTexrure!=null) { ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); }
						}
						texture = new File(texturesDir, name.toLowerCase()+"_right.png");
						if (!texture.exists()) {
							baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/dispenser_front_vertical.png"));
							if (baseTexrure!=null) { ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); }
						}
						texture = new File(texturesDir, name.toLowerCase()+"_back.png");
						if (!texture.exists()) {
							baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/piston_side.png"));
							if (baseTexrure!=null) { ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); }
						}
						texture = new File(texturesDir, name.toLowerCase()+"_left.png");
						if (!texture.exists()) {
							baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/comparator_off.png"));
							if (baseTexrure!=null) { ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); }
						}
						has = true;
					}
				}
				else if (customblock instanceof CustomLiquid) {
					texture = new File(texturesDir, fileName.toLowerCase()+"_still.png.mcmeta");
					if (!texture.exists()) {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/water_still.png.mcmeta"));
						if (baseTexrure!=null) { Files.copy(baseTexrure.getInputStream(), texture.toPath()); }
					}
					texture = new File(texturesDir, fileName.toLowerCase()+"_flow.png");
					if (!texture.exists()) {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/water_flow.png"));
						if (baseTexrure!=null) { ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); has = true; }
					}
					texture = new File(texturesDir, fileName.toLowerCase()+"_overlay.png");
					if (!texture.exists()) {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/water_overlay.png"));
						if (baseTexrure!=null) { ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); has = true; }
					}
					texture = new File(texturesDir, fileName.toLowerCase()+"_still.png");
					if (!texture.exists()) {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/water_still.png"));
						if (baseTexrure!=null) {ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); has = true; }
					}
					texture = new File(texturesDir, fileName.toLowerCase()+"_flow.png.mcmeta");
					if (!texture.exists()) {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/water_flow.png.mcmeta"));
						if (baseTexrure!=null) { Files.copy(baseTexrure.getInputStream(), texture.toPath()); }
					}
					LogWriter.debug("Create Default Texture for \""+name+"\" fluid");
					return;
				}
				else if (customblock instanceof CustomBlockSlab) {
					texture = new File(texturesDir, fileName.toLowerCase()+"_top.png");
					if (!texture.exists()) {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/stone_slab_top.png"));
						if (baseTexrure!=null) { ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); has = true; }
					}
					texture = new File(texturesDir, fileName.toLowerCase()+"_bottom.png");
					if (!texture.exists()) {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/stone_slab_top.png"));
						if (baseTexrure!=null) { ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); has = true; }
					}
					texture = new File(texturesDir, fileName.toLowerCase()+"_side.png");
					if (!texture.exists()) {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/stone_slab_side.png"));
						if (baseTexrure!=null) { ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); has = true; }
					}
					LogWriter.debug("Create Default Texture for \""+name+"\" block slab");
					return;
				}
				else if (customblock instanceof CustomBlockStairs) {
					texture = new File(texturesDir, fileName.toLowerCase()+"_top.png");
					if (!texture.exists()) {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/structure_block.png"));
						if (baseTexrure!=null) { ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); has = true; }
					}
					texture = new File(texturesDir, fileName.toLowerCase()+"_bottom.png");
					if (!texture.exists()) {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/structure_block_save.png"));
						if (baseTexrure!=null) { ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); has = true; }
					}
					texture = new File(texturesDir, fileName.toLowerCase()+"_side.png");
					if (!texture.exists()) {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/structure_block_data.png"));
						if (baseTexrure!=null) { ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); has = true; }
					}
					LogWriter.debug("Create Default Texture for \""+name+"\" block stairs");
					return;
				}
				else if (customblock instanceof CustomBlockPortal) {
					texture = new File(texturesDir, fileName.toLowerCase()+"_portal.png");
					if (!texture.exists()) {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/entity/end_portal.png"));
						if (baseTexrure!=null) { Files.copy(baseTexrure.getInputStream(), texture.toPath()); }
					}
					texture = new File(texturesDir, fileName.toLowerCase()+"_sky.png");
					if (!texture.exists()) {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/environment/end_sky.png"));
						if (baseTexrure!=null) { Files.copy(baseTexrure.getInputStream(), texture.toPath()); }
					}
					LogWriter.debug("Create Default Texture for \""+name+"\" block portal");
					return;
				}
				if (!has) {
					baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/blocks/glazed_terracotta_light_blue.png"));
					if (baseTexrure!=null) { ImageIO.write(this.getBufferImageDefaultFluid(baseTexrure), "png", texture); has = true; }
				}
			}
			catch (IOException e) { }
			if (!has) {
				try {
					BufferedImage bufferedImage = new BufferedImage(16, 16, 6);
					ImageIO.write(bufferedImage, "png", texture);
					LogWriter.debug("Create Default Empty Texture for \""+name+"\" block");
				}
				catch (IOException e) { }
			}
			else { LogWriter.debug("Create Default Texture for \""+name+"\" block"); }
		}
	}
	
	@SuppressWarnings("resource")
	public void checkItemFiles(ICustomElement customitem) {
		super.checkItemFiles(customitem);
		String name = customitem.getCustomName().toLowerCase();
		String fileName = ((Item) customitem).getRegistryName().getResourcePath();
		
		String n = name;
		if (name.equals("itemexample")) { n = "Example simple Custom Item"; }
		else if (name.equals("weaponexample")) { n = "Example Custom Weapon"; }
		else if (name.equals("toolexample")) { n = "Example Custom Tool"; }
		else if (name.equals("armorexample")) {
			String slot = ((CustomArmor) customitem).getEquipmentSlot().name();
			n = "Example Custom Armor "+(""+slot.charAt(0)).toUpperCase()+slot.toLowerCase().substring(1);
		}
		else if (name.equals("shieldexample")) { n = "Example Custom Shield"; }
		else if (name.equals("bowexample")) { n = "Example Custom Bow"; }
		else if (name.equals("foodexample")) { n = "Example Custom Food"; }
		else if (name.equals("fishingrodexample")) { n = "Example Custom Fishing Rod"; }
		while(n.indexOf('_')!=-1) { n = n.replace('_', ' '); }
		this.setLocalization("item."+fileName+".name", n);
		String textureName = ""+name;
		File itemModelsDir = new File(CustomNpcs.Dir, "assets/"+CustomNpcs.MODID+"/models/item");
		if (!itemModelsDir.exists()) { itemModelsDir.mkdirs(); }
		File itemModel = new File(itemModelsDir, fileName.toLowerCase()+".json");
		String texturePath = CustomNpcs.MODID+"/textures/items";
		if (itemModel.exists()) {
			try {
				BufferedReader reader = Files.newBufferedReader(itemModel.toPath());
				String line;
				while((line = reader.readLine()) != null) {
					if (line.indexOf("layer0")==-1) { continue; }
					String tempLine = ""+line.substring(line.indexOf('"', line.indexOf(':'))+1, +line.lastIndexOf('"'));
					if (tempLine.indexOf(':')!=-1) {
						if (tempLine.indexOf('/')!=-1) {
							textureName = tempLine.substring(tempLine.lastIndexOf('/')+1);
							texturePath = CustomNpcs.MODID+"/textures/"+tempLine.substring(tempLine.indexOf(':')+1, tempLine.lastIndexOf('/'));
						} else {
							texturePath = ""+CustomNpcs.MODID+"/textures/";
							textureName = tempLine.substring(tempLine.indexOf(':')+1);
						}
					} else {
						textureName = tempLine;
						texturePath = CustomNpcs.MODID+"/textures";
					}
					break;
				}
				reader.close();
			}
			catch (IOException e) { }
		}
		File texturesDir = new File(CustomNpcs.Dir, "assets/"+texturePath);
		if (!texturesDir.exists()) { texturesDir.mkdirs(); }
		File texture = null;
		String parentName = null;
		IResource baseTexrure;
		if (customitem instanceof CustomArmor) {
			File armorDir = new File(CustomNpcs.Dir, "assets/"+CustomNpcs.MODID+"/textures/models/armor");
			if (!armorDir.exists()) { armorDir.mkdirs(); }
			// Models
			boolean[] has = new boolean[] { false, false };
			for (int i=1; i<=2; i++) {
				texture = new File(armorDir, name+"_layer_"+i+".png");
				if (!texture.exists()) {
					try {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/models/armor/iron_layer_"+i+".png"));
						if (baseTexrure!=null) { Files.copy(baseTexrure.getInputStream(), texture.toPath()); has[i-1] = true; }
					}
					catch (IOException e) { }
				} else { continue; }
				if (has[i-1]) { continue; }
				try {
					BufferedImage bufferedImage = new BufferedImage(64, 32, 6);
					ImageIO.write(bufferedImage, "png", texture);
					has[i-1] = true;
				}
				catch (IOException e) { }
			}
			if (has[0] || has[1]) { LogWriter.debug("Create Default Armor Model Texture for \""+name+"\" item"); }
			texture = new File(texturesDir, textureName+".png");
			switch(((CustomArmor) customitem).getEquipmentSlot()) {
				case HEAD:
					parentName = "iron_helmet";
					break;
				case CHEST:
					parentName = "iron_chestplate";
					break;
				case LEGS:
					parentName = "iron_leggings";
					break;
				default:
					parentName = "iron_boots";
					break;
			}
		}
		else if (customitem instanceof CustomBow) {
			boolean[] has = new boolean[] { false, false, false, false };
			for (int i=0; i<4; i++) {
				texture = new File(texturesDir, textureName.replace("_standby", "") + (i==0 ? "_standby" : "_pulling_"+(i-1))+".png");
				if (!texture.exists()) {
					try {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/items/bow"+(i==0 ? "_standby" : "_pulling_"+(i-1))+".png"));
						if (baseTexrure!=null) { Files.copy(baseTexrure.getInputStream(), texture.toPath()); has[i] = true; }
					}
					catch (IOException e) { }
				} else { continue; }
				if (has[i]) { continue; }
				try {
					BufferedImage bufferedImage = new BufferedImage(16, 16, 6);
					ImageIO.write(bufferedImage, "png", texture);
					has[i] = true;
				}
				catch (IOException e) { }
			}
			if (has[0] || has[1] || has[2] || has[3]) { LogWriter.debug("Create Default Bow Texture for \""+name+"\" item"); }
			return;
		}
		else if (customitem instanceof CustomFishingRod) {
			boolean[] has = new boolean[] { false, false };
			n = textureName.replace("_uncast", "").replace("_cast", "");
			for (int i=0; i<2; i++) {
				texture = new File(texturesDir, n + (i==0 ? "_uncast" : "_cast")+".png");
				if (!texture.exists()) {
					try {
						baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/items/fishing_rod"+(i==0 ? "_uncast" : "_cast")+".png"));
						if (baseTexrure!=null) { Files.copy(baseTexrure.getInputStream(), texture.toPath()); has[i] = true; }
					}
					catch (IOException e) { }
				} else { continue; }
				if (has[i]) { continue; }
				try {
					BufferedImage bufferedImage = new BufferedImage(16, 16, 6);
					ImageIO.write(bufferedImage, "png", texture);
					has[i] = true;
				}
				catch (IOException e) { }
			}
			if (has[0] || has[1]) { LogWriter.debug("Create Default Fishing Rod Texture for \""+name+"\" item"); }
			return;
		}
		
		// Simple
		if (parentName==null || texture==null) {
			texture = new File(texturesDir, textureName+".png");
			if (customitem instanceof CustomWeapon) {
				parentName = "iron_sword";
			}
			else if (customitem instanceof CustomTool) {
				parentName = "iron_pickaxe";
			}
			else if (customitem instanceof CustomShield) {
				parentName = "bread";
			}
			else if (customitem instanceof CustomFood) {
				parentName = "coal";
			} else {
				parentName = "iron_ingot";
			}
		}
		
		if (texture!=null && !texture.exists() && parentName!=null) {
			try {
				baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/items/"+parentName+".png"));
				if (baseTexrure!=null) {
					Files.copy(baseTexrure.getInputStream(), texture.toPath());
					LogWriter.debug("Create Default Texture for \""+name+"\" item");
					return;
				}
			}
			catch (IOException e) { }
			try {
				BufferedImage bufferedImage = new BufferedImage(16, 16, 6);
				ImageIO.write(bufferedImage, "png", texture);
				LogWriter.debug("Create Default Empty Texture for \""+name+"\" item");
			}
			catch (IOException e) { }
		}
	}
	
	public void checkPotionFiles(ICustomElement custompotion) {
		super.checkPotionFiles(custompotion);
		String name = custompotion.getCustomName();
		
		String n = name;
		if (name.equals("potionexample")) {n = "Example Custom Potion"; }
		while(n.indexOf('_')!=-1) { n = n.replace('_', ' '); }
		this.setLocalization("effect."+name, n);
		this.setLocalization("potion.effect."+name, n);
		this.setLocalization("splash_potion.effect."+name, name.equals("potionexample") ? "Example Custom Splash Potion" : n+" Splash");
		this.setLocalization("lingering_potion.effect."+name, name.equals("potionexample") ? "Example Custom Lingering Potion" : n+" Lingering");
		this.setLocalization("tipped_arrow.effect."+name, name.equals("potionexample") ? "Example Custom Arrow Potion" : n+" Arrow");
		
		String textureName = name.toLowerCase();
		File texturesDir = new File(CustomNpcs.Dir, "assets/"+CustomNpcs.MODID+"/textures/potions");
		if (!texturesDir.exists()) { texturesDir.mkdirs(); }
		File texture = new File(texturesDir, textureName+".png");
		if (texture!=null && !texture.exists()) {
			boolean has = false;
			try {
				IResource baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/gui/container/inventory.png"));
				if (baseTexrure!=null) {
					try {
						BufferedImage potionImage = new BufferedImage(18, 18, 6);
						BufferedImage bufferedImage = ImageIO.read(baseTexrure.getInputStream());
						for (int u=0; u<18; u++) {
							for (int v=0; v<18; v++) {
								Color c = new Color(bufferedImage.getRGB(u+36, v+235));
								if (c.getRGB()!=-16777216) {
									float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
									hsb[0] += 0.25f;
									if (hsb[0]>1.0f) { hsb[0] -= 1.0f; }
									c = Color.getHSBColor(hsb[0] - (hsb[0]>1.0f ? 1.0f : 0.0f), hsb[1], hsb[2]);
									c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 128);
									potionImage.setRGB(u, v, c.getRGB());
								}
							}
						}
						ImageIO.write(potionImage, "png", texture);
						LogWriter.debug("Create Default Texture for \""+name+"\" potion");
						has = true;
					}
					catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			catch (IOException e) { }
			if (!has) {
				try {
					BufferedImage bufferedImage = new BufferedImage(18, 18, 6);
					ImageIO.write(bufferedImage, "png", texture);
					LogWriter.debug("Create Default Texture for \""+name+"\" potion");
				}
				catch (IOException e) { }
			}
		}
	}

	public void checkParticleFiles(ICustomElement customparticle) {
		super.checkParticleFiles(customparticle);
		String name = customparticle.getCustomName();
		
		String n = name;
		if (name.equalsIgnoreCase("PARTICLE_EXAMPLE")) {n = "Example Custom Particle"; }
		while(n.indexOf('_')!=-1) { n = n.replace('_', ' '); }
		this.setLocalization("particle."+name, n);
		
		INbt nbt = customparticle.getCustomNbt();
		
		String textureName = nbt.getString("Texture");
		File texturesDir = new File(CustomNpcs.Dir, "assets/"+CustomNpcs.MODID+"/textures/particle");
		if (!texturesDir.exists()) { texturesDir.mkdirs(); }
		File texture = new File(texturesDir, textureName+".png");
		if (texture!=null && !texture.exists()) {
			boolean has = false;
			try {
				IResource baseTexrure = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/particle/particles.png"));
				if (baseTexrure!=null) {
					try {
						BufferedImage particlesImage = new BufferedImage(128, 128, 6);
						BufferedImage bufferedImage = ImageIO.read(baseTexrure.getInputStream());
						for (int u=0; u<128; u++) {
							for (int v=0; v<128; v++) {
								Color c = new Color(bufferedImage.getRGB(u, v));
								if (c.getRGB()!=-16777216) {
									float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
									hsb[0] += 0.25f;
									if (hsb[0]>1.0f) { hsb[0] -= 1.0f; }
									c = Color.getHSBColor(hsb[0] - (hsb[0]>1.0f ? 1.0f : 0.0f), hsb[1], hsb[2]);
									c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 171);
									particlesImage.setRGB(u, v, c.getRGB());
								}
							}
						}
						ImageIO.write(particlesImage, "png", texture);
						has = true;
					}
					catch (IOException e) { }
				}
			}
			catch (IOException e) { }
			if (!has) {
				try {
					BufferedImage bufferedImage = new BufferedImage(128, 128, 6);
					ImageIO.write(bufferedImage, "png", texture);
					has = true;
				}
				catch (IOException e) { }
			}
			if (has) { LogWriter.debug("Create Default Texture for \""+name+"\" particle"); }
		}
	}
	
	private RenderedImage getBufferImageDefaultFluid(IResource baseTexrure) {
		BufferedImage bufferedImage = new BufferedImage(16, 16, 6);
		try {
			bufferedImage = ImageIO.read(baseTexrure.getInputStream());
			for (int u=0; u<bufferedImage.getWidth(); u++) {
				for (int v=0; v<bufferedImage.getHeight(); v++) {
					Color c = new Color(bufferedImage.getRGB(u, v));
					float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
					hsb[0] += 0.5f;
					if (hsb[0]>1.0f) { hsb[0] -= 1.0f; }
					c = Color.getHSBColor(hsb[0] - (hsb[0]>1.0f ? 1.0f : 0.0f), hsb[1], hsb[2]);
					c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 255);
					bufferedImage.setRGB(u, v, c.getRGB());
				}
			}
		}
		catch (IOException e) { }
		return bufferedImage;
	}

	private void setLocalization(String key, String name) {
		File langDir = new File(CustomNpcs.Dir, "assets/"+CustomNpcs.MODID+"/lang");
		if (!langDir.exists()) { langDir.mkdirs(); }
		BufferedWriter writer;
		String currentLanguage = ObfuscationHelper.getValue(LanguageManager.class, Minecraft.getMinecraft().getLanguageManager(), String.class);
		boolean write = false;
		for (int i=0; i<2; i++) {
			if (i==1 && currentLanguage.equals("en_us")) { break; }
			File lang = new File(langDir, (i==0 ? "en_us" : currentLanguage)+".lang");
			String jsonStr = key+"="+name;
			char chr = Character.toChars(0x000A)[0];
			writer = null;
			if (!lang.exists()) {
				try { writer = Files.newBufferedWriter(lang.toPath()); }
				catch (IOException e) { writer = null; }
			} else {
				try {
					BufferedReader reader = Files.newBufferedReader(lang.toPath());
					String line;
					jsonStr = "";
					while((line = reader.readLine()) != null) { jsonStr += line + chr; }
					reader.close();
					if (jsonStr.indexOf(key+"=")==-1) {
						jsonStr += key+"="+name + chr;
						writer = Files.newBufferedWriter(lang.toPath());
					}
				}
				catch (IOException e) { jsonStr = key+"="+name; }
			}
			if (writer!=null && jsonStr.length()>0) {
				try {
					writer.write(jsonStr);
					writer.close();
					write = true;
				} catch (IOException e) { }
			}
		}
		if (write) { LogWriter.debug("Create Default Localization key \""+key+"\""); }
	}

	public static void checkLocalization() {
		File langDir = new File(CustomNpcs.Dir, "assets/"+CustomNpcs.MODID+"/lang");
		if (!langDir.exists() || !langDir.isDirectory()) { return; }
		LanguageManager languageManager = Minecraft.getMinecraft().getLanguageManager();
		Locale locale = ObfuscationHelper.getValue(LanguageManager.class, languageManager, Locale.class);
		LanguageMap localized = ObfuscationHelper.getValue(I18n.class, I18n.class, LanguageMap.class);
		Map<String, String> properties = Maps.<String, String>newHashMap();
		Map<String, String> languageList = Maps.<String, String>newHashMap();
		if (locale!=null) {
			properties = ObfuscationHelper.getValue(Locale.class, locale, Map.class);
			if (properties==null) { properties = Maps.<String, String>newHashMap(); }
		}
		if (localized!=null) {
			languageList = ObfuscationHelper.getValue(LanguageMap.class, localized, Map.class);
			if (languageList==null) { languageList = Maps.<String, String>newHashMap(); }
		}
		
		File lang = new File(langDir, "en_us.lang");
		if (lang.exists() && lang.isFile()) {
			try {
				BufferedReader reader = Files.newBufferedReader(lang.toPath());
				String line;
				while((line = reader.readLine()) != null) {
					if (line.indexOf("=")<=0) { continue; }
					String[] loc = line.split("=");
					properties.put(loc[0], loc[1]);
					languageList.put(loc[0], loc[1]);
				}
				reader.close();
			}
			catch (IOException e) { }
		}
		String currentLanguage = ObfuscationHelper.getValue(LanguageManager.class, languageManager, String.class);
		if (ClientProxy.playerData!=null && CustomNpcs.proxy.getPlayer()!=null && CustomNpcs.proxy.getPlayer().world!=null) {
			ObfuscationHelper.setValue(PlayerGameData.class, ClientProxy.playerData.game, currentLanguage, String.class);
			NoppesUtilPlayer.sendData(EnumPlayerPacket.CurrentLanguage, currentLanguage);
		}
		if (!currentLanguage.equals("en_us")) {
			lang = new File(langDir, currentLanguage+".lang");
			if (lang.exists() && lang.isFile()) {
				try {
					BufferedReader reader = Files.newBufferedReader(lang.toPath());
					String line;
					while((line = reader.readLine()) != null) {
						if (line.indexOf("=")<=0) { continue; }
						String[] loc = line.split("=");
						properties.put(loc[0], loc[1]);
						languageList.put(loc[0], loc[1]);
					}
					reader.close();
				}
				catch (IOException e) { }
			}
		}
	}

	@Override
	public Side getSide() { return Side.CLIENT; }
	
	@Override
	public boolean isLoadTexture(ResourceLocation resource) {
		return Minecraft.getMinecraft().getTextureManager().getTexture(resource)!=null;
	}
	
}
