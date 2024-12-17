package noppes.npcs.client.gui.global;

import java.awt.*;
import java.util.Arrays;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCTraderSetup;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class SubGuiNPCManageDeal
extends GuiContainerNPCInterface2
implements ITextfieldListener {

	public static GuiScreen parent;
	private final Deal deal;

	public SubGuiNPCManageDeal(EntityNPCInterface npc, ContainerNPCTraderSetup cont) {
		super(npc, cont);
		ySize = 200;
		setBackground("tradersetup.png");

		deal = cont.deal;
		Client.sendData(EnumPacketServer.TraderMarketGet);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 0: {
				deal.setIgnoreDamage(button.getValue() == 1);
				break;
			}
			case 1: {
				deal.setIgnoreNBT(button.getValue() == 1);
				break;
			}
			case 2: {
				setSubGui(new SubGuiNpcAvailability(deal.availability,  parent));
				initGui();
				break;
			}
			case 3: {
				deal.setType(button.getValue());
				break;
			}
			case 66: {
				close();
				break;
			}
		}
	}

	@Override
	public void close() {
		super.close();
		NoppesUtil.requestOpenGUI(EnumGuiType.SetupTrader, -1, deal.getId(), 0);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		super.drawGuiContainerBackgroundLayer(f, i, j);
		if (subgui != null) {
			return;
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		for (int slotId = 0; slotId < 10; ++slotId) {
            inventorySlots.getSlot(slotId);
            int x = guiLeft + inventorySlots.getSlot(slotId).xPos;
			int y = guiTop + inventorySlots.getSlot(slotId).yPos;
			mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			drawTexturedModalRect(x - 1, y - 1, 0, 0, 18, 18);
		}
		int color = new Color(0x80000000).getRGB();
		drawHorizontalLine(guiLeft + 212, guiLeft + xSize - 4, guiTop + 14, color);
		drawHorizontalLine(guiLeft + 45, guiLeft + 210, guiTop + 133, color);
		drawVerticalLine(guiLeft + 44, guiTop + 4, guiTop + ySize + 12, color);
		drawVerticalLine(guiLeft + 211, guiTop + 4, guiTop + ySize + 12, color);
		int x0 = guiLeft + 96;
		int x1 = x0 + 63;
		int y0 = guiTop + 14;
		int y1 = y0 + 108;
		int y2 = y0 + 36;
		drawHorizontalLine(x0 + 1, x1 - 1, y0 + 1, color);
		drawHorizontalLine(x0 + 1, x1, y1, color);
		drawHorizontalLine(x0 + 1, x1, y2, color);
		drawVerticalLine(x0, y0, y1 + 1, color);
		drawVerticalLine(x1, y0, y1, color);
	}

	@Override
	public void initGui() {
		super.initGui();

		String text = Util.instance.deleteColor(new TextComponentTranslation("market.product").getFormattedText());
		int x = guiLeft + inventorySlots.getSlot(0).xPos + (18 - fontRenderer.getStringWidth(text)) / 2;
		int y = guiTop + inventorySlots.getSlot(0).yPos - 10;
		GuiNpcLabel label = new GuiNpcLabel(0, "market.product", x, y);
		label.setHoverText("market.hover.product");
		addLabel(label);
		x = guiLeft + inventorySlots.getSlot(1).xPos;
		y = guiTop + inventorySlots.getSlot(1).yPos - 10;
		label = new GuiNpcLabel(1, "market.barter", x, y);
		label.setHoverText("market.hover.item");
		addLabel(label);
		x = guiLeft + 214;
		y = guiTop + 4;
		label = new GuiNpcLabel(4, "marcet.deal.settings", x, y);
		label.setHoverText("market.hover.deal.section");
		addLabel(label);
		addLabel(new GuiNpcLabel(5, "market.currency", x, (y += 14) + 5));
		addLabel(new GuiNpcLabel(6, CustomNpcs.displayCurrencies, x + 155, y + 5));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, x + 100, y, 50, 18, "" + deal.getMoney());
		textField.setMinMaxDefault(0, Integer.MAX_VALUE, deal.getMoney());
		textField.setHoverText("market.hover.set.currency");
		addTextField(textField);

		addLabel(new GuiNpcLabel(7, "drop.chance", x, (y += 22) + 5));
		addLabel(new GuiNpcLabel(8, "%", x + 155, y + 5));
		textField = new GuiNpcTextField(1, this, x + 100, y, 50, 18, "" + deal.getChance());
		textField.setMinMaxDefault(0, 100, deal.getChance());
		textField.setHoverText("market.hover.set.chance");
		addTextField(textField);

		addLabel(new GuiNpcLabel(9, "quest.itemamount", x, (y += 22) + 5));
		textField = new GuiNpcTextField(2, this, x + 100, y, 40, 18, "" + deal.getMinCount());
		textField.setMinMaxDefault(0, Integer.MAX_VALUE, deal.getMinCount());
		textField.setHoverText("market.hover.set.amount");
		addTextField(textField);

		addLabel(new GuiNpcLabel(10, "<->", x + 145, y + 5));
		textField = new GuiNpcTextField(3, this, x + 160, y, 40, 18, "" + deal.getMaxCount());
		textField.setMinMaxDefault(0, Integer.MAX_VALUE, deal.getMaxCount());
		textField.setHoverText("market.hover.set.amount");
		addTextField(textField);

		addLabel(new GuiNpcLabel(11, "gui.ignoreDamage", x, (y += 21) + 5));
		GuiNpcButton button = new GuiNpcButton(0, x + 100, y, 80, 20, new String[] { "gui.ignoreDamage.0", "gui.ignoreDamage.1" }, deal.getIgnoreDamage() ? 1 : 0);
		button.setHoverText("recipe.hover.damage");
		addButton(button);
		addLabel(new GuiNpcLabel(12, "gui.ignoreNBT", x, (y += 22) + 5));
		button = new GuiNpcButton(1, x + 100, y, 80, 20, new String[] { "gui.ignoreNBT.0", "gui.ignoreNBT.1" }, deal.getIgnoreNBT() ? 1 : 0);
		button.setHoverText("recipe.hover.nbt");
		addButton(button);
		addLabel(new GuiNpcLabel(13, "availability.options", x, (y += 22) + 5));
		button = new GuiNpcButton(2, x + 100, y, 80, 20, "selectServer.edit");
		button.setHoverText("availability.hover");
		addButton(button);
		button = new GuiNpcButton(3, x, (y += 22), 200, 20, new String[] { "market.deal.type.0", "market.deal.type.1", "market.deal.type.2" }, deal.getType());
		button.setHoverText("market.hover.set.type");
		addButton(button);
		button = new GuiNpcButton(66, x, y + 22, 80, 20, "gui.back");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && subgui == null) {
			close();
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.TraderMarketSave, deal.writeToNBT());
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getId()) {
			case 0: {
				deal.setMoney(textField.getInteger());
				break;
			}
			case 1: {
				deal.setChance(textField.getInteger());
				break;
			}
			case 2: {
				deal.setCount(textField.getInteger(), deal.getMaxCount());
				break;
			}
			case 3: {
				deal.setCount(deal.getMinCount(), textField.getInteger());
				break;
			}
		}
		initGui();
	}

}
