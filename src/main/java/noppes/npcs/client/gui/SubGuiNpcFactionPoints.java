package noppes.npcs.client.gui;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.Faction;

public class SubGuiNpcFactionPoints
extends SubGuiInterface
implements ITextfieldListener {

	private final Faction faction;

	public SubGuiNpcFactionPoints(Faction faction) {
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		this.faction = faction;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 66) {
			close();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// default Points
		addLabel(new GuiNpcLabel(2, "faction.default", guiLeft + 4, guiTop + 33));
		GuiNpcTextField textField = new GuiNpcTextField(2, this, guiLeft + 8 + fontRenderer.getStringWidth(getLabel(2).label.get(0)), guiTop + 28, 70, 20, faction.defaultPoints + "");
		textField.setMinMaxDefault(-999999, 999999, 0);
		textField.setHoverText("faction.hover.point.def");
		addTextField(textField);
		// unfriendly -> neutral
		String title = new TextComponentTranslation("faction.unfriendly").getFormattedText() + "<->" + new TextComponentTranslation("faction.neutral").getFormattedText();
		addLabel(new GuiNpcLabel(3, title, guiLeft + 4, guiTop + 80));
		textField = new GuiNpcTextField(3, this, guiLeft + 8 + fontRenderer.getStringWidth(title), guiTop + 75, 70, 20, faction.neutralPoints + "");
		textField.setMinMaxDefault(-999999, 999999, 0);
		textField.setHoverText("faction.hover.point.unfr");
		addTextField(textField);
		// neutral -> friendly
		title = new TextComponentTranslation("faction.neutral").getFormattedText() + "<->" + new TextComponentTranslation("faction.friendly").getFormattedText();
		addLabel(new GuiNpcLabel(4, title, guiLeft + 4, guiTop + 105));
		textField = new GuiNpcTextField(4, this, guiLeft + 8 + fontRenderer.getStringWidth(title), guiTop + 100, 70, 20, faction.friendlyPoints + "");
		textField.setMinMaxDefault(-999999, 999999, 0);
		textField.setHoverText("faction.hover.point.unfr");
		addTextField(textField);
		if (getTextField(3).x > getTextField(4).x) { getTextField(4).x = getTextField(3).x; }
		else { getTextField(3).x = getTextField(4).x; }
		// exit
		GuiNpcButton button = new GuiNpcButton(66, guiLeft + 20, guiTop + 192, 90, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		switch (textfield.getId()) {
			case 2: faction.defaultPoints = textfield.getInteger(); break;
			case 3: faction.neutralPoints = textfield.getInteger(); break;
			case 4: faction.friendlyPoints = textfield.getInteger(); break;
		}
	}

}
