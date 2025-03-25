package noppes.npcs.client;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.*;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import micdoodle8.mods.galacticraft.api.client.tabs.InventoryTabFactions;
import micdoodle8.mods.galacticraft.api.client.tabs.InventoryTabQuests;
import micdoodle8.mods.galacticraft.api.client.tabs.InventoryTabVanilla;
import micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.Locale;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShield;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.RecipeBook;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.registries.IRegistryDelegate;
import noppes.npcs.*;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.IMinecraft;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.event.ClientEvent;
import noppes.npcs.api.handler.data.IKeySetting;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.api.item.IItemScripted;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.WrapperMinecraft;
import noppes.npcs.blocks.CustomBlock;
import noppes.npcs.blocks.CustomBlockPortal;
import noppes.npcs.blocks.CustomBlockSlab;
import noppes.npcs.blocks.CustomBlockStairs;
import noppes.npcs.blocks.CustomChest;
import noppes.npcs.blocks.CustomDoor;
import noppes.npcs.blocks.CustomLiquid;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.controllers.PresetController;
import noppes.npcs.client.fx.EntityEnderFX;
import noppes.npcs.client.gui.*;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.dimentions.GuiCreateDimension;
import noppes.npcs.client.gui.global.*;
import noppes.npcs.client.gui.mainmenu.GuiDropEdit;
import noppes.npcs.client.gui.mainmenu.GuiNPCGlobalMainMenu;
import noppes.npcs.client.gui.mainmenu.GuiNPCInv;
import noppes.npcs.client.gui.mainmenu.GuiNpcAI;
import noppes.npcs.client.gui.mainmenu.GuiNpcAdvanced;
import noppes.npcs.client.gui.mainmenu.GuiNpcDisplay;
import noppes.npcs.client.gui.mainmenu.GuiNpcStats;
import noppes.npcs.client.gui.player.*;
import noppes.npcs.client.gui.player.companion.GuiNpcCompanionInv;
import noppes.npcs.client.gui.player.companion.GuiNpcCompanionStats;
import noppes.npcs.client.gui.player.companion.GuiNpcCompanionTalents;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeItem;
import noppes.npcs.client.gui.roles.GuiNpcBankSetup;
import noppes.npcs.client.gui.roles.GuiNpcFollowerSetup;
import noppes.npcs.client.gui.roles.GuiNpcItemGiver;
import noppes.npcs.client.gui.roles.GuiNpcTransporter;
import noppes.npcs.client.gui.script.*;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.client.model.ModelBipedAlt;
import noppes.npcs.client.model.ModelNPCGolem;
import noppes.npcs.client.model.ModelNpcAlt;
import noppes.npcs.client.model.ModelNpcCrystal;
import noppes.npcs.client.model.ModelNpcDragon;
import noppes.npcs.client.model.ModelNpcSlime;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.part.ModelData;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.client.renderer.RenderCustomNpc;
import noppes.npcs.client.renderer.RenderNPCInterface;
import noppes.npcs.client.renderer.RenderNPCPony;
import noppes.npcs.client.renderer.RenderNpcCrystal;
import noppes.npcs.client.renderer.RenderNpcDragon;
import noppes.npcs.client.renderer.RenderNpcSlime;
import noppes.npcs.client.renderer.RenderProjectile;
import noppes.npcs.client.renderer.blocks.TileEntityCustomBannerRenderer;
import noppes.npcs.client.renderer.blocks.TileEntityItemStackCustomRenderer;
import noppes.npcs.client.util.aw.ArmourersWorkshopUtil;
import noppes.npcs.config.TrueTypeFont;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.containers.*;
import noppes.npcs.controllers.*;
import noppes.npcs.controllers.data.PlayerData;
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
import noppes.npcs.items.ItemScripted;
import noppes.npcs.api.mixin.client.gui.recipebook.IRecipeListMixin;
import noppes.npcs.api.mixin.client.network.INetworkPlayerInfoMixin;
import noppes.npcs.api.mixin.client.particle.IParticleFlameMixin;
import noppes.npcs.api.mixin.client.particle.IParticleManagerMixin;
import noppes.npcs.api.mixin.client.particle.IParticleSmokeNormalMixin;
import noppes.npcs.particles.CustomParticle;
import noppes.npcs.particles.CustomParticleSettings;
import noppes.npcs.reflection.client.ItemModelMesherForgeReflection;
import noppes.npcs.reflection.client.renderer.texture.TextureManagerReflection;
import noppes.npcs.reflection.client.renderer.tileentity.TileEntityItemStackRendererReflection;
import noppes.npcs.reflection.client.resources.LocaleReflection;
import noppes.npcs.reflection.client.settings.KeyBindingReflection;
import noppes.npcs.util.Util;
import noppes.npcs.util.TempFile;

@SuppressWarnings("deprecation")
public class ClientProxy extends CommonProxy {

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
			this.textFont = new TrueTypeFont(new Font(fontType, java.awt.Font.PLAIN, fontSize), 1.0f);
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
	public static KeyBinding frontButton = Minecraft.getMinecraft().gameSettings.keyBindForward; // w
	public static KeyBinding leftButton = Minecraft.getMinecraft().gameSettings.keyBindLeft; // a
	public static KeyBinding backButton = Minecraft.getMinecraft().gameSettings.keyBindBack; // s
	public static KeyBinding rightButton = Minecraft.getMinecraft().gameSettings.keyBindRight; // d

	public static KeyBinding QuestLog = new KeyBinding("key.quest.log", 38, "key.categories.gameplay"), Scene1, Scene2, Scene3, SceneReset;
	public static FontContainer Font;
	public static PlayerData playerData = new PlayerData();

	public static final Map<String, TempFile> loadFiles = new TreeMap<>();
	public static Map<Integer, List<UUID>> notVisibleNPC = new HashMap<>();
	public static Map<String, Map<String, TreeMap<ResourceLocation, Long>>> texturesData = new HashMap<>();
	private final static Map<Integer, KeyBinding> keyBindingMap = new HashMap<>();
    private final static List<ResourceLocation> notLoadTextures = new ArrayList<>();
	public static IMinecraft mcWrapper = null;

	public static void bindTexture(ResourceLocation location) {
		try {
			if (location == null) { return; }
			TextureManager manager = Minecraft.getMinecraft().getTextureManager();
			ITextureObject ob = manager.getTexture(location);
			if (ob == null && !notLoadTextures.contains(location)) {
				ob = new SimpleTexture(location);
				manager.loadTexture(location, ob);
				notLoadTextures.add(location);
			}
			if (ob != null) { GlStateManager.bindTexture(ob.getGlTextureId()); }
		}
		catch (Exception e) { LogWriter.error("Error:", e); }
	}

	// Apply changes to your localizations without disabling processes
	public static void checkLocalization() {
		// directory custom langs:
		File langDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/lang");
		if (!langDir.exists() || !langDir.isDirectory()) { return; }
		LogWriter.info("Check Mod Localization");

		// localization in game data
		try {
			Class<?> i18n = Class.forName("net.minecraft.client.resources.I18n");
			Map<String, String> properties = null;
			for (Field field : i18n.getDeclaredFields()) {
				if (field.getType() == Locale.class) {
					field.setAccessible(true);
					properties = LocaleReflection.getProperties((Locale) field.get(null));
					break;
				}
			}
			if (properties == null) { return; }
			LogWriter.debug("Localization properties found. Size: "+properties.size());
			// custom lang files:
			String currentLanguage = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
			for (int i = 0; i < (currentLanguage.equals("en_us") ? 1 : 2) ; i++) {
				File lang = new File(langDir, (i == 0 ? "en_us" : currentLanguage) + ".lang");
				if (lang.exists() && lang.isFile()) { // loading localizations from mod file
					try {
						BufferedReader reader = Files.newBufferedReader(lang.toPath());
						String line;
						while ((line = reader.readLine()) != null) {
							if (line.startsWith("#") || !line.contains("=")) { continue; }
							String[] vk = line.split("=");
							properties.put(vk[0], vk[1]);
						}
						reader.close();
					} catch (Exception e) { LogWriter.error("Error load custom localization", e); }
				}
			}
		}
		catch (Exception e) { LogWriter.error("Error localization class found: ", e); }

	}

	// Blending texture colors with a mask
	private static BufferedImage colorTexture(BufferedImage buffer, Color color, boolean onlyGray) {
		if (buffer == null || color == null) {
			return buffer;
		}
		for (int v = 0; v < buffer.getHeight(); v++) {
			for (int u = 0; u < buffer.getWidth(); u++) {
				int c = buffer.getRGB(u, v);
				int al = c >> 24 & 255;
				if (al == 0) {
					continue;
				}
				if (onlyGray) {
					Color k = new Color(c);
					if (k.getRed() != 127 || k.getGreen() != 127 || k.getBlue() != 127) {
						continue;
					}
					buffer.setRGB(u, v, color.getRGB());
					continue;
				}
				int r0 = c >> 16 & 255, g0 = c >> 8 & 255, b0 = c & 255;
				String a = Integer.toHexString(Math.min((al + color.getAlpha()), 255));
				if (a.length() == 1) {
					a = "0" + a;
				}
				String r = Integer.toHexString((r0 + color.getRed()) / 2);
				if (r.length() == 1) {
					r = "0" + r;
				}
				String g = Integer.toHexString((g0 + color.getGreen()) / 2);
				if (g.length() == 1) {
					g = "0" + g;
				}
				String b = Integer.toHexString((b0 + color.getBlue()) / 2);
				if (b.length() == 1) {
					b = "0" + b;
				}
				buffer.setRGB(u, v, (int) Long.parseLong(a + r + g + b, 16));
			}
		}
		return buffer;
	}

	private static BufferedImage combineTextures(BufferedImage buffer_0, BufferedImage buffer_1) {
		if (buffer_0 == null) {
			return buffer_1;
		}
		if (buffer_1 == null) {
			return buffer_0;
		}
		int w0 = buffer_0.getWidth(), w1 = buffer_1.getWidth();
		int h0 = buffer_0.getHeight(), h1 = buffer_1.getHeight();
		int w = Math.max(w0, w1);
		int h = Math.max(h0, h1);
		float sw0 = (float) w0 / (float) w, sh0 = (float) h0 / (float) h;
		float sw1 = (float) w1 / (float) w, sh1 = (float) h1 / (float) h;
		BufferedImage total = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		for (int v = 0; v < h; v++) {
			for (int u = 0; u < w; u++) {
				int c0 = buffer_0.getRGB((int) ((float) u * sw0), (int) ((float) v * sh0));
				int a0 = c0 >> 24 & 255;
				if (a0 != 0) {
					total.setRGB(u, v, c0);
				}
				int c1 = buffer_1.getRGB((int) ((float) u * sw1), (int) ((float) v * sh1));
				int a1 = c1 >> 24 & 255;
				if (a1 != 0) {
					if (a1 == 255) {
						total.setRGB(u, v, c1);
					} else {
						int r0 = c0 >> 16 & 255, g0 = c0 >> 8 & 255, b0 = c0 & 255;
						int r1 = c1 >> 16 & 255, g1 = c1 >> 8 & 255, b1 = c1 & 255;
						String a = Integer.toHexString(Math.min((a0 + a1), 255));
						if (a.length() == 1) {
							a = "0" + a;
						}
						String r = Integer.toHexString((r0 + r1) / 2);
						if (r.length() == 1) {
							r = "0" + r;
						}
						String g = Integer.toHexString((g0 + g1) / 2);
						if (g.length() == 1) {
							g = "0" + g;
						}
						String b = Integer.toHexString((b0 + b1) / 2);
						if (b.length() == 1) {
							b = "0" + b;
						}
						total.setRGB(u, v, (int) Long.parseLong(a + r + g + b, 16));
					}
				}
			}
		}
		return total;
	}

	private static ResourceLocation createPlayerSkin(ResourceLocation skin) {
		if (!skin.getResourceDomain().equals(CustomNpcs.MODID)
				|| (!skin.getResourcePath().toLowerCase().contains("textures/entity/custom/female_")
				&& !skin.getResourcePath().toLowerCase().contains("textures/entity/custom/male_"))) {
			return skin;
		}
		String locSkin = String.format("%s/%s/%s", "assets", skin.getResourceDomain(), skin.getResourcePath());
		File file = new File(CustomNpcs.Dir, locSkin);
		if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) { return skin; }
		TextureManager re = Minecraft.getMinecraft().getTextureManager();
		if (file.exists() && file.isFile()) {
			Map<ResourceLocation, ITextureObject> mapTextureObjects = TextureManagerReflection.getMapTextureObjects(re);
			if (!mapTextureObjects.containsKey(skin)) {
				try {
					BufferedImage skinImage = ImageIO.read(file);
					SimpleTexture texture = new SimpleTexture(skin);
					TextureUtil.uploadTextureImageAllocate(texture.getGlTextureId(), skinImage, false, false);
					mapTextureObjects.put(skin, texture);
				} catch (Exception e) { LogWriter.error("Error:", e); }
			}
			return skin;
		}

		IResourceManager rm = Minecraft.getMinecraft().getResourceManager();
		String[] path = skin.getResourcePath().replace(".png", "").split("_");
		String gender = "male";
		BufferedImage bodyImage = null, hairImage = null, faseImage = null, legsImage = null, jacketsImage = null,  shoesImage = null;
		List<BufferedImage> listBuffers = new ArrayList<>();
		Map<ResourceLocation, ITextureObject> mapTextureObjects = TextureManagerReflection.getMapTextureObjects(re);
		for (int i = 0; i < path.length; i++) {
			int id = -1;
			try {
				id = Integer.parseInt(path[i]);
			} catch (Exception e) { LogWriter.error("Error:", e); }

			switch (i) {
				case 0: {
					if (path[i].toLowerCase().endsWith("female")) {
						gender = "female";
					}
					break;
				}
				case 1: { // body skin
					ResourceLocation loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + gender + "/torsos/" + path[i] + ".png");
					re.bindTexture(loc);
					if (!mapTextureObjects.containsKey(loc)) {
						loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + gender + "/torsos/0.png");
						re.bindTexture(loc);
					}
					try {
						bodyImage = TextureUtil.readBufferedImage(rm.getResource(loc).getInputStream());
					}
					catch (Exception e) { LogWriter.error("Error:", e); }
					break;
				}
				case 2: { // create body
					try {
						int c = Integer.parseInt(path[i]);
						if (c != 0) {
							bodyImage = colorTexture(bodyImage, new Color(c), false);
						}
					}
					catch (Exception e) { LogWriter.error("Error:", e); }
					break;
				}
				case 3: { // hair skin
					ResourceLocation loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + gender + "/hairs/" + path[i] + ".png");
					re.bindTexture(loc);
					if (id > 0 && !mapTextureObjects.containsKey(loc)) {
						loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + gender + "/hairs/0.png");
						re.bindTexture(loc);
					}
					try {
						hairImage = TextureUtil.readBufferedImage(rm.getResource(loc).getInputStream());
					}
					catch (Exception e) { LogWriter.error("Error:", e); }
					break;
				}
				case 4: { // create hair
					try {
						int c = Integer.parseInt(path[i]);
						if (c != 0) {
							hairImage = colorTexture(hairImage, new Color(c), false);
						}
					}
					catch (Exception e) { LogWriter.error("Error:", e); }
					break;
				}
				case 5: { // fase
					ResourceLocation loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + gender + "/faces/" + path[i] + ".png");
					re.bindTexture(loc);
					if (id > -1 && !mapTextureObjects.containsKey(loc)) {
						loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + gender + "/faces/0.png");
						re.bindTexture(loc);
					}
					try {
						faseImage = TextureUtil.readBufferedImage(rm.getResource(loc).getInputStream());
					}
					catch (Exception e) { LogWriter.error("Error:", e); }
					break;
				}
				case 6: { // create fase
					try {
						int c = Integer.parseInt(path[i]);
						if (c != 0) {
							faseImage = colorTexture(faseImage, new Color(c), true);
						}
					}
					catch (Exception e) { LogWriter.error("Error:", e); }
					break;
				}
				case 7: { // legs
					ResourceLocation loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + gender + "/legs/" + path[i] + ".png");
					re.bindTexture(loc);
					if (id > 0 && !mapTextureObjects.containsKey(loc)) {
						loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + gender + "/legs/0.png");
						re.bindTexture(loc);
					}
					try {
						legsImage = TextureUtil.readBufferedImage(rm.getResource(loc).getInputStream());
					}
					catch (Exception e) { LogWriter.error("Error:", e); }
					break;
				}
				case 8: { // jacket
					ResourceLocation loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + gender + "/jackets/" + path[i] + ".png");
					re.bindTexture(loc);
					if (id > 0 && !mapTextureObjects.containsKey(loc)) {
						loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + gender + "/jackets/0.png");
						re.bindTexture(loc);
					}
					try {
						jacketsImage = TextureUtil.readBufferedImage(rm.getResource(loc).getInputStream());
					}
					catch (Exception e) { LogWriter.error("Error:", e); }
					break;
				}
				case 9: { // shoes
					ResourceLocation loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + gender + "/shoes/" + path[i] + ".png");
					re.bindTexture(loc);
					if (id > 0 && !mapTextureObjects.containsKey(loc)) {
						loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + gender + "/shoes/0.png");
						re.bindTexture(loc);
					}
					try {
						shoesImage = TextureUtil.readBufferedImage(rm.getResource(loc).getInputStream());
					}
					catch (Exception e) { LogWriter.error("Error:", e); }
					break;
				}
				default: {
					ResourceLocation loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + gender + "/peculiarities/" + path[i] + ".png");
					re.bindTexture(loc);
					if (id > 0 && !mapTextureObjects.containsKey(loc)) {
						loc = new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + gender + "/peculiarities/0.png");
						re.bindTexture(loc);
					}
					try {
						listBuffers.add(TextureUtil.readBufferedImage(rm.getResource(loc).getInputStream()));
					}
					catch (Exception e) { LogWriter.error("Error:", e); }
					break;
				}
			}
		}
		// combine
		BufferedImage skinImage = null;
		try {
			skinImage = combineTextures(bodyImage, TextureUtil.readBufferedImage(rm.getResource(
							new ResourceLocation(CustomNpcs.MODID, "textures/entity/custom/" + gender + "/torsos/-1.png"))
					.getInputStream()));
			if (!listBuffers.isEmpty()) {
				for (BufferedImage buffer : listBuffers) {
					skinImage = combineTextures(skinImage, buffer);
				}
			}
			skinImage = combineTextures(skinImage, faseImage);
			skinImage = combineTextures(skinImage, legsImage);
			skinImage = combineTextures(skinImage, shoesImage);
			skinImage = combineTextures(skinImage, jacketsImage);
			skinImage = combineTextures(skinImage, faseImage);
			skinImage = combineTextures(skinImage, hairImage);
		}
		catch (Exception e) { LogWriter.error("Error:", e); }

		try {
			if (skinImage != null) {
				ImageIO.write(skinImage, "PNG", file);
				re.bindTexture(skin);
				LogWriter.debug("Create new player skin: " + file.getAbsolutePath());
			}
		}
		catch (Exception e) { LogWriter.error("Error:", e); }
		SimpleTexture texture = new SimpleTexture(skin);
		if (skinImage != null) {
			TextureUtil.uploadTextureImageAllocate(texture.getGlTextureId(), skinImage, false, false);
		}
		mapTextureObjects.put(skin, texture);
		return skin;
	}

	public static void pressed(int keyCode) {
		for (int id : ClientProxy.keyBindingMap.keySet()) {
			KeyBinding kb = ClientProxy.keyBindingMap.get(id);
			if (kb.isActiveAndMatches(keyCode)) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.KeyActive, id);
			}
		}
	}

	public static void resetSkin(UUID uuid) {
		PlayerSkinController pData = PlayerSkinController.getInstance();
		if (uuid == null || !pData.playerTextures.containsKey(uuid) || !pData.playerNames.containsKey(uuid)) {
			return;
		}
		NetworkPlayerInfo npi = Objects.requireNonNull(Minecraft.getMinecraft().getConnection()).getPlayerInfo(uuid);
        Map<Type, ResourceLocation> map = PlayerSkinController.getInstance().playerTextures .get(uuid);
		Map<Type, ResourceLocation> playerTextures = ((INetworkPlayerInfoMixin) npi).npcs$getPlayerTextures();
		playerTextures.clear();
		for (Type epst : map.keySet()) {
			ResourceLocation loc = ClientProxy.createPlayerSkin(map.get(epst));
            LogWriter.debug("Set skin type: " + epst + " = \"" + loc + "\"");
			switch (epst) {
				case CAPE:
					playerTextures.put(Type.CAPE, loc);
					break;
				case ELYTRA:
					playerTextures.put(Type.ELYTRA, loc);
					break;
				default: {
					playerTextures.put(Type.SKIN, loc);
					TextureManager re = Minecraft.getMinecraft().getTextureManager();
					ResourceLocation locDynamic = new ResourceLocation("minecraft",
							"dynamic/skin_" + pData.playerNames.get(uuid));
					ResourceLocation locSkins = new ResourceLocation("minecraft", "skins/" + pData.playerNames.get(uuid));
					ITextureObject texture = re.getTexture(loc);
					Map<ResourceLocation, ITextureObject> mapTextureObjects = TextureManagerReflection.getMapTextureObjects(re);
					mapTextureObjects.put(locDynamic, texture);
					mapTextureObjects.put(locSkins, texture);
					break;
				}
			}
		}
		if (!playerTextures.containsKey(Type.SKIN)) {
			playerTextures.put(Type.SKIN, DefaultPlayerSkin.getDefaultSkin(npi.getGameProfile().getId()));
		}
		LogWriter.debug("Set skins to player UUID: " + uuid);
	}

	public static void sendSkin(UUID uuid) {
		NetworkPlayerInfo npi = Objects.requireNonNull(Minecraft.getMinecraft().getConnection()).getPlayerInfo(uuid);
        NBTTagCompound nbtPlayer = new NBTTagCompound();
		nbtPlayer.setUniqueId("UUID", uuid);
		NBTTagList listTxrs = new NBTTagList();
		for (Type t : Type.values()) {
			ResourceLocation loc;
			switch (t) {
				case CAPE:
					loc = npi.getLocationCape();
					break;
				case ELYTRA:
					loc = npi.getLocationElytra();
					break;
				default:
					loc = npi.getLocationSkin();
					break; // SKIN
			}
			if (loc == null) {
				continue;
			}
			NBTTagCompound nbtSkin = new NBTTagCompound();
			nbtSkin.setString("Type", t.name());
			nbtSkin.setString("Location", loc.toString());
			listTxrs.appendTag(nbtSkin);
		}
		nbtPlayer.setTag("Textures", listTxrs);
		NoppesUtilPlayer.sendData(EnumPlayerPacket.PlayerSkinSet, nbtPlayer);
	}

	@Override
	public void checkBlockFiles(ICustomElement customblock) {
		super.checkBlockFiles(customblock);
		String name = customblock.getCustomName().toLowerCase();
		String fileName = Objects.requireNonNull(((Block) customblock).getRegistryName()).getResourcePath().toLowerCase();

		// localization name
		String n = name.equals("blockexample") ? "Example Custom Block"
				: name.equals("liquidexample") ? "Example Custom Fluid"
				: name.equals("stairsexample") ? "Example Custom Stairs"
				: name.equals("slabexample") ? "Example Custom Slab"
				: name.equals("facingblockexample") ? "Example Custom Facing Block"
				: name.equals("portalexample") ? "Example Custom Portal Block"
				: name.equals("chestexample") ? "Example Custom Chest"
				: name.equals("containerexample") ? "Example Custom Container"
				: name.equals("doorexample") ? "Example Custom Door"
				: name;
		while (n.indexOf('_') != -1) {
			n = n.replace('_', ' ');
		}
		this.setLocalization("tile." + fileName + ".name", n);
		if (customblock instanceof CustomChest) {
			boolean type = ((CustomChest) customblock).isChest;
			n = name.contains("example") ? "example" : name;
			if (!n.isEmpty()) {
				n = ("" + n.charAt(0)).toUpperCase() + n.substring(1);
			}
			while (n.indexOf('_') != -1) {
				n = n.replace('_', ' ');
			}
			this.setLocalization("custom.chest." + name, "Custom " + (type ? "Chest" : "Container") + ": " + n);
		}
		if (customblock instanceof CustomLiquid) {
			this.setLocalization("fluid." + fileName, n);
		}

		// textures
		File texturesDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/textures/" + (customblock instanceof CustomLiquid ? "fluids" : customblock instanceof CustomBlockPortal ? "environment" : "blocks"));
		if (!texturesDir.exists() && !texturesDir.mkdirs()) { return; }
		File texture = new File(texturesDir, name + ".png");
		if (!texture.exists()) {
			InputStream bb = Util.instance.getModInputStream("base_block.png");
			BufferedImage bi;
			try { bi = ImageIO.read(bb); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
			boolean bo = true;
			try {
				if (customblock instanceof CustomBlock && ((CustomBlock) customblock).hasProperty()) {
					if (((CustomBlock) customblock).BO != null) {
						texture = new File(texturesDir, name + "_true.png");
						if (!texture.exists()) {
							bo = ImageIO.write(this.getBufferImageOffset(bi, -1, 0.25f, 0, 0, 0, 255), "png", texture);
						}
						texture = new File(texturesDir, name + "_true.png");
						if (!texture.exists()) {
							bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.75f, 0, 0, 0, 255), "png", texture) && bo;
						}
					}
					else if (((CustomBlock) customblock).INT != null) {
						NBTTagCompound data = ((CustomBlock) customblock).nbtData.getCompoundTag("Property");
						for (int i = data.getInteger("Min"); i <= data.getInteger("Max"); i++) {
							texture = new File(texturesDir, name + "_" + i + ".png");
							if (!texture.exists()) {
								bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.15f + (float) i / 100.0f, 0, 0, 0, 255), "png", texture) && bo;
							}
						}
					} else if (((CustomBlock) customblock).FACING != null) {
						texture = new File(texturesDir, name + "_bottom.png");
						if (!texture.exists()) {
							try { bi = ImageIO.read(Util.instance.getModInputStream("bp_bottom.png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
							bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.5f, 0, 20, 40, 255), "png", texture);
						}
						texture = new File(texturesDir, name + "_top.png");
						if (!texture.exists()) {
							try { bi = ImageIO.read(Util.instance.getModInputStream("bp_top.png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
							bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.5f, 0, 20, 40, 255), "png", texture) && bo;
						}
						texture = new File(texturesDir, name + "_front.png");
						if (!texture.exists()) {
							try { bi = ImageIO.read(Util.instance.getModInputStream("bp_front.png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
							bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.5f, 0, 20, 40, 255), "png", texture) && bo;
						}
						texture = new File(texturesDir, name + "_right.png");
						if (!texture.exists()) {
							try { bi = ImageIO.read(Util.instance.getModInputStream("bp_right.png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
							bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.5f, 0, 20, 40, 255), "png", texture) && bo;
						}
						texture = new File(texturesDir, name + "_back.png");
						if (!texture.exists()) {
							try { bi = ImageIO.read(Util.instance.getModInputStream("bp_back.png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
							bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.5f, 0, 20, 40, 255), "png", texture) && bo;
						}
						texture = new File(texturesDir, name + "_left.png");
						if (!texture.exists()) {
							try { bi = ImageIO.read(Util.instance.getModInputStream("bp_left.png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
							bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.5f, 0, 20, 40, 255), "png", texture) && bo;
						}
					}
					if (bo) {
						LogWriter.debug("Create Default Texture for \"" + name + "\" property block");
					}
				}
				else if (customblock instanceof CustomLiquid) {
					// mc_metas
					texture = new File(texturesDir, fileName.toLowerCase() + "_still.png.mcmeta");
					if (!texture.exists()) {
						bo = Util.instance.saveFile(texture, "{  \"animation\": {" + ((char) 10) + "    \"frametime\": 2" + ((char) 10) + "  }" + ((char) 10) + "}");
					}
					texture = new File(texturesDir, fileName.toLowerCase() + "_flow.png.mcmeta");
					if (!texture.exists()) {
						bo = Util.instance.saveFile(texture, "{  \"animation\": {}" + ((char) 10) + "}") && bo;
					}
					// images
					texture = new File(texturesDir, fileName.toLowerCase() + "_flow.png");
					if (!texture.exists()) {
						try { bi = ImageIO.read(Util.instance.getModInputStream("wf.png")); } catch (Exception e) { bi = new BufferedImage(32, 1024, 6); }
						bo = ImageIO.write(getBufferImageOffset(bi, 0, 0.5f, 0, 0, 0, 128), "png", texture) && bo;
					}
					texture = new File(texturesDir, fileName.toLowerCase() + "_overlay.png");
					if (!texture.exists()) {
						try { bi = ImageIO.read(Util.instance.getModInputStream("wo.png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
						bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.5f, 0, 0, 0, 128), "png", texture) && bo;
					}
					texture = new File(texturesDir, fileName.toLowerCase() + "_still.png");
					if (!texture.exists()) {
						try { bi = ImageIO.read(Util.instance.getModInputStream("ws.png")); } catch (Exception e) { bi = new BufferedImage(16, 512, 6); }
						bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.5f, 0, 0, 0, 128), "png", texture) && bo;
					}
					if (bo) {
						LogWriter.debug("Create Default Texture for \"" + name + "\" fluid");
					}
				}
				else if (customblock instanceof CustomBlockSlab || customblock instanceof CustomBlockStairs) {
					boolean isSlab = customblock instanceof CustomBlockSlab;
					texture = new File(texturesDir, fileName.toLowerCase() + "_top.png");
					if (!texture.exists()) {
						try { bi = ImageIO.read(Util.instance.getModInputStream("b" + (isSlab ? "l" : "s") + "_top.png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
						bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.5f, isSlab ? 50 : 0, isSlab ? 80 : 0, 0, 255), "png", texture);
					}
					texture = new File(texturesDir, fileName.toLowerCase() + "_bottom.png");
					if (!texture.exists()) {
						try { bi = ImageIO.read(Util.instance.getModInputStream("b" + (isSlab ? "l" : "s") + "_bottom.png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
						bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.5f, isSlab ? 50 : 0, isSlab ? 80 : 0, 0, 255), "png", texture) && bo;
					}
					texture = new File(texturesDir, fileName.toLowerCase() + "_side.png");
					if (!texture.exists()) {
						try { bi = ImageIO.read(Util.instance.getModInputStream("b" + (isSlab ? "l" : "s") + "_side.png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
						bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.5f, isSlab ? 50 : 0, isSlab ? 80 : 0, 0, 255), "png", texture) && bo;
					}
					if (bo) {
						LogWriter.debug("Create Default Texture for \"" + name + "\" " + (isSlab ? "slab" : "stairs") + " block");
					}
				}
				else if (customblock instanceof CustomBlockPortal) {
					try { bi = ImageIO.read(Util.instance.getModInputStream("ep.png")); } catch (Exception e) { bi = new BufferedImage(256, 256, 6); }
					bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.0f, 0, 0, 0, 255), "png", texture);

					texture = new File(texturesDir, fileName.toLowerCase() + "_portal.png");
					if (!texture.exists()) {
						bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.0f, 0, 0, 0, 255), "png", texture) && bo;
					}
					texture = new File(texturesDir, fileName.toLowerCase() + "_sky.png");
					if (!texture.exists()) {
						try { bi = ImageIO.read(Util.instance.getModInputStream("es.png")); } catch (Exception e) { bi = new BufferedImage(128, 128, 6); }
						bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.0f, 0, 0, 0, 255), "png", texture) && bo;
					}
					if (bo) {
						LogWriter.debug("Create Default Texture for \"" + name + "\" block portal");
					}
				}
				else if (customblock instanceof CustomDoor) {
					texture = new File(texturesDir, fileName.toLowerCase() + "_lower.png");
					if (!texture.exists()) {
						try { bi = ImageIO.read(Util.instance.getModInputStream("dwl.png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
						bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.15f, 0, 10, 0, 255), "png", texture);
					}
					texture = new File(texturesDir, fileName.toLowerCase() + "_upper.png");
					if (!texture.exists()) {
						try { bi = ImageIO.read(Util.instance.getModInputStream("dwu.png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
						bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.15f, 0, 10, 0, 255), "png", texture) && bo;
					}
					File texturesItemDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/textures/items");
					if (!texturesItemDir.exists() && !texturesItemDir.mkdirs()) { return; }
					texture = new File(texturesItemDir, fileName.toLowerCase() + ".png");
					if (!texture.exists()) {
						try { bi = ImageIO.read(Util.instance.getModInputStream("dw.png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
						bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.15f, 0, 10, 0, 255), "png", texture) && bo;
					}
					if (bo) {
						LogWriter.debug("Create Default Texture for \"" + name + "\" block door");
					}
				}
				else if (customblock instanceof CustomChest) {
					boolean type = ((CustomChest) customblock).isChest;
					if (!type) { // container
						texture = new File(texturesDir, "custom_" + name + "_side.png");
						if (!texture.exists()) {
							try { bi = ImageIO.read(Util.instance.getModInputStream("hs.png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
							bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.35f, 0, 0, 15, 255), "png", texture);
						}
						texture = new File(texturesDir, "custom_" + name + "_top.png");
						if (!texture.exists()) {
							try { bi = ImageIO.read(Util.instance.getModInputStream("ht.png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
							bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.35f, 0, 0, 15, 255), "png", texture) && bo;
						}
					} else { // chest
						texturesDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/textures/entity/chest");
						if (!texturesDir.exists() && !texturesDir.mkdirs()) { return; }
						texture = new File(texturesDir, "custom_" + name + ".png");
						if (!texture.exists()) {
							try { bi = ImageIO.read(Util.instance.getModInputStream("hc.png")); } catch (Exception e) { bi = new BufferedImage(64, 64, 6); }
							bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.35f, 25, 0, 0, 255), "png", texture);
						}
					}
					if (bo) {
						LogWriter.debug("Create Default Texture for \"" + name + "\" " + (type ? "chest" : "container") + " block");
					}
				}
				else {
					if (ImageIO.write(this.getBufferImageOffset(bi, 0, 0.65f, 25, 0, 0, 255), "png", texture)) {
						LogWriter.debug("Create Default Texture for \"" + name + "\" block");
					}
				}
			}
			catch (IOException e) { LogWriter.error("Error:", e); }
		}
	}

	public void checkItemFiles(ICustomElement customitem) {
		super.checkItemFiles(customitem);
		String name = customitem.getCustomName().toLowerCase();
		String fileName = Objects.requireNonNull(((Item) customitem).getRegistryName()).getResourcePath();
		NBTTagCompound nbtData = customitem.getCustomNbt().getMCNBT();
		// localization name
		String n = name;
		switch (name) {
			case "itemexample":
				n = "Example simple Custom Item";
				break;
			case "weaponexample":
				n = "Example Custom Weapon";
				break;
			case "toolexample":
				n = "Example Custom Tool";
				break;
			case "axeexample":
				n = "Example Custom Axe";
				break;
			case "armorexample": {
				String slot = ((CustomArmor) customitem).getEquipmentSlot().name();
				n = "Example Custom Armor (" + ("" + slot.charAt(0)).toUpperCase() + slot.toLowerCase().substring(1) + ")";
				break;
			}
			case "armorobjexample": {
				if (((CustomArmor) customitem).getEquipmentSlot() == EntityEquipmentSlot.FEET) {
					n = "Example Custom 3D Armor (Boots)";
				} else {
					String slot = ((CustomArmor) customitem).getEquipmentSlot().name();
					n = "Example Custom 3D Armor (" + ("" + slot.charAt(0)).toUpperCase() + slot.toLowerCase().substring(1) + ")";
				}
				break;
			}
			case "shieldexample":
				n = "Example Custom Shield";
				break;
			case "bowexample":
				n = "Example Custom Bow";
				break;
			case "foodexample":
				n = "Example Custom Food";
				break;
			case "fishingrodexample":
				n = "Example Custom Fishing Rod";
				break;
		}
		while (n.indexOf('_') != -1) { n = n.replace('_', ' '); }
		this.setLocalization("item." + fileName + ".name", n);
		String textureName = name;
		File itemModelsDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/models/item");
		if (!itemModelsDir.exists() && !itemModelsDir.mkdirs()) { return; }
		File itemModel = new File(itemModelsDir, fileName.toLowerCase() + ".json");
		String texturePath = CustomNpcs.MODID + "/textures/items";
		if (itemModel.exists()) {
			try {
				BufferedReader reader = Files.newBufferedReader(itemModel.toPath());
				String line;
				while ((line = reader.readLine()) != null) {
					if (!line.contains("layer0")) {
						continue;
					}
					String tempLine = line.substring(line.indexOf('"', line.indexOf(':')) + 1, +line.lastIndexOf('"'));
					if (tempLine.indexOf(':') != -1) {
						if (tempLine.indexOf('/') != -1) {
							textureName = tempLine.substring(tempLine.lastIndexOf('/') + 1);
							texturePath = CustomNpcs.MODID + "/textures/"
									+ tempLine.substring(tempLine.indexOf(':') + 1, tempLine.lastIndexOf('/'));
						} else {
							texturePath = CustomNpcs.MODID + "/textures/";
							textureName = tempLine.substring(tempLine.indexOf(':') + 1);
						}
					} else {
						textureName = tempLine;
						texturePath = CustomNpcs.MODID + "/textures";
					}
					break;
				}
				reader.close();
			} catch (IOException e) { LogWriter.error("Error:", e); }
		}

		// textures
		File texturesDir = new File(CustomNpcs.Dir, "assets/" + texturePath);
		if (!texturesDir.exists() && !texturesDir.mkdirs()) { return; }
		File texture = null;
		String parentName = null;
		BufferedImage bi;
		boolean bo = true;
		try {
			if (customitem instanceof CustomArmor) {
				File armorDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/textures/models/armor");
				if (!armorDir.exists() && !armorDir.mkdirs()) { return; }
				// Models
				if (nbtData.hasKey("OBJData", 9) || nbtData.hasKey("OBJData", 10)) {
					texture = new File(armorDir, name + ".png");
					if (texture.exists()) { return; }
					try { bi = ImageIO.read(Util.instance.getModInputStream("am_i.png")); } catch (Exception e) { bi = new BufferedImage(64, 64, 6); }
					if (ImageIO.write(bi, "png", texture)) {
						LogWriter.debug("Create Default Armor Model Texture for \"" + name + "\" item");
					}
					return;
				}
				else {
					for (int i = 1; i <= 2; i++) {
						texture = new File(armorDir, name + "_layer_" + i + ".png");
						if (!texture.exists()) {
							try { bi = ImageIO.read(Util.instance.getModInputStream("ail" + i + ".png")); } catch (Exception e) { bi = new BufferedImage(64, 32, 6); }
							bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.1f, 0, 40, 40, 255), "png", texture) && bo;
						} else { bo = false; }
					}
				}
				if (bo) {
					LogWriter.debug("Create Default Armor Model Texture for \"" + name + "\" item");
				}
				texture = new File(texturesDir, textureName + ".png");
				switch (((CustomArmor) customitem).getEquipmentSlot()) {
					case HEAD:
						parentName = "ah";
						break;
					case CHEST:
						parentName = "ac";
						break;
					case LEGS:
						parentName = "al";
						break;
					default:
						parentName = "ab";
						break;
				}
			}
			else if (customitem instanceof CustomTool && name.equals("axeexample")) {
				texture = new File(texturesDir, name + ".png");
				if (!texture.exists()) {
					try { bi = ImageIO.read(Util.instance.getModInputStream("axe.png")); } catch (Exception e) { bi = new BufferedImage(64, 32, 6); }
					if (ImageIO.write(this.getBufferImageOffset(bi, 0, 0.0f, 0, 0, 0, 255), "png", texture)) {
						LogWriter.debug("Create Default Texture for \"" + name + "\" item");
					}
				}
				return;
			}
			else if (customitem instanceof CustomBow) {
				for (int i = 0; i < 4; i++) {
					texture = new File(texturesDir, textureName.replace("_standby", "") + (i == 0 ? "_standby" : "_pulling_" + (i - 1)) + ".png");
					if (!texture.exists()) {
						try { bi = ImageIO.read(Util.instance.getModInputStream("b_" + i + ".png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
						bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.0f, 0, 40, 40, 255), "png", texture) && bo;
					}
				}
				if (bo) {
					LogWriter.debug("Create Default Bow Texture for \"" + name + "\" item");
				}
				return;
			}
			else if (customitem instanceof CustomFishingRod) {
				n = textureName.replace("_uncast", "").replace("_cast", "");
				for (int i = 0; i < 2; i++) {
					texture = new File(texturesDir, n + (i == 0 ? "_uncast" : "_cast") + ".png");
					if (!texture.exists()) {
						try { bi = ImageIO.read(Util.instance.getModInputStream("fr_" + i + ".png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
						bo = ImageIO.write(this.getBufferImageOffset(bi, 0, 0.0f, 0, 40, 0, 255), "png", texture) && bo;
					}
				}
				if (bo) {
					LogWriter.debug("Create Default Fishing Rod Texture for \"" + name + "\" item");
				}
				return;
			}
		} catch (IOException e) { LogWriter.error("Error:", e); }

		// simple
		if (parentName == null) {
			texture = new File(texturesDir, textureName + ".png");
			if (customitem instanceof CustomWeapon) {
				parentName = "sw";
			} else if (customitem instanceof CustomTool) {
				parentName = "pa";
			} else if (customitem instanceof CustomShield) {
				parentName = "sh";
			} else if (customitem instanceof CustomFood) {
				parentName = "sc";
			} else {
				parentName = "si";
			}
		}
		if (!texture.exists()) {
			try {
				try { bi = ImageIO.read(Util.instance.getModInputStream(parentName + ".png")); } catch (Exception e) { bi = new BufferedImage(16, 16, 6); }
				if (ImageIO.write(this.getBufferImageOffset(bi, 0, 0.0f, 40, 0, 40, 255), "png", texture)) {
					LogWriter.debug("Create Default Texture for \"" + name + "\" item");
				}
			} catch (IOException e) { LogWriter.error("Error:", e); }
		}
	}

	public void checkParticleFiles(ICustomElement customparticle) {
		super.checkParticleFiles(customparticle);
		String name = customparticle.getCustomName();

		String n = name;
		if (name.equalsIgnoreCase("PARTICLE_EXAMPLE")) {
			n = "Example Custom Particle";
		} else if (name.equalsIgnoreCase("PARTICLE_OBJ_EXAMPLE")) {
			n = "Example Custom OBJ Particle";
		}
		while (n.indexOf('_') != -1) {
			n = n.replace('_', ' ');
		}
		this.setLocalization("particle." + name, n);

		INbt nbt = customparticle.getCustomNbt();
		if (nbt.getMCNBT().hasKey("OBJModel", 8)) {
			File modelDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/models/particle");
			if (!modelDir.exists() && !modelDir.mkdirs()) {
				return;
			}
			name = nbt.getString("OBJModel");
			File modelFile = new File(modelDir, name + ".obj");
            if (!modelFile.exists()) {
				if (Util.instance.saveFile(modelFile, Util.instance.getDataFile("pe_o.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{name}", name))) {
					LogWriter.debug("Create Default OBJ Model for \"" + name + ".obj\" particle");
				}
			}
			File mtlFile = new File(modelDir, name + ".mtl");
			if (!mtlFile.exists()) {
				if (Util.instance.saveFile(mtlFile, Util.instance.getDataFile("pe_m.dat").replace("{mod_id}", CustomNpcs.MODID).replace("{name}", name))) {
					LogWriter.debug("Create Default OBJ Material Library for \"" + name + ".mtl\" particle");
				}
			}
		} else {
			String textureName = nbt.getString("Texture");
			File texturesDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/textures/particle");
			if (!texturesDir.exists() && !texturesDir.mkdirs()) {
				return;
			}
			File texture = new File(texturesDir, textureName + ".png");
			if (!texture.exists()) {
				boolean has = false;
				try {
					IResource baseTexture = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/particle/particles.png"));

					BufferedImage particlesImage = new BufferedImage(128, 128, 6);
					BufferedImage bufferedImage = ImageIO.read(baseTexture.getInputStream());
					for (int u = 0; u < 128; u++) {
						for (int v = 0; v < 128; v++) {
							Color c = new Color(bufferedImage.getRGB(u, v));
							if (c.getRGB() != -16777216) {
								float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
								hsb[0] += 0.25f;
								if (hsb[0] > 1.0f) {
									hsb[0] -= 1.0f;
								}
								c = Color.getHSBColor(hsb[0] - (hsb[0] > 1.0f ? 1.0f : 0.0f), hsb[1], hsb[2]);
								c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 171);
								particlesImage.setRGB(u, v, c.getRGB());
							}
						}
					}
					ImageIO.write(particlesImage, "png", texture);
					has = true;
				} catch (Exception e) { LogWriter.error("Error:", e); }
				if (!has) {
					try {
						BufferedImage bufferedImage = new BufferedImage(128, 128, 6);
						ImageIO.write(bufferedImage, "png", texture);
						has = true;
					} catch (Exception e) { LogWriter.error("Error:", e); }
				}
				if (has) {
					LogWriter.debug("Create Default Texture for \"" + name + "\" particle");
				}
			}
		}
	}

	public void checkPotionFiles(ICustomElement custompotion) {
		super.checkPotionFiles(custompotion);
		String name = custompotion.getCustomName().toLowerCase();

		String n = name;
		if (name.equals("potionexample")) {
			n = "Example Custom Potion";
		}
		while (n.indexOf('_') != -1) {
			n = n.replace('_', ' ');
		}
		this.setLocalization("effect." + name, n);
		this.setLocalization("potion.effect." + name, n);
		this.setLocalization("splash_potion.effect." + name, name.equals("potionexample") ? "Example Custom Splash Potion" : n + " Splash");
		this.setLocalization("lingering_potion.effect." + name, name.equals("potionexample") ? "Example Custom Lingering Potion" : n + " Lingering");
		this.setLocalization("tipped_arrow.effect." + name, name.equals("potionexample") ? "Example Custom Arrow Potion" : n + " Arrow");

        File texturesDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/textures/potions");
		if (!texturesDir.exists() && !texturesDir.mkdirs()) {
			return;
		}
		File texture = new File(texturesDir, name + ".png");
		if (!texture.exists()) {
			boolean has = false;
			try {
				IResource baseTexture = Minecraft.getMinecraft().getResourceManager().getResource(new ResourceLocation("minecraft", "textures/gui/container/inventory.png"));

				BufferedImage potionImage = new BufferedImage(18, 18, 6);
				BufferedImage bufferedImage = ImageIO.read(baseTexture.getInputStream());
				for (int u = 0; u < 18; u++) {
					for (int v = 0; v < 18; v++) {
						Color c = new Color(bufferedImage.getRGB(u + 36, v + 235));
						if (c.getRGB() != -16777216) {
							float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
							hsb[0] += 0.25f;
							if (hsb[0] > 1.0f) {
								hsb[0] -= 1.0f;
							}
							c = Color.getHSBColor(hsb[0] - (hsb[0] > 1.0f ? 1.0f : 0.0f), hsb[1], hsb[2]);
							c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 128);
							potionImage.setRGB(u, v, c.getRGB());
						}
					}
				}
				ImageIO.write(potionImage, "png", texture);
				LogWriter.debug("Create Default Texture for \"" + name + "\" potion");
				has = true;
			} catch (IOException e) { LogWriter.error("Error:", e); }
			if (!has) {
				try {
					BufferedImage bufferedImage = new BufferedImage(18, 18, 6);
					ImageIO.write(bufferedImage, "png", texture);
					LogWriter.debug("Create Default Texture for \"" + name + "\" potion");
				} catch (IOException e) { LogWriter.error("Error:", e); }
			}
		}
	}

	@Override
	public void checkTexture(EntityNPCInterface npc) {
		if (npc.display.skinType != 0) {
			return;
		}
		ClientProxy.createPlayerSkin(new ResourceLocation(npc.display.getSkinTexture()));
	}

	private void createFolders() {
		File dir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID);
		if (!dir.exists() && !dir.mkdirs()) {
			LogWriter.error("Failed to create directory " + dir.getAbsolutePath());
			return;
		}
		File sounds = new File(dir, "sounds");
		if (!sounds.exists() && !sounds.mkdirs()) {
			LogWriter.error("Failed to create directory " + sounds.getAbsolutePath());
		}
		File json = new File(dir, "sounds.json");
		if (!json.exists()) {
			try {
				if (!json.createNewFile()) {
					LogWriter.error("Failed to create file " + json.getAbsolutePath());
				}
				BufferedWriter writer = new BufferedWriter(new FileWriter(json));
				writer.write("{\n\n}");
				writer.close();
			} catch (IOException e) { LogWriter.error("Error:", e); }
		}
		File textures = new File(dir, "textures");
		if (!textures.exists()) {
			LogWriter.error("Failed to create directory " + textures.getAbsolutePath());
		}
	}

	@Override
	public void fixTileEntityData(TileEntity tile) {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.GetTileData, tile.writeToNBT(new NBTTagCompound()));
	}

	private RenderedImage getBufferImageOffset(@Nonnull BufferedImage bufferedImage, int type, float offset, int addRed, int addGreen, int addBlue, int alpha) {
		if (type < 0) {
			type = 0;
		} else if (type > 2) {
			type = 2;
		}
		try {
			for (int u = 0; u < bufferedImage.getWidth(); u++) {
				for (int v = 0; v < bufferedImage.getHeight(); v++) {
					Color c = new Color(bufferedImage.getRGB(u, v));
					if (c.getRGB() == -16777216) {
						continue;
					}
					float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
					hsb[type] += offset;
					hsb[type] %= 1.0f;
					c = Color.getHSBColor(hsb[0], hsb[1], hsb[2]);
					c = new Color((c.getRed() + addRed) % 256, (c.getGreen() + addGreen) % 256, (c.getBlue() + addBlue) % 256, alpha);
					bufferedImage.setRGB(u, v, c.getRGB());
				}
			}
		} catch (Exception e) { LogWriter.error("Error:", e); }
		return bufferedImage;
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

	private GuiScreen getGui(EntityNPCInterface npc, EnumGuiType gui, Container container, int x, int y, int z) {
		ClientEvent.PreGetGuiCustomNpcs preEvent = new ClientEvent.PreGetGuiCustomNpcs(npc, gui, container, x, y, z);
		MinecraftForge.EVENT_BUS.post(preEvent);
		if (preEvent.isCanceled()) { return null; }
		if (preEvent.returnGui != null) { return preEvent.returnGui; }
		GuiScreen returnGui = null;
		switch (gui) {
			case AvailabilityStack: {
				returnGui = new SubGuiNpcAvailabilityItemStacks((ContainerAvailabilityInv) container);
				break;
			}
			case CustomContainer: {
				returnGui = new GuiCustomContainer((ContainerChestCustom) container);
				break;
			}
			case CustomChest: {
				returnGui = new GuiCustomChest((ContainerCustomChest) container);
				break;
			}
			case MainMenuDisplay: {
				if (npc != null) {
					returnGui = new GuiNpcDisplay(npc);
					break;
				}
				getPlayer().sendMessage(new TextComponentString(Util.instance.translateGoogle(getPlayer(), "Unable to find npc")));
				break;
			}
			case MainMenuStats: {
				returnGui = npc == null ? null : new GuiNpcStats(npc);
				break;
			}
			case MainMenuInv: {
				returnGui = new GuiNPCInv(npc, (ContainerNPCInv) container);
				break;
			}
			case MainMenuInvDrop: {
				returnGui = new GuiDropEdit(npc, (ContainerNPCDropSetup) container, (GuiContainer) Minecraft.getMinecraft().currentScreen, x, y, z);
				break;
			}
			case MainMenuAdvanced: {
				returnGui = new GuiNpcAdvanced(npc);
				break;
			}
			case QuestReward: {
				returnGui = new GuiNpcQuestReward(npc, (ContainerNpcQuestReward) container);
				break;
			}
			case QuestTypeItem: {
				Quest quest = NoppesUtilServer.getEditingQuest(getPlayer());
				if (quest != null && quest.questInterface.tasks[x].getEnumType() == EnumQuestTask.ITEM || Objects.requireNonNull(quest).questInterface.tasks[x].getEnumType() == EnumQuestTask.CRAFT) {
					returnGui = new GuiNpcQuestTypeItem(npc, (ContainerNpcQuestTypeItem) container, quest.questInterface.tasks[x]);
				}
				break;
			}
			case QuestRewardItem: {
				returnGui = new GuiNpcQuestRewardItem((ContainerNpcQuestRewardItem) container, x);
				break;
			}
			case MovingPath: {
				returnGui = npc == null ? null : new GuiNpcPather(npc);
				break;
			}
			case ManageFactions: {
				returnGui = new GuiNPCManageFactions(npc);
				break;
			}
			case ManageLinked: {
				returnGui = new GuiNPCManageLinkedNpc(npc);
				break;
			}
			case ManageMail: {
				returnGui = new GuiNPCManageMail(npc);
				break;
			}
			case BuilderBlock: {
				returnGui = new GuiBlockBuilder(x, y, z);
				break;
			}
			case ManageTransport: {
				returnGui = new GuiNPCManageTransporters(npc, (ContainerNPCTransportSetup) container);
				break;
			}
			case ManageRecipes: {
				returnGui = new GuiNPCManageRecipes(npc, (ContainerManageRecipes) container);
				break;
			}
			case ManageDialogs: {
				returnGui = new GuiNPCManageDialogs(npc);
				break;
			}
			case ManageQuests: {
				returnGui = new GuiNPCManageQuest(npc);
				break;
			}
			case ManageBanks: {
				returnGui = new GuiNPCManageBanks(npc, (ContainerManageBanks) container);
				break;
			}
			case MainMenuGlobal: {
				returnGui = new GuiNPCGlobalMainMenu(npc);
				break;
			}
			case MainMenuAI: {
				returnGui = new GuiNpcAI(npc);
				break;
			}
			case PlayerAnvil: {
				returnGui = new GuiNpcCarpentryBench((ContainerCarpentryBench) container);
				break;
			}
			case PlayerFollowerHire: {
				returnGui = new GuiNpcFollowerHire(npc, (ContainerNPCFollowerHire) container);
				break;
			}
			case PlayerFollower: {
				returnGui = new GuiNpcFollower(npc, (ContainerNPCFollowerHire) container);
				break;
			}
			case PlayerTrader: {
				returnGui = new GuiNPCTrader(npc, (ContainerNPCTrader) container);
				break;
			}
			case PlayerBank: {
				returnGui = new GuiNPCBankChest(npc, (ContainerNPCBank) container);
				Minecraft mc = Minecraft.getMinecraft();
				if (mc.currentScreen instanceof GuiNPCBankChest
						&& ((GuiNPCBankChest) mc.currentScreen).cont.bank.id == ((ContainerNPCBank) container).bank.id
						&& ((GuiNPCBankChest) mc.currentScreen).cont.ceil == ((ContainerNPCBank) container).ceil) {
					((GuiNPCBankChest) returnGui).row = ((GuiNPCBankChest) mc.currentScreen).row;
				}
				break;
			}
			case PlayerTransporter: {
				returnGui = new GuiTransportSelection(npc);
				break;
			}
			case Script: {
				returnGui = new GuiScript(npc);
				break;
			}
			case ScriptBlock: {
				returnGui = new GuiScriptBlock(x, y, z);
				break;
			}
			case ScriptItem: {
				returnGui = new GuiScriptItem();
				break;
			}
			case ScriptDoor: {
				returnGui = new GuiScriptDoor(x, y, z);
				break;
			}
			case ScriptPlayers: {
				returnGui = new GuiScriptGlobal();
				break;
			}
			case SetupFollower: {
				returnGui = new GuiNpcFollowerSetup(npc, (ContainerNPCFollowerSetup) container);
				break;
			}
			case SetupItemGiver: {
				returnGui = new GuiNpcItemGiver(npc, (ContainerNpcItemGiver) container);
				break;
			}
			case SetupTrader: {
				if (x >= 0) {
					GuiNPCManageMarkets.marcetId = x;
				}
				if (y >= 0) {
					GuiNPCManageMarkets.dealId = y;
				}
				returnGui = new GuiNPCManageMarkets(npc);
				break;
			}
			case SetupTraderDeal: {
				returnGui = new SubGuiNPCManageDeal(npc, (ContainerNPCTraderSetup) container);
				break;
			}
			case SetupTransporter: {
				returnGui = new GuiNpcTransporter(npc);
				break;
			}
			case SetupBank: {
				returnGui = new GuiNpcBankSetup(npc);
				break;
			}
			case NpcRemote: {
				returnGui = Minecraft.getMinecraft().currentScreen == null ? new GuiNpcRemoteEditor() : null;
				break;
			}
			case PlayerMailbox: {
				returnGui = new GuiMailbox();
				break;
			}
			case PlayerMailOpen: {
				returnGui = new GuiMailmanWrite((ContainerMail) container, x == 1, y == 1);
				break;
			}
			case MerchantAdd: {
				returnGui = new GuiMerchantAdd();
				break;
			}
			case NpcDimensions: {
				returnGui = new GuiNpcDimension();
				break;
			}
			case Border: {
				returnGui = new GuiBorderBlock(x, y, z);
				break;
			}
			case RedstoneBlock: {
				returnGui = new GuiNpcRedstoneBlock(x, y, z);
				break;
			}
			case MobSpawner: {
				returnGui = new GuiNpcMobSpawner(x, y, z);
				break;
			}
			case CopyBlock: {
				returnGui = new GuiBlockCopy(x, y, z);
				break;
			}
			case MobSpawnerMounter: {
				returnGui = new GuiNpcMobSpawnerMounter(x, y, z);
				break;
			}
			case Waypoint: {
				returnGui = new GuiNpcWaypoint(x, y, z);
				break;
			}
			case Companion: {
				returnGui = new GuiNpcCompanionStats(npc);
				break;
			}
			case CompanionTalent: {
				returnGui = new GuiNpcCompanionTalents(npc);
				break;
			}
			case CompanionInv: {
				returnGui = new GuiNpcCompanionInv(npc, (ContainerNPCCompanion) container);
				break;
			}
			case NbtBook: {
				returnGui = new GuiNbtBook(x, y, z);
				break;
			}
			case CustomGui: {
				returnGui = new GuiCustom((ContainerCustomGui) container);
				break;
			}
			case BoundarySetting: {
				returnGui = new GuiBoundarySetting(x, y);
				break;
			}
			case QuestLog: {
				returnGui = new GuiLog(x);
				break;
			}
			case BuilderSetting:
            case ReplaceSetting:
            case PlacerSetting:
            case SaverSetting:
            case RemoverSetting: {
				returnGui = new GuiBuilderSetting((ContainerBuilderSettings) container);
				break;
			}
            case DimensionSetting: {
				returnGui = new GuiCreateDimension(x);
				break;
			}
			case DeadInventory: {
				returnGui = new GuiNPCDeadInventory(npc, (ContainerDead) container);
				break;
			}
			default: {
				returnGui = null;
				break;
			}
		}
		ClientEvent.PostGetGuiCustomNpcs postEvent = new ClientEvent.PostGetGuiCustomNpcs(npc, gui, container, x, y, z, returnGui);
		MinecraftForge.EVENT_BUS.post(postEvent);
		if (postEvent.isCanceled()) { return null; }
		return postEvent.returnGui;
	}

	@Override
	public EntityPlayer getPlayer() {
		return Minecraft.getMinecraft().player;
	}

	@Override
	public PlayerData getPlayerData(EntityPlayer player) {
		if (ClientProxy.playerData.getPlayer() != player) {
			ClientProxy.playerData.setPlayer(player);
		}
		return ClientProxy.playerData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void load() {
		Minecraft mc = Minecraft.getMinecraft();
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
		if (CustomNpcs.InventoryGuiEnabled) {
			MinecraftForge.EVENT_BUS.register(new TabRegistry());
			if (TabRegistry.getTabList().isEmpty()) {
				TabRegistry.registerTab(new InventoryTabVanilla());
				TabRegistry.registerTab(new InventoryTabFactions());
				TabRegistry.registerTab(new InventoryTabQuests());
			}
		}
		// registerEntityRenderingHandler(Class<T> entityClass, IRenderFactory<? super T> renderFactory)
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcPony.class, (Render) new RenderNPCPony());
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcCrystal.class, new RenderNpcCrystal(new ModelNpcCrystal()));
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcDragon.class, new RenderNpcDragon(new ModelNpcDragon(), 0.5f));
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcSlime.class, new RenderNpcSlime(new ModelNpcSlime(16), new ModelNpcSlime(0), 0.25f));
		RenderingRegistry.registerEntityRenderingHandler(EntityProjectile.class, new RenderProjectile());

		// Human Models
		RenderingRegistry.registerEntityRenderingHandler(EntityNPCGolem.class, new RenderNPCInterface(new ModelNPCGolem(0.0f), 0.0f));
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcClassicPlayer.class, new RenderCustomNpc(new ModelNpcAlt(0.0f, false, true)));
		RenderingRegistry.registerEntityRenderingHandler(EntityNPC64x32.class, new RenderCustomNpc(new ModelBipedAlt(0.0f, false, false, false)));
		RenderingRegistry.registerEntityRenderingHandler(EntityCustomNpc.class, new RenderCustomNpc(new ModelNpcAlt(0.0f, false, false)));
		RenderingRegistry.registerEntityRenderingHandler(EntityNpcAlex.class, new RenderCustomNpc(new ModelNpcAlt(0.0f, true, false)));

		mc.getItemColors().registerItemColorHandler((stack, tintIndex) -> 9127187, CustomRegisters.mount, CustomRegisters.cloner, CustomRegisters.moving, CustomRegisters.scripter, CustomRegisters.wand, CustomRegisters.teleporter);
		mc.getItemColors().registerItemColorHandler((stack, tintIndex) -> {
			IItemStack item = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(stack);
			if (stack.getItem() == CustomRegisters.scripted_item) {
				return ((IItemScripted) item).getColor();
			}
			return -1;
		}, CustomRegisters.scripted_item);
		ClientProxy.checkLocalization();
		new GuiTextureSelection(null, "", "png", 0);
		Map<Integer, IParticleFactory> map = ((IParticleManagerMixin) mc.effectRenderer).npcs$getParticleTypes();
		for (int id : CustomRegisters.customparticles.keySet()) {
			if (map.containsKey(id)) {
				continue;
			}
			map.put(id, (particleID, worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, parameters) -> {
                CustomParticleSettings ps = CustomRegisters.customparticles.get(particleID);
                return new CustomParticle(ps == null ? new NBTTagCompound() : ps.nbtData, worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn);
            });
		}
	}

	@Override
	public void openGui(EntityNPCInterface npc, EnumGuiType gui) {
		this.openGui(npc, gui, 0, 0, 0);
	}

	@Override
	public void openGui(EntityNPCInterface npc, EnumGuiType gui, int x, int y, int z) {
		Minecraft mc = Minecraft.getMinecraft();
		Container container = getContainer(gui, mc.player, x, y, z, npc);
		GuiScreen guiscreen = getGui(npc, gui, container, x, y, z);
		if (guiscreen != null) {
			mc.displayGuiScreen(guiscreen);
		}
	}

	@Override
	public void openGui(EntityPlayer player, Object guiscreen) {
		Minecraft mc = Minecraft.getMinecraft();
		if (!player.world.isRemote || !(guiscreen instanceof GuiScreen)) {
			return;
		}
		ClientEvent.NextToGuiCustomNpcs event = new ClientEvent.NextToGuiCustomNpcs(NoppesUtil.getLastNpc(), mc.currentScreen, (GuiScreen) guiscreen);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.returnGui == null || event.isCanceled()) { return; }
		mc.displayGuiScreen(event.returnGui);
		if (mc.currentScreen == null) { mc.setIngameFocus(); }
    }

	@Override
	public void openGui(int i, int j, int k, EnumGuiType gui, EntityPlayer player) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc.player != player) {
			return;
		}
		GuiScreen guiscreen = getGui(null, gui, null, i, j, k);
		if (guiscreen != null) {
			mc.displayGuiScreen(guiscreen);
		}
	}

	@Override
	public void postload() {
		// Set fields and methods in ArmourersWorkshop
		ArmourersWorkshopUtil.getInstance();
		// Banner Model Replace
		TileEntityRendererDispatcher.instance.renderers.put(TileEntityBanner.class, new TileEntityCustomBannerRenderer());
		TileEntityItemStackRendererReflection.setBanner(TileEntityItemStackRenderer.instance, new TileEntityBanner());

		// Shield Model Replace
		Item shield = Item.REGISTRY.getObject(new ResourceLocation("shield"));
		if (shield instanceof ItemShield) { shield.setTileEntityItemStackRenderer(new TileEntityItemStackCustomRenderer()); }
		// OBJ ItemStack Model Replace
		Minecraft mc = Minecraft.getMinecraft();
		RenderItem ri = mc.getRenderItem();
		Map<IRegistryDelegate<Item>, Int2ObjectMap<IBakedModel>> models = ItemModelMesherForgeReflection.getModels(ri.getItemModelMesher());
		if (models != null) {
			for (IRegistryDelegate<Item> key : models.keySet()) {
				if (!(key.get() instanceof CustomArmor) || ((CustomArmor) key.get()).objModel == null) {
					continue;
				}
				IBakedModel ibm = ModelBuffer.getIBakedModel((CustomArmor) key.get());
				if (ibm == null) {
					continue;
				}
				models.get(key).put(0, ibm);
			}
		}
		ClientProxy.mcWrapper = new WrapperMinecraft(mc);
		checkLocalization();
	}

	@Override
	public void preload() {
		ClientProxy.Font = new FontContainer(CustomNpcs.FontType, CustomNpcs.FontSize);
		this.createFolders();
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new CustomNpcResourceListener());
		CustomNpcs.Channel.register(new PacketHandlerClient());
		CustomNpcs.ChannelPlayer.register(new PacketHandlerPlayer());
		new MusicController();
		MinecraftForge.EVENT_BUS.register(new ClientTickHandler());
		MinecraftForge.EVENT_BUS.register(new ClientGuiEventHandler());

		if (CustomNpcs.SceneButtonsEnabled) {
			ClientProxy.Scene1 = new KeyBinding("key.scene.s.e.0", 79, "key.categories.gameplay");
			ClientProxy.Scene2 = new KeyBinding("key.scene.s.e.1", 80, "key.categories.gameplay");
			ClientProxy.Scene3 = new KeyBinding("key.scene.s.e.2", 81, "key.categories.gameplay");
			ClientProxy.SceneReset = new KeyBinding("key.scene.reset", 82, "key.categories.gameplay");
			ClientRegistry.registerKeyBinding(ClientProxy.Scene1);
			ClientRegistry.registerKeyBinding(ClientProxy.Scene2);
			ClientRegistry.registerKeyBinding(ClientProxy.Scene3);
			ClientRegistry.registerKeyBinding(ClientProxy.SceneReset);
		}
		ClientRegistry.registerKeyBinding(ClientProxy.QuestLog);
		for (IKeySetting ks : KeyController.getInstance().getKeySettings()) {
			KeyModifier modifer;
			switch (ks.getModiferType()) {
				case 1:
					modifer = KeyModifier.SHIFT;
					break;
				case 2:
					modifer = KeyModifier.CONTROL;
					break;
				case 3:
					modifer = KeyModifier.ALT;
					break;
				default:
					modifer = KeyModifier.NONE;
					break;
			}
			ClientRegistry.registerKeyBinding(new KeyBinding(ks.getName(), KeyConflictContext.IN_GAME, modifer, ks.getKeyId(), ks.getCategory()));
		}
		new PresetController(CustomNpcs.Dir);
		if (CustomNpcs.EnableUpdateChecker) {
			VersionChecker checker = new VersionChecker();
			checker.start();
		}
		PixelmonHelper.loadClient();
		OBJLoader.INSTANCE.addDomain(CustomNpcs.MODID);
	}

	@Override
	public void reloadItemTextures() {
		for (Map.Entry<Integer, String> entry : ItemScripted.Resources.entrySet()) {
			ModelResourceLocation mrl = new ModelResourceLocation(entry.getValue(), "inventory");
			Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(CustomRegisters.scripted_item,
					entry.getKey(), mrl);
			ModelLoader.setCustomModelResourceLocation(CustomRegisters.scripted_item, entry.getKey(), mrl);
		}
	}

	private void setLocalization(String key, String value) {
		File langDir = new File(CustomNpcs.Dir, "assets/" + CustomNpcs.MODID + "/lang");
		if (!langDir.exists() && !langDir.mkdirs()) { return; }
		BufferedWriter writer;
		boolean isExample = key.contains("example") && value.contains("Example");
		boolean isTranslate = false;
		String currentLanguage = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
		String translateValue = value;
		if (!currentLanguage.equals("en_us")) {
			String language = currentLanguage;
			if (currentLanguage.contains("_")) {
				if (currentLanguage.equals("zh_cn")) { language = "zh_CN"; }
				else if (currentLanguage.equals("zh_tw")) { language = "zh_TW"; }
				else { language = currentLanguage.substring(0, currentLanguage.indexOf("_")); }
			}
			if (isExample) {
				translateValue = Util.instance.translateGoogle("en", language, value);
				if (translateValue.equals(value)) { return; }
				isTranslate = true;
			} else {
				value = Util.instance.translateGoogle(language, "en", translateValue);
			}
		}

		boolean write = false;
		for (int i = 0; i < 2; i++) {
			if (i == 1 && currentLanguage.equals("en_us")) {
				break;
			}
			File lang = new File(langDir, (i == 0 ? "en_us" : currentLanguage) + ".lang");
			Map<String, String> jsonMap = new TreeMap<>();
			jsonMap.put(key, (i == 0 ? value : translateValue));
			char chr = Character.toChars(0x000A)[0];
			writer = null;
			if (!lang.exists()) {
				try {
					writer = Files.newBufferedWriter(lang.toPath());
				} catch (IOException e) { LogWriter.error("Error:", e); }
			} else {
				try {
					BufferedReader reader = Files.newBufferedReader(lang.toPath());
					String line;
					while ((line = reader.readLine()) != null) {
						if (!line.contains("=")) {
							continue;
						}
						String[] vk = line.split("=");
						if (vk[0].equals(key)) {
							if (isExample && !isTranslate) {
								jsonMap.put(vk[0], vk[1]);
							}
							continue;
						}
						jsonMap.put(vk[0], vk[1]);
					}
					reader.close();
					writer = Files.newBufferedWriter(lang.toPath());
				} catch (IOException e) { LogWriter.error("Error:", e); }
			}
			if (writer != null && !jsonMap.isEmpty()) {
				try {
					StringBuilder jsonStr = new StringBuilder();
                    String str = "";
                    for (String k : jsonMap.keySet()) {
						String pre = k.contains(".") ? k.substring(0, k.indexOf(".")) : k;
						if (!str.isEmpty() && !str.equals(pre)) {
							jsonStr.append(chr);
						}
						str = pre;
						jsonStr.append(k).append("=").append(jsonMap.get(k)).append(chr);
					}
					writer.write(jsonStr.toString());
					writer.close();
					write = true;
				} catch (IOException e) { LogWriter.error("Error:", e); }
			}
		}
		if (write) {
			LogWriter.debug("Create Default Localization key \"" + key + "\"");
		}
	}

	@Override
	public void spawnParticle(EntityLivingBase player, String string, Object... ob) {
		if (string.equals("Block")) {
			BlockPos pos = (BlockPos) ob[0];
			int id = (int) ob[1];
			Block block = Block.getBlockById(id & 0xFFF);
			Minecraft.getMinecraft().effectRenderer.addBlockDestroyEffects(pos, block.getStateFromMeta(id >> 12 & 0xFF));
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
				minecraft.effectRenderer.addEffect(fx);
			}
		}
	}

	@Override
	public void spawnParticle(EnumParticleTypes particle, double x, double y, double z, double motionX, double motionY, double motionZ, float scale) {
		Entity entity = Minecraft.getMinecraft().getRenderViewEntity();
		if (entity == null) { return; }
		double xx = entity.posX - x;
		double yy = entity.posY - y;
		double zz = entity.posZ - z;
		if (xx * xx + yy * yy + zz * zz > 256.0) {
			return;
		}
		Particle fx = Minecraft.getMinecraft().effectRenderer.spawnEffectParticle(particle.getParticleID(), x, y, z, motionX, motionY, motionZ);
		if (fx == null) {
			return;
		}
		if (particle == EnumParticleTypes.FLAME) {
			((IParticleFlameMixin) fx).npcs$setFlameScale(scale);
		} else if (particle == EnumParticleTypes.SMOKE_NORMAL) {
			((IParticleSmokeNormalMixin) fx).npcs$setSmokeParticleScale(scale);
		}
	}

	public void updateGUI() {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if (gui == null) {
			return;
		}
		gui.initGui();
	}

	@Override
	public void updateKeys() {
		List<KeyBinding> list = new ArrayList<>();
		for (KeyBinding kb : Minecraft.getMinecraft().gameSettings.keyBindings) {
			if (ClientProxy.keyBindingMap.containsValue(kb)) {
				continue;
			}
			list.add(kb);
		}
		Map<Integer, KeyBinding> keysMap = new HashMap<>();
		for (IKeySetting ks : KeyController.getInstance().getKeySettings()) {
			KeyModifier modifer;
			switch (ks.getModiferType()) {
				case 1:
					modifer = KeyModifier.SHIFT;
					break;
				case 2:
					modifer = KeyModifier.CONTROL;
					break;
				case 3:
					modifer = KeyModifier.ALT;
					break;
				default:
					modifer = KeyModifier.NONE;
					break;
			}

			boolean added = true;
			for (KeyBinding kbD : list) {
				if (kbD.getKeyModifier() == modifer &&
						kbD.getKeyDescription().equals(ks.getName()) &&
						kbD.getKeyCodeDefault() == ks.getKeyId() &&
						kbD.getKeyCategory().equals(ks.getCategory())) {
					added = false;
					break;
				}
			}

			if (added) {
				KeyBinding kb;
				if (ClientProxy.keyBindingMap.containsKey(ks.getId())) {
					kb = ClientProxy.keyBindingMap.get(ks.getId());
					KeyBindingReflection.setModifier(kb, modifer);
					KeyBindingReflection.setKeyDescription(kb, ks.getName());
					KeyBindingReflection.setKeyCodeDefault(kb, ks.getKeyId());
					KeyBindingReflection.setKeyCategory(kb, ks.getCategory());
				} else {
					kb = new KeyBinding(ks.getName(), KeyConflictContext.IN_GAME, modifer, ks.getKeyId(), ks.getCategory());
				}
				list.add(kb);
				keysMap.put(ks.getId(), kb);
			}
		}
		ClientProxy.keyBindingMap.clear();
		ClientProxy.keyBindingMap.putAll(keysMap);
		Minecraft.getMinecraft().gameSettings.keyBindings = list.toArray(new KeyBinding[0]);
	}

	@Override
	public void applyRecipe(INpcRecipe recipe, boolean added) {
		if (recipe == null) { return; }
		super.applyRecipe(recipe, added);
		final RecipeBook book;
		if (Minecraft.getMinecraft().player != null) { book = Minecraft.getMinecraft().player.getRecipeBook(); }
		else { book = null; }
        /*
		 * Since recipes can be created and deleted during the game,
		 * the "newRecipeList(CreativeTabs)" method can be ignored.
		 * Check and create recipe lists for such cases:
		 */
		CreativeTabNpcs tab = recipe.isGlobal() ? CustomRegisters.tab : CustomRegisters.tabItems;
		if (!RecipeBookClient.RECIPES_BY_TAB.containsKey(tab)) {
			RecipeList recipelist = new RecipeList();
			RecipeBookClient.ALL_RECIPES.add(recipelist);
			(RecipeBookClient.RECIPES_BY_TAB.computeIfAbsent(tab, (hasRecipeList) -> new ArrayList<>())).add(recipelist);
			(RecipeBookClient.RECIPES_BY_TAB.computeIfAbsent(CreativeTabs.SEARCH, (hasRecipeList) -> new ArrayList<>())).add(recipelist);
		}
		RecipeList recipeList = null;
		boolean isWork = false; // add or copy or remove
		for (RecipeList rl : RecipeBookClient.RECIPES_BY_TAB.get(tab)) {
			if (((IRecipeListMixin) rl).npcs$getGroup().equals(recipe.getNpcGroup())) {
				isWork = ((IRecipeListMixin) rl).npcs$applyRecipe(recipe, added);
				recipeList = rl;
				break;
			}
		}
		if (!isWork) {
			if (added) {
				// Create a new recipe list
				RecipeList newRecipeList = new RecipeList();
				newRecipeList.add((IRecipe) recipe);
				RecipeBookClient.RECIPES_BY_TAB.get(tab).add(newRecipeList);
				RecipeBookClient.ALL_RECIPES.add(newRecipeList);
				if (book != null) { newRecipeList.updateKnownRecipes(book); }
			}
		}
		else if (!added && recipeList.getRecipes().isEmpty()) {
			// Removing empty recipe lists
			RecipeBookClient.RECIPES_BY_TAB.get(tab).remove(recipeList);
			RecipeBookClient.ALL_RECIPES.remove(recipeList);
		}
		if (book != null && (!added || recipe.isKnown())) {
			// recipe lock / unlock
			if (!added) { book.lock((IRecipe) recipe); } else { book.unlock((IRecipe) recipe); }
			RecipeBookClient.ALL_RECIPES.forEach((recipes) -> recipes.updateKnownRecipes(book));
		}
	}

	@Override
	public String getTranslateLanguage(EntityPlayer player) {
		String lang = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
		if (lang.contains("_")) { lang = lang.substring(0, lang.indexOf("_")); }
		return lang;
	}

	public void loadAnimationModel(AnimationConfig animation) {
		ModelNpcAlt.loadAnimationModel(animation);
	}

}
