package noppes.npcs.client.gui.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.INpc;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.event.ClientEvent;
import noppes.npcs.api.mixin.client.gui.IGuiScreenMixin;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
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
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuiNPCInterface
extends GuiScreen
implements IEditNPC, ICustomScrollListener, ISubGuiListener {

	// Minecraft Resources
	public static final ResourceLocation WIDGETS = new ResourceLocation("textures/gui/widgets.png");

	// Mod Resources
	public static final ResourceLocation RESOURCE_SLOT = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");
	public static final ResourceLocation MENU_BUTTON = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubutton.png");
	public static final ResourceLocation MENU_SIDE_BUTTON = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menusidebutton.png");
	public static final ResourceLocation MENU_TOP_BUTTON = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menutopbutton.png");
	public static final ResourceLocation ANIMATION_BUTTONS = new ResourceLocation(CustomNpcs.MODID, "textures/gui/animation/buttons.png");
	public static final ResourceLocation ANIMATION_BUTTONS_SLOTS = new ResourceLocation(CustomNpcs.MODID, "textures/gui/animation/button_slots.png");

	public boolean closeOnEsc = false;
	public boolean hoverMiniWin = false;
	public boolean drawDefaultBackground = false;
	protected boolean hasArea = false;
	public int guiLeft;
	public int guiTop;
	public int mouseX;
	public int mouseY;
	public int xSize;
	public int ySize;
	public int widthTexture = 0;
	public int heightTexture = 0;
	public int borderTexture = 4;
	public float bgScale = 1.0f;
	public float translateZ = 0.0f;
	public String title = "";

	protected final List<String> hoverText = new ArrayList<>();
	public ResourceLocation background = null;
	public EntityNPCInterface npc;
	public EntityPlayerSP player;
	protected GuiButton selectedButton;
	public SubGuiInterface subgui;

	protected final List<int[]> line = new ArrayList<>(); // startX, startY, endX, endY, color, lineSize
	protected final List<IComponentGui> components = new ArrayList<>();
	protected final Map<Integer, IGuiNpcButton> buttons = new ConcurrentHashMap<>();
	protected final Map<Integer, IGuiNpcLabel> labels = new ConcurrentHashMap<>();
	protected final Map<Integer, IGuiCustomScroll> scrolls = new ConcurrentHashMap<>();
	protected final Map<Integer, IGuiMenuSideButton> sideButtons = new ConcurrentHashMap<>();
	protected final Map<Integer, IGuiNpcSlider> sliders = new ConcurrentHashMap<>();
	protected final Map<Integer, IGuiNpcTextField> textFields = new ConcurrentHashMap<>();
	protected final Map<Integer, IGuiMenuTopButton> topButtons = new ConcurrentHashMap<>();
	protected final Map<Integer, IGuiNpcMiniWindow> miniWindows = new ConcurrentHashMap<>();

	public GuiNPCInterface(INpc npc) {
		this(npc instanceof EntityNPCInterface ? (EntityNPCInterface) npc : null);
	}

	public GuiNPCInterface() {
		this(null);
	}

	public GuiNPCInterface(EntityNPCInterface npc) {
		this.npc = npc;
		xSize = 200;
		ySize = 222;
		mc = Minecraft.getMinecraft();
		player = mc.player;
		itemRender = mc.getRenderItem();
		fontRenderer = mc.fontRenderer;
		if (this.npc == null) { this.npc = NoppesUtilServer.getEditingNpc(player); }
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

	protected List<String> getHoverText() { return hoverText; }

	/**
	 * 0: LMB - used in "buttonEvent(IGuiNpcButton button)"
	 * 1: RMB
	 * 2: CMB
	 * next - extra buttons
	 */
	@Override
	public void buttonEvent(@Nonnull IGuiNpcButton button, int mouseButton) { }

	@Override
	protected void actionPerformed(@Nonnull GuiButton guibutton) {
		if (!(guibutton instanceof IGuiNpcButton)) {
			return;
		}
		for (IGuiNpcMiniWindow mwin : miniWindows.values()) {
			mwin.buttonEvent((IGuiNpcButton) guibutton);
		}
		if (hoverMiniWin) { return; }
		if (subgui != null) {
			subgui.buttonEvent((IGuiNpcButton) guibutton);
		} else {
			buttonEvent((IGuiNpcButton) guibutton);
		}
	}

	public void add(IComponentGui component) {
		components.add(component);
		if (component instanceof GuiNpcTextArea) { hasArea = true; }
	}

	@Override
	public IComponentGui get(int id) {
		for (IComponentGui component : components) {
			if (component.getID() == id) { return component; }
		}
		return id < components.size() ? components.get(id) : null;
	}

	public IComponentGui get(int id, Class<?> classType) {
		for (IComponentGui component : components) {
			if (component.getID() == id && component.getClass().isAssignableFrom(classType)) { return component; }
		}
		return id < components.size() ? components.get(id) : null;
	}

	public void addButton(GuiNpcButton button) { addButton((IGuiNpcButton) button); }

	@Override
	public void addButton(IGuiNpcButton button) {
		buttons.put(button.getID(), button);
		add(button);
	}

	@Override
	public IGuiNpcButton addButton(int id, int x, int y, int width, int height, int textureX, int textureY, ResourceLocation texture) {
		IGuiNpcButton button = new GuiNpcButton(id, x, y, width, height, textureX, textureY, texture);
		addButton(button);
		return button;
	}

	@Override
	public IGuiNpcButton addButton(int id, int x, int y, int width, int height, int val, String... display) {
		IGuiNpcButton button = new GuiNpcButton(id, x, y, width, height, val, display);
		addButton(button);
		return button;
	}

	@Override
	public IGuiNpcButton addButton(int id, int x, int y, int width, int height, String label) {
		IGuiNpcButton button = new GuiNpcButton(id, x, y, width, height, label);
		addButton(button);
		return button;
	}

	@Override
	public IGuiNpcButton addButton(int id, int x, int y, int width, int height, String label, boolean enabled) {
		IGuiNpcButton button = new GuiNpcButton(id, x, y, width, height, label, enabled);
		addButton(button);
		return button;
	}

	@Override
	public IGuiNpcButton addButton(int id, int x, int y, int width, int height, String[] display, int val) {
		IGuiNpcButton button = new GuiNpcButton(id, x, y, width, height, display, val);
		addButton(button);
		return button;
	}

	@Override
	public IGuiNpcButton addButton(int id, int x, int y, String label) {
		IGuiNpcButton button = new GuiNpcButton(id, x, y, label);
		addButton(button);
		return button;
	}

	@Override
	public IGuiNpcButton addButton(int id, int x, int y, String[] display, int val) {
		IGuiNpcButton button = new GuiNpcButton(id, x, y, display, val);
		addButton(button);
		return button;
	}

	@Override
	public IGuiNpcCheckBox addCheckBox(int id, int x, int y, int width, int height, String trueLabel, String falseLabel) {
		IGuiNpcCheckBox button = new GuiNpcCheckBox(id, x, y, width, height, trueLabel, falseLabel);
		addButton((GuiNpcButton) button);
		return button;
	}

	@Override
	public IGuiNpcCheckBox addCheckBox(int id, int x, int y, int width, int height, String trueLabel, String falseLabel, boolean select) {
		IGuiNpcCheckBox button = new GuiNpcCheckBox(id, x, y, width, height, trueLabel, falseLabel, select);
		addButton((GuiNpcButton) button);
		return button;
	}

	@Override
	public IGuiNpcCheckBox addCheckBox(int id, int x, int y, String trueLabel, String falseLabel, boolean select) {
		IGuiNpcCheckBox button = new GuiNpcCheckBox(id, x, y, trueLabel, falseLabel, select);
		addButton((GuiNpcButton) button);
		return button;
	}

	public void addLabel(GuiNpcLabel label) { addLabel((IGuiNpcLabel) label); }

	@Override
	public void addLabel(IGuiNpcLabel label) {
		labels.put(label.getID(), label);
		add(label);
	}

	@Override
	public IGuiNpcLabel addLabel(int id, Object label, int x, int y, int color) {
		IGuiNpcLabel iLabel = new GuiNpcLabel(id, label, x, y, color);
		addLabel(iLabel);
		return iLabel;
	}

	@Override
	public IGuiNpcLabel addLabel(int id, Object label, int x, int y) {
		IGuiNpcLabel iLabel = new GuiNpcLabel(id, label, x, y);
		addLabel(iLabel);
		return iLabel;
	}

	public void addScroll(GuiCustomScroll scroll) { addScroll((IGuiCustomScroll) scroll); }

	@Override
	public void addScroll(IGuiCustomScroll scroll) {
		if (scroll instanceof GuiScreen) {
			((GuiScreen) scroll).setWorldAndResolution(mc, scroll.getWidth(), scroll.getHeight());
		}
		scroll.setParent(this);
		scrolls.put(scroll.getID(), scroll);
		add(scroll);
	}

	@Override
	public IGuiCustomScroll addScroll(ICustomScrollListener parent, int scrollId) {
		IGuiCustomScroll scroll = new GuiCustomScroll(parent, scrollId);
		addScroll(scroll);
		return scroll;
	}

	@Override
	public IGuiCustomScroll addScroll(ICustomScrollListener parent, boolean setSearch, int id) {
		IGuiCustomScroll scroll = new GuiCustomScroll(parent, setSearch, id);
		addScroll(scroll);
		return scroll;
	}

	@Override
	public IGuiCustomScroll addScroll(ICustomScrollListener parent, int id, boolean isMultipleSelection) {
		IGuiCustomScroll scroll = new GuiCustomScroll(parent, id, isMultipleSelection);
		addScroll(scroll);
		return scroll;
	}

	public void addSideButton(GuiMenuSideButton slider) { addSideButton((IGuiMenuSideButton) slider); }

	@Override
	public void addSideButton(IGuiMenuSideButton slider) {
		sideButtons.put(slider.getID(), slider);
		add(slider);
	}

	public void addSlider(GuiNpcSlider slider) { addSlider((IGuiNpcSlider) slider); }

	@Override
	public void addSlider(IGuiNpcSlider slider) {
		sliders.put(slider.getID(), slider);
		add(slider);
	}

	public void addTextField(GuiNpcTextField textField) { addTextField((IGuiNpcTextField) textField); }

	@Override
	public void addTextField(IGuiNpcTextField textField) {
		textFields.put(textField.getID(), textField);
		add(textField);
	}

	public void addTopButton(IGuiMenuTopButton button) {
		topButtons.put(button.getID(), button);
		add(button);
	}

	public void addMiniWindow(GuiNpcMiniWindow miniwindows) { addMiniWindow((IGuiNpcMiniWindow) miniwindows); }

	@Override
	public void addMiniWindow(IGuiNpcMiniWindow miniwindows) {
		miniWindows.put(miniwindows.getID(), miniwindows);
		miniwindows.resetButtons();
		add(miniwindows);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) { }

	@Override
	public void close() {
		displayGuiScreen(null);
		mc.setIngameFocus();
		save();
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

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawMainScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawMainScreen(int mouseX, int mouseY, float partialTicks) {
		if (translateZ != 0.0f) {
			GlStateManager.translate(0.0f, 0.0f, translateZ);
		}
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		int x = mouseX;
		int y = mouseY;
		if (subgui != null) {
			y = (x = 0);
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (drawDefaultBackground && subgui == null) { drawDefaultBackground(); }
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (background != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft, guiTop, 0.0f);
			GlStateManager.scale(bgScale, bgScale, bgScale);
			mc.getTextureManager().bindTexture(background);
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
		hoverMiniWin = false;
		for (IComponentGui component : new ArrayList<>(components)) {
			component.render(this, x, y, partialTicks);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			if (component instanceof GuiNpcMiniWindow && ((GuiNpcMiniWindow) component).hovered) { hoverMiniWin = true; }
		}
		if (translateZ != 0.0f) {
			GlStateManager.translate(0.0f, 0.0f, -translateZ);
		}
		super.drawScreen(x, y, partialTicks);
		if (subgui != null) { subgui.drawScreen(mouseX, mouseY, partialTicks); }
		else if (CustomNpcs.ShowDescriptions && !hoverText.isEmpty()) {
			drawHoveringText(new ArrayList<>(hoverText), mouseX, mouseY, fontRenderer);
			RenderHelper.disableStandardItemLighting();
			hoverText.clear();
		}
	}

	public void postDrawBackground() { }

	public void elementClicked() {
		if (subgui != null) { subgui.elementClicked(); }
	}

	@Override
	public IGuiTextArea getTextArea(int id) {
		for (IComponentGui component : components) {
			if (component instanceof IGuiTextArea && component.getID() == id) { return (IGuiTextArea) component; }
		}
		return null;
	}

	@Override
	public IGuiNpcButton getButton(int id) { return buttons.get(id); }

	@Override
	public int getEventButton() { return ((IGuiScreenMixin) this).npcs$getEventButton(); }

	public FontRenderer getFontRenderer() {
		return fontRenderer;
	}

	@Override
	public IGuiNpcLabel getLabel(int id) {
		return labels.get(id);
	}

	@Override
	public ResourceLocation getResource(String texture) { return new ResourceLocation(CustomNpcs.MODID, "textures/gui/" + texture); }

	@Override
	public IGuiCustomScroll getScroll(int id) { return scrolls.get(id);	}

	@Override
	public IGuiMenuSideButton getSideButton(int id) { return sideButtons.get(id); }

	@Override
	public IGuiNpcSlider getSlider(int id) { return sliders.get(id); }

	@Override
	public SubGuiInterface getSubGui() {
		if (hasSubGui() && subgui.hasSubGui()) { return subgui.getSubGui(); }
		return subgui;
	}

	@Override
	public IGuiNpcTextField getTextField(int id) { return textFields.get(id); }

	@Override
	public IGuiMenuTopButton getTopButton(int id) { return topButtons.get(id); }

	@Override
	public IGuiNpcMiniWindow getMiniWindow(int id) { return miniWindows.get(id); }

	@Override
	public boolean hasSubGui() {
		return subgui != null;
	}

	@Override
	public void initGui() {
		super.initGui();
		GuiNpcTextField.unfocus();
		if (subgui != null) {
			subgui.setWorldAndResolution(mc, width, height);
			subgui.initGui();
		}
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
		hasArea = false;
		hoverText.clear();
		components.clear();
		buttonList.clear();
		buttons.clear();
		topButtons.clear();
		sideButtons.clear();
		textFields.clear();
		labels.clear();
		scrolls.clear();
		sliders.clear();
		miniWindows.clear();
	}
	
	public List<GuiButton> getButtonList() { return buttonList; }

	public void initPacket() { }

	public boolean isInventoryKey(int i) {
		return i == mc.gameSettings.keyBindInventory.getKeyCode();
	}

	@Override
	public boolean isMouseHover(int mX, int mY, int px, int py, int pWidth, int pHeight) {
		return mX >= px && mY >= py && mX < (px + pWidth) && mY < (py + pHeight);
	}

	public void keyTyped(char c, int i) {
		if (subgui != null) {
			subgui.keyTyped(c, i);
			return;
		}
		for (IGuiNpcMiniWindow mwin : miniWindows.values()) {
			mwin.customKeyTyped(c, i);
		}
		if (hoverMiniWin) { return; }
		boolean active = false;
		for (IComponentGui component : components) {
			if (component instanceof GuiTextArea && ((GuiTextArea) component).isActive()) {
				active = true;
				break;
			}
		}
		active = (active || GuiNpcTextField.isActive());
		if (subgui == null) {
			boolean helpButtons = false;
			if (i == 56 || i == 184) { helpButtons = Keyboard.isKeyDown(35); } // Alts
			else if (i == 35) { helpButtons = Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184); }
			if (helpButtons) { CustomNpcs.ShowDescriptions = !CustomNpcs.ShowDescriptions; }
		}
		if (closeOnEsc && (i == 1 || (!active && isInventoryKey(i)))) {
			close();
			return;
		}
		for (IGuiNpcTextField tf : new ArrayList<>(textFields.values())) {
			tf.customKeyTyped(c, i);
		}
		for (IComponentGui component : components) {
			if (component instanceof IKeyListener) {
				((IKeyListener) component).customKeyTyped(c, i);
			}
		}
		if (hasSubGui()) {
			return;
		}
		for (IGuiCustomScroll scroll : new ArrayList<>(scrolls.values())) {
			scroll.customKeyTyped(c, i);
		}
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (subgui != null) {
			subgui.mouseClicked(mouseX, mouseY, mouseButton);
			return;
		}
		for (IGuiNpcMiniWindow mwin : miniWindows.values()) {
			mwin.customMouseClicked(mouseX, mouseY, mouseButton);
		}
		if (hoverMiniWin) { return; }
		for (IGuiNpcTextField tf : new ArrayList<>(textFields.values())) {
			if (tf.isEnabled()) {
				tf.customMouseClicked(mouseX, mouseY, mouseButton);
			}
		}
		List<GuiButton> allButtons = new ArrayList<>();
		for (IComponentGui component : components) {
			if (component instanceof IMouseListener) {
				((IMouseListener) component).mouseClicked(mouseX, mouseY, mouseButton);
			}
			if (component instanceof GuiButton) {
				allButtons.add((GuiButton) component);
			}
		}
		mouseEvent(mouseX, mouseY, mouseButton);
		if (mouseButton != 0) {
			for (IGuiNpcButton button : buttons.values()) {
				if (button.isHovered()) {
					buttonEvent(button, mouseButton);
					break;
				}
			}
			return;
		}
		for (IGuiCustomScroll scroll : new ArrayList<>(scrolls.values())) {
			scroll.customMouseClicked(mouseX, mouseY, mouseButton);
		}
		for (GuiButton button : allButtons) {
			if (button.mousePressed(mc, mouseX, mouseY)) {
				GuiScreenEvent.ActionPerformedEvent.Pre event = new GuiScreenEvent.ActionPerformedEvent.Pre(this, button, allButtons);
				if (MinecraftForge.EVENT_BUS.post(event)) { break; }
				button = event.getButton();
				(selectedButton = button).playPressSound(mc.getSoundHandler());
				actionPerformed(button);
				if (equals(mc.currentScreen)) {
					MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Post(this, event.getButton(), allButtons));
					break;
				}
				break;
			}
		}
	}

	public void mouseEvent(int mouseX, int mouseY, int mouseButton) {
		for (IGuiNpcMiniWindow mwin : miniWindows.values()) {
			mwin.mouseEvent(mouseX, mouseY, mouseButton);
		}
	}

	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		for (IGuiNpcMiniWindow mwin : miniWindows.values()) {
			mwin.customMouseReleased(mouseX, mouseY, mouseButton);
		}
		if (hoverMiniWin) { return; }
		if (selectedButton != null && mouseButton == 0) {
			selectedButton.mouseReleased(mouseX, mouseY);
			selectedButton = null;
		}
	}

	public void onGuiClosed() {
		GuiNpcTextField.unfocus();
	}

	public void openLink(String link) {
		try {
			Class<?> oclass = Class.forName("java.awt.Desktop");
			Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null);
			oclass.getMethod("browse", URI.class).invoke(object, new URI(link));
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

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
	public void setHoverText(@Nullable List<String> newHoverText) {
		hoverText.clear();
		if (newHoverText != null && !newHoverText.isEmpty()) { hoverText.addAll(newHoverText); }
	}

	@Override
	public void setHoverText(@Nullable String text, Object ... args) {
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
		if (!CustomNpcs.ShowDescriptions) { return; }
		if (text == null) {
			if (!hoverText.isEmpty()) { drawHoveringText(new ArrayList<>(hoverText), mouseX, mouseY, fontRenderer); }
			hoverText.clear();
			return;
		}
		setHoverText(text, args);
		if (!hoverText.isEmpty()) {
			drawHoveringText(new ArrayList<>(hoverText), mouseX, mouseY, fontRenderer);
			hoverText.clear();
		}
	}

	@Override
	public INpc getNpc() { return npc; }

	@Override
	public void setNpc(INpc iNpc) {
		if (iNpc instanceof EntityNPCInterface) { npc = (EntityNPCInterface) iNpc; }
	}

	@Override
	public void setSubGui(SubGuiInterface gui) {
		subgui = gui;
		if (subgui != null) {
			subgui.setNpc(npc);
			subgui.setWorldAndResolution(mc, width, height);
			subgui.setParent(this);
			subgui.getParent().initGui();
		}
	}

	public void setWorldAndResolution(@Nonnull Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		initPacket();
	}

	public void updateScreen() {
		if (subgui != null) { subgui.updateScreen(); }
		else {
			for (IComponentGui component : components) {
				if (component instanceof GuiNpcTextField) { ((GuiNpcTextField) component).updateScreen(); }
				if (component instanceof GuiTextArea) { ((GuiTextArea) component).updateScreen(); }
			}
			super.updateScreen();
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, IGuiCustomScroll scroll) { }

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) { }

	@Override
	public void mouseDragged(IGuiNpcSlider slider) { }

	@Override
	public void mousePressed(IGuiNpcSlider slider) { }

	@Override
	public void mouseReleased(IGuiNpcSlider slider) { }

	@Override
	public void unFocused(IGuiNpcTextField textField) { }
	
	@Override
	public void addLine(int sX, int sY, int eX, int eY, int color, int size) {
		line.add(new int[] { sX, sY, eX, eY, color, size });
	}

	@Override
	public void closeMiniWindow(IGuiNpcMiniWindow miniWindow) {
		miniWindows.remove(miniWindow.getID());
	}
	
	@Override
	public void setMiniHoverText(int id, IComponentGui component) {}

	@Override
	public boolean hasArea() { return hasArea; }

	@Override
	public void drawWait() {

	}

	@Override
	public boolean hasHoverText() {
		return false;
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) { }

}
