package noppes.npcs.client.gui;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.Faction;

public class SubGuiNpcFactionPoints extends SubGuiInterface implements ITextfieldListener {

	private final Faction faction;

	public SubGuiNpcFactionPoints(Faction faction) {
		this.faction = faction;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
		this.closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 66) {
			this.close();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(2, "faction.default", this.guiLeft + 4, this.guiTop + 33));
		this.addTextField(new GuiNpcTextField(2, this,
				this.guiLeft + 8 + this.fontRenderer.getStringWidth(this.getLabel(2).label.get(0)), this.guiTop + 28,
				70, 20, this.faction.defaultPoints + ""));
		this.getTextField(2).setMaxStringLength(6);
		this.getTextField(2).setNumbersOnly();
		String title = new TextComponentTranslation("faction.unfriendly").getFormattedText() + "<->"
				+ new TextComponentTranslation("faction.neutral").getFormattedText();
		this.addLabel(new GuiNpcLabel(3, title, this.guiLeft + 4, this.guiTop + 80));
		this.addTextField(new GuiNpcTextField(3, this, this.guiLeft + 8 + this.fontRenderer.getStringWidth(title),
				this.guiTop + 75, 70, 20, this.faction.neutralPoints + ""));
		title = new TextComponentTranslation("faction.neutral").getFormattedText() + "<->"
				+ new TextComponentTranslation("faction.friendly").getFormattedText();
		this.addLabel(new GuiNpcLabel(4, title, this.guiLeft + 4, this.guiTop + 105));
		this.addTextField(new GuiNpcTextField(4, this, this.guiLeft + 8 + this.fontRenderer.getStringWidth(title),
				this.guiTop + 100, 70, 20, this.faction.friendlyPoints + ""));
		this.getTextField(3).setNumbersOnly();
		this.getTextField(4).setNumbersOnly();
		if (this.getTextField(3).x > this.getTextField(4).x) {
			this.getTextField(4).x = this.getTextField(3).x;
		} else {
			this.getTextField(3).x = this.getTextField(4).x;
		}
		this.addButton(new GuiNpcButton(66, this.guiLeft + 20, this.guiTop + 192, 90, 20, "gui.done"));
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 2) {
			this.faction.defaultPoints = textfield.getInteger();
		} else if (textfield.getId() == 3) {
			this.faction.neutralPoints = textfield.getInteger();
		} else if (textfield.getId() == 4) {
			this.faction.friendlyPoints = textfield.getInteger();
		}
	}
}
