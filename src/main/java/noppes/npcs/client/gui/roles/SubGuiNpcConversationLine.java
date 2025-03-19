package noppes.npcs.client.gui.roles;

import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.*;

public class SubGuiNpcConversationLine
extends SubGuiInterface
implements ITextfieldListener, ISubGuiListener {

	public String line;
	public String sound;

	public SubGuiNpcConversationLine(String lineStr, String soundStr) {
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		line = lineStr;
		sound = soundStr;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 1: setSubGui(new GuiSoundSelection(sound)); break;
			case 2: {
				sound = "";
				initGui();
				break;
			}
			case 66: close(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		addLabel(new GuiNpcLabel(0, "Line", guiLeft + 4, guiTop + 10));
		addTextField(new GuiNpcTextField(0, this, fontRenderer, guiLeft + 4, guiTop + 22, 200, 20, line));
		addButton(new GuiNpcButton(1, guiLeft + 4, guiTop + 55, 90, 20, "Select Sound"));
		addButton(new GuiNpcButton(2, guiLeft + 96, guiTop + 55, 20, 20, "X"));
		addLabel(new GuiNpcLabel(1, sound, guiLeft + 4, guiTop + 81));
		addButton(new GuiNpcButton(66, guiLeft + 162, guiTop + 192, 90, 20, "gui.done"));
	}

	@Override
	public void subGuiClosed(ISubGuiInterface subgui) {
		GuiSoundSelection gss = (GuiSoundSelection) subgui;
		if (gss.selectedResource != null) { sound = gss.selectedResource.toString(); }
	}

	@Override
	public void unFocused(IGuiNpcTextField textfield) { line = textfield.getText(); }

}
