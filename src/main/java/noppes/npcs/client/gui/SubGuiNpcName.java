package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.entity.data.DataDisplay;

public class SubGuiNpcName extends SubGuiInterface implements ITextfieldListener {
	private DataDisplay display;

	public SubGuiNpcName(DataDisplay display) {
		this.display = display;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
		this.closeOnEsc = true;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button.id == 1) {
			this.display.setMarkovGeneratorId(button.getValue());
		}
		if (button.id == 2) {
			this.display.setMarkovGender(button.getValue());
		}
		if (button.id == 3) {
			String name = this.display.getRandomName();
			this.display.setName(name);
			this.getTextField(0).setText(name);
		}
		if (button.id == 66) {
			this.close();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = this.guiTop + 4;
		this.addButton(new GuiNpcButton(66, this.guiLeft + this.xSize - 24, y, 20, 20, "X"));
		y += 50;
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 4, y, 226, 20, this.display.getName()));
		y += 22;
		this.addButton(new GuiButtonBiDirectional(1, this.guiLeft + 4, y, 200, 20,
				new String[] { "markov.roman.name", "markov.japanese.name", "markov.slavic.name", "markov.welsh.name",
						"markov.sami.name", "markov.oldNorse.name", "markov.ancientGreek.name", "markov.aztec.name",
						"markov.classicCNPCs.name", "markov.spanish.name" },
				this.display.getMarkovGeneratorId()));
		y += 22;
		this.addButton(new GuiButtonBiDirectional(2, this.guiLeft + 64, y, 120, 20,
				new String[] { "markov.gender.either", "markov.gender.male", "markov.gender.female" },
				this.display.getMarkovGender()));
		this.addLabel(new GuiNpcLabel(2, "markov.gender.name", this.guiLeft + 5, y + 5));
		y += 42;
		this.addButton(new GuiNpcButton(3, this.guiLeft + 4, y, 70, 20, "markov.generate"));
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.name").getFormattedText());
		}
		else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.group.name").getFormattedText());
		}
		else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.group.either").getFormattedText());
		}
		else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.random.name").getFormattedText());
		}
		else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 0) {
			if (!textfield.isEmpty()) {
				this.display.setName(textfield.getText());
			} else {
				textfield.setText(this.display.getName());
			}
		}
	}
}
