package noppes.npcs.client.gui.global;

import java.awt.*;
import java.util.*;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.api.constants.OptionType;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.player.GuiDialogInteract;
import noppes.npcs.client.gui.select.GuiDialogSelection;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.DialogOption.OptionDialogID;

public class SubGuiNpcDialogOption
extends SubGuiInterface
implements ICustomScrollListener, ITextfieldListener, ISubGuiListener {

	private static final String[] options = new String[] { "gui.close", "dialog.dialog", "gui.disabled", "menu.role", "tile.commandBlock.name" };
	public static int LastColor = new Color(0xE0E0E0).getRGB();
	public final GuiScreen parent;

	private final Map<String, OptionDialogID> data = new HashMap<>(); // {scrollTitle, dialogID}
	private final DialogOption option;
	private GuiCustomScroll scroll;
	private String select = "";

	public SubGuiNpcDialogOption(DialogOption dialogOption, GuiScreen gui) {
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		parent = gui;
		option = dialogOption;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 1: { // type
				this.option.optionType = OptionType.get(button.getValue());
				this.initGui();
				break;
			}
			case 2: { // color
				this.setSubGui(new SubGuiColorSelector(this.option.optionColor));
				break;
			}
			case 3: { // add dialog
				if (this.option.optionType != OptionType.DIALOG_OPTION) {
					return;
				}
				this.setSubGui(new GuiDialogSelection(-1, 0));
				break;
			}
			case 4: { // del dialog
				if (this.option.optionType != OptionType.DIALOG_OPTION || this.select.isEmpty()
						|| !this.data.containsKey(this.select)) {
					return;
				}
				this.option.dialogs.remove(this.data.get(this.select));
				this.initGui();
				break;
			}
			case 5: { // edit dialog
				if (this.option.optionType != OptionType.DIALOG_OPTION || this.select.isEmpty()
						|| !this.data.containsKey(this.select)) {
					return;
				}
				this.setSubGui(new GuiDialogSelection(this.data.get(this.select).dialogId, 1));
				break;
			}
			case 6: { // up dialog
				if (this.option.optionType != OptionType.DIALOG_OPTION || this.select.isEmpty()
						|| !this.data.containsKey(this.select)) {
					return;
				}
				this.option.upPos(this.data.get(this.select).dialogId);
				this.initGui();
				break;
			}
			case 7: { // down dialog
				if (this.option.optionType != OptionType.DIALOG_OPTION || this.select.isEmpty()
						|| !this.data.containsKey(this.select)) {
					return;
				}
				this.option.downPos(this.data.get(this.select).dialogId);
				this.initGui();
				break;
			}
			case 8: { // availability
				if (this.select.isEmpty() || !this.data.containsKey(this.select)) {
					return;
				}
				this.setSubGui(new SubGuiNpcAvailability(data.get(select).availability, parent));
				break;
			}
			case 9: { // icons
				if (this.option == null) {
					return;
				}
				this.option.iconId = button.getValue();
				button.texture = GuiDialogInteract.icons.get(this.option.iconId);
				break;
			}
			case 66: { // exit
				this.close();
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(66, "dialog.editoption", this.guiLeft, this.guiTop + 4));
		this.getLabel(66).center(this.xSize);
		this.addLabel(new GuiNpcLabel(0, "gui.title", this.guiLeft + 4, this.guiTop + 20));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 40, this.guiTop + 15, 196, 20, this.option.title);
		textField.setHoverText("dialog.option.hover.name");
		addTextField(textField);
		StringBuilder color = new StringBuilder(Integer.toHexString(this.option.optionColor));
		while (color.length() < 6) { color.insert(0, 0); }

		this.addLabel(new GuiNpcLabel(2, "gui.color", this.guiLeft + 4, this.guiTop + 45));
		GuiNpcButton button = new GuiNpcButton(2, this.guiLeft + 62, this.guiTop + 40, 92, 20, color.toString());
		button.setTextColor(this.option.optionColor);
		button.setHoverText("color.hover");
		addButton(button);

		List<String> list = new ArrayList<>();
		list.add("");
		for (ResourceLocation res : GuiDialogInteract.icons.values()) {
			list.add(res.getResourcePath().substring(res.getResourcePath().lastIndexOf("/"), res.getResourcePath().lastIndexOf(".")));
		}
		this.addLabel(new GuiNpcLabel(9, "dialog.icon", this.guiLeft + 159, this.guiTop + 61));
		button = new GuiNpcButton(9, this.guiLeft + 210, this.guiTop + 45, 32, 32, list.toArray(new String[0]), this.option.iconId);
		button.hasDefBack = true;
		button.texture = GuiDialogInteract.icons.get(this.option.iconId);
		button.txrW = 256;
		button.txrH = 256;
		button.setHoverText("dialog.option.hover.name");
		addButton(button);

		this.addLabel(new GuiNpcLabel(1, "dialog.optiontype", this.guiLeft + 4, this.guiTop + 67));
		button = new GuiNpcButton(1, this.guiLeft + 62, this.guiTop + 62, 92, 20, options, this.option.optionType.get());
		button.setHoverText("dialog.option.hover.type." + option.optionType);
		addButton(button);

		if (this.option.optionType == OptionType.DIALOG_OPTION) { // next dialog
			this.data.clear();
			char c = ((char) 167);
			DialogController dData = DialogController.instance;
			List<String> keys = new ArrayList<>();
			int pos = -1, i = 0;
			OptionDialogID del = null;
			for (OptionDialogID od : this.option.dialogs) {
				if (od.dialogId <= 0) {
					del = od;
				}
				String key;
				Dialog d = (Dialog) dData.get(od.dialogId);
				if (d == null) {
					key = c + "7ID: " + od.dialogId + c + "c Dialog Not Found!";
				} else {
					key = d.getKey();
				}
				this.data.put(key, od);
				keys.add(key);
				if (key.equals(this.select)) {
					pos = i;
				}
				i++;
			}
			if (del != null) { this.option.dialogs.remove(del); }
			if (!this.data.containsKey(this.select)) { this.select = ""; }
			this.addLabel(new GuiNpcLabel(4, "gui.options", this.guiLeft + 4, this.guiTop + 84));
			if (this.scroll == null) { (this.scroll = new GuiCustomScroll(this, 0)).setSize(141, 116); }
			this.scroll.setList(new ArrayList<>());
			this.scroll.setListNotSorted(keys);
			this.scroll.guiLeft = this.guiLeft + 4;
			this.scroll.guiTop = this.guiTop + 96;
			if (!this.select.isEmpty()) { this.scroll.setSelected(this.select); }
			this.addScroll(this.scroll);
			button = new GuiNpcButton(3, this.guiLeft + 149, this.guiTop + 96, 50, 20, "gui.add");
			button.setHoverText("dialog.option.hover.add");
			addButton(button);
			button = new GuiNpcButton(4, this.guiLeft + 201, this.guiTop + 96, 50, 20, "gui.remove", !this.select.isEmpty());
			button.setHoverText("dialog.option.hover.вуд");
			addButton(button);
			button = new GuiNpcButton(5, this.guiLeft + 149, this.guiTop + 118, 80, 20, "gui.edit");
			button.setHoverText("dialog.option.hover.увше");
			addButton(button);
			button = new GuiNpcButton(6, this.guiLeft + 149, this.guiTop + 140, 50, 20, "type.up", !this.select.isEmpty() && pos != 0);
			button.setHoverText("dialog.option.hover.гз");
			addButton(button);
			button = new GuiNpcButton(7, this.guiLeft + 201, this.guiTop + 140, 50, 20, "type.down", !this.select.isEmpty() && pos > -1 && pos < this.data.size() - 1);
			button.setHoverText("dialog.option.hover.вщцт");
			addButton(button);
			button = new GuiNpcButton(8, this.guiLeft + 149, this.guiTop + 162, 80, 20, "availability.available");
        } else {
			button = new GuiNpcButton(8, this.guiLeft + 64, this.guiTop + 192, 80, 20, "availability.available");
        }
        button.setHoverText("dialog.option.hover.availability", select);
        addButton(button);
        if (this.option.optionType == OptionType.COMMAND_BLOCK) { // command
			textField = new GuiNpcTextField(4, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 84, 248, 20, this.option.command);
			textField.setMaxStringLength(Short.MAX_VALUE);
			textField.setHoverText("dialog.option.hover.command");
			addTextField(textField);
			this.addLabel(new GuiNpcLabel(4, "advMode.command", this.guiLeft + 4, this.guiTop + 110));
			this.addLabel(new GuiNpcLabel(5, "advMode.nearestPlayer", this.guiLeft + 4, this.guiTop + 125));
			this.addLabel(new GuiNpcLabel(6, "advMode.randomPlayer", this.guiLeft + 4, this.guiTop + 140));
			this.addLabel(new GuiNpcLabel(7, "advMode.allPlayers", this.guiLeft + 4, this.guiTop + 155));
			this.addLabel(new GuiNpcLabel(8, "dialog.commandoptionplayer", this.guiLeft + 4, this.guiTop + 170));
		}
		button = new GuiNpcButton(66, this.guiLeft + 149, this.guiTop + 192, 80, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int ticks, GuiCustomScroll scroll) {
		if (this.option.optionType != OptionType.DIALOG_OPTION || scroll.getSelected() == null) { return; }
		select = scroll.getSelected();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (option.optionType != OptionType.DIALOG_OPTION || select.isEmpty()  || !data.containsKey(this.select)) { return; }
		setSubGui(new GuiDialogSelection(data.get(select).dialogId, 1));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiColorSelector) {
			DialogOption option = this.option;
			int color = ((SubGuiColorSelector) subgui).color;
			option.optionColor = color;
			SubGuiNpcDialogOption.LastColor = color;
		}
		if (subgui instanceof GuiDialogSelection) {
			Dialog dialog = ((GuiDialogSelection) subgui).selectedDialog;
			if (dialog == null) {
				return;
			}
			if (((GuiDialogSelection) subgui).id == 0) {
				this.option.addDialog(dialog.id);
				this.select = dialog.getKey();
			} else if (((GuiDialogSelection) subgui).id == 1 && !this.select.isEmpty()
					&& this.data.containsKey(this.select)) {
				this.option.replaceDialogIDs(this.data.get(this.select).dialogId, dialog.id); // edit
			}
			this.initGui();
		}
		this.initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 0) {
			if (textfield.isEmpty()) {
				this.option.title = "Talk";
				textfield.setText(this.option.title);
			} else {
				this.option.title = textfield.getText();
			}
		}
		if (textfield.getId() == 4) {
			this.option.command = textfield.getText();
		}
	}

}
