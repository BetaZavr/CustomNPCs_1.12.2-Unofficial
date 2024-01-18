package noppes.npcs.client.gui;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.roles.data.HealerSettings;

public class SubGuiNpcJobHealerSettings
extends SubGuiInterface
implements ITextfieldListener {

	public HealerSettings hs;

	public SubGuiNpcJobHealerSettings(int id, HealerSettings settings) {
		this.background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubg.png");
		this.xSize = 171;
		this.ySize = 217;
		this.closeOnEsc = true;
		this.id = id;
		this.hs = settings;
	}
	
	@Override
	public void initGui() {
		super.initGui();
		int y = 5;
		this.addLabel(new GuiNpcLabel(1, "beacon.range", this.guiLeft + 10, this.guiTop + y + 5));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 123, this.guiTop + y, 45, 20, this.hs.range + ""));
		this.getTextField(1).setNumbersOnly();
		this.getTextField(1).setMinMaxDefault(1, 64, 16);
		y += 24;
		this.addLabel(new GuiNpcLabel(2, "stats.speed", this.guiLeft + 10, this.guiTop + y + 5));
		this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, this.guiLeft + 123, this.guiTop + y, 45, 20, this.hs.speed + ""));
		this.getTextField(2).setNumbersOnly();
		this.getTextField(2).setMinMaxDefault(10, 72000, 20);
		y += 24;
		this.addLabel(new GuiNpcLabel(3, "beacon.amplifier", this.guiLeft + 10, this.guiTop + y + 5));
		this.addTextField(new GuiNpcTextField(3, this, this.fontRenderer, this.guiLeft + 123, this.guiTop + y, 45, 20, (this.hs.amplifier+1) + ""));
		this.getTextField(3).setNumbersOnly();
		this.getTextField(3).setMinMaxDefault(1, 4, 1);
		y += 24;
		this.addLabel(new GuiNpcLabel(4, "gui.time", this.guiLeft + 10, this.guiTop + y + 5));
		this.addTextField(new GuiNpcTextField(4, this, this.fontRenderer, this.guiLeft + 123, this.guiTop + y, 45, 20, this.hs.time + ""));
		this.getTextField(4).setNumbersOnly();
		this.getTextField(4).setMinMaxDefault(1, 72000, 1);
		y += 24;
		this.addLabel(new GuiNpcLabel(5, "beacon.affect", this.guiLeft + 10, this.guiTop + y + 5));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 88, this.guiTop + y, 80, 20, new String[] { "faction.friendly", "faction.unfriendly", "spawner.all" }, this.hs.type));
		y += 24;
		this.addLabel(new GuiNpcLabel(6, "beacon.applicability", this.guiLeft + 10, this.guiTop + y + 5));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 88, this.guiTop + y, 80, 20, new String[] { "beacon.massive", "beacon.not.massive" }, this.hs.isMassive ? 0 : 1));
		y += 24;
		GuiNpcCheckBox checkBox = new GuiNpcCheckBox(3, this.guiLeft + 10, this.guiTop + y, 168, 20, "beacon.on.him.self");
		checkBox.setSelected(this.hs.onHimself);
		this.addButton(checkBox);
		y += 17;
		checkBox = new GuiNpcCheckBox(4, this.guiLeft + 10, this.guiTop + y, 168, 20, "beacon.on.mobs");
		checkBox.setSelected(this.hs.possibleOnMobs);
		this.addButton(checkBox);
		this.addButton(new GuiNpcButton(66, this.guiLeft + 61, this.guiTop + this.ySize - 24, 45, 20, "gui.done"));
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch(button.id) {
			case 1: {
				this.hs.type = (byte) button.getValue();
				break;
			}
			case 2: {
				this.hs.isMassive = button.getValue() == 0;
				break;
			}
			case 3: {
				if (!(button instanceof GuiNpcCheckBox)) { return; }
				this.hs.onHimself = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 4: {
				if (!(button instanceof GuiNpcCheckBox)) { return; }
				this.hs.possibleOnMobs = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 66: {
				this.close();
				break;
			}
		}
	}
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("beacon.hover.type").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("beacon.hover.massive").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("beacon.hover.on.him.self").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("beacon.hover.on.mobs").getFormattedText());
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("beacon.hover.dist").getFormattedText());
		} else if (this.getTextField(2)!=null && this.getTextField(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("beacon.hover.speed").getFormattedText());
		} else if (this.getTextField(3)!=null && this.getTextField(3).isMouseOver()) {
			int p = this.hs.amplifier;
			if (this.getTextField(3).isInteger()) { p = this.getTextField(3).getInteger() - 1; }
			this.setHoverText(new TextComponentTranslation("beacon.hover.power", new TextComponentTranslation("enchantment.level." + p).getFormattedText()).getFormattedText());
		} else if (this.getTextField(4)!=null && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("beacon.hover.time").getFormattedText());
		}
	}
	
	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch(textField.getId()) {
			case 1: { this.hs.range = textField.getInteger(); break; }
			case 2: { this.hs.speed = textField.getInteger(); break; }
			case 3: { this.hs.amplifier = textField.getInteger() - 1; break; }
			case 4: { this.hs.time = textField.getInteger(); break; }
		}
	}
	
	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i == 1) { this.close(); }
	}
	
}
