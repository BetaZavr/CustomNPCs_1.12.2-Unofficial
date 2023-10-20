package noppes.npcs.client.gui.custom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiComponentWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiLabelWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiScrollWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTextFieldWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTexturedRectWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.custom.components.CustomGuiButton;
import noppes.npcs.client.gui.custom.components.CustomGuiLabel;
import noppes.npcs.client.gui.custom.components.CustomGuiScrollComponent;
import noppes.npcs.client.gui.custom.components.CustomGuiTextField;
import noppes.npcs.client.gui.custom.components.CustomGuiTexturedRect;
import noppes.npcs.client.gui.custom.interfaces.IClickListener;
import noppes.npcs.client.gui.custom.interfaces.ICustomKeyListener;
import noppes.npcs.client.gui.custom.interfaces.IDataHolder;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerCustomGui;

public class GuiCustom
extends GuiContainer
implements ICustomScrollListener, IGuiData {
	
	ResourceLocation background;
	Map<Integer, IGuiComponent> components;
	CustomGuiWrapper gui;
	
	List<IClickListener> clickListeners;
	List<ICustomKeyListener> keyListeners;
	List<IDataHolder> dataHolders;
	
	protected int xSize, ySize;
	private int stretched, bgW, bgH, bgTx, bgTy;

	public static int guiLeft, guiTop;
	public String[] hoverText;
	public ResourceLocation slot = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");

	public GuiCustom(ContainerCustomGui container) {
		super((Container) container);
		this.components = new HashMap<Integer, IGuiComponent>();
		this.clickListeners = new ArrayList<IClickListener>();
		this.keyListeners = new ArrayList<ICustomKeyListener>();
		this.dataHolders = new ArrayList<IDataHolder>();
		this.stretched = 0;
		this.bgW = 0;
		this.bgH = 0;
		this.bgTx = 256;
		this.bgTy = 256;
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CustomGuiButton, this.updateGui().toNBT(), button.id);
	}

	public void addClickListener(IClickListener component) {
		this.clickListeners.add(component);
	}

	private void addComponent(ICustomGuiComponent component) {
		CustomGuiComponentWrapper c = (CustomGuiComponentWrapper) component;
		switch (c.getType()) {
			case 0: {
				CustomGuiButton button = CustomGuiButton.fromComponent((CustomGuiButtonWrapper) component);
				button.setParent(this);
				this.components.put(button.getId(), button);
				this.addClickListener(button);
				break;
			}
			case 1: {
				CustomGuiLabel lbl = CustomGuiLabel.fromComponent((CustomGuiLabelWrapper) component);
				lbl.setParent(this);
				this.components.put(lbl.getId(), lbl);
				break;
			}
			case 3: {
				CustomGuiTextField textField = CustomGuiTextField.fromComponent((CustomGuiTextFieldWrapper) component);
				textField.setParent(this);
				this.components.put(textField.id, textField);
				this.addDataHolder(textField);
				this.addClickListener(textField);
				this.addKeyListener(textField);
				break;
			}
			case 2: {
				CustomGuiTexturedRect rect = CustomGuiTexturedRect.fromComponent((CustomGuiTexturedRectWrapper) component);
				rect.setParent(this);
				this.components.put(rect.getId(), rect);
				break;
			}
			case 4: {
				CustomGuiScrollComponent scroll = new CustomGuiScrollComponent(this.mc, (GuiScreen) this, component.getId(),
						(CustomGuiScrollWrapper) component);
				scroll.fromComponent((CustomGuiScrollWrapper) component);
				scroll.setParent(this);
				this.components.put(scroll.getId(), scroll);
				this.addDataHolder(scroll);
				this.addClickListener(scroll);
				break;
			}
		}
	}

	public void addDataHolder(IDataHolder component) {
		this.dataHolders.add(component);
	}

	public void addKeyListener(ICustomKeyListener component) {
		this.keyListeners.add(component);
	}

	public void buttonClick(CustomGuiButton button) {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CustomGuiButton, this.updateGui().toNBT(), button.id);
	}

	public boolean doesGuiPauseGame() {
		return this.gui == null || this.gui.getDoesPauseGame();
	}

	void drawBackgroundTexture() { // Changed
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.getTextureManager().bindTexture(this.background);
		GlStateManager.translate((float) GuiCustom.guiLeft, (float) GuiCustom.guiTop, 0.0f);
		if (this.bgW>0 && this.bgH>0) {
			if (this.stretched==0) {
				float scaleU = (float) this.xSize / (float) this.bgW;
				float scaleV = (float) this.ySize / (float) this.bgH;
				GlStateManager.scale(scaleU, scaleV, 1.0f);
				this.drawTexturedModalRect(0, 0, this.bgTx, this.bgTy, this.bgW, this.bgH);
			} else {
				int hS = this.ySize, h=0;
				int stepW = this.stretched==2 ? this.xSize / (int) Math.ceil((double) this.xSize / (double) this.bgW) : this.bgW;
				int stepH = this.stretched==2 ? this.ySize / (int) Math.ceil((double) this.ySize / (double) this.bgH) : this.bgH;
				if (this.stretched==2) {
					if (stepW>=this.xSize) { stepW = this.xSize/2; }
					if (stepH>=this.ySize) { stepH = this.ySize/2; }
				}
				while (hS>0) {
					int height = hS < stepH ? hS : stepH;
					int startV = h * stepH;
					int textureV = this.bgTy;
					if (this.stretched==2) {
						if (hS <= stepH) { // last
							if (h==0) { // and first
								height = this.ySize / 2;
								hS = height + stepH;
							} else {
								startV = this.ySize - height;
								textureV += this.bgH-hS;
								height = stepH;
							}
						} else {
							if (h!=0 && stepH!=this.bgW) { textureV += (this.bgH-stepH)/2;}
						}
					}
					int wS = this.xSize, w=0;
					while (wS>0) {
						int width = wS < stepW ? wS : stepW;
						int startU = w * stepW;
						int textureU = this.bgTx;
						if (this.stretched==2) {
							if (wS <= stepW) { // last
								if (w==0) { // and first
									width = this.xSize / 2;
									wS = width + stepW;
								} else {
									textureU += this.bgW-wS;
									width = stepW;
								}
							} else {
								if (w!=0 && stepW!=this.bgW) { textureU += (this.bgW-stepW)/2;}
							}
						}
						this.drawTexturedModalRect(startU, startV, textureU, textureV, width, height);
						wS -= stepW;
						w++;
					}
					hS -= stepH;
					h++;
				}
			}
		} else {
			this.drawTexturedModalRect(0, 0, 0, 0, this.xSize, this.ySize);
		}
		GlStateManager.popMatrix();
	}

	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.hoverText = null;
		this.drawDefaultBackground();
		if (this.background != null) {
			this.drawBackgroundTexture();
		}
		for (IGuiComponent component : this.components.values()) {
			component.onRender(this.mc, mouseX, mouseY, Mouse.getDWheel(), partialTicks);
		}
		if (this.gui != null && this.gui.getSlots().length > 0) { // New
			int cx = -41 + (256 - this.gui.getWidth()) / 2;
			int cy = -46 + (256 - this.gui.getHeight()) / 2;
			
			GlStateManager.pushMatrix();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.mc.getTextureManager().bindTexture(this.slot);
			for (IItemSlot slot : this.gui.getSlots()) {
				if (!slot.isShowBack()) { continue; }
				this.drawTexturedModalRect(this.getGuiLeft() + slot.getPosX() + cx, this.getGuiTop() + slot.getPosY() + cy, 0, 0, 18, 18);
			}
			GlStateManager.popMatrix();
		}

		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY);
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	NBTTagCompound getScrollSelection(CustomGuiScrollComponent scroll) {
		NBTTagList list = new NBTTagList();
		if (scroll.component.isMultiSelect()) {
			for (String s : scroll.getSelectedList()) {
				list.appendTag(new NBTTagString(s));
			}
		} else {
			list.appendTag(new NBTTagString(scroll.getSelected()));
		}
		NBTTagCompound selection = new NBTTagCompound();
		selection.setTag("selection", list);
		return selection;
	}

	public void initGui() {
		super.initGui();
		if (this.gui != null) {
			GuiCustom.guiLeft = (this.width - this.xSize) / 2;
			GuiCustom.guiTop = (this.height - this.ySize) / 2;
			this.components.clear();
			this.clickListeners.clear();
			this.keyListeners.clear();
			this.dataHolders.clear();
			for (ICustomGuiComponent c : this.gui.getComponents()) {
				this.addComponent(c);
			}
		}
	}

	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		Client.sendDataDelayCheck(EnumPlayerPacket.CustomGuiKeyPressed, this, 0, keyCode);
		for (ICustomKeyListener listener : this.keyListeners) {
			listener.keyTyped(typedChar, keyCode);
		}
		if (keyCode == 1) { // Changed
			if (this.gui != null) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.CustomGuiClose, this.updateGui().toNBT());
				return;
			}
		}
		if (this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) {
			return;
		}
		super.keyTyped(typedChar, keyCode);
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		for (IClickListener listener : this.clickListeners) {
			listener.mouseClicked(this, mouseX, mouseY, mouseButton);
		}
	}

	public void onGuiClosed() {
		super.onGuiClosed();
	}

	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CustomGuiScrollClick, this.updateGui().toNBT(), scroll.id,
				scroll.selected, this.getScrollSelection((CustomGuiScrollComponent) scroll), false);
	}

	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CustomGuiScrollClick, this.updateGui().toNBT(), scroll.id,
				scroll.selected, this.getScrollSelection((CustomGuiScrollComponent) scroll), true);
	}

	public void setGuiData(NBTTagCompound compound) {

		Minecraft mc = Minecraft.getMinecraft();
		CustomGuiWrapper gui = (CustomGuiWrapper) new CustomGuiWrapper(mc.player).fromNBT(compound);
		((ContainerCustomGui) this.inventorySlots).setGui(gui, (EntityPlayer) mc.player);
		this.gui = gui;
		
		this.xSize = gui.getWidth();
		this.ySize = gui.getHeight();
		if (!gui.getBackgroundTexture().isEmpty()) {
			this.background = new ResourceLocation(gui.getBackgroundTexture());
			this.stretched = gui.stretched;
			this.bgW = gui.bgW;
			this.bgH = gui.bgH;
			this.bgTx = gui.bgTx;
			this.bgTy = gui.bgTy;
		} else {
			this.stretched = 0;
			this.bgW = 0;
			this.bgH = 0;
			this.bgTx = 256;
			this.bgTy = 256;
		}
		this.initGui();
	}

	CustomGuiWrapper updateGui() {
		for (IDataHolder component : this.dataHolders) {
			this.gui.updateComponent(component.toComponent());
		}
		return this.gui;
	}

	public void updateScreen() {
		super.updateScreen();
		for (IDataHolder component : this.dataHolders) {
			if (component instanceof GuiTextField) {
				((GuiTextField) component).updateCursorCounter();
			}
		}
	}
}
