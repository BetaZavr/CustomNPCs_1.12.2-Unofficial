package noppes.npcs.client.gui;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.Faction;

import javax.annotation.Nonnull;

public class SubGuiNpcFactionPoints extends SubGuiInterface implements ITextfieldListener {

	protected final Faction faction;

	public SubGuiNpcFactionPoints(Faction factionIn) {
		super(0);
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		faction = factionIn;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		if (button.id == 66) { onClosed(); }
	}

	@Override
	public void initGui() {
		super.initGui();
		// default Points
		addLabel(new GuiNpcLabel(2, "faction.default", guiLeft + 4, guiTop + 33));
		addTextField(new GuiNpcTextField(2, this, guiLeft + 8 + fontRenderer.getStringWidth(getLabel(2).getLabels().get(0)), guiTop + 28, 70, 20, faction.defaultPoints + "")
				.setMinMaxDefault(-999999, 999999, 0)
				.setHoverText("faction.hover.point.def"));
		// unfriendly -> neutral
		String title = new TextComponentTranslation("faction.unfriendly").getFormattedText() + "<->" + new TextComponentTranslation("faction.neutral").getFormattedText();
		addLabel(new GuiNpcLabel(3, title, guiLeft + 4, guiTop + 80));
		addTextField(new GuiNpcTextField(3, this, guiLeft + 8 + fontRenderer.getStringWidth(title), guiTop + 75, 70, 20, faction.neutralPoints + "")
				.setMinMaxDefault(-999999, 999999, 0)
				.setHoverText("faction.hover.point.unfr"));
		// neutral -> friendly
		title = new TextComponentTranslation("faction.neutral").getFormattedText() + "<->" + new TextComponentTranslation("faction.friendly").getFormattedText();
		addLabel(new GuiNpcLabel(4, title, guiLeft + 4, guiTop + 105));
		addTextField(new GuiNpcTextField(4, this, guiLeft + 8 + fontRenderer.getStringWidth(title), guiTop + 100, 70, 20, faction.friendlyPoints + "")
				.setMinMaxDefault(-999999, 999999, 0)
				.setHoverText("faction.hover.point.unfr"));
		if (getTextField(3).x > getTextField(4).x) { getTextField(4).x = getTextField(3).x; }
		else { getTextField(3).x = getTextField(4).x; }
		// exit
		addButton(new GuiNpcButton(66, guiLeft + 20, guiTop + 192, 90, 20, "gui.done")
				.setHoverText("hover.back"));
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		switch (textfield.getID()) {
			case 2: faction.defaultPoints = textfield.getInteger(); break;
			case 3: faction.neutralPoints = textfield.getInteger(); break;
			case 4: faction.friendlyPoints = textfield.getInteger(); break;
		}
	}

}
