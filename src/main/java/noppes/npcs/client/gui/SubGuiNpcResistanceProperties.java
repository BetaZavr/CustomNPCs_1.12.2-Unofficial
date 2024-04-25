package noppes.npcs.client.gui;

import java.util.Arrays;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.entity.data.Resistances;

public class SubGuiNpcResistanceProperties extends SubGuiInterface implements ISliderListener {

	private Resistances resistances;

	public SubGuiNpcResistanceProperties(Resistances resistances) {
		this.resistances = resistances;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
		this.closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
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
		if (this.getSlider(0) != null && this.getSlider(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.resist.knockback").getFormattedText());
		} else if (this.getSlider(1) != null && this.getSlider(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.resist.range").getFormattedText());
		} else if (this.getSlider(2) != null && this.getSlider(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.resist.melle").getFormattedText());
		} else if (this.getSlider(3) != null && this.getSlider(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.resist.explosion").getFormattedText());
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
		this.addLabel(new GuiNpcLabel(0, "enchantment.knockback", this.guiLeft + 4, this.guiTop + 15));
		this.addSlider(new GuiNpcSlider(this, 0, this.guiLeft + 94, this.guiTop + 10,
				(this.resistances.knockback * 100.0f - 100.0f) + "%", this.resistances.knockback / 2.0f));
		this.addLabel(new GuiNpcLabel(1, "item.arrow.name", this.guiLeft + 4, this.guiTop + 37));
		this.addSlider(new GuiNpcSlider(this, 1, this.guiLeft + 94, this.guiTop + 32,
				(this.resistances.arrow * 100.0f - 100.0f) + "%", this.resistances.arrow / 2.0f));
		this.addLabel(new GuiNpcLabel(2, "stats.melee", this.guiLeft + 4, this.guiTop + 59));
		this.addSlider(new GuiNpcSlider(this, 2, this.guiLeft + 94, this.guiTop + 54,
				(this.resistances.melee * 100.0f - 100.0f) + "%", this.resistances.melee / 2.0f));
		this.addLabel(new GuiNpcLabel(3, "stats.explosion", this.guiLeft + 4, this.guiTop + 81));
		this.addSlider(new GuiNpcSlider(this, 3, this.guiLeft + 94, this.guiTop + 76,
				(this.resistances.explosion * 100.0f - 100.0f) + "%", this.resistances.explosion / 2.0f));
		this.addButton(new GuiNpcButton(66, this.guiLeft + 190, this.guiTop + 190, 60, 20, "gui.done"));
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		slider.displayString = (slider.sliderValue * 200.0f - 100.0f) + "%";
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) {
	}

	@Override
	public void mouseReleased(GuiNpcSlider slider) {
		if (slider.id == 0) {
			this.resistances.knockback = slider.sliderValue * 2.0f;
		}
		if (slider.id == 1) {
			this.resistances.arrow = slider.sliderValue * 2.0f;
		}
		if (slider.id == 2) {
			this.resistances.melee = slider.sliderValue * 2.0f;
		}
		if (slider.id == 3) {
			this.resistances.explosion = slider.sliderValue * 2.0f;
		}
	}

}
