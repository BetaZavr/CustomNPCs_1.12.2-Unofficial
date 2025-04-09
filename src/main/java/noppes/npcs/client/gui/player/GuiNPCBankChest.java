package noppes.npcs.client.gui.player;

import java.io.IOException;
import java.util.*;

import noppes.npcs.client.ClientGuiEventHandler;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.util.Util;
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
import noppes.npcs.client.gui.SubGuiEditBankAccess;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNPCBank;
import noppes.npcs.controllers.data.Bank.CeilSettings;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCBankChest
extends GuiContainerNPCInterface
implements ISubGuiListener {

	private static final ResourceLocation backTexture = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
	private static final ResourceLocation tabsTexture = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	private static final ResourceLocation rowTexture = new ResourceLocation("textures/gui/container/creative_inventory/tab_items.png");
	public ContainerNPCBank cont;

	public int row = 0;
	private final int maxRows;
    private int yPos;
	private int ceilPos = -1;
	private int allCost = 0;
    private final int step;
	private final boolean isMany;
    private boolean hoverScroll;
    private boolean isScrolling = false;
	private boolean upgrade;
	private boolean isWait;
	private boolean isOwner;
	private ItemStack stack;
	private int money;

	public GuiNPCBankChest(EntityNPCInterface npc, ContainerNPCBank container) {
		super(npc, container);
        mc = Minecraft.getMinecraft();
		cont = container;
		isMany = container.items.getSizeInventory() > 45;
		allowUserInput = false;
		maxRows = (int) Math.ceil((double) container.items.getSizeInventory() / 9.0d);
		ySize = 114 + cont.height;
		step = maxRows > 5 ? (int) (73.0f / ((float) maxRows - 5.0f)) : 0;
		closeOnEsc = true;
		resetSlots();
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (isWait) { return; }
		if (button.getID() > 2 && button.getID() < 8) {
			close();
			NoppesUtilPlayer.sendData(EnumPlayerPacket.OpenCeilBank, cont.bank.id, ((GuiMenuSideButton) button).data);
			return;
		}
		switch (button.getID()) {
			case 0: {
				NoppesUtilPlayer.sendData((upgrade ? EnumPlayerPacket.BankUpgrade : EnumPlayerPacket.BankUnlock), npc.getEntityId(), true, 1);
				isWait = true;
				break;
			}
			case 1: { // up
				if (ceilPos <= 0) {
					return;
				}
				ceilPos--;
				initGui();
				break;
			}
			case 2: { // down
				if (ceilPos >= Math.floor((double) cont.bank.ceilSettings.size() / 5.0d)) {
					return;
				}
				ceilPos++;
				initGui();
				break;
			}
			case 9: { // settings
				if (cont.bank == null) {
					return;
				}
				setSubGui(new SubGuiEditBankAccess(0, cont.bank));
				break;
			}
			case 10: { // clear stacks
				NoppesUtilPlayer.sendData(EnumPlayerPacket.BankClearCeil, npc.getEntityId());
				isWait = true;
				break;
			}
			case 11: { // lock
				NoppesUtilPlayer.sendData(EnumPlayerPacket.BankLock, npc.getEntityId());
				isWait = true;
				break;
			}
			case 12: { // regrade
				NoppesUtilPlayer.sendData(EnumPlayerPacket.BankRegrade, npc.getEntityId());
				isWait = true;
				break;
			}
			case 13: { // reset
				NoppesUtilPlayer.sendData(EnumPlayerPacket.BankResetCeil, npc.getEntityId());
				isWait = true;
				break;
			}
			case 14: {
				if (allCost <= 0) { return; }
				NoppesUtilPlayer.sendData(EnumPlayerPacket.BankUpgrade, npc.getEntityId(), true, allCost);
				isWait = true;
				break;
			}
		}
	}

	@Override
	public void subGuiClosed(ISubGuiInterface gui) {
		if (gui instanceof SubGuiEditBankAccess) {
			SubGuiEditBankAccess subGui = (SubGuiEditBankAccess) gui;
			boolean isChanged = false;
			if (cont.bank.isChanging != subGui.isChanging) {
				cont.bank.isChanging = subGui.isChanging;
				isChanged = true;
			}
			if (!cont.bank.owner.equals(subGui.owner)) {
				cont.bank.owner = subGui.owner;
				isChanged = true;
			}
			if (subGui.names.size() != cont.bank.access.size()) {
				cont.bank.access.clear();
				cont.bank.access.addAll(subGui.names);
				isChanged = true;
			} else {
				for (String name : subGui.names) {
					if (cont.bank.access.contains(name)) {
						continue;
					}
					cont.bank.access.clear();
					cont.bank.access.addAll(subGui.names);
					isChanged = true;
					break;
				}
			}
			if (isChanged) {
				NBTTagCompound compound = new NBTTagCompound();
				cont.bank.writeToNBT(compound);
				isWait = true;
				Client.sendData(EnumPacketServer.BankSave, compound);
				NoppesUtilPlayer.sendData(EnumPlayerPacket.OpenCeilBank, cont.bank.id, cont.ceil);
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(backTexture);
		int u = (width - xSize) / 2 - 8;
		int v = (height - ySize) / 2;
		int h = cont.height + 107;
		// background
		drawTexturedModalRect(u, v, 0, 0, 176, h - 4);
		drawTexturedModalRect(u, v + h - 4, 0, 218, 176, 4);
		int o = isMany ? 36 : 20;
		drawTexturedModalRect(u + 172, v, 176 - o, 0, o, h - 4);
		drawTexturedModalRect(u + 172, v + h - 4, 176 - o, 218, o, 4);
		// Slots
		mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
		for (int s = 0; s < cont.inventorySlots.size(); s++) {
			Slot slot = cont.getSlot(s);
			if (slot.xPos > 0 && slot.yPos > 0) {
				drawTexturedModalRect(u + 8 + slot.xPos - 1, v + slot.yPos - 1, 0, 0, 18, 18);
			}
		}
		// Rows
		int i;
		for (i = 0; i < Math.ceil(cont.items.getSizeInventory() / 9.0d) && i < 5; i++) {
			fontRenderer.drawString("" + (1 + i + row), u + 13 - fontRenderer.getStringWidth("" + (1 + i + row)), v + 4 + (i + 1) * 18, CustomNpcs.LableColor.getRGB());
			drawHorizontalLine(u + 4, u + 181, v + 17 + (i + 1) * 18, CustomNpcs.LableColor.getRGB());
			if (i == 0) {
				drawHorizontalLine(u + 4, u + 181, v - 1 + (i + 1) * 18, CustomNpcs.LableColor.getRGB());
			}
		}
		drawVerticalLine(u + 15, v + 14, v + 1 + (i + 1) * 18, CustomNpcs.LableColor.getRGB());
		drawVerticalLine(u + 177, v + 14, v + 1 + (i + 1) * 18, CustomNpcs.LableColor.getRGB());
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		// upgrade / new tab
		CeilSettings cs = cont.bank.ceilSettings.get(cont.ceil);
		if (isOwner && (cs.maxCells > cont.items.getSizeInventory() || cont.items.getSizeInventory() == 0)) {
			Slot slot = cont.getSlot(cont.items.getSizeInventory());
			if (!stack.isEmpty()) {
				mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
				drawTexturedModalRect(u + slot.xPos + 53, v + slot.yPos - 23, 0, 0, 18, 18);
				ITextComponent t = new TextComponentTranslation(upgrade ? "bank.upg.cost" : "bank.tab.cost");
				fontRenderer.drawString(t.getFormattedText() + ":", u + slot.xPos, v + slot.yPos - (money <= 0 ? 18 : 24), CustomNpcs.LableColor.getRGB());
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			}
			if (money > 0) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(u + slot.xPos - 5, v + slot.yPos - 17, 0.0f);
				mc.getTextureManager().bindTexture(ClientGuiEventHandler.COIN_NPC);
				float s = 16.0f / 250.f;
				GlStateManager.scale(s, s, s);
				GlStateManager.enableBlend();
				GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
				this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
				GlStateManager.popMatrix();

				String text = Util.instance.getTextReducedNumber(money, true, true, false) + CustomNpcs.displayCurrencies;
				fontRenderer.drawString(text, u + slot.xPos + 11, v + slot.yPos - 12, CustomNpcs.LableColor.getRGB(), false);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			}
		}
		else {
			ITextComponent text;
			if (cont.items.getSizeInventory() == 0) { text = new TextComponentTranslation("bank.slots.empty"); }
			else {
				text = new TextComponentTranslation("bank.slots.info", "" + cont.items.getCountEmpty(), "" + cont.items.getSizeInventory());
				if (player.capabilities.isCreativeMode) { text.appendSibling(new TextComponentString(((char)167) + "3 (GM total in ceil: " + cont.bank.ceilSettings.get(cont.ceil).maxCells + ")")); }
			}
			fontRenderer.drawString(text.getFormattedText(), u + 8, v + 8 + (i + 1) * 18, CustomNpcs.LableColor.getRGB());
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}
		// Scroll
		if (isMany) {
			mc.getTextureManager().bindTexture(rowTexture);
			drawTexturedModalRect(u + 184, v + 17, 174, 17, 14, 86);
			drawTexturedModalRect(u + 184, v + 103, 174, 125, 14, 4);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		ITextComponent b = new TextComponentTranslation("gui.bank", ": ");
		ITextComponent n = new TextComponentTranslation(cont.bank.name);
		n.getStyle().setBold(true);
		b.appendSibling(n).appendSibling(new TextComponentString("; "));
		b.appendSibling(new TextComponentTranslation("gui.ceil", " #" + ((char)167) + "l" +(cont.ceil + 1)));
		fontRenderer.drawString(b.getFormattedText(), 8, 6, CustomNpcs.LableColor.getRGB());
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (isWait) {
			drawWait();
			return;
		}
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		CeilSettings cs = cont.bank.ceilSettings.get(cont.ceil);
		hoverScroll = false;
		if (isMany && subgui == null) {
			GlStateManager.pushMatrix();
			GlStateManager.color(2.0F, 2.0F, 2.0F, 1.0F);
			mc.getTextureManager().bindTexture(tabsTexture);
            float currentScroll = maxRows > 5 ? (float) row / ((float) maxRows - 5.0f) : 0;
			int h = (int) (currentScroll * 73.0f);
			int u = (width - xSize) / 2 + 177;
			int v = (height - ySize) / 2 + 18 + h;
			hoverScroll = mouseX >= u && mouseX <= u + 12 && mouseY >= v && mouseY <= v + 15;
			drawTexturedModalRect(u, v, (hoverScroll ? 244 : 232), 0, 12, 15);
			GlStateManager.popMatrix();
			int dWheel = Mouse.getDWheel();
			if (dWheel != 0) {
				resetRow(dWheel < 0);
			}
		}
		PlayerData data = CustomNpcs.proxy.getPlayerData(player);
		Slot slot = cont.getSlot(cont.items.getSizeInventory());
		int u = (width - xSize) / 2;
		int v = (height - ySize) / 2;
		if (!stack.isEmpty() && isOwner) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(u + slot.xPos + 46, v + slot.yPos - 22, 50.0f);
			RenderHelper.enableGUIStandardItemLighting();
			mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
			GlStateManager.translate(0.0f, 0.0f, 200.0f);
			drawString(mc.fontRenderer, "" + stack.getCount(),
					16 - mc.fontRenderer.getStringWidth("" + stack.getCount()), 9, 0xFFFFFFFF);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
			if (isMouseHover(mouseX, mouseY, u + slot.xPos + 46, v + slot.yPos - 22, 18, 18)) {
				List<String> list = new ArrayList<>();
				String t = new TextComponentTranslation("bank." + (upgrade ? "upg" : "tab") + ".cost.info", "" + cs.startCells, "" + cs.maxCells).getFormattedText();
				if (!t.contains("<br>")) {
					list.add(t);
				} else {
                    Collections.addAll(list, t.split("<br>"));
				}
				list.addAll(stack.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL));
				setHoverText(list);
			}
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		}
		if (money > 0 && isOwner) {
			if (isMouseHover(mouseX, mouseY, u + slot.xPos - 11, v + slot.yPos - 14, 50, 12)) {
				String hover = new TextComponentTranslation("market.hover.currency.0",
						Util.instance.getTextReducedNumber(money, true, true, false),
						CustomNpcs.displayCurrencies,
						Util.instance.getTextReducedNumber(data.game.getMoney(), true, true, false) + CustomNpcs.displayCurrencies).getFormattedText();
				setHoverText(Arrays.asList(hover.split("<br>")));
			}
		}
		allCost = 0;
		int max = 0;
		boolean canPayStack = false, canPayMoney = false;
		if (getButton(0) != null && getButton(14) != null) {
			max = cs.maxCells - cont.items.getSizeInventory();
			allCost = Math.max(max, 0);
			if (player.capabilities.isCreativeMode) {
				canPayStack = true;
				canPayMoney = true;
			} else {
				canPayStack = stack.isEmpty();
				if (!canPayStack) {
					int allSt = Util.instance.inventoryItemCount(player, stack, null, false, false);
					allCost = Math.min(allCost, allSt / stack.getCount());
					canPayStack = allSt >= stack.getCount();
				}
				canPayMoney = money <= 0;
				if (!canPayMoney) {
					allCost = Math.min(allCost, (int) data.game.getMoney() / money);
					canPayMoney = data.game.getMoney() >= money;
				}
			}
			getButton(0).setEnabled(player.capabilities.isCreativeMode || (max > 0 && canPayStack && canPayMoney));

			getButton(0).setIsVisible(isOwner);
			getButton(14).setEnabled(allCost != 0 && allCost <= max);
			getButton(14).setIsVisible(isOwner && upgrade && max > 0);
		}
		if (getButton(10) != null) {
			getButton(10).setEnabled(cont.items.getSizeInventory() > 0 && !cont.items.isEmpty());
		}
		if (getButton(13) != null) {
			getButton(13).setEnabled(cont.items.getSizeInventory() > 0 && (!cont.items.isEmpty() || cont.items.getSizeInventory() != cont.bank.ceilSettings.get(cont.ceil).startCells));
		}
		if (CustomNpcs.ShowDescriptions && subgui == null) {
			if (getButton(0) != null && getButton(0).isHovered()) {
				ITextComponent it = new TextComponentTranslation("bank.hover.update." + upgrade, ((char)167) + "61", "" + cont.items.getSizeInventory(), "" + cont.bank.ceilSettings.get(cont.ceil).maxCells);
				if (!upgrade && cont.dataCeil < cont.bank.ceilSettings.size()) {
					it.appendSibling(new TextComponentTranslation("bank.hover.update.not.1", "" + (cont.dataCeil + 1)));
				}
				if (!canPayStack) {
					it.appendSibling(new TextComponentTranslation("bank.hover.update.not.0"));
				}
				if (!canPayMoney) {
					it.appendSibling(new TextComponentTranslation("bank.hover.update.not.1"));
				}
				setHoverText(it.getFormattedText());
			} else if (getButton(1) != null && getButton(1).isHovered()) {
				setHoverText(new TextComponentTranslation("bank.hover.up").getFormattedText());
			} else if (getButton(2) != null && getButton(2).isHovered()) {
				setHoverText(new TextComponentTranslation("bank.hover.down").getFormattedText());
			} else if (getButton(9) != null && getButton(9).isVisible() && getButton(9).isHovered()) {
				setHoverText(new TextComponentTranslation("bank.hover.settings").getFormattedText());
			} else if (getButton(10) != null && getButton(10).isVisible() && getButton(10).isHovered()) {
				setHoverText(new TextComponentTranslation("bank.hover.clear.slots").getFormattedText());
			} else if (getButton(11) != null && getButton(11).isVisible() && getButton(11).isHovered()) {
				setHoverText(new TextComponentTranslation("bank.hover.lock").getFormattedText());
			} else if (getButton(12) != null && getButton(12).isVisible() && getButton(12).isHovered()) {
				setHoverText(new TextComponentTranslation("bank.hover.regrade").getFormattedText());
			} else if (getButton(13) != null && getButton(13).isVisible() && getButton(13).isHovered()) {
				setHoverText(new TextComponentTranslation("bank.hover.reset").getFormattedText());
			} else if (getButton(14) != null && getButton(14).isVisible() && getButton(14).isHovered()) {
				ITextComponent it = new TextComponentTranslation("bank.hover.update.true", ((char)167) + "6" + allCost + ((char)167) + "7/" + max, "" + cont.items.getSizeInventory(), "" + cont.bank.ceilSettings.get(cont.ceil).maxCells);
				if (!upgrade && cont.dataCeil < cont.bank.ceilSettings.size()) {
					it.appendSibling(new TextComponentTranslation("bank.hover.update.not.1", "" + (cont.dataCeil + 1)));
				}
				if (!canPayStack) {
					it.appendSibling(new TextComponentTranslation("bank.hover.update.not.0"));
				}
				if (!canPayMoney) {
					it.appendSibling(new TextComponentTranslation("bank.hover.update.not.1"));
				}
				setHoverText(it.getFormattedText());
			} else {
				// left tabs
				for (int i = 3; i < 8; i++) {
					if (getButton(i) != null && getButton(i).isHovered()) {
						setHoverText(new TextComponentTranslation("bank.hover.ceil." + ((GuiMenuSideButton) getButton(i)).active, "" + ((GuiMenuSideButton) getButton(i)).data).getFormattedText());
					}
				}
			}
		}
		if (hasHoverText()) { drawHoverText(null); }
		else { renderHoveredToolTip(mouseX, mouseY); }
	}

	@Override
	public void handleMouseInput() throws IOException {
		if (isScrolling) {
			int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
			if (mouseY - yPos >= step) {
				resetRow(true);
				yPos = mouseY;
			} else if (((mouseY - yPos) * -1) >= step) {
				resetRow(false);
				yPos = mouseY;
			}
		}
		super.handleMouseInput();
	}

	@Override
	public void initGui() {
		super.initGui();
		resetSlots();
		stack = ItemStack.EMPTY;
		money = 0;
		CeilSettings cs = cont.bank.ceilSettings.get(cont.ceil);
		isOwner = !cont.bank.isPublic || player.capabilities.isCreativeMode || player.getName().equals(cont.bank.owner);
		upgrade = cont.items.getSizeInventory() > 0;
		if (cont.items.getSizeInventory() == 0 && !cs.openStack.isEmpty()) {
			stack = cs.openStack;
			money = cs.openMoney;
			upgrade = false;
		}
		else if (cont.items.getSizeInventory() < cs.maxCells && !cs.upgradeStack.isEmpty()) {
			stack = cs.upgradeStack;
			money = cs.upgradeMoney;
			upgrade = true;
		}
		Slot slot = cont.getSlot(cont.items.getSizeInventory());
		int u = (width - xSize) / 2 - 8;
		int v = (height - ySize) / 2;
		if (!stack.isEmpty() || player.capabilities.isCreativeMode) {
			int x = u + slot.xPos + 80 + (stack.isEmpty() ? 104 + (isMany ? 8 : 0) : 0);
			int y = stack.isEmpty() ? guiTop + 36 : v + slot.yPos - 23; // upgrade button up + right or center
			GuiNpcButton button = new GuiNpcButton(0, x, y, 50, 18, upgrade ? "bank.upgrade" : "bank.unlock");
			button.enabled = player.capabilities.isCreativeMode ? (!upgrade || cont.items.getSizeInventory() < cs.maxCells) : cont.dataCeil == cont.bank.ceilSettings.size();
			addButton(button);

			button = new GuiNpcButton(14, x + 52, y, 25, 18, "gui.max");
			button.enabled = getButton(0).isEnabled();
			button.visible = cs.maxCells - cont.items.getSizeInventory() > 0;
			addButton(button);

			if (player.capabilities.isCreativeMode) {
				x = u + slot.xPos + 184 + (isMany ? 8 : 0);
				y = guiTop + 14;
				button = new GuiNpcButton(11, x, y, 50, 18, "bank.lock");
				button.enabled = cont.items.getSizeInventory() > 0 && !cs.openStack.isEmpty();
				addButton(button);
				button = new GuiNpcButton(12, x, y += stack.isEmpty() ? 44 : 22, 50, 18, "bank.regrade");
				button.enabled = cont.items.getSizeInventory() > 0;
				addButton(button);
				button = new GuiNpcButton(13, x, y + 22, 50, 18, "gui.reset");
				button.enabled = cont.items.getSizeInventory() > 0 && (!cont.items.isEmpty() || cont.items.getSizeInventory() != cs.startCells);
				addButton(button);
			}
		}
		GuiMenuSideButton tab;
		if (ceilPos < 0) {
			ceilPos = (int) (Math.floor((double) cont.ceil / 5.0d));
		}
		if (cont.bank.ceilSettings.size() > 1) {
			if (cont.bank.ceilSettings.size() > 5) {
				if (ceilPos > 0) {
					tab = new GuiMenuSideButton(1, guiLeft - 8, guiTop + 4, "" + ((char) 708));
					tab.height = 12;
					tab.offsetText = 1;
					addButton(tab);
				}
				if (ceilPos < Math.floor((double) cont.bank.ceilSettings.size() / 5.0d)) {
					tab = new GuiMenuSideButton(2, guiLeft - 8, guiTop + 84, "" + ((char) 709));
					tab.height = 12;
					tab.offsetText = 2;
					addButton(tab);
				}
			}
			for (int i = 0; i < 5 && (i + ceilPos * 5) < cont.bank.ceilSettings.size(); i++) {
				tab = new GuiMenuSideButton(3 + i, guiLeft - 8, guiTop + 20 + i * 12, "" + (1 + i + ceilPos * 5));
				tab.data = i + ceilPos * 5;
				tab.height = 12;
				if (i + ceilPos * 5 == cont.ceil) {
					tab.active = true;
				}
				addButton(tab);
			}
		}
		if (cont.bank.isPublic) {
			if (!cont.bank.owner.isEmpty() || player.capabilities.isCreativeMode) {
				this.addButton(new GuiNpcButton(9, (isMany ? 12 : -4) + (width + xSize) / 2, guiTop - 8, 20, 20, 20, 146, GuiNPCInterface.WIDGETS));
				getButton(9).setIsVisible(isOwner);
			}
		}
		GuiNpcButton button = new GuiNpcButton(10, u + slot.xPos + (isMany ? 166 : 159), v + slot.yPos - 24, 20, 20, "");
		button.texture = GuiNPCInterface.MENU_BUTTON;
		button.hasDefBack = false;
		button.txrX = 236;
		button.txrW = 20;
		button.txrH = 20;
		button.visible = isOwner;
		addButton(button);
		if (row > maxRows) {
			row = maxRows;
		}
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) {
		if (keyCode == 200 || keyCode == mc.gameSettings.keyBindForward.getKeyCode()) {
			resetRow(false);
		}
		if (keyCode == 208 || keyCode == mc.gameSettings.keyBindBack.getKeyCode()) {
			resetRow(true);
		}
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (hoverScroll) {
			yPos = mouseY;
			isScrolling = true;
		}
		else if (isMany) {
			int u = 173 + (width - xSize) / 2;
			int v = 18 + (height - ySize) / 2;
			if (mouseX >= u && mouseX <= u + 11 && mouseY >= v && mouseY <= v + 88) {
				int h = mouseY - v, r;
				if (h <= 7) {
					r = 0;
				} else if (h >= 81) {
					r = maxRows;
				} else {
					r = (int) ((double) maxRows * (double) h / 88.0d);
				}
				int old = row;
				if (r < 0) {
					r = 0;
				}
				if (r > maxRows) {
					r = maxRows;
				}
				if (old != r) {
					row = r;
					resetSlots();
				}
			}
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		if (isScrolling) { isScrolling = false; }
		super.mouseReleased(mouseX, mouseY, state);
	}

	private void resetRow(boolean bo) {
		if (!isMany) { return; }
		int old = row;
		if (bo) {
			row++;
		} else {
			row--;
		}
		if (row < 0) {
			row = 0;
		}
		if (row > maxRows - 5) {
			row = maxRows - 5;
		}
		if (old != row) {
			resetSlots();
		}
	}

	private void resetSlots() {
		if (!isMany) { return; }
		int m = row * 9, n = m + 45, i = -1;
		int t = cont.items.getSizeInventory(), u = 0, e = t;
		if (t % 9 != 0) {
			e -= t % 9;
		}
		for (int s = 0; s < t; s++) {
			Slot slot = cont.getSlot(s);
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

}
