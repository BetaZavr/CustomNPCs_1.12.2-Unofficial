package noppes.npcs.client.gui.select;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.reflection.client.renderer.texture.TextureManagerReflection;
import noppes.npcs.reflection.client.resources.*;
import noppes.npcs.reflection.client.renderer.texture.TextureMapReflection;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class GuiTextureSelection
extends SubGuiInterface
implements ICustomScrollListener {

	protected GuiCustomScroll scroll;
	protected final Map<String, TreeMap<ResourceLocation, Long>> data = new TreeMap<>(); // (Directory, Files)
	protected ResourceLocation selectDir;
	protected final String suffix;
	protected final int type;
	protected final String back = "   " + Character.toChars(0x2190)[0] + " (" + new TextComponentTranslation("gui.back").getFormattedText() + ")";
	protected String baseResource = "";
	protected EntityNPCInterface displayNPC;

	public ResourceLocation resource;

	public static boolean dark = false;

	public GuiTextureSelection(int id, EntityNPCInterface npcIn, String texture, String suffix, int type) {
		this(npcIn, texture, suffix, type);
		this.id = id;
	}
	
	public GuiTextureSelection(EntityNPCInterface npcIn, @Nonnull String texture, String suffixIn, int typeIn) {
		super(npcIn);
		if (npc != null) {
			displayNPC = Util.instance.copyToGUI(npc, mc.world, false);
		}
		drawDefaultBackground = false;
		title = "";
		setBackground("menubg.png");
		xSize = 366;
		ySize = 226;

		this.type = typeIn;
		selectDir = null;
		suffix = suffixIn.toLowerCase();
		if (ClientProxy.texturesData.containsKey(suffix)) {
			data.putAll(ClientProxy.texturesData.get(suffix));
		}
		ResourceLocation loc = new ResourceLocation(texture);
		if (data.containsKey(loc.getResourceDomain()) && !data.get(loc.getResourceDomain()).containsKey(loc)) {
			try {
				if (!texture.isEmpty()) { mc.getTextureManager().bindTexture(loc); }
			}
			catch (Exception ignored) { }
			data.remove(loc.getResourceDomain());
		}
		if (!data.containsKey(loc.getResourceDomain())) {
			resetFiles();
			ClientProxy.texturesData.put(suffix, data);
		}
		baseResource = texture;
		if (texture.isEmpty()) {
			if (selectDir == null) {
				switch (type) {
					case 1: {
						selectDir = new ResourceLocation(CustomNpcs.MODID, "textures/cloak");
						break;
					}
					case 2: {
						selectDir = new ResourceLocation(CustomNpcs.MODID, "textures/overlays");
						break;
					}
					case 3: {
						selectDir = new ResourceLocation(CustomNpcs.MODID, "textures/gui");
						break;
					}
					default: {
						selectDir = new ResourceLocation(CustomNpcs.MODID, "textures/entity/humanmale");
					}
				}
			}
			return;
		}
		resource = new ResourceLocation(texture);
		if (texture.lastIndexOf("/") != -1) {
			texture = texture.substring(0, texture.lastIndexOf("/"));
		}
		selectDir = new ResourceLocation(texture);
		if (!data.containsKey(selectDir.getResourceDomain())) {
			selectDir = null;
			return;
		}
		for (ResourceLocation r : data.get(selectDir.getResourceDomain()).keySet()) {
			if (r.getResourcePath().indexOf(selectDir.getResourcePath()) == 0) {
				return;
			}
		}
		selectDir = null;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() == 3) {
			GuiTextureSelection.dark = ((GuiNpcCheckBox) button).isSelected();
			return;
		}
		String res = baseResource;
		if (button.getID() == 2 && resource != null) { res = resource.toString(); }

		if (npc != null && type >= 0 && type <= 2) {
			switch (type) {
				case 1: {
					npc.display.setCapeTexture(res);
					displayNPC.display.setCapeTexture(res);
					break;
				}
				case 2: {
					npc.display.setOverlayTexture(res);
					displayNPC.display.setOverlayTexture(res);
					break;
				}
				default: {
					npc.display.setSkinTexture(res);
					displayNPC.display.setSkinTexture(res);
				}
			}
			npc.textureLocation = null;
			displayNPC.textureLocation = null;
		}
		close();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		GlStateManager.pushMatrix();
		GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
		int x = guiLeft + 271, y = guiTop + 6;
		int w = 80;
		if (type == 4) { // faction flag
			GlStateManager.translate(19.5f, 0.0f, 0.0f);
			w = 41;
		}
		Gui.drawRect(x - 1, y - 1, x + w + 1, y + 81, GuiTextureSelection.dark ?
				new Color(0xFFE0E0E0).getRGB() :
				new Color(0xFF202020).getRGB());
		Gui.drawRect(x, y, x + w, y + 80, GuiTextureSelection.dark ?
				new Color(0xFF000000).getRGB() :
				new Color(0xFFFFFFFF).getRGB());
		int g = 5;
		for (int u = 0; u < w / g; u++) {
			for (int v = 0; v < 80 / g; v++) {
				if (u % 2 == (v % 2 == 0 ? 1 : 0)) {
					Gui.drawRect(x + u * g, y + v * g, x + u * g + g, y + v * g + g, GuiTextureSelection.dark ?
							new Color(0xFF343434).getRGB() :
							new Color(0xFFCCCCCC).getRGB());
				}
			}
		}
		GlStateManager.popMatrix();
		if (resource != null) {
			float scale = 80.0f / 256.0f;
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.translate(x, y, 0.0f);
			GlStateManager.scale(scale, scale, 1.0f);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			try {
				int tX = 0;
				int tY = 0;
				int tW = 256;
				int tH = 256;
				int tS = 256;
				if (type == 4) { // faction flag
					GlStateManager.translate(62.0f, 0.0f, 0.0f);
					GlStateManager.scale(3.3f, 2.0f, 1.0f);
					tX = 4;
					tY = 4;
					tW = 40;
					tH = 128;
				}
				GuiNpcUtil.drawTexturedModalRect(resource, tX, tY, tW, tH, tS);
			} catch (Exception e) { LogWriter.error(e); }
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
		if (npc != null && type >= 0 && type <= 2) {
			if (type == 0) {
				npc.textureLocation = resource;
				displayNPC.textureLocation = resource;
			}
			int rot;
			float s = 1.25f;
			int mouse = 0;
			x = 0;
			y = 0;
			if (type == 0) {
				rot = (int) (3 * player.world.getTotalWorldTime() % 360);
			} else if (type == 1) {
				rot = 215;
			} else {
				rot = 325;
			}
			if (npc.textureLocation != null) {
				drawNpc(displayNPC, guiLeft + 276 + x, guiTop + 155 + y, s, rot, 0, mouse);
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		GuiNpcButton button = new GuiNpcButton(1, guiLeft + 264, guiTop + 190, 90, 20, "gui.cancel");
		button.setHoverText("hover.back");
		addButton(button);
		button = new GuiNpcButton(2, guiLeft + 264, guiTop + 170, 90, 20, "gui.done");
		button.setHoverText("texture.hover.done");
		addButton(button);
		button = new GuiNpcCheckBox(3, guiLeft + 256, guiTop + 2, 15, 15, null, null, GuiTextureSelection.dark);
		button.setHoverText("texture.hover.dark");
		addButton(button);
		if (scroll == null) { (scroll = new GuiCustomScroll(this, 0)).setSize(250, 199); }
		scroll.guiLeft = guiLeft + 4;
		scroll.guiTop = guiTop + 14;
		String domain = "All Data in Game/";
		if (selectDir == null) {
			scroll.setList(new ArrayList<>(data.keySet()));
		} else {
			List<String> list = new ArrayList<>();
			Map<String, Long> ds = new TreeMap<>();
			Map<String, Long> fs = new TreeMap<>();
			String path = selectDir.getResourcePath();
			for (ResourceLocation res : data.get(selectDir.getResourceDomain()).keySet()) {
				if (res.getResourcePath().indexOf(path) == 0) {
					String key = res.getResourcePath().substring(path.length() + 1);
					if (key.contains("/")) {
						ds.put(key.substring(0, key.indexOf("/")), data.get(selectDir.getResourceDomain()).get(res));
					} else if ((suffix.isEmpty() || res.getResourcePath().toLowerCase().endsWith(suffix))) {
						fs.put(res.getResourcePath().substring(res.getResourcePath().lastIndexOf("/") + 1), data.get(selectDir.getResourceDomain()).get(res));
					}
				}
			}
			String txrName = resource != null ? resource.getResourcePath() : "";
			if (!txrName.isEmpty()) {
				txrName = txrName.substring(txrName.lastIndexOf("/") + 1);
			}
			List<Integer> colors = new ArrayList<>();
			List<String> suffixes = new ArrayList<>();
			int i = 1, pos = -1;
			suffixes.add("");
			colors.add(new Color(0xF3BE1E).getRGB());
			for (String key : ds.keySet()) {
				suffixes.add("");
				list.add(key);
				colors.add(new Color(0xF3BE1E).getRGB());
				i++;
			}
			for (String key : fs.keySet()) {
				suffixes.add(Util.instance.getTextReducedNumber(fs.get(key), false, false, true) + "b");
				list.add(key);
				colors.add(new Color(0xCAEAEA).getRGB());
				if (txrName.equals(key)) {
					pos = i;
				}
				i++;
			}
			list.add(0, back);
			scroll.setColors(colors);
			scroll.setSuffixes(suffixes);
			scroll.setListNotSorted(list);
			if (scroll.getSelect() != pos) {
				scroll.setSelect(pos);
			}
			scroll.resetRoll();
			char chr = ((char) 167);
			domain = chr + "l" + selectDir.getResourceDomain() + "/" + path;
			while (mc.fontRenderer.getStringWidth(domain) > 250 && path.contains("/")) {
				path = path.substring(path.indexOf("/") + 1);
				domain = chr + "l" + selectDir.getResourceDomain() + "/.../" + path;
			}
		}
		addScroll(scroll);
		addLabel(new GuiNpcLabel(0, domain, guiLeft + 4, guiTop + 4));
		getLabel(0).setColor(new Color(new Color(0xFF000000).getRGB()).getRGB());
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (scroll.getSelected().equals(back)) {
			if (selectDir == null) { return; }
			if (!selectDir.getResourcePath().contains("/")) {
				selectDir = null;
			} else {
				selectDir = new ResourceLocation(selectDir.getResourceDomain(), selectDir.getResourcePath().substring(0, selectDir.getResourcePath().lastIndexOf("/")));
			}
			initGui();
		}
		else if (selectDir != null) {
			if (!scroll.getSelected().endsWith(suffix)) {
				selectDir = new ResourceLocation(selectDir.getResourceDomain(), selectDir.getResourcePath() + "/" + scroll.getSelected());
				initGui();
			} else {
				resource = new ResourceLocation(selectDir.getResourceDomain(), selectDir.getResourcePath() + "/" + scroll.getSelected());
				if (npc != null && type >= 0 && type <= 2) {
					switch (type) {
						case 1: {
							npc.display.setCapeTexture(resource.toString());
							break;
						}
						case 2: {
							npc.display.setOverlayTexture(resource.toString());
							break;
						}
						default: {
							npc.display.setSkinTexture(resource.toString());
						}
					}
				}
			}
		} else if (data.containsKey(scroll.getSelected())) {
			String res = null, def = null;
			for (ResourceLocation loc : data.get(scroll.getSelected()).keySet()) {
				if (def == null) {
					def = loc.getResourcePath().substring(0, loc.getResourcePath().indexOf("/"));
				}
				if (loc.getResourcePath().substring(0, loc.getResourcePath().indexOf("/")).equals("textures")) {
					res = "textures";
					break;
				}
			}
			if (res == null && def != null) {
				res =  def;
			}
			if (res == null) { return; }
			selectDir = new ResourceLocation(scroll.getSelected(), res);
			initGui();
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) {
		if (resource != null) {
			if (npc != null && type >= 0 && type <= 2) {
				switch (type) {
					case 1: {
						npc.display.setCapeTexture(resource.toString());
						displayNPC.display.setCapeTexture(resource.toString());
						break;
					}
					case 2: {
						npc.display.setOverlayTexture(resource.toString());
						displayNPC.display.setOverlayTexture(resource.toString());
						break;
					}
					default: {
						npc.display.setSkinTexture(resource.toString());
						displayNPC.display.setSkinTexture(resource.toString());
					}
				}
			}
			close();
			parent.initGui();
		}
	}

	private void progressFile(File file) {
		try {
			if (!file.isDirectory() && (file.getName().endsWith(".jar") || file.getName().endsWith(".zip"))) {
				ZipFile zip = new ZipFile(file);
				Enumeration<? extends ZipEntry> entries = zip.entries();
				while (entries.hasMoreElements()) {
					ZipEntry zipentry = entries.nextElement();
					String entryName = zipentry.getName();
					int a = entryName.indexOf("assets");
					int t = entryName.indexOf("texture", a);
					if (a != -1 && t != -1) {
						addFile(entryName, zipentry.getSize());
					}
				}
				zip.close();
			} else if (file.isDirectory()) {
				checkFolder(file);
			}
		} catch (Exception e) { LogWriter.error(e); }
	}

	private void resetFiles() {
		data.clear();
		mc = Minecraft.getMinecraft();
		/* Texture manager data */
		for (ResourceLocation key : TextureManagerReflection.getMapTextureObjects(mc.getTextureManager()).keySet()) {
			addFile(key);
		}
		/* Texture blocks data */
		for (String key : TextureMapReflection.getMapRegisteredSprites(mc.getTextureMapBlocks()).keySet()) {
			try {
				addFile(new ResourceLocation(key.substring(0, key.indexOf(":")), "textures/" + key.substring(key.indexOf(":") +1) + ".png"));
			}
			catch (Exception ignored) { }
		}
		/* Resource manager data */
		Map<String, FallbackResourceManager> map = IResourceManagerReflection.getDomainResourceManagers(mc.getResourceManager());
		if (map == null) { return; }
		for (String name : map.keySet()) {
			List<IResourcePack> list = FallbackResourceManagerReflection.getResourcePacks(map.get(name));
			if (list == null) { return; }
			for (IResourcePack pack : list) {
				if (pack instanceof LegacyV2Adapter) {
					pack = LegacyV2AdapterReflection.getIResourcePack((LegacyV2Adapter) pack);
				}
				if (pack instanceof DefaultResourcePack) {
					ResourceIndex resourceIndex = DefaultResourcePackReflection.getResourceIndex((DefaultResourcePack) pack);
					Map<String, File> resourceMap = ResourceIndexReflection.getResourceMap(resourceIndex);
					for (String key : resourceMap.keySet()) {
						File f = resourceMap.get(key);
						addFile(key, f.length());
					}
				}
				else if (pack instanceof AbstractResourcePack) {
					File directory = AbstractResourcePackReflection.getResourcePackFile((AbstractResourcePack) pack);
					if (directory == null || !directory.isDirectory()) { continue; }
					File dir = new File(directory, "assets");
					if (!dir.exists() || !dir.isDirectory()) { continue; }
					checkFolder(dir);
				}
			}
		}
		/* Mod jars */
		for (ModContainer mod : Loader.instance().getModList()) {
			if (mod.getSource().exists()) {
				progressFile(mod.getSource());
			}
		}
		/* Resource packs */
		ResourcePackRepository repos = Minecraft.getMinecraft().getResourcePackRepository();
		for (ResourcePackRepository.Entry entry : repos.getRepositoryEntries()) {
			File file = new File(repos.getDirResourcepacks(), entry.getResourcePackName());
			if (file.exists()) {
				progressFile(file);
			}
		}
		/* Custom mod resources */
		checkFolder(new File(CustomNpcs.Dir, "assets"));
	}

	private void addFile(ResourceLocation location) {
		String path = location.getResourcePath();
		if (!suffix.isEmpty() && !path.toLowerCase().endsWith(suffix)) { return; }
		String domain = location.getResourceDomain();
		if (!data.containsKey(domain)) {
			data.put(domain, new TreeMap<>());
		} else {
			for (ResourceLocation r : data.get(domain).keySet()) {
				if (r.getResourcePath().equals(path)) {
					return;
				}
			}
		}
		long size = 0L;
		try {
			IResource res = Minecraft.getMinecraft().getResourceManager().getResource(location);
			try (InputStream inputStream = res.getInputStream()) { // Ваш InputStream
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				int readByte;
				while ((readByte = inputStream.read()) != -1) {
					byteArrayOutputStream.write(readByte);
				}
				byte[] bytes = byteArrayOutputStream.toByteArray();
				size = bytes.length;
			}
		}
		catch (Exception ignored) { }
		data.get(domain).put(location, size);
	}

	private void addFile(String path, long size) {
		if (!suffix.isEmpty() && !path.toLowerCase().endsWith(suffix)) { return; }
		if (path == null || !path.contains("assets")) { return; }
		if (path.contains("\\")) {
			List<String> list = new ArrayList<>();
			while (path.contains("\\")) {
				list.add(path.substring(0, path.indexOf("\\")));
				path = path.substring(path.indexOf("\\") + 1);
			}
			list.add(path);
			StringBuilder pathBuilder = new StringBuilder();
			for (String p : list) {
				pathBuilder.append(p).append("/");
			}
			path = pathBuilder.toString();
			path = path.substring(0, path.length() - 1);
		}
		path = path.substring(path.lastIndexOf("assets") + 7);
		String domain = path.substring(0, path.indexOf("/"));
		if (domain.isEmpty()) { return; }
		path = path.substring(path.indexOf("/") + 1);
		if (!path.startsWith("textures")) { return; }
		ResourceLocation res = new ResourceLocation(domain, path);
		if (!data.containsKey(domain)) {
			data.put(domain, new TreeMap<>());
		} else {
			for (ResourceLocation r : data.get(domain).keySet()) {
				if (r.getResourcePath().equals(path)) { return; }
			}
		}
		data.get(domain).put(res, size);
	}

	private void checkFolder(File file) {
		File[] files = file.listFiles();
		if (files == null) {
			return;
		}
		for (File f : files) {
			if (f.isDirectory()) {
				checkFolder(f);
				continue;
			}
			addFile(f.getAbsolutePath(), f.length());
		}
	}

}
