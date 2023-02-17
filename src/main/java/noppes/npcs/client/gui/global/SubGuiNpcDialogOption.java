package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiButton;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.select.GuiDialogSelection;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;

public class SubGuiNpcDialogOption extends SubGuiInterface implements ITextfieldListener, ISubGuiListener {
	public static int LastColor = 14737632;
	private DialogOption option;

	public SubGuiNpcDialogOption(DialogOption option) {
		this.option = option;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
		this.closeOnEsc = true;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button.id == 1) {
			this.option.optionType = button.getValue();
			this.initGui();
		}
		if (button.id == 2) {
			this.setSubGui(new SubGuiColorSelector(this.option.optionColor));
		}
		if (button.id == 3) {
			this.setSubGui(new GuiDialogSelection(this.option.dialogId));
		}
		if (button.id == 66) {
			this.close();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(66, "dialog.editoption", this.guiLeft, this.guiTop + 4));
		this.getLabel(66).center(this.xSize);
		this.addLabel(new GuiNpcLabel(0, "gui.title", this.guiLeft + 4, this.guiTop + 20));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 40, this.guiTop + 15, 196, 20,
				this.option.title));
		String color;
		for (color = Integer.toHexString(this.option.optionColor); color.length() < 6; color = 0 + color) {
		}
		this.addLabel(new GuiNpcLabel(2, "gui.color", this.guiLeft + 4, this.guiTop + 45));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 62, this.guiTop + 40, 92, 20, color));
		this.getButton(2).setTextColor(this.option.optionColor);
		this.addLabel(new GuiNpcLabel(1, "dialog.optiontype", this.guiLeft + 4, this.guiTop + 67));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 62, this.guiTop + 62, 92, 20,
				new String[] { "gui.close", "dialog.dialog", "gui.disabled", "menu.role", "tile.commandBlock.name" },
				this.option.optionType));
		if (this.option.optionType == 1) {
			this.addButton(new GuiNpcButton(3, this.guiLeft + 4, this.guiTop + 84, "availability.selectdialog"));
			if (this.option.dialogId >= 0) {
				Dialog dialog = DialogController.instance.dialogs.get(this.option.dialogId);
				if (dialog != null) {
					this.getButton(3).setDisplayText(dialog.title);
				}
			}
		}
		if (this.option.optionType == 4) {
			this.addTextField(new GuiNpcTextField(4, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 84, 248,
					20, this.option.command));
			this.getTextField(4).setMaxStringLength(32767);
			this.addLabel(new GuiNpcLabel(4, "advMode.command", this.guiLeft + 4, this.guiTop + 110));
			this.addLabel(new GuiNpcLabel(5, "advMode.nearestPlayer", this.guiLeft + 4, this.guiTop + 125));
			this.addLabel(new GuiNpcLabel(6, "advMode.randomPlayer", this.guiLeft + 4, this.guiTop + 140));
			this.addLabel(new GuiNpcLabel(7, "advMode.allPlayers", this.guiLeft + 4, this.guiTop + 155));
			this.addLabel(new GuiNpcLabel(8, "dialog.commandoptionplayer", this.guiLeft + 4, this.guiTop + 170));
		}
		this.addButton(new GuiNpcButton(66, this.guiLeft + 82, this.guiTop + 190, 98, 20, "gui.done"));
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
			if (dialog != null) {
				this.option.dialogId = dialog.id;
			}
		}
		this.initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 0) {
			if (textfield.isEmpty()) {
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
