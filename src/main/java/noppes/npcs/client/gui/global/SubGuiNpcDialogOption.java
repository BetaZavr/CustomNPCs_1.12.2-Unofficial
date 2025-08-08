package noppes.npcs.client.gui.global;

import java.awt.*;
import java.util.*;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.api.constants.OptionType;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.availability.SubGuiNpcAvailability;
import noppes.npcs.client.gui.player.GuiDialogInteract;
import noppes.npcs.client.gui.select.GuiDialogSelection;
import noppes.npcs.client.gui.util.*;
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
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 1: {
				option.optionType = OptionType.get(button.getValue());
				initGui();
				break;
			} // type
			case 2: {
				setSubGui(new SubGuiColorSelector(option.optionColor));
				break;
			} // color
			case 3: {
				if (option.optionType != OptionType.DIALOG_OPTION) { return; }
				setSubGui(new GuiDialogSelection(-1, 0));
				break;
			} // add dialog
			case 4: {
				if (option.optionType != OptionType.DIALOG_OPTION || select.isEmpty() || !data.containsKey(select)) { return; }
				option.dialogs.remove(data.get(select));
				initGui();
				break;
			} // del dialog
			case 5: {
				if (option.optionType != OptionType.DIALOG_OPTION || select.isEmpty() || !data.containsKey(select)) { return; }
				setSubGui(new GuiDialogSelection(data.get(select).dialogId, 1));
				break;
			} // edit dialog
			case 6: {
				if (option.optionType != OptionType.DIALOG_OPTION || select.isEmpty() || !data.containsKey(select)) { return; }
				option.upPos(data.get(select).dialogId);
				initGui();
				break;
			} // up dialog
			case 7: {
				if (option.optionType != OptionType.DIALOG_OPTION || select.isEmpty() || !data.containsKey(select)) { return; }
				option.downPos(data.get(select).dialogId);
				initGui();
				break;
			} // down dialog
			case 8: {
				if (select.isEmpty() || !data.containsKey(select)) { return; }
				setSubGui(new SubGuiNpcAvailability(data.get(select).availability, parent));
				break;
			} // availability
			case 9: {
				if (option == null) { return; }
				option.iconId = button.getValue();
				button.setTexture(GuiDialogInteract.icons.get(option.iconId));
				break;
			} // icons
			case 66: {
				close();
				break;
			} // exit
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		addLabel(new GuiNpcLabel(66, "dialog.editoption", guiLeft, guiTop + 4));
		getLabel(66).setCenter(xSize);
		addLabel(new GuiNpcLabel(0, "gui.title", guiLeft + 4, guiTop + 20));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, fontRenderer, guiLeft + 40, guiTop + 15, 196, 20, option.title);
		textField.setHoverText("dialog.option.hover.name");
		addTextField(textField);
		StringBuilder color = new StringBuilder(Integer.toHexString(option.optionColor));
		while (color.length() < 6) { color.insert(0, 0); }

		addLabel(new GuiNpcLabel(2, "gui.color", guiLeft + 4, guiTop + 45));
		GuiNpcButton button = new GuiNpcButton(2, guiLeft + 62, guiTop + 40, 92, 20, color.toString());
		button.setTextColor(option.optionColor);
		button.setHoverText("color.hover");
		addButton(button);

		List<String> list = new ArrayList<>();
		list.add("");
		for (ResourceLocation res : GuiDialogInteract.icons.values()) {
			list.add(res.getResourcePath().substring(res.getResourcePath().lastIndexOf("/") + 1, res.getResourcePath().lastIndexOf(".")));
		}
		addLabel(new GuiNpcLabel(9, "dialog.icon", guiLeft + 159, guiTop + 61));
		button = new GuiNpcButton(9, guiLeft + 210, guiTop + 45, 32, 32, list.toArray(new String[0]), option.iconId);
		button.hasDefBack = true;
		button.texture = GuiDialogInteract.icons.get(option.iconId);
		button.txrW = 256;
		button.txrH = 256;
		button.setHoverText("dialog.option.hover.name");
		addButton(button);

		addLabel(new GuiNpcLabel(1, "dialog.optiontype", guiLeft + 4, guiTop + 67));
		button = new GuiNpcButton(1, guiLeft + 62, guiTop + 62, 92, 20, options, option.optionType.get());
		button.setHoverText("dialog.option.hover.type." + option.optionType.get());
		addButton(button);

		if (option.optionType == OptionType.DIALOG_OPTION) { // next dialog
			data.clear();
			char c = ((char) 167);
			DialogController dData = DialogController.instance;
			List<String> keys = new ArrayList<>();
			int pos = -1, i = 0;
			OptionDialogID del = null;
			for (OptionDialogID od : option.dialogs) {
				if (od.dialogId <= 0) {
					del = od;
				}
				String key;
				Dialog d = dData.get(od.dialogId);
				if (d == null) {
					key = c + "7ID: " + od.dialogId + c + "c Dialog Not Found!";
				} else {
					key = d.getKey();
				}
				data.put(key, od);
				keys.add(key);
				if (key.equals(select)) {
					pos = i;
				}
				i++;
			}
			if (del != null) { option.dialogs.remove(del); }
			if (!data.containsKey(select)) { select = ""; }
			addLabel(new GuiNpcLabel(4, "gui.options", guiLeft + 4, guiTop + 84));
			if (scroll == null) { (scroll = new GuiCustomScroll(this, 0)).setSize(141, 116); }
			scroll.setList(new ArrayList<>());
			scroll.setListNotSorted(keys);
			scroll.guiLeft = guiLeft + 4;
			scroll.guiTop = guiTop + 96;
			if (!select.isEmpty()) { scroll.setSelected(select); }
			addScroll(scroll);
			button = new GuiNpcButton(3, guiLeft + 149, guiTop + 96, 50, 20, "gui.add");
			button.setHoverText("dialog.option.hover.add");
			addButton(button);
			button = new GuiNpcButton(4, guiLeft + 201, guiTop + 96, 50, 20, "gui.remove", !select.isEmpty());
			button.setHoverText("dialog.option.hover.del");
			addButton(button);
			button = new GuiNpcButton(5, guiLeft + 149, guiTop + 118, 80, 20, "gui.edit");
			button.setHoverText("dialog.option.hover.edit");
			addButton(button);
			button = new GuiNpcButton(6, guiLeft + 149, guiTop + 140, 50, 20, "type.up", !select.isEmpty() && pos != 0);
			button.setHoverText("dialog.option.hover.up");
			addButton(button);
			button = new GuiNpcButton(7, guiLeft + 201, guiTop + 140, 50, 20, "type.down", !select.isEmpty() && pos > -1 && pos < data.size() - 1);
			button.setHoverText("dialog.option.hover.down");
			addButton(button);
			button = new GuiNpcButton(8, guiLeft + 149, guiTop + 162, 80, 20, "availability.available");
        } else {
			button = new GuiNpcButton(8, guiLeft + 64, guiTop + 192, 80, 20, "availability.available");
        }
        button.setHoverText("dialog.option.hover.availability", select);
        addButton(button);
        if (option.optionType == OptionType.COMMAND_BLOCK) { // command
			textField = new GuiNpcTextField(4, this, fontRenderer, guiLeft + 4, guiTop + 84, 248, 20, option.command);
			textField.setMaxStringLength(Short.MAX_VALUE);
			textField.setHoverText("dialog.option.hover.command");
			addTextField(textField);
			addLabel(new GuiNpcLabel(4, "advMode.command", guiLeft + 4, guiTop + 110));
			addLabel(new GuiNpcLabel(5, "advMode.nearestPlayer", guiLeft + 4, guiTop + 125));
			addLabel(new GuiNpcLabel(6, "advMode.randomPlayer", guiLeft + 4, guiTop + 140));
			addLabel(new GuiNpcLabel(7, "advMode.allPlayers", guiLeft + 4, guiTop + 155));
			addLabel(new GuiNpcLabel(8, "dialog.commandoptionplayer", guiLeft + 4, guiTop + 170));
		}
		button = new GuiNpcButton(66, guiLeft + 149, guiTop + 192, 80, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiColorSelector) {
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
				option.addDialog(dialog.id);
				select = dialog.getKey();
			} else if (((GuiDialogSelection) subgui).id == 1 && !select.isEmpty()
					&& data.containsKey(select)) {
				option.replaceDialogIDs(data.get(select).dialogId, dialog.id); // edit
			}
		}
		initGui();
	}

	@Override
	public void unFocused(IGuiNpcTextField textfield) {
		if (textfield.getID() == 0) {
			if (textfield.isEmpty()) {
				option.title = "Talk";
				textfield.setFullText(option.title);
			} else {
				option.title = textfield.getFullText();
			}
		}
		else if (textfield.getID() == 4) {
			option.command = textfield.getFullText();
		}
	}

	// New from Unofficial (BetaZavr)
	@Override
	public void scrollClicked(int mouseX, int mouseY, int ticks, IGuiCustomScroll scroll) {
		if (option.optionType != OptionType.DIALOG_OPTION || scroll.getSelected() == null) { return; }
		select = scroll.getSelected();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) {
		if (option.optionType != OptionType.DIALOG_OPTION || select.isEmpty()  || !data.containsKey(select)) { return; }
		setSubGui(new GuiDialogSelection(data.get(select).dialogId, 1));
	}
}
