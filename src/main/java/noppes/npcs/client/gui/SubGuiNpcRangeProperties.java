package noppes.npcs.client.gui;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.data.DataRanged;
import noppes.npcs.entity.data.DataStats;

public class SubGuiNpcRangeProperties
extends SubGuiInterface
implements ITextfieldListener, ISubGuiListener {

	private final DataRanged ranged;
	private GuiNpcTextField soundSelected;
	private final DataStats stats;
	private final String[] fireType = new String[] { "gui.no", "gui.whendistant", "gui.whenhidden" };

	public SubGuiNpcRangeProperties(DataStats st) {
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		soundSelected = null;
		stats = st;
		ranged = st.ranged;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getId() == 7) {
			soundSelected = (GuiNpcTextField) getTextField(7);
			setSubGui(new GuiSoundSelection(soundSelected.getText()));
		}
		if (button.getId() == 11) {
			soundSelected = (GuiNpcTextField) getTextField(11);
			setSubGui(new GuiSoundSelection(soundSelected.getText()));
		}
		if (button.getId() == 10) {
			soundSelected = (GuiNpcTextField) getTextField(10);
			setSubGui(new GuiSoundSelection(soundSelected.getText()));
		} else if (button.getId() == 66) {
			close();
		} else if (button.getId() == 9) {
			ranged.setHasAimAnimation(((GuiNpcButtonYesNo) button).getBoolean());
		} else if (button.getId() == 13) {
			ranged.setFireType(button.getValue());
			ITextComponent hover = new TextComponentTranslation("stats.hover.availability");
			hover.appendSibling(new TextComponentTranslation("stats.hover.availability." + ranged.getFireType(), new TextComponentTranslation(fireType[ranged.getFireType()]).getFormattedText(), "" + (ranged.getRange() / 2.0d)));
			if (ranged.getFireType() != 0) {
				hover.appendSibling(new TextComponentTranslation("stats.hover.availability.3")); }
			button.setHoverText(hover.getFormattedText());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = guiTop + 4;
		// accuracy
		GuiNpcTextField textField = new GuiNpcTextField(1, this, fontRenderer, guiLeft + 80, y, 50, 18, ranged.getAccuracy() + "");
		textField.setMinMaxDefault(0, 100, 90);
		textField.setHoverText("stats.hover.attack.accuracy");
		addTextField(textField);
		addLabel(new GuiNpcLabel(1, "stats.accuracy", guiLeft + 5, y + 5));
		// shotCount
		textField = new GuiNpcTextField(8, this, fontRenderer, guiLeft + 200, y, 50, 18, ranged.getShotCount() + "");
		textField.setMinMaxDefault(1, 10, 1);
		textField.setHoverText("stats.hover.shot.count");
		addTextField(textField);
		addLabel(new GuiNpcLabel(8, "stats.burstcount", guiLeft + 135, y + 5));
		// ranged range
		y += 22;
		textField = new GuiNpcTextField(2, this, fontRenderer, guiLeft + 80, y, 50, 18, ranged.getRange() + "");
		addLabel(new GuiNpcLabel(2, "gui.range", guiLeft + 5, y + 5));
		textField.setMinMaxDoubleDefault(2.0d, 64.0d, ranged.getRange());
		textField.setHoverText("stats.hover.attack.distance");
		addTextField(textField);
		// melee range
		textField = new GuiNpcTextField(9, this, fontRenderer, guiLeft + 200, y, 30, 20, ranged.getMeleeRange() + "");
		textField.setMinMaxDefault(0, stats.aggroRange, 5);
		textField.setHoverText("stats.hover.attack.range");
		addTextField(textField);
		addLabel(new GuiNpcLabel(16, "stats.meleerange", guiLeft + 135, y + 5));
		// ranged min delay
		int j = guiLeft + 80;
		y += 22;
		textField = new GuiNpcTextField(3, this, fontRenderer, j, y, 50, 18, ranged.getDelayMin() + "");
		textField.setMinMaxDefault(1, 9999, 20);
		textField.setHoverText("stats.hover.attack.min.time");
		addTextField(textField);
		addLabel(new GuiNpcLabel(3, "stats.mindelay", guiLeft + 5, y + 5));
		// ranged max delay
		textField = new GuiNpcTextField(4, this, fontRenderer, guiLeft + 200, y, 50, 18, ranged.getDelayMax() + "");
		textField.setMinMaxDefault(1, 9999, 20);
		textField.setHoverText("stats.hover.attack.max.time");
		addTextField(textField);
		addLabel(new GuiNpcLabel(4, "stats.maxdelay", guiLeft + 135, y + 5));
		// shot count
		y += 22;
		textField = new GuiNpcTextField(6, this, fontRenderer, guiLeft + 80, y, 50, 18, ranged.getBurst() + "");
		textField.setMinMaxDefault(1, 100, 20);
		textField.setHoverText("stats.hover.shot.amount");
		addTextField(textField);
		addLabel(new GuiNpcLabel(6, "stats.shotcount", guiLeft + 5, y + 5));
		// shot speed
		textField = new GuiNpcTextField(5, this, fontRenderer, guiLeft + 200, y, 50, 18, ranged.getBurstDelay() + "");
		textField.setMinMaxDefault(1, 30, 5);
		textField.setHoverText("stats.hover.shot.speed");
		addTextField(textField);
		addLabel(new GuiNpcLabel(5, "stats.burstspeed", guiLeft + 135, y + 5));
		// fire sound
		y += 22;
		textField = new GuiNpcTextField(7, this, fontRenderer, guiLeft + 80, y, 100, 20, ranged.getSound(0));
		textField.setHoverText("stats.hover.sound.shot");
		addTextField(textField);
		GuiNpcButton button = new GuiNpcButton(7, guiLeft + 187, y, 60, 20, "mco.template.button.select");
		button.setHoverText("hover.set");
		addButton(button);
		addLabel(new GuiNpcLabel(7, "stats.firesound", guiLeft + 5, y + 5));
		// hitting sound
		y += 22;
		textField = new GuiNpcTextField(11, this, fontRenderer, guiLeft + 80, y, 100, 20, ranged.getSound(1));
		textField.setHoverText("stats.hover.sound.hurt");
		addTextField(textField);
		button = new GuiNpcButton(11, guiLeft + 187, y, 60, 20, "mco.template.button.select");
		button.setHoverText("hover.set");
		addButton(button);
		addLabel(new GuiNpcLabel(11, "stats.hittingsound", guiLeft + 5, y + 5));
		// hit sound
		y += 22;
		textField = new GuiNpcTextField(10, this, fontRenderer, guiLeft + 80, y, 100, 20, ranged.getSound(2));
		textField.setHoverText("stats.hover.sound.live");
		addTextField(textField);
		addButton(new GuiNpcButton(10, guiLeft + 187, y, 60, 20, "mco.template.button.select"));
		button.setHoverText("hover.set");
		addButton(button);
		addLabel(new GuiNpcLabel(10, "stats.hitsound", guiLeft + 5, y + 5));
		// aim while shooting
		y += 22;
		button = new GuiNpcButtonYesNo(9, guiLeft + 100, y, ranged.getHasAimAnimation());
		button.setHoverText("stats.hover.aim");
		addButton(button);
		addLabel(new GuiNpcLabel(9, "stats.aimWhileShooting", guiLeft + 5, y + 5));
		// indirect
		y += 22;
		button = new GuiNpcButton(13, guiLeft + 100, y, 80, 20, fireType, ranged.getFireType());
		ITextComponent hover = new TextComponentTranslation("stats.hover.availability");
		hover.appendSibling(new TextComponentTranslation("stats.hover.availability." + ranged.getFireType(), new TextComponentTranslation(fireType[ranged.getFireType()]).getFormattedText(), "" + (ranged.getRange() / 2.0d)));
		if (ranged.getFireType() != 0) {
			hover.appendSibling(new TextComponentTranslation("stats.hover.availability.3")); }
		button.setHoverText(hover.getFormattedText());
		addButton(button);
		addLabel(new GuiNpcLabel(13, "stats.indirect", guiLeft + 5, y + 5));
		// exit
		button = new GuiNpcButton(66, guiLeft + 190, guiTop + 190, 60, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void subGuiClosed(ISubGuiInterface subgui) {
		GuiSoundSelection gss = (GuiSoundSelection) subgui;
		if (gss.selectedResource != null) {
			soundSelected.setText(gss.selectedResource.toString());
			unFocused(soundSelected);
		}
	}

	@Override
	public void unFocused(IGuiNpcTextField textfield) {
		if (textfield.getId() == 1) {
			ranged.setAccuracy(textfield.getInteger());
		} else if (textfield.getId() == 2) {
			ranged.setRange(textfield.getDouble());
		} else if (textfield.getId() == 3) {
			ranged.setDelay(textfield.getInteger(), ranged.getDelayMax());
			initGui();
		} else if (textfield.getId() == 4) {
			ranged.setDelay(ranged.getDelayMin(), textfield.getInteger());
			initGui();
		} else if (textfield.getId() == 5) {
			ranged.setBurstDelay(textfield.getInteger());
		} else if (textfield.getId() == 6) {
			ranged.setBurst(textfield.getInteger());
		} else if (textfield.getId() == 7) {
			ranged.setSound(0, textfield.getText());
		} else if (textfield.getId() == 8) {
			ranged.setShotCount(textfield.getInteger());
		} else if (textfield.getId() == 9) {
			ranged.setMeleeRange(textfield.getInteger());
		} else if (textfield.getId() == 10) {
			ranged.setSound(2, textfield.getText());
		} else if (textfield.getId() == 11) {
			ranged.setSound(1, textfield.getText());
		}
		initGui();
	}

}
