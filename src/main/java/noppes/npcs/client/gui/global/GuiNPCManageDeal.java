package noppes.npcs.client.gui.global;

import java.util.Arrays;

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
import noppes.npcs.util.AdditionalMethods;

public class GuiNPCManageDeal extends GuiContainerNPCInterface2 implements ITextfieldListener {

	private final Deal deal;

	public GuiNPCManageDeal(EntityNPCInterface npc, ContainerNPCTraderSetup cont) {
		super(npc, cont);
		this.ySize = 200;
		this.deal = cont.deal;
		this.setBackground("tradersetup.png");
		Client.sendData(EnumPacketServer.TraderMarketGet);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
		case 0: {
			this.deal.setIgnoreDamage(button.getValue() == 1);
			break;
		}
		case 1: {
			this.deal.setIgnoreNBT(button.getValue() == 1);
			break;
		}
		case 2: {
			this.setSubGui(new SubGuiNpcAvailability(this.deal.availability));
			this.initGui();
			break;
		}
		case 3: {
			this.deal.setType(button.getValue());
			break;
		}
		case 66: {
			this.close();
			break;
		}
		}
	}

	@Override
	public void close() {
		super.close();
		NoppesUtil.requestOpenGUI(EnumGuiType.SetupTrader, -1, this.deal.getId(), 0);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		super.drawGuiContainerBackgroundLayer(f, i, j);
		if (this.subgui != null) {
			return;
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		for (int slotId = 0; slotId < 10; ++slotId) {
            this.inventorySlots.getSlot(slotId);
            int x = this.guiLeft + this.inventorySlots.getSlot(slotId).xPos;
			int y = this.guiTop + this.inventorySlots.getSlot(slotId).yPos;
			this.mc.renderEngine.bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.drawTexturedModalRect(x - 1, y - 1, 0, 0, 18, 18);
		}
		this.drawHorizontalLine(this.guiLeft + 212, this.guiLeft + this.xSize - 4, this.guiTop + 14, 0x80000000);
		this.drawHorizontalLine(this.guiLeft + 45, this.guiLeft + 210, this.guiTop + 133, 0x80000000);
		this.drawVerticalLine(this.guiLeft + 44, this.guiTop + 4, this.guiTop + this.ySize + 12, 0x80000000);
		this.drawVerticalLine(this.guiLeft + 211, this.guiTop + 4, this.guiTop + this.ySize + 12, 0x80000000);
		int x0 = this.guiLeft + 96;
		int x1 = x0 + 63;
		int y0 = this.guiTop + 14;
		int y1 = y0 + 108;
		int y2 = y0 + 36;
		this.drawHorizontalLine(x0 + 1, x1 - 1, y0 + 1, 0x80000000);
		this.drawHorizontalLine(x0 + 1, x1, y1, 0x80000000);
		this.drawHorizontalLine(x0 + 1, x1, y2, 0x80000000);
		this.drawVerticalLine(x0, y0, y1 + 1, 0x80000000);
		this.drawVerticalLine(x1, y0, y1, 0x80000000);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui != null || !CustomNpcs.ShowDescriptions) {
			return;
		}
		// Labels
		if (this.getLabel(0) != null && this.getLabel(0).hovered) {
			this.setHoverText(new TextComponentTranslation("market.hover.product").getFormattedText());
		} else if (this.getLabel(1) != null && this.getLabel(1).hovered) {
			this.setHoverText(new TextComponentTranslation("market.hover.item").getFormattedText());
		} else if (this.getTextField(0) != null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.set.currency").getFormattedText());
		} else if (this.getTextField(1) != null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.set.chance").getFormattedText());
		} else if (this.getTextField(2) != null && this.getTextField(2).isMouseOver()
				|| this.getTextField(3) != null && this.getTextField(3).isMouseOver()
				|| this.getTextField(4) != null && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.set.amount").getFormattedText());
		} else if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.damage").getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.nbt").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.set.type").getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.deal.section").getFormattedText());
		} else if (this.getButton(66) != null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();

		String text = AdditionalMethods.instance
				.deleteColor(new TextComponentTranslation("market.product").getFormattedText());
		int x = this.guiLeft + this.inventorySlots.getSlot(0).xPos + (18 - this.fontRenderer.getStringWidth(text)) / 2;
		int y = this.guiTop + this.inventorySlots.getSlot(0).yPos - 10;
		this.addLabel(new GuiNpcLabel(0, "market.product", x, y));

		x = this.guiLeft + this.inventorySlots.getSlot(1).xPos;
		y = this.guiTop + this.inventorySlots.getSlot(1).yPos - 10;
		this.addLabel(new GuiNpcLabel(1, "market.barter", x, y));

		x = this.guiLeft + 214;
		y = this.guiTop + 4;
		this.addLabel(new GuiNpcLabel(4, "marcet.deal.settings", x, y));

		this.addLabel(new GuiNpcLabel(5, "market.currency", x, (y += 14) + 5));
		this.addLabel(new GuiNpcLabel(6, CustomNpcs.displayCurrencies, x + 155, y + 5));
		this.addTextField(new GuiNpcTextField(0, this, x + 100, y, 50, 18, "" + this.deal.getMoney()));
		this.getTextField(0).setNumbersOnly();
		this.getTextField(0).setMinMaxDefault(0, Integer.MAX_VALUE, this.deal.getMoney());

		this.addLabel(new GuiNpcLabel(7, "drop.chance", x, (y += 22) + 5));
		this.addLabel(new GuiNpcLabel(8, "%", x + 155, y + 5));
		this.addTextField(new GuiNpcTextField(1, this, x + 100, y, 50, 18, "" + this.deal.getChance()));
		this.getTextField(1).setNumbersOnly();
		this.getTextField(1).setMinMaxDefault(0, 100, this.deal.getChance());

		this.addLabel(new GuiNpcLabel(9, "quest.itemamount", x, (y += 22) + 5));
		this.addTextField(new GuiNpcTextField(2, this, x + 100, y, 40, 18, "" + this.deal.getMinCount()));
		this.getTextField(2).setNumbersOnly();
		this.getTextField(2).setMinMaxDefault(0, Integer.MAX_VALUE, this.deal.getMinCount());
		this.addLabel(new GuiNpcLabel(10, "<->", x + 145, y + 5));
		this.addTextField(new GuiNpcTextField(3, this, x + 160, y, 40, 18, "" + this.deal.getMaxCount()));
		this.getTextField(3).setNumbersOnly();
		this.getTextField(3).setMinMaxDefault(0, Integer.MAX_VALUE, this.deal.getMaxCount());

		this.addLabel(new GuiNpcLabel(11, "gui.ignoreDamage", x, (y += 21) + 5));
		this.addButton(new GuiNpcButton(0, x + 100, y, 80, 20,
				new String[] { "gui.ignoreDamage.0", "gui.ignoreDamage.1" }, this.deal.getIgnoreDamage() ? 1 : 0));

		this.addLabel(new GuiNpcLabel(12, "gui.ignoreNBT", x, (y += 22) + 5));
		this.addButton(new GuiNpcButton(1, x + 100, y, 80, 20, new String[] { "gui.ignoreNBT.0", "gui.ignoreNBT.1" },
				this.deal.getIgnoreNBT() ? 1 : 0));

		this.addLabel(new GuiNpcLabel(13, "availability.options", x, (y += 22) + 5));
		this.addButton(new GuiNpcButton(2, x + 100, y, 80, 20, "selectServer.edit"));

		this.addButton(new GuiNpcButton(3, x, (y += 22), 200, 20,
				new String[] { "market.deal.type.0", "market.deal.type.1", "market.deal.type.2" },
				this.deal.getType()));

		this.addButton(new GuiNpcButton(66, x, y + 22, 80, 20, "gui.back"));
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui == null) {
			this.close();
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.TraderMarketSave, this.deal.writeToNBT());
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getId()) {
		case 0: {
			this.deal.setMoney(textField.getInteger());
			this.initGui();
			break;
		}
		case 1: {
			this.deal.setChance(textField.getInteger());
			this.initGui();
			break;
		}
		case 2: {
			this.deal.setCount(textField.getInteger(), this.deal.getMaxCount());
			this.initGui();
			break;
		}
		case 3: {
			this.deal.setCount(this.deal.getMinCount(), textField.getInteger());
			this.initGui();
			break;
		}
		}
	}

}
