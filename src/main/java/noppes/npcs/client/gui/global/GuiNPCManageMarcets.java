package noppes.npcs.client.gui.global;

import java.util.*;

import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.data.IDeal;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiNpcMarketSettings;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.DealMarkup;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.MarcetSection;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.CustomNPCsScheduler;

public class GuiNPCManageMarcets extends GuiNPCInterface2 implements IGuiData, ICustomScrollListener, GuiYesNoCallback, ISubGuiListener {

	public static int marcetId, dealId;
	private static Marcet selectedMarcet;
	private static Deal selectedDeal;

	private final Map<String, Integer> dataDeals = new LinkedHashMap<>();
	private final Map<String, Integer> dataMarkets = new TreeMap<>();
	private GuiCustomScroll scrollMarkets;
	private GuiCustomScroll scrollDeals;
	private GuiCustomScroll scrollAllDeals;
	private MarcetController mData;
	private int tabSelect;

	public GuiNPCManageMarcets(EntityNPCInterface npc) {
		super(npc);
		mData = MarcetController.getInstance();
		selectedMarcet = (Marcet) mData.getMarcet(marcetId);
		if (selectedMarcet != null) {
			if (selectedMarcet.getSection(dealId) >= 0) {
				IDeal[] deals = selectedMarcet.getAllDeals();
				if (deals.length > 0) {
					dealId = deals[0].getId();
				} else {
					dealId = 0;
				}
			}
			selectedDeal = (Deal) mData.getDeal(dealId);
		}
		tabSelect = 0;
		ySize = 200;
		Client.sendData(EnumPacketServer.TraderMarketGet);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 0: { // Add market
				save();
				selectedMarcet = (Marcet) mData.addMarcet();
				marcetId = selectedMarcet.getId();
				initGui();
				CustomNPCsScheduler.runTack(() -> setSubGui(new SubGuiNpcMarketSettings(selectedMarcet)), 50);
				break;
			}
			case 1: { // Del market
				GuiYesNo guiyesno = new GuiYesNo(this, scrollMarkets.getSelected(),
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			}
			case 2: { // Market settings
				setSubGui(new SubGuiNpcMarketSettings(selectedMarcet));
				break;
			}
			case 3: { // Add deal
				save();
				SubGuiNPCManageDeal.parent = this;
				NoppesUtil.requestOpenGUI(EnumGuiType.SetupTraderDeal, marcetId, mData.getUnusedDealId(), 0);
				break;
			}
			case 4: { // Del deal
				GuiYesNo guiyesno = new GuiYesNo(this, scrollDeals.getSelected(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 1);
				displayGuiScreen(guiyesno);
				break;
			}
			case 5: { // Deal settings
				if (scrollAllDeals.selected < 0 || dealId < 0) {
					return;
				}
				SubGuiNPCManageDeal.parent = this;
				NoppesUtil.requestOpenGUI(EnumGuiType.SetupTraderDeal, marcetId, dealId, 0);
				close();
				break;
			}
			case 6: { // tab
				tabSelect = button.getValue();
				initGui();
				break;
			}
			case 7: { // <
				if (selectedMarcet == null || selectedDeal == null) {
					return;
				}
				int tab = selectedMarcet.getSection(selectedDeal.getId());
				if (tab == tabSelect) {
					return;
				}
				selectedMarcet.sections.get(tabSelect).addDeal(selectedDeal.getId());
				setGuiData(null);
				initGui();
				break;
			}
			case 8: { // >
				if (!dataDeals.containsKey(scrollDeals.getSelected())) {
					return;
				}
				int id = dataDeals.get(scrollDeals.getSelected());
				selectedMarcet.sections.get(tabSelect).removeDeal(id);
				setGuiData(null);
				initGui();
				break;
			}
			case 9: { // <<
				if (dataDeals.isEmpty()) {
					return;
				}
				for (int id : dataDeals.values()) {
					selectedMarcet.sections.get(tabSelect).addDeal(id);
				}
				setGuiData(null);
				initGui();
				break;
			}
			case 10: { // >>
				if (scrollDeals.getList().isEmpty()) {
					return;
				}
				selectedMarcet.sections.get(tabSelect).removeAllDeals();
				setGuiData(null);
				initGui();
				break;
			}
		}
	}

	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI(player, this);
		if (!result) {
			return;
		}
		switch (id) {
		case 0: {
			if (selectedMarcet == null) {
				return;
			}
			Client.sendData(EnumPacketServer.TraderMarketDel, marcetId, -1);
			marcetId = 0;
			break;
		}
		case 1: {
			if (selectedMarcet == null || dataDeals.containsKey(scrollDeals.getSelected())) {
				return;
			}
			Client.sendData(EnumPacketServer.TraderMarketDel, -1, dealId);
			dealId = 0;
			break;
		}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (subgui != null || !CustomNpcs.ShowDescriptions) {
			return;
		}
		// Labels
		if (getLabel(0) != null && getLabel(0).hovered) {
			setHoverText(new TextComponentTranslation("market.hover.names").getFormattedText());
		} else if (getLabel(1) != null && getLabel(1).hovered) {
			setHoverText(new TextComponentTranslation("market.hover.deals").getFormattedText());
		} else if (getLabel(2) != null && getLabel(2).hovered) {
			setHoverText(new TextComponentTranslation("market.hover.all.deals").getFormattedText());
		} else if (getButton(0) != null && getButton(0).isMouseOver()) {
			setHoverText(new TextComponentTranslation("market.hover.market.add").getFormattedText());
		} else if (getButton(1) != null && getButton(1).isMouseOver()) {
			setHoverText(new TextComponentTranslation("market.hover.market.del").getFormattedText());
		} else if (getButton(2) != null && getButton(2).isMouseOver()) {
			setHoverText(new TextComponentTranslation("market.hover.market.settings").getFormattedText());
		} else if (getButton(3) != null && getButton(3).isMouseOver()) {
			setHoverText(new TextComponentTranslation("market.hover.deal.add").getFormattedText());
		} else if (getButton(4) != null && getButton(4).isMouseOver()) {
			setHoverText(new TextComponentTranslation("market.hover.deal.del").getFormattedText());
		} else if (getButton(5) != null && getButton(5).isMouseOver()) {
			setHoverText(new TextComponentTranslation("market.hover.deal.settings").getFormattedText());
		} else if (getButton(6) != null && getButton(6).isMouseOver()) {
			setHoverText(new TextComponentTranslation("market.hover.section").getFormattedText());
		} else if (getButton(7) != null && getButton(7).isMouseOver()) {
			setHoverText(new TextComponentTranslation("market.hover.add.deal").getFormattedText());
		} else if (getButton(8) != null && getButton(8).isMouseOver()) {
			setHoverText(new TextComponentTranslation("market.hover.del.deal").getFormattedText());
		} else if (getButton(9) != null && getButton(9).isMouseOver()) {
			setHoverText(new TextComponentTranslation("market.hover.add.deals").getFormattedText());
		} else if (getButton(10) != null && getButton(10).isMouseOver()) {
			setHoverText(new TextComponentTranslation("market.hover.del.deals").getFormattedText());
		}
		if (hoverText != null) {
			drawHoveringText(Arrays.asList(hoverText), mouseX, mouseY, fontRenderer);
			hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		mData = MarcetController.getInstance();
		int w = 120, h = ySize - 24;
		if (scrollMarkets == null) {
			(scrollMarkets = new GuiCustomScroll(this, 0)).setSize(w, h);
		}
		if (scrollDeals == null) {
			(scrollDeals = new GuiCustomScroll(this, 1)).setSize(w, h);
		}
		if (scrollAllDeals == null) {
			(scrollAllDeals = new GuiCustomScroll(this, 2)).setSize(w, h);
		}

		int x0 = guiLeft + 5, x1 = x0 + w + 5, x2 = x1 + w + 45, y = guiTop + 14;
		// Marcets:
		scrollMarkets.setListNotSorted(new ArrayList<>(dataMarkets.keySet()));
		if (!dataMarkets.isEmpty()) {
			List<String[]> infoList = new ArrayList<>();
			for (int id : dataMarkets.values()) {
				Marcet marcet = (Marcet) mData.getMarcet(id);
				List<String> info = new ArrayList<>();
				info.add(((char) 167) + "7ID: " + ((char) 167) + "r" + marcet.getId());
				info.add(((char) 167) + "7" + new TextComponentTranslation("gui.name").getFormattedText() + ((char) 167) + "7: " + ((char) 167) + "r" + marcet.name);
				if (!marcet.isValid()) {
					info.add(new TextComponentTranslation("market.hover.nv.market").getFormattedText());
					for (MarcetSection ms : selectedMarcet.sections.values()) {
						for (Deal deal : ms.deals) {
							if (deal.isValid()) {
								continue;
							}
							if (deal.getProduct().getMCItemStack() == null
									|| deal.getProduct().getMCItemStack().getItem() == Items.AIR) {
								info.add(
										new TextComponentTranslation("market.hover.nv.market.deal.0", "" + deal.getId())
												.getFormattedText());
							}
							if (deal.getMoney() == 0 && deal.getCurrency().isEmpty()) {
								info.add(
										new TextComponentTranslation("market.hover.nv.market.deal.1", "" + deal.getId())
												.getFormattedText());
							}
						}
					}
					if (info.size() > 9) {
						info.add("...");
						break;
					}
				}
				infoList.add(info.toArray(new String[0]));
			}
			scrollMarkets.hoversTexts = infoList.toArray(new String[infoList.size()][1]);
		} else {
			scrollMarkets.hoversTexts = null;
		}
		scrollMarkets.setSelected(selectedMarcet.getSettingName());

		// Deals:
		scrollAllDeals.setListNotSorted(new ArrayList<>(dataDeals.keySet()));
		if (!dataDeals.isEmpty()) {
			List<String[]> infoList = new ArrayList<>();
			List<ItemStack> stacks = new ArrayList<>();

			List<String> marcetDeals = new ArrayList<>();
			List<String[]> marcetInfoList = new ArrayList<>();
			List<ItemStack> marcetStacks = new ArrayList<>();
			for (String key : dataDeals.keySet()) {
				int dealID = dataDeals.get(key);
				Deal deal = (Deal) mData.getDeal(dealID);
				List<String> totalInfo = new ArrayList<>();
				totalInfo.add(((char) 167) + "7ID: " + ((char) 167) + "r" + dealID);
				List<String> marcetInfo = new ArrayList<>();
				marcetInfo.add(((char) 167) + "7ID: " + ((char) 167) + "r" + dealID);
				DealMarkup dm = new DealMarkup();
				if (deal != null) {
					dm.set(deal);
				}
				ItemStack stack = ItemStack.EMPTY;
				int tab = selectedMarcet.getSection(dealID);
				if (deal == null || !deal.isValid()) {
					totalInfo.add(new TextComponentTranslation("market.hover.nv.deal").getFormattedText());
					if (deal == null) {
						totalInfo.add(new TextComponentTranslation("hover.total.error").getFormattedText());
					} else {
						stack = dm.main;
						if (dm.main == null || dm.main.getItem() == Items.AIR) {
							totalInfo.add(
									new TextComponentTranslation("market.hover.nv.deal.product").getFormattedText());
						}
						if (dm.baseMoney == 0 && dm.baseItems.isEmpty()) {
							totalInfo.add(
									new TextComponentTranslation("market.hover.nv.deal.barter").getFormattedText());
						}
					}
				} else {
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
					marcetInfo.add(dm.main.getDisplayName() + " x" + dm.count
							+ (deal.getMaxCount() > 0 ? " " + new TextComponentTranslation("market.hover.item.amount", "" + deal.getAmount()).getFormattedText() : ""));
					if (!dm.baseItems.isEmpty()) {
						totalInfo.add(new TextComponentTranslation("market.hover.item").getFormattedText());
						for (ItemStack curr : dm.baseItems.keySet()) {
							totalInfo.add(curr.getDisplayName() + " x" + dm.baseItems.get(curr));
						}
					}
					if (dm.baseMoney > 0) {
						totalInfo.add(new TextComponentTranslation("market.hover.currency").getFormattedText());
						totalInfo.add(dm.baseMoney + CustomNpcs.displayCurrencies);
					}
					totalInfo.add(((char) 167) + "e" + (new TextComponentTranslation("market.deal.type." + dm.deal.getType()).getFormattedText()));
					totalInfo.add(((char) 167) + "6" + (new TextComponentTranslation("drop.chance").getFormattedText() + ((char) 167) + "6: " + ((char) 167) + "r" + dm.deal.getChance() + "%"));
				}
				infoList.add(totalInfo.toArray(new String[0]));
				marcetInfoList.add(marcetInfo.toArray(new String[0]));
				stacks.add(stack);
				if (tabSelect == tab) {
					marcetDeals.add(key);
					marcetStacks.add(stack);
				}
			}
			scrollAllDeals.hoversTexts = infoList.toArray(new String[infoList.size()][1]);
			scrollAllDeals.setStacks(stacks);

			scrollDeals.hoversTexts = marcetInfoList.toArray(new String[marcetInfoList.size()][1]);
			scrollDeals.setStacks(marcetStacks);
			scrollDeals.setListNotSorted(marcetDeals);
		}
		if (selectedDeal != null) {
			scrollAllDeals.setSelected(selectedDeal.getSettingName());
		}

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
		addLabel(new GuiNpcLabel(lId++, "global.market", x0 + 2, y - 9));
		addLabel(new GuiNpcLabel(lId++, "market.deals", x1, y - 9));
		addLabel(new GuiNpcLabel(lId, "market.all.deals", x2, y - 9));

		y += scrollMarkets.height + 2;
		int bw = (w - 2) / 3;
		addButton(new GuiNpcButton(0, x0, y, bw, 20, "gui.add"));
		addButton(new GuiNpcButton(1, x0 + 2 + bw, y, bw, 20, "gui.remove"));
		getButton(1).setEnabled(marcetId > 0 && selectedMarcet != null && mData.markets.size() > 1);
		addButton(new GuiNpcButton(2, x0 + (2 + bw) * 2, y, bw, 20, "selectServer.edit"));
		getButton(2).setEnabled(selectedMarcet != null);

		addButton(new GuiNpcButton(3, x2, y, bw, 20, "gui.add"));
		addButton(new GuiNpcButton(4, x2 + 2 + bw, y, bw, 20, "gui.remove"));
		getButton(4).setEnabled(dealId > 0 && selectedDeal != null);
		addButton(new GuiNpcButton(5, x2 + (2 + bw) * 2, y, bw, 20, "selectServer.edit"));
		getButton(5).setEnabled(selectedDeal != null);

		String[] tabs = new String[selectedMarcet.sections.size()];
		int i = 0;
		for (MarcetSection tab : selectedMarcet.sections.values()) {
			tabs[i] = new TextComponentTranslation(tab.name).getFormattedText();
			i++;
		}
		addButton(new GuiButtonBiDirectional(6, x1, y, w, 20, tabs, tabSelect));

		int x3 = x2 - 43;
		y = guiTop + 60;
		addButton(new GuiNpcButton(7, x3, y, 41, 20, "<"));
		getButton(7).setEnabled(selectedMarcet != null && selectedMarcet.getDeal(dealId) == null
				&& scrollAllDeals.hasSelected() && selectedDeal != null && selectedDeal.isValid());
		addButton(new GuiNpcButton(8, x3, y += 22, 41, 20, ">"));
		getButton(8).setEnabled(scrollDeals.hasSelected());
		addButton(new GuiNpcButton(9, x3, y += 22, 41, 20, "<<"));
		getButton(9).setEnabled(!dataDeals.isEmpty());
		addButton(new GuiNpcButton(10, x3, y + 22, 41, 20, ">>"));
		getButton(10).setEnabled(!scrollDeals.getList().isEmpty());
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && subgui == null) {
			save();
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void save() {
		if (selectedMarcet == null) {
			return;
		}
		Client.sendData(EnumPacketServer.TraderMarketSave, selectedMarcet.writeToNBT());
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		try {
			switch (scroll.id) {
				case 0: { // Marcets
					if (!dataMarkets.containsKey(scroll.getSelected())) {
						return;
					}
					selectedMarcet = (Marcet) mData.getMarcet(dataMarkets.get(scroll.getSelected()));
					marcetId = selectedMarcet.getId();
					initGui();
					break;
				}
				case 1: // Deals
				case 2: { // All Deals
					if (!dataDeals.containsKey(scroll.getSelected())) {
						return;
					}
					selectedDeal = (Deal) mData.getDeal(dataDeals.get(scroll.getSelected()));
					dealId = selectedDeal.getId();
					initGui();
					break;
				}
			}
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		switch (scroll.id) {
		case 0: { // Marcets
			if (selectedMarcet == null) {
				return;
			}
			setSubGui(new SubGuiNpcMarketSettings(selectedMarcet));
			break;
		}
		case 1: // Deals
		case 2: { // All Deals
			if (selectedMarcet == null || !dataDeals.containsKey(scroll.getSelected())) {
				return;
			}
			save();
			SubGuiNPCManageDeal.parent = this;
			NoppesUtil.requestOpenGUI(EnumGuiType.SetupTraderDeal, marcetId, dealId, 0);
			break;
		}
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
		if (subgui instanceof SubGuiNpcMarketSettings) {
			setGuiData(null);
		}
		NoppesUtil.openGUI(player, this);
	}

}
