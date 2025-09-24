package noppes.npcs.client.gui.global;

import java.util.*;

import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.data.IDeal;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.player.GuiNPCTrader;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.client.util.ResourceData;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.DealMarkup;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.MarcetSection;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.CustomNPCsScheduler;

import javax.annotation.Nonnull;

public class GuiNPCManageMarkets extends GuiNPCInterface2
		implements IGuiData, ICustomScrollListener, GuiYesNoCallback {

	protected static Marcet selectedMarcet;
	protected static Deal selectedDeal;
	public static int marcetId;
	public static int dealId;

	protected final Map<String, Integer> dataDeals = new LinkedHashMap<>();
	protected final Map<String, Integer> dataMarkets = new TreeMap<>();
	protected GuiCustomScroll scrollMarkets;
	protected GuiCustomScroll scrollDeals;
	protected GuiCustomScroll scrollAllDeals;
	protected MarcetController mData;
	private int tabSelect;

	public GuiNPCManageMarkets(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		ySize = 200;
		parentGui = EnumGuiType.MainMenuGlobal;

		mData = MarcetController.getInstance();
		selectedMarcet = mData.getMarcet(marcetId);
		if (selectedMarcet != null) {
			if (selectedMarcet.getSection(dealId) >= 0) {
				IDeal[] deals = selectedMarcet.getAllDeals();
				if (deals.length > 0) { dealId = deals[0].getId(); }
				else { dealId = 0; }
			}
			selectedDeal = mData.getDeal(dealId);
		}
		tabSelect = 0;
		Client.sendData(EnumPacketServer.TraderMarketGet);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				save();
				selectedMarcet = mData.addMarcet();
				marcetId = selectedMarcet.getId();
				initGui();
				CustomNPCsScheduler.runTack(() -> setSubGui(new SubGuiNpcMarketSettings(selectedMarcet)), 50);
				break;
			} // Add market
			case 1: {
				GuiYesNo guiyesno = new GuiYesNo(this, scrollMarkets.getSelected(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			} // Del market
			case 2: {
				setSubGui(new SubGuiNpcMarketSettings(selectedMarcet));
				break;
			} // Market settings
			case 3: {
				save();
				SubGuiNPCManageDeal.parent = this;
				NoppesUtil.requestOpenGUI(EnumGuiType.SetupTraderDeal, marcetId, mData.getUnusedDealId(), 0);
				break;
			} // Add deal
			case 4: {
				if (!dataDeals.containsKey(scrollAllDeals.getSelected())) { return; }
				Client.sendData(EnumPacketServer.TraderMarketDel, -1, dealId);
				dealId = 0;
				break;
			} // Del deal
			case 5: {
				if (dealId < 0) { return; }
				SubGuiNPCManageDeal.parent = this;
				NoppesUtil.requestOpenGUI(EnumGuiType.SetupTraderDeal, marcetId, dealId, 0);
				onClosed();
				break;
			} // Deal settings
			case 6: {
				tabSelect = button.getValue();
				initGui();
				break;
			} // tab / MarcetSection
			case 7: {
				if (selectedMarcet == null || selectedDeal == null) { return; }
				int tab = selectedMarcet.getSection(selectedDeal.getId());
				if (tab == tabSelect) { return; }
				selectedMarcet.sections.get(tabSelect).addDeal(selectedDeal.getId());
				setGuiData(null);
				initGui();
				break;
			} // <
			case 8: {
				if (!dataDeals.containsKey(scrollDeals.getSelected())) { return; }
				int id = dataDeals.get(scrollDeals.getSelected());
				selectedMarcet.sections.get(tabSelect).removeDeal(id);
				setGuiData(null);
				initGui();
				break;
			} // >
			case 9: {
				if (dataDeals.isEmpty()) { return; }
				for (int id : dataDeals.values()) { selectedMarcet.sections.get(tabSelect).addDeal(id); }
				setGuiData(null);
				initGui();
				break;
			} // <<
			case 10: {
				if (scrollDeals.getList().isEmpty()) { return; }
				selectedMarcet.sections.get(tabSelect).removeAllDeals();
				setGuiData(null);
				initGui();
				break;
			} // >>
			case 11: {
				if (selectedMarcet == null || !dataDeals.containsKey(scrollDeals.getSelected())) { return; }
				int pos = scrollDeals.getSelect();
				Collections.swap(selectedMarcet.sections.get(tabSelect).deals, pos, pos - 1);
				scrollDeals.setSelect(pos - 1);
				setGuiData(null);
				break;
			} // up
			case 12: {
				if (selectedMarcet == null || !dataDeals.containsKey(scrollDeals.getSelected())) { return; }
				int pos = scrollDeals.getSelect();
				Collections.swap(selectedMarcet.sections.get(tabSelect).deals, pos, pos + 1);
				scrollDeals.setSelect(pos + 1);
				setGuiData(null);
				break;
			} // down
		}
	}

	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI(player, this);
		if (!result) { return; }
        if (id == 0) {
            if (selectedMarcet == null) { return; }
            Client.sendData(EnumPacketServer.TraderMarketDel, marcetId, -1);
            marcetId = 0;
        }
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		int u = 0;
		int v = 0;
		if (selectedMarcet != null && selectedMarcet.sections.containsKey(tabSelect)) {
			int icon = selectedMarcet.sections.get(tabSelect).getIcon();
			u = (icon % 10) * 24;
			v = (int) Math.floor((float) icon / 10.0f) * 72;
		}
		mc.getTextureManager().bindTexture(GuiNPCTrader.ICONS);
		drawTexturedModalRect(guiLeft + 252, guiTop + 189, u, v, 24, 24);
	}

	@Override
	public void initGui() {
		super.initGui();
		mData = MarcetController.getInstance();
		int w = 120, h = ySize - 24;
		if (scrollMarkets == null) { scrollMarkets = new GuiCustomScroll(this, 0).setSize(w, h); }
		if (scrollDeals == null) { scrollDeals = new GuiCustomScroll(this, 1).setSize(w, h); }
		if (scrollAllDeals == null) { scrollAllDeals = new GuiCustomScroll(this, 2).setSize(w, h); }
		int x0 = guiLeft + 5;
		int x1 = x0 + w + 5;
		int x2 = x1 + w + 45;
		int y = guiTop + 14;
		// Markets:
		LinkedHashMap<Integer, List<String>> htsM = new LinkedHashMap<>();
		if (!dataMarkets.isEmpty()) {
			int i = 0;
			for (int id : dataMarkets.values()) {
				Marcet marcet = mData.getMarcet(id);
				List<String> info = new ArrayList<>();
				info.add(((char) 167) + "7ID: " + ((char) 167) + "r" + marcet.getId());
				info.add(((char) 167) + "7" + new TextComponentTranslation("gui.name").getFormattedText() + ((char) 167) + "7: " + ((char) 167) + "r" + marcet.name);
				if (!marcet.isValid()) {
					info.add(new TextComponentTranslation("market.hover.nv.market").getFormattedText());
					for (MarcetSection ms : selectedMarcet.sections.values()) {
						for (Deal deal : ms.deals) {
							if (deal.isValid()) { continue; }
							if (deal.getProduct().getMCItemStack() == null || deal.getProduct().getMCItemStack().getItem() == Items.AIR) {
								info.add(new TextComponentTranslation("market.hover.nv.market.deal.0", "" + deal.getId()).getFormattedText());
							}
							if (deal.getMoney() == 0 && deal.getCurrency().isEmpty()) {
								info.add(new TextComponentTranslation("market.hover.nv.market.deal.1", "" + deal.getId()).getFormattedText());
							}
						}
					}
					if (info.size() > 9) {
						info.add("...");
						break;
					}
				}
				htsM.put(i, info);
				i++;
			}
		}
		scrollMarkets.setUnsortedList(new ArrayList<>(dataMarkets.keySet()))
				.setHoverTexts(htsM)
				.setSelected(selectedMarcet.getSettingName());
		// Deals:
		if (!dataDeals.isEmpty()) {
			List<String> allDeals = new ArrayList<>(dataDeals.keySet());
			LinkedHashMap<Integer, List<String>> htsAD = new LinkedHashMap<>();
			List<ResourceData> allPrefixes = new ArrayList<>();
			List<ItemStack> stacks = new ArrayList<>();
			Map<Integer, TempDealInfo> map = new TreeMap<>();
			int i = 0;
			for (String key : dataDeals.keySet()) {
				int dealID = dataDeals.get(key);
				Deal deal = mData.getDeal(dealID);
				List<String> totalInfo = new ArrayList<>();
				totalInfo.add(((char) 167) + "7ID: " + ((char) 167) + "r" + dealID);
				List<String> marcetInfo = new ArrayList<>();
				marcetInfo.add(((char) 167) + "7ID: " + ((char) 167) + "r" + dealID);
				DealMarkup dm = new DealMarkup();
				if (deal != null) { dm.set(deal); }
				ItemStack stack = ItemStack.EMPTY;
				int tab = selectedMarcet.getSection(dealID);
				if (deal == null || !deal.isValid()) {
					totalInfo.add(new TextComponentTranslation("market.hover.nv.deal").getFormattedText());
					if (deal == null) { totalInfo.add(new TextComponentTranslation("hover.total.error").getFormattedText()); }
					else {
						stack = dm.main;
						if (dm.main == null || dm.main.getItem() == Items.AIR) { totalInfo.add(new TextComponentTranslation("market.hover.nv.deal.product").getFormattedText()); }
						if (dm.baseMoney == 0 && dm.baseItems.isEmpty()) { totalInfo.add(new TextComponentTranslation("market.hover.nv.deal.barter").getFormattedText()); }
					}
				}
				else {
					String section;
					if (tab == tabSelect) {
						section = "\"" + (new TextComponentTranslation(selectedMarcet.sections.get(tab).name).getFormattedText()) + "\"";
						marcetInfo.add(((char) 167) + "7" + new TextComponentTranslation("gui.sections").getFormattedText()
										+ " ID: " + ((char) 167) + "r" + tab + ((char) 167) + "7; "
										+ new TextComponentTranslation("gui.name").getFormattedText() + ((char) 167)
										+ "7: " + ((char) 167) + "r" + section);
					}
					stack = dm.main;
					totalInfo.add(new TextComponentTranslation("market.hover.product").getFormattedText());
					totalInfo.add(dm.main.getDisplayName() + " x" + dm.count);
					marcetInfo.add(new TextComponentTranslation("market.hover.product").getFormattedText());
					marcetInfo.add(dm.main.getDisplayName() + " x" + dm.count + (deal.getMaxCount() > 0 ? " " + new TextComponentTranslation("market.hover.item.amount", "" + deal.getAmount()).getFormattedText() : ""));
					if (!dm.baseItems.isEmpty()) {
						totalInfo.add(new TextComponentTranslation("market.hover.item").getFormattedText());
						for (ItemStack curr : dm.baseItems.keySet()) { totalInfo.add(curr.getDisplayName() + " x" + dm.baseItems.get(curr)); }
					}
					if (dm.baseMoney > 0) {
						totalInfo.add(new TextComponentTranslation("market.hover.currency").getFormattedText());
						totalInfo.add(dm.baseMoney + CustomNpcs.displayCurrencies);
					}
					totalInfo.add(((char) 167) + "e" + (new TextComponentTranslation("market.deal.type." + dm.deal.getType()).getFormattedText()));
					totalInfo.add(((char) 167) + "6" + (new TextComponentTranslation("drop.chance").getFormattedText() + ((char) 167) + "6: " + ((char) 167) + "r" + dm.deal.getChance() + "%"));
				}
				if (tabSelect == tab) {
					map.put(dealID, new TempDealInfo(key, stack, marcetInfo));
					allDeals.remove(key);
				}
				else {
					htsAD.put(i++, totalInfo);
					stacks.add(stack);
					if (deal != null && deal.isCase()) {
						ResourceLocation objCase = deal.getCaseObjModel();
						if (objCase != null) {
							try { mc.getResourceManager().getResource(objCase); }
							catch (Exception e) { objCase = null; }
						}
						if (objCase == null) {
							try {
								mc.getResourceManager().getResource(Deal.defaultCaseOBJ);
								objCase = Deal.defaultCaseOBJ;
							} catch (Exception ignored) { }
						}
						if (objCase == null) { allPrefixes.add(ResourceData.EMPTY); }
						else {
							ResourceData rd = new ResourceData(objCase, 0, 0, 1, 1);
							rd.rotateX = -15.0f;
							rd.rotateY = -75.0f;
							rd.tH = -2.0f;
							rd.scaleX = rd.scaleY = rd.scaleZ = 8.0f;
							allPrefixes.add(rd);
						}
					}
					else { allPrefixes.add(ResourceData.EMPTY); }
				}
			}
			int j = 0;
			List<String> marcetDeals = new ArrayList<>();
			List<ItemStack> marcetStacks = new ArrayList<>();
			List<ResourceData> marcetPrefixes = new ArrayList<>();
			LinkedHashMap<Integer, List<String>> htsD = new LinkedHashMap<>();
			for (IDeal deal : selectedMarcet.getDeals(tabSelect)) {
				if (map.containsKey(deal.getId())) {
					TempDealInfo tdi = map.get(deal.getId());
					marcetDeals.add(tdi.key);
					marcetStacks.add(tdi.stack);
					if (deal.isCase()) {
						ResourceLocation objCase = deal.getCaseObjModel();
						if (objCase != null) {
							try { mc.getResourceManager().getResource(objCase); }
							catch (Exception e) { objCase = null; }
						}
						if (objCase == null) {
							try {
								mc.getResourceManager().getResource(Deal.defaultCaseOBJ);
								objCase = Deal.defaultCaseOBJ;
							} catch (Exception ignored) { }
						}
						if (objCase == null) { marcetPrefixes.add(ResourceData.EMPTY); }
						else {
							ResourceData rd = new ResourceData(objCase, 0, 0, 1, 1);
							rd.rotateX = -15.0f;
							rd.rotateY = -75.0f;
							rd.tH = -2.0f;
							rd.scaleX = rd.scaleY = rd.scaleZ = 8.0f;
							marcetPrefixes.add(rd);
						}
					} else { marcetPrefixes.add(ResourceData.EMPTY); }
					htsD.put(j++, tdi.marcetInfo);
				}
			}
			scrollAllDeals.setUnsortedList(allDeals)
					.setHoverTexts(htsAD)
					.setStacks(stacks)
					.setPrefixes(allPrefixes);
			scrollDeals.setUnsortedList(marcetDeals)
					.setStacks(marcetStacks)
					.setHoverTexts(htsD)
					.setPrefixes(marcetPrefixes);
		}
		if (selectedDeal != null) { scrollAllDeals.setSelected(selectedDeal.getSettingName()); }
		scrollMarkets.guiLeft = x0;
		scrollMarkets.guiTop = y;
		scrollDeals.guiLeft = x1;
		scrollDeals.guiTop = y;
		scrollAllDeals.guiLeft = x2;
		scrollAllDeals.guiTop = y;
		addScroll(scrollMarkets);
		addScroll(scrollDeals);
		addScroll(scrollAllDeals);
		scrollMarkets.resetRoll();
		scrollDeals.resetRoll();
		scrollAllDeals.resetRoll();

		int lId = 0;
		// market keys
		addLabel(new GuiNpcLabel(lId++, "global.market", x0 + 2, y - 9)
				.setHoverText("market.hover.names"));
		// market deals keys
		addLabel(new GuiNpcLabel(lId++, "market.deals", x1, y - 9)
				.setHoverText("market.hover.deals"));
		// all deals keys
		addLabel(new GuiNpcLabel(lId, "market.all.deals", x2, y - 9)
				.setHoverText("market.hover.all.deals"));
		y += h + 2;
		int bw = (w - 2) / 3;
		// add market
		addButton(new GuiNpcButton(0, x0, y, bw, 20, "gui.add")
				.setHoverText("market.hover.market.add"));
		// del market
		addButton(new GuiNpcButton(1, x0 + 2 + bw, y, bw, 20, "gui.remove")
				.setIsEnable(marcetId > 0 && selectedMarcet != null && mData.markets.size() > 1)
				.setHoverText("market.hover.market.del"));
		// edit market
		addButton(new GuiNpcButton(2, x0 + (2 + bw) * 2, y, bw, 20, "selectServer.edit")
				.setIsEnable(selectedMarcet != null)
				.setHoverText("market.hover.market.settings"));
		// add deal
		addButton(new GuiNpcButton(3, x2, y, bw, 20, "gui.add")
				.setHoverText("market.hover.deal.add"));
		// del deal
		addButton(new GuiNpcButton(4, x2 + 2 + bw, y, bw, 20, "gui.remove")
				.setIsEnable(dealId >= 0 && selectedDeal != null && dataDeals.size() > 1)
				.setHoverText("market.hover.deal.del"));
		// edit deal
		addButton(new GuiNpcButton(5, x2 + (2 + bw) * 2, y, bw, 20, "selectServer.edit")
				.setIsEnable(selectedDeal != null)
				.setHoverText("market.hover.deal.settings"));
		// market tabs
		String[] tabs = new String[selectedMarcet.sections.size()];
		int i = 0;
		for (MarcetSection tab : selectedMarcet.sections.values()) {
			tabs[i] = new TextComponentTranslation(tab.name).getFormattedText();
			i++;
		}
		addButton(new GuiButtonBiDirectional(6, x1, y, w, 20, tabs, tabSelect)
				.setHoverText("market.hover.section"));
		// work buttons
		int x3 = x2 - 43;
		y = guiTop + 60;
		// add
		addButton(new GuiNpcButton(7, x3, y, 41, 20, "<")
				.setIsEnable(selectedMarcet != null && selectedMarcet.getDeal(dealId) == null && scrollAllDeals.hasSelected() && selectedDeal != null && selectedDeal.isValid())
				.setHoverText("market.hover.add.deal"));
		// del
		addButton(new GuiNpcButton(8, x3, y += 22, 41, 20, ">")
				.setHoverText("market.hover.del.deal")
				.setIsEnable(scrollDeals.hasSelected()));
		// add all
		addButton(new GuiNpcButton(9, x3, y += 22, 41, 20, "<<")
				.setIsEnable(!dataDeals.isEmpty())
				.setHoverText("market.hover.add.deals"));
		// del all
		addButton(new GuiNpcButton(10, x3, y + 22, 41, 20, ">>")
				.setIsEnable(!scrollDeals.getList().isEmpty())
				.setHoverText("market.hover.del.deals"));
		// up
		addButton(new GuiNpcButton(11, x3, y += 28, 41, 20, "↑ " + new TextComponentTranslation("gui.up").getFormattedText())
				.setIsEnable(scrollDeals.getSelect() > 0)
				.setHoverText("hover.up"));
		// down
		addButton(new GuiNpcButton(12, x3, y + 22, 41, 20, "↓ " + new TextComponentTranslation("gui.down").getFormattedText())
				.setIsEnable(scrollDeals.getSelect() >= 0 && scrollDeals.getSelect() < scrollDeals.getList().size() - 1)
				.setHoverText("hover.down"));
	}

	@Override
	public void save() {
		if (selectedMarcet == null) { return; }
		selectedMarcet.resetAllDeals();
		Client.sendData(EnumPacketServer.TraderMarketSave, selectedMarcet.save());
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		try {
			switch (scroll.getID()) {
				case 0: {
					if (!dataMarkets.containsKey(scroll.getSelected())) { return; }
					selectedMarcet = mData.getMarcet(dataMarkets.get(scroll.getSelected()));
					marcetId = selectedMarcet.getId();
					initGui();
					break;
				} // Markets
				case 1: // Deals
				case 2: {
					if (!dataDeals.containsKey(scroll.getSelected())) { return; }
					selectedDeal = mData.getDeal(dataDeals.get(scroll.getSelected()));
					if (selectedDeal != null) { dealId = selectedDeal.getId(); }
					if (scroll.id == 1) { scrollAllDeals.setSelect(-1); }
					else { scrollDeals.setSelect(-1); }
					initGui();
					break;
				} // All Deals
			}
		} catch (Exception e) { LogWriter.error(e); }
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		switch (scroll.getID()) {
			case 0: {
				if (selectedMarcet == null) { return; }
				setSubGui(new SubGuiNpcMarketSettings(selectedMarcet));
				break;
			} // Markets
			case 1: // Deals
			case 2: {
				if (selectedMarcet == null || !dataDeals.containsKey(scroll.getSelected())) { return; }
				save();
				SubGuiNPCManageDeal.parent = this;
				NoppesUtil.requestOpenGUI(EnumGuiType.SetupTraderDeal, marcetId, dealId, 0);
				break;
			} // All Deals
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		mData = MarcetController.getInstance();
		dataMarkets.clear();
		for (Marcet m : mData.markets.values()) {
			dataMarkets.put(m.getSettingName(), m.getId());
			if (marcetId < 0 || marcetId == m.getId() || selectedMarcet == null) {
				selectedMarcet = m;
				marcetId = m.getId();
			}
		}
		dataDeals.clear();
		for (Deal d : mData.deals.values()) {
			dataDeals.put(d.getSettingName(), d.getId());
			if (dealId < 0 || dealId == d.getId() || selectedDeal == null) {
				selectedDeal = d;
				dealId = d.getId();
			}
		}
		initGui();
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcMarketSettings) { setGuiData(null); }
		NoppesUtil.openGUI(player, this);
	}

	public static class TempDealInfo {

		public final String key;
		public final ItemStack stack;
		public final List<String> marcetInfo;

		public TempDealInfo(String keyIn, ItemStack stackIn, List<String> marcetInfoIn) {
			key = keyIn;
			stack = stackIn;
			marcetInfo = marcetInfoIn;
		}

	}

}
