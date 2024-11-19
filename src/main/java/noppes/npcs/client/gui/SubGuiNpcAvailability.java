package noppes.npcs.client.gui;

import java.util.Arrays;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumDayTime;
import noppes.npcs.controllers.data.Availability;

public class SubGuiNpcAvailability extends SubGuiInterface implements ISliderListener, ITextfieldListener {

	private final Availability availability;

	public SubGuiNpcAvailability(Availability availability) {
		super();
		this.availability = availability;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 217;
		this.closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
		case 0: {
			this.setSubGui(new SubGuiNpcAvailabilityDialog(this.availability));
			break;
		}
		case 1: {
			this.setSubGui(new SubGuiNpcAvailabilityQuest(this.availability));
			break;
		}
		case 2: {
			this.setSubGui(new SubGuiNpcAvailabilityFaction(this.availability));
			break;
		}
		case 3: {
			this.setSubGui(new SubGuiNpcAvailabilityScoreboard(this.availability));
			break;
		}
		case 4: {
			this.availability.healthType = button.getValue();
			if (this.getSlider(5) != null) {
				this.getSlider(5).visible = this.availability.healthType != 0;
			}
			break;
		}
		case 6: {
			this.setSubGui(new SubGuiNpcAvailabilityNames(this.availability));
			break;
		}
		case 7: {
			this.setSubGui(new SubGuiNpcAvailabilityStoredData(this.availability));
			break;
		}
		case 8: { // ItemStacks

			break;
		}
		case 50: {
			if (button.getValue() == 0) {
				this.getTextField(52).setText("" + this.availability.daytime[0]);
				this.getTextField(53).setText("" + this.availability.daytime[1]);
			} else {
				switch (EnumDayTime.values()[button.getValue() - 1]) {
				case Always: {
					this.getTextField(52).setText("0");
					this.getTextField(53).setText("0");
					break;
				}
				case Night: {
					this.getTextField(52).setText("18");
					this.getTextField(53).setText("6");
					break;
				}
				case Day: {
					this.getTextField(52).setText("6");
					this.getTextField(53).setText("18");
					break;
				}
				}
			}
			break;
		}
		case 66: {
			this.close();
			break;
		}
		default: {

		}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui != null || !CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getTextField(51) != null && this.getTextField(51).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover.level").getFormattedText());
		} else if (this.getTextField(52) != null && this.getTextField(52).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover.daytime.1").getFormattedText());
		} else if (this.getTextField(53) != null && this.getTextField(53).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover.daytime.2").getFormattedText());
		} else if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover.selectdialog").getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover.selectquest").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover.selectfaction").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover.selectscoreboard").getFormattedText());
		} else if (this.getButton(50) != null && this.getButton(50).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover.daytime.0").getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover.health.type").getFormattedText());
		} else if (this.getButton(6) != null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover.selectnames").getFormattedText());
		} else if (this.getButton(7) != null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover.storeddata").getFormattedText());
		} else if (this.getButton(8) != null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover.stack").getFormattedText());
		} else if (this.getSlider(5) != null && this.getSlider(5).visible && this.getSlider(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover.health").getFormattedText());
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
		this.addLabel(new GuiNpcLabel(1, "availability.available", this.guiLeft, this.guiTop + 4));
		this.getLabel(1).center(this.xSize);

		// colloquium 1
		int x = this.guiLeft + 6;
		int y = this.guiTop + 14;
		int h = 18;
		this.addButton(new GuiNpcButton(0, x, y, 120, h, "availability.selectdialog"));
		this.addButton(new GuiNpcButton(1, x, y += h + 2, 120, h, "availability.selectquest"));
		this.addButton(new GuiNpcButton(2, x, y += h + 2, 120, h, "availability.selectfaction"));
		this.addButton(new GuiNpcButton(8, x, y + h + 2, 120, h, "availability.stack"));

		// colloquium 2
		x += 124;
		y = this.guiTop + 14;
		this.addButton(new GuiNpcButton(3, x, y, 120, h, "availability.selectscoreboard"));
		this.addButton(new GuiNpcButton(6, x, y += h + 2, 120, h, "availability.selectnames"));
		this.addButton(new GuiNpcButton(7, x, y + h + 2, 120, h, "availability.storeddata"));

		// next
		this.addButton(new GuiNpcButton(66, this.guiLeft + 82, this.guiTop + 192, 98, h, "gui.done"));

		this.addLabel(new GuiNpcLabel(50, "availability.daytime", this.guiLeft + 4, this.guiTop + 131));
		this.addButton(new GuiNpcButton(50, this.guiLeft + 70, this.guiTop + 126, 70, h,
				new String[] { "availability.own", "availability.always", "availability.night", "availability.day" },
				this.availability.daytime[0] == this.availability.daytime[1] ? 1
						: this.availability.daytime[0] == 18 && this.availability.daytime[1] == 6 ? 2
								: this.availability.daytime[0] == 6 && this.availability.daytime[1] == 18 ? 3 : 1));

		this.addLabel(new GuiNpcLabel(51, "availability.minlevel", this.guiLeft + 4, this.guiTop + 153));
		this.addTextField(new GuiNpcTextField(51, this, this.fontRenderer, this.guiLeft + 70, this.guiTop + 149, 70, h - 2, this.availability.minPlayerLevel + ""));
		this.getTextField(51).setNumbersOnly();
		this.getTextField(51).setMinMaxDefault(0, Integer.MAX_VALUE, 0);
		this.addTextField(new GuiNpcTextField(52, this, this.fontRenderer, this.guiLeft + 145, this.guiTop + 127, 40, h - 2, this.availability.daytime[0] + ""));
		this.getTextField(52).setNumbersOnly();
		this.getTextField(52).setMinMaxDefault(0, 23, this.availability.daytime[0]);
		this.addTextField(new GuiNpcTextField(53, this, this.fontRenderer, this.guiLeft + 190, this.guiTop + 127, 40, h - 2, this.availability.daytime[1] + ""));
		this.getTextField(53).setNumbersOnly();
		this.getTextField(53).setMinMaxDefault(0, 23, this.availability.daytime[1]);

		this.addLabel(new GuiNpcLabel(52, "availability.health", this.guiLeft + 4, this.guiTop + 175));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 70, this.guiTop + 170, 70, h, new String[] { "availability.always", "availability.bigger", "availability.smaller" }, this.availability.healthType));
		GuiNpcSlider slider = new GuiNpcSlider(this, 5, this.guiLeft + 145, this.guiTop + 170, this.availability.health / 100.0f);
		slider.width = 106;
		slider.visible = this.availability.healthType != 0;
		this.addSlider(slider);
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		this.availability.health = (int) (slider.sliderValue * 100.0f);
		slider.setString(this.availability.health + "%");
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) {
	}

	@Override
	public void mouseReleased(GuiNpcSlider slider) {
	}

	@Override
	public void save() {
		this.availability.minPlayerLevel = this.getTextField(51).getInteger();
		this.availability.daytime[0] = this.getTextField(52).getInteger();
		this.availability.daytime[1] = this.getTextField(53).getInteger();
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 51) {
			this.availability.minPlayerLevel = textfield.getInteger();
		} else if (textfield.getId() == 52) {
			this.availability.daytime[0] = textfield.getInteger();
		} else if (textfield.getId() == 53) {
			this.availability.daytime[1] = textfield.getInteger();
		}
	}

}
