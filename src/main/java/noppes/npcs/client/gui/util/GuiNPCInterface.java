package noppes.npcs.client.gui.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
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
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.GuiBoundarySetting;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public abstract class GuiNPCInterface
extends GuiScreen
implements IEditNPC {

	public static final ResourceLocation RESOURCE_SLOT = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");

	public static final ResourceLocation MENU_BUTTON = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubutton.png");
	public static final ResourceLocation MENU_SIDE_BUTTON = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menusidebutton.png");
	public static final ResourceLocation MENU_TOP_BUTTON = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menutopbutton.png");


	public boolean closeOnEsc = false;
	public boolean hoverMiniWin = false;
	public boolean drawDefaultBackground = false;
	public int guiLeft, guiTop, mouseX, mouseY, xSize, ySize, widthTexture, heightTexture;
	public float bgScale = 1.0f;
	public String title = "";
	public String[] hoverText;

	public ResourceLocation background = null;
	public EntityNPCInterface npc;
	public EntityPlayerSP player;
	private GuiButton selectedButton;
	public SubGuiInterface subgui;

	private final List<int[]> line = Lists.newArrayList(); // startX, startY, endX, endY, color, lineSize
	private final List<IGui> components = Lists.newArrayList();
	public final Map<Integer, GuiNpcButton> buttons = new ConcurrentHashMap<>();
	public final Map<Integer, GuiScreen> extra = new ConcurrentHashMap<>();
	public final Map<Integer, GuiNpcLabel> labels = new ConcurrentHashMap<>();
	public final Map<Integer, GuiCustomScroll> scrolls = new ConcurrentHashMap<>();
	public final Map<Integer, GuiMenuSideButton> sidebuttons = new ConcurrentHashMap<>();
	public final Map<Integer, GuiNpcSlider> sliders = new ConcurrentHashMap<>();
	public final Map<Integer, GuiNpcTextField> textfields = new ConcurrentHashMap<>();
	public final Map<Integer, GuiMenuTopButton> topbuttons = new ConcurrentHashMap<>();
	public final Map<Integer, GuiMenuLeftButton> leftbuttons = new ConcurrentHashMap<>();
	public final Map<Integer, GuiNpcMiniWindow> mwindows = new ConcurrentHashMap<>();

	public GuiNPCInterface() {
		this(null);
	}

	public GuiNPCInterface(EntityNPCInterface npc) {
		this.xSize = 200;
		this.ySize = 222;
		this.npc = npc;
		this.mc = Minecraft.getMinecraft();
		this.player = this.mc.player;
		this.itemRender = this.mc.getRenderItem();
		this.fontRenderer = this.mc.fontRenderer;
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton guibutton) {
		if (!(guibutton instanceof GuiNpcButton)) {
			return;
		}
		for (GuiNpcMiniWindow mwin : this.mwindows.values()) {
			mwin.buttonEvent((GuiNpcButton) guibutton);
		}
		if (this.hoverMiniWin) { return; }
		if (this.subgui != null) {
			this.subgui.buttonEvent((GuiNpcButton) guibutton);
		} else {
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

	public void addExtra(GuiHoverText gui) {
		gui.setWorldAndResolution(this.mc, 350, 250);
		this.extra.put(gui.id, gui);
	}

	public void addLabel(GuiNpcLabel label) {
		this.labels.put(label.id, label);
	}

	public void addLeftButton(GuiMenuLeftButton button) {
		this.leftbuttons.put(button.id, button);
		this.buttonList.add(button);
	}

	public void addScroll(GuiCustomScroll scroll) {
		scroll.setWorldAndResolution(this.mc, scroll.width, scroll.height);
		scroll.setParent(this);
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

	public void addMiniWindow(GuiNpcMiniWindow miniwindows) {
		this.mwindows.put(miniwindows.id, miniwindows);
		miniwindows.resetButtons();
	}
	
	public void buttonEvent(GuiNpcButton button) {
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
		float f6 = mouseFocus == 0 || mouseFocus == 2 ? 0 : this.guiLeft + x - this.mouseX;
		float f7 = mouseFocus == 0 || mouseFocus == 3 ? 0 : this.guiTop + y - 50.0f * scale * zoomed - this.mouseY;
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
		this.mc.getRenderManager().playerViewY = 180.0f;
		if (mouseFocus != 0 && vertical != 0) {
			GlStateManager.translate(0.0f, 1.0f - Math.cos((double) vertical * 3.14d / 180.0d), 0.0f);
			GlStateManager.rotate(vertical, 1.0f, 0.0f, 0.0f);
		}
		this.mc.getRenderManager().renderEntity(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
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
			if (this.xSize > 252 && this.ySize <= 256) {
				this.drawTexturedModalRect(0, 0, 0, 0, 252, this.ySize);
				int w = this.xSize - 252;
				this.drawTexturedModalRect(252, 0, 256 - w, 0, w, this.ySize);
			} else {
				if (this.widthTexture !=0 && this.heightTexture != 0) {
					if (this.xSize < this.widthTexture) {
						if (this.ySize < this.heightTexture) {
							this.drawTexturedModalRect(0, 0, 0, 0, this.xSize - 4, this.ySize - 4);
							this.drawTexturedModalRect(this.xSize - 4, 0, this.widthTexture - 4, 0, 4, this.ySize - 4);
							this.drawTexturedModalRect(0, this.ySize - 4, 0, this.heightTexture - 4, this.xSize - 4, 4);
							this.drawTexturedModalRect(this.xSize - 4, this.ySize - 4, this.widthTexture - 4, this.heightTexture - 4, 4, 4);
						} else {
							this.drawTexturedModalRect(0, 0, 0, 0, this.xSize, this.ySize);
						}
					}
				}
				else { this.drawTexturedModalRect(0, 0, 0, 0, this.xSize, this.ySize); }
			}
			GlStateManager.popMatrix();
		}
		this.postDrawBackground();
		GlStateManager.translate(0, 0, 1.0f);
		if (!this.line.isEmpty()) {
			for (int[] ln : this.line) {
				if (ln == null || ln.length < 6) { continue; }
				GuiBoundarySetting.drawLine(ln[0], ln[1], ln[2], ln[3], ln[4], ln[5]);
			}
			this.line.clear();
		}
		this.drawCenteredString(this.fontRenderer, this.title, this.width / 2, this.height + 10, CustomNpcs.MainColor.getRGB());
		boolean hasArea = false;
		for (GuiNpcLabel label : new ArrayList<>(this.labels.values())) {
			label.drawLabel(this, this.fontRenderer, mouseX, mouseY);
		}
		for (GuiNpcTextField tf : new ArrayList<>(this.textfields.values())) {
			tf.drawTextBox(x, y);
			if (tf instanceof GuiNpcTextArea) {
				hasArea = true;
			}
		}
		for (IGui comp : new ArrayList<>(this.components)) {
			comp.drawScreen(x, y);
			if (comp instanceof GuiNpcTextArea) {
				hasArea = true;
			}
		}
		for (GuiCustomScroll scroll : new ArrayList<>(this.scrolls.values())) {
			scroll.drawScreen(x, y,
					(!this.hasSubGui() && (scroll.hovered || (this.scrolls.isEmpty() && !hasArea)))
							? Mouse.getDWheel()
							: 0);
		}
		for (GuiScreen gui : new ArrayList<>(this.extra.values())) {
			gui.drawScreen(x, y, partialTicks);
		}
		super.drawScreen(x, y, partialTicks);
		if (this.subgui != null) {
			this.subgui.drawScreen(mouseX, mouseY, partialTicks);
		}
		else {
			this.hoverMiniWin = false;
			for (GuiNpcMiniWindow mwin : this.mwindows.values()) {
				mwin.drawScreen(mouseX, mouseY, partialTicks);
				if (mwin.hovered) { this.hoverMiniWin = true; }
			}
			if (this.hoverMiniWin) { return; }
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	public void postDrawBackground() { }

	public void elementClicked() {
		if (this.subgui != null) {
			this.subgui.elementClicked();
		}
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

	public GuiMenuLeftButton getLeftButton(int i) {
		return this.leftbuttons.get(i);
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
		if (this.subgui != null) {
			this.subgui.setWorldAndResolution(this.mc, this.width, this.height);
			this.subgui.initGui();
		}
		this.guiLeft = (this.width - this.xSize) / 2;
		this.guiTop = (this.height - this.ySize) / 2;
		this.components.clear();
		this.buttonList.clear();
		this.buttons.clear();
		this.topbuttons.clear();
		this.leftbuttons.clear();
		this.sidebuttons.clear();
		this.textfields.clear();
		this.labels.clear();
		this.scrolls.clear();
		this.sliders.clear();
		this.extra.clear();
		this.mwindows.clear();
	}
	
	public List<GuiButton> getButtonList() { return this.buttonList; }

	public void initPacket() {
	}

	public boolean isInventoryKey(int i) {
		return i == this.mc.gameSettings.keyBindInventory.getKeyCode();
	}

	public boolean isMouseHover(int mX, int mY, int px, int py, int pwidth, int pheight) {
		return mX >= px && mY >= py && mX < (px + pwidth) && mY < (py + pheight);
	}

	public void keyTyped(char c, int i) {
		if (this.subgui != null) {
			subgui.keyTyped(c, i);
			return;
		}
		for (GuiNpcMiniWindow mwin : this.mwindows.values()) {
			mwin.keyTyped(c, i);
		}
		if (this.hoverMiniWin) { return; }
		boolean active = false;
		for (IGui gui : this.components) {
			if (gui.isActive()) {
				active = true;
				break;
			}
		}
		active = (active || GuiNpcTextField.isActive());
		if (this.subgui == null) {
			boolean helpButtons = false;
			if (i == 56 || i == 184) {
				helpButtons = Keyboard.isKeyDown(35);
			} // Alts
			else if (i == 35) {
				helpButtons = Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184);
			}
			if (helpButtons) {
				CustomNpcs.ShowDescriptions = !CustomNpcs.ShowDescriptions;
			}
		}
		if (closeOnEsc && (i == 1 || (!active && this.isInventoryKey(i)))) {
			close();
			return;
		}
		for (GuiNpcTextField tf : new ArrayList<>(this.textfields.values())) {
			tf.textboxKeyTyped(c, i);
		}
		for (IGui comp : new ArrayList<>(this.components)) {
			if (comp instanceof IKeyListener) {
				((IKeyListener) comp).keyTyped(c, i);
			}
		}
		if (this.hasSubGui()) {
			return;
		}
		for (GuiCustomScroll scroll : new ArrayList<>(this.scrolls.values())) {
			scroll.keyTyped(c, i);
		}
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
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
		for (IGui comp : new ArrayList<>(this.components)) {
			if (comp instanceof IMouseListener) {
				((IMouseListener) comp).mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
		this.mouseEvent(mouseX, mouseY, mouseButton);
		if (mouseButton != 0) {
			return;
		}
		for (GuiCustomScroll scroll : new ArrayList<>(this.scrolls.values())) {
			scroll.mouseClicked(mouseX, mouseY, mouseButton);
		}
		for (GuiButton guibutton : this.buttonList) {
			if (guibutton.mousePressed(this.mc, this.mouseX, this.mouseY)) {
				GuiScreenEvent.ActionPerformedEvent.Pre event = new GuiScreenEvent.ActionPerformedEvent.Pre(this, guibutton, this.buttonList);
				if (MinecraftForge.EVENT_BUS.post(event)) {
					break;
				}
				guibutton = event.getButton();
				(this.selectedButton = guibutton).playPressSound(this.mc.getSoundHandler());
				this.actionPerformed(guibutton);
				if (this.equals(this.mc.currentScreen)) {
					MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Post(this, event.getButton(), this.buttonList));
					break;
				}
				break;
			}
		}
	}

	public void mouseEvent(int mouseX, int mouseY, int mouseButton) {
		for (GuiNpcMiniWindow mwin : this.mwindows.values()) {
			mwin.mouseEvent(mouseX, mouseY, mouseButton);
		}
	}

	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		for (GuiNpcMiniWindow mwin : this.mwindows.values()) {
			mwin.mouseReleased(mouseX, mouseY, mouseButton);
		}
		if (this.hoverMiniWin) { return; }
		if (this.selectedButton != null && mouseButton == 0) {
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
			Object object = oclass.getMethod("getDesktop", new Class[0]).invoke(null);
			oclass.getMethod("browse", URI.class).invoke(object, new URI(link));
		} catch (Exception e) { LogWriter.error("Error:", e); }
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
		this.subgui = gui;
		this.subgui.npc = this.npc;
		this.subgui.setWorldAndResolution(this.mc, this.width, this.height);
		(this.subgui.parent = this).initGui();
	}

	public void setWorldAndResolution(@Nonnull Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		this.initPacket();
	}

	public void updateScreen() {
		if (this.subgui != null) {
			this.subgui.updateScreen();
		} else {
			for (GuiNpcTextField tf : new ArrayList<>(this.textfields.values())) {
				if (tf.enabled) {
					tf.updateCursorCounter();
				}
			}
			for (IGui comp : new ArrayList<>(this.components)) {
				comp.updateScreen();
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
	public void mouseReleased(GuiNpcSlider slider) { }

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
