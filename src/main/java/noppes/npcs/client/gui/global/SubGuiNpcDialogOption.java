package noppes.npcs.client.gui.global;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
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
	
	public static int LastColor = 14737632;
	private DialogOption option;
	
	private final Map<String, OptionDialogID> data; // {scrollTitle, dialogID}
	private GuiCustomScroll scroll;
	private String select;

	public SubGuiNpcDialogOption(DialogOption option) {
		this.option = option;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
		this.closeOnEsc = true;
		
		this.select = "";
		this.data = Maps.<String, OptionDialogID>newHashMap();
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(66, "dialog.editoption", this.guiLeft, this.guiTop + 4));
		this.getLabel(66).center(this.xSize);
		this.addLabel(new GuiNpcLabel(0, "gui.title", this.guiLeft + 4, this.guiTop + 20));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 40, this.guiTop + 15, 196, 20, this.option.title));
		String color;
		for (color = Integer.toHexString(this.option.optionColor); color.length() < 6; color = 0 + color) { }
		this.addLabel(new GuiNpcLabel(2, "gui.color", this.guiLeft + 4, this.guiTop + 45));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 62, this.guiTop + 40, 92, 20, color));
		this.getButton(2).setTextColor(this.option.optionColor);
		this.addLabel(new GuiNpcLabel(1, "dialog.optiontype", this.guiLeft + 4, this.guiTop + 67));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 62, this.guiTop + 62, 92, 20, new String[] { "gui.close", "dialog.dialog", "gui.disabled", "menu.role", "tile.commandBlock.name" }, this.option.optionType));
		if (this.option.optionType == 1) { // next dialog
			this.data.clear();
			char c = ((char) 167);
			DialogController dData = DialogController.instance;
			List<String> keys = Lists.newArrayList();
			int pos = -1, i = 0;
			OptionDialogID del = null;
			for (OptionDialogID od : this.option.dialogs) {
				if (od.dialogId<=0) { del = od; }
				String key;
				Dialog d = (Dialog) dData.get(od.dialogId);
				if (d == null) { key = c + "7ID: " + od.dialogId + c + "c Dialog Not Found!"; }
				else { key = d.getKey(); }
				this.data.put(key, od);
				keys.add(key);
				if (key.equals(this.select)) { pos = i; }
				i++;
			}
			if (del!=null) { this.option.dialogs.remove(del); }
			if (!this.data.containsKey(this.select)) { this.select = ""; }
			
			this.addLabel(new GuiNpcLabel(4, "gui.options", this.guiLeft + 4, this.guiTop + 84));
			if (this.scroll == null) { (this.scroll = new GuiCustomScroll(this, 0)).setSize(141, 116); }
			this.scroll.setList(Lists.newArrayList());
			this.scroll.setListNotSorted(keys);
			this.scroll.guiLeft = this.guiLeft + 4;
			this.scroll.guiTop = this.guiTop + 96;
			if (!this.select.isEmpty()) { this.scroll.setSelected(this.select); }
			this.addScroll(this.scroll);
			
			this.addButton(new GuiNpcButton(3, this.guiLeft + 149, this.guiTop + 96, 50, 20, "gui.add"));
			this.addButton(new GuiNpcButton(4, this.guiLeft + 201, this.guiTop + 96, 50, 20, "gui.remove", !this.select.isEmpty()));
			this.addButton(new GuiNpcButton(5, this.guiLeft + 149, this.guiTop + 118, 80, 20, "gui.edit"));
			this.addButton(new GuiNpcButton(6, this.guiLeft + 149, this.guiTop + 140, 50, 20, "type.up", !this.select.isEmpty() && pos != 0));
			this.addButton(new GuiNpcButton(7, this.guiLeft + 201, this.guiTop + 140, 50, 20, "type.down", !this.select.isEmpty() && pos>-1 && pos < this.data.size() - 1));
			this.addButton(new GuiNpcButton(8, this.guiLeft + 149, this.guiTop + 162, 80, 20, "availability.available", !this.select.isEmpty() && this.data.containsKey(this.select)));
		}
		if (this.option.optionType == 4) { // command
			this.addTextField(new GuiNpcTextField(4, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 84, 248, 20, this.option.command));
			this.getTextField(4).setMaxStringLength(32767);
			this.addLabel(new GuiNpcLabel(4, "advMode.command", this.guiLeft + 4, this.guiTop + 110));
			this.addLabel(new GuiNpcLabel(5, "advMode.nearestPlayer", this.guiLeft + 4, this.guiTop + 125));
			this.addLabel(new GuiNpcLabel(6, "advMode.randomPlayer", this.guiLeft + 4, this.guiTop + 140));
			this.addLabel(new GuiNpcLabel(7, "advMode.allPlayers", this.guiLeft + 4, this.guiTop + 155));
			this.addLabel(new GuiNpcLabel(8, "dialog.commandoptionplayer", this.guiLeft + 4, this.guiTop + 170));
		}
		this.addButton(new GuiNpcButton(66, this.guiLeft + 171, this.guiTop + 192, 80, 20, "gui.done"));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui !=null || !CustomNpcs.showDescriptions) { return; }
		if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.option.hover.name").getFormattedText());
		}
		else if (this.getTextField(4)!=null && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.option.hover.command").getFormattedText());
		}
		else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.option.hover.type."+this.option.optionType).getFormattedText());
		}
		else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("color.hover").getFormattedText());
		}
		else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.option.hover.add").getFormattedText());
		}
		else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.option.hover.del").getFormattedText());
		}
		else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.option.hover.edit").getFormattedText());
		}
		else if (this.getButton(6)!=null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.option.hover.down").getFormattedText());
		}
		else if (this.getButton(7)!=null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.option.hover.up").getFormattedText());
		}
		else if (this.getButton(8)!=null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.option.hover.availability", this.select).getFormattedText());
		}
		else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button.id == 1) {
			this.option.optionType = button.getValue();
			this.initGui();
		}
		switch (button.id) {
			case 1: { // type
				this.option.optionType = button.getValue();
				this.initGui();
				break;
			}
			case 2: { // color
				this.setSubGui(new SubGuiColorSelector(this.option.optionColor));
				break;
			}
			case 3: { // add dialog
				if (this.option.optionType!=1) { return; }
				this.setSubGui(new GuiDialogSelection(-1, 0));
				break;
			}
			case 4: { // del dialog
				if (this.option.optionType!=1 || this.select.isEmpty() || !this.data.containsKey(this.select)) { return; }
				this.option.dialogs.remove(this.data.get(this.select));
				this.initGui();
				break;
			}
			case 5: { // edit dialog
				if (this.option.optionType!=1 || this.select.isEmpty() || !this.data.containsKey(this.select)) { return; }
				this.setSubGui(new GuiDialogSelection(this.data.get(this.select).dialogId, 1));
				break;
			}
			case 6: { // up dialog
				if (this.option.optionType!=1 || this.select.isEmpty() || !this.data.containsKey(this.select)) { return; }
				this.option.upPos(this.data.get(this.select).dialogId);
				this.initGui();
				break;
			}
			case 7: { // down dialog
				if (this.option.optionType!=1 || this.select.isEmpty() || !this.data.containsKey(this.select)) { return; }
				this.option.downPos(this.data.get(this.select).dialogId);
				this.initGui();
				break;
			}
			case 8: { // availability
				if (this.option.optionType!=1 || this.select.isEmpty() || !this.data.containsKey(this.select)) { return; }
				this.setSubGui(new SubGuiNpcAvailability(this.data.get(this.select).availability));
				break;
			}
			case 66: { // exit
				this.close();
				break;
			}
		}
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
			if (dialog == null) { return; }
			if (((GuiDialogSelection) subgui).id==0) {
				this.option.addDialog(dialog.id);
				this.select = dialog.getKey();
			}
			else if (((GuiDialogSelection) subgui).id==1 && !this.select.isEmpty() && this.data.containsKey(this.select)) {
				this.option.replaceDialogIDs(this.data.get(this.select).dialogId, dialog.id);  // edit
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
			}
			else { this.option.title = textfield.getText(); }
		}
		if (textfield.getId() == 4) {
			this.option.command = textfield.getText();
		}
	}


	@Override
	public void scrollClicked(int mouseX, int mouseY, int ticks, GuiCustomScroll scroll) {
		if (this.option.optionType!=1 || scroll.getSelected() == null) { return; }
		this.select = scroll.getSelected();
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (this.option.optionType!=1 || this.select.isEmpty() || !this.data.containsKey(this.select)) { return; }
		this.setSubGui(new GuiDialogSelection(this.data.get(this.select).dialogId, 1));
	}
	
}
