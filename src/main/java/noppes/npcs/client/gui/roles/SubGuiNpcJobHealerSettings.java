package noppes.npcs.client.gui.roles;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.roles.data.HealerSettings;

import javax.annotation.Nonnull;

public class SubGuiNpcJobHealerSettings extends SubGuiInterface implements ITextfieldListener {

	public HealerSettings healerSettings;

	public SubGuiNpcJobHealerSettings(int id, HealerSettings settings) {
		super(id);
		setBackground("menubg.png");
		closeOnEsc = true;
		xSize = 171;
		ySize = 217;

		healerSettings = settings;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 1: healerSettings.type = (byte) button.getValue(); break;
			case 2: healerSettings.isMassive = button.getValue() == 0; break;
			case 3: healerSettings.onHimself = ((GuiNpcCheckBox) button).isSelected(); break;
			case 4: healerSettings.possibleOnMobs = ((GuiNpcCheckBox) button).isSelected(); break;
			case 66: onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x0 = guiLeft + 10;
		int x1 = guiLeft + 123;
		int y = guiTop + 5;
		addLabel(new GuiNpcLabel(1, "beacon.range", x0, y + 5));
		addTextField(new GuiNpcTextField(1, this, x1, y, 45, 20, healerSettings.range + "")
				.setMinMaxDefault(1, 64, 16)
				.setHoverText("beacon.hover.dist"));
		addLabel(new GuiNpcLabel(2, "stats.speed", x0, (y += 24) + 5));
		addTextField(new GuiNpcTextField(2, this, x1, y, 45, 20, healerSettings.speed + "")
				.setMinMaxDefault(10, 72000, 20)
				.setHoverText("beacon.hover.speed"));
		addLabel(new GuiNpcLabel(3, "beacon.amplifier", x0, (y += 24) + 5));
		String lv = "enchantment.level." + healerSettings.amplifier;
		if (!new TextComponentTranslation(lv).getFormattedText().equals(lv)) { lv = new TextComponentTranslation(lv).getFormattedText(); }
		else { lv = "" + (healerSettings.amplifier + 1); }
		addTextField(new GuiNpcTextField(3, this, x1, y, 45, 20, (healerSettings.amplifier + 1) + "")
				.setMinMaxDefault(1, 4, 1)
				.setHoverText("beacon.hover.power", lv));
		addLabel(new GuiNpcLabel(4, "gui.time", x0, (y += 24) + 5));
		addTextField(new GuiNpcTextField(4, this, x1, y, 45, 20, healerSettings.time + "")
				.setMinMaxDefault(1, 72000, 1)
				.setHoverText("beacon.hover.time"));
		addLabel(new GuiNpcLabel(5, "beacon.affect", x0, (y += 24) + 5));
		addButton(new GuiNpcButton(1, guiLeft + 88, y, 80, 20, new String[] { "faction.friendly", "faction.unfriendly", "spawner.all" }, healerSettings.type)
				.setHoverText("beacon.hover.type"));
		addLabel(new GuiNpcLabel(6, "beacon.applicability", x0, (y += 24) + 5));
		addButton(new GuiNpcButton(2, guiLeft + 88, y, 80, 20, new String[] { "beacon.massive", "beacon.not.massive" }, healerSettings.isMassive ? 0 : 1)
				.setHoverText("beacon.hover.massive"));
		addButton(new GuiNpcCheckBox(3, x0, y += 24, 168, 20, "beacon.on.him.self", null, healerSettings.onHimself)
				.setHoverText("beacon.hover.on.him.self"));
		addButton(new GuiNpcCheckBox(4, x0, y + 17, 168, 20, "beacon.on.mobs", null, healerSettings.possibleOnMobs)
				.setHoverText("beacon.hover.on.mobs"));
		addButton(new GuiNpcButton(66, guiLeft + 61, guiTop + ySize - 24, 45, 20, "gui.done")
				.setHoverText("hover.back"));
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getID()) {
			case 1: healerSettings.range = textField.getInteger(); break;
			case 2: healerSettings.speed = textField.getInteger(); break;
			case 3: {
				healerSettings.amplifier = textField.getInteger() - 1;
				String lv = "enchantment.level." + healerSettings.amplifier;
				if (!new TextComponentTranslation(lv).getFormattedText().equals(lv)) { lv = new TextComponentTranslation(lv).getFormattedText(); }
				else { lv = "" + (healerSettings.amplifier + 1); }
				textField.setHoverText("beacon.hover.power", lv);
				break;
			}
			case 4: healerSettings.time = textField.getInteger(); break;
		}
	}

}
