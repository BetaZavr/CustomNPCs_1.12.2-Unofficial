package noppes.npcs.client.gui.availability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.select.GuiDialogSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumAvailabilityDialog;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.Dialog;

public class SubGuiNpcAvailabilityDialog
extends SubGuiInterface
implements ICustomScrollListener, ISubGuiListener {

	protected final Availability availability;
	protected final Map<String, EnumAvailabilityDialog> dataEnum = new HashMap<>();
	protected final Map<String, Integer> dataIDs = new HashMap<>();
	protected GuiCustomScroll scroll;
	protected String select = "";

	public SubGuiNpcAvailabilityDialog(Availability availability) {
		setBackground("menubg.png");
		xSize = 256;
		ySize = 217;
		closeOnEsc = true;

		this.availability = availability;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case  0: {
				if (select.isEmpty()) { return; }
				EnumAvailabilityDialog ead = EnumAvailabilityDialog.values()[button.getValue()];
				int id = dataIDs.get(select);
				availability.dialogues.put(id, ead);
				Dialog dialog = DialogController.instance.dialogs.get(dataIDs.get(select));
				select = "ID:" + id + " - ";
				if (dialog == null) {
					select += ((char) 167) + "4" + (new TextComponentTranslation("quest.notfound").getFormattedText());
				} else {
					select += ((char) 167) + "7" + dialog.getCategory().getName() + "/" + ((char) 167) + "r" + dialog.getName() + ((char) 167) + "7 (" + ((char) 167) + "9" + new TextComponentTranslation(("availability." + ead).toLowerCase()).getFormattedText() + ((char) 167) + "7)";
				}
				initGui();
				break;
			}
			case  1: {
				setSubGui(new GuiDialogSelection(select.isEmpty() ? 0 : dataIDs.get(select), 0));
				break;
			}
			case  2: {
				availability.dialogues.remove(dataIDs.get(select));
				select = "";
				initGui();
				break;
			}
			case  3: {
				save();
				initGui();
				break;
			}
			case 66 : close(); break;
		}
	}

	@Override
	public void close() {
		super.close();
		List<Integer> delete = new ArrayList<>();
		for (int id : availability.dialogues.keySet()) {
			if (availability.dialogues.get(id) == EnumAvailabilityDialog.Always) {
				delete.add(id);
			}
		}
		for (int id : delete) {
			availability.dialogues.remove(id);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// title
		GuiNpcLabel label = new GuiNpcLabel(1, "availability.available", guiLeft, guiTop + 4);
		label.setCenter(xSize);
		addLabel(label);
		// exit
		GuiNpcButton button = new GuiNpcButton(66, guiLeft + 6, guiTop + 192, 70, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
		// data
		if (scroll == null) { (scroll = new GuiCustomScroll(this, 6)).setSize(xSize - 12, ySize - 66); }
		dataIDs.clear();
		dataEnum.clear();
		for (int id : availability.dialogues.keySet()) {
			String key = "ID:" + id + " - ";
			Dialog dialog = DialogController.instance.dialogs.get(id);
			if (dialog == null) {
				key += ((char) 167) + "4" + (new TextComponentTranslation("quest.notfound").getFormattedText());
			} else {
				key += ((char) 167) + "7" + dialog.getCategory().getName() + "/" + ((char) 167) + "r" + dialog.getName() + ((char) 167) + "7 (" + ((char) 167) + "9" + new TextComponentTranslation(("availability." + availability.dialogues.get(id)).toLowerCase()).getFormattedText() + ((char) 167) + "7)";
			}
			dataIDs.put(key, id);
			dataEnum.put(key, availability.dialogues.get(id));
		}
		if (!select.isEmpty() && !dataIDs.containsKey(select)) { select = ""; }
		scroll.setList(new ArrayList<>(dataIDs.keySet()));
		scroll.guiLeft = guiLeft + 6;
		scroll.guiTop = guiTop + 14;
		if (!select.isEmpty()) { scroll.setSelected(select); }
		addScroll(scroll);
		int p = 0;
		if (!select.isEmpty()) {
			switch (dataEnum.get(select)) {
				case After: {
					p = 1;
					break;
				}
				case Before: {
					p = 2;
					break;
				}
				default: {
				}
			}
		}
		// type
		button = new GuiNpcButton(0, guiLeft + 6, guiTop + ySize - 46, 50, 20, new String[] { "availability.always", "availability.after", "availability.before" }, p);
		button.setHoverText("availability.hover.enum.type");
		addButton(button);
		// select
		button = new GuiNpcButton(1, guiLeft + 58, guiTop + ySize - 46, 170, 20, "availability.select");
		button.setHoverText("availability.hover.dialog");
		addButton(button);
		// del
		button = new GuiNpcButton(2, guiLeft + 230, guiTop + ySize - 46, 20, 20, "X");
		button.setHoverText("availability.hover.remove");
		addButton(button);
		// extra
		button = new GuiNpcButton(3, guiLeft + xSize - 76, guiTop + 192, 70, 20, "availability.more");
		button.setEnabled(!select.isEmpty());
		button.setHoverText("availability.hover.more");
		addButton(button);

		updateGuiButtons();
	}

	@Override
	public void save() {
		if (select.isEmpty()) {
			return;
		}
		EnumAvailabilityDialog ead = EnumAvailabilityDialog.values()[getButton(0).getValue()];
		int id = dataIDs.get(select);
		if (ead != EnumAvailabilityDialog.Always) {
			availability.dialogues.put(id, ead);
			dataEnum.put(select, ead);
		} else {
			availability.dialogues.remove(id);
		}
		select = "";
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		select = scroll.getSelected();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
		setSubGui(new GuiDialogSelection(dataIDs.get(select), 0));
	}

	@Override
	public void subGuiClosed(ISubGuiInterface subgui) {
		GuiDialogSelection selector = (GuiDialogSelection) subgui;
		if (selector.selectedDialog == null) {
			return;
		}
		if (!select.isEmpty()) {
			availability.dialogues.remove(dataIDs.get(select));
		}
		select = "ID:" + selector.selectedDialog.id + " - " + ((char) 167) + "7" + selector.selectedCategory.getName() + "/" + ((char) 167) + "r" + selector.selectedDialog.getName() + ((char) 167) + "7 (" + ((char) 167) + "9" + new TextComponentTranslation("availability.after").getFormattedText() + ((char) 167) + "7)";
		availability.dialogues.put(selector.selectedDialog.id, EnumAvailabilityDialog.After);
		initGui();
	}

	private void updateGuiButtons() {
		int p = 0;
		getButton(1).setDisplayText("availability.selectdialog");
		Dialog dialog = null;
		if (!select.isEmpty()) {
			dialog = DialogController.instance.dialogs.get(dataIDs.get(select));
			p = dataEnum.get(select).ordinal();
		}
		getButton(0).setDisplay(p);
		getButton(0).setEnabled(!select.isEmpty());
		getButton(1).setEnabled(p != 0 || select.isEmpty());
		getButton(1).setDisplayText(dialog == null ? "availability.select" : dialog.getName());
		getButton(2).setEnabled(p != 0);
	}

}
