package noppes.npcs.client.gui.global;

import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCTraderSetup;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.AdditionalMethods;

public class GuiNPCManageMarcets
extends GuiContainerNPCInterface2
implements ITextfieldListener, IGuiData, ICustomScrollListener {
	
	private ContainerNPCTraderSetup container;
	private Map<String, Integer> dataDeals;
	private Map<String, Integer> dataMarcets;
	private GuiCustomScroll scrollMarcets, scrollDeal;
	private ResourceLocation slot = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");
	private MarcetController mData;
	private boolean wait;

	public GuiNPCManageMarcets(EntityNPCInterface npc, ContainerNPCTraderSetup container) {
		super(npc, container);
		this.container = container;
		this.ySize = 200;
		this.dataMarcets = Maps.<String, Integer>newTreeMap();
		this.dataDeals = Maps.<String, Integer>newTreeMap();
		this.wait = false;
	}
	
	@Override
	public void initGui() {
		if (this.wait) {
			this.clear();
			super.initGui();
			return;
		}
		super.initGui();
		this.mData = MarcetController.getInstance();
		this.setBackground("tradersetup.png");
		if (this.scrollMarcets == null) {
			(this.scrollMarcets = new GuiCustomScroll(this, 0)).setSize(105, 96);
		}
		if (this.scrollDeal == null) {
			(this.scrollDeal = new GuiCustomScroll(this, 1)).setSize(96, 96);
		}
		this.scrollMarcets.setListNotSorted(Lists.newArrayList(this.dataMarcets.keySet()));
		this.scrollDeal.setListNotSorted(Lists.newArrayList(this.dataDeals.keySet()));
		this.scrollMarcets.guiLeft = this.guiLeft + 5;
		this.scrollMarcets.guiTop = this.guiTop + 14;
		this.scrollDeal.guiLeft = this.guiLeft + 113;
		this.scrollDeal.guiTop = this.guiTop + 14;
		this.addScroll(this.scrollMarcets);
		this.addScroll(this.scrollDeal);
		int i = 0;
		if (this.container.marcet!=null) {
			for (int idM : this.dataMarcets.values()) {
				if (idM == this.container.marcet.id) {
					this.scrollMarcets.selected = i;
					break;
				}
				i++;
			}
		}
		if (this.container.deal!=null) {
			i = 0;
			for (int idD : this.dataDeals.values()) {
				if (idD == this.container.deal.id) {
					this.scrollDeal.selected = i;
					break;
				}
				i++;
			}
		}
		this.addLabel(new GuiNpcLabel(0, "global.market", this.guiLeft + 5, this.guiTop + 4));
		this.addLabel(new GuiNpcLabel(1, "gui.market.deals", this.guiLeft + 113, this.guiTop + 4));
		this.addLabel(new GuiNpcLabel(2, "role.marketname", this.guiLeft + 214, this.guiTop + 140));
		this.addLabel(new GuiNpcLabel(3, "availability.options", this.guiLeft + 270, this.guiTop + 14));
		this.addLabel(new GuiNpcLabel(4, "gui.market.product", this.guiLeft + 214, this.guiTop + 4));
		this.addLabel(new GuiNpcLabel(5, "gui.market.barter", this.guiLeft + 214, this.guiTop + 34));
		this.addLabel(new GuiNpcLabel(6, "gui.market.currency", this.guiLeft + 214, this.guiTop + 102));
		this.addLabel(new GuiNpcLabel(7, "gui.market.uptime", this.guiLeft + 214, this.guiTop + 173));
		this.addLabel(new GuiNpcLabel(8, "quest.itemamount", this.guiLeft + 304, this.guiTop + 102));
		this.addLabel(new GuiNpcLabel(9, "drop.chance", this.guiLeft + 270, this.guiTop + 33));

		if (this.container.marcet!=null) {
			this.addTextField(new GuiNpcTextField(0, this, this.guiLeft + 214, this.guiTop + 150, 180, 20,
					this.container.marcet.name));
			this.addTextField(new GuiNpcTextField(2, this, this.guiLeft + 214, this.guiTop + 183, 50, 20,
					"" + this.container.marcet.updateTime));
			this.getTextField(2).setNumbersOnly();
			this.getTextField(2).setMinMaxDefault(0, 360, this.container.marcet.updateTime);

			this.addButton(new GuiNpcButton(5, this.guiLeft + 5, this.guiTop + 112, 52, 20, "gui.add"));
			this.addButton(new GuiNpcButton(6, this.guiLeft + 59, this.guiTop + 112, 51, 20, "gui.remove"));
			this.getButton(6).enabled = this.container.marcet!=null && MarcetController.getInstance().marcets.size() > 1;
		}
		
		if (this.container.deal!=null) {
			this.addTextField(new GuiNpcTextField(1, this, this.guiLeft + 214, this.guiTop + 114, 50, 20,
					"" + this.container.deal.money));
			this.getTextField(1).setNumbersOnly();
			this.getTextField(1).setMinMaxDefault(0, Integer.MAX_VALUE, this.container.deal.money);
			this.addTextField(new GuiNpcTextField(3, this, this.guiLeft + 274, this.guiTop + 114, 50, 20,
					"" + this.container.deal.count[0]));
			this.getTextField(3).setNumbersOnly();
			this.getTextField(3).setMinMaxDefault(0,
					this.container.deal.inventorySold.getStackInSlot(0).getMaxStackSize() * 3,
					this.container.deal.count[0]);
			this.addTextField(new GuiNpcTextField(4, this, this.guiLeft + 330, this.guiTop + 114, 50, 20,
					"" + this.container.deal.count[1]));
			this.getTextField(4).setNumbersOnly();
			this.getTextField(4).setMinMaxDefault(0,
					this.container.deal.inventorySold.getStackInSlot(0).getMaxStackSize() * 12,
					this.container.deal.count[1]);
			double chance = Math.round(this.container.deal.chance * 1000000.0d) / 10000.0d;
			this.addTextField(new GuiNpcTextField(5, this, this.guiLeft + 270, this.guiTop + 45, 50, 20, "" + chance));
			this.getTextField(5).setDoubleNumbersOnly();
			this.getTextField(5).setMinMaxDoubleDefault(0.0d, 100.0d, chance);
	
			this.addButton(new GuiNpcButton(1, this.guiLeft + 363, this.guiTop + 31, 50, 20, new String[] { "gui.ignoreDamage.0", "gui.ignoreDamage.1" }, this.container.deal.ignoreDamage ? 1 : 0));
			this.addButton(new GuiNpcButton(2, this.guiLeft + 363, this.guiTop + 53, 50, 20,
					new String[] { "gui.ignoreNBT.0", "gui.ignoreNBT.1" }, this.container.deal.ignoreNBT ? 1 : 0));
			this.addButton(new GuiNpcButton(3, this.guiLeft + 363, this.guiTop + 9, 50, 20, "selectServer.edit"));
			this.addButton(new GuiNpcButton(4, this.guiLeft + 293, this.guiTop + 75, 120, 20,
					new String[] { "gui.market.deal.type.0", "gui.market.deal.type.1", "gui.market.deal.type.2" },
					this.container.deal.type));
			this.getButton(1).enabled = this.container.deal!=null;
			this.getButton(2).enabled = this.container.deal!=null;
			this.getButton(3).enabled = this.container.deal!=null;
			this.getButton(4).enabled = this.container.deal!=null;
			this.addButton(new GuiNpcButton(7, this.guiLeft + 113, this.guiTop + 112, 48, 20, "gui.add"));
			this.addButton(new GuiNpcButton(8, this.guiLeft + 163, this.guiTop + 112, 47, 20, "gui.remove"));
			boolean notEmpty = true;
			if (this.container.marcet != null) {
				for (Deal d : this.container.marcet.data.values()) {
					if (d != null && d.inventorySold.getStackInSlot(0).isEmpty()) {
						notEmpty = false;
						break;
					}
				}
			} else {
				notEmpty = false;
			}
			this.getButton(7).enabled = notEmpty;
			this.getButton(8).enabled = this.container.deal!=null && this.container.marcet.data.size() > 1;
		}
	}

	public void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
			case 1: {
				if (this.container.deal == null || this.container.deal.inventorySold.getStackInSlot(0).isEmpty()) { return; }
				this.container.deal.ignoreDamage = button.getValue() == 1;
				break;
			}
			case 2: {
				if (this.container.deal == null || this.container.deal.inventorySold.getStackInSlot(0).isEmpty()) {
					return;
				}
				this.container.deal.ignoreNBT = button.getValue() == 1;
				break;
			}
			case 3: {
				if (this.container.deal == null || this.container.deal.inventorySold.getStackInSlot(0).isEmpty()) { return; }
				this.setSubGui(new SubGuiNpcAvailability((Availability) this.container.deal.availability));
				this.initGui();
				break;
			}
			case 4: {
				if (this.container.deal == null || this.container.deal.inventorySold.getStackInSlot(0).isEmpty()) { return; }
				this.container.deal.type = button.getValue();
				break;
			}
			case 5: { // Add Marcet
				this.save();
				Marcet m = new Marcet();
				m.id = this.mData.getUnusedId();
				this.container.marcet = m;
				Client.sendData(EnumPacketServer.TraderMarketNew, -1);
				this.wait = true;
				break;
			}
			case 6: { // Del Marcet
				if (this.container.marcet == null) { return; }
				Client.sendData(EnumPacketServer.TraderMarketDel, this.container.marcet.id, -1);
				this.container.marcet = null;
				this.wait = true;
				break;
			}
			case 7: { // Add Deal
				this.save();
				Deal d = new Deal();
				d.id = this.container.marcet.data.size();
				this.container.deal = d;
				Client.sendData(EnumPacketServer.TraderMarketNew, this.container.marcet.id);
				this.wait = true;
				break;
			}
			case 8: { // Del Deal
				if (this.container.deal == null) { return; }
				Client.sendData(EnumPacketServer.TraderMarketDel, this.container.marcet.id, this.container.deal.id);
				this.container.marcet.remove(this.container.deal.id);
				this.container.setDeal(null);
				this.wait = true;
				break;
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		super.drawGuiContainerBackgroundLayer(f, i, j);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		for (int slotId = 0; slotId < 10; ++slotId) {
			int x = this.guiLeft + this.container.getSlot(slotId).xPos;
			int y = this.guiTop + this.container.getSlot(slotId).yPos;
			this.mc.renderEngine.bindTexture(this.slot);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.drawTexturedModalRect(x - 1, y - 1, 0, 0, 18, 18);
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		this.drawHorizontalLine(this.guiLeft + 212, this.guiLeft + this.xSize - 4, this.guiTop + 137, 0x80000000);
		this.drawVerticalLine(this.guiLeft + 211, this.guiTop + 4, this.guiTop + this.ySize + 12, 0x80000000);
		if (this.subgui != null || this.wait) { return; }
		if (this.player.world.getTotalWorldTime()%5==0 && this.container.deal!=null && this.container.deal.id==this.scrollDeal.selected && this.container.getSlot(0)!=null) {
			ItemStack stDeal = this.container.deal.inventorySold.getStackInSlot(0);
			ItemStack stCon = this.container.getSlot(0).getStack();
			if ((!stDeal.isEmpty() || !stCon.isEmpty()) && !stDeal.isItemEqual(stCon)) {
				this.container.deal.inventorySold.setInventorySlotContents(0, this.container.getSlot(0).getStack());
				this.dataDeals.clear();
				boolean notEmpty = true;
				for (Deal d : this.container.marcet.data.values()) {
					this.dataDeals.put(d.getSettingName(), d.id);
					if (d != null && d.inventorySold.getStackInSlot(0).isEmpty()) {
						notEmpty = false;
						break;
					}
				}
				this.scrollDeal.setListNotSorted(Lists.newArrayList(this.dataDeals.keySet()));
				if (this.getButton(7)!=null) {
					this.getButton(7).enabled = notEmpty;
				}
			}
		}		
		if (!CustomNpcs.showDescriptions) { return; }
		// Labels
		if (this.getLabel(0)!=null && this.getLabel(0).hovered) {
			this.setHoverText(new TextComponentTranslation("market.hover.names").getFormattedText());
		} else if (this.getLabel(1)!=null && this.getLabel(1).hovered) {
			this.setHoverText(new TextComponentTranslation("market.hover.deals").getFormattedText());
		} else if (this.getLabel(4)!=null && this.getLabel(4).hovered) {
			this.setHoverText(new TextComponentTranslation("market.hover.product").getFormattedText());
		} else if (this.getLabel(5)!=null && this.getLabel(5).hovered) {
			this.setHoverText(new TextComponentTranslation("market.hover.item").getFormattedText());
		}
		// TextFields
		else if (isMouseHover(i, j, this.guiLeft + 214, this.guiTop + 114, 50, 20)) {
			this.setHoverText(new TextComponentTranslation("market.hover.set.currency").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 274, this.guiTop + 114, 106, 20)) {
			this.setHoverText(new TextComponentTranslation("market.hover.set.amount").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 270, this.guiTop + 45, 50, 20)) {
			this.setHoverText(new TextComponentTranslation("market.hover.set.chance").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 214, this.guiTop + 150, 180, 20)) {
			this.setHoverText(new TextComponentTranslation("market.hover.set.name").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 214, this.guiTop + 183, 50, 20)) {
			this.setHoverText(new TextComponentTranslation("market.hover.set.update", new Object[] { AdditionalMethods.ticksToElapsedTime(this.container.marcet.updateTime * 1200, false, false, false) }).getFormattedText());
		}
		// Buttons
		else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.damage").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("recipe.hover.nbt").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.set.type").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.market.add").getFormattedText());
		} else if (this.getButton(6)!=null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.market.del").getFormattedText());
		} else if (this.getButton(7)!=null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.deal.add").getFormattedText());
		} else if (this.getButton(8)!=null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.deal.del").getFormattedText());
		}
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.TraderMarketGet);
	}

	@Override
	public void save() {
		if (this.container.marcet==null) { return; }
		NBTTagCompound compound = new NBTTagCompound();
		this.container.saveMarcet();
		this.container.marcet.writeEntityToNBT(compound);
		Client.sendData(EnumPacketServer.TraderMarketSave, compound, false);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		String name = scroll.getSelected();
		switch (scroll.id) {
			case 0: { // Marcets
				String key = AdditionalMethods.getDeleteColor(this.dataMarcets, name, true, false);
				if (!this.dataMarcets.containsKey(key)) { return; }
				this.save();
				NoppesUtil.requestOpenGUI(EnumGuiType.SetupTrader, this.dataMarcets.get(key), 0, 0);
				this.wait = true;
				this.initGui();
				break;
			}
			case 1: { // Deals
				this.save();
				String key = AdditionalMethods.getDeleteColor(this.dataDeals, name, true, false);
				if (!this.dataDeals.containsKey(key)) { return; }
				this.save();
				NoppesUtil.requestOpenGUI(EnumGuiType.SetupTrader, this.container.marcet.id, this.dataDeals.get(key), 0);
				this.wait = true;
				this.initGui();
				break;
			}
		}
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.mData = MarcetController.getInstance();
		this.dataMarcets.clear();
		this.dataDeals.clear();
		int idM = -1, idD = -1;
		int nowIdM = this.container.marcet == null ? -1 : this.container.marcet.id;
		int nowIdD = this.container.deal == null ? 0 : this.container.deal.id;
		for (Marcet m : this.mData.marcets.values()) {
			String name = m.getSettingName();
			this.dataMarcets.put(name, m.id);
			if (nowIdM == m.id) { this.container.marcet = m; }
			else if (idM<m.id) { idM = m.id; }
		}
		if (nowIdM<0 && idM>=0) { this.container.marcet = this.mData.getMarcet(idM); }
		if (this.container.marcet!=null) {
			for (Deal d : this.container.marcet.data.values()) {
				String name = d.getSettingName();
				this.dataDeals.put(name, d.id);
				if (nowIdD == d.id) { this.container.setDeal(d); }
				else if (idD<d.id) { idM = d.id; }
			}
		}
		if (nowIdD<0 && idD>=0) { this.container.setDeal(this.container.marcet.data.get(idD)); }
		this.wait = false;
		this.initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		String text = textField.getText();
		switch (textField.getId()) {
			case 0: { // Name
				if (text.equals(this.container.marcet.name)) { return; }
				this.container.marcet.name = text;
				this.initGui();
				break;
			}
			case 1: {
				this.container.deal.money = textField.getInteger();
				this.initGui();
				break;
			}
			case 2: {
				int time = textField.getInteger();
				if (time < 5) {
					time = 0;
				} else if (time > 360) {
					time = 360;
				}
				this.container.marcet.updateTime = time;
				this.initGui();
				break;
			}
			case 3: {
				this.container.deal.count[0] = textField.getInteger();
				this.initGui();
				break;
			}
			case 4: {
				this.container.deal.count[1] = textField.getInteger();
				this.initGui();
				break;
			}
			case 5: {
				this.container.deal.chance = (float) textField.getDouble() / 100.0f;
				this.initGui();
				break;
			}
		}
	}
	
	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui==null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
		}
		super.keyTyped(c, i);
	}

}
