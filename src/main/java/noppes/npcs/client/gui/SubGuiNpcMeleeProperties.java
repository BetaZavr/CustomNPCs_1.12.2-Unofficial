package noppes.npcs.client.gui;

import java.util.Arrays;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.entity.data.DataMelee;

public class SubGuiNpcMeleeProperties extends SubGuiInterface implements ITextfieldListener {

	private final String[] potionNames;
	private final DataMelee stats;

	public SubGuiNpcMeleeProperties(DataMelee stats) {
		this.potionNames = new String[] { "gui.none", "tile.fire.name", "effect.poison", "effect.hunger", "effect.weakness", "effect.moveSlowdown", "effect.confusion", "effect.blindness", "effect.wither" };
		this.stats = stats;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
		this.closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 5) {
			this.stats.setEffect(button.getValue(), this.stats.getEffectStrength(), this.stats.getEffectTime());
			this.initGui();
		}
		if (button.id == 7) {
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
			this.setHoverText(new TextComponentTranslation("stats.hover.attack.range").getFormattedText());
		} else if (this.getTextField(3) != null && this.getTextField(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.attack.speed").getFormattedText());
		} else if (this.getTextField(4) != null && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.attack.knockback").getFormattedText());
		} else if (this.getTextField(6) != null && this.getTextField(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.attack.effect").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.attack.effects").getFormattedText());
		} else if (this.getButton(7) != null && this.getButton(7).isMouseOver()) {
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
		this.addLabel(new GuiNpcLabel(1, "stats.meleestrength", this.guiLeft + 5, this.guiTop + 15));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 85, this.guiTop + 10, 50, 18, this.stats.getStrength() + ""));
		this.getTextField(1).setNumbersOnly();
		this.getTextField(1).setMinMaxDefault(0, Integer.MAX_VALUE, 5);
		this.addLabel(new GuiNpcLabel(2, "stats.meleerange", this.guiLeft + 5, this.guiTop + 45));
		this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, this.guiLeft + 85, this.guiTop + 40, 50, 18, (Math.round(this.stats.getRange() * 10.0f) / 10.0f) + ""));
		this.getTextField(2).setDoubleNumbersOnly();
		this.getTextField(2).setMinMaxDoubleDefault(0.2d, 30.0d, 2.0d);
		this.addLabel(new GuiNpcLabel(3, "stats.meleespeed", this.guiLeft + 5, this.guiTop + 75));
		this.addTextField(new GuiNpcTextField(3, this, this.fontRenderer, this.guiLeft + 85, this.guiTop + 70, 50, 18, this.stats.getDelay() + ""));
		this.getTextField(3).setNumbersOnly();
		this.getTextField(3).setMinMaxDefault(1, 1000, 20);
		this.addLabel(new GuiNpcLabel(4, "enchantment.knockback", this.guiLeft + 5, this.guiTop + 105));
		this.addTextField(new GuiNpcTextField(4, this, this.fontRenderer, this.guiLeft + 85, this.guiTop + 100, 50, 18, this.stats.getKnockback() + ""));
		this.getTextField(4).setNumbersOnly();
		this.getTextField(4).setMinMaxDefault(0, 4, 0);
		this.addLabel(new GuiNpcLabel(5, "stats.meleeeffect", this.guiLeft + 5, this.guiTop + 135));
		this.addButton(new GuiButtonBiDirectional(5, this.guiLeft + 85, this.guiTop + 130, 100, 20, this.potionNames, this.stats.getEffectType()));
		if (this.stats.getEffectType() != 0) {
			this.addLabel(new GuiNpcLabel(6, "gui.time", this.guiLeft + 5, this.guiTop + 165));
			this.addTextField(new GuiNpcTextField(6, this, this.fontRenderer, this.guiLeft + 85, this.guiTop + 160, 50, 18, this.stats.getEffectTime() + ""));
			this.getTextField(6).setNumbersOnly();
			this.getTextField(6).setMinMaxDefault(1, 99999, 5);
			if (this.stats.getEffectType() != 1) {
				this.addLabel(new GuiNpcLabel(7, "stats.amplify", this.guiLeft + 5, this.guiTop + 195));
				this.addButton(new GuiButtonBiDirectional(7, this.guiLeft + 85, this.guiTop + 190, 52, 20,
						new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10" },
						this.stats.getEffectStrength()));
			}
		}
		this.addButton(new GuiNpcButton(66, this.guiLeft + 164, this.guiTop + 192, 90, 20, "gui.done"));
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 1) {
			this.stats.setStrength(textfield.getInteger());
		} else if (textfield.getId() == 2) {
			this.stats.setRange(textfield.getDouble());
		} else if (textfield.getId() == 3) {
			this.stats.setDelay(textfield.getInteger());
		} else if (textfield.getId() == 4) {
			this.stats.setKnockback(textfield.getInteger());
		} else if (textfield.getId() == 6) {
			this.stats.setEffect(this.stats.getEffectType(), this.stats.getEffectStrength(), textfield.getInteger());
		}
	}

}
