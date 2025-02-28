package noppes.npcs.client.gui;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.roles.data.HealerSettings;

public class SubGuiNpcJobHealerSettings
extends SubGuiInterface
implements ITextfieldListener {

	public HealerSettings hs;

	public SubGuiNpcJobHealerSettings(int id, HealerSettings settings) {
		background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubg.png");
		xSize = 171;
		ySize = 217;
		closeOnEsc = true;

		this.id = id;
		hs = settings;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getId()) {
			case 1: {
				hs.type = (byte) button.getValue();
				break;
			}
			case 2: {
				hs.isMassive = button.getValue() == 0;
				break;
			}
			case 3: {
				hs.onHimself = ((IGuiNpcCheckBox) button).isSelected();
				break;
			}
			case 4: {
				hs.possibleOnMobs = ((IGuiNpcCheckBox) button).isSelected();
				break;
			}
			case 66: {
				close();
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = 5;
		addLabel(new GuiNpcLabel(1, "beacon.range", guiLeft + 10, guiTop + y + 5));
		GuiNpcTextField textField = new GuiNpcTextField(1, this, fontRenderer, guiLeft + 123, guiTop + y, 45, 20, hs.range + "");
		textField.setMinMaxDefault(1, 64, 16);
		textField.setHoverText("beacon.hover.dist");
		addTextField(textField);
		y += 24;
		addLabel(new GuiNpcLabel(2, "stats.speed", guiLeft + 10, guiTop + y + 5));
		textField = new GuiNpcTextField(2, this, fontRenderer, guiLeft + 123, guiTop + y, 45, 20, hs.speed + "");
		textField.setMinMaxDefault(10, 72000, 20);
		textField.setHoverText("beacon.hover.speed");
		addTextField(textField);
		y += 24;
		addLabel(new GuiNpcLabel(3, "beacon.amplifier", guiLeft + 10, guiTop + y + 5));
		textField = new GuiNpcTextField(3, this, fontRenderer, guiLeft + 123, guiTop + y, 45, 20, (hs.amplifier + 1) + "");
		textField.setMinMaxDefault(1, 4, 1);
		textField.setHoverText("beacon.hover.power", new TextComponentTranslation("enchantment.level." + hs.amplifier).getFormattedText());
		addTextField(textField);
		y += 24;
		addLabel(new GuiNpcLabel(4, "gui.time", guiLeft + 10, guiTop + y + 5));
		textField = new GuiNpcTextField(4, this, fontRenderer, guiLeft + 123, guiTop + y, 45, 20, hs.time + "");
		textField.setMinMaxDefault(1, 72000, 1);
		textField.setHoverText("beacon.hover.time");
		addTextField(textField);
		y += 24;
		addLabel(new GuiNpcLabel(5, "beacon.affect", guiLeft + 10, guiTop + y + 5));
		GuiNpcButton button = new GuiNpcButton(1, guiLeft + 88, guiTop + y, 80, 20, new String[] { "faction.friendly", "faction.unfriendly", "spawner.all" }, hs.type);
		button.setHoverText("beacon.hover.type");
		addButton(button);
		y += 24;
		addLabel(new GuiNpcLabel(6, "beacon.applicability", guiLeft + 10, guiTop + y + 5));
		button = new GuiNpcButton(2, guiLeft + 88, guiTop + y, 80, 20, new String[] { "beacon.massive", "beacon.not.massive" }, hs.isMassive ? 0 : 1);
		button.setHoverText("beacon.hover.massive");
		addButton(button);
		y += 24;
		button = new GuiNpcCheckBox(3, guiLeft + 10, guiTop + y, 168, 20, "beacon.on.him.self", null, hs.onHimself);
		button.setHoverText("beacon.hover.on.him.self");
		addButton(button);
		y += 17;
		button = new GuiNpcCheckBox(4, guiLeft + 10, guiTop + y, 168, 20, "beacon.on.mobs", null, hs.possibleOnMobs);
		button.setHoverText("beacon.hover.on.mobs");
		addButton(button);
		button = new GuiNpcButton(66, guiLeft + 61, guiTop + ySize - 24, 45, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i == 1) {
			close();
		}
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		switch (textField.getId()) {
			case 1: {
				hs.range = textField.getInteger();
				break;
			}
			case 2: {
				hs.speed = textField.getInteger();
				break;
			}
			case 3: {
				hs.amplifier = textField.getInteger() - 1;
				textField.setHoverText("beacon.hover.power", new TextComponentTranslation("enchantment.level." + hs.amplifier).getFormattedText());
				break;
			}
			case 4: {
				hs.time = textField.getInteger();
				break;
			}
		}
	}

}
