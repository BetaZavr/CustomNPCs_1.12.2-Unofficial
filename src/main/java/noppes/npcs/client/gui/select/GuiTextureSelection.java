package noppes.npcs.client.gui.select;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.mixin.client.renderer.texture.ITextureManagerMixin;
import noppes.npcs.api.mixin.client.renderer.texture.ITextureMapMixin;
import noppes.npcs.api.mixin.client.resources.*;
import noppes.npcs.api.mixin.client.resources.ILegacyV2AdapterMixin;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class GuiTextureSelection extends SubGuiInterface implements ICustomScrollListener {

	private GuiCustomScroll scroll;
	public ResourceLocation resource;
	private final Map<String, TreeMap<ResourceLocation, Long>> data = new TreeMap<>(); // (Directory, Files)
	private ResourceLocation selectDir;
	private final String suffix;
	private final int type;
    private int showName = 0;
	private final String back = "   " + Character.toChars(0x2190)[0] + " (" + new TextComponentTranslation("gui.back").getFormattedText() + ")";
	private String baseResource = "";
	public static boolean dark = false;

	public GuiTextureSelection(int id, EntityNPCInterface npc, String texture, String suffix, int type) {
		this(npc, texture, suffix, type);
		this.id = id;
	}
	
	public GuiTextureSelection(EntityNPCInterface npc, String texture, String suffix, int type) {
		super(npc);
		if (this.npc != null) {
			this.showName = this.npc.display.getShowName();
			this.npc.display.setShowName(2);
		}
		this.drawDefaultBackground = false;
		this.title = "";
		this.setBackground("menubg.png");
		this.xSize = 366;
		this.ySize = 226;
		this.type = type;
		this.selectDir = null;
		this.suffix = suffix.toLowerCase();
		if (ClientProxy.texturesData.containsKey(suffix)) {
			data.putAll(ClientProxy.texturesData.get(suffix));
		}
		ResourceLocation loc = new ResourceLocation(texture);
		if (data.containsKey(loc.getResourceDomain()) && !data.get(loc.getResourceDomain()).containsKey(loc)) {
			mc.getTextureManager().bindTexture(loc);
			data.remove(loc.getResourceDomain());
		}
		if (!data.containsKey(loc.getResourceDomain())) {
			resetFiles();
			ClientProxy.texturesData.put(suffix, data);
		}
		this.baseResource = texture;
		if (texture.isEmpty()) {
			if (this.selectDir == null) {
				switch (this.type) {
					case 1: {
						this.selectDir = new ResourceLocation(CustomNpcs.MODID, "textures/cloak");
						break;
					}
					case 2: {
						this.selectDir = new ResourceLocation(CustomNpcs.MODID, "textures/overlays");
						break;
					}
					case 3: {
						this.selectDir = new ResourceLocation(CustomNpcs.MODID, "textures/gui");
						break;
					}
					default: {
						this.selectDir = new ResourceLocation(CustomNpcs.MODID, "textures/entity/humanmale");
					}
				}
			}
			return;
		}
		this.resource = new ResourceLocation(texture);
		if (texture.lastIndexOf("/") != -1) {
			texture = texture.substring(0, texture.lastIndexOf("/"));
		}
		this.selectDir = new ResourceLocation(texture);
		if (!data.containsKey(selectDir.getResourceDomain())) {
			this.selectDir = null;
			return;
		}
		for (ResourceLocation r : this.data.get(this.selectDir.getResourceDomain()).keySet()) {
			if (r.getResourcePath().indexOf(this.selectDir.getResourcePath()) == 0) {
				return;
			}
		}
		this.selectDir = null;
	}

	@Override
	public void actionPerformed(@Nonnull GuiButton guibutton) {
		if (guibutton.id == 3) {
			GuiTextureSelection.dark = ((GuiNpcCheckBox) guibutton).isSelected();
			return;
		}
		super.actionPerformed(guibutton);
		String res = this.baseResource;
		if (guibutton.id == 2 && this.resource != null) {
			res = this.resource.toString();
		}
		if (this.npc != null && this.type >= 0 && this.type <= 2) {
			switch (this.type) {
			case 1: {
				this.npc.display.setCapeTexture(res);
				break;
			}
			case 2: {
				this.npc.display.setOverlayTexture(res);
				break;
			}
			default: {
				this.npc.display.setSkinTexture(res);
			}
			}
			this.npc.textureLocation = null;
		}
		this.close();
	}

	private void addFile(ResourceLocation location) {
		String path = location.getResourcePath();
		if (!suffix.isEmpty() && !path.toLowerCase().endsWith(suffix.toLowerCase())) {
			return;
		}
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
		if (!suffix.isEmpty() && !path.toLowerCase().endsWith(suffix.toLowerCase())) {
			return;
		}
		if (path == null || !path.contains("assets")) {
			return;
		}
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
		if (domain.isEmpty()) {
			return;
		}
		path = path.substring(path.indexOf("/") + 1);
		if (!path.startsWith("textures")) { return; }
		ResourceLocation res = new ResourceLocation(domain, path);
		if (!data.containsKey(domain)) {
			data.put(domain, new TreeMap<>());
		} else {
			for (ResourceLocation r : data.get(domain).keySet()) {
				if (r.getResourcePath().equals(path)) {
					return;
				}
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
				this.checkFolder(f);
				continue;
			}
			this.addFile(f.getAbsolutePath(), f.length());
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		GlStateManager.pushMatrix();
		GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
		int x = this.guiLeft + 271, y = this.guiTop + 6;
		int w = 80;
		if (this.type == 4) { // faction flag
			GlStateManager.translate(19.5f, 0.0f, 0.0f);
			w = 41;
		}
		Gui.drawRect(x - 1, y - 1, x + w + 1, y + 81, GuiTextureSelection.dark ? 0xFFE0E0E0 : 0xFF202020);
		Gui.drawRect(x, y, x + w, y + 80, GuiTextureSelection.dark ? 0xFF000000 : 0xFFFFFFFF);
		int g = 5;
		for (int u = 0; u < w / g; u++) {
			for (int v = 0; v < 80 / g; v++) {
				if (u % 2 == (v % 2 == 0 ? 1 : 0)) {
					Gui.drawRect(x + u * g, y + v * g, x + u * g + g, y + v * g + g, GuiTextureSelection.dark ? 0xFF343434 : 0xFFCCCCCC);
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
			GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
			try {
				int tX = 0;
				int tY = 0;
				int tW = 256;
				int tH = 256;
				int tS = 256;
				if (this.type == 4) { // faction flag
					GlStateManager.translate(62.0f, 0.0f, 0.0f);
					GlStateManager.scale(3.3f, 2.0f, 1.0f);
					tX = 4;
					tY = 4;
					tW = 40;
					tH = 128;
				}
				GuiNpcUtil.drawTexturedModalRect(resource, tX, tY, tW, tH, tS);
				//mc.getTextureManager().bindTexture(resource);
				//drawTexturedModalRect(0, 0, tX, tY, tW, tH);
			} catch (Exception e) { LogWriter.error("Error:", e); }
			GlStateManager.depthMask(true);
			GlStateManager.enableTexture2D();
			GlStateManager.disableBlend();
			GlStateManager.popMatrix();
		}
		if (this.npc != null && this.type >= 0 && this.type <= 2) {
			if (this.type == 0) {
				this.npc.textureLocation = resource;
			}
			int rot;
			float s = 1.25f;
			int mouse = 0;
			x = 0;
			y = 0;
			if (this.type == 0) {
				rot = (int) (3 * this.player.world.getTotalWorldTime() % 360);
			} else if (this.type == 1) {
				rot = 215;
			} else {
				rot = 325;
			}
			if (this.npc.textureLocation != null) {
				this.drawNpc(this.npc, this.guiLeft + 276 + x, this.guiTop + 155 + y, s, rot, 0, mouse);
			}
		}
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("texture.hover.done").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("texture.hover.dark").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addButton(new GuiNpcButton(1, this.guiLeft + 264, this.guiTop + 190, 90, 20, "gui.cancel"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 264, this.guiTop + 170, 90, 20, "gui.done"));
		GuiNpcCheckBox cBox = new GuiNpcCheckBox(3, this.guiLeft + 256, this.guiTop + 2, 15, 15, "");
		cBox.setSelected(GuiTextureSelection.dark);
		this.addButton(cBox);
		if (scroll == null) {
			(scroll = new GuiCustomScroll(this, 0)).setSize(250, 199);
		}
		scroll.guiLeft = this.guiLeft + 4;
		scroll.guiTop = this.guiTop + 14;
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
			colors.add(0xF3BE1E);
			for (String key : ds.keySet()) {
				suffixes.add("");
				list.add(key);
				colors.add(0xF3BE1E);
				i++;
			}
			for (String key : fs.keySet()) {
				suffixes.add(Util.instance.getTextReducedNumber(fs.get(key), false, false, true) + "b");
				list.add(key);
				colors.add(0xCAEAEA);
				if (txrName.equals(key)) {
					pos = i;
				}
				i++;
			}
			list.add(0, this.back);
			this.scroll.setColors(colors);
			this.scroll.setSuffixes(suffixes);
			this.scroll.setListNotSorted(list);
			if (this.scroll.selected != pos) {
				this.scroll.selected = pos;
			}
			this.scroll.resetRoll();
			char chr = ((char) 167);
			domain = chr + "l" + this.selectDir.getResourceDomain() + "/" + path;
			while (this.mc.fontRenderer.getStringWidth(domain) > 250 && path.contains("/")) {
				path = path.substring(path.indexOf("/") + 1);
				domain = chr + "l" + this.selectDir.getResourceDomain() + "/.../" + path;
			}
		}
		this.addScroll(this.scroll);
		this.addLabel(new GuiNpcLabel(0, domain, this.guiLeft + 4, this.guiTop + 4));
		this.getLabel(0).color = 0xFF000000;
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
						this.addFile(entryName, zipentry.getSize());
					}
				}
				zip.close();
			} else if (file.isDirectory()) {
				this.checkFolder(file);
			}
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	private void resetFiles() {
		data.clear();
		mc = Minecraft.getMinecraft();
		/* Texture manager data */
		for (ResourceLocation key : ((ITextureManagerMixin) mc.getTextureManager()).npcs$getMapTextureObjects().keySet()) {
			addFile(key);
		}
		/* Texture blocks data */
		for (String key : ((ITextureMapMixin) mc.getTextureMapBlocks()).npcs$getMapRegisteredSprites().keySet()) {
			addFile(new ResourceLocation(key.substring(0, key.indexOf(":")), "textures/" + key.substring(key.indexOf(":") +1) + ".png"));
		}
		/* Resource manager data */
		Map<String, FallbackResourceManager> map = ((ISimpleReloadableResourceManagerMixin) mc.getResourceManager()).npcs$getDomainResourceManagers();
		if (map == null) { return; }
		for (String name : map.keySet()) {
			FallbackResourceManager manager = map.get(name);
			List<IResourcePack> list = ((IFallbackResourceManagerMixin) manager).npcs$getResourcePacks();
			if (list == null) { return; }
			for (IResourcePack pack : list) {
				if (pack instanceof LegacyV2Adapter) {
					pack = ((ILegacyV2AdapterMixin) pack).npcs$getIResourcePack();
				}
				if (pack instanceof DefaultResourcePack) {
					ResourceIndex resourceIndex = ((IDefaultResourcePackMixin) pack).npcs$getResourceIndex();
					Map<String, File> resourceMap = ((IResourceIndexMixin) resourceIndex).npcs$getResourceMap();
					for (String key : resourceMap.keySet()) {
						File f = resourceMap.get(key);
						this.addFile(key, f.length());
					}
				}
				else if (pack instanceof AbstractResourcePack) {
					AbstractResourcePack p = (AbstractResourcePack) pack;
					File directory = ((IAbstractResourcePackMixin) p).npcs$getResourcePackFile();
					if (directory == null || !directory.isDirectory()) { continue; }
					File dir = new File(directory, "assets");
					if (!dir.exists() || !dir.isDirectory()) { continue; }
					this.checkFolder(dir);
				}
			}
		}
		/* Mod jars */
		for (ModContainer mod : Loader.instance().getModList()) {
			if (mod.getSource().exists()) {
				this.progressFile(mod.getSource());
			}
		}
		/* Resource packs */
		ResourcePackRepository repos = Minecraft.getMinecraft().getResourcePackRepository();
		for (ResourcePackRepository.Entry entry : repos.getRepositoryEntries()) {
			File file = new File(repos.getDirResourcepacks(), entry.getResourcePackName());
			if (file.exists()) {
				this.progressFile(file);
			}
		}
		/* Custom mod resources */
		this.checkFolder(new File(CustomNpcs.Dir, "assets"));
	}

	@Override
	public void save() {
		if (this.npc != null && this.type >= 0 && this.type <= 2) {
			this.npc.display.setShowName(this.showName);
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
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
			if (!scroll.getSelected().endsWith(this.suffix)) {
				this.selectDir = new ResourceLocation(this.selectDir.getResourceDomain(),
						this.selectDir.getResourcePath() + "/" + scroll.getSelected());
				this.initGui();
			} else {
				resource = new ResourceLocation(selectDir.getResourceDomain(), selectDir.getResourcePath() + "/" + scroll.getSelected());
				if (npc != null && type >= 0 && type <= 2) {
					switch (type) {
						case 1: {
							this.npc.display.setCapeTexture(resource.toString());
							break;
						}
						case 2: {
							this.npc.display.setOverlayTexture(resource.toString());
							break;
						}
						default: {
							this.npc.display.setSkinTexture(resource.toString());
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
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (this.resource != null) {
			if (this.npc != null && this.type >= 0 && this.type <= 2) {
				switch (this.type) {
				case 1: {
					this.npc.display.setCapeTexture(this.resource.toString());
					break;
				}
				case 2: {
					this.npc.display.setOverlayTexture(this.resource.toString());
					break;
				}
				default: {
					this.npc.display.setSkinTexture(this.resource.toString());
				}
				}
			}
			this.close();
			this.parent.initGui();
		}
	}

}
