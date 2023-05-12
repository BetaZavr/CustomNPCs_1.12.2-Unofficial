package noppes.npcs.client.gui.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.entity.EntityNPCInterface;

public abstract class GuiNPCInterface
extends GuiScreen {
	
	public ResourceLocation background;
	public float bgScale;
	private Map<Integer, GuiNpcButton> buttons;
	public boolean closeOnEsc;
	private List<IGui> components;
	public boolean drawDefaultBackground;
	private Map<Integer, GuiScreen> extra;
	public int guiLeft;
	public int guiTop;
	// New
	public String[] hoverText;
	private Map<Integer, GuiNpcLabel> labels;
	public int mouseX;
	public int mouseY;
	public EntityNPCInterface npc;
	public EntityPlayerSP player;
	private Map<Integer, GuiCustomScroll> scrolls;
	private GuiButton selectedButton;
	private Map<Integer, GuiMenuSideButton> sidebuttons;
	private Map<Integer, GuiNpcSlider> sliders;
	public SubGuiInterface subgui; // Changed
	private Map<Integer, GuiNpcTextField> textfields;
	public String title;
	private Map<Integer, GuiMenuTopButton> topbuttons;
	private Map<Integer, GuiMenuLeftButton> leftbuttons;
	public int xSize;
	public int ySize;

	public GuiNPCInterface() {
		this(null);
	}

	public GuiNPCInterface(EntityNPCInterface npc) {
		this.buttons = new ConcurrentHashMap<Integer, GuiNpcButton>();
		this.topbuttons = new ConcurrentHashMap<Integer, GuiMenuTopButton>();
		this.leftbuttons = new ConcurrentHashMap<Integer, GuiMenuLeftButton>();
		this.sidebuttons = new ConcurrentHashMap<Integer, GuiMenuSideButton>();
		this.textfields = new ConcurrentHashMap<Integer, GuiNpcTextField>();
		this.labels = new ConcurrentHashMap<Integer, GuiNpcLabel>();
		this.scrolls = new ConcurrentHashMap<Integer, GuiCustomScroll>();
		this.sliders = new ConcurrentHashMap<Integer, GuiNpcSlider>();
		this.extra = new ConcurrentHashMap<Integer, GuiScreen>();
		this.components = new ArrayList<IGui>();
		this.background = null;
		this.closeOnEsc = false;
		this.bgScale = 1.0f;
		this.player = Minecraft.getMinecraft().player;
		this.npc = npc;
		this.title = "";
		this.xSize = 200;
		this.ySize = 222;
		this.drawDefaultBackground = false;
		this.mc = Minecraft.getMinecraft();
		this.itemRender = this.mc.getRenderItem();
		this.fontRenderer = this.mc.fontRenderer;
	}

	protected void actionPerformed(GuiButton guibutton) {
		if (this.subgui != null) {
			this.subgui.buttonEvent(guibutton);
		} else {
			this.buttonEvent(guibutton);
		}
	}

	public void add(IGui gui) {
		this.components.add(gui);
	}

	public void addButton(GuiNpcButton button) {
		this.buttons.put(button.id, button);
		this.buttonList.add(button);
	}

	public void addExtra(GuiHoverText gui) {
		gui.setWorldAndResolution(this.mc, 350, 250);
		this.extra.put(gui.id, gui);
	}

	public void addLabel(GuiNpcLabel label) {
		this.labels.put(label.id, label);
	}

	public void addScroll(GuiCustomScroll scroll) {
		scroll.setWorldAndResolution(this.mc, scroll.width, scroll.height);
		this.scrolls.put(scroll.id, scroll);
	}

	public void addSideButton(GuiMenuSideButton button) {
		this.sidebuttons.put(button.id, button);
		this.buttonList.add(button);
	}

	public void addSlider(GuiNpcSlider slider) {
		this.sliders.put(slider.id, slider);
		this.buttonList.add(slider);
	}

	public void addTextField(GuiNpcTextField tf) {
		this.textfields.put(tf.getId(), tf);
	}

	public void addTopButton(GuiMenuTopButton button) {
		this.topbuttons.put(button.id, button);
		this.buttonList.add(button);
	}

	public void addLeftButton(GuiMenuLeftButton button) {
		this.leftbuttons.put(button.id, button);
		this.buttonList.add(button);
	}

	public void buttonEvent(GuiButton guibutton) {
	}

	public void close() {
		this.displayGuiScreen(null);
		this.mc.setIngameFocus();
		this.save();
	}

	public void closeSubGui(SubGuiInterface gui) {
		this.subgui = null;
	}

	public void displayGuiScreen(GuiScreen gui) {
		this.mc.displayGuiScreen(gui);
	}

	public boolean doesGuiPauseGame() {
		return false;
	}

	public void doubleClicked() {
	}

	public void drawDefaultBackground() {
		super.drawDefaultBackground();
	}

	public void drawNpc(EntityLivingBase entity, int x, int y, float zoomed, int rotation, boolean mouseFocus) {
		EntityNPCInterface npc = null;
		if (entity instanceof EntityNPCInterface) {
			npc = (EntityNPCInterface) entity;
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate((this.guiLeft + x), (this.guiTop + y), 50.0f);
		float scale = 1.0f;
		if (entity.height > 2.4) {
			scale = 2.0f / entity.height;
		}
		GlStateManager.scale(-30.0f * scale * zoomed, 30.0f * scale * zoomed, 30.0f * scale * zoomed);
		GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
		RenderHelper.enableStandardItemLighting();
		float f2 = entity.renderYawOffset;
		float f3 = entity.rotationYaw;
		float f4 = entity.rotationPitch;
		float f5 = entity.rotationYawHead;
		float f6 = !mouseFocus ? 0 : this.guiLeft + x - this.mouseX;
		float f7 = !mouseFocus ? 0 : this.guiTop + y - 50.0f * scale * zoomed - this.mouseY;
		int orientation = 0;
		if (npc != null) {
			orientation = npc.ais.orientation;
			npc.ais.orientation = rotation;
		}
		GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate((float) (-Math.atan(f7 / 40.0f) * 20.0f), 1.0f, 0.0f, 0.0f);
		entity.renderYawOffset = rotation;
		entity.rotationYaw = (float) (Math.atan(f6 / 80.0f) * 40.0f + rotation);
		entity.rotationPitch = (float) (-Math.atan(f7 / 40.0f) * 20.0f);
		entity.rotationYawHead = entity.rotationYaw;
		this.mc.getRenderManager().playerViewY = 180.0f;
		this.mc.getRenderManager().renderEntity(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
		float n = f2;
		entity.renderYawOffset = n;
		entity.prevRenderYawOffset = n;
		float n2 = f3;
		entity.rotationYaw = n2;
		entity.prevRotationYaw = n2;
		float n3 = f4;
		entity.rotationPitch = n3;
		entity.prevRotationPitch = n3;
		float n4 = f5;
		entity.rotationYawHead = n4;
		entity.prevRotationYawHead = n4;
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
		this.drawNpc(this.npc, x, y, 1.0f, 0, true);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		//this.close();
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		int x = mouseX;
		int y = mouseY;
		if (this.subgui != null) {
			y = (x = 0);
		}
		if (this.drawDefaultBackground && this.subgui == null) {
			this.drawDefaultBackground();
		}
		if (this.background != null && this.mc.renderEngine != null) {
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft, this.guiTop, 0.0f);
			GlStateManager.scale(this.bgScale, this.bgScale, this.bgScale);
			this.mc.renderEngine.bindTexture(this.background);
			if (this.xSize>252) {
				this.drawTexturedModalRect(0, 0, 0, 0, 252, this.ySize);
				int w = this.xSize-252;
				this.drawTexturedModalRect(252, 0, 256-w, 0, w, this.ySize);
			}
			else { this.drawTexturedModalRect(0, 0, 0, 0, this.xSize,this.ySize); }
			GlStateManager.popMatrix();
		}
		GlStateManager.translate(0, 0, 1.0f);
		this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, this.height + 10, 16777215);
		for (GuiNpcLabel label : new ArrayList<GuiNpcLabel>(this.labels.values())) {
			label.drawLabel((GuiScreen) this, this.fontRenderer, mouseX, mouseY, partialTicks);
		}
		for (GuiNpcTextField tf : new ArrayList<GuiNpcTextField>(this.textfields.values())) {
			tf.drawTextBox(x, y);
		}
		for (IGui comp : new ArrayList<IGui>(this.components)) {
			comp.drawScreen(x, y);
		}
		for (GuiCustomScroll scroll : new ArrayList<GuiCustomScroll>(this.scrolls.values())) {
			scroll.drawScreen(x, y, partialTicks, (!this.hasSubGui() && scroll.isMouseOver(x, y)) ? Mouse.getDWheel() : 0);
		}
		for (GuiScreen gui : new ArrayList<GuiScreen>(this.extra.values())) {
			gui.drawScreen(x, y, partialTicks);
		}
		super.drawScreen(x, y, partialTicks);
		if (this.subgui != null) {
			this.subgui.drawScreen(mouseX, mouseY, partialTicks);
		}
		// New
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	public void elementClicked() {
		if (this.subgui != null) {
			this.subgui.elementClicked();
		}
	}

	public IGui get(int id) {
		for (IGui comp : this.components) {
			if (comp.getID() == id) {
				return comp;
			}
		}
		return null;
	}

	public GuiNpcButton getButton(int i) {
		return this.buttons.get(i);
	}

	public FontRenderer getFontRenderer() {
		return this.fontRenderer;
	}

	public GuiNpcLabel getLabel(int i) {
		return this.labels.get(i);
	}

	public ResourceLocation getResource(String texture) {
		return new ResourceLocation(CustomNpcs.MODID, "textures/gui/" + texture);
	}

	public GuiCustomScroll getScroll(int id) {
		return this.scrolls.get(id);
	}

	public GuiMenuSideButton getSideButton(int i) {
		return this.sidebuttons.get(i);
	}

	public GuiNpcSlider getSlider(int i) {
		return this.sliders.get(i);
	}

	public SubGuiInterface getSubGui() {
		if (this.hasSubGui() && this.subgui.hasSubGui()) {
			return this.subgui.getSubGui();
		}
		return this.subgui;
	}

	public GuiNpcTextField getTextField(int i) {
		return this.textfields.get(i);
	}

	public GuiMenuTopButton getTopButton(int i) {
		return this.topbuttons.get(i);
	}

	public GuiMenuLeftButton getLeftButton(int i) {
		return this.leftbuttons.get(i);
	}

	public boolean hasSubGui() {
		return this.subgui != null;
	}

	public void initGui() {
		super.initGui();
		GuiNpcTextField.unfocus();
		if (this.subgui != null) {
			this.subgui.setWorldAndResolution(this.mc, this.width, this.height);
			this.subgui.initGui();
		}
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		this.buttonList = Lists.newArrayList();
		this.buttons = new ConcurrentHashMap<Integer, GuiNpcButton>();
		this.topbuttons = new ConcurrentHashMap<Integer, GuiMenuTopButton>();
		this.leftbuttons = new ConcurrentHashMap<Integer, GuiMenuLeftButton>();
		this.sidebuttons = new ConcurrentHashMap<Integer, GuiMenuSideButton>();
		this.textfields = new ConcurrentHashMap<Integer, GuiNpcTextField>();
		this.labels = new ConcurrentHashMap<Integer, GuiNpcLabel>();
		this.scrolls = new ConcurrentHashMap<Integer, GuiCustomScroll>();
		this.sliders = new ConcurrentHashMap<Integer, GuiNpcSlider>();
		this.extra = new ConcurrentHashMap<Integer, GuiScreen>();
		this.components = new ArrayList<IGui>();
	}

	public void initPacket() {
	}

	public boolean isInventoryKey(int i) {
		return i == this.mc.gameSettings.keyBindInventory.getKeyCode();
	}

	// New
	public boolean isMouseHover(int mX, int mY, int px, int py, int pwidth, int pheight) {
		return mX >= px && mY >= py && mX < (px + pwidth) && mY < (py + pheight);
	}

	public void keyTyped(char c, int i) {
		if (this.subgui != null) {
			this.subgui.keyTyped(c, i);
			return;
		}
		boolean active = false;
		for (IGui gui : this.components) {
			if (gui.isActive()) {
				active = true;
				break;
			}
		}
		active = (active || GuiNpcTextField.isActive());
		boolean helpButtons = false;
		if (i==56 || i==29 || i==184) { // Alt
			helpButtons = Keyboard.isKeyDown(35); // + H
		} else if (i==35) { // H
			helpButtons = Keyboard.isKeyDown(56)||Keyboard.isKeyDown(29)||Keyboard.isKeyDown(184); // + Alt
		}
		if (helpButtons) {
			CustomNpcs.showDescriptions = !CustomNpcs.showDescriptions;
		}
		if (this.closeOnEsc && (i == 1 || (!active && this.isInventoryKey(i)))) {
			this.close();
			return;
		}
		for (GuiNpcTextField tf : new ArrayList<GuiNpcTextField>(this.textfields.values())) {
			tf.textboxKeyTyped(c, i);
		}
		for (IGui comp : new ArrayList<IGui>(this.components)) {
			if (comp instanceof IKeyListener) {
				((IKeyListener) comp).keyTyped(c, i);
			}
		}
		if (this.hasSubGui()) { return; }
		for (GuiCustomScroll scroll : new ArrayList<GuiCustomScroll>(this.scrolls.values())) {
			scroll.keyTyped(c, i);
		}
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) { // Changed
		if (this.subgui != null) {
			this.subgui.mouseClicked(mouseX, mouseY, mouseBottom);
			return;
		}
		for (GuiNpcTextField tf : new ArrayList<GuiNpcTextField>(this.textfields.values())) {
			if (tf.enabled) {
				tf.mouseClicked(mouseX, mouseY, mouseBottom);
			}
		}
		for (IGui comp : new ArrayList<IGui>(this.components)) {
			if (comp instanceof IMouseListener) {
				((IMouseListener) comp).mouseClicked(mouseX, mouseY, mouseBottom);
			}
		}
		this.mouseEvent(mouseX, mouseY, mouseBottom);
		if (mouseBottom != 0) {
			return;
		}
		for (GuiCustomScroll scroll : new ArrayList<GuiCustomScroll>(this.scrolls.values())) {
			scroll.mouseClicked(mouseX, mouseY, mouseBottom);
		}
		for (GuiButton guibutton : this.buttonList) {
			if (guibutton.mousePressed(this.mc, this.mouseX, this.mouseY)) {
				GuiScreenEvent.ActionPerformedEvent.Pre event = new GuiScreenEvent.ActionPerformedEvent.Pre(
						(GuiScreen) this, guibutton, this.buttonList);
				if (MinecraftForge.EVENT_BUS.post((Event) event)) {
					break;
				}
				guibutton = event.getButton();
				(this.selectedButton = guibutton).playPressSound(this.mc.getSoundHandler());
				this.actionPerformed(guibutton);
				if (this.equals(this.mc.currentScreen)) {
					MinecraftForge.EVENT_BUS.post((Event) new GuiScreenEvent.ActionPerformedEvent.Post((GuiScreen) this,
							event.getButton(), this.buttonList));
					break;
				}
				break;
			}
		}
	}

	public void mouseEvent(int i, int j, int k) {
	}

	public void mouseReleased(int mouseX, int mouseY, int state) {
		if (this.selectedButton != null && state == 0) {
			this.selectedButton.mouseReleased(mouseX, mouseY);
			this.selectedButton = null;
		}
	}

	public void onGuiClosed() {
		GuiNpcTextField.unfocus();
	}

	public void openLink(String link) {
		try {
			Class<?> oclass = Class.forName("java.awt.Desktop");
			Object object = oclass.getMethod("getDesktop", (Class[]) new Class[0]).invoke(null, new Object[0]);
			oclass.getMethod("browse", URI.class).invoke(object, new URI(link));
		} catch (Throwable t) {
		}
	}

	public abstract void save();

	public void setBackground(String texture) {
		this.background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/" + texture);
	}

	public void setHoverText(String text) {
		List<String> list = new ArrayList<String>();
		if (text.indexOf("%")==-1) { text = new TextComponentTranslation(text).getFormattedText(); }
		if (text.indexOf("~~~")!=-1) {
			while (text.indexOf("~~~")!=-1) { text = text.replace("~~~", "%"); }
		}
		while (text.indexOf("<br>") != -1) {
			list.add(text.substring(0, text.indexOf("<br>")));
			text = text.substring(text.indexOf("<br>") + 4);
		}
		list.add(text);
		this.hoverText = list.toArray(new String[list.size()]);
	}

	public void setSubGui(SubGuiInterface gui) {
		this.subgui = gui;
		this.subgui.npc = this.npc;
		this.subgui.setWorldAndResolution(this.mc, this.width, this.height);
		((GuiNPCInterface) (this.subgui.parent = this)).initGui();
	}

	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		this.initPacket();
	}

	public void updateScreen() {
		if (this.subgui != null) {
			this.subgui.updateScreen();
		} else {
			for (GuiNpcTextField tf : new ArrayList<GuiNpcTextField>(this.textfields.values())) {
				if (tf.enabled) {
					tf.updateCursorCounter();
				}
			}
			for (IGui comp : new ArrayList<IGui>(this.components)) {
				comp.updateScreen();
			}
			super.updateScreen();
		}
	}
}
