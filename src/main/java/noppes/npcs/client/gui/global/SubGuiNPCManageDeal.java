package noppes.npcs.client.gui.global;

import java.awt.*;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.availability.SubGuiNpcAvailability;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCTraderSetup;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class SubGuiNPCManageDeal extends GuiContainerNPCInterface implements ITextfieldListener {

	protected final Deal deal;
	public static GuiScreen parent;

	public SubGuiNPCManageDeal(EntityNPCInterface npc, ContainerNPCTraderSetup cont) {
		super(npc, cont);
		setBackground("tradersetup.png");
		closeOnEsc = true;
		ySize = 200;

		deal = cont.deal;
		Client.sendData(EnumPacketServer.TraderMarketGet);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: deal.setIgnoreDamage(button.getValue() == 1); break;
			case 1: deal.setIgnoreNBT(button.getValue() == 1); break;
			case 2: setSubGui(new SubGuiNpcAvailability(deal.availability,  parent)); initGui(); break;
			case 3: deal.setType(button.getValue()); break;
			case 66: onClosed(); break;
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		if (parent != null) { displayGuiScreen(parent); }
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
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
		addLabel(new GuiNpcLabel(0, "market.product", x, y)
				.setHoverText("market.hover.product"));
		x = guiLeft + inventorySlots.getSlot(1).xPos;
		y = guiTop + inventorySlots.getSlot(1).yPos - 10;
		addLabel(new GuiNpcLabel(1, "market.barter", x, y)
				.setHoverText("market.hover.item"));
		x = guiLeft + 214;
		y = guiTop + 4;
		addLabel(new GuiNpcLabel(4, "marcet.deal.settings", x, y)
				.setHoverText("market.hover.deal.section"));
		addLabel(new GuiNpcLabel(5, "market.currency", x, (y += 14) + 5));
		addLabel(new GuiNpcLabel(6, CustomNpcs.displayCurrencies, x + 155, y + 5));
		addTextField(new GuiNpcTextField(0, this, x + 100, y, 50, 18, "" + deal.getMoney())
				.setMinMaxDefault(0, Integer.MAX_VALUE, deal.getMoney())
				.setHoverText("market.hover.set.currency"));
		addLabel(new GuiNpcLabel(7, "drop.chance", x, (y += 22) + 5));
		addLabel(new GuiNpcLabel(8, "%", x + 155, y + 5));
		addTextField(new GuiNpcTextField(1, this, x + 100, y, 50, 18, "" + deal.getChance())
				.setMinMaxDefault(0, 100, deal.getChance())
				.setHoverText("market.hover.set.chance"));
		addLabel(new GuiNpcLabel(9, "quest.itemamount", x, (y += 22) + 5));
		addTextField(new GuiNpcTextField(2, this, x + 100, y, 40, 18, "" + deal.getMinCount())
				.setMinMaxDefault(0, Integer.MAX_VALUE, deal.getMinCount())
				.setHoverText("market.hover.set.amount"));
		addLabel(new GuiNpcLabel(10, "<->", x + 145, y + 5));
		addTextField(new GuiNpcTextField(3, this, x + 160, y, 40, 18, "" + deal.getMaxCount())
				.setMinMaxDefault(0, Integer.MAX_VALUE, deal.getMaxCount())
				.setHoverText("market.hover.set.amount"));
		addLabel(new GuiNpcLabel(11, "gui.ignoreDamage", x, (y += 21) + 5));
		addButton(new GuiNpcButton(0, x + 100, y, 80, 20, new String[] { "gui.ignoreDamage.0", "gui.ignoreDamage.1" }, deal.getIgnoreDamage() ? 1 : 0)
				.setHoverText("recipe.hover.damage"));
		addLabel(new GuiNpcLabel(12, "gui.ignoreNBT", x, (y += 22) + 5));
		addButton(new GuiNpcButton(1, x + 100, y, 80, 20, new String[] { "gui.ignoreNBT.0", "gui.ignoreNBT.1" }, deal.getIgnoreNBT() ? 1 : 0)
				.setHoverText("recipe.hover.nbt"));
		addLabel(new GuiNpcLabel(13, "availability.options", x, (y += 22) + 5));
		addButton(new GuiNpcButton(2, x + 100, y, 80, 20, "selectServer.edit")
				.setHoverText("availability.hover"));
		addButton(new GuiNpcButton(3, x, (y += 22), 200, 20, new String[] { "market.deal.type.0", "market.deal.type.1", "market.deal.type.2" }, deal.getType())
				.setHoverText("market.hover.set.type"));
		addButton(new GuiNpcButton(66, x, y + 22, 80, 20, "gui.back")
				.setHoverText("hover.back"));
	}

	@Override
	public void save() { Client.sendData(EnumPacketServer.TraderMarketSave, deal.write()); }

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getID()) {
			case 0: deal.setMoney(textField.getInteger()); break;
			case 1: deal.setChance(textField.getInteger()); break;
			case 2: deal.setCount(textField.getInteger(), deal.getMaxCount()); break;
			case 3: deal.setCount(deal.getMinCount(), textField.getInteger()); break;
		}
		initGui();
	}

}
