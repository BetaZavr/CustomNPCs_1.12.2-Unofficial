package noppes.npcs.client.gui.player;

import java.util.*;
import java.util.List;

import net.minecraft.client.gui.Gui;
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
import noppes.npcs.api.handler.data.IDeal;
import noppes.npcs.api.handler.data.IMarcet;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNPCTrader;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.DealMarkup;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.MarcetSection;
import noppes.npcs.controllers.data.MarkupData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class GuiNPCTrader
extends GuiContainerNPCInterface
implements ICustomScrollListener, IGuiData {

	private static final ResourceLocation BUTTONS = new ResourceLocation(CustomNpcs.MODID, "textures/gui/trader_buttons.png");
	private static boolean isIdSort = true;
	private static boolean isSearch = true;
	private static int section = -1;
	private static Marcet marcet;

	private static final Comparator<TempDeal> comparator = (t1, t2) -> {
        if (isIdSort) {
			Map<Integer, Integer> indexMap = new HashMap<>();
			int i = 0;
			for (IDeal iDeal : GuiNPCTrader.marcet.getDeals(GuiNPCTrader.section)) { indexMap.put(iDeal.getId(), i++); }
			return Integer.compare(indexMap.getOrDefault(t1.id, Integer.MAX_VALUE), indexMap.getOrDefault(t2.id, Integer.MAX_VALUE));
        } else {
            return t1.stackName.compareToIgnoreCase(t2.stackName);
        }
    };

	private final Map<String, Deal> data = new TreeMap<>();
	private int px;
	private int py;
	private int canBuy = 0;
	private int canSell = 0;
	private int ceilPos = -1;
	private int colorP = 0x01000000;
	private final ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/trader.png");
	private GuiCustomScroll scroll;
	boolean wait = false;
	private DealMarkup selectDealData;
	private long money = 0L;

	public GuiNPCTrader(EntityNPCInterface npc, ContainerNPCTrader container) {
		super(npc, container);
		ySize = 224;
		xSize = 224;
		title = "role.trader";
		closeOnEsc = true;

		marcet = container.marcet;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() > 4 && button.getID() < 10) {
			section = button.getID() - 5 + ceilPos * 5;
			initGui();
			return;
		}
		switch (button.getID()) {
			case 0: { // buy
				NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderMarketBuy, marcet.getId(), selectDealData.deal.getId(), npc.getEntityId());
				break;
			}
			case 1: { // Sell
				NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderMarketSell, marcet.getId(), selectDealData.deal.getId(), npc.getEntityId());
				break;
			}
			case 2: { // Reset
				NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderMarketReset, marcet.getId(), selectDealData.deal.getId(), npc.getEntityId());
				break;
			}
			case 3: { // up
				if (ceilPos <= 0) { return; }
				ceilPos--;
				initGui();
				return;
			}
			case 4: { // down
				if (ceilPos >= Math.floor((double) marcet.sections.size() / 5.0d)) { return; }
				ceilPos++;
				initGui();
				return;
			}
			case 11: {
				isIdSort = ((GuiNpcCheckBox) button).isSelected();
				initGui();
				return;
			}
			case 12: {
				isSearch = ((GuiNpcCheckBox) button).isSelected();
				initGui();
				return;
			}
		}
		wait = true;
		initGui();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		drawWorldBackground(0);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(resource);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		super.drawGuiContainerBackgroundLayer(f, i, j);
		if (selectDealData != null && !selectDealData.main.isEmpty()) {
			// Main Slot
			GlStateManager.enableAlpha();
			GlStateManager.enableBlend();
			GlStateManager.enableRescaleNormal();
			mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			drawTexturedModalRect(px, py, 0, 0, 17, 17);
			drawTexturedModalRect(px + 17, py, 10, 0, 8, 17);
			drawTexturedModalRect(px, py + 17, 0, 10, 17, 8);
			drawTexturedModalRect(px + 17, py + 17, 10, 10, 8, 8);
			// Main Slot colored
			int redC = 0x80FF0000;
			int orangeC = 0x80FF6E00;
			int greenC = 0x8000FF00;
			if (colorP != redC && selectDealData.deal.getMaxCount() > 0 && selectDealData.deal.getAmount() == 0) { colorP = redC; }
			if (player.capabilities.isCreativeMode && colorP == redC) { colorP = orangeC; }
			Gui.drawRect(px + 1, py + 1, px + 24, py + 24, colorP);
			GlStateManager.disableRescaleNormal();
			// Currency Colored
			if (getLabel(4) != null && getLabel(4).isEnabled()) {
				if (money != ClientProxy.playerData.game.getMoney()) {
					money = ClientProxy.playerData.game.getMoney();
					if (selectDealData.buyMoney > 0) {
						String text = Util.instance.getTextReducedNumber(selectDealData.buyMoney, true, true, false) + CustomNpcs.displayCurrencies + " / " + ClientProxy.playerData.game.getTextMoney() + CustomNpcs.displayCurrencies;
						if (marcet.isLimited) {
							text += " / " + Util.instance.getTextReducedNumber(marcet.money, true, true, false) + CustomNpcs.displayCurrencies;
						}
						getLabel(4).setLabel(text);
					}
				}
				if (getButton(0) != null && getButton(0).isHovered()) {
					int color = player.capabilities.isCreativeMode ? orangeC : redC;
					if (money >= selectDealData.deal.getMoney()) { color = greenC; }
					Gui.drawRect(px - 2, guiTop + 112, guiLeft + 218, guiTop + 136, color);
				}
			}
			// Items
			if (!selectDealData.buyHasPlayerItems.isEmpty()) {
				GlStateManager.pushMatrix();
				mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
				int slot = 0;
				for (ItemStack curr : selectDealData.buyHasPlayerItems.keySet()) {
					int u = px + (slot % 3) * 18;
					int v = py + 38 + (slot / 3) * 18;
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					drawTexturedModalRect(u, v, 0, 0, 18, 18);
					if (getButton(0) != null && getButton(0).isHovered()) {
						Gui.drawRect(u + 1, v + 1, u + 17, v + 17, selectDealData.buyHasPlayerItems.get(curr) ? greenC : player.capabilities.isCreativeMode ? orangeC : redC);
					}
					slot++;
				}
				GlStateManager.popMatrix();

			}
		}
		if (marcet.showXP) {
			mc.getTextureManager().bindTexture(resource);
			GlStateManager.enableBlend();
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			drawTexturedModalRect(guiLeft + 6, guiTop + 139, 234, 0, 22, 76);
			MarkupData md = ClientProxy.playerData.game.getMarkupData(marcet.getId());
			double plXP = md.xp;
			double mXP = marcet.markup.get(md.level).xp;
			if (plXP > mXP) {
				plXP = mXP;
			}
			double h = 74.0d * plXP / mXP;
			int g = (int) h, t = 0;
			if (h > 0.0d && h < 74.0d) {
				g--;
				t = 1;
			}
			if (g > 0) {
				drawTexturedModalRect(guiLeft + 7, guiTop + 214 - g, 235, 151 - g, 20, g);
			}
			if (t == 1) {
				GlStateManager.color(0.85f, 0.85f, 0.85f, 1.0f);
				if (g == 0) {
					g = 1;
				}
				drawTexturedModalRect(guiLeft + 8, guiTop + 213 - g, 236, 151 - g, 20, 1);
			}
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.disableBlend();
			String lv = Util.instance
					.deleteColor(new TextComponentTranslation("enchantment.level." + md.level).getFormattedText());
			if (lv.equals("enchantment.level." + md.level)) {
				lv = "" + md.level;
			}
			mc.fontRenderer.drawString(lv, guiLeft + 16 - (float) mc.fontRenderer.getStringWidth(lv) / 2, guiTop + 205, CustomNpcs.MainColor.getRGB(), true);
		}
		if (subgui != null) {
			return;
		}
		int u0 = px - 2;
		int u1 = guiLeft + xSize - 7;
		int backC = 0xA0000000;
		drawHorizontalLine(u0, u1, guiTop + 14, backC);
		drawVerticalLine(u0 - 1, guiTop + 14, guiTop + 43, backC);
		drawVerticalLine(u1 + 1, guiTop + 14, guiTop + 43, backC);
		drawHorizontalLine(u0, u1, guiTop + 43, backC);
		if (selectDealData != null && !selectDealData.baseItems.isEmpty()) {
			drawVerticalLine(u0 - 1, guiTop + 43, guiTop + 111, backC);
			drawVerticalLine(u1 + 1, guiTop + 43, guiTop + 111, backC);
		}
		if (selectDealData != null && (!selectDealData.baseItems.isEmpty() || selectDealData.baseMoney > 0)) {
			drawHorizontalLine(u0, u1, guiTop + 111, backC);
		}
		if (selectDealData != null && selectDealData.baseMoney > 0) {
			drawHorizontalLine(u0, u1, guiTop + 136, backC);
			drawVerticalLine(u0 - 1, guiTop + 111, guiTop + 136, backC);
			drawVerticalLine(u1 + 1, guiTop + 111, guiTop + 136, backC);
		}
		colorP = 0x01000000;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		ItemStack stack = ItemStack.EMPTY;
		int ct = 0;
		if (selectDealData != null && !selectDealData.main.isEmpty()) {
			stack = selectDealData.main;
			ct = selectDealData.count;

			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft, guiTop, 50.0f);
			GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
			GlStateManager.translate(px - guiLeft, py - guiTop, 0.0f);
			RenderHelper.enableGUIStandardItemLighting();
			float s = 1.5f;
			GlStateManager.scale(s, s, s);
			mc.getRenderItem().renderItemAndEffectIntoGUI(selectDealData.main, 0, 0);
			GlStateManager.translate(0.0f, 0.0f, 200.0f);
			drawString(mc.fontRenderer, "" + selectDealData.count, 16 - mc.fontRenderer.getStringWidth("" + selectDealData.count), 9, 0xFFFFFFFF);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();

			if (isMouseHover(mouseX, mouseY, px, guiTop + 14, 25, 25)) {
				List<String> list = new ArrayList<>();
				list.add(new TextComponentTranslation("market.hover.product").getFormattedText());
				list.addAll(selectDealData.main.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL));
				setHoverText(list);
			}
			if (!selectDealData.buyItems.isEmpty()) {
				int slot = 0;
				for (ItemStack curr : selectDealData.buyItems.keySet()) {
					int u = px + 1 + (slot % 3) * 18;
					int v = py + 39 + (slot / 3) * 18;
					GlStateManager.pushMatrix();
					GlStateManager.translate(u, v, 50.0f);
					mc.getRenderItem().renderItemAndEffectIntoGUI(curr, 0, 0);
					GlStateManager.translate(0.0f, 0.0f, 200.0f);
					int count = selectDealData.buyItems.get(curr);
					drawString(mc.fontRenderer, "" + count, 16 - mc.fontRenderer.getStringWidth("" + count), 9, 0xFFFFFFFF);
					GlStateManager.popMatrix();
					if (isMouseHover(mouseX, mouseY, u, v, 18, 18)) {
						List<String> list = new ArrayList<>();
						list.add(new TextComponentTranslation("market.hover.item").getFormattedText());
						list.addAll(curr.getTooltip(mc.player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL));
						setHoverText(list);
					}
					GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
					drawTexturedModalRect(u, v, 0, 0, 18, 18);
					slot++;
				}
			}
		}
		if (getLabel(6) != null && getLabel(6).isEnabled()) {
			getLabel(6).setLabel(new TextComponentTranslation("market.uptime", Util.instance.ticksToElapsedTime(marcet.nextTime / 50, false, false, false)).getFormattedText());
			if (marcet.nextTime <= 0) {
				NoppesUtilPlayer.sendDataCheckDelay(EnumPlayerPacket.MarketTime, this, 2500, marcet.getId());
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);

		if (subgui != null) { return; }
		if (getLabel(3) != null && getLabel(3).isEnabled() && isMouseHover(mouseX, mouseY, px - 2, guiTop + 113, 80, 24)) {
			int buyMoney = selectDealData.deal.getMoney();
			if (selectDealData != null && selectDealData.buyMoney > 0) { buyMoney = (int) selectDealData.buyMoney; }
			TextComponentBase text = new TextComponentTranslation("market.hover.currency.0", "" + buyMoney, CustomNpcs.displayCurrencies, "" + money, CustomNpcs.displayCurrencies);
			if (marcet.isLimited) { text.appendSibling(new TextComponentTranslation("market.hover.currency.1", "" + marcet.money)); }
			setHoverText(text.getFormattedText());
		} else if (getButton(0) != null && getButton(0).isVisible() && getButton(0).isHovered()) {
			ITextComponent text = new TextComponentTranslation("market.hover.buy", stack.getDisplayName());
			if (canBuy != 0) { text.appendSibling(new TextComponentTranslation("market.hover.notbuy." + canBuy)); }
			setHoverText(text.getFormattedText());
		} else if (getButton(1) != null && getButton(1).isVisible() && getButton(1).isHovered()) {
			colorP = 0x8000FF00;
			if (Util.instance.inventoryItemCount(player, stack, selectDealData.deal.availability, selectDealData.deal.getIgnoreDamage(), selectDealData.deal.getIgnoreNBT()) < ct) { colorP = 0x80FF0000; }
			TextComponentBase text = new TextComponentTranslation("market.hover.sell.0", stack.getDisplayName());
			if (canSell != 0) { text.appendSibling(new TextComponentTranslation("market.hover.notsell." + canSell)); }
			else {
				if (selectDealData != null && !selectDealData.sellHasPlayerItems.isEmpty()) {
					if (!selectDealData.sellItems.isEmpty()) {
						if (selectDealData.sellOneOfEach) { text.appendSibling(new TextComponentTranslation("market.hover.sell.3")); }
						else { text.appendSibling(new TextComponentTranslation("market.hover.sell.1")); }
					}
					for (ItemStack s : selectDealData.sellItems.keySet()) {
						text.appendSibling(new TextComponentString("<br>" + s.getDisplayName() + (selectDealData.sellOneOfEach ? "" : " x" + (selectDealData.sellItems.get(s)))));
					}
				}
				if (selectDealData != null && selectDealData.sellMoney > 0) { text.appendSibling(new TextComponentTranslation("market.hover.sell.2", "" + selectDealData.sellMoney, CustomNpcs.displayCurrencies)); }
			}
			setHoverText(text.getFormattedText());
		}
		if (marcet.showXP && isMouseHover(mouseX, mouseY, guiLeft + 7, guiTop + 140, 22, 74)) {
			MarkupData md = ClientProxy.playerData.game.getMarkupData(marcet.getId());
			MarkupData mm = marcet.markup.get(md.level);
			int pXP = Math.min(md.xp, mm.xp);
			setHoverText("market.hover.you.level", "" + (md.level + 1), "" + pXP, "" + mm.xp, "" + (int) (mm.buy * 100.0f), "" + (int) (mm.sell * 100.0f));
		} else if (getButton(2) != null && getButton(2).isVisible() && getButton(2).isHovered()) {
			setHoverText(new TextComponentTranslation("market.hover.reset").getFormattedText());
		} else if (selectDealData.deal != null && selectDealData.deal.getMaxCount() > 0 && isMouseHover(mouseX, mouseY, guiLeft + 177, guiTop + 24, 45, 14)) {
			setHoverText(selectDealData.deal.getMaxCount() > 0 ? new TextComponentTranslation("market.hover.item.amount", "" + selectDealData.deal.getAmount()).getFormattedText() : "");
		} else if (getLabel(6) != null && getLabel(6).isEnabled() && getLabel(6).isHovered()) {
			setHoverText(new TextComponentTranslation("market.hover.update").getFormattedText());
		}
		drawHoverText(null);
	}

	@Override
	public void initGui() {
		super.initGui();
		data.clear();

		px = guiLeft + 162;
		py = guiTop + 17;

		GuiMenuSideButton tab;
		if (ceilPos < 0) {
			ceilPos = 0;
			section = 0;
		}
		if (marcet.sections.size() > 1) {
			if (marcet.sections.size() > 5) {
				if (ceilPos > 0) {
					tab = new GuiMenuSideButton(3, guiLeft + 1, guiTop + 4, "" + ((char) 708));
					tab.height = 16;
					tab.offsetText = 1;
					addButton(tab);
				}
				if (ceilPos < Math.floor((double) marcet.sections.size() / 5.0d)) {
					tab = new GuiMenuSideButton(4, guiLeft + 1, guiTop + 100, "" + ((char) 709));
					tab.height = 16;
					tab.offsetText = 2;
					addButton(tab);
				}
			}
			for (int i = 0; i < 5 && (i + ceilPos * 5) < marcet.sections.size(); i++) {
				tab = new GuiMenuSideButton(5 + i, guiLeft + 1, guiTop + 20 + i * 16, marcet.sections.get(i + ceilPos * 5).getName());
				tab.data = i + ceilPos * 5;
				tab.height = 16;
				if (i + ceilPos * 5 == section) { tab.active = true; }
				addButton(tab);
			}
		}
		List<TempDeal> selectInTrade = new ArrayList<>();
		List<TempDeal> selectNotTrade = new ArrayList<>();
		int level = ClientProxy.playerData.game.getMarcetLevel(marcet.getId());
		MarcetController mData = MarcetController.getInstance();
		MarcetSection ms = marcet.sections.get(section);
		if (ms != null && !ms.deals.isEmpty()) {
			for (Deal deal : ms.deals) {
				String key = deal.getName();
				while (data.containsKey(key)) { key = ((char)167) + "r" + key; }
				if (deal.getMaxCount() != 0 && deal.getAmount() == 0) { selectNotTrade.add(new TempDeal(deal.getId(), deal.getProduct().getDisplayName(), key)); }
				else {selectInTrade.add(new TempDeal(deal.getId(), deal.getProduct().getDisplayName(), key)); }
				data.put(key, deal);
				if (selectDealData != null && selectDealData.deal != null
						&& selectDealData.deal.getId() == deal.getId()) {
					selectDealData = mData.getBuyData(marcet, deal, level);
				}
			}
		}
		if (scroll == null) { (scroll = new GuiCustomScroll(this, 6)).setSize(154, 123); }
		selectInTrade.sort(comparator);
		selectNotTrade.sort(comparator);
		List<String> sel = new ArrayList<>();
		for (TempDeal td : selectInTrade) { sel.add(td.key); }
		for (TempDeal td : selectNotTrade) { sel.add(td.key); }
		List<ItemStack> stacks = new ArrayList<>();
		List<String> suffixes = new ArrayList<>();
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		int i = 0;
		for (String key : sel) {
			Deal deal = data.get(key);
			DealMarkup dm = mData.getBuyData(marcet, deal, level);
			stacks.add(dm.main);
			List<String> info = new ArrayList<>();
			info.add(new TextComponentTranslation("market.hover.product").getFormattedText());

			if (deal.getMaxCount() > 0) { suffixes.add(((char) 167) + (deal.getAmount() == 0 ? "4" : deal.getAmount() < deal.getProduct().getMaxStackSize() ? "b" : "a") + Util.instance.getTextReducedNumber(deal.getAmount(), true, true, false)); }
			else { suffixes.add(((char) 167) + "a" + new String(Character.toChars(0x221E))); }
			info.add(dm.main.getDisplayName() + " x" + dm.count + " " + (new TextComponentTranslation("market.hover.item." + (deal.getMaxCount() > 0 ? deal.getAmount() == 0 ? "not" : "amount" : "infinitely"), "" + deal.getAmount()).getFormattedText()));
			if (!dm.buyItems.isEmpty()) {
				info.add(new TextComponentTranslation("market.hover.item").getFormattedText());
				for (ItemStack curr : dm.buyItems.keySet()) { info.add(curr.getDisplayName() + " x" + dm.buyItems.get(curr)); }
			}
			if (dm.buyMoney > 0) {
				info.add(new TextComponentTranslation("market.hover.currency").getFormattedText());
				info.add(dm.buyMoney + CustomNpcs.displayCurrencies);
			}
			hts.put(i++, info);
		}
		if (selectDealData == null || selectDealData.deal == null) { selectDealData = mData.getBuyData(marcet, data.get(sel.get(0)), level); }
		selectDealData.check(mc.player.inventory.mainInventory);
		scroll.canSearch(isSearch);
		scroll.setListNotSorted(sel);
		scroll.setStacks(stacks);
		scroll.setHoverTexts(hts);
		scroll.setSuffixes(suffixes);

		scroll.guiLeft = guiLeft + 4;
		scroll.guiTop = guiTop + 14;
		scroll.setSelected(selectDealData.deal.getName());
		title = marcet.getShowName();
		addScroll(scroll);

		int gray = 0xFF202020;
		addLabel(new GuiNpcLabel(1, "market.deals", guiLeft + 4, guiTop + 5));
		GuiNpcLabel label = new GuiNpcLabel(2, "market.barter", px, guiTop + 46);
		label.color = gray;
		label.enabled = !selectDealData.buyItems.isEmpty();
		addLabel(label);
		addLabel(new GuiNpcLabel(3, "market.currency", px, guiTop + 114, gray));
		addLabel(new GuiNpcLabel(4, "", px, guiTop + 126, gray)); // Money
		money = ClientProxy.playerData.game.getMoney();
		if (selectDealData.buyMoney > 0) {
			getLabel(3).setEnabled(true);
			String text = Util.instance.getTextReducedNumber(selectDealData.buyMoney, true, true, false) + CustomNpcs.displayCurrencies + " / " + ClientProxy.playerData.game.getTextMoney() + CustomNpcs.displayCurrencies;
			if (marcet.isLimited) { text += " / " + Util.instance.getTextReducedNumber(marcet.money, true, true, false) + CustomNpcs.displayCurrencies; }
			getLabel(4).setEnabled(true);
			getLabel(4).setLabel(text);
		}
		else {
			getLabel(3).setEnabled(false);
			getLabel(4).setEnabled(false);
		}
		addLabel(new GuiNpcLabel(5, "", px + 27, guiTop + 25, gray)); // amount
		if (selectDealData.deal.getMaxCount() > 0) {
			getLabel(5).setLabel(((char) 167) + (selectDealData.deal.getAmount() == 0 ? "4" : selectDealData.deal.getAmount() < selectDealData.deal.getProduct().getMaxStackSize() ? "1" : "2") + "x" + Util.instance.getTextReducedNumber(selectDealData.deal.getAmount(), true, true, false));
		}
		else {
			getLabel(5).setLabel(new String(Character.toChars(0x221E)));
		}
		if (marcet.updateTime > 0) {
			addLabel(new GuiNpcLabel(6, "", guiLeft + 80, guiTop + 5, gray)); // time
			getLabel(6).setEnabled(marcet.updateTime > 0);
		}
		// buy
		int x = guiLeft + 194;
		int y = guiTop + 139;
		GuiNpcButton button;
		addButton(button = new GuiNpcButton(0, x, y, 12, 18, 0, 0, BUTTONS));
		button.txrW = 24;
		button.txrH = 36;
		button.setIsAnim(true);
		button.setIsVisible(selectDealData.deal.getType() != 1);
		canBuy = 0;
		if (button.isVisible()) {
			if (!player.capabilities.isCreativeMode) {
				if (wait || selectDealData.deal.getType() == 1) { canBuy = 1; }
				if (canBuy == 0 && selectDealData.deal.getAmount() <= 0) { canBuy = 6; }
				if (canBuy == 0 && !selectDealData.deal.availability.isAvailable(player)) { canBuy = 2; }
				if (canBuy == 0 && selectDealData.buyMoney > 0 && money < selectDealData.buyMoney) { canBuy = 3; }
				if (canBuy == 0 && !Util.instance.canRemoveItems(player.inventory.mainInventory, selectDealData.buyItems, selectDealData.ignoreDamage, selectDealData.ignoreNBT)) { canBuy = 4; }
				if (canBuy == 0
						&& !Util.instance.canAddItemAfterRemoveItems(player.inventory.mainInventory,
								selectDealData.main, selectDealData.buyItems,
								selectDealData.ignoreDamage, selectDealData.ignoreNBT)) {
					canBuy = 5;
				}
			}
			button.setEnabled(canBuy == 0);
		}
		// sell
		addButton(button = new GuiNpcButton(1, x + 13, y, 12, 18, 24, 0, BUTTONS));
		button.txrW = 24;
		button.txrH = 36;
		button.setIsAnim(true);
		button.setIsVisible(selectDealData.deal.getType() != 0);
		canSell = 0;
		if (button.isVisible()) {
			if (!player.capabilities.isCreativeMode) {
				if (wait) {
					canSell = 1;
				} else if (!selectDealData.deal.availability.isAvailable(player)) {
					canSell = 2;
				} else {
					Map<ItemStack, Integer> map = new HashMap<>();
					map.put(selectDealData.main, selectDealData.count);
					if (canSell == 0 && !selectDealData.main.isEmpty()
							&& !Util.instance.canRemoveItems(player.inventory.mainInventory, map,
							selectDealData.ignoreDamage, selectDealData.ignoreNBT)) {
						canSell = 3;
					}
					if (canSell == 0 && marcet.isLimited) {
						if (selectDealData.sellMoney > marcet.money) {
							canSell = 4;
						}
						if (canSell == 0 && !selectDealData.sellItems.isEmpty()
								&& !Util.instance.canRemoveItems(marcet.inventory,
										selectDealData.sellItems, selectDealData.ignoreDamage,
										selectDealData.ignoreNBT)) {
							canSell = 5;
						}
					}
					if (canSell == 0 && selectDealData.deal.getMaxCount() == 0 && canBuy == 6
							&& selectDealData.deal.getType() != 1) {
						canSell = 6;
					}
				}
			}
			button.setEnabled(canSell == 0);
		}

		addButton(new GuiNpcButton(2, guiLeft - 66, guiTop + 117, 64, 20, "remote.reset"));
		getButton(2).setIsVisible(player.capabilities.isCreativeMode);

		addButton(button = new GuiNpcCheckBox(11, x, y += 20, 26, 12, "type.id", "N", isIdSort));
		button.setHoverText("hover.sort",
				new TextComponentTranslation("market.deals").getFormattedText(),
				new TextComponentTranslation(isIdSort ? "type.id" : "gui.name").getFormattedText());

		if (sel.size() > 9) {
			addButton(button = new GuiNpcCheckBox(12, x, y + 18, 26, 12, "+", "-", isSearch));
			button.setHoverText("market.hover.is.search");
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i == 200 || i == mc.gameSettings.keyBindForward.getKeyCode() || i == 208 || i == mc.gameSettings.keyBindBack.getKeyCode()) {
			int pos = scroll.getSelect() + ((i == 200 || i == mc.gameSettings.keyBindForward.getKeyCode()) ? -1 : 1);
			if (pos < 0 || pos >= scroll.getList().size()) { return; }
			String sel = scroll.getList().get(pos);
			if (!data.containsKey(sel)) { return; }
			selectDealData.deal = data.get(sel);
			initGui();
		}
	}

	@Override
	public void save() {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.TraderMarketRemove, marcet.getId());
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (!data.containsKey(scroll.getSelected())) {
			return;
		}
		selectDealData.deal = data.get(scroll.getSelected());
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		IMarcet market = MarcetController.getInstance().getMarcet(marcet.getId());
		wait = false;
		marcet = (Marcet) market;
		((ContainerNPCTrader) inventorySlots).marcet = (Marcet) market;
		initGui();
	}

	public static class TempDeal {

		public final int id;
		public final String stackName;
		public final String key;

		public TempDeal(int id, String stackName, String key) {
			this.id = id;
			this.stackName = stackName;
			this.key = key;
		}

	}

}
