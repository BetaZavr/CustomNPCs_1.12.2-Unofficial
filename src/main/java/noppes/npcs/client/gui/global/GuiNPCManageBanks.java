package noppes.npcs.client.gui.global;

import java.util.*;

import net.minecraft.client.gui.GuiScreen;
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
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerManageBanks;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.Bank.CeilSettings;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiNPCManageBanks extends GuiContainerNPCInterface2
		implements IScrollData, ICustomScrollListener, ITextfieldListener, IGuiData, GuiYesNoCallback {

	protected final HashMap<String, Integer> data = new HashMap<>();
	protected final ContainerManageBanks container;
	protected GuiCustomScroll scroll;
	protected Bank bank = new Bank();
	protected String selected = "";
	protected boolean isWait;
	protected int ceil = 0;
	protected int waitTime = 30;

	public GuiNPCManageBanks(EntityNPCInterface npc, ContainerManageBanks cont) {
		super(npc, cont);
		setBackground("inventorymenu.png");
		drawDefaultBackground = false;
		closeOnEsc = true;
		ySize = 200;
		parentGui = EnumGuiType.MainMenuGlobal;

		container = cont;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				if (ceil == button.getValue()) { return; }
				save();
				ceil = button.getValue();
				Client.sendData(EnumPacketServer.BankGet, bank.id, ceil);
				isWait = true;
				waitTime = 30;
				initGui();
				break;
			} // ceil
			case 1: {
				ceil = bank.ceilSettings.size();
				Client.sendData(EnumPacketServer.BankAddCeil, bank.id, ceil);
				isWait = true;
				waitTime = 30;
				initGui();
				break;
			} // add ceil
			case 2: {
				if (!data.containsKey(selected) || !bank.ceilSettings.containsKey(ceil)) { return; }
				String msg = new TextComponentTranslation("bank.hover.ceil.del").getFormattedText();
				while (msg.contains("<br>")) { msg = msg.replace("<br>", "" + ((char) 10)); }
				GuiYesNo guiyesno = new GuiYesNo(this, new TextComponentTranslation("gui.bank", ": ID:" + bank.id + " \"" + bank.name + "\"; " + new TextComponentTranslation("gui.ceil", ": ID:" + (ceil + 1)).getFormattedText()).getFormattedText(), msg, 1);
				displayGuiScreen(guiyesno);
				break;
			} // remove ceil
			case 3: bank.isPublic = ((GuiNpcCheckBox) button).isSelected(); break; // public
			case 6: {
				save();
				scroll.clear();
				StringBuilder t = new StringBuilder("New");
				while (data.containsKey(t.toString())) { t.append("_"); }
				selected = t.toString();
				Bank bank = new Bank();
				bank.name = selected;
				NBTTagCompound compound = new NBTTagCompound();
				bank.writeToNBT(compound);
				Client.sendData(EnumPacketServer.BankSave, compound);
				isWait = true;
				waitTime = 30;
				initGui();
				break;
			} // add bank
			case 7: {
				if (!data.containsKey(selected)) { return; }
				String msg = new TextComponentTranslation("bank.hover.del").getFormattedText();
				while (msg.contains("<br>")) { msg = msg.replace("<br>", "" + ((char) 10)); }
				GuiYesNo guiyesno = new GuiYesNo(this, new TextComponentTranslation("gui.bank", ": ID:" + bank.id + " \"" + bank.name + "\"").getFormattedText(), msg, 0);
				displayGuiScreen(guiyesno);
				break;
			} // remove bank
			case 8: {
				if (bank == null) { return; }
				setSubGui(new SubGuiEditBankAccess(0, bank));
				break;
			} // settings
		}
	}

	@Override
	public void subGuiClosed(GuiScreen gui) {
		if (gui instanceof SubGuiEditBankAccess) {
			SubGuiEditBankAccess subGui = (SubGuiEditBankAccess) gui;
			if (bank.isChanging != subGui.isChanging) { bank.isChanging = subGui.isChanging; }
			if (!bank.owner.equals(subGui.owner)) { bank.owner = subGui.owner; }
			if (subGui.names.size() != bank.access.size()) {
				bank.access.clear();
				bank.access.addAll(subGui.names);
			} else {
				for (String name : subGui.names) {
					if (bank.access.contains(name)) { continue; }
					bank.access.clear();
					bank.access.addAll(subGui.names);
					break;
				}
			}
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI(player, this);
		if (!result) { return; }
		switch (id) {
			case 0: {
				if (!data.containsKey(selected)) { return; }
				Client.sendData(EnumPacketServer.BankRemove, data.get(selected), -1);
				isWait = true;
				waitTime = 30;
				initGui();
				break;
			} // remove bank
			case 1: {
				if (!data.containsKey(selected) || !bank.ceilSettings.containsKey(ceil)) { return; }
				Client.sendData(EnumPacketServer.BankRemove, data.get(selected), ceil);
				isWait = true;
				waitTime = 30;
				initGui();
				break;
			} // remove ceil
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		if (isWait || waitTime > 0 || subgui != null) { return; }
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		for (int slotId = 0; slotId < 2; ++slotId) {
            inventorySlots.getSlot(slotId).xPos = selected.isEmpty() ? -5000 : 180;
			inventorySlots.getSlot(slotId).yPos = selected.isEmpty() ? -5000 : slotId == 0 ? 123 : 159;
			int x = guiLeft + inventorySlots.getSlot(slotId).xPos;
			int y = guiTop + inventorySlots.getSlot(slotId).yPos;
			mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			drawTexturedModalRect(x - 1, y - 1, 0, 0, 18, 18);
		}
		if (!selected.isEmpty()) {
			int x = guiLeft + 5;
			int y = guiTop + 12;
			fontRenderer.drawString(new TextComponentTranslation("gui.name").getFormattedText() + ":", x, y, CustomNpcResourceListener.DefaultTextColor);
			fontRenderer.drawString(new TextComponentTranslation("gui.ceil").getFormattedText() + ":", x, (y += 22), CustomNpcResourceListener.DefaultTextColor);
			fontRenderer.drawString(new TextComponentTranslation("gui.start").getFormattedText() + ":", x, (y += 22), CustomNpcResourceListener.DefaultTextColor);
			fontRenderer.drawString(new TextComponentTranslation("gui.max").getFormattedText() + ":", x + 126, y, CustomNpcResourceListener.DefaultTextColor);
			x = guiLeft + 179;
			y = guiTop + 112;
			fontRenderer.drawString(new TextComponentTranslation("bank.tab.cost").getFormattedText() + ":", x, y, CustomNpcResourceListener.DefaultTextColor);
			fontRenderer.drawString(new TextComponentTranslation("bank.upg.cost").getFormattedText() + ":", x, y + 36, CustomNpcResourceListener.DefaultTextColor);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (isWait || waitTime > 0) {
			if (!isWait) {
				waitTime--;
				if (waitTime == 0) { initGui(); }
			}
			String text = new TextComponentTranslation("gui.wait", ": " + new TextComponentTranslation("gui.wait.data").getFormattedText()).getFormattedText();
			fontRenderer.drawString(text, guiLeft + (width - fontRenderer.getStringWidth(text)) / 2, guiTop + 60, CustomNpcs.LableColor.getRGB());
			return;
		}
		if (subgui != null || !CustomNpcs.ShowDescriptions || selected.isEmpty()) { return; }
		int x = guiLeft + 179;
		int y = guiTop + 112;
		if (isMouseHover(mouseX, mouseY, x, y, 60, 12)) {
			drawHoverText("bank.tab.cost.info", "" + bank.ceilSettings.get(ceil).startCells, "" + bank.ceilSettings.get(ceil).maxCells);
		}
		else if (isMouseHover(mouseX, mouseY, x, y + 36, 60, 12)) {
			drawHoverText("bank.upg.cost.info", "" + bank.ceilSettings.get(ceil).startCells, "" + bank.ceilSettings.get(ceil).maxCells);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(160, 180); }
		if (isWait || waitTime > 0) { return; }
		int x = guiLeft + 254;
		int y = guiTop + 8;
		scroll.guiLeft = x;
		scroll.guiTop = y;
		addScroll(scroll);
		if (!selected.isEmpty()) { scroll.setSelected(selected); }
		List<String> list = scroll.getList();
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		if (list != null && !list.isEmpty()) {
			int i = 0;
			for (String key : list) {
				hts.put(i, Collections.singletonList("ID: " + data.get(key)));
				i++;
			}
		}
		scroll.setHoverTexts(hts);
		// add bank
		y += scroll.height + 2;
		addButton(new GuiNpcButton(6, x, y, 50, 20, "gui.add")
				.setHoverText("bank.hover.add"));
		// del bank
		addButton(new GuiNpcButton(7, x + scroll.width - 50, y, 50, 20, "gui.remove")
				.setIsEnable(!selected.isEmpty() && data.size() > 1)
				.setHoverText(new TextComponentTranslation("bank.hover.del").appendSibling(new TextComponentTranslation("bank.hover.change")).getFormattedText()));
		// name
		x = guiLeft + 75;
		y = guiTop + 8;
		addTextField(new GuiNpcTextField(0, this, x, y, 160, 16, selected)
				.setIsVisible(!selected.isEmpty())
				.setHoverText("bank.hover.name"));
		getTextField(0).setMaxStringLength(20);
		// cells
		y += 22;
		List<String> csIds = new ArrayList<>();
		if (bank != null) {
			for (int i = 0; i < bank.ceilSettings.size(); i++) { csIds.add("" + (i + 1)); }
		}
		addButton(new GuiButtonBiDirectional(0, x, y, 50, 20, csIds.toArray(new String[0]), ceil)
				.setIsVisible(!selected.isEmpty())
				.setHoverText("bank.hover.ceil", "" + bank.ceilSettings.size()));
		// add ceil
		addButton(new GuiNpcButton(1, x + 55, y, 50, 20, "gui.add")
				.setIsVisible(!selected.isEmpty())
				.setHoverText(new TextComponentTranslation("bank.hover.ceil.add").appendSibling(new TextComponentTranslation("bank.hover.change")).getFormattedText()));
		// del ceil
		addButton(new GuiNpcButton(2, x + 110, y, 50, 20, "gui.remove")
				.setIsVisible(!selected.isEmpty())
				.setIsEnable(ceil > 0)
				.setHoverText(new TextComponentTranslation("bank.hover.ceil.add").appendSibling(new TextComponentTranslation("bank.hover.change")).getFormattedText()));
		// slots
		y += 22;
		CeilSettings cs = bank.ceilSettings.get(ceil);
		int sc = cs.startCells;
		int mc = cs.maxCells;
		// min
		addTextField(new GuiNpcTextField(1, this, x, y, 50, 18, "" + sc)
				.setIsVisible(!selected.isEmpty())
				.setMinMaxDefault(1, mc, sc)
				.setHoverText(new TextComponentTranslation("bank.hover.slots.min").appendSibling(new TextComponentTranslation("bank.hover.change")).getFormattedText()));
		// max
		addTextField(new GuiNpcTextField(2, this, x + 110, y, 50, 18, "" + mc)
				.setIsVisible(!selected.isEmpty())
				.setMinMaxDefault(1, 198, mc)
				.setHoverText(new TextComponentTranslation("bank.hover.slots.max").appendSibling(new TextComponentTranslation("bank.hover.change")).getFormattedText()));
		// is public
		addButton(new GuiNpcCheckBox(3, x, (y += 18), 160, 16, "bank.public.true", "bank.public.false", bank.isPublic)
				.setIsVisible(!selected.isEmpty())
				.setHoverText("bank.hover.public"));
		// setting names
		addButton(new GuiNpcButton(8, x, y + 20, 20, 20, 20, 146, GuiNPCInterface.WIDGETS)
				.setHoverText("bank.hover.settings")
				.setIsVisible(!selected.isEmpty() && bank.isPublic));
		// open money
		addTextField(new GuiNpcTextField(3, this, x += 126, y += 52, 50, 18, "" + cs.openMoney)
				.setIsVisible(!selected.isEmpty())
				.setMinMaxDefault(0, Integer.MAX_VALUE, cs.openMoney)
				.setHoverText("bank.hover.open.money"));
		// upgrade money
		addTextField(new GuiNpcTextField(4, this, x, y + 36, 50, 18, "" + cs.upgradeMoney)
				.setIsVisible(!selected.isEmpty())
				.setMinMaxDefault(0, Integer.MAX_VALUE, cs.upgradeMoney)
				.setHoverText("bank.hover.upgrade.money"));
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.BanksGet);
		isWait = false;
		waitTime = 30;
	}

	@Override
	public void save() {
		if (selected != null && data.containsKey(selected) && bank != null && bank.ceilSettings.containsKey(ceil)) {
			bank.ceilSettings.get(ceil).openStack = container.getSlot(0).getStack();
			bank.ceilSettings.get(ceil).upgradeStack = container.getSlot(1).getStack();
			NBTTagCompound compound = new NBTTagCompound();
			bank.writeToNBT(compound);
			isWait = true;
			waitTime = 30;
			initGui();
			Client.sendData(EnumPacketServer.BankSave, compound);
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getID() == 0 && !scroll.getSelected().equals(selected)) {
			save();
			ceil = 0;
			selected = scroll.getSelected();
			Client.sendData(EnumPacketServer.BankGet, data.get(selected), 0);
			isWait = true;
			waitTime = 30;
			initGui();
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { }

	@Override
	public void setData(Vector<String> dataList, HashMap<String, Integer> dataMap) {
		data.clear();
		data.putAll(dataMap);
		scroll.setList(dataList);
		isWait = false;
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (bank == null) { bank = new Bank(); }
		bank.readFromNBT(compound);
		if (compound.hasKey("CurrentCeil", 3)) { ceil = compound.getInteger("CurrentCeil"); }
		container.setBank(bank, ceil);
		selected = bank.name;
		isWait = false;
	}

	@Override
	public void setSelected(String sel) {
		scroll.setSelected(sel);
		selected = scroll.getSelected() == null ? "" : scroll.getSelected();
		isWait = false;
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (bank.id == -1) { return; }
		switch (textField.getID()) {
			case 0: {
				String name = textField.getText();
				if (!name.isEmpty() && !data.containsKey(name)) {
					String old = bank.name;
					data.remove(bank.name);
					bank.name = name;
					data.put(bank.name, bank.id);
					selected = name;
					scroll.replace(old, bank.name);
				}
				break;
			} // name
			case 1: {
				if (!textField.isInteger()) {
					textField.setText("" + textField.def);
					return;
				}
				bank.ceilSettings.get(ceil).startCells = textField.getInteger();
				break;
			} // startCells
			case 2: {
				if (!textField.isInteger()) {
					textField.setText("" + textField.def);
					return;
				}
				bank.ceilSettings.get(ceil).maxCells = textField.getInteger();
				break;
			} // maxCells
			case 3: {
				if (!textField.isInteger()) {
					textField.setText("" + textField.def);
					return;
				}
				bank.ceilSettings.get(ceil).openMoney = textField.getInteger();
				break;
			} // open money
			case 4: {
				if (!textField.isInteger()) {
					textField.setText("" + textField.def);
					return;
				}
				bank.ceilSettings.get(ceil).upgradeMoney = textField.getInteger();
				break;
			} // upgrade money
		}
	}

}
