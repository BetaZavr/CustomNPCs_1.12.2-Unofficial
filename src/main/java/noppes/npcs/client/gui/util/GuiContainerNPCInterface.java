package noppes.npcs.client.gui.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import noppes.npcs.LogWriter;
import noppes.npcs.mixin.api.client.gui.GuiScreenAPIMixin;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;

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

public abstract class GuiContainerNPCInterface
extends GuiContainer
implements IEditNPC {

	public static ResourceLocation ball = new ResourceLocation(CustomNpcs.MODID, "textures/gui/info.png");

	public boolean closeOnEsc = false;
	public boolean hoverMiniWin = false;
	public boolean drawDefaultBackground = false;
	public int guiLeft, guiTop, mouseX, mouseY;
	public float bgScale = 1.0f;
	public String title = "Npc Mainmenu";
	public String[] hoverText;

	public ResourceLocation background;
	public EntityNPCInterface npc;
	public EntityPlayerSP player;
	public SubGuiInterface subgui;

	private final Poses[] ps;
	private final List<int[]> line = Lists.newArrayList(); // startX, startY, endX, endY, color, lineSize
	private final List<IGui> components = Lists.newArrayList();
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
		this.mc = Minecraft.getMinecraft();
		this.player = this.mc.player;
		this.itemRender = this.mc.getRenderItem();
		this.fontRenderer = this.mc.fontRenderer;
		this.ps = new Poses[] { new Poses(this, 0), new Poses(this, 1), new Poses(this, 2), new Poses(this, 3),
				new Poses(this, 4), new Poses(this, 5), new Poses(this, 6), new Poses(this, 7) };
	}

	protected void actionPerformed(@Nonnull GuiButton guibutton) {
		if (!(guibutton instanceof GuiNpcButton)) {
			return;
		}
		if (this.subgui != null) {
			this.subgui.buttonEvent((GuiNpcButton) guibutton);
		} else {
			for (GuiNpcMiniWindow mwin : this.mwindows.values()) {
				mwin.buttonEvent((GuiNpcButton) guibutton);
			}
			if (this.hoverMiniWin) { return; }
			this.buttonEvent((GuiNpcButton) guibutton);
		}
	}

	public void add(IGui gui) {
		this.components.add(gui);
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

	public void addMiniWindow(GuiNpcMiniWindow miniwindows) {
		this.mwindows.put(miniwindows.id, miniwindows);
		miniwindows.resetButtons();
	}
	
	public void buttonEvent(GuiNpcButton button) {
	}

	public void clear() {
		this.buttons.clear();
		this.labels.clear();
		this.scrolls.clear();
		this.sliders.clear();
		this.textfields.clear();
		this.topbuttons.clear();
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
			if (this.background != null && this.mc.renderEngine != null) {
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft, this.guiTop, 0.0f);
				GlStateManager.scale(this.bgScale, this.bgScale, this.bgScale);
				this.mc.renderEngine.bindTexture(this.background);
				if (this.xSize > 252) {
					this.drawTexturedModalRect(0, 0, 0, 0, 252, this.ySize);
					int w = this.xSize - 252;
					this.drawTexturedModalRect(252, 0, 256 - w, 0, w, this.ySize);
				} else {
					this.drawTexturedModalRect(0, 0, 0, 0, this.xSize, this.ySize);
				}
				GlStateManager.popMatrix();
			}
			this.postDrawBackground();
			super.drawDefaultBackground();
		}
	}

	public void postDrawBackground() { }

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.drawCenteredString(this.fontRenderer, new TextComponentTranslation(this.title).getFormattedText(), this.width / 2, this.guiTop - 8, 16777215);
		if (!this.line.isEmpty()) {
			for (int[] ln : this.line) {
				if (ln == null || ln.length < 6) { continue; }
				GuiBoundarySetting.drawLine(ln[0], ln[1], ln[2], ln[3], ln[4], ln[5]);
			}
			this.line.clear();
		}
		for (GuiNpcLabel label : new ArrayList<>(this.labels.values())) {
			label.drawLabel(this, this.fontRenderer, mouseX, mouseY);
		}
		boolean hasArea = false;
		for (IGui comp : new ArrayList<>(this.components)) {
			comp.drawScreen(mouseX, mouseY);
			if (comp instanceof GuiNpcTextArea) {
				hasArea = true;
			}
		}
		for (GuiNpcTextField tf : new ArrayList<>(this.textfields.values())) {
			tf.drawTextBox(mouseX, mouseY);
			if (tf instanceof GuiNpcTextArea) {
				hasArea = true;
			}
		}
		for (GuiCustomScroll scroll : new ArrayList<>(this.scrolls.values())) {
			scroll.drawScreen(mouseX, mouseY,
					(!this.hasSubGui() && (scroll.hovered || (this.scrolls.isEmpty() && !hasArea)))
							? Mouse.getDWheel()
							: 0);
		}
		this.hoverMiniWin = false;
		for (GuiNpcMiniWindow mwin : this.mwindows.values()) {
			mwin.drawScreen(mouseX, mouseY, partialTicks);
			if (mwin.hovered) { this.hoverMiniWin = true; }
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

		GlStateManager.translate((this.guiLeft + x), (this.guiTop + y), 50.0f);
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
		float f6 = mouseFocus == 0 || mouseFocus == 2 ? 0 : this.guiLeft + x - this.mouseX;
		float f7 = mouseFocus == 0 || mouseFocus == 3 ? 0 : this.guiTop + y - 50.0f * scale * zoomed - this.mouseY;
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
		this.mc.getRenderManager().playerViewY = 180.0f;
		if (mouseFocus != 0 && vertical != 0) {
			GlStateManager.translate(0.0f, 1.0f - Math.cos((double) vertical * 3.14d / 180.0d), 0.0f);
			GlStateManager.rotate(vertical, 1.0f, 0.0f, 0.0f);
		}
		this.mc.getRenderManager().renderEntity(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
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
		this.drawNpc(this.npc, x, y, 1.0f, 0, 0, 1);
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
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, fontRenderer);
			this.hoverText = null;
		}
	}

	public void drawWait() {
		this.drawCenteredString(this.fontRenderer, new TextComponentTranslation("gui.wait").getFormattedText(), this.mc.displayWidth / 2, this.mc.displayHeight / 2, CustomNpcs.MainColor.getRGB());
		int pos_0 = (int) Math.floor((double) (this.player.world.getTotalWorldTime() % 16) / 2.0d);
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(GuiContainerNPCInterface.ball);
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

	public IGui get(int id) {
		for (IGui comp : this.components) {
			if (comp.getId() == id) {
				return comp;
			}
		}
		return null;
	}

	public GuiNpcButton getButton(int i) {
		return this.buttons.get(i);
	}

	@Override
	public int getEventButton() {
		return ((GuiScreenAPIMixin) this).npcs$getEventButton();
	}

	public FontRenderer getFontRenderer() {
		return this.fontRenderer;
	}

	public GuiNpcLabel getLabel(int i) {
		return this.labels.get(i);
	}

	@Override
	public EntityNPCInterface getNPC() {
		return this.npc;
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
	
	public GuiNpcMiniWindow getMiniWindow(int i) {
		return this.mwindows.get(i);
	}

	@Override
	public boolean hasSubGui() {
		return this.subgui != null;
	}

	public void initGui() {
		super.initGui();
		GuiNpcTextField.unfocus();
		this.components.clear();
		this.buttonList.clear();
		this.buttons.clear();
		this.topbuttons.clear();
		this.scrolls.clear();
		this.sliders.clear();
		this.labels.clear();
		this.textfields.clear();
		this.mwindows.clear();
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

	public boolean isInventoryKey(int i) {
		return i == this.mc.gameSettings.keyBindInventory.getKeyCode();
	}

	// New
	public boolean isMouseHover(int mX, int mY, int px, int py, int pwidth, int pheight) {
		return mX >= px && mY >= py && mX < (px + pwidth) && mY < (py + pheight);
	}

	protected void keyTyped(char c, int i) {
		if (this.subgui != null) {
			this.subgui.keyTyped(c, i);
			return;
		}
		for (GuiNpcMiniWindow mwin : this.mwindows.values()) {
			mwin.keyTyped(c, i);
		}
		if (this.hoverMiniWin) { return; }
		boolean helpButtons = false;
		if (i == 56 || i == 29 || i == 184) {
			helpButtons = Keyboard.isKeyDown(35);
		} else if (i == 35) {
			helpButtons = Keyboard.isKeyDown(56) || Keyboard.isKeyDown(29) || Keyboard.isKeyDown(184);
		}
		if (helpButtons) {
			CustomNpcs.ShowDescriptions = !CustomNpcs.ShowDescriptions;
		}
		for (GuiNpcTextField tf : new ArrayList<>(this.textfields.values())) {
			tf.textboxKeyTyped(c, i);
		}
		if (this.closeOnEsc && (i == 1
				|| (i == this.mc.gameSettings.keyBindInventory.getKeyCode() && !GuiNpcTextField.isActive()))) {
			this.close();
		}
		for (GuiCustomScroll scroll : new ArrayList<>(this.scrolls.values())) {
			scroll.keyTyped(c, i);
		}
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (this.subgui != null) {
			this.subgui.mouseClicked(mouseX, mouseY, mouseButton);
			return;
		}
		for (GuiNpcMiniWindow mwin : this.mwindows.values()) {
			mwin.mouseClicked(mouseX, mouseY, mouseButton);
		}
		if (this.hoverMiniWin) { return; }
		for (GuiNpcTextField tf : new ArrayList<>(this.textfields.values())) {
			if (tf.enabled) {
				tf.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
		if (mouseButton == 0) {
			for (GuiCustomScroll scroll : new ArrayList<>(this.scrolls.values())) {
				scroll.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
		this.mouseEvent(mouseX, mouseY, mouseButton);
		try {
			super.mouseClicked(mouseX, mouseY, mouseButton);
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	public void mouseEvent(int mouseX, int mouseY, int mouseButton) {
		for (GuiNpcMiniWindow mwin : this.mwindows.values()) {
			mwin.mouseEvent(mouseX, mouseY, mouseButton);
		}
	}

	public abstract void save();

	public void setBackground(String texture) {
		this.background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/" + texture);
	}

	public void setHoverText(String text) {
		List<String> list = new ArrayList<>();
		if (!text.contains("%")) {
			text = new TextComponentTranslation(text).getFormattedText();
		}
		if (text.contains("~~~")) {
			while (text.contains("~~~")) {
				text = text.replace("~~~", "%");
			}
		}
		while (text.contains("<br>")) {
			list.add(text.substring(0, text.indexOf("<br>")));
			text = text.substring(text.indexOf("<br>") + 4);
		}
		list.add(text);
		this.hoverText = list.toArray(new String[0]);
	}

	public void setSubGui(SubGuiInterface gui) {
		(this.subgui = gui).setWorldAndResolution(this.mc, this.width, this.height);
		(this.subgui.parent = this).initGui();
	}

	public void setWorldAndResolution(@Nonnull Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		this.initPacket();
	}

	public void updateScreen() {
		try {
			for (GuiNpcTextField tf : new ArrayList<>(this.textfields.values())) {
				if (tf.enabled) {
					tf.updateCursorCounter();
				}
			}
		} catch (Exception e) { LogWriter.error("Error:", e); }
		super.updateScreen();
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
		this.line.add(new int[] { sX, sY, eX, eY, color, size });
	}
	
	@Override
	public void closeMiniWindow(GuiNpcMiniWindow miniWindow) {
		this.mwindows.remove(miniWindow.id);
	}
	
	@Override
	public void setMiniHoverText(int id, IComponentGui component) {}
	
}
