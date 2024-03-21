package noppes.npcs.client.gui;

import java.util.Arrays;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.entity.data.DataRanged;
import noppes.npcs.entity.data.DataStats;

public class SubGuiNpcRangeProperties
extends SubGuiInterface
implements ITextfieldListener, ISubGuiListener {
	
	private DataRanged ranged;
	private GuiNpcTextField soundSelected;
	private DataStats stats;
	private String[] fireType = new String[] { "gui.no", "gui.whendistant", "gui.whenhidden" };

	public SubGuiNpcRangeProperties(DataStats stats) {
		this.soundSelected = null;
		this.ranged = stats.ranged;
		this.stats = stats;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
		this.closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 7) {
			this.soundSelected = this.getTextField(7);
			this.setSubGui(new GuiSoundSelection(this.soundSelected.getText()));
		}
		if (button.id == 11) {
			this.soundSelected = this.getTextField(11);
			this.setSubGui(new GuiSoundSelection(this.soundSelected.getText()));
		}
		if (button.id == 10) {
			this.soundSelected = this.getTextField(10);
			this.setSubGui(new GuiSoundSelection(this.soundSelected.getText()));
		} else if (button.id == 66) {
			this.close();
		} else if (button.id == 9) {
			this.ranged.setHasAimAnimation(((GuiNpcButtonYesNo) button).getBoolean());
		} else if (button.id == 13) {
			this.ranged.setFireType(button.getValue());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = this.guiTop + 4;
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 80, y, 50, 18, this.ranged.getAccuracy() + ""));
		this.addLabel(new GuiNpcLabel(1, "stats.accuracy", this.guiLeft + 5, y + 5));
		this.getTextField(1).setNumbersOnly();
		this.getTextField(1).setMinMaxDefault(0, 100, 90);
		this.addTextField(new GuiNpcTextField(8, this, this.fontRenderer, this.guiLeft + 200, y, 50, 18, this.ranged.getShotCount() + ""));
		this.addLabel(new GuiNpcLabel(8, "stats.burstcount", this.guiLeft + 135, y + 5));
		this.getTextField(8).setNumbersOnly();
		this.getTextField(8).setMinMaxDefault(1, 10, 1);
		y += 22;
		this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, this.guiLeft + 80, y, 50, 18, this.ranged.getRange() + ""));
		this.addLabel(new GuiNpcLabel(2, "gui.range", this.guiLeft + 5, y + 5));
		this.getTextField(2).setNumbersOnly();
		this.getTextField(2).setMinMaxDefault(1, 64, 2);
		this.addTextField(new GuiNpcTextField(9, this, this.fontRenderer, this.guiLeft + 200, y, 30, 20, this.ranged.getMeleeRange() + ""));
		this.addLabel(new GuiNpcLabel(16, "stats.meleerange", this.guiLeft + 135, y + 5));
		this.getTextField(9).setNumbersOnly();
		this.getTextField(9).setMinMaxDefault(0, this.stats.aggroRange, 5);
		int j = this.guiLeft + 80;
		y += 22;
		this.addTextField(new GuiNpcTextField(3, this, this.fontRenderer, j, y, 50, 18, this.ranged.getDelayMin() + ""));
		this.addLabel(new GuiNpcLabel(3, "stats.mindelay", this.guiLeft + 5, y + 5));
		this.getTextField(3).setNumbersOnly();
		this.getTextField(3).setMinMaxDefault(1, 9999, 20);
		this.addTextField(new GuiNpcTextField(4, this, this.fontRenderer, this.guiLeft + 200, y, 50, 18, this.ranged.getDelayMax() + ""));
		this.addLabel(new GuiNpcLabel(4, "stats.maxdelay", this.guiLeft + 135, y + 5));
		this.getTextField(4).setNumbersOnly();
		this.getTextField(4).setMinMaxDefault(1, 9999, 20);
		y += 22;
		this.addTextField(new GuiNpcTextField(6, this, this.fontRenderer, this.guiLeft + 80, y, 50, 18, this.ranged.getBurst() + ""));
		this.addLabel(new GuiNpcLabel(6, "stats.shotcount", this.guiLeft + 5, y + 5));
		this.getTextField(6).setNumbersOnly();
		this.getTextField(6).setMinMaxDefault(1, 100, 20);
		this.addTextField(new GuiNpcTextField(5, this, this.fontRenderer, this.guiLeft + 200, y, 50, 18, this.ranged.getBurstDelay() + ""));
		this.addLabel(new GuiNpcLabel(5, "stats.burstspeed", this.guiLeft + 135, y + 5));
		this.getTextField(5).setNumbersOnly();
		this.getTextField(5).setMinMaxDefault(1, 30, 5);
		y += 22;
		this.addTextField(new GuiNpcTextField(7, this, this.fontRenderer, this.guiLeft + 80, y, 100, 20, this.ranged.getSound(0)));
		this.addLabel(new GuiNpcLabel(7, "stats.firesound", this.guiLeft + 5, y + 5));
		this.addButton(new GuiNpcButton(7, this.guiLeft + 187, y, 60, 20, "mco.template.button.select"));
		y += 22;
		this.addTextField(new GuiNpcTextField(11, this, this.fontRenderer, this.guiLeft + 80, y, 100, 20, this.ranged.getSound(1)));
		this.addLabel(new GuiNpcLabel(11, "stats.hittingsound", this.guiLeft + 5, y + 5));
		this.addButton(new GuiNpcButton(11, this.guiLeft + 187, y, 60, 20, "mco.template.button.select"));
		y += 22;
		this.addTextField(new GuiNpcTextField(10, this, this.fontRenderer, this.guiLeft + 80, y, 100, 20, this.ranged.getSound(2)));
		this.addLabel(new GuiNpcLabel(10, "stats.hitsound", this.guiLeft + 5, y + 5));
		this.addButton(new GuiNpcButton(10, this.guiLeft + 187, y, 60, 20, "mco.template.button.select"));
		y += 22;
		this.addButton(new GuiNpcButtonYesNo(9, this.guiLeft + 100, y, this.ranged.getHasAimAnimation()));
		this.addLabel(new GuiNpcLabel(9, "stats.aimWhileShooting", this.guiLeft + 5, y + 5));
		y += 22;
		this.addButton(new GuiNpcButton(13, this.guiLeft + 100, y, 80, 20, fireType, this.ranged.getFireType()));
		this.addLabel(new GuiNpcLabel(13, "stats.indirect", this.guiLeft + 5, y + 5));
		this.addButton(new GuiNpcButton(66, this.guiLeft + 190, this.guiTop + 190, 60, 20, "gui.done"));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		GuiSoundSelection gss = (GuiSoundSelection) subgui;
		if (gss.selectedResource != null) {
			this.soundSelected.setText(gss.selectedResource.toString());
			this.unFocused(this.soundSelected);
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 1) {
			this.ranged.setAccuracy(textfield.getInteger());
		} else if (textfield.getId() == 2) {
			this.ranged.setRange(textfield.getInteger());
		} else if (textfield.getId() == 3) {
			this.ranged.setDelay(textfield.getInteger(), this.ranged.getDelayMax());
			this.initGui();
		} else if (textfield.getId() == 4) {
			this.ranged.setDelay(this.ranged.getDelayMin(), textfield.getInteger());
			this.initGui();
		} else if (textfield.getId() == 5) {
			this.ranged.setBurstDelay(textfield.getInteger());
		} else if (textfield.getId() == 6) {
			this.ranged.setBurst(textfield.getInteger());
		} else if (textfield.getId() == 7) {
			this.ranged.setSound(0, textfield.getText());
		} else if (textfield.getId() == 8) {
			this.ranged.setShotCount(textfield.getInteger());
		} else if (textfield.getId() == 9) {
			this.ranged.setMeleeRange(textfield.getInteger());
		} else if (textfield.getId() == 10) {
			this.ranged.setSound(2, textfield.getText());
		} else if (textfield.getId() == 11) {
			this.ranged.setSound(1, textfield.getText());
		}
		this.initGui();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.attack.accuracy").getFormattedText());
		} else if (this.getTextField(2)!=null && this.getTextField(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.attack.distance").getFormattedText());
		} else if (this.getTextField(3)!=null && this.getTextField(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.attack.min.time").getFormattedText());
		} else if (this.getTextField(4)!=null && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.attack.max.time").getFormattedText());
		} else if (this.getTextField(5)!=null && this.getTextField(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.shot.speed").getFormattedText());
		} else if (this.getTextField(6)!=null && this.getTextField(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.shot.amount").getFormattedText()); //
		} else if (this.getTextField(7)!=null && this.getTextField(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.sound.shot").getFormattedText());
		} else if (this.getTextField(8)!=null && this.getTextField(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.shot.count").getFormattedText()); //
		} else if (this.getTextField(9)!=null && this.getTextField(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.attack.range").getFormattedText());
		} else if (this.getTextField(10)!=null && this.getTextField(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.sound.live").getFormattedText());
		} else if (this.getTextField(11)!=null && this.getTextField(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.sound.hurt").getFormattedText());
		} else if (this.getButton(7)!=null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.set").getFormattedText());
		} else if (this.getButton(9)!=null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("stats.hover.aim").getFormattedText());
		} else if (this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.set").getFormattedText());
		} else if (this.getButton(11)!=null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.set").getFormattedText());
		} else if (this.getButton(13)!=null && this.getButton(13).isMouseOver()) {
			ITextComponent hover = new TextComponentTranslation("stats.hover.availability");
			hover.appendSibling(new TextComponentTranslation("stats.hover.availability."+this.ranged.getFireType(), 
					new TextComponentTranslation(this.fireType[this.ranged.getFireType()]).getFormattedText(),
					"" + ((double) this.ranged.getRange() / 2.0d)));
			if (this.ranged.getFireType() != 0) {
				hover.appendSibling(new TextComponentTranslation("stats.hover.availability.3"));
			}
			this.setHoverText(hover.getFormattedText());
			
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}
	
}
