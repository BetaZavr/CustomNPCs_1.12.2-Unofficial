package noppes.npcs.client.gui.global;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiNpcMarketSettings;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.DealMarkup;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCManageMarcets
extends GuiNPCInterface2
implements IGuiData, ICustomScrollListener, GuiYesNoCallback {
	
	private final Map<String, Integer> dataDeals;
	private final Map<String, Integer> dataMarcets;
	private GuiCustomScroll scrollMarcets, scrollDeals;
	private MarcetController mData;
	private int marcetID, dealID;
	private Marcet selectedMarcet;
	private Deal selectedDeal;
	private boolean addNewMarcet, addNewDeal;

	public GuiNPCManageMarcets(EntityNPCInterface npc, int marcetID, int dealID) {
		super(npc);
		this.marcetID = marcetID;
		this.dealID = dealID;
		this.ySize = 200;
		this.dataDeals = Maps.<String, Integer>newTreeMap();
		this.dataMarcets = Maps.<String, Integer>newTreeMap();
		Client.sendData(EnumPacketServer.TraderMarketGet);
	}
	
	@Override
	public void initGui() {
		super.initGui();
		this.mData = MarcetController.getInstance();
		int w = 202, h = this.ySize - 24;
		if (this.scrollMarcets == null) { (this.scrollMarcets = new GuiCustomScroll(this, 0)).setSize(w, h); }
		if (this.scrollDeals == null) { (this.scrollDeals = new GuiCustomScroll(this, 1)).setSize(w, h); }
		int x0 = this.guiLeft + 5, x1 = this.guiLeft + w + 10, y = this.guiTop + 14;
		this.scrollMarcets.setListNotSorted(Lists.newArrayList(this.dataMarcets.keySet()));
		this.scrollDeals.setListNotSorted(Lists.newArrayList(this.dataDeals.keySet()));
		this.scrollMarcets.guiLeft = x0;
		this.scrollMarcets.guiTop = y;
		this.scrollDeals.guiLeft = x1;
		this.scrollDeals.guiTop = y;
		this.addScroll(this.scrollMarcets);
		this.addScroll(this.scrollDeals);
		if (this.selectedMarcet!=null) {
			this.scrollMarcets.setSelected(this.selectedMarcet.getSettingName());
			List<String[]> infoList = new ArrayList<String[]>();
			List<ItemStack> stacks = Lists.<ItemStack>newArrayList();
			for (Integer dealID : this.selectedMarcet.getDealIDs()) {
				Deal deal = (Deal) this.mData.getDeal(dealID);
				List<String> info = new ArrayList<String>();
				DealMarkup dm = new DealMarkup();
				if (deal != null) { dm.set((Deal) deal); }
				if (deal==null || !deal.isValid()) {
					info.add(new TextComponentTranslation("market.hover.nv.deal").getFormattedText());
					if (deal==null) {
						stacks.add(ItemStack.EMPTY);
						info.add(new TextComponentTranslation("hover.total.error").getFormattedText());
					} else {
						stacks.add(dm.main);
						if (dm.main == null || dm.main.getItem()==Items.AIR) { info.add(new TextComponentTranslation("market.hover.nv.deal.product").getFormattedText()); }
						if (dm.baseMoney == 0 && dm.baseItems.isEmpty()) { info.add(new TextComponentTranslation("market.hover.nv.deal.barter").getFormattedText()); }
					}
				}
				else {
					stacks.add(dm.main);
					info.add(new TextComponentTranslation("market.hover.product").getFormattedText());
					info.add(dm.main.getDisplayName() + " x" + dm.count + (deal.getMaxCount() > 0 ? " " + new TextComponentTranslation("market.hover.item.amount", new Object[] { "" + deal.getAmount() }).getFormattedText() : ""));
					if (!dm.baseItems.isEmpty()) {
						info.add(new TextComponentTranslation("market.hover.item").getFormattedText());
						for (ItemStack curr : dm.baseItems.keySet()) {
							info.add(curr.getDisplayName() + " x" + dm.baseItems.get(curr));
						}
					}
					if (dm.baseMoney > 0) {
						info.add(new TextComponentTranslation("market.hover.currency").getFormattedText());
						info.add("" + dm.baseMoney + CustomNpcs.charCurrencies.charAt(0));
					}
					info.add(((char) 167)+"e"+(new TextComponentTranslation("market.deal.type." + dm.deal.getType()).getFormattedText()));
					info.add(((char) 167)+"6"+(new TextComponentTranslation("drop.chance").getFormattedText() + ((char) 167)+"6: " + ((char) 167)+"r"+dm.deal.getChance()+"%"));
				}
					
				infoList.add(info.toArray(new String[info.size()]));
			}
			this.scrollDeals.hoversTexts = infoList.toArray(new String[infoList.size()][1]);
			this.scrollDeals.setStacks(stacks);
		}
		else {this.scrollDeals.hoversTexts = null;}
		if (this.selectedDeal!=null) { this.scrollDeals.setSelected(this.selectedDeal.getSettingName()); }
		if (!this.dataMarcets.isEmpty()) {
			List<String[]> infoList = new ArrayList<String[]>();
			for (int marcetId : this.dataMarcets.values()) {
				Marcet marcet = (Marcet) this.mData.getMarcet(marcetId);
				List<String> info = new ArrayList<String>();
				info.add(new TextComponentTranslation("market.hover.market.id", ""+marcet.getId(), ""+marcet.name).getFormattedText());
				if (marcet.isEmpty() || marcet.hasEmptyDeal()) {
					info.add(new TextComponentTranslation("market.hover.nv.market").getFormattedText());
					for (int dealId : marcet.getDealIDs()) {
						Deal deal = (Deal) this.mData.getDeal(dealId);
						if (deal == null) { info.add(new TextComponentTranslation("market.hover.nv.market.null", ""+dealId).getFormattedText()); }
						else if (!deal.isValid()) {
							if (deal.getProduct().getMCItemStack() == null || deal.getProduct().getMCItemStack().getItem()==Items.AIR) { info.add(new TextComponentTranslation("market.hover.nv.market.deal.0", ""+dealId).getFormattedText()); }
							if (deal.getMoney() == 0 && deal.getCurrency().isEmpty()) { info.add(new TextComponentTranslation("market.hover.nv.market.deal.1", ""+dealId).getFormattedText()); }
						}
					}
					if (info.size()>9) { info.add("..."); break; }
				}
				infoList.add(info.toArray(new String[info.size()]));
			}
			this.scrollMarcets.hoversTexts = infoList.toArray(new String[infoList.size()][1]);
		}
		else {this.scrollMarcets.hoversTexts = null;}
		
		this.scrollMarcets.resetRoll();
		this.scrollDeals.resetRoll();
		this.addLabel(new GuiNpcLabel(0, "global.market", x0 + 2, y - 10));
		this.addLabel(new GuiNpcLabel(1, "market.deals", x1, y - 10));
		
		y += this.scrollMarcets.height + 2;
		int bw = (w - 2)/3;
		this.addButton(new GuiNpcButton(0, x0, y, bw, 20, "gui.add"));
		this.addButton(new GuiNpcButton(1, x0 + 2 + bw, y, bw, 20, "gui.remove"));
		this.getButton(1).setEnabled(this.selectedMarcet!=null && this.mData.marcets.size()>1);
		this.addButton(new GuiNpcButton(2, x0 + (2 + bw) * 2, y, bw, 20, "selectServer.edit"));
		this.getButton(2).setEnabled(this.selectedMarcet!=null);
		
		this.addButton(new GuiNpcButton(3, x1, y, bw, 20, "gui.add"));
		this.getButton(3).setEnabled(this.selectedMarcet!=null && !this.selectedMarcet.hasEmptyDeal());
		this.addButton(new GuiNpcButton(4, x1 + 2 + bw, y, bw, 20, "gui.remove"));
		this.getButton(4).setEnabled(this.selectedMarcet!=null &&this.selectedMarcet.getDealIDs().length>1);
		this.addButton(new GuiNpcButton(5, x1 + (2 + bw) * 2, y, bw, 20, "selectServer.edit"));
		this.getButton(5).setEnabled(this.selectedDeal!=null);
	}

	public void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
			case 0: { // Add market
				this.save();
				Client.sendData(EnumPacketServer.TraderMarketNew, -1);
				this.addNewMarcet = true;
				break;
			}
			case 1: { // Del market
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, this.scrollMarcets.getSelected(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 2: { // Market settings
				this.setSubGui(new SubGuiNpcMarketSettings(this.selectedMarcet));
				break;
			}
			case 3: { // Add deal
				this.save();
				Client.sendData(EnumPacketServer.TraderMarketNew, this.marcetID);
				this.addNewDeal = true;
				break;
			}
			case 4: { // Del deal
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, this.scrollDeals.getSelected(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 1);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 5: { // Deal settings
				NoppesUtil.requestOpenGUI(EnumGuiType.SetupTraderDeal, this.marcetID, this.dealID, 0);
				this.close();
				break;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui != null || !CustomNpcs.showDescriptions) { return; }
		// Labels
		if (this.getLabel(0)!=null && this.getLabel(0).hovered) {
			this.setHoverText(new TextComponentTranslation("market.hover.names").getFormattedText());
		} else if (this.getLabel(1)!=null && this.getLabel(1).hovered) {
			this.setHoverText(new TextComponentTranslation("market.hover.deals").getFormattedText());
		} else if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.market.add").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.market.del").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.market.settings").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.deal.add").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.deal.del").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.deal.settings").getFormattedText());
		}
	}
	
	@Override
	public void save() {
		if (this.selectedMarcet == null) { return; }
		Client.sendData(EnumPacketServer.TraderMarketSave, this.selectedMarcet.writeToNBT());
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		switch (scroll.id) {
			case 0: { // Marcets
				if (!this.dataMarcets.containsKey(scroll.getSelected())) { return; }
				this.setMarket(this.dataMarcets.get(scroll.getSelected()));
				this.initGui();
				break;
			}
			case 1: { // Deals
				if (!this.dataDeals.containsKey(scroll.getSelected())) { return; }
				this.dealID = this.dataDeals.get(scroll.getSelected());
				this.selectedDeal = (Deal) this.mData.getDeal(this.dealID);
				this.initGui();
				break;
			}
		}
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		switch (scroll.id) {
			case 0: { // Marcets
				if (this.selectedMarcet == null) { return; }
				this.setSubGui(new SubGuiNpcMarketSettings(this.selectedMarcet));
				break;
			}
			case 1: { // Deals
				if (this.selectedMarcet == null || this.selectedDeal == null) { return; }
				NoppesUtil.requestOpenGUI(EnumGuiType.SetupTraderDeal, this.selectedMarcet.getId(), this.selectedDeal.getId(), 0);
				this.close();
				break;
			}
		}
	}
	
	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui==null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
	}

	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI((EntityPlayer) this.player, this);
		if (!result) { return; }
		switch(id) {
			case 0: {
				if (this.selectedMarcet==null) { return; }
				Client.sendData(EnumPacketServer.TraderMarketDel, this.marcetID, -1);
				break;
			}
			case 1: {
				if (this.selectedMarcet==null) { return; }
				Client.sendData(EnumPacketServer.TraderMarketDel, this.marcetID, this.dealID);
				break;
			}
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.mData = MarcetController.getInstance();
		this.dataMarcets.clear();
		this.selectedMarcet = null;
		for (Marcet m : this.mData.marcets.values()) {
			this.dataMarcets.put(m.getSettingName(), m.getId());
			if (this.addNewMarcet || this.marcetID == m.getId() || (this.marcetID<=0 && this.selectedMarcet == null)) {
				this.selectedMarcet = m;
			}
		}
		if (this.selectedMarcet!=null) { this.setMarket(this.selectedMarcet.getId()); }
		this.addNewMarcet = false;
		this.initGui();
	}
	
	private void setMarket(int marcetID) {
		this.marcetID = marcetID;
		this.dataDeals.clear();
		this.mData = MarcetController.getInstance();
		this.selectedMarcet = (Marcet) this.mData.getMarcet(marcetID);
		if (this.selectedMarcet!=null) {
			this.selectedDeal = null;
			for (Integer dealId : this.selectedMarcet.getDealIDs()) {
				Deal deal = (Deal) this.mData.getDeal(dealId);
				if (deal == null) {
					this.dataDeals.put("ID: "+dealId + ": " + ((char) 167) + "4" + new TextComponentTranslation("type.empty").getFormattedText(), dealId);
					continue;
				}
				this.dataDeals.put(deal.getSettingName(), dealId);
				if (this.addNewDeal || this.dealID == deal.getId() || (this.dealID<=0 && this.selectedDeal == null)) {
					this.selectedDeal = deal;
					this.dealID = deal.getId();
				}
			}
		}
		this.addNewDeal = false;
	}
	
}
