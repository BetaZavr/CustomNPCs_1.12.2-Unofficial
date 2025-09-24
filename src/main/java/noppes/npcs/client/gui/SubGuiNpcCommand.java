package noppes.npcs.client.gui;

import noppes.npcs.client.gui.util.*;

import javax.annotation.Nonnull;

public class SubGuiNpcCommand extends SubGuiInterface implements ITextfieldListener {

	public String command;

	public SubGuiNpcCommand(String commandText) {
		super(0);
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		command = commandText;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		if (button.id == 66) { onClosed(); }
	}

	@Override
	public void initGui() {
		super.initGui();
		// text
		addTextField(new GuiNpcTextField(4, this, guiLeft + 4, guiTop + 84, 248, 20, command)
				.setHoverText("command.hover.text", ((char) 167) + "6" + Short.MAX_VALUE));
		getTextField(4).setMaxStringLength(Short.MAX_VALUE);
		// extra info
		addLabel(new GuiNpcLabel(4, "advMode.command", guiLeft + 4, guiTop + 110));
		addLabel(new GuiNpcLabel(5, "advMode.nearestPlayer", guiLeft + 4, guiTop + 125));
		addLabel(new GuiNpcLabel(6, "advMode.randomPlayer", guiLeft + 4, guiTop + 140));
		addLabel(new GuiNpcLabel(7, "advMode.allPlayers", guiLeft + 4, guiTop + 155));
		addLabel(new GuiNpcLabel(8, "dialog.commandoptionplayer", guiLeft + 4, guiTop + 170));
		// exit
		addButton(new GuiNpcButton(66, guiLeft + 82, guiTop + 190, 98, 20, "gui.done")
				.setHoverText("hover.back"));
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getID() == 4) { command = textfield.getText(); }
	}

}
