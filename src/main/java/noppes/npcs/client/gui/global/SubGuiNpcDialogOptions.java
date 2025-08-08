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
import noppes.npcs.entity.EntityNPCInterface;

public class SubGuiNpcDialogOptions
extends SubGuiInterface
implements ICustomScrollListener, ISubGuiListener, GuiYesNoCallback {

	private final Dialog dialog;
	private final Map<String, Integer> data = new TreeMap<>(); // {scrollTitle, dialogID}
	private GuiCustomScroll scroll;

	// New from Unofficial (BetaZavr)
	public final GuiScreen parent;

	public SubGuiNpcDialogOptions(EntityNPCInterface npcIn, Dialog d, GuiScreen gui) {
		super(npcIn);
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
			case 0: {
				DialogOption option = new DialogOption();
				option.slot = dialog.options.size();
				dialog.options.put(option.slot, option);
				option.optionColor = SubGuiNpcDialogOption.LastColor;
				scroll.setSelect(option.slot);
				setSubGui(new SubGuiNpcDialogOption(option, parent));
				break;
			} // add new
			case 1: {
				if (!data.containsKey(scroll.getSelected())) { return; }
				DialogOption option = dialog.options.get(data.get(scroll.getSelected()));
				GuiYesNo guiyesno = new GuiYesNo(this, "ID:" + option.slot + " - " + option.title,
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			} // remove
			case 2: {
				if (!data.containsKey(scroll.getSelected())) { return; }
				DialogOption option = dialog.options.get(data.get(scroll.getSelected()));
				if (option != null) { setSubGui(new SubGuiNpcDialogOption(option, parent)); }
				break;
			} // edit
			case 3: {
				if (!data.containsKey(scroll.getSelected())) { return; }
				dialog.upPos(data.get(scroll.getSelected()));
				scroll.setSelect(scroll.getSelect() - 1);
				initGui();
				break;
			} // up dialog
			case 4: {
				if (!data.containsKey(scroll.getSelected())) { return; }
				dialog.downPos(data.get(scroll.getSelected()));
				scroll.setSelect(scroll.getSelect() + 1);
				initGui();
				break;
			} // down dialog
			case 66: {
				close();
				break;
			} // back
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		if (parent instanceof GuiDialogEdit && ((GuiDialogEdit) parent).parent != null) {
			NoppesUtil.openGUI(player, ((GuiDialogEdit) parent).parent);
		} else {
			NoppesUtil.openGUI(player, this);
		}
		if (!result) {
			return;
		}
		dialog.options.remove(data.get(scroll.getSelected()));
		initGui();
	}

	@Override
	public void initGui() {
		super.initGui();
		addLabel(new GuiNpcLabel(66, "dialog.options", guiLeft, guiTop + 4));
		getLabel(66).setCenter(xSize);
		data.clear();
		List<String> list = new ArrayList<>();
		List<Integer> colors = new ArrayList<>();
		fix();
		DialogController dData = DialogController.instance;
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		for (int id : dialog.options.keySet()) {
			DialogOption option = dialog.options.get(id);
			String key = ((char) 167) + "7ID:" + id + " ";
			if (option == null) { continue; }
			switch (option.optionType) {
				case COMMAND_BLOCK: {
					key += ((char) 167) + "eC";
					List<String> hovers = new ArrayList<>();
					hovers.add(new TextComponentTranslation("gui.type").getFormattedText() + ": " + option.optionType.get() + " - " + ((char) 167) + "e" + option.optionType.name());
					hovers.add(new TextComponentTranslation("quest.has." + !option.command.isEmpty()).getFormattedText() + (!option.command.isEmpty() ? " - \"" + option.command + "\"" : ""));
					hts.put(id, hovers);
					break;
				}
				case DIALOG_OPTION: {
					key += ((char) 167) + "3D";
					List<String> hovers = new ArrayList<>();
					hovers.add(new TextComponentTranslation("gui.type").getFormattedText() + ": " + option.optionType.get()
							+ " - " + ((char) 167) + "3" + option.optionType.name());
					if (option.hasDialogs()) {
						hovers.add(new TextComponentTranslation("availability.selectdialog").getFormattedText() + ":");
						for (OptionDialogID od : option.dialogs) {
							String hd = "ID: " + od.dialogId + " -";
							if (dData.hasDialog(od.dialogId)) {
								hd += " \"" + new TextComponentTranslation(dData.get(od.dialogId).title).getFormattedText() +
										"\" " + new TextComponentTranslation("quest.task.item.0").getFormattedText();
							}
							else {
								hd += new TextComponentTranslation("quest.task.item.1").getFormattedText();
							}
							hovers.add(hd);
						}
					} else {
						hovers.add(new TextComponentTranslation("quest.has.false").getFormattedText());
					}
					hts.put(id, hovers);
					break;
				}
				case QUIT_OPTION: {
					key += ((char) 167) + "dE";
					hts.put(id, Collections.singletonList(new TextComponentTranslation("gui.type").getFormattedText() + ": " + option.optionType.get() + " - " + ((char) 167) + "d" + option.optionType.name()));
					break;
				}
				case ROLE_OPTION: {
					key += ((char) 167) + "aR";
					List<String> hovers = new ArrayList<>();
					hovers.add(new TextComponentTranslation("gui.type").getFormattedText() + ": " + option.optionType.get()
							+ " - " + ((char) 167) + "a" + option.optionType.name());
					hovers.add(new TextComponentTranslation("role.name").getFormattedText() + " -"
							+ new TextComponentTranslation("quest.task.item."
							+ (npc != null && npc.advanced.roleInterface.getEnumType() != RoleType.DEFAULT
							? "0"
							: "1")).getFormattedText());
					hts.put(id, hovers);
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
			data.put(key, id);
			list.add(key);
		}
		if (scroll == null) {
			(scroll = new GuiCustomScroll(this, 0)).setSize(248, 154);
		}
		scroll.setListNotSorted(list);
		scroll.setColors(colors);
		scroll.setHoverTexts(hts);
		scroll.guiLeft = guiLeft + 4;
		scroll.guiTop = guiTop + 14;
		addScroll(scroll);
		addButton(new GuiNpcButton(0, guiLeft + 4, guiTop + 170, 48, 20, "gui.add"));
		addButton(new GuiNpcButton(1, guiLeft + 54, guiTop + 170, 48, 20, "gui.remove"));
		getButton(1).setEnabled(scroll.hasSelected());
		addButton(new GuiNpcButton(2, guiLeft + 104, guiTop + 170, 48, 20, "selectServer.edit"));
		getButton(2).setEnabled(scroll.hasSelected());
		addButton(new GuiNpcButton(3, guiLeft + 154, guiTop + 170, 48, 20, "type.up", scroll.getSelect() != -1 && scroll.getSelect() != 0));
		addButton(new GuiNpcButton(4, guiLeft + 204, guiTop + 170, 48, 20, "type.down", scroll.getSelect() != -1 && scroll.getSelect() > -1 && scroll.getSelect() < data.size() - 1));
		addButton(new GuiNpcButton(66, guiLeft + 82, guiTop + 192, 98, 20, "gui.done"));
	}

	// New from Unofficial (BetaZavr)
	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, IGuiCustomScroll scroll) {
		if (!data.containsKey(scroll.getSelected())) {
			scroll.setSelect(-1);
			return;
		}
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
		if (!data.containsKey(scroll.getSelected())) {
			return;
		}
		DialogOption option = dialog.options.get(data.get(scroll.getSelected()));
		if (option == null) {
			return;
		}
		setSubGui(new SubGuiNpcDialogOption(option, parent));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (parent instanceof GuiDialogEdit && ((GuiDialogEdit) parent).parent != null) {
			NoppesUtil.openGUI(player, ((GuiDialogEdit) parent).parent);
		}
		initGui();
	}

	private void fix() {
		Map<Integer, DialogOption> map = new TreeMap<>();
		int i = 0;
		boolean bo = false;
		for (int id : dialog.options.keySet()) {
			if (id != i) {
				bo = true;
			}
			DialogOption dlOp = dialog.options.get(id).copy();
			dlOp.slot = i;
			map.put(i, dlOp);
			i++;
		}
		if (bo) {
			dialog.options.clear();
			dialog.options.putAll(map);
		}
	}

}
