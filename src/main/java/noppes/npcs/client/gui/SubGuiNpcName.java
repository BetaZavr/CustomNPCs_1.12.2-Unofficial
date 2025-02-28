package noppes.npcs.client.gui;

import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.data.DataDisplay;

public class SubGuiNpcName
extends SubGuiInterface
implements ITextfieldListener {

	private static final String[] markovNames = new String[] { "markov.roman.name", "markov.japanese.name", "markov.slavic.name", "markov.welsh.name",
			"markov.sami.name", "markov.oldNorse.name", "markov.ancientGreek.name", "markov.aztec.name",
			"markov.classicCNPCs.name", "markov.spanish.name" };
	private static final String[] markovGenders = new String[] { "markov.gender.either", "markov.gender.male", "markov.gender.female" };
	private final DataDisplay display;

	public SubGuiNpcName(DataDisplay disp) {
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		display = disp;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getId()){
			case 1: display.setMarkovGeneratorId(button.getValue()); break;
			case 2: display.setMarkovGender(button.getValue()); break;
			case 3: {
				String name = display.getRandomName();
				display.setName(name);
				getTextField(0).setText(name);
				break;
			}
			case 66: close(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = guiTop + 4;
		GuiNpcButton button = new GuiNpcButton(66, guiLeft + xSize - 24, y, 20, 20, "X");
		button.setHoverText("hover.back");
		addButton(button);
		y += 50;
		GuiNpcTextField textField = new GuiNpcTextField(0, this, fontRenderer, guiLeft + 4, y, 226, 20, display.getName());
		textField.setHoverText("display.hover.name");
		addTextField(textField);
		y += 22;
		button = new GuiButtonBiDirectional(1, guiLeft + 4, y, 200, 20, markovNames, display.getMarkovGeneratorId());
		button.setHoverText("display.hover.group.name");
		addButton(button);
		y += 22;
		button = new GuiButtonBiDirectional(2, guiLeft + 64, y, 120, 20, markovGenders, display.getMarkovGender());
		button.setHoverText("display.hover.group.either");
		addButton(button);
		addLabel(new GuiNpcLabel(2, "markov.gender.name", guiLeft + 5, y + 5));
		y += 42;
		button = new GuiNpcButton(3, guiLeft + 4, y, 70, 20, "markov.generate");
		button.setHoverText("display.hover.random.name");
		addButton(button);
	}

	@Override
	public void unFocused(IGuiNpcTextField textfield) {
		if (textfield.getId() == 0) {
			if (!textfield.isEmpty()) { display.setName(textfield.getText());
			}
			else { textfield.setText(display.getName()); }
		}
	}
}
