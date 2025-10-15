package noppes.npcs.client.gui.util;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.event.ClientEvent;
import noppes.npcs.api.mixin.client.gui.IGuiScreenMixin;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.GuiBoundarySetting;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuiNPCInterface extends GuiScreen implements IEditNPC {

	public static final Poses[] ps = new Poses[] { new Poses(0), new Poses(1), new Poses(2), new Poses(3), new Poses(4), new Poses(5), new Poses(6), new Poses(7) };

	// Minecraft Resources
	public static final ResourceLocation MONEY = new ResourceLocation(CustomNpcs.MODID, "textures/items/coin_gold.png");
	public static final ResourceLocation DONAT = new ResourceLocation(CustomNpcs.MODID, "textures/items/coin_donat.png");
	public static final ResourceLocation WIDGETS = new ResourceLocation("textures/gui/widgets.png");
	public static long altH = System.currentTimeMillis();
	public static boolean showHoverText = true;

	// Mod Resources
	public static final ResourceLocation INFO = new ResourceLocation(CustomNpcs.MODID, "textures/gui/info.png");
	public static final ResourceLocation RESOURCE_SLOT = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");
	public static final ResourceLocation MENU_BUTTON = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubutton.png");
	public static final ResourceLocation MENU_SIDE_BUTTON = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menusidebutton.png");
	public static final ResourceLocation MENU_TOP_BUTTON = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menutopbutton.png");
	public static final ResourceLocation ANIMATION_BUTTONS = new ResourceLocation(CustomNpcs.MODID, "textures/gui/animation/buttons.png");
	public static final ResourceLocation ANIMATION_BUTTONS_SLOTS = new ResourceLocation(CustomNpcs.MODID, "textures/gui/animation/button_slots.png");
	public static int savedMouseX = -1;
	public static int savedMouseY = -1;

	protected boolean hasArea = false;
	protected boolean isInit = true;
	protected final List<String> hoverText = new ArrayList<>();
	protected final List<int[]> line = new ArrayList<>(); // startX, startY, endX, endY, color, lineSize
	protected final List<IComponentGui> components = new ArrayList<>();
	protected final Map<Integer, GuiNpcButton> buttons = new ConcurrentHashMap<>();
	protected final Map<Integer, GuiNpcLabel> labels = new ConcurrentHashMap<>();
	protected final Map<Integer, GuiCustomScroll> scrolls = new ConcurrentHashMap<>();
	protected final Map<Integer, GuiMenuSideButton> sideButtons = new ConcurrentHashMap<>();
	protected final Map<Integer, GuiNpcSlider> sliders = new ConcurrentHashMap<>();
	protected final Map<Integer, GuiNpcTextField> textFields = new ConcurrentHashMap<>();
	protected final Map<Integer, GuiMenuTopButton> topButtons = new ConcurrentHashMap<>();
	protected final Map<Integer, GuiNpcMiniWindow> miniWindows = new ConcurrentHashMap<>();
	public int mouseX;
	public int mouseY;
	public boolean drawDefaultBackground = false;
	public boolean hoverIsGame = false;
	public boolean closeOnEsc = false;
	public int guiLeft;
	public int guiTop;
	public int xSize = 200;
	public int ySize = 222;
	public int widthTexture = 0;
	public int heightTexture = 0;
	public int borderTexture = 4;
	public float bgScale = 1.0f;
	public float translateZ = 0.0f;
	public String title = "";
	public ResourceLocation background = null;
	public EntityNPCInterface npc;
	public EntityPlayerSP player;
	public SubGuiInterface subgui;

	public GuiNPCInterface() {
		this(null);
	}

	public GuiNPCInterface(EntityNPCInterface npcIn) {
		mc = Minecraft.getMinecraft();
		player = mc.player;
		itemRender = mc.getRenderItem();
		fontRenderer = mc.fontRenderer;
		if (npcIn == null) { npc = NoppesUtilServer.getEditingNpc(player); } else { npc = npcIn; }
	}

	public static void fill(int left, int top, int right, int bottom, float zLevel, int startColor, int endColor) {
		float alpha_0 = (float)(startColor >> 24 & 255) / 255.0F;
		float red_0 = (float)(startColor >> 16 & 255) / 255.0F;
		float green_0 = (float)(startColor >> 8 & 255) / 255.0F;
		float blue_0 = (float)(startColor & 255) / 255.0F;

		float alpha_1 = (float)(endColor >> 24 & 255) / 255.0F;
		float red_1 = (float)(endColor >> 16 & 255) / 255.0F;
		float green_1 = (float)(endColor >> 8 & 255) / 255.0F;
		float blue_1 = (float)(endColor & 255) / 255.0F;

		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		bufferbuilder.pos(right, top, zLevel).color(red_1, green_1, blue_1, alpha_1).endVertex();
		bufferbuilder.pos(left, top, zLevel).color(red_0, green_0, blue_0, alpha_0).endVertex();
		bufferbuilder.pos(left, bottom, zLevel).color(red_0, green_0, blue_0, alpha_0).endVertex();
		bufferbuilder.pos(right, bottom, zLevel).color(red_1, green_1, blue_1, alpha_1).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	@Override
	public List<String> getHoverText() { return hoverText; }

	/**
	 * 0: LMB - used in "buttonEvent(IGuiNpcButton button)"
	 * 1: RMB
	 * 2: CMB
	 * next - extra buttons
	 */
	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) { }

	public void add(IComponentGui component) {
		components.removeIf(c -> c.getID() == component.getID() && c.getClass().isAssignableFrom(component.getClass()));
		components.add(component);
		if (component instanceof GuiNpcTextArea) { hasArea = true; }
	}

	@Override
	public IComponentGui get(int id, Class<?> classType) {
		for (IComponentGui component : new ArrayList<>(components)) {
			if (component.getID() == id && component.getClass().isAssignableFrom(classType)) { return component; }
		}
		return id < components.size() ? components.get(id) : null;
	}

	public void addButton(GuiNpcButton button) { buttons.put(button.getID(), button); add(button); }

	public void addLabel(GuiNpcLabel label) { labels.put(label.getID(), label); add(label); }

	public void addScroll(GuiCustomScroll scroll) {
		scroll.setWorldAndResolution(mc, scroll.width, scroll.height);
		scrolls.put(scroll.getID(), scroll);
		add(scroll);
	}

	public void addSideButton(GuiMenuSideButton slider) { sideButtons.put(slider.getID(), slider); add(slider); }

	public void addSlider(GuiNpcSlider slider) { sliders.put(slider.getID(), slider); add(slider); }

	public void addTextField(GuiNpcTextField textField) { textFields.put(textField.getID(), textField); add(textField); }

	public void addTopButton(GuiMenuTopButton button) { topButtons.put(button.getID(), button); add(button); }

	public void addMiniWindow(GuiNpcMiniWindow miniwindows) {
		miniWindows.put(miniwindows.getID(), miniwindows);
		miniwindows.resetButtons();
		add(miniwindows);
	}

	@Override
	public void onClosed() { onGuiClosed(); }

	@Override
	public void onGuiClosed() {
		GuiNpcTextField.unfocus();
		save();
		displayGuiScreen(null);
		mc.setIngameFocus();
	}

	public void displayGuiScreen(GuiScreen gui) {
		ClientEvent.NextToGuiCustomNpcs event = new ClientEvent.NextToGuiCustomNpcs(npc, this, gui);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.returnGui == null || event.isCanceled()) { return; }
		mc.displayGuiScreen(event.returnGui);
		if (mc.currentScreen == null) { mc.setIngameFocus(); }
	}

	@Override
	public boolean doesGuiPauseGame() { return false; }

	public void doubleClicked() { }

	@Override
	public void drawDefaultBackground() {
		super.drawDefaultBackground();
	}

	@Override
	public void drawNpc(Entity entity, int x, int y, float zoomed, int rotation, int vertical, int mouseFocus) {
		EntityNPCInterface npc = null;
		if (entity instanceof EntityNPCInterface) {
			npc = (EntityNPCInterface) entity;
		}
		if (!(entity instanceof EntityLivingBase)) {
			mouseFocus = 0;
		}
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableColorMaterial();

		GlStateManager.translate((guiLeft + x), (guiTop + y), 50.0f);
		float scale = 1.0f;
		if (entity.height > 2.4) {
			scale = 2.0f / entity.height;
		}
		GlStateManager.scale(-30.0f * scale * zoomed, 30.0f * scale * zoomed, 30.0f * scale * zoomed);
		GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
		float f2 = entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).renderYawOffset
				: entity.rotationYaw;
		float f3 = entity.rotationYaw;
		float f4 = entity.rotationPitch;
		float f5 = entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).rotationYawHead
				: entity.rotationYaw;
		float f6 = mouseFocus == 0 || mouseFocus == 2 ? 0 : guiLeft + x - mouseX;
		float f7 = mouseFocus == 0 || mouseFocus == 3 ? 0 : guiTop + y - 50.0f * scale * zoomed - mouseY;
		int orientation = 0;
		if (npc != null) {
			orientation = npc.ais.orientation;
			npc.ais.orientation = rotation;
		}
		GlStateManager.rotate((float) (-Math.atan(f6 / 400.0f) * 20.0f), 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate((float) (-Math.atan(f7 / 40.0f) * 20.0f), 1.0f, 0.0f, 0.0f);
		entity.rotationYaw = (float) (Math.atan(f6 / 80.0f) * 40.0f + rotation);
		entity.rotationPitch = (float) (-Math.atan(f7 / 40.0f) * 20.0f);
		if (entity instanceof EntityLivingBase) {
			((EntityLivingBase) entity).renderYawOffset = rotation;
			((EntityLivingBase) entity).rotationYawHead = entity.rotationYaw;
		}
		mc.getRenderManager().playerViewY = 180.0f;
		if (mouseFocus != 0 && vertical != 0) {
			GlStateManager.translate(0.0f, 1.0f - Math.cos((double) vertical * 3.14d / 180.0d), 0.0f);
			GlStateManager.rotate(vertical, 1.0f, 0.0f, 0.0f);
		}
		mc.getRenderManager().renderEntity(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
		if (entity instanceof EntityLivingBase) {
			((EntityLivingBase) entity).renderYawOffset = f2;
			((EntityLivingBase) entity).prevRenderYawOffset = f2;
		}
		entity.rotationYaw = f3;
		entity.prevRotationYaw = f3;
		entity.rotationPitch = f4;
		entity.prevRotationPitch = f4;
		if (entity instanceof EntityLivingBase) {
			((EntityLivingBase) entity).rotationYawHead = f5;
			((EntityLivingBase) entity).prevRotationYawHead = f5;
		}
		if (npc != null) {
			npc.ais.orientation = orientation;
		}
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
	}

	@Override
	public void drawNpc(int x, int y) {
		drawNpc(npc, x, y, 1.0f, 0, 0, 1);
	}

	@Override
	public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
		if (translateZ != 0.0f) { GlStateManager.translate(0.0f, 0.0f, translateZ); }
		if (isInit && savedMouseX >= 0) {
			Mouse.setCursorPosition(savedMouseX, savedMouseY);
			isInit = false;
		}
		RenderHelper.disableStandardItemLighting();
		savedMouseX = Mouse.getX();
		savedMouseY = Mouse.getY();
		mouseX = mouseXIn;
		mouseY = mouseYIn;
		int x = mouseXIn;
		int y = mouseYIn;
		if (subgui != null) { y = (x = 0); }
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (drawDefaultBackground) {
			drawDefaultBackground();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		}
		if (background != null && !(this instanceof GuiNPCInterface2)) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft, guiTop, 0.0f);
			GlStateManager.scale(bgScale, bgScale, bgScale);
			mc.getTextureManager().bindTexture(background);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			if (widthTexture != 0 && heightTexture != 0) {
				int maxCol = ValueUtil.correctInt((int) Math.ceil((float) xSize / (float) (widthTexture - 2 * borderTexture)), 2, 10);
				int maxRow = ValueUtil.correctInt((int) Math.ceil((float) ySize / (float) (heightTexture - 2 * borderTexture)), 2, 10);
				int tileWidth = xSize / maxCol;
				int tileHeight = ySize / maxRow;
				int lastTileWidth = xSize - tileWidth * (maxCol - 1);
				int lastTileHeight = ySize - tileHeight * (maxRow - 1);

				int uOffset = (widthTexture - 2 * borderTexture - tileWidth) / 2;
				int uMax = widthTexture - lastTileWidth;
				int vOffset = (heightTexture - 2 * borderTexture - tileHeight) / 2;
				int vMax = heightTexture - lastTileHeight;


				for (int col = 0; col < maxCol; ++col) {
					for (int row = 0; row < maxRow; ++row) {
						drawTexturedModalRect(col * tileWidth,
								row * tileHeight,
								col == 0 ? 0 : col == maxCol - 1 ? uMax : uOffset,
								row == 0 ? 0 : row == maxRow - 1 ? vMax : vOffset,
								col == maxCol - 1 ? lastTileWidth : tileWidth,
								row == maxRow - 1 ? lastTileHeight : tileHeight);
					}
				}
			}
			else if (xSize > 256) {
				drawTexturedModalRect(0, 0, 0, 0, 250, ySize);
				drawTexturedModalRect(250, 0, 256 - (xSize - 250), 0, xSize - 250, ySize);
			}
			else { drawTexturedModalRect(0, 0, 0, 0, xSize, ySize); }
			GlStateManager.popMatrix();
		}
		postDrawBackground();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.translate(0, 0, 1.0f);
		if (!line.isEmpty()) {
			for (int[] ln : line) {
				if (ln == null || ln.length < 6) { continue; }
				GuiBoundarySetting.drawLine(ln[0], ln[1], ln[2], ln[3], ln[4], ln[5]);
			}
			line.clear();
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		drawCenteredString(fontRenderer, title, width / 2, height + 10, CustomNpcs.MainColor.getRGB());
		RenderHelper.enableGUIStandardItemLighting();
		for (IComponentGui component : new ArrayList<>(components)) {
			component.render(this, x, y, partialTicks);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		}
		postDrawScreen(mouseXIn, mouseYIn, partialTicks);
		if (translateZ != 0.0f) { GlStateManager.translate(0.0f, 0.0f, -translateZ); }
		if (subgui != null) { subgui.drawScreen(mouseXIn, mouseYIn, partialTicks); }
		else if (hoverIsGame || (CustomNpcs.ShowDescriptions && showHoverText) && !hoverText.isEmpty()) {
			drawHoveringText(new ArrayList<>(hoverText), mouseXIn, mouseYIn, fontRenderer);
			RenderHelper.disableStandardItemLighting();
			hoverText.clear();
		}
	}

	protected void postDrawScreen(int mouseXIn, int mouseYIn, float partialTicks) { }

	public void postDrawBackground() { }

	public GuiTextArea getTextArea(int id) {
		for (IComponentGui component : new ArrayList<>(components)) {
			if (component instanceof GuiTextArea && component.getID() == id) { return (GuiTextArea) component; }
		}
		return null;
	}

	public GuiNpcButton getButton(int id) { return buttons.get(id); }

	@Override
	public int getEventButton() { return ((IGuiScreenMixin) this).npcs$getEventButton(); }

	public GuiNpcLabel getLabel(int id) { return labels.get(id); }

	@SuppressWarnings("all")
	public GuiCustomScroll getScroll(int id) { return scrolls.get(id);	}

	public GuiMenuSideButton getSideButton(int id) { return sideButtons.get(id); }

	public GuiNpcSlider getSlider(int id) { return sliders.get(id); }

	public SubGuiInterface getSubGui() {
		if (hasSubGui() && subgui.hasSubGui()) { return subgui.getSubGui(); }
		return subgui;
	}

	public GuiNpcTextField getTextField(int id) { return textFields.get(id); }

	public GuiMenuTopButton getTopButton(int id) { return topButtons.get(id); }

	@SuppressWarnings("all")
	public GuiNpcMiniWindow getMiniWindow(int id) { return miniWindows.get(id); }

	@Override
	public boolean hasSubGui() { return subgui != null; }

	@Override
	public void initGui() {
		super.initGui();
		GuiNpcTextField.unfocus();
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
		hasArea = false;
		components.clear();
		buttons.clear();
		topButtons.clear();
		sideButtons.clear();
		textFields.clear();
		labels.clear();
		scrolls.clear();
		sliders.clear();
		miniWindows.clear();
		hoverText.clear();
		if (subgui != null) {
			subgui.setWorldAndResolution(mc, width, height);
			subgui.initGui();
		}
	}

	public void initPacket() { }

	public boolean isInventoryKey(int keyCode) { return keyCode == mc.gameSettings.keyBindInventory.getKeyCode(); }

	public boolean isMouseHover(int mX, int mY, int px, int py, int pWidth, int pHeight) {
		return mX >= px && mY >= py && mX < (px + pWidth) && mY < (py + pHeight);
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException { keyCnpcsPressed(typedChar, keyCode); }

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		if (altH < System.currentTimeMillis() && (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) && Keyboard.isKeyDown(Keyboard.KEY_H)) {
			altH = System.currentTimeMillis() + 1000L;
			showHoverText = !showHoverText;
		}
		if (hasSubGui()) {
			subgui.keyCnpcsPressed(typedChar, keyCode);
			return true;
		}
		else if (closeOnEsc && keyCode == Keyboard.KEY_ESCAPE) {
			onClosed();
			return true;
		}
		for (IComponentGui component : new ArrayList<>(components)) {
			if (component.keyCnpcsPressed(typedChar, keyCode)) { return true; }
		}
		return false;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException { mouseCnpcsPressed(mouseX, mouseY, mouseButton); }

	@Override
	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) {
		if (subgui != null) {
			subgui.mouseCnpcsPressed(mouseX, mouseY, mouseButton);
			return true;
		}
		List<GuiButton> list = new ArrayList<>(buttons.values());
		for (IComponentGui component : new ArrayList<>(components)) {
			if (component.mouseCnpcsPressed(mouseX, mouseY, mouseButton)) {
				if (component instanceof GuiNpcButton) {
					GuiNpcButton button = (GuiNpcButton) component;
					if (mouseButton == 0) {
						GuiScreenEvent.ActionPerformedEvent.Pre event = new GuiScreenEvent.ActionPerformedEvent.Pre(this, button, list);
						if (!MinecraftForge.EVENT_BUS.post(event)) {
							GuiButton mcButton = event.getButton();
							selectedButton = mcButton;
							mcButton.playPressSound(this.mc.getSoundHandler());
							if (mcButton instanceof GuiNpcButton) { buttonEvent((GuiNpcButton) mcButton, mouseButton); }
							else {
								try { actionPerformed(mcButton); } catch (Exception ignored) { }
							}
							if (equals(mc.currentScreen)) { MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Post(this, mcButton, list)); }
						}
						else { continue; }
					}
					else { buttonEvent(button, mouseButton); }
				}
				return true;
			}
		}
		return false;
	}

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state) {
		mouseCnpcsReleased(mouseX, mouseY, state);
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public boolean mouseCnpcsReleased(int mouseX, int mouseY, int state) {
		if (subgui != null) {
			subgui.mouseCnpcsReleased(mouseX, mouseY, state);
			return true;
		}
		for (IComponentGui component : components) {
			if (component.mouseCnpcsReleased(mouseX, mouseY, state)) { return true; }
		}
		return false;
	}

	public void openLink(String link) {
		try {
			Class<?> oclass = Class.forName("java.awt.Desktop");
			Object object = oclass.getMethod("getDesktop").invoke(null);
			oclass.getMethod("browse", URI.class).invoke(object, new URI(link));
		} catch (Exception e) { LogWriter.error(e); }
	}

	@Override
	public void save() {}

	@Override
	public void setBackground(String texture) {
		background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/" + texture);
		switch (texture) {
			case "bgfilled.png": {
				widthTexture = 256;
				heightTexture = 256;
				break;
			}
			case "companion_empty.png": {
				widthTexture = 172;
				heightTexture = 167;
				break;
			}
			case "extrasmallbg.png": {
				widthTexture = 176;
				heightTexture = 71;
				break;
			}
			case "largebg.png": {
				widthTexture = 192;
				heightTexture = 231;
				break;
			}
			case "menubg.png": {
				widthTexture = 256;
				heightTexture = 217;
				break;
			}
			case "smallbg.png": {
				widthTexture = 176;
				heightTexture = 222;
				break;
			}
			case "standardbg.png": {
				widthTexture = 256;
				heightTexture = 195;
				break;
			}
		}
	}

	@Override
	public void putHoverText(@Nullable List<String> newHoverText) {
		hoverText.clear();
		if (newHoverText != null && !newHoverText.isEmpty()) { hoverText.addAll(newHoverText); }
	}

	@Override
	public void putHoverText(@Nullable String text, Object ... args) {
		hoverText.clear();
		if (text == null || text.isEmpty()) { return; }
		if (!text.contains("%")) { text = new TextComponentTranslation(text, args).getFormattedText(); }
		if (text.contains("~~~")) { text = text.replaceAll("~~~", "%"); }
		while (text.contains("<br>")) {
			hoverText.add(text.substring(0, text.indexOf("<br>")));
			text = text.substring(text.indexOf("<br>") + 4);
		}
		hoverText.add(text);
	}

	@Override
	public void drawHoverText(String text, Object... args) {
		if (!CustomNpcs.ShowDescriptions || !GuiNPCInterface.showHoverText) { return; }
		if (text == null) {
			if (!hoverText.isEmpty()) { drawHoveringText(new ArrayList<>(hoverText), mouseX, mouseY, fontRenderer); }
			hoverText.clear();
			return;
		}
		putHoverText(text, args);
		if (!hoverText.isEmpty()) {
			drawHoveringText(new ArrayList<>(hoverText), mouseX, mouseY, fontRenderer);
			hoverText.clear();
		}
	}

	@Override
	public boolean hasHoverText() { return !hoverText.isEmpty(); }

	@Override
	public void setSubGui(SubGuiInterface gui) {
		subgui = gui;
		if (subgui != null) {
			subgui.npc = npc;
			subgui.setWorldAndResolution(mc, width, height);
			subgui.setParent(this);
			subgui.getParent().initGui();
		}
	}

	@Override
	public void setWorldAndResolution(@Nonnull Minecraft mcIn, int widthIn, int heightIn) {
		mc = mcIn;
		itemRender = mc.getRenderItem();
		fontRenderer = mc.fontRenderer;
		width = widthIn;
		height = heightIn;
		List<GuiButton> list = new ArrayList<>(buttons.values());
		if (!net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Pre(this, list))) {
			buttonList.clear();
			initGui();
		}
		net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent.Post(this, list));
		initPacket();
	}

	@Override
	public void updateScreen() {
		if (subgui != null) { subgui.updateScreen(); }
		else {
			for (IComponentGui component : new ArrayList<>(components)) { component.updateCnpcsScreen(); }
			super.updateScreen();
		}
	}

	@Override
	public void addLine(int sX, int sY, int eX, int eY, int color, int size) { line.add(new int[] { sX, sY, eX, eY, color, size }); }

	@Override
	public void closeMiniWindow(GuiNpcMiniWindow miniWindow) {  }

	@Override
	public boolean hasArea() { return hasArea; }

	@Override
	public void subGuiClosed(GuiScreen subgui) { }

	@Override
	public void elementClicked() {}


	// New from Unofficial (BetaZavr)
	public void drawWait() {
		ScaledResolution scaleW = new ScaledResolution(mc);
		int x = scaleW.getScaledWidth() / 2;
		int y = scaleW.getScaledHeight() / 2 - 30;
		drawCenteredString(fontRenderer, new TextComponentTranslation("gui.wait").getFormattedText(), mc.displayWidth / 2, mc.displayHeight / 2, CustomNpcs.MainColor.getRGB());
		int pos_0 = (int) Math.floor((double) (player.world.getTotalWorldTime() % 16) / 2.0d);
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(INFO);
		drawTexturedModalRect(x + ps[pos_0].x - 1, y + ps[pos_0].y - 1, 0, 12, 6, 6);
		int pos_1 = pos_0 - 1;
		if (pos_1 < 0) { pos_1 += 8; }
		drawTexturedModalRect(x + ps[pos_1].x, y + ps[pos_1].y, 6, 12, 5, 5);
		int pos_2 = pos_0 - 2;
		if (pos_2 < 0) { pos_2 += 8; }
		drawTexturedModalRect(x + ps[pos_2].x + 1, y + ps[pos_2].y + 1, 11, 12, 4, 4);
		GlStateManager.popMatrix();
	}

}
