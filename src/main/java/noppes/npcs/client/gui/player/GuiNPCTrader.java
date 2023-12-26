package noppes.npcs.client.gui.player;

import java.util.ArrayList;
import java.util.Collections;
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
import noppes.npcs.api.handler.data.IMarcet;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.DealMarkup;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.MarkupData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

public class GuiNPCTrader
extends GuiContainerNPCInterface
implements ICustomScrollListener, IGuiData {

	private Map<String, Deal> data;
	int px, py, colorP = 0x01000000;
	private ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/trader.png");
	private GuiCustomScroll scroll;
	boolean showHasItems = false;
	private ResourceLocation slot = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");
	boolean wait = false;
	private int canBuy = 0, canSell = 0;
	private DealMarkup selectDealData;
	private Marcet marcet;
	private long money;

	public GuiNPCTrader(EntityNPCInterface npc, ContainerNPCTrader container) {
		super(npc, container);
		this.closeOnEsc = true;
		this.ySize = 224;
		this.xSize = 224;
		this.title = "role.trader";
		this.marcet = container.marcet;
		this.data = Maps.<String, Deal>newTreeMap();
		this.money = 0L;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.data.clear();
		List<String> sel = new ArrayList<String>();
		List<String> selNot = new ArrayList<String>();
		int level = ClientProxy.playerData.game.getMarcetLevel(this.marcet.getId());
		MarcetController mData = MarcetController.getInstance();
		for (Deal deal : mData.deals.values()) {
			if (deal == null || deal.getMarcetID()!=this.marcet.getId() || !deal.isValid()) { continue; }
			String key = deal.getName();
			if (deal.getAmount()==0) { selNot.add(key); }
			else { sel.add(key); }
			this.data.put(key, (Deal) deal);
			if (this.selectDealData != null && this.selectDealData.deal != null && this.selectDealData.deal.getId() == deal.getId()) {
				this.selectDealData = mData.getBuyData(this.marcet, deal, level);
			}
		}
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 6)).setSize(130, 100);
		}
		Collections.sort(sel);
		Collections.sort(selNot);
		sel.addAll(selNot);
		List<ItemStack> stacks = new ArrayList<ItemStack>();
		List<String[]> infoList = new ArrayList<String[]>();
		for (String key : sel) {
			Deal deal = this.data.get(key);
			DealMarkup dm = mData.getBuyData(this.marcet, deal , level);
			stacks.add(dm.main);
			List<String> info = new ArrayList<String>();
			info.add(new TextComponentTranslation("market.hover.product").getFormattedText());
			info.add(dm.main.getDisplayName() + " x" + dm.count + " " + (new TextComponentTranslation("market.hover.item."+(deal.getMaxCount() > 0 ? "amount" : deal.getAmount()==0 ? "not" : "infinitely"), new Object[] { "" + deal.getAmount() }).getFormattedText()));
			
			if (!dm.buyItems.isEmpty()) {
				info.add(new TextComponentTranslation("market.hover.item").getFormattedText());
				for (ItemStack curr : dm.buyItems.keySet()) {
					info.add(curr.getDisplayName() + " x" + dm.buyItems.get(curr));
				}
			}
			if (dm.buyMoney > 0) {
				info.add(new TextComponentTranslation("market.hover.currency").getFormattedText());
				info.add("" + dm.buyMoney + CustomNpcs.charCurrencies.charAt(0));
			}
			infoList.add(info.toArray(new String[info.size()]));
		}
		if (this.selectDealData == null || this.selectDealData.deal == null) {
			this.selectDealData = mData.getBuyData(this.marcet, this.data.get(sel.get(0)), level);
		}
		this.selectDealData.cheak(this.mc.player.inventory.mainInventory);
		this.scroll.setListNotSorted(sel);
		this.scroll.setStacks(stacks);
		this.scroll.hoversTexts = infoList.toArray(new String[infoList.size()][1]);

		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 14;
		this.scroll.setSelected(this.selectDealData.deal.getName());
		this.title = this.marcet.getShowName();
		this.addScroll(this.scroll);

		this.addLabel(new GuiNpcLabel(1, "market.deals", this.guiLeft + 4, this.guiTop + 5));
		this.addLabel(new GuiNpcLabel(2, "market.barter", this.guiLeft + 140, this.guiTop + 46));
		this.getLabel(2).color = 0xFF202020;
		this.getLabel(2).enabled = !this.selectDealData.buyItems.isEmpty();
		this.addLabel(new GuiNpcLabel(3, "market.currency", this.guiLeft + 140, this.guiTop + 114));
		this.addLabel(new GuiNpcLabel(4, "", this.guiLeft + 140, this.guiTop + 126)); // Money
		this.getLabel(3).color = 0xFF202020;
		this.getLabel(4).color = 0xFF202020;
		this.money = ClientProxy.playerData.game.getMoney();
		if (this.selectDealData.buyMoney > 0) {
			this.getLabel(3).enabled = true;
			this.getLabel(4).enabled = true;
			String text = AdditionalMethods.getTextReducedNumber(this.selectDealData.buyMoney, true, true, false) + CustomNpcs.charCurrencies.charAt(0) + " / " + ClientProxy.playerData.game.getTextMoney() + CustomNpcs.charCurrencies.charAt(0);
			if (this.marcet.isLimited) {
				text += " / " + AdditionalMethods.getTextReducedNumber(this.marcet.money, true, true, false) + CustomNpcs.charCurrencies.charAt(0);
			}
			this.getLabel(4).setLabel(text);
		} else {
			this.getLabel(3).enabled = false;
			this.getLabel(4).enabled = false;
		}

		this.addLabel(new GuiNpcLabel(5, "", this.guiLeft + 177, this.guiTop + 25)); // amount
		this.getLabel(5).color = 0xFF202020;
		if (this.selectDealData.deal.getMaxCount() > 0) {
			this.getLabel(5).setLabel(new String(Character.toChars(0x00A7)) + (this.selectDealData.deal.getAmount() == 0 ? "4" : this.selectDealData.deal.getAmount() < this.selectDealData.deal.getProduct().getMaxStackSize() ? "1" : "2") + "x" + AdditionalMethods.getTextReducedNumber(this.selectDealData.deal.getAmount(), true, true, false));
		} else {
			this.getLabel(5).setLabel(new String(Character.toChars(0x221E)));
		}
		if (this.marcet.updateTime > 0) {
			this.addLabel(new GuiNpcLabel(6, "", this.guiLeft + 80, this.guiTop + 5)); // time
			this.getLabel(6).color = 0xFF202020;
			this.getLabel(6).enabled = this.marcet.updateTime > 0;
		}

		this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 117, 64, 20, "gui.buy"));
		this.getButton(0).setVisible(this.selectDealData.deal.getType() != 1);
		this.canBuy = 0;
		if (this.getButton(0).visible) {
			if (!this.player.capabilities.isCreativeMode) {
				if (this.wait || this.selectDealData.deal.getType() == 1) { this.canBuy = 1; }
				if (this.canBuy==0 && this.selectDealData.deal.getAmount() <= 0) { this.canBuy = 6; }
				if (this.canBuy==0 && !this.selectDealData.deal.availability.isAvailable(this.player)) { this.canBuy = 2; }
				if (this.canBuy==0 && this.selectDealData.buyMoney > 0 && this.money < this.selectDealData.buyMoney) { this.canBuy = 3; }
				if (this.canBuy==0 && !AdditionalMethods.canRemoveItems(this.player.inventory.mainInventory, this.selectDealData.buyItems, this.selectDealData.ignoreDamage, this.selectDealData.ignoreNBT)) { this.canBuy = 4; }
				if (this.canBuy==0 && !AdditionalMethods.canAddItemAfterRemoveItems(this.player.inventory.mainInventory, this.selectDealData.main, this.selectDealData.buyItems, this.selectDealData.ignoreDamage, this.selectDealData.ignoreNBT)) { this.canBuy = 5; }
			}
			this.getButton(0).setEnabled(this.canBuy==0 && this.canBuy!=6);
		}

		this.addButton(new GuiNpcButton(1, this.guiLeft + 70, this.guiTop + 117, 64, 20, "gui.sell"));
		this.getButton(1).setVisible(this.selectDealData.deal.getType() != 0);
		this.canSell = 0;
		if (this.getButton(1).visible) {
			if (!this.player.capabilities.isCreativeMode) {
				if (this.wait) { this.canSell = 1; }
				else if (!this.selectDealData.deal.availability.isAvailable(this.player)) { this.canSell = 2; }
				else {
					Map<ItemStack, Integer> map = Maps.newHashMap();
					map.put(this.selectDealData.main, this.selectDealData.count);
					if (this.canSell==0 && !this.selectDealData.buyItems.isEmpty() && !AdditionalMethods.canRemoveItems(this.player.inventory.mainInventory, map, this.selectDealData.ignoreDamage, this.selectDealData.ignoreNBT)) { this.canSell = 3; }
					if (this.canSell==0 && this.marcet.isLimited) {
						if (this.selectDealData.sellMoney > this.marcet.money) { this.canSell = 4; }
						if (this.canSell==0 && !this.selectDealData.sellItems.isEmpty() && !AdditionalMethods.canRemoveItems(this.marcet.inventory, this.selectDealData.sellItems, this.selectDealData.ignoreDamage, this.selectDealData.ignoreNBT)) { this.canSell = 5; }
					}
					if (this.canSell==0 && this.selectDealData.deal.getMaxCount()==0 && this.canBuy==6 && this.selectDealData.deal.getType()!=1) { this.canSell = 6; }
				}
			}
			this.getButton(1).setEnabled(this.canSell==0);
		}

		this.addButton(new GuiNpcButton(2, this.guiLeft - 66, this.guiTop + 117, 64, 20, "remote.reset"));
		this.getButton(2).setVisible(this.player.capabilities.isCreativeMode);

		this.px = this.guiLeft + 150;
		this.py = this.guiTop + 17;
	}

	@Override
	public void buttonEvent(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
			case 0: { // buy
				NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderMarketBuy, this.marcet.getId(), this.selectDealData.deal.getId(), this.npc.getEntityId());
				break;
			}
			case 1: { // Sell
				NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderMarketSell, this.marcet.getId(), this.selectDealData.deal.getId(), this.npc.getEntityId());
				break;
			}
			case 2: { // Reset
				NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderMarketReset, this.marcet.getId(), this.selectDealData.deal.getId(), this.npc.getEntityId());
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
		if (this.selectDealData!=null && !this.selectDealData.main.isEmpty()) {
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
			if (this.colorP != 0x80FF0000 && this.selectDealData.deal.getMaxCount() > 0 && this.selectDealData.deal.getAmount() == 0) {
				this.colorP = 0x80FF0000;
			}
			if (this.player.capabilities.isCreativeMode && this.colorP == 0x80FF0000) {
				this.colorP = 0x80FF6E00;
			}
			Gui.drawRect(this.px + 1, this.py + 1, this.px + 24, this.py + 24, this.colorP);
			GlStateManager.disableRescaleNormal();
			// Currency Colored
			if (this.getLabel(4) != null && this.getLabel(4).enabled) {
				if (this.money != ClientProxy.playerData.game.getMoney()) {
					this.money = ClientProxy.playerData.game.getMoney();
					if (this.selectDealData.buyMoney > 0) {
						String text = AdditionalMethods.getTextReducedNumber(this.selectDealData.buyMoney, true, true, false) + CustomNpcs.charCurrencies.charAt(0) + " / " + ClientProxy.playerData.game.getTextMoney() + CustomNpcs.charCurrencies.charAt(0);
						if (this.marcet.isLimited) {
							text += " / " + AdditionalMethods.getTextReducedNumber(this.marcet.money, true, true, false) + CustomNpcs.charCurrencies.charAt(0);
						}
						this.getLabel(4).setLabel(text);
					}
				}
				//System.out.println("CNPCs: "+this.money);
				if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
					int color = this.player.capabilities.isCreativeMode ? 0x80FF6E00 : 0x80FF0000;
					if (this.money >= this.selectDealData.deal.getMoney()) { color = 0x8000FF00; }
					Gui.drawRect(this.guiLeft + 138, this.guiTop + 112, this.guiLeft + 218, this.guiTop + 136, color);
				}
			}
			// Items
			if (!this.selectDealData.buyHasPlayerItems.isEmpty()) {
				GlStateManager.pushMatrix();
				this.mc.renderEngine.bindTexture(this.slot);
				int slot = 0;
				for (ItemStack curr : this.selectDealData.buyHasPlayerItems.keySet()) {
					int u = this.px - 10 + (slot % 3) * 18;
					int v = this.py + 38 + (slot / 3) * 18;
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					this.drawTexturedModalRect(u, v, 0, 0, 18, 18);
					if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
						Gui.drawRect(u + 1, v + 1, u + 17, v + 17, this.selectDealData.buyHasPlayerItems.get(curr) ? 0x8000FF00 : this.player.capabilities.isCreativeMode ? 0x80FF6E00 : 0x80FF0000);
					}
					slot++;
				}
				GlStateManager.popMatrix();
				
			}
		}
		if (this.marcet.showXP) {
			this.mc.renderEngine.bindTexture(this.resource);
			GlStateManager.enableBlend();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.drawTexturedModalRect(this.guiLeft + 6, this.guiTop + 139, 234, 0, 22, 76);
			MarkupData md = ClientProxy.playerData.game.getMarkupData(this.marcet.getId());
			double plXP = (double) md.xp;
			double mXP = (double) this.marcet.markup.get(md.level).xp;
			if (plXP > mXP) { plXP = mXP; }
			double h = 74.0d * plXP / mXP;
			int g = (int) h, t = 0;
			if (h > 0.0d && h < 74.0d) { g--; t = 1; }
			if (g > 0) {
				this.drawTexturedModalRect(this.guiLeft + 7, this.guiTop + 214 - g, 235, 151 - g, 20, g);
			}
			if (t==1) {
				GlStateManager.color(0.85f, 0.85f, 0.85f, 1.0f);
				if (g == 0) { g = 1; }
				this.drawTexturedModalRect(this.guiLeft + 8, this.guiTop + 213 - g, 236, 151 - g, 20, 1);
			}
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.disableBlend();
			String lv = AdditionalMethods.instance.deleteColor(new TextComponentTranslation("enchantment.level." + md.level).getFormattedText());
			if (lv.equals("enchantment.level." + md.level)) { lv = "" + md.level; }
			this.mc.fontRenderer.drawString(lv, this.guiLeft + 16, this.guiTop + 205, CustomNpcs.mainColor, true);
		}
		if (this.subgui != null) { return; }
		int u0 = this.guiLeft + 138, u1 = this.guiLeft + this.xSize - 7;
		this.drawHorizontalLine(u0, u1, this.guiTop + 14, 0xA0000000);
		this.drawVerticalLine(u0 - 1, this.guiTop + 14, this.guiTop + 43, 0xA0000000);
		this.drawVerticalLine(u1 + 1, this.guiTop + 14, this.guiTop + 43, 0xA0000000);
		this.drawHorizontalLine(u0, u1, this.guiTop + 43, 0xA0000000);
		if (this.selectDealData!=null && !this.selectDealData.baseItems.isEmpty()) {
			this.drawVerticalLine(u0 - 1, this.guiTop + 43, this.guiTop + 111, 0xA0000000);
			this.drawVerticalLine(u1 + 1, this.guiTop + 43, this.guiTop + 111, 0xA0000000);
		}
		if (this.selectDealData!=null && (!this.selectDealData.baseItems.isEmpty() || this.selectDealData.baseMoney>0)) {
			this.drawHorizontalLine(u0, u1, this.guiTop + 111, 0xA0000000);
		}
		if (this.selectDealData!=null && this.selectDealData.baseMoney>0) {
			this.drawHorizontalLine(u0, u1, this.guiTop + 136, 0xA0000000);
			this.drawVerticalLine(u0 - 1, this.guiTop + 111, this.guiTop + 136, 0xA0000000);
			this.drawVerticalLine(u1 + 1, this.guiTop + 111, this.guiTop + 136, 0xA0000000);
		}
		this.colorP = 0x01000000;
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		ItemStack stack = ItemStack.EMPTY;
		int ct = 0;
		if (this.selectDealData!=null && !this.selectDealData.main.isEmpty()) {
			stack = this.selectDealData.main;
			ct = this.selectDealData.count;
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft, this.guiTop, 50.0f);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.translate(this.px - this.guiLeft, this.py - this.guiTop, 0.0f);
			RenderHelper.enableStandardItemLighting();
			float s = 1.5f;
			GlStateManager.scale(s, s, s);
			this.mc.getRenderItem().renderItemAndEffectIntoGUI(this.selectDealData.main, 0, 0);
			GlStateManager.translate(0.0f, 0.0f, 200.0f);
			this.drawString(this.mc.fontRenderer, "" + this.selectDealData.count, 16 - this.mc.fontRenderer.getStringWidth(""+this.selectDealData.count), 9, 0xFFFFFFFF);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
			if (isMouseHover(i, j, this.guiLeft + 150, this.guiTop + 14, 25, 25)) {
				List<String> list = new ArrayList<String>();
				list.add(new TextComponentTranslation("market.hover.product").getFormattedText());
				list.addAll(this.selectDealData.main.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL));
				this.hoverText = list.toArray(new String[list.size()]);
			}
			if (!this.selectDealData.buyItems.isEmpty()) {
				int slot = 0;
				for (ItemStack curr : this.selectDealData.buyItems.keySet()) {
					int u = this.px - 9 + (slot % 3) * 18;
					int v = this.py + 39 + (slot / 3) * 18;
					GlStateManager.pushMatrix();
					GlStateManager.translate(u, v, 50.0f);
					RenderHelper.enableGUIStandardItemLighting();
					this.mc.getRenderItem().renderItemAndEffectIntoGUI(curr, 0, 0);
					GlStateManager.translate(0.0f, 0.0f, 200.0f);
					int count = this.selectDealData.buyItems.get(curr);
					this.drawString(this.mc.fontRenderer, "" + count, 16 - this.mc.fontRenderer.getStringWidth(""+count), 9, 0xFFFFFFFF);
					RenderHelper.disableStandardItemLighting();
					GlStateManager.popMatrix();
					if (isMouseHover(i, j, u, v, 18, 18)) {
						List<String> list = new ArrayList<String>();
						list.add(new TextComponentTranslation("market.hover.item").getFormattedText());
						list.addAll(curr.getTooltip(this.mc.player, this.mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL));
						this.hoverText = list.toArray(new String[list.size()]);
					}
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					this.drawTexturedModalRect(u, v, 0, 0, 18, 18);
					slot++;
				}
			}
		}
		if (this.getLabel(6) != null && this.getLabel(6).enabled) {
			this.getLabel(6).setLabel(new TextComponentTranslation("market.uptime", new Object[] { AdditionalMethods.ticksToElapsedTime(this.marcet.nextTime / 50, false, false, false) }).getFormattedText());
			if (this.marcet.nextTime <= 0) {
				Client.sendDataDelayCheck(EnumPlayerPacket.MarketTime, this, 2500, this.marcet.getId());
			}
		}
		super.drawScreen(i, j, f);
		if (this.subgui != null) { return; }
		if (this.getLabel(3)!=null && this.getLabel(3).enabled && isMouseHover(i, j, this.guiLeft + 140, this.guiTop + 113, 80, 24)) {
			int buyMoney = this.selectDealData.deal.getMoney();
			if (this.selectDealData!=null && this.selectDealData.buyMoney > 0) {
				buyMoney = (int) this.selectDealData.buyMoney;
			}
			TextComponentBase text = new TextComponentTranslation("market.hover.currency.0", new Object[] { ""+buyMoney, CustomNpcs.charCurrencies.charAt(0), "" + this.money, CustomNpcs.charCurrencies.charAt(0) });
			if (this.marcet.isLimited) {
				text.appendSibling(new TextComponentTranslation("market.hover.currency.1", ""+this.marcet.money));
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
			if (AdditionalMethods.inventoryItemCount(this.player, stack, this.selectDealData.deal.availability, this.selectDealData.deal.getIgnoreDamage(), this.selectDealData.deal.getIgnoreNBT()) < ct) {
				this.colorP = 0x80FF0000;
			}
			TextComponentBase text = new TextComponentTranslation("market.hover.sell.0", stack.getDisplayName());
			if (this.canSell!=0) {
				text.appendSibling(new TextComponentTranslation("market.hover.notsell."+this.canSell));
			} else {
				if (this.selectDealData!=null && !this.selectDealData.sellHasPlayerItems.isEmpty()) {
					if (!this.selectDealData.sellItems.isEmpty()) {
						if (this.selectDealData.sellOneOfEach) { text.appendSibling(new TextComponentTranslation("market.hover.sell.3")); }
						else { text.appendSibling(new TextComponentTranslation("market.hover.sell.1")); }
					}
					for (ItemStack s : this.selectDealData.sellItems.keySet()) {
						text.appendSibling(new TextComponentString("<br>" + s.getDisplayName() + (this.selectDealData.sellOneOfEach ? "" : " x" + (this.selectDealData.sellItems.get(s)))));
					}
				}
				if (this.selectDealData.sellMoney > 0) {
					text.appendSibling(new TextComponentTranslation("market.hover.sell.2", new Object[] { "" + this.selectDealData.sellMoney, CustomNpcs.charCurrencies.charAt(0) }));
				}
			}
			this.setHoverText(text.getFormattedText());
		}
		if (this.marcet.showXP && isMouseHover(i, j, this.guiLeft + 7, this.guiTop + 140, 22, 74)) {
			MarkupData md = ClientProxy.playerData.game.getMarkupData(this.marcet.getId());
			MarkupData mm = this.marcet.markup.get(md.level);
			int pXP = md.xp > mm.xp ? mm.xp : md.xp;
			this.setHoverText(new TextComponentTranslation("market.hover.you.level", ""+(md.level+1), ""+pXP, ""+mm.xp, ""+(int) (mm.buy * 100.0f), ""+(int) (mm.sell * 100.0f)).getFormattedText());
			
		} else if (this.getButton(2) != null && this.getButton(2).visible && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.reset").getFormattedText());
		} else if (this.selectDealData.deal!=null && this.selectDealData.deal.getMaxCount() > 0 && isMouseHover(i, j, this.guiLeft + 177, this.guiTop + 24, 45, 14)) {
			this.setHoverText( this.selectDealData.deal.getMaxCount() > 0 ? new TextComponentTranslation("market.hover.item.amount", new Object[] { "" + this.selectDealData.deal.getAmount() }).getFormattedText() : "");
		} else if (this.getLabel(6)!=null && this.getLabel(6).enabled && this.getLabel(6).hovered) {
			this.setHoverText(new TextComponentTranslation("market.hover.update").getFormattedText());
		}
	}
	@Override
	public void save() {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderMarketRemove, this.marcet.getId());
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (!this.data.containsKey(scroll.getSelected())) { return; }
		this.selectDealData.deal = this.data.get(scroll.getSelected());
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
	}

	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i==200 || i==ClientProxy.frontButton.getKeyCode() || i==208 || i==ClientProxy.backButton.getKeyCode()) {
			int pos = this.scroll.selected + ((i==200 || i==ClientProxy.frontButton.getKeyCode()) ? -1 : 1);
			if (pos < 0 || pos > this.scroll.getList().size()) { return; }
			String sel = this.scroll.getList().get(pos);
			if (!this.data.containsKey(sel)) { return; }
			this.selectDealData.deal = this.data.get(sel);
			this.initGui();
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		IMarcet market = MarcetController.getInstance().getMarcet(this.marcet.getId());
		this.wait = false;
		this.marcet = (Marcet) market;
		((ContainerNPCTrader) this.inventorySlots).marcet = (Marcet) market;
		this.initGui();
	}
	
}
