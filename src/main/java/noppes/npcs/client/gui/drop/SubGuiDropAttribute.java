package noppes.npcs.client.gui.drop;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.entity.data.DataInventory.AttributeSet;

public class SubGuiDropAttribute extends SubGuiInterface implements ITextfieldListener {
	public AttributeSet attribute;
	private double[] values;

	public SubGuiDropAttribute(AttributeSet attr) {
		this.attribute = attr;
		this.setBackground("companion_empty.png");
		this.xSize = 172;
		this.ySize = 167;
		this.closeOnEsc = true;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		int id = button.id;
		if (id == 70) { // slot
			this.attribute.setSlot(button.getValue() - 1);
			this.initGui();
		} else if (id == 71) { // done
			this.close();
		}
	}

	private boolean check() {
		if (this.getTextField(72) == null) {
			return false;
		}
		if (this.getTextField(72).getText().length() == 0) {
			return false;
		}
		return true;
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (!CustomNpcs.showDescriptions) { return; }
		String tied = new TextComponentTranslation("drop.tied.random", new Object[0]).getFormattedText();
		if (this.attribute.parent.tiedToLevel) {
			tied = new TextComponentTranslation("drop.tied.level", new Object[0]).getFormattedText();
		}
		if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 7, 159, 24)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.attribute.name", new Object[] {
					new TextComponentTranslation("attribute.name." + this.attribute.getAttribute(), new Object[0])
							.getFormattedText() }).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 29, 46, 24)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.attribute.values", new Object[] { tied })
					.getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 59, 76, 16)) {
			this.setHoverText(
					new TextComponentTranslation("drop.hover.attribute.slot", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 81, 46, 16)) {
			this.setHoverText(
					new TextComponentTranslation("drop.hover.attribute.chance", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 144, 76, 16)) {
			this.setHoverText(new TextComponentTranslation("hover.back", new Object[0]).getFormattedText());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int anyIDs = 80;
		// name
		GuiNpcTextField nameAttr = new GuiNpcTextField(72, (GuiScreen) this, this.guiLeft + 4, this.guiTop + 5, 163, 20,
				this.attribute.getAttribute());
		this.addTextField(nameAttr);
		// values
		this.values = new double[] { this.attribute.getMinValue(), this.attribute.getMaxValue() };
		this.addLabel(new GuiNpcLabel(anyIDs++, "type.value", this.guiLeft + 56, this.guiTop + 36));
		GuiNpcTextField valueMin = new GuiNpcTextField(73, (GuiScreen) this, this.guiLeft + 4, this.guiTop + 27, 50, 14,
				"" + this.values[0]);
		valueMin.setDoubleNumbersOnly().setMinMaxDoubleDefault(-4096.0d, 4096.0d, this.attribute.getMinValue());
		this.addTextField(valueMin);
		GuiNpcTextField valueMax = new GuiNpcTextField(74, (GuiScreen) this, this.guiLeft + 4, this.guiTop + 41, 50, 14,
				"" + this.values[1]);
		valueMax.setDoubleNumbersOnly().setMinMaxDoubleDefault(-4096.0d, 4096.0d, this.attribute.getMaxValue());
		this.addTextField(valueMax);
		// slot
		String[] slots = new String[7];
		for (int i = 0; i < 7; i++) {
			slots[i] = "attribute.slot." + i;
		}
		this.addButton(
				new GuiNpcButton(70, this.guiLeft + 4, this.guiTop + 57, 87, 20, slots, this.attribute.slot + 1));
		// chance
		this.addLabel(new GuiNpcLabel(anyIDs++, "drop.chance", this.guiLeft + 56, this.guiTop + 84));
		GuiNpcTextField chanceE = new GuiNpcTextField(75, (GuiScreen) this, this.guiLeft + 4, this.guiTop + 79, 50, 20,
				String.valueOf(this.attribute.getChance()));
		chanceE.setDoubleNumbersOnly().setMinMaxDoubleDefault(0.0001d, 100.0d, this.attribute.getChance());
		this.addTextField(chanceE);
		// done
		this.addButton(new GuiNpcButton(71, this.guiLeft + 4, this.guiTop + 142, 80, 20, "gui.done", check()));
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 72) { // name
			this.attribute.setAttribute(textfield.getText());
			this.initGui();
		} else if (textfield.getId() == 73) { // value min
			this.values[0] = textfield.getDouble();
			this.attribute.setValues(this.values[0], this.values[1]);
		} else if (textfield.getId() == 74) { // value max
			this.values[1] = textfield.getDouble();
			this.attribute.setValues(this.values[0], this.values[1]);
		} else if (textfield.getId() == 75) { // chance
			this.attribute.setChance(textfield.getDouble());
			this.initGui();
		}
	}
}
