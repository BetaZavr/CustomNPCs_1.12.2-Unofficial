package noppes.npcs.client.gui.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.containers.ContainerEmpty;
import noppes.npcs.entity.EntityNPCInterface;

public abstract class GuiContainerNPCInterface
extends GuiContainer {
	
	public static ResourceLocation ball = new ResourceLocation(CustomNpcs.MODID, "textures/gui/info.png");
	private HashMap<Integer, GuiNpcButton> buttons;
	public boolean closeOnEsc;
	public boolean drawDefaultBackground;
	public int guiLeft;
	public int guiTop;
	// New
	public String[] hoverText;
	private HashMap<Integer, GuiNpcLabel> labels;
	public int mouseX;
	public int mouseY;
	public EntityNPCInterface npc;
	public EntityPlayerSP player;
	private Poses[] ps;
	private HashMap<Integer, GuiCustomScroll> scrolls;
	private HashMap<Integer, GuiNpcSlider> sliders;
	public SubGuiInterface subgui; // Changed
	private HashMap<Integer, GuiNpcTextField> textfields;
	public String title;
	private HashMap<Integer, GuiMenuTopButton> topbuttons;

	public GuiContainerNPCInterface(EntityNPCInterface npc, Container cont) {
		super(cont);
		this.drawDefaultBackground = false;
		this.buttons = new HashMap<Integer, GuiNpcButton>();
		this.topbuttons = new HashMap<Integer, GuiMenuTopButton>();
		this.textfields = new HashMap<Integer, GuiNpcTextField>();
		this.labels = new HashMap<Integer, GuiNpcLabel>();
		this.scrolls = new HashMap<Integer, GuiCustomScroll>();
		this.sliders = new HashMap<Integer, GuiNpcSlider>();
		this.closeOnEsc = false;
		this.player = Minecraft.getMinecraft().player;
		this.npc = npc;
		this.title = "Npc Mainmenu";
		this.mc = Minecraft.getMinecraft();
		this.itemRender = this.mc.getRenderItem();
		this.fontRenderer = this.mc.fontRenderer;
		// New
		this.ps = new Poses[] { new Poses(this, 0), new Poses(this, 1), new Poses(this, 2), new Poses(this, 3),
				new Poses(this, 4), new Poses(this, 5), new Poses(this, 6), new Poses(this, 7) };
	}

	protected void actionPerformed(GuiButton guibutton) {
		if (this.subgui != null) {
			this.subgui.buttonEvent(guibutton);
		} else {
			this.buttonEvent(guibutton);
		}
	}

	public void addButton(GuiNpcButton button) {
		this.buttons.put(button.id, button);
		this.buttonList.add(button);
	}

	public void addLabel(GuiNpcLabel label) {
		this.labels.put(label.id, label);
	}

	public void addScroll(GuiCustomScroll scroll) {
		scroll.setWorldAndResolution(this.mc, scroll.width, scroll.height);
		this.scrolls.put(scroll.id, scroll);
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

	public void buttonEvent(GuiButton guibutton) {
	}
	
	public void close() {
		GuiNpcTextField.unfocus();
		this.save();
		this.player.closeScreen();
		this.displayGuiScreen(null);
		this.mc.setIngameFocus();
	}

	public void closeSubGui(SubGuiInterface gui) {
		this.subgui = null;
	}

	public void displayGuiScreen(GuiScreen gui) {
		this.mc.displayGuiScreen(gui);
	}

	public void drawDefaultBackground() {
		if (this.drawDefaultBackground && this.subgui == null) {
			super.drawDefaultBackground();
		}
	}

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.drawCenteredString(this.fontRenderer, new TextComponentTranslation(this.title).getFormattedText(),
				this.width / 2, this.guiTop - 8, 16777215);
		for (GuiNpcLabel label : new ArrayList<GuiNpcLabel>(this.labels.values())) {
			label.drawLabel((GuiScreen) this, this.fontRenderer, mouseX, mouseY, partialTicks);
		}
		for (GuiNpcTextField tf : new ArrayList<GuiNpcTextField>(this.textfields.values())) {
			tf.drawTextBox(mouseX, mouseY);
		}
		for (GuiCustomScroll scroll : new ArrayList<GuiCustomScroll>(this.scrolls.values())) {
			scroll.drawScreen(mouseX, mouseY, partialTicks, this.hasSubGui() ? 0 : Mouse.getDWheel());
		}
	}

	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
	}

	public void drawNpc(int x, int y) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableColorMaterial();
		GlStateManager.pushMatrix();
		GlStateManager.translate((this.guiLeft + x), (this.guiTop + y), 50.0f);
		float scale = 1.0f;
		if (this.npc.height > 2.4) {
			scale = 2.0f / this.npc.height;
		}
		GlStateManager.scale(-30.0f * scale, 30.0f * scale, 30.0f * scale);
		GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
		float f2 = this.npc.renderYawOffset;
		float f3 = this.npc.rotationYaw;
		float f4 = this.npc.rotationPitch;
		float f5 = this.npc.rotationYawHead;
		float f6 = this.guiLeft + x - this.mouseX;
		float f7 = this.guiTop + y - 50 - this.mouseY;
		int orientation = 0;
		if (this.npc != null) {
			orientation = this.npc.ais.orientation;
			this.npc.ais.orientation = 0;
		}
		GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f);
		GlStateManager.rotate((float) (-Math.atan(f7 / 40.0f) * 20.0f), 1.0f, 0.0f, 0.0f);
		this.npc.renderYawOffset = (float) (Math.atan(f6 / 40.0f) * 20.0f);
		this.npc.rotationYaw = (float) (Math.atan(f6 / 40.0f) * 40.0f);
		this.npc.rotationPitch = (float) (-Math.atan(f7 / 40.0f) * 20.0f);
		this.npc.rotationYawHead = this.npc.rotationYaw;
		this.mc.getRenderManager().playerViewY = 180.0f;
		this.mc.getRenderManager().renderEntity(this.npc, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
		this.npc.renderYawOffset = f2;
		this.npc.rotationYaw = f3;
		this.npc.rotationPitch = f4;
		this.npc.rotationYawHead = f5;
		if (this.npc != null) {
			this.npc.ais.orientation = orientation;
		}
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		Container container = this.inventorySlots;
		if (this.subgui != null) {
			this.inventorySlots = new ContainerEmpty();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.zLevel = 0.0f;
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (this.subgui != null) {
			this.inventorySlots = container;
			RenderHelper.disableStandardItemLighting();
			this.subgui.drawScreen(mouseX, mouseY, partialTicks);
		} else {
			this.renderHoveredToolTip(this.mouseX, this.mouseY);
		}
		// New
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, fontRenderer);
			this.hoverText = null;
		}
	}

	public void drawWait(int mouseX, int mouseY, float partialTicks) {
		ScaledResolution scaleW = new ScaledResolution(this.mc);
		this.drawCenteredString(this.fontRenderer, new TextComponentTranslation("gui.wait").getFormattedText(),
				scaleW.getScaledWidth() / 2, scaleW.getScaledHeight() / 2, 0xFFFF0000);
		int pos_0 = (int) Math.floor((double) (this.player.world.getTotalWorldTime() % 16) / 2.0d);
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.getTextureManager().bindTexture(GuiContainerNPCInterface.ball);
		this.drawTexturedModalRect(this.ps[pos_0].x - 1, this.ps[pos_0].y - 1, 0, 12, 6, 6);
		int pos_1 = pos_0 - 1;
		if (pos_1 < 0) {
			pos_1 += 8;
		}
		this.drawTexturedModalRect(this.ps[pos_1].x, this.ps[pos_1].y, 6, 12, 5, 5);
		int pos_2 = pos_0 - 2;
		if (pos_2 < 0) {
			pos_2 += 8;
		}
		this.drawTexturedModalRect(this.ps[pos_2].x + 1, this.ps[pos_2].y + 1, 11, 12, 4, 4);
		GlStateManager.popMatrix();
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

	public boolean hasSubGui() {
		return this.subgui != null;
	}

	public void initGui() {
		super.initGui();
		GuiNpcTextField.unfocus();
		this.buttonList.clear();
		this.buttons.clear();
		this.topbuttons.clear();
		this.scrolls.clear();
		this.sliders.clear();
		this.labels.clear();
		this.textfields.clear();
		Keyboard.enableRepeatEvents(true);
		if (this.subgui != null) {
			this.subgui.setWorldAndResolution(this.mc, this.width, this.height);
			this.subgui.initGui();
		}
		this.buttonList.clear();
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
	}

	public void initPacket() {
	}

	// New
	public boolean isMouseHover(int mX, int mY, int px, int py, int pwidth, int pheight) {
		return mX >= px && mY >= py && mX < (px + pwidth) && mY < (py + pheight);
	}

	protected void keyTyped(char c, int i) {
		boolean helpButtons = false;
		if (i==56 || i==29 || i==184) { // Alt
			helpButtons = Keyboard.isKeyDown(35); // + H
		} else if (i==35) { // H
			helpButtons = Keyboard.isKeyDown(56)||Keyboard.isKeyDown(29)||Keyboard.isKeyDown(184); // + Alt
		}
		if (helpButtons) {
			CustomNpcs.showDescriptions = !CustomNpcs.showDescriptions;
		}
		if (this.subgui != null) {
			this.subgui.keyTyped(c, i);
		} else {
			for (GuiNpcTextField tf : new ArrayList<GuiNpcTextField>(this.textfields.values())) {
				tf.textboxKeyTyped(c, i);
			}
			if (this.closeOnEsc && (i == 1
					|| (i == this.mc.gameSettings.keyBindInventory.getKeyCode() && !GuiNpcTextField.isActive()))) {
				this.close();
			}
			for (GuiCustomScroll scroll : new ArrayList<GuiCustomScroll>(this.scrolls.values())) {
				scroll.keyTyped(c, i);
			}
		}
	}

	protected void mouseClicked(int i, int j, int k) throws IOException {
		if (this.subgui != null) {
			this.subgui.mouseClicked(i, j, k);
		} else {
			for (GuiNpcTextField tf : new ArrayList<GuiNpcTextField>(this.textfields.values())) {
				if (tf.enabled) {
					tf.mouseClicked(i, j, k);
				}
			}
			if (k == 0) {
				for (GuiCustomScroll scroll : new ArrayList<GuiCustomScroll>(this.scrolls.values())) {
					scroll.mouseClicked(i, j, k);
				}
			}
			this.mouseEvent(i, j, k);
			super.mouseClicked(i, j, k);
		}
	}

	public void mouseEvent(int i, int j, int k) {
	}

	public abstract void save();

	public void setHoverText(String text) {
		List<String> list = new ArrayList<String>();
		text = new TextComponentTranslation(text).getFormattedText();
		while (text.indexOf("<br>") != -1) {
			list.add(text.substring(0, text.indexOf("<br>")));
			text = text.substring(text.indexOf("<br>") + 4);
		}
		list.add(text);
		String[] array = new String[list.size()];
		int i = 0;
		for (String str : list) {
			array[i] = str;
			i++;
		}
		this.hoverText = array;
	}

	public void setSubGui(SubGuiInterface gui) {
		(this.subgui = gui).setWorldAndResolution(this.mc, this.width, this.height);
		((GuiContainerNPCInterface) (this.subgui.parent = (GuiScreen) this)).initGui();
	}

	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		this.initPacket();
	}

	public void updateScreen() {
		for (GuiNpcTextField tf : new ArrayList<GuiNpcTextField>(this.textfields.values())) {
			if (tf.enabled) {
				tf.updateCursorCounter();
			}
		}
		super.updateScreen();
	}

	public void clear() {
		this.buttons.clear();
		this.labels.clear();
		this.scrolls.clear();
		this.sliders.clear();
		this.textfields.clear();
		this.topbuttons.clear();
	}
	
}
