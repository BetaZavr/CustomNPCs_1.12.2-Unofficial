package noppes.npcs.client.gui.player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.SubGuiEditBankAccess;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiMenuLeftButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNPCBank;
import noppes.npcs.controllers.data.Bank.CeilSettings;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCBankChest extends GuiContainerNPCInterface {

	private static final ResourceLocation backTexture = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
	private static final ResourceLocation tabsTexture = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	private static final ResourceLocation rowTexture = new ResourceLocation("textures/gui/container/creative_inventory/tab_items.png");
	public ContainerNPCBank cont;

	public int row;
	private final int maxRows;
    private int yPos;
	private int ceilPos;
    private final int step;
	private final boolean isMany;
    private boolean hoverScroll;

    private boolean isScrolling, update, isWait;
	private ItemStack stack;

	public GuiNPCBankChest(EntityNPCInterface npc, ContainerNPCBank container) {
		super(npc, container);
        this.mc = Minecraft.getMinecraft();
		this.cont = container;
		this.isMany = container.items.getSizeInventory() > 45;
		this.allowUserInput = false;
		this.row = 0;
		this.ceilPos = -1;
		this.maxRows = (int) Math.ceil((double) container.items.getSizeInventory() / 9.0d) - 5;
		this.ySize = 114 + this.cont.height;
		this.step = this.maxRows > 0 ? (int) (73.0f / (float) this.maxRows) : 0;
		this.isScrolling = false;
		this.resetSlots();
		this.closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (this.isWait) {
			return;
		}
		if (button.id > 2 && button.id < 8) {
			this.close();
			NoppesUtilPlayer.sendData(EnumPlayerPacket.OpenCeilBank, this.cont.bank.id,
					((GuiMenuLeftButton) button).data);
			return;
		}
		switch (button.id) {
		case 0: {
			NoppesUtilPlayer.sendData(this.update ? EnumPlayerPacket.BankUpgrade : EnumPlayerPacket.BankUnlock,
					this.npc.getEntityId());
			this.isWait = true;
			break;
		}
		case 1: { // up
			if (this.ceilPos <= 0) {
				return;
			}
			this.ceilPos--;
			this.initGui();
			break;
		}
		case 2: { // down
			if (this.ceilPos >= Math.floor((double) this.cont.bank.ceilSettings.size() / 5.0d)) {
				return;
			}
			this.ceilPos++;
			this.initGui();
			break;
		}
		case 9: { // settings
			if (this.cont.bank == null) {
				return;
			}
			this.setSubGui(new SubGuiEditBankAccess(0, this.cont.bank));
			break;
		}
		case 10: { // clear stacks
			NoppesUtilPlayer.sendData(EnumPlayerPacket.BankClearCeil, this.npc.getEntityId());
			this.isWait = true;
			break;
		}
		case 11: { // lock
			NoppesUtilPlayer.sendData(EnumPlayerPacket.BankLock, this.npc.getEntityId());
			this.isWait = true;
			break;
		}
		case 12: { // regrade
			NoppesUtilPlayer.sendData(EnumPlayerPacket.BankRegrade, this.npc.getEntityId());
			this.isWait = true;
			break;
		}
		case 13: { // reset
			NoppesUtilPlayer.sendData(EnumPlayerPacket.BankResetCeil, this.npc.getEntityId());
			this.isWait = true;
			break;
		}
		}
	}

	@Override
	public void closeSubGui(SubGuiInterface gui) {
		super.closeSubGui(gui);
		if (gui instanceof SubGuiEditBankAccess) {
			SubGuiEditBankAccess subGui = (SubGuiEditBankAccess) gui;
			boolean isChanged = false;
			if (!this.cont.bank.owner.equals(subGui.owner)) {
				this.cont.bank.owner = subGui.owner;
				isChanged = true;
			}
			if (subGui.names.size() != this.cont.bank.access.size()) {
				this.cont.bank.access.clear();
				this.cont.bank.access.addAll(subGui.names);
				isChanged = true;
			} else {
				for (String name : subGui.names) {
					if (this.cont.bank.access.contains(name)) {
						continue;
					}
					this.cont.bank.access.clear();
					this.cont.bank.access.addAll(subGui.names);
					isChanged = true;
					break;
				}
			}
			if (isChanged) {
				NBTTagCompound compound = new NBTTagCompound();
				this.cont.bank.writeToNBT(compound);
				this.isWait = true;
				Client.sendData(EnumPacketServer.BankSave, compound);
				NoppesUtilPlayer.sendData(EnumPlayerPacket.OpenCeilBank, this.cont.bank.id, this.cont.ceil);
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		this.mc.renderEngine.bindTexture(backTexture);
		int u = (this.width - this.xSize) / 2 - 8;
		int v = (this.height - this.ySize) / 2;
		int h = this.cont.height + 107;
		// Main
		this.drawTexturedModalRect(u, v, 0, 0, 176, h - 4);
		this.drawTexturedModalRect(u, v + h - 4, 0, 218, 176, 4);
		int o = this.isMany ? 36 : 20;
		this.drawTexturedModalRect(u + 172, v, 176 - o, 0, o, h - 4);
		this.drawTexturedModalRect(u + 172, v + h - 4, 176 - o, 218, o, 4);
		// Rows
		int i;
		for (i = 0; i < Math.ceil(this.cont.items.getSizeInventory() / 9.0d) && i < 5; i++) {
			this.fontRenderer.drawString("" + (1 + i + this.row),
					u + 13 - this.fontRenderer.getStringWidth("" + (1 + i + this.row)), v + 4 + (i + 1) * 18,
					CustomNpcs.LableColor.getRGB());
			this.drawHorizontalLine(u + 5, u + 180, v + 16 + (i + 1) * 18, CustomNpcs.LableColor.getRGB());
			if (i == 0) {
				this.drawHorizontalLine(u + 5, u + 180, v - 1 + (i + 1) * 18, CustomNpcs.LableColor.getRGB());
			}
		}
		this.drawVerticalLine(u + 15, v + 14, v + 1 + (i + 1) * 18, CustomNpcs.LableColor.getRGB());
		this.drawVerticalLine(u + 176, v + 14, v + 1 + (i + 1) * 18, CustomNpcs.LableColor.getRGB());
		// Slots
		this.mc.renderEngine.bindTexture(GuiNPCInterface.RESOURCE_SLOT);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		for (int s = 0; s < this.cont.inventorySlots.size(); s++) {
			Slot slot = this.cont.getSlot(s);
			if (slot.xPos > 0 && slot.yPos > 0) {
				this.drawTexturedModalRect(u + 8 + slot.xPos - 1, v + slot.yPos - 1, 0, 0, 18, 18);
			}
		}
		if (!this.stack.isEmpty()) {
			Slot slot = this.cont.getSlot(this.cont.items.getSizeInventory());
			this.drawTexturedModalRect(u + slot.xPos + 53, v + slot.yPos - 23, 0, 0, 18, 18);
			ITextComponent t = new TextComponentTranslation(this.update ? "bank.upg.cost" : "bank.tab.cost");
			this.fontRenderer.drawString(t.getFormattedText() + ":", u + slot.xPos, v + slot.yPos - 19,
					CustomNpcs.LableColor.getRGB());
		} else {
			ITextComponent text = new TextComponentTranslation("bank.slots.info", "" + this.cont.items.getCountEmpty(),
					"" + this.cont.items.getSizeInventory());
			if (this.player.capabilities.isCreativeMode) {
				text.appendSibling(new TextComponentString(
						" / (" + this.cont.bank.ceilSettings.get(this.cont.ceil).maxCeils + ")"));
			}
			this.fontRenderer.drawString(text.getFormattedText(), u + 8, v + 8 + (i + 1) * 18,
					CustomNpcs.LableColor.getRGB());
		}
		if (this.isMany) {
			this.mc.renderEngine.bindTexture(rowTexture);
			this.drawTexturedModalRect(u + 184, v + 17, 174, 17, 14, 86);
			this.drawTexturedModalRect(u + 184, v + 103, 174, 125, 14, 4);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		ITextComponent b = new TextComponentTranslation("gui.bank", ": ");
		ITextComponent n = new TextComponentTranslation(this.cont.bank.name);
		n.getStyle().setBold(true);
		this.fontRenderer.drawString(b.appendSibling(n).appendSibling(new TextComponentString("; "))
				.appendSibling(new TextComponentTranslation("gui.ceil", " #" + (this.cont.ceil + 1)))
				.getFormattedText(), 8, 6, CustomNpcs.LableColor.getRGB());
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.hoverScroll = false;
		if (this.isMany && this.subgui == null) {
			GlStateManager.pushMatrix();
			GlStateManager.color(2.0F, 2.0F, 2.0F, 1.0F);
			this.mc.renderEngine.bindTexture(tabsTexture);
            float currentScroll = (float) this.row / (float) this.maxRows;
			int h = (int) (currentScroll * 73.0f);
			int u = (this.width - this.xSize) / 2 + 177;
			int v = (this.height - this.ySize) / 2 + 18 + h;
			this.hoverScroll = mouseX >= u && mouseX <= u + 12 && mouseY >= v && mouseY <= v + 15;
			this.drawTexturedModalRect(u, v, (this.hoverScroll ? 244 : 232), 0, 12, 15);
			GlStateManager.popMatrix();
			if (this.hoverScroll) {
				int dWheel = Mouse.getDWheel();
				if (dWheel > 0) {
					this.resetRow(false);
				} else if (dWheel < 0) {
					this.resetRow(true);
				}
			}
		}
		if (!this.stack.isEmpty()) {
			int u = (this.width - this.xSize) / 2;
			int v = (this.height - this.ySize) / 2;
			Slot slot = this.cont.getSlot(this.cont.items.getSizeInventory());

			GlStateManager.pushMatrix();
			GlStateManager.translate(u + slot.xPos + 46, v + slot.yPos - 22, 50.0f);
			RenderHelper.enableGUIStandardItemLighting();
			this.mc.getRenderItem().renderItemAndEffectIntoGUI(this.stack, 0, 0);
			GlStateManager.translate(0.0f, 0.0f, 200.0f);
			this.drawString(this.mc.fontRenderer, "" + this.stack.getCount(),
					16 - this.mc.fontRenderer.getStringWidth("" + this.stack.getCount()), 9, 0xFFFFFFFF);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
			if (this.isMouseHover(mouseX, mouseY, u + slot.xPos + 54, v + slot.yPos - 22, 18, 18)) {
				List<String> list = new ArrayList<>();
				CeilSettings cs = this.cont.bank.ceilSettings.get(this.cont.ceil);
				String t = new TextComponentTranslation("bank." + (this.update ? "upg" : "tab") + ".cost.info",
						"" + cs.startCeils, "" + cs.maxCeils).getFormattedText();
				if (!t.contains("<br>")) {
					list.add(t);
				} else {
                    Collections.addAll(list, t.split("<br>"));
				}
				list.addAll(this.stack.getTooltip(this.mc.player,
						this.mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL));
				this.hoverText = list.toArray(new String[0]);
			}
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		}
		if (this.getButton(0) != null) {
			this.getButton(0).enabled = this.player.capabilities.isCreativeMode
					|| (this.cont.dataCeil == this.cont.bank.ceilSettings.size());
		}
		if (this.getButton(10) != null) {
			this.getButton(10).enabled = this.cont.items.getSizeInventory() > 0 && !this.cont.items.isEmpty();
		}
		if (this.getButton(13) != null) {
			this.getButton(13).enabled = this.cont.items.getSizeInventory() > 0
					&& (!this.cont.items.isEmpty() || this.cont.items
							.getSizeInventory() != this.cont.bank.ceilSettings.get(this.cont.ceil).startCeils);
		}
		if (CustomNpcs.ShowDescriptions && this.subgui == null) {
			if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
				ITextComponent it = new TextComponentTranslation("bank.hover.update." + this.update,
						"" + this.cont.items.getSizeInventory(),
						"" + this.cont.bank.ceilSettings.get(this.cont.ceil).maxCeils);
				if (this.cont.dataCeil < 0) {
					it.appendSibling(new TextComponentTranslation("bank.hover.update.not.0"));
				} else if (!this.update && this.cont.dataCeil < this.cont.bank.ceilSettings.size()) {
					it.appendSibling(new TextComponentTranslation("bank.hover.update.not.1", "" + (this.cont.dataCeil + 1)));
				}
				this.setHoverText(it.getFormattedText());
			} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("bank.hover.up").getFormattedText());
			} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("bank.hover.down").getFormattedText());
			} else if (this.getButton(9) != null && this.getButton(9).visible && this.getButton(9).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("bank.hover.settings").getFormattedText());
			} else if (this.getButton(10) != null && this.getButton(10).visible && this.getButton(10).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("bank.hover.clear.slots").getFormattedText());
			} else if (this.getButton(11) != null && this.getButton(11).visible && this.getButton(11).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("bank.hover.lock").getFormattedText());
			} else if (this.getButton(12) != null && this.getButton(12).visible && this.getButton(12).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("bank.hover.regrade").getFormattedText());
			} else if (this.getButton(13) != null && this.getButton(13).visible && this.getButton(13).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("bank.hover.reset").getFormattedText());
			} else {
				for (int i = 3; i < 8; i++) {
					if (this.getButton(i) != null && this.getButton(i).isMouseOver()) {
						this.setHoverText(new TextComponentTranslation(
								"bank.hover.ceil." + ((GuiMenuLeftButton) this.getButton(i)).active,
								"" + ((GuiMenuLeftButton) this.getButton(i)).data).getFormattedText());
					}
				}
			}
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, fontRenderer);
			this.hoverText = null;
		} else {
			this.renderHoveredToolTip(mouseX, mouseY);
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		if (this.isScrolling) {
			int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
			if (mouseY - this.yPos >= this.step) {
				this.resetRow(true);
				this.yPos = mouseY;
			} else if (((mouseY - this.yPos) * -1) >= this.step) {
				this.resetRow(false);
				this.yPos = mouseY;
			}
		}
		super.handleMouseInput();
	}

	@Override
	public void initGui() {
		super.initGui();
		this.resetSlots();
		this.stack = ItemStack.EMPTY;
		CeilSettings cs = this.cont.bank.ceilSettings.get(this.cont.ceil);
		boolean isOwner = !this.cont.bank.isPublic || this.player.capabilities.isCreativeMode || this.player.getName().equals(this.cont.bank.owner);
		this.update = this.cont.items.getSizeInventory() > 0;
		if (this.cont.items.getSizeInventory() == 0 && !cs.openStack.isEmpty()) {
			this.stack = cs.openStack;
			this.update = false;
		} else if (this.cont.items.getSizeInventory() < cs.maxCeils && !cs.upgradeStack.isEmpty()) {
			this.stack = cs.upgradeStack;
			this.update = true;
		}
		Slot slot = this.cont.getSlot(this.cont.items.getSizeInventory());
		int u = (this.width - this.xSize) / 2 - 8;
		int v = (this.height - this.ySize) / 2;
		if (!this.stack.isEmpty() || this.player.capabilities.isCreativeMode) {
			int x = u + slot.xPos + 80 + (this.stack.isEmpty() ? 104 + (this.isMany ? 8 : 0) : 0);
			int y = this.stack.isEmpty() ? this.guiTop + 36 : v + slot.yPos - 23;
			GuiNpcButton button = new GuiNpcButton(0, x, y, 60, 18, this.update ? "bank.upgrade" : "bank.unlock");
			button.enabled = this.player.capabilities.isCreativeMode
					? (!this.update || this.cont.items.getSizeInventory() < cs.maxCeils)
					: this.cont.dataCeil == this.cont.bank.ceilSettings.size();
			this.addButton(button);
			if (this.player.capabilities.isCreativeMode) {
				x = u + slot.xPos + 184 + (this.isMany ? 8 : 0);
				y = this.guiTop + 14;
				button = new GuiNpcButton(11, x, y, 60, 18, "bank.lock");
				button.enabled = this.cont.items.getSizeInventory() > 0 && !cs.openStack.isEmpty();
				this.addButton(button);
				button = new GuiNpcButton(12, x, y += this.stack.isEmpty() ? 44 : 22, 60, 18, "bank.regrade");
				button.enabled = this.cont.items.getSizeInventory() > 0;
				this.addButton(button);
				button = new GuiNpcButton(13, x, y + 22, 60, 18, "quest.reset");
				button.enabled = this.cont.items.getSizeInventory() > 0 && (!this.cont.items.isEmpty() || this.cont.items.getSizeInventory() != cs.startCeils);
				this.addButton(button);

			}
		}
		GuiMenuLeftButton tab;
		if (this.ceilPos < 0) {
			this.ceilPos = (int) (Math.floor((double) this.cont.ceil / 5.0d));
		}
		if (this.cont.bank.ceilSettings.size() > 1) {
			if (this.cont.bank.ceilSettings.size() > 5) {
				if (this.ceilPos > 0) {
					tab = new GuiMenuLeftButton(1, this.guiLeft - 8, this.guiTop + 4, "" + ((char) 708));
					tab.height = 12;
					tab.offsetYtext = 1;
					this.addButton(tab);
				}
				if (this.ceilPos < Math.floor((double) this.cont.bank.ceilSettings.size() / 5.0d)) {
					tab = new GuiMenuLeftButton(2, this.guiLeft - 8, this.guiTop + 84, "" + ((char) 709));
					tab.height = 12;
					tab.offsetYtext = 2;
					this.addButton(tab);
				}
			}
			for (int i = 0; i < 5 && (i + this.ceilPos * 5) < this.cont.bank.ceilSettings.size(); i++) {
				tab = new GuiMenuLeftButton(3 + i, this.guiLeft - 8, this.guiTop + 20 + i * 12,
						"" + (1 + i + this.ceilPos * 5));
				tab.data = i + this.ceilPos * 5;
				tab.height = 12;
				if (i + this.ceilPos * 5 == this.cont.ceil) {
					tab.active = true;
				}
				this.addButton(tab);
			}
		}
		if (this.cont.bank.isPublic) {
			if (!this.cont.bank.owner.isEmpty() || this.player.capabilities.isCreativeMode) {
				this.addButton(new GuiNpcButton(9, (this.isMany ? 12 : -4) + (this.width + this.xSize) / 2,
						this.guiTop - 8, 20, 20, 20, 146, GuiNPCInterface.MENU_SIDE_BUTTON));
				this.getButton(9).visible = isOwner;
			}
		}
		this.addButton(new GuiNpcButton(10, u + slot.xPos + (this.isMany ? 166 : 159), v + slot.yPos - 24, 20, 20, 20, 66, new ResourceLocation(CustomNpcs.MODID, "textures/gui/menusidebutton.png")));
		this.getButton(10).visible = isOwner;
		if (this.row > this.maxRows) {
			this.row = this.maxRows;
		}
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if (keyCode == 200 || keyCode == ClientProxy.frontButton.getKeyCode()) {
			this.resetRow(false);
		}
		if (keyCode == 208 || keyCode == ClientProxy.backButton.getKeyCode()) {
			this.resetRow(true);
		}
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (this.hoverScroll) {
			this.yPos = mouseY;
			this.isScrolling = true;
		} else if (this.isMany) {
			int u = 173 + (this.width - this.xSize) / 2;
			int v = 18 + (this.height - this.ySize) / 2;
			if (mouseX >= u && mouseX <= u + 11 && mouseY >= v && mouseY <= v + 88) {
				int h = mouseY - v, r;
				if (h <= 7) {
					r = 0;
				} else if (h >= 81) {
					r = this.maxRows;
				} else {
					r = (int) ((double) this.maxRows * (double) h / 88.0d);
				}
				int old = this.row;
				if (r < 0) {
					r = 0;
				}
				if (r > this.maxRows) {
					r = this.maxRows;
				}
				if (old != r) {
					this.row = r;
					this.resetSlots();
				}

			}
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		if (this.isScrolling) {
			this.isScrolling = false;
		}
		super.mouseReleased(mouseX, mouseY, state);
	}

	private void resetRow(boolean bo) {
		if (!this.isMany) {
			return;
		}
		int old = this.row;
		if (bo) {
			this.row++;
		} else {
			this.row--;
		}
		if (this.row < 0) {
			this.row = 0;
		}
		if (this.row > this.maxRows) {
			this.row = this.maxRows;
		}
		if (old != this.row) {
			this.resetSlots();
		}
	}

	private void resetSlots() {
		if (!this.isMany) {
			return;
		}
		int m = this.row * 9, n = m + 45, i = -1;
		int t = this.cont.items.getSizeInventory(), u = 0, e = t;
		if (t % 9 != 0) {
			e -= t % 9;
		}
		for (int s = 0; s < t; s++) {
			Slot slot = this.cont.getSlot(s);
            if (s < m || s >= n) {
				slot.xPos = -5000;
				slot.yPos = -5000;
				continue;
			}
			i++;
			if (s >= e) {
				u = (int) (((9.0d - ((double) t % 9.0d)) / 2.0d) * 18.0d);
			}
			slot.xPos = 8 + u + (i % 9) * 18;
			slot.yPos = 18 + (i / 9) * 18;
		}
	}

	@Override
	public void save() {
	}

}
