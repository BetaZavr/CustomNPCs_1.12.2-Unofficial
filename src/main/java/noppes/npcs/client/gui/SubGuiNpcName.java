package noppes.npcs.client.gui;

import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.data.DataDisplay;

import javax.annotation.Nonnull;

public class SubGuiNpcName extends SubGuiInterface implements ITextfieldListener {

	protected static final String[] markovNames = new String[] { "markov.roman.name", "markov.japanese.name", "markov.slavic.name", "markov.welsh.name",
			"markov.sami.name", "markov.oldNorse.name", "markov.ancientGreek.name", "markov.aztec.name",
			"markov.classicCNPCs.name", "markov.spanish.name" };
	protected static final String[] markovGenders = new String[] { "markov.gender.either", "markov.gender.male", "markov.gender.female" };
	protected final DataDisplay display;

	public SubGuiNpcName(DataDisplay displayIn) {
		super(0);
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		display = displayIn;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()){
			case 1: display.setMarkovGeneratorId(button.getValue()); break;
			case 2: display.setMarkovGender(button.getValue()); break;
			case 3: {
				String name = display.getRandomName();
				display.setName(name);
				if (getTextField(0) != null) { getTextField(0).setText(name); }
				break;
			}
			case 66: onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = guiTop + 4;
		addButton(new GuiNpcButton(66, guiLeft + xSize - 24, y, 20, 20, "X")
				.setHoverText("hover.back"));
		addTextField(new GuiNpcTextField(0, this, guiLeft + 4, y += 50, 226, 20, display.getName())
				.setHoverText("display.hover.name"));
		addButton(new GuiButtonBiDirectional(1, guiLeft + 4, y += 22, 200, 20, markovNames, display.getMarkovGeneratorId())
				.setHoverText("display.hover.group.name"));
		addButton(new GuiButtonBiDirectional(2, guiLeft + 64, y += 22, 120, 20, markovGenders, display.getMarkovGender())
				.setHoverText("display.hover.group.either"));
		addLabel(new GuiNpcLabel(2, "markov.gender.name", guiLeft + 5, y + 5));
		addButton(new GuiNpcButton(3, guiLeft + 4, y + 42, 70, 20, "markov.generate")
				.setHoverText("display.hover.random.name"));
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getID() == 0) {
			if (!textfield.isEmpty()) { display.setName(textfield.getText()); }
			else { textfield.setText(display.getName()); }
		}
	}
}
