package noppes.npcs.client.gui;

import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.data.DataRanged;

import javax.annotation.Nonnull;

public class SubGuiNpcProjectiles extends SubGuiInterface implements ITextfieldListener {

	protected static final String[] potionNames = new String[] { "gui.none", "tile.fire.name", "effect.poison", "effect.hunger", "effect.weakness", "effect.moveSlowdown", "effect.confusion", "effect.blindness", "effect.wither" };
	protected static final String[] trailNames= new String[] { "gui.none", "Smoke", "Portal", "Redstone", "Lightning", "LargeSmoke", "Magic", "Enchant" };
	protected final DataRanged stats;

	public SubGuiNpcProjectiles(DataRanged statsIn) {
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
			case 66: onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// attack strength / arrow damage
		addLabel(new GuiNpcLabel(1, "enchantment.arrowDamage", guiLeft + 5, guiTop + 15));
		addTextField(new GuiNpcTextField(1, this, guiLeft + 45, guiTop + 10, 50, 18, stats.getStrength() + "")
				.setMinMaxDefault(0, Integer.MAX_VALUE, 5)
				.setHoverText("stats.hover.attack.strength"));
		// arrow knockback
		addLabel(new GuiNpcLabel(2, "enchantment.arrowKnockback", guiLeft + 110, guiTop + 15));
		addTextField(new GuiNpcTextField(2, this, guiLeft + 150, guiTop + 10, 50, 18, stats.getKnockback() + "")
				.setMinMaxDefault(0, 3, 0)
				.setHoverText("stats.hover.attack.knockback"));
		// arrow size
		addLabel(new GuiNpcLabel(3, "stats.size", guiLeft + 5, guiTop + 45));
		addTextField(new GuiNpcTextField(3, this, guiLeft + 45, guiTop + 40, 50, 18, stats.getSize() + "")
				.setMinMaxDefault(2, 20, 10)
				.setHoverText("stats.hover.bullet.size"));
		// arrow speed
		addLabel(new GuiNpcLabel(4, "stats.speed", guiLeft + 5, guiTop + 75));
		addTextField(new GuiNpcTextField(4, this, guiLeft + 45, guiTop + 70, 50, 18, stats.getSpeed() + "")
				.setMinMaxDefault(1, 50, 10)
				.setHoverText("stats.hover.bullet.speed"));
		// hasgravity
		addLabel(new GuiNpcLabel(5, "stats.hasgravity", guiLeft + 5, guiTop + 105));
		addButton(new GuiNpcButton(0, guiLeft + 60, guiTop + 100, 60, 20, new String[] { "gui.no", "gui.yes" }, (stats.getHasGravity() ? 1 : 0))
				.setHoverText("stats.hover.gravity"));
		if (!stats.getHasGravity()) {
			addButton(new GuiNpcButton(1, guiLeft + 140, guiTop + 100, 60, 20, new String[] { "gui.constant", "gui.accelerate" }, (stats.getAccelerate() ? 1 : 0))
					.setHoverText("stats.hover.accelerating"));
		}
		// explosive
		addLabel(new GuiNpcLabel(6, "stats.explosive", guiLeft + 5, guiTop + 135));
		addButton(new GuiNpcButton(3, guiLeft + 60, guiTop + 130, 60, 20, new String[] { "gui.none", "gui.small", "gui.medium", "gui.large" }, stats.getExplodeSize() % 4)
				.setHoverText("stats.hover.explosion"));
		// ranged effect
		addLabel(new GuiNpcLabel(7, "stats.rangedeffect", guiLeft + 5, guiTop + 165));
		addButton(new GuiNpcButton(4, guiLeft + 60, guiTop + 160, 60, 20, potionNames, stats.getEffectType())
				.setHoverText("stats.hover.attack.effects"));
		if (stats.getEffectType() != 0) {
			addTextField(new GuiNpcTextField(5, this, guiLeft + 140, guiTop + 160, 60, 18, stats.getEffectTime() + "")
					.setMinMaxDefault(1, 99999, 5)
					.setHoverText("stats.hover.effect.time"));
			if (stats.getEffectType() != 1) {
				addButton(new GuiNpcButton(10, guiLeft + 210, guiTop + 160, 40, 20, new String[] { "stats.regular", "stats.amplified" }, stats.getEffectStrength() % 2)
						.setHoverText("stats.hover.effect.power"));
			}
		}
		// trail
		addLabel(new GuiNpcLabel(8, "stats.trail", guiLeft + 5, guiTop + 195));
		addButton(new GuiNpcButton(5, guiLeft + 60, guiTop + 190, 60, 20, trailNames, stats.getParticle())
				.setHoverText("stats.hover.particle"));
		addButton(new GuiNpcButton(7, guiLeft + 220, guiTop + 10, 30, 20, new String[] { "2D", "3D" }, (stats.getRender3D() ? 1 : 0))
				.setHoverText("stats.hover.bullet.3d"));
		if (stats.getRender3D()) {
			// spin
			addLabel(new GuiNpcLabel(10, "stats.spin", guiLeft + 160, guiTop + 45));
			addButton(new GuiNpcButton(8, guiLeft + 220, guiTop + 40, 30, 20, new String[] { "gui.no", "gui.yes" }, (stats.getSpins() ? 1 : 0))
					.setHoverText("stats.hover.bullet.rotate"));
			// stick
			addLabel(new GuiNpcLabel(11, "stats.stick", guiLeft + 160, guiTop + 75));
			addButton(new GuiNpcButton(9, guiLeft + 220, guiTop + 70, 30, 20, new String[] { "gui.no", "gui.yes" }, (stats.getSticks() ? 1 : 0))
					.setHoverText("stats.hover.bullet.cling"));
		}
		// glows
		addButton(new GuiNpcButton(6, guiLeft + 140, guiTop + 190, 60, 20, new String[] { "stats.noglow", "stats.glows" }, (stats.getGlows() ? 1 : 0))
				.setHoverText("stats.hover.in.fire"));
		// exit
		addButton(new GuiNpcButton(66, guiLeft + 210, guiTop + 190, 40, 20, "gui.done")
				.setHoverText("hover.back"));
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		switch (textfield.getID()) {
			case 1: stats.setStrength(textfield.getInteger()); break;
			case 2: stats.setKnockback(textfield.getInteger()); break;
			case 3: stats.setSize(textfield.getInteger()); break;
			case 4: stats.setSpeed(textfield.getInteger()); break;
			case 5: stats.setEffect(stats.getEffectType(), stats.getEffectStrength(), textfield.getInteger()); break;
		}
	}

}
