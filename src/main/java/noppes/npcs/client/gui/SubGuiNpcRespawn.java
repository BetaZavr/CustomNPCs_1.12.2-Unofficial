package noppes.npcs.client.gui;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.entity.data.DataStats;

public class SubGuiNpcRespawn
extends SubGuiInterface
implements ITextfieldListener {

	private final DataStats stats;

	public SubGuiNpcRespawn(DataStats ds) {
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		stats = ds;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0: {
				stats.spawnCycle = button.getValue();
				if (stats.spawnCycle == 3 || stats.spawnCycle == 4) {
					stats.respawnTime = 0;
				} else {
					stats.respawnTime = 20;
				}
				initGui();
				break;
			}
			case 4: {
				stats.hideKilledBody = (button.getValue() == 1);
				break;
			}
			case 66: close(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		addLabel(new GuiNpcLabel(0, "stats.respawn", guiLeft + 5, guiTop + 35));
		GuiNpcButton button = new GuiButtonBiDirectional(0, guiLeft + 122, guiTop + 30, 80, 20, new String[] { "gui.yes", "gui.day", "gui.night", "gui.no", "stats.naturally" }, stats.spawnCycle);
		ITextComponent mes = new TextComponentTranslation("stats.hover.respawn.type");
		mes.appendSibling(new TextComponentTranslation("stats.hover.respawn.type." + stats.spawnCycle));
		button.setHoverText(mes.getFormattedText());
		addButton(button);
		if (stats.respawnTime > 0) {
			addLabel(new GuiNpcLabel(3, "gui.time", guiLeft + 5, guiTop + 57));
			GuiNpcTextField textField = new GuiNpcTextField(2, this, fontRenderer, guiLeft + 122, guiTop + 53, 50, 18, stats.respawnTime + "");
			textField.setMinMaxDefault(1, Integer.MAX_VALUE, 20);
			textField.setHoverText("stats.hover.respawn.time");
			addTextField(textField);
			addLabel(new GuiNpcLabel(4, "stats.deadbody", guiLeft + 4, guiTop + 79));
			button = new GuiNpcButton(4, guiLeft + 122, guiTop + 74, 60, 20, new String[] { "gui.no", "gui.yes" }, (stats.hideKilledBody ? 1 : 0));
			button.setHoverText("stats.hover.respawn.body");
			addButton(button);
		}
		button = new GuiNpcButton(66, guiLeft + 82, guiTop + 190, 98, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		if (textField.getID() == 2) {
			stats.respawnTime = textField.getInteger();
		}
	}
}
