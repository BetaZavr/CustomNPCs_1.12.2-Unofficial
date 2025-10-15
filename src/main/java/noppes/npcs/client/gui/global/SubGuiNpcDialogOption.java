package noppes.npcs.client.gui.global;

import java.awt.*;
import java.util.*;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.api.constants.OptionType;
import noppes.npcs.client.gui.select.SubGuiColorSelector;
import noppes.npcs.client.gui.availability.SubGuiNpcAvailability;
import noppes.npcs.client.gui.player.GuiDialogInteract;
import noppes.npcs.client.gui.select.SubGuiDialogSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.DialogOption.OptionDialogID;

import javax.annotation.Nonnull;

public class SubGuiNpcDialogOption extends SubGuiInterface implements ICustomScrollListener, ITextfieldListener {

	protected static final String[] options = new String[] { "gui.close", "dialog.dialog", "gui.disabled", "menu.role", "tile.commandBlock.name" };
	public static int LastColor = new Color(0xE0E0E0).getRGB();

	protected final Map<String, OptionDialogID> data = new HashMap<>(); // {scrollTitle, dialogID}
	protected final DialogOption option;
	protected GuiCustomScroll scroll;
	protected String select = "";
	public final GuiScreen parent;

	public SubGuiNpcDialogOption(DialogOption dialogOption, GuiScreen gui) {
		super(0);
		setBackground("menubg.png");
		closeOnEsc = true;
		xSize = 256;
		ySize = 216;

		parent = gui;
		option = dialogOption;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 1: {
				option.optionType = OptionType.get(button.getValue());
				initGui();
				break;
			} // type
			case 2: setSubGui(new SubGuiColorSelector(option.optionColor)); break; // color
			case 3: {
				if (option.optionType != OptionType.DIALOG_OPTION) { return; }
				setSubGui(new SubGuiDialogSelection(-1, 0));
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
				setSubGui(new SubGuiDialogSelection(data.get(select).dialogId, 1));
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
			case 66: onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		addLabel(new GuiNpcLabel(66, "dialog.editoption", guiLeft, guiTop + 4)
				.setCenter(xSize));
		addLabel(new GuiNpcLabel(0, "gui.title", guiLeft + 4, guiTop + 20));
		addTextField(new GuiNpcTextField(0, this, guiLeft + 40, guiTop + 15, 196, 20, option.title)
				.setHoverText("dialog.option.hover.name"));
		StringBuilder color = new StringBuilder(Integer.toHexString(option.optionColor));
		while (color.length() < 6) { color.insert(0, 0); }
		addLabel(new GuiNpcLabel(2, "gui.color", guiLeft + 4, guiTop + 45));
		addButton(new GuiNpcButton(2, guiLeft + 62, guiTop + 40, 92, 20, color.toString())
				.setTextColor(option.optionColor)
				.setHoverText("color.hover"));
		List<String> list = new ArrayList<>();
		list.add("");
		for (ResourceLocation res : GuiDialogInteract.icons.values()) {
			list.add(res.getResourcePath().substring(res.getResourcePath().lastIndexOf("/") + 1, res.getResourcePath().lastIndexOf(".")));
		}
		addLabel(new GuiNpcLabel(9, "dialog.icon", guiLeft + 159, guiTop + 61));
		addButton(new GuiNpcButton(9, guiLeft + 210, guiTop + 45, 32, 32, list.toArray(new String[0]), option.iconId)
				.setTexture(GuiDialogInteract.icons.get(option.iconId))
				.setUV(0, 0, 256, 256)
				.setHasDefaultBack(true)
				.setHoverText("dialog.option.hover.name"));
		addLabel(new GuiNpcLabel(1, "dialog.optiontype", guiLeft + 4, guiTop + 67));
		addButton(new GuiNpcButton(1, guiLeft + 62, guiTop + 62, 92, 20, options, option.optionType.get())
				.setHoverText("dialog.option.hover.type." + option.optionType.get()));
		if (option.optionType == OptionType.DIALOG_OPTION) { // next dialog
			data.clear();
			char c = ((char) 167);
			DialogController dData = DialogController.instance;
			List<String> keys = new ArrayList<>();
			int pos = -1, i = 0;
			OptionDialogID del = null;
			for (OptionDialogID od : option.dialogs) {
				if (od.dialogId <= 0) { del = od; }
				String key;
				Dialog d = dData.get(od.dialogId);
				if (d == null) { key = c + "7ID: " + od.dialogId + c + "c Dialog Not Found!"; }
				else { key = d.getKey(); }
				data.put(key, od);
				keys.add(key);
				if (key.equals(select)) { pos = i; }
				i++;
			}
			if (del != null) { option.dialogs.remove(del); }
			if (!data.containsKey(select)) { select = ""; }
			addLabel(new GuiNpcLabel(4, "gui.options", guiLeft + 4, guiTop + 84));
			if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(141, 116); }
			scroll.guiLeft = guiLeft + 4;
			scroll.guiTop = guiTop + 96;
			scroll.setList(new ArrayList<>())
					.setUnsortedList(keys);
			if (!select.isEmpty()) { scroll.setSelected(select); }
			addScroll(scroll);
			addButton(new GuiNpcButton(3, guiLeft + 149, guiTop + 96, 50, 20, "gui.add")
					.setHoverText("dialog.option.hover.add"));
			addButton(new GuiNpcButton(4, guiLeft + 201, guiTop + 96, 50, 20, "gui.remove", !select.isEmpty())
					.setHoverText("dialog.option.hover.del"));
			addButton(new GuiNpcButton(5, guiLeft + 149, guiTop + 118, 80, 20, "gui.edit")
					.setHoverText("dialog.option.hover.edit"));
			addButton(new GuiNpcButton(6, guiLeft + 149, guiTop + 140, 50, 20, "type.up", !select.isEmpty() && pos != 0)
					.setHoverText("dialog.option.hover.up"));
			addButton(new GuiNpcButton(7, guiLeft + 201, guiTop + 140, 50, 20, "type.down", !select.isEmpty() && pos > -1 && pos < data.size() - 1)
					.setHoverText("dialog.option.hover.down"));
			addButton(new GuiNpcButton(8, guiLeft + 149, guiTop + 162, 80, 20, "availability.available")
					.setIsEnable(!select.isEmpty())
					.setHoverText("dialog.option.hover.availability", select));
        } else {
			addButton(new GuiNpcButton(8, guiLeft + 64, guiTop + 192, 80, 20, "availability.available")
					.setHoverText("dialog.option.hover.availability", select));
        }
        if (option.optionType == OptionType.COMMAND_BLOCK) { // command
			addTextField(new GuiNpcTextField(4, this, guiLeft + 4, guiTop + 84, 248, 20, option.command)
					.setHoverText("dialog.option.hover.command"));
			getTextField(4).setMaxStringLength(Short.MAX_VALUE);
			addLabel(new GuiNpcLabel(4, "advMode.command", guiLeft + 4, guiTop + 110));
			addLabel(new GuiNpcLabel(5, "advMode.nearestPlayer", guiLeft + 4, guiTop + 125));
			addLabel(new GuiNpcLabel(6, "advMode.randomPlayer", guiLeft + 4, guiTop + 140));
			addLabel(new GuiNpcLabel(7, "advMode.allPlayers", guiLeft + 4, guiTop + 155));
			addLabel(new GuiNpcLabel(8, "dialog.commandoptionplayer", guiLeft + 4, guiTop + 170));
		}
		addButton(new GuiNpcButton(66, guiLeft + 149, guiTop + 192, 80, 20, "gui.done")
				.setHoverText("hover.back"));
	}

	@Override
	public void subGuiClosed(GuiScreen subgui) {
		if (subgui instanceof SubGuiColorSelector) {
			int color = ((SubGuiColorSelector) subgui).color;
			option.optionColor = color;
			SubGuiNpcDialogOption.LastColor = color;
		}
		if (subgui instanceof SubGuiDialogSelection) {
			Dialog dialog = ((SubGuiDialogSelection) subgui).selectedDialog;
			if (dialog == null) { return; }
			if (((SubGuiDialogSelection) subgui).id == 0) {
				option.addDialog(dialog.id);
				select = dialog.getKey();
			}
			else if (((SubGuiDialogSelection) subgui).id == 1 && !select.isEmpty() && data.containsKey(select)) {
				option.replaceDialogIDs(data.get(select).dialogId, dialog.id); // edit
			}
		}
		initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (textField.getID() == 0) {
			if (textField.isEmpty()) {
				option.title = "Talk";
				textField.setText(option.title);
			}
			else { option.title = textField.getText(); }
		}
		else if (textField.getID() == 4) { option.command = textField.getText(); }
	}

	// New from Unofficial (BetaZavr)
	@Override
	public void scrollClicked(int mouseX, int mouseY, int ticks, GuiCustomScroll scroll) {
		if (option.optionType != OptionType.DIALOG_OPTION || scroll.getSelected() == null) { return; }
		select = scroll.getSelected();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (option.optionType != OptionType.DIALOG_OPTION || select.isEmpty()  || !data.containsKey(select)) { return; }
		setSubGui(new SubGuiDialogSelection(data.get(select).dialogId, 1));
	}

}
