package noppes.npcs.client.gui;

import java.util.Arrays;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.entity.data.DataRanged;

public class SubGuiNpcProjectiles extends SubGuiInterface implements ITextfieldListener {
	private String[] potionNames;
	private DataRanged stats;
	private String[] trailNames;

	public SubGuiNpcProjectiles(DataRanged stats) {
		this.potionNames = new String[] { "gui.none", "tile.fire.name", "effect.poison", "effect.hunger",
				"effect.weakness", "effect.moveSlowdown", "effect.confusion", "effect.blindness", "effect.wither" };
		this.trailNames = new String[] { "gui.none", "Smoke", "Portal", "Redstone", "Lightning", "LargeSmoke", "Magic",
				"Enchant" };
		this.stats = stats;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
		this.closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			this.stats.setHasGravity(button.getValue() == 1);
			this.initGui();
		}
		if (button.id == 1) {
			this.stats.setAccelerate(button.getValue() == 1);
		}
		if (button.id == 3) {
			this.stats.setExplodeSize(button.getValue());
		}
		if (button.id == 4) {
			this.stats.setEffect(button.getValue(), this.stats.getEffectStrength(), this.stats.getEffectTime());
			this.initGui();
		}
		if (button.id == 5) {
			this.stats.setParticle(button.getValue());
		}
		if (button.id == 6) {
			this.stats.setGlows(button.getValue() == 1);
		}
		if (button.id == 7) {
			this.stats.setRender3D(button.getValue() == 1);
			this.initGui();
		}
		if (button.id == 8) {
			this.stats.setSpins(button.getValue() == 1);
		}
		if (button.id == 9) {
			this.stats.setSticks(button.getValue() == 1);
		}
		if (button.id == 10) {
			this.stats.setEffect(this.stats.getEffectType(), button.getValue(), this.stats.getEffectTime());
		}
		if (button.id == 66) {
			this.close();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getTextField(1) != null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.attack.strength").getFormattedText());
		} else if (this.getTextField(2) != null && this.getTextField(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.attack.knockback").getFormattedText());
		} else if (this.getTextField(3) != null && this.getTextField(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.bulet.size").getFormattedText());
		} else if (this.getTextField(4) != null && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.bulet.speed").getFormattedText());
		} else if (this.getTextField(5) != null && this.getTextField(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.attack.effect").getFormattedText());
		} else if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.gravity").getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.accelerating").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.explosion").getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.attack.effects").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.particle").getFormattedText());
		} else if (this.getButton(6) != null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.in.fire").getFormattedText());
		} else if (this.getButton(7) != null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.bulet.3d").getFormattedText());
		} else if (this.getButton(8) != null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.bulet.rotate").getFormattedText());
		} else if (this.getButton(9) != null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.bulet.cling").getFormattedText());
		} else if (this.getButton(10) != null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.effect.power").getFormattedText());
		} else if (this.getButton(66) != null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(1, "enchantment.arrowDamage", this.guiLeft + 5, this.guiTop + 15));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 45, this.guiTop + 10, 50, 18,
				this.stats.getStrength() + ""));
		this.getTextField(1).setNumbersOnly();
		this.getTextField(1).setMinMaxDefault(0, Integer.MAX_VALUE, 5);
		this.addLabel(new GuiNpcLabel(2, "enchantment.arrowKnockback", this.guiLeft + 110, this.guiTop + 15));
		this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, this.guiLeft + 150, this.guiTop + 10, 50, 18,
				this.stats.getKnockback() + ""));
		this.getTextField(2).setNumbersOnly();
		this.getTextField(2).setMinMaxDefault(0, 3, 0);
		this.addLabel(new GuiNpcLabel(3, "stats.size", this.guiLeft + 5, this.guiTop + 45));
		this.addTextField(new GuiNpcTextField(3, this, this.fontRenderer, this.guiLeft + 45, this.guiTop + 40, 50, 18,
				this.stats.getSize() + ""));
		this.getTextField(3).setNumbersOnly();
		this.getTextField(3).setMinMaxDefault(2, 20, 10); // Changed
		this.addLabel(new GuiNpcLabel(4, "stats.speed", this.guiLeft + 5, this.guiTop + 75));
		this.addTextField(new GuiNpcTextField(4, this, this.fontRenderer, this.guiLeft + 45, this.guiTop + 70, 50, 18,
				this.stats.getSpeed() + ""));
		this.getTextField(4).setNumbersOnly();
		this.getTextField(4).setMinMaxDefault(1, 50, 10);
		this.addLabel(new GuiNpcLabel(5, "stats.hasgravity", this.guiLeft + 5, this.guiTop + 105));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 60, this.guiTop + 100, 60, 20,
				new String[] { "gui.no", "gui.yes" }, (this.stats.getHasGravity() ? 1 : 0)));
		if (!this.stats.getHasGravity()) {
			this.addButton(new GuiNpcButton(1, this.guiLeft + 140, this.guiTop + 100, 60, 20,
					new String[] { "gui.constant", "gui.accelerate" }, (this.stats.getAccelerate() ? 1 : 0)));
		}
		this.addLabel(new GuiNpcLabel(6, "stats.explosive", this.guiLeft + 5, this.guiTop + 135));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 60, this.guiTop + 130, 60, 20,
				new String[] { "gui.none", "gui.small", "gui.medium", "gui.large" }, this.stats.getExplodeSize() % 4));
		this.addLabel(new GuiNpcLabel(7, "stats.rangedeffect", this.guiLeft + 5, this.guiTop + 165));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 60, this.guiTop + 160, 60, 20, this.potionNames,
				this.stats.getEffectType()));
		if (this.stats.getEffectType() != 0) {
			this.addTextField(new GuiNpcTextField(5, this, this.fontRenderer, this.guiLeft + 140, this.guiTop + 160, 60,
					18, this.stats.getEffectTime() + ""));
			this.getTextField(5).setNumbersOnly();
			this.getTextField(5).setMinMaxDefault(1, 99999, 5);
			if (this.stats.getEffectType() != 1) {
				this.addButton(new GuiNpcButton(10, this.guiLeft + 210, this.guiTop + 160, 40, 20,
						new String[] { "stats.regular", "stats.amplified" }, this.stats.getEffectStrength() % 2));
			}
		}
		this.addLabel(new GuiNpcLabel(8, "stats.trail", this.guiLeft + 5, this.guiTop + 195));
		this.addButton(new GuiNpcButton(5, this.guiLeft + 60, this.guiTop + 190, 60, 20, this.trailNames,
				this.stats.getParticle()));
		this.addButton(new GuiNpcButton(7, this.guiLeft + 220, this.guiTop + 10, 30, 20, new String[] { "2D", "3D" },
				(this.stats.getRender3D() ? 1 : 0)));
		if (this.stats.getRender3D()) {
			this.addLabel(new GuiNpcLabel(10, "stats.spin", this.guiLeft + 160, this.guiTop + 45));
			this.addButton(new GuiNpcButton(8, this.guiLeft + 220, this.guiTop + 40, 30, 20,
					new String[] { "gui.no", "gui.yes" }, (this.stats.getSpins() ? 1 : 0)));
			this.addLabel(new GuiNpcLabel(11, "stats.stick", this.guiLeft + 160, this.guiTop + 75));
			this.addButton(new GuiNpcButton(9, this.guiLeft + 220, this.guiTop + 70, 30, 20,
					new String[] { "gui.no", "gui.yes" }, (this.stats.getSticks() ? 1 : 0)));
		}
		this.addButton(new GuiNpcButton(6, this.guiLeft + 140, this.guiTop + 190, 60, 20,
				new String[] { "stats.noglow", "stats.glows" }, (this.stats.getGlows() ? 1 : 0)));
		this.addButton(new GuiNpcButton(66, this.guiLeft + 210, this.guiTop + 190, 40, 20, "gui.done"));
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 1) {
			this.stats.setStrength(textfield.getInteger());
		} else if (textfield.getId() == 2) {
			this.stats.setKnockback(textfield.getInteger());
		} else if (textfield.getId() == 3) {
			this.stats.setSize(textfield.getInteger());
		} else if (textfield.getId() == 4) {
			this.stats.setSpeed(textfield.getInteger());
		} else if (textfield.getId() == 5) {
			this.stats.setEffect(this.stats.getEffectType(), this.stats.getEffectStrength(), textfield.getInteger());
		}
	}

}
