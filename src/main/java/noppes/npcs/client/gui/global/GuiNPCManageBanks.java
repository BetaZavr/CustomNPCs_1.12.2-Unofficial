package noppes.npcs.client.gui.global;

import java.util.*;

import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditBankAccess;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerManageBanks;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.Bank.CeilSettings;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCManageBanks
extends GuiContainerNPCInterface2
implements IScrollData, ICustomScrollListener, ITextfieldListener, IGuiData, GuiYesNoCallback {

	private Bank bank = new Bank();
	private final ContainerManageBanks container;
	private final HashMap<String, Integer> data = new HashMap<>();
	private GuiCustomScroll scroll;
	private String selected = "";
	private int ceil = 0, waitTime = 30;
	private boolean isWait;

	public GuiNPCManageBanks(EntityNPCInterface npc, ContainerManageBanks container) {
		super(npc, container);
		this.container = container;
		this.drawDefaultBackground = false;
		this.setBackground("inventorymenu.png");
		this.ySize = 200;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
		case 0: { // ceil
			if (this.ceil == button.getValue()) {
				return;
			}
			this.save();
			this.ceil = button.getValue();
			Client.sendData(EnumPacketServer.BankGet, this.bank.id, this.ceil);
			this.isWait = true;
			this.waitTime = 30;
			this.initGui();
			break;
		}
		case 1: { // add ceil
			this.ceil = this.bank.ceilSettings.size();
			Client.sendData(EnumPacketServer.BankAddCeil, this.bank.id, this.ceil);
			this.isWait = true;
			this.waitTime = 30;
			this.initGui();
			break;
		}
		case 2: { // remove ceil
			if (!this.data.containsKey(this.selected) || !this.bank.ceilSettings.containsKey(this.ceil)) {
				return;
			}
			String msg = new TextComponentTranslation("bank.hover.ceil.del").getFormattedText();
			while (msg.contains("<br>")) {
				msg = msg.replace("<br>", "" + ((char) 10));
			}
			GuiYesNo guiyesno = new GuiYesNo(this, new TextComponentTranslation("gui.bank", ": ID:" + this.bank.id + " \"" + this.bank.name + "\"; " + new TextComponentTranslation("gui.ceil", ": ID:" + (this.ceil + 1)).getFormattedText()).getFormattedText(), msg, 1);
			this.displayGuiScreen(guiyesno);
			break;
		}
		case 3: { // public
			bank.isPublic = ((GuiNpcCheckBox) button).isSelected();
			break;
		}
		case 6: { // add bank
			this.save();
			this.scroll.clear();
			StringBuilder t = new StringBuilder("New");
			while (this.data.containsKey(t.toString())) { t.append("_"); }
			this.selected = t.toString();
			Bank bank = new Bank();
			bank.name = this.selected;
			NBTTagCompound compound = new NBTTagCompound();
			bank.writeToNBT(compound);
			Client.sendData(EnumPacketServer.BankSave, compound);
			this.isWait = true;
			this.waitTime = 30;
			this.initGui();
			break;
		}
		case 7: { // remove bank
			if (!this.data.containsKey(this.selected)) {
				return;
			}
			String msg = new TextComponentTranslation("bank.hover.del").getFormattedText();
			while (msg.contains("<br>")) {
				msg = msg.replace("<br>", "" + ((char) 10));
			}
			GuiYesNo guiyesno = new GuiYesNo(this, new TextComponentTranslation("gui.bank", ": ID:" + this.bank.id + " \"" + this.bank.name + "\"").getFormattedText(), msg, 0);
			this.displayGuiScreen(guiyesno);
			break;
		}
		case 8: { // settings
			if (this.bank == null) {
				return;
			}
			this.setSubGui(new SubGuiEditBankAccess(0, this.bank));
			break;
		}
		}
	}

	@Override
	public void closeSubGui(SubGuiInterface gui) {
		super.closeSubGui(gui);
		if (gui instanceof SubGuiEditBankAccess) {
			SubGuiEditBankAccess subGui = (SubGuiEditBankAccess) gui;
			if (bank.isChanging != subGui.isChanging) {
				bank.isChanging = subGui.isChanging;
			}
			if (!bank.owner.equals(subGui.owner)) {
				bank.owner = subGui.owner;
			}
			if (subGui.names.size() != this.bank.access.size()) {
				this.bank.access.clear();
				this.bank.access.addAll(subGui.names);
			} else {
				for (String name : subGui.names) {
					if (this.bank.access.contains(name)) {
						continue;
					}
					this.bank.access.clear();
					this.bank.access.addAll(subGui.names);
					break;
				}
			}
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI(this.player, this);
		if (!result) {
			return;
		}
		switch (id) { // remove bank
			case 0: {
				if (!this.data.containsKey(this.selected)) {
					return;
				}
				Client.sendData(EnumPacketServer.BankRemove, this.data.get(this.selected), -1);
				this.isWait = true;
				this.waitTime = 30;
				this.initGui();
				break;
			}
			case 1: { // remove ceil
				if (!this.data.containsKey(this.selected) || !this.bank.ceilSettings.containsKey(this.ceil)) {
					return;
				}
				Client.sendData(EnumPacketServer.BankRemove, this.data.get(this.selected), this.ceil);
				this.isWait = true;
				this.waitTime = 30;
				this.initGui();
				break;
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		if (this.isWait || this.waitTime > 0 || this.subgui != null) {
			return;
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		for (int slotId = 0; slotId < 2; ++slotId) {
            this.inventorySlots.getSlot(slotId).xPos = this.selected.isEmpty() ? -5000 : 180;
			this.inventorySlots.getSlot(slotId).yPos = this.selected.isEmpty() ? -5000 : slotId == 0 ? 123 : 159;

			int x = this.guiLeft + this.inventorySlots.getSlot(slotId).xPos;
			int y = this.guiTop + this.inventorySlots.getSlot(slotId).yPos;
			this.mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.drawTexturedModalRect(x - 1, y - 1, 0, 0, 18, 18);
		}
		if (!this.selected.isEmpty()) {
			int x = this.guiLeft + 5;
			int y = this.guiTop + 12;
			this.fontRenderer.drawString(new TextComponentTranslation("gui.name").getFormattedText() + ":", x, y, CustomNpcResourceListener.DefaultTextColor);
			this.fontRenderer.drawString(new TextComponentTranslation("gui.ceil").getFormattedText() + ":", x, (y += 22), CustomNpcResourceListener.DefaultTextColor);
			this.fontRenderer.drawString(new TextComponentTranslation("gui.start").getFormattedText() + ":", x, (y += 22), CustomNpcResourceListener.DefaultTextColor);
			this.fontRenderer.drawString(new TextComponentTranslation("gui.max").getFormattedText() + ":", x + 126, y, CustomNpcResourceListener.DefaultTextColor);
			x = this.guiLeft + 179;
			y = this.guiTop + 112;
			this.fontRenderer.drawString(new TextComponentTranslation("bank.tab.cost").getFormattedText() + ":", x, y, CustomNpcResourceListener.DefaultTextColor);
			this.fontRenderer.drawString(new TextComponentTranslation("bank.upg.cost").getFormattedText() + ":", x, y + 36, CustomNpcResourceListener.DefaultTextColor);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.isWait || this.waitTime > 0) {
			if (!this.isWait) {
				this.waitTime--;
				if (this.waitTime == 0) {
					this.initGui();
				}
			}
			String text = new TextComponentTranslation("gui.wait", ": " + new TextComponentTranslation("gui.wait.data").getFormattedText()).getFormattedText();
			this.fontRenderer.drawString(text, this.guiLeft + (this.width - this.fontRenderer.getStringWidth(text)) / 2, this.guiTop + 60, CustomNpcs.LableColor.getRGB());
			return;
		}
		if (this.subgui != null || !CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getTextField(0) != null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover.name").getFormattedText());
		} else if (this.getTextField(1) != null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover.slots.min").appendSibling(new TextComponentTranslation("bank.hover.change")).getFormattedText());
		} else if (this.getTextField(2) != null && this.getTextField(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover.slots.max").appendSibling(new TextComponentTranslation("bank.hover.change")).getFormattedText());
		} else if (this.getTextField(3) != null && this.getTextField(3).getVisible() && this.getTextField(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover.open.money").getFormattedText());
		} else if (this.getTextField(4) != null && this.getTextField(4).getVisible() && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover.upgrade.money").getFormattedText());
		} else if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover.ceil", "" + this.bank.ceilSettings.size()).getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover.ceil.add").appendSibling(new TextComponentTranslation("bank.hover.change")).getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover.ceil.del").appendSibling(new TextComponentTranslation("bank.hover.change")).getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover.public").getFormattedText());
		} else if (this.getButton(6) != null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover.add").getFormattedText());
		} else if (this.getButton(7) != null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover.del").appendSibling(new TextComponentTranslation("bank.hover.change")).getFormattedText());
		} else if (this.getButton(8) != null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover.settings").getFormattedText());
		} else if (!this.selected.isEmpty()) {
			int x = this.guiLeft + 179;
			int y = this.guiTop + 112;
			if (this.isMouseHover(mouseX, mouseY, x, y, 60, 12)) {
				this.setHoverText(new TextComponentTranslation("bank.tab.cost.info",
						"" + this.bank.ceilSettings.get(this.ceil).startCells,
						"" + this.bank.ceilSettings.get(this.ceil).maxCells).getFormattedText());
			} else if (this.isMouseHover(mouseX, mouseY, x, y + 36, 60, 12)) {
				this.setHoverText(new TextComponentTranslation("bank.upg.cost.info",
						"" + this.bank.ceilSettings.get(this.ceil).startCells,
						"" + this.bank.ceilSettings.get(this.ceil).maxCells).getFormattedText());
			}
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(160, 180);
		}
		if (this.isWait || this.waitTime > 0) {
			return;
		}
		int x = this.guiLeft + 254;
		int y = this.guiTop + 8;
		this.scroll.guiLeft = x;
		this.scroll.guiTop = y;
		this.addScroll(this.scroll);
		if (!this.selected.isEmpty()) {
			this.scroll.setSelected(this.selected);
		}
		this.scroll.hoversTexts = null;
		List<String> list = this.scroll.getList();
		if (list != null && !list.isEmpty()) {
			this.scroll.hoversTexts = new String[list.size()][];
			int i = 0;
			for (String key : list) {
				this.scroll.hoversTexts[i] = new String[] { "ID: " + this.data.get(key) };
				i++;
			}
		}

		y += this.scroll.height + 2;
		this.addButton(new GuiNpcButton(6, x, y, 50, 20, "gui.add"));
		this.addButton(new GuiNpcButton(7, x + this.scroll.width - 50, y, 50, 20, "gui.remove"));
		this.getButton(7).enabled = !this.selected.isEmpty() && this.data.size() > 1;

		x = this.guiLeft + 75;
		y = this.guiTop + 8;
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, x, y, 160, 16, this.selected));
		this.getTextField(0).setMaxStringLength(20);
		this.getTextField(0).setVisible(!this.selected.isEmpty());

		y += 22;
		List<String> csIds = new ArrayList<>();
		if (this.bank != null) {
			for (int i = 0; i < this.bank.ceilSettings.size(); i++) {
				csIds.add("" + (i + 1));
			}
		}
		this.addButton(new GuiButtonBiDirectional(0, x, y, 50, 20, csIds.toArray(new String[0]), this.ceil));
		this.getButton(0).visible = !this.selected.isEmpty();

		this.addButton(new GuiNpcButton(1, x + 55, y, 50, 20, "gui.add"));
		this.getButton(1).visible = !this.selected.isEmpty();

		this.addButton(new GuiNpcButton(2, x + 110, y, 50, 20, "gui.remove"));
		this.getButton(2).visible = !this.selected.isEmpty();
		this.getButton(2).enabled = this.ceil > 0;

		y += 22;
		CeilSettings cs = this.bank.ceilSettings.get(this.ceil);
		int sc = cs.startCells;
		int mc = cs.maxCells;
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, x, y, 50, 18, "" + sc));
		this.getTextField(1).setVisible(!this.selected.isEmpty());
		this.getTextField(1).setNumbersOnly();
		this.getTextField(1).setMinMaxDefault(1, mc, sc);

		this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, x + 110, y, 50, 18, "" + mc));
		this.getTextField(2).setVisible(!this.selected.isEmpty());
		this.getTextField(2).setNumbersOnly();
		this.getTextField(2).setMinMaxDefault(1, 198, mc);

		GuiNpcCheckBox checkBox = new GuiNpcCheckBox(3, x, (y += 18), 160, 16, "bank.public.true", "bank.public.false", bank.isPublic);
		checkBox.setVisible(!selected.isEmpty());
		this.addButton(checkBox);

		this.addButton(new GuiNpcButton(8, x, y + 20, 20, 20, 20, 146, GuiNPCInterface.WIDGETS));

		this.getButton(8).setVisible(!this.selected.isEmpty() && this.bank.isPublic);

		this.addTextField(new GuiNpcTextField(3, this, this.fontRenderer, x += 126, y += 52, 50, 18, "" + cs.openMoney));
		this.getTextField(3).setVisible(!this.selected.isEmpty());
		this.getTextField(3).setNumbersOnly();
		this.getTextField(3).setMinMaxDefault(0, Integer.MAX_VALUE, cs.openMoney);

		this.addTextField(new GuiNpcTextField(4, this, this.fontRenderer, x, y += 36, 50, 18, "" + cs.upgradeMoney));
		this.getTextField(4).setVisible(!this.selected.isEmpty());
		this.getTextField(4).setNumbersOnly();
		this.getTextField(4).setMinMaxDefault(0, Integer.MAX_VALUE, cs.upgradeMoney);
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.BanksGet);
		this.isWait = false;
		this.waitTime = 30;
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui == null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void save() {
		if (this.selected != null && this.data.containsKey(this.selected) && this.bank != null && this.bank.ceilSettings.containsKey(this.ceil)) {
			this.bank.ceilSettings.get(this.ceil).openStack = this.container.getSlot(0).getStack();
			this.bank.ceilSettings.get(this.ceil).upgradeStack = this.container.getSlot(1).getStack();
			NBTTagCompound compound = new NBTTagCompound();
			this.bank.writeToNBT(compound);
			this.isWait = true;
			this.waitTime = 30;
			this.initGui();
			Client.sendData(EnumPacketServer.BankSave, compound);
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.id == 0 && !scroll.getSelected().equals(this.selected)) {
			this.save();
			this.ceil = 0;
			this.selected = scroll.getSelected();
			Client.sendData(EnumPacketServer.BankGet, this.data.get(this.selected), 0);
			this.isWait = true;
			this.waitTime = 30;
			this.initGui();
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data.clear();
		this.data.putAll(data);
		this.scroll.setList(list);
		this.isWait = false;
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (this.bank == null) {
			this.bank = new Bank();
		}
		this.bank.readFromNBT(compound);
		if (compound.hasKey("CurrentCeil", 3)) {
			this.ceil = compound.getInteger("CurrentCeil");
		}
		this.container.setBank(this.bank, this.ceil);
		this.selected = this.bank.name;
		this.isWait = false;
	}

	@Override
	public void setSelected(String selected) {
		this.selected = selected;
		this.scroll.setSelected(selected);
		this.isWait = false;
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (this.bank.id == -1) {
			return;
		}
		switch (textField.getId()) {
			case 0: { // name
				String name = textField.getText();
				if (!name.isEmpty() && !this.data.containsKey(name)) {
					String old = this.bank.name;
					this.data.remove(this.bank.name);
					this.bank.name = name;
					this.data.put(this.bank.name, this.bank.id);
					this.selected = name;
					this.scroll.replace(old, this.bank.name);
				}
				break;
			}
			case 1: { // startCells
				if (!textField.isInteger()) {
					textField.setText("" + textField.def);
					return;
				}
				this.bank.ceilSettings.get(this.ceil).startCells = textField.getInteger();
				break;
			}
			case 2: { // maxCells
				if (!textField.isInteger()) {
					textField.setText("" + textField.def);
					return;
				}
				this.bank.ceilSettings.get(this.ceil).maxCells = textField.getInteger();
				break;
			}
			case 3: { // open money
				if (!textField.isInteger()) {
					textField.setText("" + textField.def);
					return;
				}
				this.bank.ceilSettings.get(this.ceil).openMoney = textField.getInteger();
				break;
			}
			case 4: { // upgrade money
				if (!textField.isInteger()) {
					textField.setText("" + textField.def);
					return;
				}
				this.bank.ceilSettings.get(this.ceil).upgradeMoney = textField.getInteger();
				break;
			}
		}
	}

}
