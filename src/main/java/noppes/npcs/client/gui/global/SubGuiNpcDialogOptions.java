package noppes.npcs.client.gui.global;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.DialogOption.OptionDialogID;

public class SubGuiNpcDialogOptions
extends SubGuiInterface
implements ICustomScrollListener, ISubGuiListener, GuiYesNoCallback {
	
	private Dialog dialog;
	private final Map<String, Integer> data; // {scrollTitle, dialogID}
	private GuiCustomScroll scroll;

	public SubGuiNpcDialogOptions(Dialog dialog) {
		this.dialog = dialog;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
		this.data = Maps.<String, Integer>newTreeMap();
		this.closeOnEsc = true;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(66, "dialog.options", this.guiLeft, this.guiTop + 4));
		this.getLabel(66).center(this.xSize);
		this.data.clear();
		List<String> list = Lists.<String>newArrayList();
		List<Integer> colors = Lists.<Integer>newArrayList();
		fix();
		String[][] hts = new String[this.dialog.options.size()][];
		DialogController dData = DialogController.instance;
		for (int id : this.dialog.options.keySet()) {
			DialogOption option = this.dialog.options.get(id);
			String key = ((char) 167) + "7ID:" + id + " ";
			if (option == null) { continue; }
			switch(option.optionType) {
				case COMMAND_BLOCK: {
					key += ((char) 167) + "eC";
					hts[id] = new String[] { new TextComponentTranslation("gui.type").getFormattedText() + ": " + option.optionType.get() + " - " + ((char) 167) + "e" + option.optionType.name(),
							new TextComponentTranslation("quest.has." + !option.command.isEmpty()).getFormattedText() + (!option.command.isEmpty() ? " - \"" + option.command + "\"" : "") };
					break;
				}
				case DIALOG_OPTION: {
					key += ((char) 167) + "3D";
					List<String> ht = Lists.<String>newArrayList();
					ht.add(new TextComponentTranslation("gui.type").getFormattedText() + ": " + option.optionType.get() + " - " + ((char) 167) + "3" + option.optionType.name());
					if (option.hasDialogs()) {
						ht.add(new TextComponentTranslation("availability.selectdialog").getFormattedText() + ":");
						for (OptionDialogID od : option.dialogs) {
							if (!dData.hasDialog(od.dialogId)) { continue; }
							ht.add("ID: " + od.dialogId + " -" + new TextComponentTranslation("quest.task.item." + (dData.hasDialog(od.dialogId) ? "0" : "1")).getFormattedText());
						}
					}
					else { ht.add(new TextComponentTranslation("quest.has.false").getFormattedText()); }
					hts[id] = ht.toArray(new String[ht.size()]);
					break;
				}
				case QUIT_OPTION: {
					key += ((char) 167) + "dE";
					hts[id] = new String[] { new TextComponentTranslation("gui.type").getFormattedText() + ": " + option.optionType.get() + " - " + ((char) 167) + "d" + option.optionType.name() };
					break;
				}
				case ROLE_OPTION: {
					key += ((char) 167) + "aR";
					List<String> ht = Lists.<String>newArrayList();
					ht.add(new TextComponentTranslation("gui.type").getFormattedText() + ": " + option.optionType.get() + " - " + ((char) 167) + "a" + option.optionType.name());
					ht.add(new TextComponentTranslation("role.name").getFormattedText() + " -" + new TextComponentTranslation("quest.task.item." + (this.npc != null && this.npc.advanced.roleInterface.getEnumType() != RoleType.DEFAULT? "0" : "1")).getFormattedText());
					hts[id] = ht.toArray(new String[ht.size()]);
					break;
				}
				case DISABLED: {
					key += ((char) 167) + "4N";
					hts[id] = new String[] { new TextComponentTranslation("gui.type").getFormattedText() + ": " + option.optionType.get() + " - " + ((char) 167) + "4" + option.optionType.name() };
					break;
				}
			}
			key += ((char) 167) + "7 - \"" + ((char) 167) + "r" + option.title + ((char) 167) + "7\"";
			colors.add(option.optionColor);
			this.data.put(key, id);
			list.add(key);
		}
		if (this.scroll == null) { (this.scroll = new GuiCustomScroll(this, 0)).setSize(248, 154); }
		this.scroll.setListNotSorted(list);
		this.scroll.setColors(colors);
		this.scroll.hoversTexts = hts;
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 14;
		this.addScroll(this.scroll);
		this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 170, 48, 20, "gui.add"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 54, this.guiTop + 170, 48, 20, "gui.remove"));
		this.getButton(1).setEnabled(this.scroll.selected != -1);
		this.addButton(new GuiNpcButton(2, this.guiLeft + 104, this.guiTop + 170, 48, 20, "selectServer.edit"));
		this.getButton(2).setEnabled(this.scroll.selected != -1);
		this.addButton(new GuiNpcButton(3, this.guiLeft + 154, this.guiTop + 170, 48, 20, "type.up", this.scroll.selected != -1 && this.scroll.selected != 0));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 204, this.guiTop + 170, 48, 20, "type.down", this.scroll.selected != -1 && this.scroll.selected >- 1 && this.scroll.selected < this.data.size() - 1));
		this.addButton(new GuiNpcButton(66, this.guiLeft + 82, this.guiTop + 192, 98, 20, "gui.done"));
	}

	private void fix() {
		Map<Integer, DialogOption> map = Maps.newTreeMap();
		int i = 0;
		boolean bo = false;
		for (int id : this.dialog.options.keySet()) {
			if (id != i) { bo = true; }
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
	public void buttonEvent(GuiNpcButton button) {
		switch(button.id) {
			case 0: { // add new
				DialogOption option = new DialogOption();
				option.slot = this.dialog.options.size();
				this.dialog.options.put(option.slot, option);
				option.optionColor = SubGuiNpcDialogOption.LastColor;
				this.scroll.selected = option.slot;
				this.setSubGui(new SubGuiNpcDialogOption(option));
				break;
			}
			case 1: { // remove
				if (!this.data.containsKey(this.scroll.getSelected())) { return; }
				DialogOption option = this.dialog.options.get(this.data.get(this.scroll.getSelected()));
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, "ID:"+option.slot+" - "+option.title, new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 2: { // edit
				if (!this.data.containsKey(this.scroll.getSelected())) { return; }
				DialogOption option = this.dialog.options.get(this.data.get(this.scroll.getSelected()));
				if (option != null) {
					this.setSubGui(new SubGuiNpcDialogOption(option));
				}
				break;
			}
			case 3: { // up dialog
				if (!this.data.containsKey(this.scroll.getSelected())) { return; }
				this.dialog.upPos(this.data.get(this.scroll.getSelected()));
				this.scroll.selected--;
				this.initGui();
				break;
			}
			case 4: { // down dialog
				if (!this.data.containsKey(this.scroll.getSelected())) { return; }
				this.dialog.downPos(this.data.get(this.scroll.getSelected()));
				this.scroll.selected++;
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
		if (this.parent instanceof GuiDialogEdit && ((GuiDialogEdit) this.parent).parent != null) { NoppesUtil.openGUI((EntityPlayer) this.player, ((GuiDialogEdit) this.parent).parent); }
		else { NoppesUtil.openGUI((EntityPlayer) this.player, this); }
		if (!result) { return; }
		this.dialog.options.remove(this.data.get(this.scroll.getSelected()));
		this.initGui();
	}
	
	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (!this.data.containsKey(scroll.getSelected())) {
			scroll.selected = -1;
			return;
		}
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		if (!this.data.containsKey(scroll.getSelected())) { return; }
		DialogOption option = this.dialog.options.get(this.data.get(scroll.getSelected()));
		if (option == null) { return; }
		this.setSubGui(new SubGuiNpcDialogOption(option));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (this.parent instanceof GuiDialogEdit && ((GuiDialogEdit) this.parent).parent != null) { NoppesUtil.openGUI((EntityPlayer) this.player, ((GuiDialogEdit) this.parent).parent); }
		this.initGui();
	}
	
}
