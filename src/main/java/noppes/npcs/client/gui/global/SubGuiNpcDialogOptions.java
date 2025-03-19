package noppes.npcs.client.gui.global;

import java.util.*;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.DialogOption.OptionDialogID;

public class SubGuiNpcDialogOptions
extends SubGuiInterface
implements ICustomScrollListener, ISubGuiListener, GuiYesNoCallback {

	private final Dialog dialog;
	private final Map<String, Integer> data = new TreeMap<>(); // {scrollTitle, dialogID}
	private GuiCustomScroll scroll;

	public final GuiScreen parent;

	public SubGuiNpcDialogOptions(Dialog d, GuiScreen gui) {
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		dialog = d;
		parent = gui;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0: { // add new
				DialogOption option = new DialogOption();
				option.slot = this.dialog.options.size();
				this.dialog.options.put(option.slot, option);
				option.optionColor = SubGuiNpcDialogOption.LastColor;
				this.scroll.setSelect(option.slot);
				this.setSubGui(new SubGuiNpcDialogOption(option, parent));
				break;
			}
			case 1: { // remove
				if (!this.data.containsKey(this.scroll.getSelected())) {
					return;
				}
				DialogOption option = this.dialog.options.get(this.data.get(this.scroll.getSelected()));
				GuiYesNo guiyesno = new GuiYesNo(this, "ID:" + option.slot + " - " + option.title,
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			}
			case 2: { // edit
				if (!this.data.containsKey(this.scroll.getSelected())) {
					return;
				}
				DialogOption option = this.dialog.options.get(this.data.get(this.scroll.getSelected()));
				if (option != null) {
					this.setSubGui(new SubGuiNpcDialogOption(option, parent));
				}
				break;
			}
			case 3: { // up dialog
				if (!this.data.containsKey(this.scroll.getSelected())) {
					return;
				}
				this.dialog.upPos(this.data.get(this.scroll.getSelected()));
				scroll.setSelect(scroll.getSelect() - 1);
				this.initGui();
				break;
			}
			case 4: { // down dialog
				if (!this.data.containsKey(this.scroll.getSelected())) {
					return;
				}
				this.dialog.downPos(this.data.get(this.scroll.getSelected()));
				scroll.setSelect(scroll.getSelect() + 1);
				this.initGui();
				break;
			}
			case 66: { // back
				this.close();
				break;
			}
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		if (this.parent instanceof GuiDialogEdit && ((GuiDialogEdit) this.parent).parent != null) {
			NoppesUtil.openGUI(this.player, ((GuiDialogEdit) this.parent).parent);
		} else {
			NoppesUtil.openGUI(this.player, this);
		}
		if (!result) {
			return;
		}
		this.dialog.options.remove(this.data.get(this.scroll.getSelected()));
		this.initGui();
	}

	private void fix() {
		Map<Integer, DialogOption> map = new TreeMap<>();
		int i = 0;
		boolean bo = false;
		for (int id : this.dialog.options.keySet()) {
			if (id != i) {
				bo = true;
			}
			DialogOption dlOp = this.dialog.options.get(id).copy();
			dlOp.slot = i;
			map.put(i, dlOp);
			i++;
		}
		if (bo) {
			this.dialog.options.clear();
			this.dialog.options.putAll(map);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(66, "dialog.options", this.guiLeft, this.guiTop + 4));
		this.getLabel(66).setCenter(this.xSize);
		this.data.clear();
		List<String> list = new ArrayList<>();
		List<Integer> colors = new ArrayList<>();
		fix();
		DialogController dData = DialogController.instance;
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		for (int id : this.dialog.options.keySet()) {
			DialogOption option = this.dialog.options.get(id);
			String key = ((char) 167) + "7ID:" + id + " ";
			if (option == null) {
				continue;
			}
			switch (option.optionType) {
			case COMMAND_BLOCK: {
				key += ((char) 167) + "eC";
				List<String> ht = new ArrayList<>();
				ht.add(new TextComponentTranslation("gui.type").getFormattedText() + ": " + option.optionType.get() + " - " + ((char) 167) + "e" + option.optionType.name());
				ht.add(new TextComponentTranslation("quest.has." + !option.command.isEmpty()).getFormattedText() + (!option.command.isEmpty() ? " - \"" + option.command + "\"" : ""));
				hts.put(id, ht);
				break;
			}
			case DIALOG_OPTION: {
				key += ((char) 167) + "3D";
				List<String> ht = new ArrayList<>();
				ht.add(new TextComponentTranslation("gui.type").getFormattedText() + ": " + option.optionType.get()
						+ " - " + ((char) 167) + "3" + option.optionType.name());
				if (option.hasDialogs()) {
					ht.add(new TextComponentTranslation("availability.selectdialog").getFormattedText() + ":");
					for (OptionDialogID od : option.dialogs) {
						if (!dData.hasDialog(od.dialogId)) {
							continue;
						}
						ht.add("ID: " + od.dialogId + " -"
								+ new TextComponentTranslation(
										"quest.task.item." + (dData.hasDialog(od.dialogId) ? "0" : "1"))
												.getFormattedText());
					}
				} else {
					ht.add(new TextComponentTranslation("quest.has.false").getFormattedText());
				}
				hts.put(id, ht);
				break;
			}
			case QUIT_OPTION: {
				key += ((char) 167) + "dE";
				hts.put(id, Collections.singletonList(new TextComponentTranslation("gui.type").getFormattedText() + ": " + option.optionType.get() + " - " + ((char) 167) + "d" + option.optionType.name()));
				break;
			}
			case ROLE_OPTION: {
				key += ((char) 167) + "aR";
				final List<String> ht = getHt(option);
				hts.put(id, ht);
				break;
			}
			case DISABLED: {
				key += ((char) 167) + "4N";
				hts.put(id, Collections.singletonList(new TextComponentTranslation("gui.type").getFormattedText() + ": " + option.optionType.get() + " - " + ((char) 167) + "4" + option.optionType.name()));
				break;
			}
			}
			key += ((char) 167) + "7 - \"" + ((char) 167) + "r" + option.title + ((char) 167) + "7\"";
			colors.add(option.optionColor);
			this.data.put(key, id);
			list.add(key);
		}
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(248, 154);
		}
		this.scroll.setListNotSorted(list);
		this.scroll.setColors(colors);
		this.scroll.setHoverTexts(hts);
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 14;
		this.addScroll(this.scroll);
		this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 170, 48, 20, "gui.add"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 54, this.guiTop + 170, 48, 20, "gui.remove"));
		this.getButton(1).setEnabled(this.scroll.getSelect() != -1);
		this.addButton(new GuiNpcButton(2, this.guiLeft + 104, this.guiTop + 170, 48, 20, "selectServer.edit"));
		this.getButton(2).setEnabled(this.scroll.getSelect() != -1);
		this.addButton(new GuiNpcButton(3, this.guiLeft + 154, this.guiTop + 170, 48, 20, "type.up",
				this.scroll.getSelect() != -1 && this.scroll.getSelect() != 0));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 204, this.guiTop + 170, 48, 20, "type.down",
				this.scroll.getSelect() != -1 && this.scroll.getSelect() > -1
						&& this.scroll.getSelect() < this.data.size() - 1));
		this.addButton(new GuiNpcButton(66, this.guiLeft + 82, this.guiTop + 192, 98, 20, "gui.done"));
	}

	private List<String> getHt(DialogOption option) {
		List<String> ht = new ArrayList<>();
		ht.add(new TextComponentTranslation("gui.type").getFormattedText() + ": " + option.optionType.get()
				+ " - " + ((char) 167) + "a" + option.optionType.name());
		ht.add(new TextComponentTranslation("role.name").getFormattedText() + " -"
				+ new TextComponentTranslation("quest.task.item."
						+ (this.npc != null && this.npc.advanced.roleInterface.getEnumType() != RoleType.DEFAULT
								? "0"
								: "1")).getFormattedText());
		return ht;
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, IGuiCustomScroll scroll) {
		if (!this.data.containsKey(scroll.getSelected())) {
			scroll.setSelect(-1);
			return;
		}
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
		if (!this.data.containsKey(scroll.getSelected())) {
			return;
		}
		DialogOption option = this.dialog.options.get(this.data.get(scroll.getSelected()));
		if (option == null) {
			return;
		}
		this.setSubGui(new SubGuiNpcDialogOption(option, parent));
	}

	@Override
	public void subGuiClosed(ISubGuiInterface subgui) {
		if (this.parent instanceof GuiDialogEdit && ((GuiDialogEdit) this.parent).parent != null) {
			NoppesUtil.openGUI(this.player, ((GuiDialogEdit) this.parent).parent);
		}
		this.initGui();
	}

}
