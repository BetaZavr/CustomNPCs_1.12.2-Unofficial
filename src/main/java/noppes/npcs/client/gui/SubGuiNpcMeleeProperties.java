package noppes.npcs.client.gui;

import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.entity.data.DataMelee;

public class SubGuiNpcMeleeProperties
extends SubGuiInterface
implements ITextfieldListener {

	private static final String[] potionNames  = new String[] { "gui.none", "tile.fire.name", "effect.poison", "effect.hunger", "effect.weakness", "effect.moveSlowdown", "effect.confusion", "effect.blindness", "effect.wither" };
	private final DataMelee stats;

	public SubGuiNpcMeleeProperties(DataMelee data) {
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		stats = data;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 5) {
			stats.setEffect(button.getValue(), stats.getEffectStrength(), stats.getEffectTime());
			initGui();
		}
		if (button.id == 7) {
			stats.setEffect(stats.getEffectType(), button.getValue(), stats.getEffectTime());
		}
		if (button.id == 66) {
			close();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// power
		addLabel(new GuiNpcLabel(1, "stats.meleestrength", guiLeft + 5, guiTop + 15));
		GuiNpcTextField textField = new GuiNpcTextField(1, this, fontRenderer, guiLeft + 85, guiTop + 10, 50, 18, stats.getStrength() + "");
		textField.setMinMaxDefault(0, Integer.MAX_VALUE, 5);
		textField.setHoverText("stats.hover.attack.strength");
		addTextField(textField);
		// range
		addLabel(new GuiNpcLabel(2, "stats.meleerange", guiLeft + 5, guiTop + 45));
		textField = new GuiNpcTextField(2, this, fontRenderer, guiLeft + 85, guiTop + 40, 50, 18, (Math.round(stats.getRange() * 10.0f) / 10.0f) + "");
		textField.setMinMaxDoubleDefault(0.2d, 30.0d, 2.0d);
		textField.setHoverText("stats.hover.attack.range");
		addTextField(textField);
		// speed
		addLabel(new GuiNpcLabel(3, "stats.meleespeed", guiLeft + 5, guiTop + 75));
		textField = new GuiNpcTextField(3, this, fontRenderer, guiLeft + 85, guiTop + 70, 50, 18, stats.getDelay() + "");
		textField.setMinMaxDefault(1, 1000, 20);
		textField.setHoverText("stats.hover.attack.speed");
		addTextField(textField);
		// knockback
		addLabel(new GuiNpcLabel(4, "enchantment.knockback", guiLeft + 5, guiTop + 105));
		textField = new GuiNpcTextField(4, this, fontRenderer, guiLeft + 85, guiTop + 100, 50, 18, stats.getKnockback() + "");
		textField.setMinMaxDefault(0, 4, 0);
		textField.setHoverText("stats.hover.attack.knockback");
		addTextField(textField);
		// effect
		addLabel(new GuiNpcLabel(5, "stats.meleeeffect", guiLeft + 5, guiTop + 135));
		GuiNpcButton button = new GuiButtonBiDirectional(5, guiLeft + 85, guiTop + 130, 100, 20, potionNames, stats.getEffectType());
		button.setHoverText("stats.hover.attack.effects");
		addButton(button);
		if (stats.getEffectType() != 0) {
			addLabel(new GuiNpcLabel(6, "gui.time", guiLeft + 5, guiTop + 165));
			textField = new GuiNpcTextField(6, this, fontRenderer, guiLeft + 85, guiTop + 160, 50, 18, stats.getEffectTime() + "");
			textField.setMinMaxDefault(1, 99999, 5);
			textField.setHoverText("stats.hover.attack.effect");
			addTextField(textField);
			if (stats.getEffectType() != 1) {
				addLabel(new GuiNpcLabel(7, "stats.amplify", guiLeft + 5, guiTop + 195));
				button = new GuiButtonBiDirectional(7, guiLeft + 85, guiTop + 190, 52, 20, new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }, stats.getEffectStrength());
				button.setHoverText("stats.hover.effect.power");
				addButton(button);
			}
		}
		button = new GuiNpcButton(66, guiLeft + 164, guiTop + 192, 90, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		switch (textfield.getId()) {
			case 1: stats.setStrength(textfield.getInteger()); break;
			case 2: stats.setRange(textfield.getDouble()); break;
			case 3: stats.setDelay(textfield.getInteger()); break;
			case 4: stats.setKnockback(textfield.getInteger()); break;
			case 6: stats.setEffect(stats.getEffectType(), stats.getEffectStrength(), textfield.getInteger()); break;
		}
	}

}
