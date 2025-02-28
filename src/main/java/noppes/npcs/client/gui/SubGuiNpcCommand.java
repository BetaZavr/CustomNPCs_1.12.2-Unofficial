package noppes.npcs.client.gui;

import noppes.npcs.client.gui.util.*;

public class SubGuiNpcCommand
extends SubGuiInterface
implements ITextfieldListener {

	public String command;

	public SubGuiNpcCommand(String commandText) {
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		command = commandText;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getId() == 66) {
			close();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// text
		GuiNpcTextField textField = new GuiNpcTextField(4, this, fontRenderer, guiLeft + 4, guiTop + 84, 248, 20, command);
		textField.setMaxStringLength(Short.MAX_VALUE);
		textField.setHoverText("command.hover.text", ((char) 167) + "6" + Short.MAX_VALUE);
		addTextField(textField);
		// extra info
		addLabel(new GuiNpcLabel(4, "advMode.command", guiLeft + 4, guiTop + 110));
		addLabel(new GuiNpcLabel(5, "advMode.nearestPlayer", guiLeft + 4, guiTop + 125));
		addLabel(new GuiNpcLabel(6, "advMode.randomPlayer", guiLeft + 4, guiTop + 140));
		addLabel(new GuiNpcLabel(7, "advMode.allPlayers", guiLeft + 4, guiTop + 155));
		addLabel(new GuiNpcLabel(8, "dialog.commandoptionplayer", guiLeft + 4, guiTop + 170));
		// exit
		GuiNpcButton button = new GuiNpcButton(66, guiLeft + 82, guiTop + 190, 98, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void unFocused(IGuiNpcTextField textfield) {
		if (textfield.getId() == 4) {
			this.command = textfield.getText();
		}
	}
}
