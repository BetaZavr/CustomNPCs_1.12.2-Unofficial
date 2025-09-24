package noppes.npcs.client.gui;

import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.data.DataMelee;

import javax.annotation.Nonnull;

public class SubGuiNpcMeleeProperties extends SubGuiInterface implements ITextfieldListener {

	protected static final String[] potionNames  = new String[] { "gui.none", "tile.fire.name", "effect.poison", "effect.hunger", "effect.weakness", "effect.moveSlowdown", "effect.confusion", "effect.blindness", "effect.wither" };
	protected final DataMelee stats;

	public SubGuiNpcMeleeProperties(DataMelee statsIn) {
		super(0);
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		stats = statsIn;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 5 : {
				stats.setEffect(button.getValue(), stats.getEffectStrength(), stats.getEffectTime());
				initGui();
				break;
			}
			case 7 : stats.setEffect(stats.getEffectType(), button.getValue(), stats.getEffectTime()); break;
			case 66 : onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// power
		addLabel(new GuiNpcLabel(1, "stats.meleestrength", guiLeft + 5, guiTop + 15));
		addTextField(new GuiNpcTextField(1, this, guiLeft + 85, guiTop + 10, 50, 18, stats.getStrength() + "")
				.setMinMaxDefault(0, Integer.MAX_VALUE, 5)
				.setHoverText("stats.hover.attack.strength"));
		// range
		addLabel(new GuiNpcLabel(2, "stats.meleerange", guiLeft + 5, guiTop + 45));
		addTextField(new GuiNpcTextField(2, this, guiLeft + 85, guiTop + 40, 50, 18, (Math.round(stats.getRange() * 10.0f) / 10.0f) + "")
				.setMinMaxDoubleDefault(0.2d, 30.0d, 2.0d)
				.setHoverText("stats.hover.attack.range"));
		// speed
		addLabel(new GuiNpcLabel(3, "stats.meleespeed", guiLeft + 5, guiTop + 75));
		addTextField(new GuiNpcTextField(3, this, guiLeft + 85, guiTop + 70, 50, 18, stats.getDelay() + "")
				.setMinMaxDefault(1, 1000, 20)
				.setHoverText("stats.hover.attack.speed"));
		// knockback
		addLabel(new GuiNpcLabel(4, "enchantment.knockback", guiLeft + 5, guiTop + 105));
		addTextField(new GuiNpcTextField(4, this, guiLeft + 85, guiTop + 100, 50, 18, stats.getKnockback() + "")
				.setMinMaxDefault(0, 4, 0)
				.setHoverText("stats.hover.attack.knockback"));
		// effect
		addLabel(new GuiNpcLabel(5, "stats.meleeeffect", guiLeft + 5, guiTop + 135));
		addButton(new GuiButtonBiDirectional(5, guiLeft + 85, guiTop + 130, 100, 20, potionNames, stats.getEffectType())
				.setHoverText("stats.hover.attack.effects"));
		if (stats.getEffectType() != 0) {
			addLabel(new GuiNpcLabel(6, "gui.time", guiLeft + 5, guiTop + 165));
			addTextField(new GuiNpcTextField(6, this, guiLeft + 85, guiTop + 160, 50, 18, stats.getEffectTime() + "")
					.setMinMaxDefault(1, 99999, 5)
					.setHoverText("stats.hover.attack.effect"));
			if (stats.getEffectType() != 1) {
				addLabel(new GuiNpcLabel(7, "stats.amplify", guiLeft + 5, guiTop + 195));
				addButton(new GuiButtonBiDirectional(7, guiLeft + 85, guiTop + 190, 52, 20, new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" }, stats.getEffectStrength())
						.setHoverText("stats.hover.effect.power"));
			}
		}
		addButton(new GuiNpcButton(66, guiLeft + 164, guiTop + 192, 90, 20, "gui.done")
				.setHoverText("hover.back"));
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		switch (textfield.getID()) {
			case 1: stats.setStrength(textfield.getInteger()); break;
			case 2: stats.setRange(textfield.getDouble()); break;
			case 3: stats.setDelay(textfield.getInteger()); break;
			case 4: stats.setKnockback(textfield.getInteger()); break;
			case 6: stats.setEffect(stats.getEffectType(), stats.getEffectStrength(), textfield.getInteger()); break;
		}
	}

}
