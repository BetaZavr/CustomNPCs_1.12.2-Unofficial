package noppes.npcs.client.gui.advanced;

import java.util.ArrayList;
import java.util.Arrays;
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
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCDialogNpcOptions extends GuiNPCInterface2 implements GuiSelectionListener, IGuiData, ICustomScrollListener, ISubGuiListener {

	private final HashMap<Integer, NBTTagCompound> data = new HashMap<>(); // slotID, dialogData
	private int selectedSlot = -1;
	// New
	private GuiCustomScroll scroll;
	private int error = 0;

	public GuiNPCDialogNpcOptions(EntityNPCInterface npc) {
		super(npc);
		this.drawDefaultBackground = true;
		Client.sendData(EnumPacketServer.DialogNpcGet);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
		case 1: { // add
			this.selectedSlot = -1;
			this.setSubGui(new GuiDialogSelection(-1, 0));
			break;
		}
		case 2: { // del
			this.data.clear();
			Client.sendData(EnumPacketServer.DialogNpcRemove, this.selectedSlot);
			this.selectedSlot = -1;
			this.initGui();
			break;
		}
		case 3: { // change
			if (!this.data.containsKey(this.selectedSlot)) {
				return;
			}
			this.setSubGui(new GuiDialogSelection(this.data.get(this.selectedSlot).getInteger("Id"), 0));
			break;
		}
		case 4: { // up
			if (this.selectedSlot < 1) {
				return;
			}
			Client.sendData(EnumPacketServer.DialogNpcMove, this.selectedSlot, true);
			this.selectedSlot--;
			this.initGui();
			break;
		}
		case 5: { // down
			if (this.selectedSlot >= this.data.size()) {
				return;
			}
			Client.sendData(EnumPacketServer.DialogNpcMove, this.selectedSlot, false);
			this.selectedSlot++;
			this.initGui();
			break;
		}
		default: {

		}
		}
		/*
		 * if (id >= 0 && id < 20) { //this.selectedSlot = id; int dialogID = -1; if
		 * (this.data.containsKey(id)) { dialogID = this.data.get(id).getInteger("Id");
		 * } this.setSubGui(new GuiDialogSelection(dialogID)); } if (id >= 20 && id <
		 * 40) { int slot = id - 20; this.data.remove(slot);
		 * Client.sendData(EnumPacketServer.DialogNpcRemove, slot); this.initGui(); }
		 */
	}

	@Override
	public void close() {
		this.save();
		CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		if (this.error > 0) {
			if (this.scroll != null) {
				this.scroll.colorBack = 0xC0A00000;
			}
			this.error--;
			if (this.error <= 0) {
				if (this.scroll != null) {
					this.scroll.colorBack = 0xC0101010;
				}
			}
		}
		super.drawScreen(i, j, f);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.add").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.del").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.change").getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.up").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.down").getFormattedText());
		} else if (this.getLabel(6) != null && this.getLabel(6).hovered) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.info").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		List<String> dialogs = new ArrayList<>();
		for (int slot : this.data.keySet()) {
			NBTTagCompound nbt = this.data.get(slot);
			String str = (slot + 1) + "; " + ((char) 167) + "7" + "ID:" + nbt.getInteger("Id") + " - ";
			str += ((char) 167) + "8" + nbt.getString("Category") + "/";
			str += ((char) 167) + "r" + nbt.getString("Title");
			dialogs.add(str);
		}
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(210, 196);
		}
		this.scroll.setListNotSorted(dialogs);
		this.scroll.guiLeft = this.guiLeft + 5;
		this.scroll.guiTop = this.guiTop + 14;
		if (this.selectedSlot >= 0 && this.data.containsKey(this.selectedSlot)) {
			this.scroll.selected = this.selectedSlot;
		} else {
			this.selectedSlot = -1;
			this.scroll.selected = -1;
		}
		this.addScroll(this.scroll);
		this.addButton(new GuiNpcButton(1, this.guiLeft + 220, this.guiTop + 14, 64, 20, "gui.add"));
		GuiNpcButton button = new GuiNpcButton(2, this.guiLeft + 220, this.guiTop + 36, 64, 20, "gui.remove");
		button.enabled = this.selectedSlot >= 0;
		this.addButton(button);
		button = new GuiNpcButton(3, this.guiLeft + 220, this.guiTop + 58, 64, 20, "advanced.editingmode");
		button.enabled = this.selectedSlot >= 0;
		this.addButton(button);

		button = new GuiNpcButton(4, this.guiLeft + 220, this.guiTop + 102, 64, 20, "type.up");
		button.enabled = this.selectedSlot >= 0 && this.selectedSlot >= 1;
		this.addButton(button);
		button = new GuiNpcButton(5, this.guiLeft + 220, this.guiTop + 124, 64, 20, "type.down");
		button.enabled = this.selectedSlot >= 0 && this.selectedSlot < (this.data.size() - 1);
		this.addButton(button);

		GuiNpcLabel label = new GuiNpcLabel(6, new TextComponentTranslation("type.help").getFormattedText(),
				this.guiLeft + 230, this.guiTop + 150);
		label.backColor = 0x40FF0000;
		label.borderColor = 0x80808080;
		label.color = 0xFF000000;
		this.addLabel(label);
		this.addLabel(new GuiNpcLabel(7, new TextComponentTranslation("dialog.dialogs").getFormattedText() + ":",
				this.guiLeft + 5, this.guiTop + 4));
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui == null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
		}
		super.keyTyped(c, i);
	}

	@Override
	public void save() {
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		this.selectedSlot = scroll.selected;
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		// change
		if (!this.data.containsKey(this.selectedSlot)) {
			return;
		}
		this.setSubGui(new GuiDialogSelection(this.data.get(this.selectedSlot).getInteger("Id"), 0));
	}

	@Override
	public void selected(int id, String name) {
		if (this.selectedSlot < 0) {
			this.selectedSlot = this.data.size();
		} // new
		for (int slot : this.data.keySet()) {
			if (this.selectedSlot == slot) {
				continue;
			}
			if (this.data.get(slot).getInteger("Id") == id) {
				this.error = 60;
				ITextComponent end = new TextComponentTranslation("trader.busy");
				end.getStyle().setColor(TextFormatting.RED);
				this.player
						.sendMessage(
								CustomNpcs.prefix.appendSibling(new TextComponentTranslation("dialog.dialog"))
										.appendSibling(new TextComponentString(((char) 167) + "7 ID:" + id
												+ ((char) 167) + "r \"" + name + "\"" + ((char) 167) + "c - "))
										.appendSibling(end));
				return;
			}
		}
		Client.sendData(EnumPacketServer.DialogNpcSet, this.selectedSlot, id);
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (!compound.getKeySet().isEmpty()) {
			int pos = compound.getInteger("Slot");
			this.data.put(pos, compound);
		}
		this.initGui();
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {

	}

}
