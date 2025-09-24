package noppes.npcs.client.gui.roles;

import noppes.npcs.client.gui.select.SubGuiSoundSelection;
import noppes.npcs.client.gui.util.*;

import javax.annotation.Nonnull;

public class SubGuiNpcConversationLine extends SubGuiInterface implements ITextfieldListener {

	public String line;
	public String sound;

	public SubGuiNpcConversationLine(String lineStr, String soundStr) {
		super(0);
		setBackground("menubg.png");
		closeOnEsc = true;
		xSize = 256;
		ySize = 216;

		line = lineStr;
		sound = soundStr;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 1: setSubGui(new SubGuiSoundSelection(sound)); break;
			case 2: sound = ""; initGui(); break;
			case 66: onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		addLabel(new GuiNpcLabel(0, "Line", guiLeft + 4, guiTop + 10));
		addTextField(new GuiNpcTextField(0, this, guiLeft + 4, guiTop + 22, 200, 20, line));
		addButton(new GuiNpcButton(1, guiLeft + 4, guiTop + 55, 90, 20, "Select Sound"));
		addButton(new GuiNpcButton(2, guiLeft + 96, guiTop + 55, 20, 20, "X"));
		addLabel(new GuiNpcLabel(1, sound, guiLeft + 4, guiTop + 81));
		addButton(new GuiNpcButton(66, guiLeft + 162, guiTop + 192, 90, 20, "gui.done"));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		SubGuiSoundSelection gss = (SubGuiSoundSelection) subgui;
		if (gss.selectedResource != null) { sound = gss.selectedResource.toString(); }
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) { line = textfield.getText(); }

}
