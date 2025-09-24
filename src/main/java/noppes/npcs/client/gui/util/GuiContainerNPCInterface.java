package noppes.npcs.client.gui.util;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.ClickType;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
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
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.GuiBoundarySetting;
import noppes.npcs.containers.ContainerEmpty;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GuiContainerNPCInterface extends GuiContainer implements IEditNPC {

	protected final List<String> hoverText = new ArrayList<>();
	protected final List<int[]> line = new ArrayList<>(); // startX, startY, endX, endY, color, lineSize
	protected final List<IComponentGui> components = new ArrayList<>();
	protected final Map<Integer, GuiNpcLabel> labels = new ConcurrentHashMap<>();
	protected final Map<Integer, GuiCustomScroll> scrolls = new ConcurrentHashMap<>();
	protected final Map<Integer, GuiNpcSlider> sliders = new ConcurrentHashMap<>();
	protected final Map<Integer, GuiNpcTextField> textFields = new ConcurrentHashMap<>();
	protected final Map<Integer, GuiMenuTopButton> topButtons = new ConcurrentHashMap<>();
	protected final Map<Integer, GuiNpcButton> buttons = new ConcurrentHashMap<>();
	protected final Map<Integer, GuiNpcMiniWindow> miniWindows = new ConcurrentHashMap<>();
	protected boolean hasArea = false;
	protected boolean isInit = true;

	public int mouseX;
	public int mouseY;
	public boolean drawDefaultBackground = false;
	public boolean hoverIsGame = false;
	public boolean closeOnEsc = false;
	public int widthTexture = 0;
	public int heightTexture = 0;
	public int borderTexture = 4;
	public float bgScale = 1.0f;
	public String title = "Npc Mainmenu";
	public ResourceLocation background;
	public EntityNPCInterface npc;
	public EntityPlayerSP player;
	public SubGuiInterface subgui;

	public GuiContainerNPCInterface(EntityNPCInterface npcIn, Container cont) {
		super(cont);
		npc = npcIn;
		mc = Minecraft.getMinecraft();
		player = mc.player;
		itemRender = mc.getRenderItem();
		fontRenderer = mc.fontRenderer;
		if (npcIn == null) { npc = NoppesUtilServer.getEditingNpc(player); } else { npc = npcIn; }
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

	public void addSlider(GuiNpcSlider slider) { sliders.put(slider.getID(), slider); add(slider); }

	public void addTextField(GuiNpcTextField textField) { textFields.put(textField.getID(), textField); add(textField); }

	public void addTopButton(GuiMenuTopButton button) { topButtons.put(button.id, button); add(button); }

	@SuppressWarnings("all")
	public void addMiniWindow(GuiNpcMiniWindow miniwindows) {
		miniWindows.put(miniwindows.getID(), miniwindows);
		miniwindows.resetButtons();
		add(miniwindows);
	}

	@Override
	public void onClosed() {
		GuiNpcTextField.unfocus();
		save();
		player.closeScreen();
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
	public void drawDefaultBackground() {
		RenderHelper.disableStandardItemLighting();
		if (drawDefaultBackground && subgui == null) {
			postDrawBackground();
			super.drawDefaultBackground();
			if (background != null && !(this instanceof GuiContainerNPCInterface2)) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(guiLeft, guiTop, 0.0f);
				GlStateManager.scale(bgScale, bgScale, bgScale);
				mc.getTextureManager().bindTexture(background);
				if (widthTexture != 0 && heightTexture != 0) {
					int maxRow = ValueUtil.correctInt((int) Math.ceil((float) ySize / (float) (heightTexture - 2 * borderTexture)), 2, 10);
					int maxCol = ValueUtil.correctInt((int) Math.ceil((float) xSize / (float) (widthTexture - 2 * borderTexture)), 2, 10);
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
				else { drawTexturedModalRect(0, 0, 0, 0, xSize, ySize); }
				GlStateManager.popMatrix();
			}
		}
	}

	public void postDrawBackground() { }

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		drawDefaultBackground();
		drawCenteredString(fontRenderer, new TextComponentTranslation(title).getFormattedText(), width / 2, guiTop - 8, new Color(0xFFFFFF).getRGB());
		if (!line.isEmpty()) {
			for (int[] ln : line) {
				if (ln == null || ln.length < 6) { continue; }
				GuiBoundarySetting.drawLine(ln[0], ln[1], ln[2], ln[3], ln[4], ln[5]);
			}
			line.clear();
		}
		for (IComponentGui component : new ArrayList<>(components)) {
			component.render(this, mouseX, mouseY, partialTicks);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		}
	}

	@Override
    public void drawNpc(Entity entity, int x, int y, float zoomed, int rotation, int vertical, int mouseFocus) {
		EntityNPCInterface npc = null;
		if (entity instanceof EntityNPCInterface) { npc = (EntityNPCInterface) entity; }
		if (!(entity instanceof EntityLivingBase)) { mouseFocus = 0; }
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
		RenderHelper.enableStandardItemLighting();
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
		if (entity instanceof EntityLivingBase) {
			((EntityLivingBase) entity).renderYawOffset = rotation;
		}
		entity.rotationYaw = (float) (Math.atan(f6 / 80.0f) * 40.0f + rotation);
		entity.rotationPitch = (float) (-Math.atan(f7 / 40.0f) * 20.0f);
		if (entity instanceof EntityLivingBase) {
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
		}
		if (entity instanceof EntityLivingBase) {
			((EntityLivingBase) entity).prevRenderYawOffset = f2;
		}
		entity.rotationYaw = f3;
		entity.prevRotationYaw = f3;
		entity.rotationPitch = f4;
		entity.prevRotationPitch = f4;
		if (entity instanceof EntityLivingBase) {
			((EntityLivingBase) entity).rotationYawHead = f5;
		}
		if (entity instanceof EntityLivingBase) {
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
	public void drawNpc(int x, int y) { drawNpc(npc, x, y, 1.0f, 0, 0, 1); }

	@Override
	public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
		if (isInit && GuiNPCInterface.savedMouseX >= 0) {
			Mouse.setCursorPosition(GuiNPCInterface.savedMouseX, GuiNPCInterface.savedMouseY);
			isInit = false;
		}
		GuiNPCInterface.savedMouseX = Mouse.getX();
		GuiNPCInterface.savedMouseY = Mouse.getY();
		mouseX = mouseXIn;
		mouseY = mouseYIn;
		Container container = inventorySlots;
		if (subgui != null) { inventorySlots = new ContainerEmpty(); }
		super.drawScreen(mouseXIn, mouseYIn, partialTicks);
		zLevel = 0.0f;
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (subgui != null) {
			inventorySlots = container;
			subgui.drawScreen(mouseXIn, mouseYIn, partialTicks);
		}
		else if (mc.player.inventory.getItemStack().isEmpty() && getSlotUnderMouse() != null && getSlotUnderMouse().getHasStack()) {
			renderToolTip(getSlotUnderMouse().getStack(), mouseXIn, mouseYIn);
		}
		else if (hoverIsGame || (CustomNpcs.ShowDescriptions && GuiNPCInterface.showHoverText) && !hoverText.isEmpty()) {
			drawHoveringText(new ArrayList<>(hoverText), mouseXIn, mouseYIn, fontRenderer);
			RenderHelper.disableStandardItemLighting();
			hoverText.clear();
		}
	}

	@SuppressWarnings("all")
	public GuiTextArea getTextArea(int id) {
		for (IComponentGui component : new ArrayList<>(components)) {
			if (component instanceof GuiTextArea && component.getID() == id) { return (GuiTextArea) component; }
		}
		return null;
	}

	public GuiNpcButton getButton(int id) { return buttons.get(id); }

	@Override
	public int getEventButton() {
		return ((IGuiScreenMixin) this).npcs$getEventButton();
	}

	public GuiNpcLabel getLabel(int id) { return labels.get(id); }

	@SuppressWarnings("all")
	public GuiCustomScroll getScroll(int id) { return scrolls.get(id); }

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
		components.clear();
		buttons.clear();
		topButtons.clear();
		scrolls.clear();
		sliders.clear();
		labels.clear();
		textFields.clear();
		miniWindows.clear();
		hoverText.clear();
		hasArea = false;
		Keyboard.enableRepeatEvents(true);
		if (subgui != null) {
			subgui.setWorldAndResolution(mc, width, height);
			subgui.initGui();
		}
	}

	public void initPacket() { }

	public boolean isInventoryKey(int keyCode) { return keyCode == mc.gameSettings.keyBindInventory.getKeyCode(); }

	public boolean isMouseHover(int mX, int mY, int px, int py, int pWidth, int pHeight) { return mX >= px && mY >= py && mX < (px + pWidth) && mY < (py + pHeight); }

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		keyCnpcsPressed(typedChar, keyCode);
		checkHotbarKeys(keyCode);
		if (this.getSlotUnderMouse() != null && getSlotUnderMouse().getHasStack()) {
			if (mc.gameSettings.keyBindPickBlock.isActiveAndMatches(keyCode)) {
				handleMouseClick(getSlotUnderMouse(), getSlotUnderMouse().slotNumber, 0, ClickType.CLONE);
			}
			else if (this.mc.gameSettings.keyBindDrop.isActiveAndMatches(keyCode)) {
				handleMouseClick(getSlotUnderMouse(), getSlotUnderMouse().slotNumber, isCtrlKeyDown() ? 1 : 0, ClickType.THROW);
			}
		}
	}

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		if (GuiNPCInterface.altH < System.currentTimeMillis() && (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU)) && Keyboard.isKeyDown(Keyboard.KEY_H)) {
			GuiNPCInterface.altH = System.currentTimeMillis() + 1000L;
			GuiNPCInterface.showHoverText = !GuiNPCInterface.showHoverText;
		}
		if (subgui != null) { subgui.keyCnpcsPressed(typedChar, keyCode); return true; }
		if (closeOnEsc && keyCode == Keyboard.KEY_ESCAPE) {
			onClosed();
			return true;
		}
		for (IComponentGui component : new ArrayList<>(components)) {
			if (component.keyCnpcsPressed(typedChar, keyCode)) { return true; }
		}
		return false;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		mouseCnpcsPressed(mouseX, mouseY, mouseButton);
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

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

	@SuppressWarnings("all")
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
		drawDefaultBackground = true;
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
			for (IComponentGui component : new ArrayList<>(components)) {
				component.updateCnpcsScreen();
			}
			super.updateScreen();
		}
	}

	@Override
	public void addLine(int sX, int sY, int eX, int eY, int color, int size) { line.add(new int[] { sX, sY, eX, eY, color, size }); }
	
	@Override
	public void closeMiniWindow(GuiNpcMiniWindow miniWindow) { }

	@Override
	public boolean hasArea() { return hasArea; }

	@Override
	public void subGuiClosed(SubGuiInterface subgui) { }

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
		mc.getTextureManager().bindTexture(GuiNPCInterface.INFO);
		drawTexturedModalRect(x + GuiNPCInterface.ps[pos_0].x - 1, y + GuiNPCInterface.ps[pos_0].y - 1, 0, 12, 6, 6);
		int pos_1 = pos_0 - 1;
		if (pos_1 < 0) { pos_1 += 8; }
		drawTexturedModalRect(x + GuiNPCInterface.ps[pos_1].x, y + GuiNPCInterface.ps[pos_1].y, 6, 12, 5, 5);
		int pos_2 = pos_0 - 2;
		if (pos_2 < 0) { pos_2 += 8; }
		drawTexturedModalRect(x + GuiNPCInterface.ps[pos_2].x + 1, y + GuiNPCInterface.ps[pos_2].y + 1, 11, 12, 4, 4);
		GlStateManager.popMatrix();
	}

}
