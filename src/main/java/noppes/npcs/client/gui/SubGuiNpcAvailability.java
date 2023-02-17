package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiButton;
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

public class SubGuiNpcAvailability
extends SubGuiInterface
implements ISliderListener, ITextfieldListener {
	
	private Availability availabitily;
	// private int slot; Changed

	public SubGuiNpcAvailability(Availability availabitily) {
		// this.slot = 0; Changed
		this.availabitily = availabitily;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 217;
		this.closeOnEsc = true;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(1, "availability.available", this.guiLeft, this.guiTop + 4));
		this.getLabel(1).center(this.xSize);
		this.addButton(new GuiNpcButton(0, this.guiLeft + 34, this.guiTop + 14, 180, 20, "availability.selectdialog"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 34, this.guiTop + 37, 180, 20, "availability.selectquest"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 34, this.guiTop + 60, 180, 20, "availability.selectfaction"));

		this.addLabel(new GuiNpcLabel(50, "availability.daytime", this.guiLeft + 4, this.guiTop + 131));
		this.addButton(new GuiNpcButton(50, this.guiLeft + 70, this.guiTop + 126, 70, 20,
				new String[] { "availability.own", "availability.always", "availability.night", "availability.day" },
				this.availabitily.daytime[0] == this.availabitily.daytime[1] ? 1
						: this.availabitily.daytime[0] == 18 && this.availabitily.daytime[1] == 6 ? 2
								: this.availabitily.daytime[0] == 6 && this.availabitily.daytime[1] == 18 ? 3 : 1));

		this.addLabel(new GuiNpcLabel(51, "availability.minlevel", this.guiLeft + 4, this.guiTop + 153));
		this.addTextField(new GuiNpcTextField(51, this, this.fontRenderer, this.guiLeft + 70, this.guiTop + 148, 70, 20,
				this.availabitily.minPlayerLevel + ""));
		this.getTextField(51).numbersOnly = true;
		this.getTextField(51).setMinMaxDefault(0, Integer.MAX_VALUE, 0);

		this.addButton(new GuiNpcButton(66, this.guiLeft + 82, this.guiTop + 192, 98, 20, "gui.done"));
		// this.updateGuiButtons(); Changed
		// New
		this.addButton(new GuiNpcButton(3, this.guiLeft + 34, this.guiTop + 83, 180, 20, "availability.selectscoreboard"));

		this.addTextField(new GuiNpcTextField(52, this, this.fontRenderer, this.guiLeft + 145, this.guiTop + 126, 40, 20, this.availabitily.daytime[0] + ""));
		this.getTextField(52).numbersOnly = true;
		this.getTextField(52).setMinMaxDefault(0, 23, this.availabitily.daytime[0]);
		this.addTextField(new GuiNpcTextField(53, this, this.fontRenderer, this.guiLeft + 190, this.guiTop + 126, 40, 20, this.availabitily.daytime[1] + ""));
		this.getTextField(53).numbersOnly = true;
		this.getTextField(53).setMinMaxDefault(0, 23, this.availabitily.daytime[1]);
		
		this.addLabel(new GuiNpcLabel(52, "availability.health", this.guiLeft + 4, this.guiTop + 175));
        this.addButton(new GuiNpcButton(4, this.guiLeft + 70, this.guiTop + 170, 70, 20,
        		new String[] { "availability.always", "availability.bigger", "availability.smaller" }
        , this.availabitily.healthType));
		GuiNpcSlider slider = new GuiNpcSlider(this, 5, this.guiLeft + 145, this.guiTop + 170, this.availabitily.health/100.0f);
		slider.width = 106;
		slider.visible = this.availabitily.healthType!=0;
        this.addSlider(slider);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch(guibutton.id) {
			case 0: {
				this.setSubGui(new SubGuiNpcAvailabilityDialog(this.availabitily));
				break;
			}
			case 1: {
				this.setSubGui(new SubGuiNpcAvailabilityQuest(this.availabitily));
				break;
			}
			case 2: {
				this.setSubGui(new SubGuiNpcAvailabilityFaction(this.availabitily));
				break;
			}
			case 3: {
				this.setSubGui(new SubGuiNpcAvailabilityScoreboard(this.availabitily));
				break;
			}
			case 4: {
				this.availabitily.healthType = ((GuiNpcButton) guibutton).getValue();
				if (this.getSlider(5)!=null) {
					this.getSlider(5).visible = this.availabitily.healthType!=0;
				}
				break;
			}
			case 50: {
				if (((GuiNpcButton) guibutton).getValue() == 0) {
					this.getTextField(52).setText("" + this.availabitily.daytime[0]);
					this.getTextField(53).setText("" + this.availabitily.daytime[1]);
				} else {
					switch (EnumDayTime.values()[((GuiNpcButton) guibutton).getValue() - 1]) {
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

	// New
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui != null || !CustomNpcs.showDescriptions) { return; }
		if (this.getTextField(51)!=null && this.getTextField(51).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.level").getFormattedText());
		} else if (this.getTextField(52)!=null && this.getTextField(52).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.daytime.1").getFormattedText());
		} else if (this.getTextField(53)!=null && this.getTextField(53).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.daytime.2").getFormattedText());
		} else if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.selectdialog").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.selectquest").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.selectfaction").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.selectscoreboard").getFormattedText());
		} else if (this.getButton(50)!=null && this.getButton(50).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.daytime.0").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.health.type").getFormattedText());
		} else if (this.getSlider(5)!=null && this.getSlider(5).visible && this.getSlider(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.health").getFormattedText());
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
	}

	@Override
	public void save() {
		this.availabitily.minPlayerLevel = this.getTextField(51).getInteger();
		this.availabitily.daytime[0] = this.getTextField(52).getInteger();
		this.availabitily.daytime[1] = this.getTextField(53).getInteger();
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 51) {
			this.availabitily.minPlayerLevel = textfield.getInteger();
		} else if (textfield.getId() == 52) {
			this.availabitily.daytime[0] = textfield.getInteger();
		} else if (textfield.getId() == 53) {
			this.availabitily.daytime[1] = textfield.getInteger();
		}
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		this.availabitily.health = (int) (slider.sliderValue * 100.0f);
		slider.setString(this.availabitily.health + "%");
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) { }

	@Override
	public void mouseReleased(GuiNpcSlider slider) { }
	
}
