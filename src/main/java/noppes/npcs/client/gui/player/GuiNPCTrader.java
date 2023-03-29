package noppes.npcs.client.gui.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

public class GuiNPCTrader extends GuiContainerNPCInterface implements ICustomScrollListener, IGuiData {
	// New
	private ContainerNPCTrader container;
	private Map<Integer, Deal> data;
	int px, py, colorP = 0x01000000;
	private ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/trader.png");
	private GuiCustomScroll scroll;
	private Integer selectDeal;
	boolean showHasItems = false;
	private ResourceLocation slot = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");
	boolean wait = false;
	private int canBuy = 0, canSell = 0;

	public GuiNPCTrader(EntityNPCInterface npc, ContainerNPCTrader container) {
		super(npc, container);
		this.closeOnEsc = true;
		this.ySize = 224;
		this.xSize = 224;
		this.title = "role.trader";
		// New
		this.container = container;
		this.selectDeal = -1;
		this.data = Maps.<Integer, Deal>newTreeMap();
	}

	@Override
	public void buttonEvent(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
		case 0: { // buy
			NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderMarketBuy, this.container.marcet.id, this.container.deal.id);
			break;
		}
		case 1: { // Sell
			NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderMarketSell, this.container.marcet.id, this.container.deal.id);
			break;
		}
		case 2: { // Reset
			NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderMarketReset, this.container.marcet.id, this.container.deal.id);
			break;
		}
		}
		this.wait = true;
		this.initGui();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		this.drawWorldBackground(0);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.resource);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);

		super.drawGuiContainerBackgroundLayer(f, i, j);
		ItemStack stack = this.container.deal.inventorySold.getStackInSlot(0);
		if (!stack.isEmpty()) {
			// Main Slot
			this.px = this.guiLeft + 150;
			this.py = this.guiTop + 17;
			GlStateManager.enableRescaleNormal();
			this.mc.renderEngine.bindTexture(this.slot);
			this.drawTexturedModalRect(this.px, this.py, 0, 0, 17, 17);
			this.drawTexturedModalRect(this.px + 17, this.py, 10, 0, 8, 17);
			this.drawTexturedModalRect(this.px, this.py + 17, 0, 10, 17, 8);
			this.drawTexturedModalRect(this.px + 17, this.py + 17, 10, 10, 8, 8);
			// Main Slot colored
			if (this.colorP != 0x80FF0000 && this.container.deal.count[1] > 0 && this.container.deal.amount == 0) {
				this.colorP = 0x80FF0000;
			}
			if (this.player.capabilities.isCreativeMode && this.colorP == 0x80FF0000) {
				this.colorP = 0x80FF6E00;
			}
			Gui.drawRect(this.px + 1, this.py + 1, this.px + 24, this.py + 24, this.colorP);
			GlStateManager.disableRescaleNormal();
			// Currency Colored
			if (this.getLabel(4) != null && this.getLabel(4).enabled && this.getButton(0) != null
					&& this.getButton(0).isMouseOver()) {
				int color = this.player.capabilities.isCreativeMode ? 0x80FF6E00 : 0x80FF0000;
				if (CustomNpcs.proxy.getPlayerData(this.player).game.money >= this.container.deal.money) {
					color = 0x8000FF00;
				}
				Gui.drawRect(this.guiLeft + 138, this.guiTop + 112, this.guiLeft + 218, this.guiTop + 136, color);
			}
			// Items
			if (!this.container.deal.inventoryCurrency.isEmpty()) {
				GlStateManager.pushMatrix();
				this.mc.renderEngine.bindTexture(this.slot);
				for (int slot = 0, pos = 0; slot < 9; slot++) {
					ItemStack curr = this.container.deal.inventoryCurrency.getStackInSlot(slot);
					if (curr.isEmpty()) {
						continue;
					}
					int u = this.px - 10 + (pos % 3) * 18;
					int v = this.py + 38 + (pos / 3) * 18;
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					this.drawTexturedModalRect(u, v, 0, 0, 18, 18);
					if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
						int color = this.player.capabilities.isCreativeMode ? 0x80FF6E00 : 0x80FF0000;
						if (AdditionalMethods.inventoryItemCount(this.player, curr, this.container.deal.availability,
								this.container.deal.ignoreDamage, this.container.deal.ignoreNBT) >= curr.getCount()) {
							color = 0x8000FF00;
						}
						Gui.drawRect(u + 1, v + 1, u + 17, v + 17, color);
					}
					pos++;
				}
				GlStateManager.popMatrix();
			}
		}
		if (this.subgui != null) { return; }
		int u0 = this.guiLeft + 138, u1 = this.guiLeft + this.xSize - 7;
		this.drawHorizontalLine(u0, u1, this.guiTop + 14, 0xA0000000);
		this.drawVerticalLine(u0 - 1, this.guiTop + 14, this.guiTop + 43, 0xA0000000);
		this.drawVerticalLine(u1 + 1, this.guiTop + 14, this.guiTop + 43, 0xA0000000);
		this.drawHorizontalLine(u0, u1, this.guiTop + 43, 0xA0000000);
		this.drawVerticalLine(u0 - 1, this.guiTop + 43, this.guiTop + 111, 0xA0000000);
		this.drawVerticalLine(u1 + 1, this.guiTop + 43, this.guiTop + 111, 0xA0000000);
		this.drawHorizontalLine(u0, u1, this.guiTop + 111, 0xA0000000);
		this.drawHorizontalLine(u0, u1, this.guiTop + 136, 0xA0000000);
		this.drawVerticalLine(u0 - 1, this.guiTop + 111, this.guiTop + 136, 0xA0000000);
		this.drawVerticalLine(u1 + 1, this.guiTop + 111, this.guiTop + 136, 0xA0000000);
		this.colorP = 0x01000000;
	}

	// New
	@Override
	public void drawScreen(int i, int j, float f) {
		ItemStack stack = this.container.deal.inventorySold.getStackInSlot(0);
		if (!stack.isEmpty()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft, this.guiTop, 50.0f);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.translate(this.px - this.guiLeft, this.py - this.guiTop, 0.0f);
			RenderHelper.enableStandardItemLighting();
			float s = 1.5f;
			GlStateManager.scale(s, s, s);
			this.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
			GlStateManager.translate(0.0f, 0.0f, 200.0f);
			this.drawString(this.mc.fontRenderer, "" + stack.getCount(), (int) ((17 - (stack.getCount() > 9 ? 9 : 0)) / s), (int) (14 / s), 0xFFFFFFFF);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();

			if (isMouseHover(i, j, this.guiLeft + 150, this.guiTop + 14, 25, 25)) {
				List<String> list = new ArrayList<String>();
				list.add(new TextComponentTranslation("market.hover.product").getFormattedText());
				list.addAll(stack.getTooltip(this.mc.player,
						this.mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL));
				this.hoverText = list.toArray(new String[list.size()]);
			}
			if (!this.container.deal.inventoryCurrency.isEmpty()) {
				for (int slot = 0, pos = 0; slot < 9; slot++) {
					ItemStack curr = this.container.deal.inventoryCurrency.getStackInSlot(slot);
					if (curr.isEmpty()) {
						continue;
					}
					int u = this.px - 10 + (pos % 3) * 18;
					int v = this.py + 38 + (pos / 3) * 18;
					GlStateManager.pushMatrix();
					GlStateManager.translate(u, v, 50.0f);
					this.mc.getRenderItem().renderItemAndEffectIntoGUI(curr, 0, 0);
					// this.mc.getRenderItem().renderItemOverlayIntoGUI(this.mc.fontRenderer, stack,
					// 0, 0, (String)null);
					GlStateManager.translate(0.0f, 0.0f, 200.0f);
					this.drawString(this.mc.fontRenderer, "" + curr.getCount(), (12 - (curr.getCount() > 9 ? 6 : 0)), 10, 0xFFFFFFFF);
					RenderHelper.disableStandardItemLighting();
					GlStateManager.popMatrix();
					if (isMouseHover(i, j, u, v, 18, 18)) {
						List<String> list = new ArrayList<String>();
						list.add(new TextComponentTranslation("market.hover.item").getFormattedText());
						list.addAll(curr.getTooltip(this.mc.player,
								this.mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED
										: TooltipFlags.NORMAL));
						this.hoverText = list.toArray(new String[list.size()]);
					}
					pos++;
				}
			}
		}
		if (this.getLabel(6) != null && this.getLabel(6).enabled) {
			this.getLabel(6).setLabel(new TextComponentTranslation("gui.market.uptime", new Object[] {
					AdditionalMethods.ticksToElapsedTime(this.container.marcet.nextTime / 50, false, false, false) })
							.getFormattedText());
		}
		super.drawScreen(i, j, f);
		if (this.subgui != null) { return; }
		if (this.getLabel(3).enabled && isMouseHover(i, j, this.guiLeft + 140, this.guiTop + 113, 80, 24)) {
			TextComponentBase text = new TextComponentTranslation("market.hover.currency.0",
					new Object[] { this.container.deal.money, CustomNpcs.charCurrencies.charAt(0),
							"" + CustomNpcs.proxy.getPlayerData(this.player).game.money,
							CustomNpcs.charCurrencies.charAt(0) });
			if (this.container.deal.type != 0) {
				text.appendSibling(new TextComponentTranslation("market.hover.currency.1", new Object[] {
						AdditionalMethods.getTextReducedNumber(this.container.deal.money / 4, true, true, false),
						CustomNpcs.charCurrencies.charAt(0) }));
			}
			this.setHoverText(text.getFormattedText());
		} else if (this.getButton(0) != null && this.getButton(0).visible && this.getButton(0).isMouseOver()) {
			ITextComponent text = new TextComponentTranslation("market.hover.buy", new Object[] { stack.getDisplayName() });
			if (this.canBuy!=0) {
				text.appendSibling(new TextComponentTranslation("market.hover.notbuy."+this.canBuy));
			}
			this.setHoverText(text.getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).visible && this.getButton(1).isMouseOver()) {
			this.colorP = 0x8000FF00;
			if (AdditionalMethods.inventoryItemCount(this.player, stack, this.container.deal.availability,
					this.container.deal.ignoreDamage, this.container.deal.ignoreNBT) < stack.getCount()) {
				this.colorP = 0x80FF0000;
			}
			TextComponentBase text = new TextComponentTranslation("market.hover.sell.0",
					new Object[] { stack.getDisplayName() });
			if (!this.container.deal.inventoryCurrency.isEmpty()) {
				List<ITextComponent> items = new ArrayList<ITextComponent>();
				for (int slot = 0; slot < 9; slot++) {
					ItemStack curr = this.container.deal.inventoryCurrency.getStackInSlot(slot);
					if (curr.isEmpty() || curr.getCount() / 4 == 0) {
						continue;
					}
					items.add(new TextComponentString("<br>" + curr.getDisplayName() + " x" + (curr.getCount() / 4)));
				}
				if (items.size()==0 && !this.container.deal.inventoryCurrency.isEmpty()) {
					text.appendSibling(new TextComponentTranslation("market.hover.sell.3"));
				}
				if (items.size() > 0) {
					text.appendSibling(new TextComponentTranslation("market.hover.sell.1"));
					for (ITextComponent it : items) {
						text.appendSibling(it);
					}
				}
			}
			if (this.container.deal.money / 4 > 0) {
				text.appendSibling(new TextComponentTranslation("market.hover.sell.2",
						new Object[] { "" + (this.container.deal.money / 4), CustomNpcs.charCurrencies.charAt(0) }));
			}
			if (this.canSell!=0) {
				text.appendSibling(new TextComponentTranslation("market.hover.notsell."+this.canSell));
			}
			this.setHoverText(text.getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).visible && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.reset").getFormattedText());
		} else if (this.container.deal.count[1] > 0
				&& isMouseHover(i, j, this.guiLeft + 177, this.guiTop + 24, 45, 14)) {
			this.setHoverText(
					this.container.deal.count[1] > 0
							? new TextComponentTranslation("market.hover.item.amount",
									new Object[] { "" + this.container.deal.amount }).getFormattedText()
							: "");
		} else if (this.getLabel(6).enabled && isMouseHover(i, j, this.guiLeft + 80, this.guiTop + 5,
				this.mc.fontRenderer.getStringWidth(this.getLabel(6).label.get(0)), 10)) {
			this.setHoverText(new TextComponentTranslation("market.hover.update").getFormattedText());
		}

	}

	@Override
	public void initGui() {
		super.initGui();
		this.data.clear();
		List<ItemStack> list = new ArrayList<ItemStack>();
		List<String[]> infoList = new ArrayList<String[]>();
		List<String> sel = new ArrayList<String>();
		for (Deal d : this.container.marcet.data.values()) {
			if (this.selectDeal<0) { this.selectDeal = d.id; }
			this.data.put(d.id, d);
			sel.add(d.getName());
			ItemStack stack = d.inventorySold.getStackInSlot(0);
			list.add(stack);

			List<String> info = new ArrayList<String>();
			info.add(new TextComponentTranslation("market.hover.product").getFormattedText());
			info.add(stack.getDisplayName() + " x" + stack.getCount()
					+ (d.count[1] > 0 ? " "
							+ new TextComponentTranslation("market.hover.item.amount", new Object[] { "" + d.amount })
									.getFormattedText()
							: ""));
			if (!d.inventoryCurrency.isEmpty()) {
				info.add(new TextComponentTranslation("market.hover.item").getFormattedText());
				for (int slot = 0; slot < 9; slot++) {
					ItemStack curr = this.container.deal.inventoryCurrency.getStackInSlot(slot);
					if (curr.isEmpty()) {
						continue;
					}
					info.add(curr.getDisplayName() + " x" + curr.getCount());
				}
			}
			if (d.money > 0) {
				info.add(new TextComponentTranslation("market.hover.currency").getFormattedText());
				info.add("" + d.money + CustomNpcs.charCurrencies.charAt(0));
			}
			infoList.add(info.toArray(new String[info.size()]));
		}
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 6)).setSize(130, 100);
		}
		this.scroll.setListNotSorted(sel);
		this.scroll.setStacks(list);

		this.scroll.hoversTexts = infoList.toArray(new String[infoList.size()][1]);

		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 14;
		if (this.selectDeal>=0) {
			this.scroll.selected = this.selectDeal;
			this.title = this.container.marcet.getShowName();
		} else {
			this.title = new TextComponentTranslation("role.trader").getFormattedText() + ": " + this.npc.getName();
		}
		this.addScroll(this.scroll);

		this.addLabel(new GuiNpcLabel(1, "gui.market.deals", this.guiLeft + 4, this.guiTop + 5));
		this.addLabel(new GuiNpcLabel(2, "gui.market.barter", this.guiLeft + 140, this.guiTop + 46));
		this.getLabel(2).color = 0xFF202020;
		if (this.container.deal.inventoryCurrency.isEmpty()) {
			this.getLabel(2).enabled = false;
		} else {
			this.getLabel(2).enabled = true;
		}
		this.addLabel(new GuiNpcLabel(3, "gui.market.currency", this.guiLeft + 140, this.guiTop + 114));
		this.addLabel(new GuiNpcLabel(4, "", this.guiLeft + 140, this.guiTop + 126)); // Money
		this.getLabel(3).color = 0xFF202020;
		this.getLabel(4).color = 0xFF202020;
		if (this.container.deal.money > 0) {
			this.getLabel(3).enabled = true;
			this.getLabel(4).enabled = true;
			this.getLabel(4).setLabel(AdditionalMethods.getTextReducedNumber(this.container.deal.money, true, true,
					false) + CustomNpcs.charCurrencies.charAt(0) + " / "
					+ CustomNpcs.proxy.getPlayerData(this.player).game.getTextMoney()
					+ CustomNpcs.charCurrencies.charAt(0));
		} else {
			this.getLabel(3).enabled = false;
			this.getLabel(4).enabled = false;
		}

		this.addLabel(new GuiNpcLabel(5, "", this.guiLeft + 177, this.guiTop + 25)); // amount
		this.getLabel(5).color = 0xFF202020;
		if (this.container.deal.count[1] > 0) {
			this.getLabel(5).setLabel(new String(Character.toChars(0x00A7))
					+ (this.container.deal.amount == 0 ? "4"
							: this.container.deal.amount < this.container.deal.inventorySold.getStackInSlot(0)
									.getMaxStackSize() ? "1" : "2")
					+ "x" + AdditionalMethods.getTextReducedNumber(this.container.deal.amount, true, true, false));
		} else {
			this.getLabel(5).setLabel(new String(Character.toChars(0x221E)));
		}
		this.addLabel(new GuiNpcLabel(6, "", this.guiLeft + 80, this.guiTop + 5)); // time
		this.getLabel(6).color = 0xFF202020;
		this.getLabel(6).enabled = this.container.marcet.updateTime > 0;

		this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 117, 64, 20, "gui.buy"));
		this.getButton(0).setVisible(this.container.deal.type != 1);
		this.canBuy = 0;
		if (this.getButton(0).visible) {
			if (!this.player.capabilities.isCreativeMode) {
				if(this.wait || this.container.deal.type == 1 || this.container.deal.count[1] != 0 || this.container.deal.amount <=0) {
					this.canBuy = 1;
				}
				else if (this.canBuy==0 && !this.container.deal.availability.isAvailable(this.player)) {
					this.canBuy = 2;
				}
				else if (this.canBuy==0 && this.container.deal.money > 0 && CustomNpcs.proxy.getPlayerData(this.player).game.money < this.container.deal.money) {
					this.canBuy = 3;
				}
				else if (this.canBuy==0 && !AdditionalMethods.canRemoveItems(this.player, this.container.deal.inventoryCurrency.items, this.container.deal.ignoreDamage, this.container.deal.ignoreNBT)) {
					this.canBuy = 4;
				}
				else if (this.canBuy==0 && !AdditionalMethods.canAddItemAfterRemoveItems(this.player, this.container.deal.inventorySold.getStackInSlot(0), this.container.deal.inventoryCurrency.items, this.container.deal.ignoreDamage, this.container.deal.ignoreNBT)) {
					this.canBuy = 5;
				}
			}
			this.getButton(0).setEnabled(this.canBuy==0);
		}

		this.addButton(new GuiNpcButton(1, this.guiLeft + 70, this.guiTop + 117, 64, 20, "gui.sell"));
		this.getButton(1).setVisible(this.container.deal.type != 0);
		this.canSell = 0;
		if (this.getButton(1).visible) {
			if (!this.player.capabilities.isCreativeMode) {
				if (this.canSell==0 && !this.container.deal.availability.isAvailable(this.player)) {
					this.canSell = 2;
				}
				else if (this.canSell==0 && !this.container.deal.inventoryCurrency.isEmpty() && !AdditionalMethods.canRemoveItems(this.player, this.container.deal.inventorySold.items, this.container.deal.ignoreDamage, this.container.deal.ignoreNBT)) {
					this.canSell = 4;
				}
				else if (this.wait) { this.canSell = 1; }
			}
			this.getButton(1).setEnabled(this.canSell==0);
		}

		this.addButton(new GuiNpcButton(2, this.guiLeft - 66, this.guiTop + 117, 64, 20, "remote.reset"));
		this.getButton(2).setVisible(this.player.capabilities.isCreativeMode && this.container.marcet.updateTime > 0);

		this.px = this.guiLeft + 150;
		this.py = this.guiTop + 17;
	}

	@Override
	public void save() {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderMarketRemove, this.container.marcet.id);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (!this.data.containsKey(scroll.selected)) { return; }
		this.selectDeal = scroll.selected;
		this.container.deal = this.data.get(this.selectDeal);
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.wait = false;
		this.container.marcet.readEntityFromNBT(compound);
		if (this.selectDeal>=0) {
			for (Deal d : this.container.marcet.data.values()) {
				if (d.id==this.selectDeal) {
					this.container.deal = d;
					break;
				}
			}
		}
		this.initGui();
	}

	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i==200 || i==ClientProxy.frontButton.getKeyCode() || i==208 || i==ClientProxy.backButton.getKeyCode()) { // up or down
			if (!this.data.containsKey(scroll.selected)) { return; }
			this.selectDeal = this.scroll.selected;
			this.container.deal = this.data.get(this.selectDeal);
			this.initGui();
		}
	}
	
}
