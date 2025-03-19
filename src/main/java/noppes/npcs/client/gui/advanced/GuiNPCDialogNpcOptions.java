package noppes.npcs.client.gui.advanced;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.select.GuiDialogSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCDialogNpcOptions
extends GuiNPCInterface2
implements GuiSelectionListener, IGuiData, ICustomScrollListener {

	private final HashMap<Integer, NBTTagCompound> data = new HashMap<>(); // slotID, dialogData
	private int selectedSlot = -1;
	private GuiCustomScroll scroll;
	private int error = 0;

	public GuiNPCDialogNpcOptions(EntityNPCInterface npc) {
		super(npc);
		drawDefaultBackground = true;
		Client.sendData(EnumPacketServer.DialogNpcGet);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 1: { // add
				selectedSlot = -1;
				setSubGui(new GuiDialogSelection(-1, 0));
				break;
			}
			case 2: { // del
				data.clear();
				Client.sendData(EnumPacketServer.DialogNpcRemove, selectedSlot);
				selectedSlot = -1;
				initGui();
				break;
			}
			case 3: { // change
				if (!data.containsKey(selectedSlot)) {
					return;
				}
				setSubGui(new GuiDialogSelection(data.get(selectedSlot).getInteger("Id"), 0));
				break;
			}
			case 4: { // up
				if (selectedSlot < 1) {
					return;
				}
				Client.sendData(EnumPacketServer.DialogNpcMove, selectedSlot, true);
				selectedSlot--;
				initGui();
				break;
			}
			case 5: { // down
				if (selectedSlot >= data.size()) {
					return;
				}
				Client.sendData(EnumPacketServer.DialogNpcMove, selectedSlot, false);
				selectedSlot++;
				initGui();
				break;
			}
			default: {

			}
		}
	}

	@Override
	public void close() {
		save();
		CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (error > 0) {
			if (scroll != null) { scroll.colorBack = 0xC0A00000; }
			error--;
			if (error <= 0 && scroll != null) { scroll.colorBack = 0xC0101010; }
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
		List<String> dialogs = new ArrayList<>();
		for (int slot : data.keySet()) {
			NBTTagCompound nbt = data.get(slot);
			String str = (slot + 1) + "; " + ((char) 167) + "7" + "ID:" + nbt.getInteger("Id") + " - ";
			str += ((char) 167) + "8" + nbt.getString("Category") + "/";
			str += ((char) 167) + "r" + nbt.getString("Title");
			dialogs.add(str);
		}
		if (scroll == null) {
			(scroll = new GuiCustomScroll(this, 0)).setSize(210, 196);
		}
		scroll.setListNotSorted(dialogs);
		scroll.guiLeft = guiLeft + 5;
		scroll.guiTop = guiTop + 14;
		if (selectedSlot >= 0 && data.containsKey(selectedSlot)) {
			scroll.setSelect(selectedSlot);
		} else {
			selectedSlot = -1;
			scroll.setSelect(-1);
		}
		addScroll(scroll);
		// add
		GuiNpcButton button = new GuiNpcButton(1, guiLeft + 220, guiTop + 14, 64, 20, "gui.add");
		button.setHoverText("dialog.hover.add");
		addButton(button);
		// del
		button = new GuiNpcButton(2, guiLeft + 220, guiTop + 36, 64, 20, "gui.remove");
		button.enabled = selectedSlot >= 0;
		button.setHoverText("dialog.hover.del");
		addButton(button);
		// edit
		button = new GuiNpcButton(3, guiLeft + 220, guiTop + 58, 64, 20, "advanced.editingmode");
		button.enabled = selectedSlot >= 0;
		button.setHoverText("dialog.hover.change");
		addButton(button);
		// up pos
		button = new GuiNpcButton(4, guiLeft + 220, guiTop + 102, 64, 20, "type.up");
		button.enabled = selectedSlot >= 0 && selectedSlot >= 1;
		button.setHoverText("dialog.hover.up");
		addButton(button);
		// down pos
		button = new GuiNpcButton(5, guiLeft + 220, guiTop + 124, 64, 20, "type.down");
		button.enabled = selectedSlot >= 0 && selectedSlot < (data.size() - 1);
		button.setHoverText("dialog.hover.down");
		addButton(button);
		// help
		GuiNpcLabel label = new GuiNpcLabel(6, new TextComponentTranslation("type.help").getFormattedText(), guiLeft + 230, guiTop + 150);
		label.backColor = 0x40FF0000;
		label.borderColor = 0x80808080;
		label.color = 0xFF000000;
		label.setHoverText("dialog.hover.info");
		addLabel(label);
		addLabel(new GuiNpcLabel(7, new TextComponentTranslation("dialog.dialogs").getFormattedText() + ":", guiLeft + 5, guiTop + 4));
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && subgui == null) {
			save();
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
		}
		super.keyTyped(c, i);
	}

	@Override
	public void save() { }

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		selectedSlot = scroll.getSelect();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
		// change
		if (!data.containsKey(selectedSlot)) {
			return;
		}
		setSubGui(new GuiDialogSelection(data.get(selectedSlot).getInteger("Id"), 0));
	}

	@Override
	public void selected(int id, String name) {
		if (selectedSlot < 0) {
			selectedSlot = data.size();
		}
		for (int slot : data.keySet()) {
			if (selectedSlot == slot) {
				continue;
			}
			if (data.get(slot).getInteger("Id") == id) {
				error = 60;
				ITextComponent end = new TextComponentTranslation("trader.busy");
				end.getStyle().setColor(TextFormatting.RED);
				player.sendMessage(CustomNpcs.prefix.appendSibling(new TextComponentTranslation("dialog.dialog")).appendSibling(new TextComponentString(((char) 167) + "7 ID:" + id + ((char) 167) + "r \"" + name + "\"" + ((char) 167) + "c - ")).appendSibling(end));
				return;
			}
		}
		Client.sendData(EnumPacketServer.DialogNpcSet, selectedSlot, id);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (!compound.getKeySet().isEmpty()) {
			int pos = compound.getInteger("Slot");
			data.put(pos, compound);
		}
		initGui();
	}

}
