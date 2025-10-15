package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.select.SubGuiSoundSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.data.DataRanged;
import noppes.npcs.entity.data.DataStats;

import javax.annotation.Nonnull;

public class SubGuiNpcRangeProperties extends SubGuiInterface implements ITextfieldListener {

	protected final DataRanged ranged;
	protected final DataStats stats;
	protected final String[] fireType = new String[] { "gui.no", "gui.whendistant", "gui.whenhidden" };
	protected GuiNpcTextField soundSelected;

	public SubGuiNpcRangeProperties(DataStats statsIn) {
		super(0);
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		soundSelected = null;
		stats = statsIn;
		ranged = statsIn.ranged;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 7: {
				soundSelected = getTextField(7);
				setSubGui(new SubGuiSoundSelection(soundSelected.getText()));
				break;
			}
			case 9: {
				ranged.setHasAimAnimation(((GuiNpcButtonYesNo) button).getBoolean());
				break;
			}
			case 10: {
				soundSelected = getTextField(10);
				setSubGui(new SubGuiSoundSelection(soundSelected.getText()));
				break;
			}
			case 11: {
				soundSelected = getTextField(11);
				setSubGui(new SubGuiSoundSelection(soundSelected.getText()));
				break;
			}
			case 13: {
				ranged.setFireType(button.getValue());
				ITextComponent hover = new TextComponentTranslation("stats.hover.availability")
						.appendSibling(new TextComponentTranslation("stats.hover.availability." + ranged.getFireType(), new TextComponentTranslation(fireType[ranged.getFireType()]).getFormattedText(), "" + (ranged.getRange() / 2.0d)));
				if (ranged.getFireType() != 0) { hover.appendSibling(new TextComponentTranslation("stats.hover.availability.3")); }
				button.setHoverText(hover.getFormattedText());
				break;
			}
			case 66: onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x0 = guiLeft + 5;
		int x1 = guiLeft + 80;
		int x2 = guiLeft + 135;
		int x3 = guiLeft + 200;
		int x4 = guiLeft + 187;
		int y = guiTop + 4;
		// accuracy
		addLabel(new GuiNpcLabel(1, "stats.accuracy", x0, y + 5));
		addTextField(new GuiNpcTextField(1, this, x1, y, 50, 18, ranged.getAccuracy() + "")
				.setMinMaxDefault(0, 100, 90)
				.setHoverText("stats.hover.attack.accuracy"));
		// shotCount
		addLabel(new GuiNpcLabel(8, "stats.burstcount", x2, y + 5));
		addTextField(new GuiNpcTextField(8, this, x3, y, 50, 18, ranged.getShotCount() + "")
				.setMinMaxDefault(1, 10, 1)
				.setHoverText("stats.hover.shot.count"));
		// ranged range
		y += 22;
		addLabel(new GuiNpcLabel(2, "gui.range", x0, y + 5));
		addTextField(new GuiNpcTextField(2, this, x1, y, 50, 18, ranged.getRange() + "")
				.setMinMaxDoubleDefault(2.0d, 64.0d, ranged.getRange())
				.setHoverText("stats.hover.attack.distance"));
		// melee range
		addLabel(new GuiNpcLabel(9, "stats.meleerange", x2, y + 5));
		addTextField(new GuiNpcTextField(9, this, x3, y, 30, 20, ranged.getMeleeRange() + "")
				.setMinMaxDefault(0, stats.aggroRange, 5)
				.setHoverText("stats.hover.attack.range"));
		// ranged min delay
		y += 22;
		addLabel(new GuiNpcLabel(3, "stats.mindelay", x0, y + 5));
		addTextField(new GuiNpcTextField(3, this, x1, y, 50, 18, ranged.getDelayMin() + "")
				.setMinMaxDefault(1, 9999, 20)
				.setHoverText("stats.hover.attack.min.time"));
		// ranged max delay
		addLabel(new GuiNpcLabel(4, "stats.maxdelay", x2, y + 5));
		addTextField(new GuiNpcTextField(4, this, x3, y, 50, 18, ranged.getDelayMax() + "")
				.setMinMaxDefault(1, 9999, 20)
				.setHoverText("stats.hover.attack.max.time"));
		// shot count
		y += 22;
		addLabel(new GuiNpcLabel(6, "stats.shotcount", x0, y + 5));
		addTextField(new GuiNpcTextField(6, this, x1, y, 50, 18, ranged.getBurst() + "")
				.setMinMaxDefault(1, 100, 20)
				.setHoverText("stats.hover.shot.amount"));
		// shot speed
		addLabel(new GuiNpcLabel(5, "stats.burstspeed", x2, y + 5));
		addTextField(new GuiNpcTextField(5, this, x3, y, 50, 18, ranged.getBurstDelay() + "")
				.setMinMaxDefault(1, 30, 5)
				.setHoverText("stats.hover.shot.speed"));
		// fire sound
		y += 22;
		addTextField(new GuiNpcTextField(7, this, x1, y, 100, 20, ranged.getSound(0))
				.setHoverText("stats.hover.sound.shot"));
		addButton(new GuiNpcButton(7, x4, y, 60, 20, "mco.template.button.select")
				.setHoverText("hover.set"));
		addLabel(new GuiNpcLabel(7, "stats.firesound", x0, y + 5));
		// hitting sound
		y += 22;
		addTextField(new GuiNpcTextField(11, this, x1, y, 100, 20, ranged.getSound(1))
				.setHoverText("stats.hover.sound.hurt"));
		addButton(new GuiNpcButton(11, x4, y, 60, 20, "mco.template.button.select")
				.setHoverText("hover.set"));
		addLabel(new GuiNpcLabel(11, "stats.hittingsound", x0, y + 5));
		// hit sound
		y += 22;
		addTextField(new GuiNpcTextField(10, this, x1, y, 100, 20, ranged.getSound(2))
				.setHoverText("stats.hover.sound.live"));
		addButton(new GuiNpcButton(10, x4, y, 60, 20, "mco.template.button.select")
				.setHoverText("hover.set"));
		addLabel(new GuiNpcLabel(10, "stats.hitsound", x0, y + 5));
		// aim while shooting
		y += 22;
		addButton(new GuiNpcButtonYesNo(9, x1 + 20, y, ranged.getHasAimAnimation())
				.setHoverText("stats.hover.aim"));
		addLabel(new GuiNpcLabel(9, "stats.aimWhileShooting", x0, y + 5));
		// indirect
		y += 22;
		ITextComponent hover = new TextComponentTranslation("stats.hover.availability")
				.appendSibling(new TextComponentTranslation("stats.hover.availability." + ranged.getFireType(),
						new TextComponentTranslation(fireType[ranged.getFireType()]).getFormattedText(), "" + (ranged.getRange() / 2.0d)));
		if (ranged.getFireType() != 0) { hover.appendSibling(new TextComponentTranslation("stats.hover.availability.3")); }
		addButton(new GuiNpcButton(13, x1 + 20, y, 80, 20, fireType, ranged.getFireType())
				.setHoverText(hover.getFormattedText()));
		addLabel(new GuiNpcLabel(13, "stats.indirect", x0, y + 5));
		// exit
		addButton(new GuiNpcButton(66, x4 + 3, guiTop + 190, 60, 20, "gui.done").setHoverText("hover.back"));
	}

	@Override
	public void subGuiClosed(GuiScreen subgui) {
		SubGuiSoundSelection gss = (SubGuiSoundSelection) subgui;
		if (gss.selectedResource != null) {
			soundSelected.setText(gss.selectedResource.toString());
			unFocused(soundSelected);
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		switch (textfield.getID()) {
			case 1: ranged.setAccuracy(textfield.getInteger()); break;
			case 2: ranged.setRange(textfield.getDouble()); break;
			case 3: ranged.setDelay(textfield.getInteger(), ranged.getDelayMax()); break;
			case 4: ranged.setDelay(ranged.getDelayMin(), textfield.getInteger()); break;
			case 5: ranged.setBurstDelay(textfield.getInteger()); break;
			case 6: ranged.setBurst(textfield.getInteger()); break;
			case 7: ranged.setSound(0, textfield.getText()); break;
			case 8: ranged.setShotCount(textfield.getInteger()); break;
			case 9: ranged.setMeleeRange(textfield.getInteger()); break;
			case 10: ranged.setSound(2, textfield.getText()); break;
			case 11: ranged.setSound(1, textfield.getText()); break;
		}
		initGui();
	}

}
