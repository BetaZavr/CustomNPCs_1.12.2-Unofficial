package noppes.npcs.client.gui.custom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import noppes.npcs.client.gui.util.*;
import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiComponentWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiEntityWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiLabelWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiScrollWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTextFieldWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTexturedRectWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.client.gui.custom.components.CustomGuiButton;
import noppes.npcs.client.gui.custom.components.CustomGuiEntity;
import noppes.npcs.client.gui.custom.components.CustomGuiLabel;
import noppes.npcs.client.gui.custom.components.CustomGuiScrollComponent;
import noppes.npcs.client.gui.custom.components.CustomGuiTextField;
import noppes.npcs.client.gui.custom.components.CustomGuiTexturedRect;
import noppes.npcs.client.gui.custom.interfaces.IClickListener;
import noppes.npcs.client.gui.custom.interfaces.ICustomKeyListener;
import noppes.npcs.client.gui.custom.interfaces.IDataHolder;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerCustomGui;

import javax.annotation.Nonnull;

public class GuiCustom extends GuiContainer implements ICustomScrollListener, IGuiData {

	public static int guiLeft;
	public static int guiTop;
	protected Map<Integer, IGuiComponent> components;
	protected List<IClickListener> clickListeners;
	protected List<ICustomKeyListener> keyListeners;
	protected List<IDataHolder> dataHolders;
	protected ResourceLocation background;
	protected CustomGuiWrapper gui;
	protected int xSize;
	protected int ySize;
	protected int stretched;
	protected int bgW;
	protected int bgH;
	protected int bgTx;
	protected int bgTy;
	public String[] hoverText;
	public ItemStack hoverStack;
	public int mouseWheel;

	public GuiCustom(ContainerCustomGui container) {
		super(container);
		components = new HashMap<>();
		clickListeners = new ArrayList<>();
		keyListeners = new ArrayList<>();
		dataHolders = new ArrayList<>();
		stretched = 0;
		bgW = 0;
		bgH = 0;
		bgTx = 256;
		bgTy = 256;
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton button) throws IOException {
		super.actionPerformed(button);
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CustomGuiButton, updateGui().toNBT(), button.id);
	}

	public void addClickListener(IClickListener component) { clickListeners.add(component); }

	private void addComponent(ICustomGuiComponent component) {
		CustomGuiComponentWrapper c = (CustomGuiComponentWrapper) component;
		switch (c.getType()) {
			case 0: {
				CustomGuiButton button = CustomGuiButton.fromComponent((CustomGuiButtonWrapper) component);
				button.setParent(this);
				components.put(button.getId(), button);
				addClickListener(button);
				break;
			}
			case 1: {
				CustomGuiLabel lbl = CustomGuiLabel.fromComponent((CustomGuiLabelWrapper) component);
				lbl.setParent(this);
				components.put(lbl.getId(), lbl);
				break;
			}
			case 3: {
				CustomGuiTextField textField = CustomGuiTextField.fromComponent((CustomGuiTextFieldWrapper) component);
				textField.setParent(this);
				components.put(textField.id, textField);
				addDataHolder(textField);
				addClickListener(textField);
				addKeyListener(textField);
				break;
			}
			case 2: {
				CustomGuiTexturedRect rect = CustomGuiTexturedRect.fromComponent((CustomGuiTexturedRectWrapper) component);
				rect.setParent(this);
				components.put(rect.getId(), rect);
				break;
			}
			case 4: {
				CustomGuiScrollComponent scroll = new CustomGuiScrollComponent(mc, this, component.getId(), (CustomGuiScrollWrapper) component);
				scroll.fromComponent((CustomGuiScrollWrapper) component);
				scroll.setParent(this);
				components.put(scroll.getId(), scroll);
				addDataHolder(scroll);
				addClickListener(scroll);
				break;
			}
			case 7: {
				CustomGuiEntity entt = CustomGuiEntity.fromComponent((CustomGuiEntityWrapper) component);
				entt.setParent(this);
				components.put(entt.getId(), entt);
				break;
			}
		}
	}

	public void addDataHolder(IDataHolder component) { dataHolders.add(component);}

	public void addKeyListener(ICustomKeyListener component) { keyListeners.add(component);}

	public void buttonClick(CustomGuiButton button) { NoppesUtilPlayer.sendData(EnumPlayerPacket.CustomGuiButton, updateGui().toNBT(), button.id);}

	@Override
	public boolean doesGuiPauseGame() {
		return gui == null || gui.getDoesPauseGame();
	}

	void drawBackgroundTexture() {
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(background);
		GlStateManager.translate((float) guiLeft, (float) guiTop, 0.0f);
		if (bgW > 0 && bgH > 0) {
			if (stretched == 0) {
				float scaleU = (float) xSize / (float) bgW;
				float scaleV = (float) ySize / (float) bgH;
				GlStateManager.scale(scaleU, scaleV, 1.0f);
				drawTexturedModalRect(0, 0, bgTx, bgTy, bgW, bgH);
			}
			else {
				int hS = ySize, h = 0;
				int stepW = stretched == 2 ? xSize / (int) Math.ceil((double) xSize / (double) bgW) : bgW;
				int stepH = stretched == 2 ? ySize / (int) Math.ceil((double) ySize / (double) bgH) : bgH;
				if (stretched == 2) {
					if (stepW >= xSize) { stepW = xSize / 2; }
					if (stepH >= ySize) { stepH = ySize / 2; }
				}
				while (hS > 0) {
					int height = Math.min(hS, stepH);
					int startV = h * stepH;
					int textureV = bgTy;
					if (stretched == 2) {
						if (hS <= stepH) { // last
							if (h == 0) { // and first
								height = ySize / 2;
								hS = height + stepH;
							} else {
								startV = ySize - height;
								textureV += bgH - hS;
								height = stepH;
							}
						} else {
							if (h != 0 && stepH != bgW) { textureV += (bgH - stepH) / 2; }
						}
					}
					int wS = xSize, w = 0;
					while (wS > 0) {
						int width = Math.min(wS, stepW);
						int startU = w * stepW;
						int textureU = bgTx;
						if (stretched == 2) {
							if (wS <= stepW) { // last
								if (w == 0) { // and first
									width = xSize / 2;
									wS = width + stepW;
								} else {
									textureU += bgW - wS;
									width = stepW;
								}
							} else {
								if (w != 0 && stepW != bgW) { textureU += (bgW - stepW) / 2; }
							}
						}
						drawTexturedModalRect(startU, startV, textureU, textureV, width, height);
						wS -= stepW;
						w++;
					}
					hS -= stepH;
					h++;
				}
			}
		}
		else { drawTexturedModalRect(0, 0, 0, 0, xSize, ySize); }
		GlStateManager.popMatrix();
		if (gui.getShowPlayerSlots() && inventorySlots != null) {
			mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			for (int slotId = inventorySlots.inventorySlots.size() - 1, i = 0; i < 36; slotId--, i++) {
				Slot slot = inventorySlots.getSlot(slotId);
				drawTexturedModalRect(getGuiLeft() + slot.xPos - 1, getGuiTop() + slot.yPos - 1, 0, 0, 18, 18);
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) { }

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		mouseWheel = Mouse.getDWheel();
		hoverText = null;
		hoverStack = null;
		drawDefaultBackground();
		if (background != null) { drawBackgroundTexture(); }
		for (IGuiComponent component : components.values()) { component.onRender(mc, mouseX, mouseY, mouseWheel, partialTicks); }
		if (gui != null && gui.getSlots().length > 0) {
			int cx = -41 + (256 - gui.getWidth()) / 2;
			int cy = -46 + (256 - gui.getHeight()) / 2;
			GlStateManager.pushMatrix();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			for (IItemSlot slot : gui.getSlots()) {
				if (!slot.isShowBack()) { continue; }
				drawTexturedModalRect(getGuiLeft() + slot.getPosX() + cx, getGuiTop() + slot.getPosY() + cy, 0, 0, 18, 18);
			}
			GlStateManager.popMatrix();
		}
		if (hoverStack != null) {
			drawHoveringText(hoverStack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL), mouseX, mouseY);
		}
		else if (hoverText != null) {
			drawHoveringText(Arrays.asList(hoverText), mouseX, mouseY);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
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

	@Override
	public void initGui() {
		super.initGui();
		if (gui != null) {
			guiLeft = (width - xSize) / 2;
			guiTop = (height - ySize) / 2;
			components.clear();
			clickListeners.clear();
			keyListeners.clear();
			dataHolders.clear();
			for (ICustomGuiComponent c : gui.getComponents()) { addComponent(c); }
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CustomGuiKeyPressed, keyCode);
		for (ICustomKeyListener listener : keyListeners) { listener.keyTyped(typedChar, keyCode); }
		if (keyCode == 1) {
			if (gui != null) {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.CustomGuiClose, updateGui().toNBT());
				return;
			}
		}
		if (mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode)) { return; }
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		for (IClickListener listener : clickListeners) {
			listener.mouseClicked(this, mouseX, mouseY, mouseButton);
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CustomGuiScrollClick, updateGui().toNBT(), scroll.getID(), scroll.getSelect(), getScrollSelection((CustomGuiScrollComponent) scroll), false);
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CustomGuiScrollClick, updateGui().toNBT(), scroll.getID(), scroll.getSelect(), getScrollSelection((CustomGuiScrollComponent) scroll), true);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		Minecraft mc = Minecraft.getMinecraft();
		CustomGuiWrapper guiWrapper = (CustomGuiWrapper) new CustomGuiWrapper(mc.player).fromNBT(compound);
		((ContainerCustomGui) inventorySlots).setGui(guiWrapper, mc.player);
		gui = guiWrapper;
		xSize = guiWrapper.getWidth();
		ySize = guiWrapper.getHeight();
		if (!guiWrapper.getBackgroundTexture().isEmpty()) {
			background = new ResourceLocation(guiWrapper.getBackgroundTexture());
			stretched = guiWrapper.stretched;
			bgW = guiWrapper.bgW;
			bgH = guiWrapper.bgH;
			bgTx = guiWrapper.bgTx;
			bgTy = guiWrapper.bgTy;
		}
		else {
			stretched = 0;
			bgW = 0;
			bgH = 0;
			bgTx = 256;
			bgTy = 256;
		}
		initGui();
	}

	CustomGuiWrapper updateGui() {
		for (IDataHolder component : dataHolders) { gui.updateComponent(component.toComponent()); }
		return gui;
	}

	@Override
	public void updateScreen() {
		super.updateScreen();
		for (IDataHolder component : dataHolders) {
			if (component instanceof GuiTextField) { ((GuiTextField) component).updateCursorCounter(); }
		}
	}

}
