package noppes.npcs.client.gui.drop;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.data.AttributeSet;

public class SubGuiDropAttribute
extends SubGuiInterface
implements ITextfieldListener {

	public AttributeSet attribute;
	private double[] values;

	public SubGuiDropAttribute(AttributeSet attr) {
		setBackground("companion_empty.png");
		xSize = 172;
		ySize = 167;
		closeOnEsc = true;

		attribute = attr;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getId() == 70) { // slot
			attribute.setSlot(button.getValue() - 1);
			initGui();
		} else if (button.getId() == 71) { // done
			close();
		}
	}

	private boolean check() {
		if (getTextField(72) == null) {
			return false;
		}
        return !getTextField(72).getText().isEmpty();
    }

	@Override
	public void initGui() {
		super.initGui();
		int anyIDs = 80;
		// name
		GuiNpcTextField textField = new GuiNpcTextField(72, this, guiLeft + 4, guiTop + 5, 163, 20, attribute.getAttribute());
		textField.setHoverText("drop.hover.attribute.name", new TextComponentTranslation("attribute.name." + attribute.getAttribute()).getFormattedText());
		addTextField(textField);
		// values
		values = new double[] { attribute.getMinValue(), attribute.getMaxValue() };
		addLabel(new GuiNpcLabel(anyIDs++, "type.value", guiLeft + 56, guiTop + 36));
		String tied = new TextComponentTranslation("drop.tied.random").getFormattedText();
		if (attribute.parent.tiedToLevel) { tied = new TextComponentTranslation("drop.tied.level").getFormattedText(); }
		// min
		textField = new GuiNpcTextField(73, this, guiLeft + 4, guiTop + 27, 50, 14, "" + values[0]);
		textField.setMinMaxDoubleDefault(-4096.0d, 4096.0d, attribute.getMinValue());
		textField.setHoverText("drop.hover.attribute.values", tied);
		addTextField(textField);
		// max
		textField = new GuiNpcTextField(74, this, guiLeft + 4, guiTop + 41, 50, 14, "" + values[1]);
		textField.setMinMaxDoubleDefault(-4096.0d, 4096.0d, attribute.getMaxValue());
		textField.setHoverText("drop.hover.attribute.values", tied);
		addTextField(textField);
		// slot
		String[] slots = new String[7];
		for (int i = 0; i < 7; i++) { slots[i] = "attribute.slot." + i; }
		GuiNpcButton button = new GuiNpcButton(70, guiLeft + 4, guiTop + 57, 87, 20, slots, attribute.slot + 1);
		button.setHoverText("drop.hover.attribute.slot");
		addButton(button);
		// chance
		addLabel(new GuiNpcLabel(anyIDs, "drop.chance", guiLeft + 56, guiTop + 84));
		textField = new GuiNpcTextField(75, this, guiLeft + 4, guiTop + 79, 50, 20, String.valueOf(attribute.getChance()));
		textField.setMinMaxDoubleDefault(0.0001d, 100.0d, attribute.getChance());
		textField.setHoverText("drop.hover.attribute.chance");
		addTextField(textField);
		// done
		button = new GuiNpcButton(71, guiLeft + 4, guiTop + 142, 80, 20, "gui.done", check());
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void unFocused(IGuiNpcTextField textfield) {
		if (textfield.getId() == 72) { // name
			attribute.setAttribute(textfield.getText());
			initGui();
		} else if (textfield.getId() == 73) { // value min
			values[0] = textfield.getDouble();
			attribute.setValues(values[0], values[1]);
		} else if (textfield.getId() == 74) { // value max
			values[1] = textfield.getDouble();
			attribute.setValues(values[0], values[1]);
		} else if (textfield.getId() == 75) { // chance
			attribute.setChance(textfield.getDouble());
			initGui();
		}
	}
}
