package noppes.npcs.client.gui.util;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.LogWriter;
import noppes.npcs.api.event.ClientEvent;
import noppes.npcs.api.mixin.client.gui.IGuiScreenMixin;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.GuiBoundarySetting;
import noppes.npcs.containers.ContainerEmpty;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class GuiContainerNPCInterface
extends GuiContainer
implements IEditNPC, ICustomScrollListener {

	public static ResourceLocation ball = new ResourceLocation(CustomNpcs.MODID, "textures/gui/info.png");

	public boolean closeOnEsc = false;
	public boolean hoverMiniWin = false;
	public boolean drawDefaultBackground = false;
	private boolean hasArea = false;
	public int guiLeft;
	public int guiTop;
	public int mouseX;
	public int mouseY;
	public int widthTexture = 0;
	public int heightTexture = 0;
	public float bgScale = 1.0f;
	public String title = "Npc Mainmenu";
	private final List<String> hoverText = new ArrayList<>();

	public ResourceLocation background;
	public EntityNPCInterface npc;
	public EntityPlayerSP player;
	public SubGuiInterface subgui;

	private final Poses[] ps;
	private final List<int[]> line = new ArrayList<>(); // startX, startY, endX, endY, color, lineSize
	protected final List<IComponentGui> components = new ArrayList<>();
	private final HashMap<Integer, GuiNpcLabel> labels = new HashMap<>();
	private final HashMap<Integer, GuiCustomScroll> scrolls = new HashMap<>();
	private final HashMap<Integer, GuiNpcSlider> sliders = new HashMap<>();
	private final HashMap<Integer, GuiNpcTextField> textfields = new HashMap<>();
	private final HashMap<Integer, GuiMenuTopButton> topbuttons = new HashMap<>();
	private final HashMap<Integer, GuiNpcButton> buttons = new HashMap<>();
	protected final Map<Integer, GuiNpcMiniWindow> mwindows = new ConcurrentHashMap<>();

	public GuiContainerNPCInterface(EntityNPCInterface npc, Container cont) {
		super(cont);
		this.npc = npc;
		mc = Minecraft.getMinecraft();
		player = mc.player;
		itemRender = mc.getRenderItem();
		fontRenderer = mc.fontRenderer;
		ps = new Poses[] { new Poses(this, 0), new Poses(this, 1), new Poses(this, 2), new Poses(this, 3), new Poses(this, 4), new Poses(this, 5), new Poses(this, 6), new Poses(this, 7) };
	}

	/**
	 * 0: LMB - used in "buttonEvent(GuiNpcButton button)"
	 * 1: RMB
	 * 2: CMB
	 * next - extra buttons
	 */
	protected void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) { }

	@Override
	protected void actionPerformed(@Nonnull GuiButton guibutton) {
		if (!(guibutton instanceof GuiNpcButton)) { return; }
		if (subgui != null) {
			subgui.buttonEvent((GuiNpcButton) guibutton);
		} else {
			for (GuiNpcMiniWindow mwin : mwindows.values()) {
				mwin.buttonEvent((GuiNpcButton) guibutton);
			}
			if (hoverMiniWin) { return; }
			buttonEvent((GuiNpcButton) guibutton);
		}
	}

	public void add(IComponentGui component) {
		components.add(component);
		if (component instanceof GuiNpcTextArea) { hasArea = true; }
	}

	public void addButton(GuiNpcButton button) {
		buttons.put(button.id, button);
		add(button);
	}

	public void addLabel(GuiNpcLabel label) {
		labels.put(label.id, label);
		add(label);
	}

	public void addScroll(GuiCustomScroll scroll) {
		scroll.setWorldAndResolution(mc, scroll.width, scroll.height);
		scroll.setParent(this);
		scrolls.put(scroll.id, scroll);
		add(scroll);
	}

	public void addSlider(GuiNpcSlider slider) {
		sliders.put(slider.id, slider);
		add(slider);
	}

	public void addTextField(GuiNpcTextField textField) {
		textfields.put(textField.getId(), textField);
		add(textField);
	}

	public void addTopButton(GuiMenuTopButton button) {
		topbuttons.put(button.id, button);
		add(button);
	}

	public void addMiniWindow(GuiNpcMiniWindow miniwindows) {
		mwindows.put(miniwindows.id, miniwindows);
		miniwindows.resetButtons();
		add(miniwindows);
	}
	
	public void buttonEvent(GuiNpcButton button) { }

	public void close() {
		GuiNpcTextField.unfocus();
		save();
		player.closeScreen();
		displayGuiScreen(null);
		mc.setIngameFocus();
	}

	@Override
	public void closeSubGui(SubGuiInterface gui) { subgui = null; }

	public void displayGuiScreen(GuiScreen gui){
		ClientEvent.NextToGuiCustomNpcs event = new ClientEvent.NextToGuiCustomNpcs(npc, this, gui);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.returnGui == null || event.isCanceled()) { return; }
		mc.displayGuiScreen(event.returnGui);
		if (mc.currentScreen == null) { mc.setIngameFocus(); }
	}

	@Override
	public void drawDefaultBackground() {
		if (drawDefaultBackground && subgui == null) {
			postDrawBackground();
			super.drawDefaultBackground();
			if (background != null) {
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.pushMatrix();
				GlStateManager.translate(guiLeft, guiTop, 0.0f);
				GlStateManager.scale(bgScale, bgScale, bgScale);
				mc.getTextureManager().bindTexture(background);
				if (xSize > 252 || ySize > 252) {
					if (widthTexture != 0 && heightTexture != 0) {
						int tilesW = xSize / 2;
						int tilesH = ySize / 2;
						drawTexturedModalRect(0, 0, 0, 0, tilesW, tilesH);
						drawTexturedModalRect(tilesW, 0, widthTexture - tilesW, 0, tilesW, tilesH);
						drawTexturedModalRect(0, tilesH, 0, heightTexture - tilesH, tilesW, tilesH);
						drawTexturedModalRect(tilesW, tilesH, widthTexture - tilesW, heightTexture - tilesH, tilesW, tilesH);
					}
					else if (ySize < 257) {
						int tilesW = xSize / 2;
						drawTexturedModalRect(0, 0, 0, 0, tilesW, ySize);
						drawTexturedModalRect(tilesW, 0, 256 - tilesW, 0, tilesW, ySize);
					}
					else { drawTexturedModalRect(0, 0, 0, 0, xSize, ySize); }
				}
				else { drawTexturedModalRect(0, 0, 0, 0, xSize, ySize); }
				GlStateManager.popMatrix();
			}
		}
	}

	public void postDrawBackground() { }

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
		hoverMiniWin = false;
		for (IComponentGui component : new ArrayList<>(components)) {
			component.render(this, mouseX, mouseY, partialTicks);
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			if (component instanceof GuiNpcMiniWindow && ((GuiNpcMiniWindow) component).hovered) { hoverMiniWin = true; }
		}
	}

    public void drawNpc(Entity entity, int x, int y, float zoomed, int rotation, int vertical, int mouseFocus) {
		EntityNPCInterface npc = null;
		if (entity instanceof EntityNPCInterface) {
			npc = (EntityNPCInterface) entity;
		}
		if (!(entity instanceof EntityLivingBase)) {
			mouseFocus = 0;
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();

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
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	public void drawNpc(int x, int y) {
		drawNpc(npc, x, y, 1.0f, 0, 0, 1);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		Container container = inventorySlots;
		if (subgui != null) { inventorySlots = new ContainerEmpty(); }
		super.drawScreen(mouseX, mouseY, partialTicks);
		zLevel = 0.0f;
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (subgui != null) {
			inventorySlots = container;
			RenderHelper.disableStandardItemLighting();
			subgui.drawScreen(mouseX, mouseY, partialTicks);
		} else {
			renderHoveredToolTip(mouseX, mouseY);
		}
		if (hoverMiniWin) { return; }
		if (CustomNpcs.ShowDescriptions && !hoverText.isEmpty()) {
			drawHoveringText(hoverText, mouseX, mouseY, fontRenderer);
			RenderHelper.enableGUIStandardItemLighting();
			hoverText.clear();
		}
	}

	public void drawWait() {
		drawCenteredString(fontRenderer, new TextComponentTranslation("gui.wait").getFormattedText(), mc.displayWidth / 2, mc.displayHeight / 2, CustomNpcs.MainColor.getRGB());
		int pos_0 = (int) Math.floor((double) (player.world.getTotalWorldTime() % 16) / 2.0d);
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(GuiContainerNPCInterface.ball);
		drawTexturedModalRect(ps[pos_0].x - 1, ps[pos_0].y - 1, 0, 12, 6, 6);
		int pos_1 = pos_0 - 1;
		if (pos_1 < 0) {
			pos_1 += 8;
		}
		drawTexturedModalRect(ps[pos_1].x, ps[pos_1].y, 6, 12, 5, 5);
		int pos_2 = pos_0 - 2;
		if (pos_2 < 0) {
			pos_2 += 8;
		}
		drawTexturedModalRect(ps[pos_2].x + 1, ps[pos_2].y + 1, 11, 12, 4, 4);
		GlStateManager.popMatrix();
	}

	public GuiTextArea getTextArea(int id) {
		for (IComponentGui component : components) {
			if (component instanceof GuiTextArea && component.getId() == id) { return (GuiTextArea) component; }
		}
		return null;
	}

	public GuiNpcButton getButton(int id) { return buttons.get(id); }

	@Override
	public int getEventButton() {
		return ((IGuiScreenMixin) this).npcs$getEventButton();
	}

	public FontRenderer getFontRenderer() {
		return fontRenderer;
	}

	public GuiNpcLabel getLabel(int id) { return labels.get(id); }

	@Override
	public EntityNPCInterface getNPC() {
		return npc;
	}

	public ResourceLocation getResource(String texture) { return new ResourceLocation(CustomNpcs.MODID, "textures/gui/" + texture); }

	public GuiCustomScroll getScroll(int id) {
		return scrolls.get(id);
	}

	public GuiNpcSlider getSlider(int id) {
		return sliders.get(id);
	}

	public SubGuiInterface getSubGui() {
		if (hasSubGui() && subgui.hasSubGui()) {
			return subgui.getSubGui();
		}
		return subgui;
	}

	public GuiNpcTextField getTextField(int id) {
		return textfields.get(id);
	}

	public GuiMenuTopButton getTopButton(int id) {
		return topbuttons.get(id);
	}
	
	public GuiNpcMiniWindow getMiniWindow(int id) {
		return mwindows.get(id);
	}

	@Override
	public boolean hasSubGui() {
		return subgui != null;
	}

	public void initGui() {
		super.initGui();
		GuiNpcTextField.unfocus();
		components.clear();
		buttonList.clear();
		buttons.clear();
		topbuttons.clear();
		scrolls.clear();
		sliders.clear();
		labels.clear();
		textfields.clear();
		mwindows.clear();
		Keyboard.enableRepeatEvents(true);
		if (subgui != null) {
			subgui.setWorldAndResolution(mc, width, height);
			subgui.initGui();
		}
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
		hoverText.clear();
		hasArea = false;
	}

	public void initPacket() { }

	public boolean isInventoryKey(int i) {
		return i == mc.gameSettings.keyBindInventory.getKeyCode();
	}

	public boolean isMouseHover(int mX, int mY, int px, int py, int pwidth, int pheight) {
		return mX >= px && mY >= py && mX < (px + pwidth) && mY < (py + pheight);
	}

	protected void keyTyped(char c, int i) {
		if (subgui != null) {
			subgui.keyTyped(c, i);
			return;
		}
		for (GuiNpcMiniWindow mwin : mwindows.values()) {
			mwin.keyTyped(c, i);
		}
		if (hoverMiniWin) { return; }
		boolean helpButtons = false;
		if (i == 56 || i == 29 || i == 184) {
			helpButtons = Keyboard.isKeyDown(35);
		} else if (i == 35) {
			helpButtons = Keyboard.isKeyDown(56) || Keyboard.isKeyDown(29) || Keyboard.isKeyDown(184);
		}
		if (helpButtons) {
			CustomNpcs.ShowDescriptions = !CustomNpcs.ShowDescriptions;
		}
		for (GuiNpcTextField tf : new ArrayList<>(textfields.values())) {
			tf.textboxKeyTyped(c, i);
		}
		if (closeOnEsc && (i == 1 || (i == mc.gameSettings.keyBindInventory.getKeyCode() && !GuiNpcTextField.isActive()))) {
			close();
		}
		for (GuiCustomScroll scroll : new ArrayList<>(scrolls.values())) {
			scroll.keyTyped(c, i);
		}
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (subgui != null) {
			subgui.mouseClicked(mouseX, mouseY, mouseButton);
			return;
		}
		for (GuiNpcMiniWindow mwin : mwindows.values()) {
			mwin.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (hoverMiniWin) { return; }
		for (GuiNpcTextField tf : new ArrayList<>(textfields.values())) {
			if (tf.enabled) {
				tf.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
		if (mouseButton == 0) {
			for (GuiCustomScroll scroll : new ArrayList<>(scrolls.values())) {
				scroll.mouseClicked(mouseX, mouseY, mouseButton);
			}
		} else {
			for (GuiNpcButton button : buttons.values()) {
				if (button.isMouseOver()) {
					buttonEvent(button, mouseButton);
					break;
				}
			}
		}
		mouseEvent(mouseX, mouseY, mouseButton);
		List<GuiButton> allButtons = new ArrayList<>();
		for (IComponentGui component : components) {
			if (component instanceof GuiButton) { allButtons.add((GuiButton) component); }
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
		try {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	public void mouseEvent(int mouseX, int mouseY, int mouseButton) {
		for (GuiNpcMiniWindow mwin : mwindows.values()) {
			mwin.mouseEvent(mouseX, mouseY, mouseButton);
		}
	}

	public abstract void save();

	public void setBackground(String texture) {
		background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/" + texture);
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
			if (!hoverText.isEmpty()) { drawHoveringText(hoverText, mouseX, mouseY, fontRenderer); }
			hoverText.clear();
			return;
		}
		setHoverText(text, args);
		if (!hoverText.isEmpty()) {
			drawHoveringText(hoverText, mouseX, mouseY, fontRenderer);
			hoverText.clear();
		}
	}

	public boolean hasHoverText() { return !hoverText.isEmpty(); }

	public void setSubGui(SubGuiInterface gui) {
		(subgui = gui).setWorldAndResolution(mc, width, height);
		(subgui.parent = this).initGui();
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
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) { }

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

	@Override
	public void mouseDragged(GuiNpcSlider slider) { }

	@Override
	public void mousePressed(GuiNpcSlider slider) { }

	@Override
	public void mouseReleased(GuiNpcSlider slider) {
	}

	@Override
	public void unFocused(GuiNpcTextField textField) { }
	
	@Override
	public void addLine(int sX, int sY, int eX, int eY, int color, int size) {
		line.add(new int[] { sX, sY, eX, eY, color, size });
	}
	
	@Override
	public void closeMiniWindow(GuiNpcMiniWindow miniWindow) {
		mwindows.remove(miniWindow.id);
	}
	
	@Override
	public void setMiniHoverText(int id, IComponentGui component) {}

	@Override
	public boolean hasArea() { return hasArea; }

}
