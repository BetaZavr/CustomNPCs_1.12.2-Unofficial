package noppes.npcs.client.gui.drop;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.data.AttributeSet;

import javax.annotation.Nonnull;

public class SubGuiDropAttribute extends SubGuiInterface implements ITextfieldListener {

	protected double[] values;
	public AttributeSet attribute;

	public SubGuiDropAttribute(AttributeSet attr) {
		super(0);
		setBackground("companion_empty.png");
		closeOnEsc = true;
		xSize = 172;
		ySize = 167;

		attribute = attr;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 70: attribute.setSlot(button.getValue() - 1); initGui(); break;
			case 66: onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int anyIDs = 80;
		// name
		addTextField(new GuiNpcTextField(72, this, guiLeft + 4, guiTop + 5, 163, 20, attribute.getAttribute())
				.setHoverText("drop.hover.attribute.name", new TextComponentTranslation("attribute.name." + attribute.getAttribute()).getFormattedText()));
		// values
		values = new double[] { attribute.getMinValue(), attribute.getMaxValue() };
		addLabel(new GuiNpcLabel(anyIDs++, "type.value", guiLeft + 56, guiTop + 36));
		String tied = new TextComponentTranslation("drop.tied.random").getFormattedText();
		if (attribute.parent.tiedToLevel) { tied = new TextComponentTranslation("drop.tied.level").getFormattedText(); }
		// min
		addTextField(new GuiNpcTextField(73, this, guiLeft + 4, guiTop + 27, 50, 14, "" + values[0])
				.setMinMaxDoubleDefault(-4096.0d, 4096.0d, attribute.getMinValue())
				.setHoverText("drop.hover.attribute.values", tied));
		// max
		addTextField(new GuiNpcTextField(74, this, guiLeft + 4, guiTop + 41, 50, 14, "" + values[1])
				.setMinMaxDoubleDefault(-4096.0d, 4096.0d, attribute.getMaxValue())
				.setHoverText("drop.hover.attribute.values", tied));
		// slot
		String[] slots = new String[7];
		for (int i = 0; i < 7; i++) { slots[i] = "attribute.slot." + i; }
		addButton(new GuiNpcButton(70, guiLeft + 4, guiTop + 57, 87, 20, slots, attribute.slot + 1)
				.setHoverText("drop.hover.attribute.slot"));
		// chance
		addLabel(new GuiNpcLabel(anyIDs, "drop.chance", guiLeft + 56, guiTop + 84));
		addTextField(new GuiNpcTextField(75, this, guiLeft + 4, guiTop + 79, 50, 20, String.valueOf(attribute.getChance()))
				.setMinMaxDoubleDefault(0.0001d, 100.0d, attribute.getChance())
				.setHoverText("drop.hover.attribute.chance"));
		// done
		addButton(new GuiNpcButton(66, guiLeft + 4, guiTop + 142, 80, 20, "gui.done", check())
				.setHoverText("hover.back"));
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		switch (textfield.getID()) {
			case 72: attribute.setAttribute(textfield.getText()); initGui(); break; // name
			case 73: values[0] = textfield.getDouble(); attribute.setValues(values[0], values[1]); break; // value min
			case 74: values[1] = textfield.getDouble(); attribute.setValues(values[0], values[1]); break; // value max
			case 75: attribute.setChance(textfield.getDouble()); initGui(); break; // chance
		}
	}

	private boolean check() {
		if (getTextField(72) == null) { return false; }
		return !getTextField(72).getText().isEmpty();
	}

}
