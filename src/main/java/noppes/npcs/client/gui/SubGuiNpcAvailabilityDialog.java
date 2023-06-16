package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.select.GuiDialogSelection;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumAvailabilityDialog;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.Dialog;

public class SubGuiNpcAvailabilityDialog extends SubGuiInterface implements ICustomScrollListener, ISubGuiListener {
	private Availability availabitily;
	private String chr = new String(Character.toChars(0x00A7));
	private Map<String, EnumAvailabilityDialog> dataEnum;
	private Map<String, Integer> dataIDs;
	// New
	private GuiCustomScroll scroll;
	private String select;

	public SubGuiNpcAvailabilityDialog(Availability availabitily) {
		// this.slot = 0;
		this.availabitily = availabitily;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 217;
		this.closeOnEsc = true;
		// New
		this.dataIDs = new HashMap<String, Integer>();
		this.dataEnum = new HashMap<String, EnumAvailabilityDialog>();
		this.select = "";
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button.id == 0) {
			if (this.select.isEmpty()) {
				return;
			}
			EnumAvailabilityDialog ead = EnumAvailabilityDialog.values()[button.getValue()];
			int id = this.dataIDs.get(this.select);
			this.availabitily.dialogues.put(id, ead);
			Dialog dialog = DialogController.instance.dialogs.get(this.dataIDs.get(this.select));
			this.select = "ID:" + id + " - ";
			if (dialog == null) {
				this.select += chr + "4" + (new TextComponentTranslation("quest.notfound").getFormattedText());
			} else {
				this.select += chr + "7" + dialog.getCategory().getName() + "/" + chr + "r" + dialog.getName() + chr
						+ "7 (" + chr + "9"
						+ new TextComponentTranslation(("availability." + ead).toLowerCase()).getFormattedText() + chr
						+ "7)";
			}
			this.initGui();
		}
		if (button.id == 1) {
			this.setSubGui(new GuiDialogSelection(this.select.isEmpty() ? 0 : this.dataIDs.get(this.select), 0));
		}
		if (button.id == 2) {
			this.availabitily.dialogues.remove(this.dataIDs.get(this.select));
			this.select = "";
			this.initGui();
		}
		if (button.id == 3) { // More
			this.save();
			this.initGui();
		}
		if (button.id == 66) {
			this.close();
		}
	}

	// New
	@Override
	public void close() {
		super.close();
		List<Integer> delete = new ArrayList<Integer>();
		for (int id : this.availabitily.dialogues.keySet()) {
			if (this.availabitily.dialogues.get(id) == EnumAvailabilityDialog.Always) {
				delete.add(id);
			}
		}
		for (int id : delete) {
			this.availabitily.dialogues.remove(id);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (isMouseHover(mouseX, mouseY, this.guiLeft + 6, this.guiTop + this.ySize - 46, 50, 20)) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.enum.type").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + 58, this.guiTop + this.ySize - 46, 170, 20)) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.dialog").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + 230, this.guiTop + this.ySize - 46, 20, 20)) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.remove").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + this.xSize - 76, this.guiTop + 192, 70, 20)) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.more").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + 6, this.guiTop + 192, 70, 20)) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(1, "availability.available", this.guiLeft, this.guiTop + 4));
		this.getLabel(1).center(this.xSize);
		this.addButton(new GuiNpcButton(66, this.guiLeft + 6, this.guiTop + 192, 70, 20, "gui.done"));
		// New
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 6)).setSize(this.xSize - 12, this.ySize - 66);
		}
		this.dataIDs = new HashMap<String, Integer>();
		this.dataEnum = new HashMap<String, EnumAvailabilityDialog>();
		for (int id : this.availabitily.dialogues.keySet()) {
			String key = "ID:" + id + " - ";
			Dialog dialog = DialogController.instance.dialogs.get(id);
			if (dialog == null) {
				key += chr + "4" + (new TextComponentTranslation("quest.notfound").getFormattedText());
			} else {
				key += chr + "7" + dialog.getCategory().getName() + "/" + chr + "r" + dialog.getName() + chr + "7 ("
						+ chr + "9"
						+ new TextComponentTranslation(
								("availability." + this.availabitily.dialogues.get(id)).toLowerCase())
										.getFormattedText()
						+ chr + "7)";
			}
			this.dataIDs.put(key, id);
			this.dataEnum.put(key, this.availabitily.dialogues.get(id));
		}
		if (!this.select.isEmpty() && !this.dataIDs.containsKey(this.select)) {
			this.select = "";
		}
		this.scroll.setList(Lists.newArrayList(this.dataIDs.keySet()));
		this.scroll.guiLeft = this.guiLeft + 6;
		this.scroll.guiTop = this.guiTop + 14;
		if (!this.select.isEmpty()) {
			this.scroll.setSelected(this.select);
		}
		this.addScroll(this.scroll);
		int p = 0;
		if (!this.select.isEmpty()) {
			switch (this.dataEnum.get(this.select)) {
			case After: {
				p = 1;
				break;
			}
			case Before: {
				p = 2;
				break;
			}
			default: {
				p = 0;
			}
			}
		}
		this.addButton(new GuiNpcButton(0, this.guiLeft + 6, this.guiTop + this.ySize - 46, 50, 20,
				new String[] { "availability.always", "availability.after", "availability.before" }, p));
		this.addButton(
				new GuiNpcButton(1, this.guiLeft + 58, this.guiTop + this.ySize - 46, 170, 20, "availability.select"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 230, this.guiTop + this.ySize - 46, 20, 20, "X"));

		this.addButton(
				new GuiNpcButton(3, this.guiLeft + this.xSize - 76, this.guiTop + 192, 70, 20, "availability.more"));
		this.getButton(3).setEnabled(!this.select.isEmpty());

		this.updateGuiButtons();
	}

	@Override
	public void save() {
		if (this.select.isEmpty()) {
			return;
		}
		EnumAvailabilityDialog ead = EnumAvailabilityDialog.values()[this.getButton(0).getValue()];
		int id = this.dataIDs.get(this.select);
		if (ead != EnumAvailabilityDialog.Always) {
			this.availabitily.dialogues.put(id, ead);
			this.dataEnum.put(this.select, ead);
		} else {
			this.availabitily.dialogues.remove(id);
		}
		this.select = "";
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		this.select = scroll.getSelected();
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		this.setSubGui(new GuiDialogSelection(this.dataIDs.get(this.select), 0));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		GuiDialogSelection selector = (GuiDialogSelection) subgui;
		if (selector.selectedDialog == null) {
			return;
		}
		if (!this.select.isEmpty()) {
			this.availabitily.dialogues.remove(this.dataIDs.get(this.select));
		}
		this.select = "ID:" + selector.selectedDialog.id + " - " + chr + "7" + selector.selectedCategory.getName() + "/"
				+ chr + "r" + selector.selectedDialog.getName() + chr + "7 (" + chr + "9"
				+ new TextComponentTranslation("availability.after").getFormattedText() + chr + "7)";
		this.availabitily.dialogues.put(selector.selectedDialog.id, EnumAvailabilityDialog.After);
		this.initGui();
	}

	private void updateGuiButtons() {
		// New
		int p = 0;
		this.getButton(1).setDisplayText("availability.selectdialog");
		Dialog dialog = null;
		if (!this.select.isEmpty()) {
			dialog = DialogController.instance.dialogs.get(this.dataIDs.get(this.select));
			p = this.dataEnum.get(this.select).ordinal();
		}
		this.getButton(0).setDisplay(p);
		this.getButton(0).setEnabled(!this.select.isEmpty());
		this.getButton(1).setEnabled(p != 0 || this.select.isEmpty());
		this.getButton(1).setDisplayText(dialog == null ? "availability.select" : dialog.getName());
		this.getButton(2).setEnabled(p != 0);
	}

}
