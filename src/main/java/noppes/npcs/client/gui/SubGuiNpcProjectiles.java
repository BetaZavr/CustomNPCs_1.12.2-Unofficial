package noppes.npcs.client.gui;

import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.data.DataRanged;

public class SubGuiNpcProjectiles
extends SubGuiInterface
implements ITextfieldListener {

	private static final String[] potionNames = new String[] { "gui.none", "tile.fire.name", "effect.poison", "effect.hunger", "effect.weakness", "effect.moveSlowdown", "effect.confusion", "effect.blindness", "effect.wither" };
	private static final String[] trailNames= new String[] { "gui.none", "Smoke", "Portal", "Redstone", "Lightning", "LargeSmoke", "Magic", "Enchant" };
	private final DataRanged stats;

	public SubGuiNpcProjectiles(DataRanged st) {
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		stats = st;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getId()) {
			case 0: {
				stats.setHasGravity(button.getValue() == 1);
				initGui();
				break;
			}
			case 1: stats.setAccelerate(button.getValue() == 1); break;
			case 3: stats.setExplodeSize(button.getValue()); break;
			case 4: {
				stats.setEffect(button.getValue(), stats.getEffectStrength(), stats.getEffectTime());
				initGui();
				break;
			}
			case 5: stats.setParticle(button.getValue()); break;
			case 6: stats.setGlows(button.getValue() == 1); break;
			case 7: {
				stats.setRender3D(button.getValue() == 1);
				initGui();
				break;
			}
			case 8: stats.setSpins(button.getValue() == 1); break;
			case 9: stats.setSticks(button.getValue() == 1); break;
			case 10: stats.setEffect(stats.getEffectType(), button.getValue(), stats.getEffectTime()); break;
			case 66: close(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// attack strength / arrow damage
		addLabel(new GuiNpcLabel(1, "enchantment.arrowDamage", guiLeft + 5, guiTop + 15));
		GuiNpcTextField textField = new GuiNpcTextField(1, this, fontRenderer, guiLeft + 45, guiTop + 10, 50, 18, stats.getStrength() + "");
		textField.setMinMaxDefault(0, Integer.MAX_VALUE, 5);
		textField.setHoverText("stats.hover.attack.strength");
		addTextField(textField);
		// arrow knockback
		addLabel(new GuiNpcLabel(2, "enchantment.arrowKnockback", guiLeft + 110, guiTop + 15));
		textField = new GuiNpcTextField(2, this, fontRenderer, guiLeft + 150, guiTop + 10, 50, 18, stats.getKnockback() + "");
		textField.setMinMaxDefault(0, 3, 0);
		textField.setHoverText("stats.hover.attack.knockback");
		addTextField(textField);
		// arrow size
		addLabel(new GuiNpcLabel(3, "stats.size", guiLeft + 5, guiTop + 45));
		textField = new GuiNpcTextField(3, this, fontRenderer, guiLeft + 45, guiTop + 40, 50, 18, stats.getSize() + "");
		textField.setMinMaxDefault(2, 20, 10);
		textField.setHoverText("stats.hover.bullet.size");
		addTextField(textField);
		// arrow speed
		addLabel(new GuiNpcLabel(4, "stats.speed", guiLeft + 5, guiTop + 75));
		textField = new GuiNpcTextField(4, this, fontRenderer, guiLeft + 45, guiTop + 70, 50, 18, stats.getSpeed() + "");
		textField.setMinMaxDefault(1, 50, 10);
		textField.setHoverText("stats.hover.bullet.speed");
		addTextField(textField);
		// hasgravity
		addLabel(new GuiNpcLabel(5, "stats.hasgravity", guiLeft + 5, guiTop + 105));
		GuiNpcButton button = new GuiNpcButton(0, guiLeft + 60, guiTop + 100, 60, 20, new String[] { "gui.no", "gui.yes" }, (stats.getHasGravity() ? 1 : 0));
		button.setHoverText("stats.hover.gravity");
		addButton(button);
		if (!stats.getHasGravity()) {
			button = new GuiNpcButton(1, guiLeft + 140, guiTop + 100, 60, 20, new String[] { "gui.constant", "gui.accelerate" }, (stats.getAccelerate() ? 1 : 0));
			button.setHoverText("stats.hover.accelerating");
			addButton(button);
		}
		// explosive
		addLabel(new GuiNpcLabel(6, "stats.explosive", guiLeft + 5, guiTop + 135));
		button = new GuiNpcButton(3, guiLeft + 60, guiTop + 130, 60, 20, new String[] { "gui.none", "gui.small", "gui.medium", "gui.large" }, stats.getExplodeSize() % 4);
		button.setHoverText("stats.hover.explosion");
		addButton(button);
		// ranged effect
		addLabel(new GuiNpcLabel(7, "stats.rangedeffect", guiLeft + 5, guiTop + 165));
		button = new GuiNpcButton(4, guiLeft + 60, guiTop + 160, 60, 20, potionNames, stats.getEffectType());
		button.setHoverText("stats.hover.attack.effects");
		addButton(button);
		if (stats.getEffectType() != 0) {
			textField = new GuiNpcTextField(5, this, fontRenderer, guiLeft + 140, guiTop + 160, 60, 18, stats.getEffectTime() + "");
			textField.setMinMaxDefault(1, 99999, 5);
			textField.setHoverText("stats.hover.effect.time");
			addTextField(textField);
			if (stats.getEffectType() != 1) {
				button = new GuiNpcButton(10, guiLeft + 210, guiTop + 160, 40, 20, new String[] { "stats.regular", "stats.amplified" }, stats.getEffectStrength() % 2);
				button.setHoverText("stats.hover.effect.power");
				addButton(button);
			}
		}
		// trail
		addLabel(new GuiNpcLabel(8, "stats.trail", guiLeft + 5, guiTop + 195));
		button = new GuiNpcButton(5, guiLeft + 60, guiTop + 190, 60, 20, trailNames, stats.getParticle());
		button.setHoverText("stats.hover.particle");
		addButton(button);
		button = new GuiNpcButton(7, guiLeft + 220, guiTop + 10, 30, 20, new String[] { "2D", "3D" }, (stats.getRender3D() ? 1 : 0));
		button.setHoverText("stats.hover.bullet.3d");
		addButton(button);
		if (stats.getRender3D()) {
			// spin
			addLabel(new GuiNpcLabel(10, "stats.spin", guiLeft + 160, guiTop + 45));
			button = new GuiNpcButton(8, guiLeft + 220, guiTop + 40, 30, 20, new String[] { "gui.no", "gui.yes" }, (stats.getSpins() ? 1 : 0));
			button.setHoverText("stats.hover.bullet.rotate");
			addButton(button);
			// stick
			addLabel(new GuiNpcLabel(11, "stats.stick", guiLeft + 160, guiTop + 75));
			button = new GuiNpcButton(9, guiLeft + 220, guiTop + 70, 30, 20, new String[] { "gui.no", "gui.yes" }, (stats.getSticks() ? 1 : 0));
			button.setHoverText("stats.hover.bullet.cling");
			addButton(button);
		}
		// glows
		button = new GuiNpcButton(6, guiLeft + 140, guiTop + 190, 60, 20, new String[] { "stats.noglow", "stats.glows" }, (stats.getGlows() ? 1 : 0));
		button.setHoverText("stats.hover.in.fire");
		addButton(button);
		// exit
		button = new GuiNpcButton(66, guiLeft + 210, guiTop + 190, 40, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void unFocused(IGuiNpcTextField textfield) {
		if (textfield.getId() == 1) {
			stats.setStrength(textfield.getInteger());
		} else if (textfield.getId() == 2) {
			stats.setKnockback(textfield.getInteger());
		} else if (textfield.getId() == 3) {
			stats.setSize(textfield.getInteger());
		} else if (textfield.getId() == 4) {
			stats.setSpeed(textfield.getInteger());
		} else if (textfield.getId() == 5) {
			stats.setEffect(stats.getEffectType(), stats.getEffectStrength(), textfield.getInteger());
		}
	}

}
