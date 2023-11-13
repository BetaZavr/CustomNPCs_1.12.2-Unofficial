package noppes.npcs.client.gui;

import java.util.Arrays;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.MarkupData;
import noppes.npcs.util.AdditionalMethods;

public class SubGuiNpcMarketSettings
extends SubGuiInterface
implements ITextfieldListener {

	public Marcet marcet;
	public int level;

	public SubGuiNpcMarketSettings(Marcet marcet) {
		super();
		this.marcet = marcet;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 217;
		this.closeOnEsc = true;
		this.level = 0;
	}
	@Override
	public void initGui() {
		super.initGui();
		int x = this.guiLeft + 4, y = this.guiTop + 5;
		this.addLabel(new GuiNpcLabel(0, "role.marketname", x + 2, y + 5));
		this.addTextField(new GuiNpcTextField(0, this, x + 80, y, 167, 18, "" + this.marcet.name));
		
		y += 22;
		this.addLabel(new GuiNpcLabel(1, "market.uptime", x + 2, y + 5));
		this.addTextField(new GuiNpcTextField(1, this, x + 80, y, 60, 18, "" + this.marcet.updateTime));
		this.getTextField(1).setNumbersOnly();
		this.getTextField(1).setMinMaxDefault(0, 360, this.marcet.updateTime);
		if (this.marcet.updateTime>=5) {
			y += 22;
			this.addButton(new GuiNpcButton(0, x, y, 200, 20, new String[] { "market.limited.0" , "market.limited.1", "market.limited.2" }, this.marcet.limitedType));
		}

		y += 22;
		this.addButton(new GuiNpcButton(1, x, y, 200, 20, "lines.title"));
		
		y += 20;
		GuiNpcCheckBox checkBox = new GuiNpcCheckBox(2, x, y, 170, 18, "");
		checkBox.setSelected(this.marcet.isLimited);
		checkBox.setText("market.select.limited."+this.marcet.isLimited);
		this.addButton(checkBox);
		
		y += 20;
		checkBox = new GuiNpcCheckBox(3, x, y, 170, 18, "");
		checkBox.setSelected(this.marcet.showXP);
		checkBox.setText("market.select.show.xp."+this.marcet.showXP);
		this.addButton(checkBox);

		y += 25;
		String[] values = new String[this.marcet.markup.size()];
		int i = 0;
		for (int level : this.marcet.markup.keySet()) {
			values[i] = (new TextComponentTranslation("type.level")).getFormattedText() + " " + level;
			i++;
		}
		this.addLabel(new GuiNpcLabel(2, "gui.type", x + 2, y + 5));
		this.addButton(new GuiNpcButton(4, x + 22, y, 50, 20, values, this.level));
		
		MarkupData md = this.marcet.markup.get(this.level);
		if (md==null) {
			this.level = 0;
			if (!this.marcet.markup.containsKey(0)) { this.marcet.markup.put(0, new MarkupData(0, 0.15f, 0.80f, 1000)); }
			md = this.marcet.markup.get(this.level);
		}
		this.addLabel(new GuiNpcLabel(3, "market.extra.markup", x + 76, y + 5));
		this.addLabel(new GuiNpcLabel(4, "%", x + 174, y + 5));
		this.addTextField(new GuiNpcTextField(2, this, x + 120, y, 50, 20, ""+(int) (md.buy * 100.0f)));
		this.getTextField(2).setNumbersOnly();
		this.getTextField(2).setMinMaxDefault(-100, 500, (int) (md.buy * 100.0f));

		this.addLabel(new GuiNpcLabel(5, "%", x + 238, y + 5));
		this.addTextField(new GuiNpcTextField(3, this, x + 184, y, 50, 20, ""+(int) (md.sell * 100.0f)));
		this.getTextField(3).setDoubleNumbersOnly();
		this.getTextField(3).setMinMaxDoubleDefault(-500, 100, (int) (md.sell * 100.0f));

		y += 22;
		this.addLabel(new GuiNpcLabel(6, "quest.exp", x + 76, y + 5));
		this.addTextField(new GuiNpcTextField(4, this, x + 120, y, 50, 20, ""+md.xp));
		this.getTextField(4).setNumbersOnly();
		this.getTextField(4).setMinMaxDefault(0, Integer.MAX_VALUE, md.xp);
		
		this.addButton(new GuiNpcButton(66, x, this.guiTop + this.ySize - 24, 60, 20, "gui.done"));
	}

	public void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
			case 0: {
				this.marcet.limitedType = button.getValue();
				break;
			}
			case 1: { // message
				this.setSubGui(new SubGuiNPCLinesEdit(0, this.npc, this.marcet.lines, null));
				break;
			}
			case 2: { // is limited
				this.marcet.isLimited = ((GuiNpcCheckBox) button).isSelected();
				((GuiNpcCheckBox) button).setText("market.select.limited."+this.marcet.isLimited);
				break;
			}
			case 3: { // show xp
				this.marcet.showXP = ((GuiNpcCheckBox) button).isSelected();
				((GuiNpcCheckBox) button).setText("market.select.show.xp."+this.marcet.showXP);
				break;
			}
			case 4: { // level
				this.level = button.getValue();
				if (!this.marcet.markup.containsKey(0)) { this.marcet.markup.put(0, new MarkupData(0, 0.15f, 0.80f, 1000)); }
				if (!this.marcet.markup.containsKey(this.level)) { this.level = 0; }
				this.initGui();
				break;
			}
			case 66: { // exit
				this.close();
				break;
			}
		}
	}
	
	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (this.subgui!=null) { return; }
		if (this.getButton(4)!=null) {
			this.drawHorizontalLine(this.guiLeft + 4, this.guiLeft + this.xSize - 4, this.getButton(4).y - 3, 0x80000000);
			this.drawHorizontalLine(this.guiLeft + 4, this.guiLeft + this.xSize - 4, this.getButton(4).y + 44, 0x80000000);
		}
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.set.name", new TextComponentTranslation(this.marcet.name).getFormattedText()).getFormattedText());
		} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.set.update", new Object[] { AdditionalMethods.ticksToElapsedTime(this.marcet.updateTime * 1200, false, false, false) }).getFormattedText());
		} else if (this.getTextField(2)!=null && this.getTextField(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.extra.buy").getFormattedText());
		} else if (this.getTextField(3)!=null && this.getTextField(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.extra.sell").getFormattedText());
		} else if (this.getTextField(4)!=null && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.xp").getFormattedText());
		} else if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.only.ñurrency."+this.getButton(0).getValue()).getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.message").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.limited").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.show.xp").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.extra.slot").getFormattedText());
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}
	
	@Override
	public void unFocused(GuiNpcTextField textField) {
		String text = textField.getText();
		MarkupData md = this.marcet.markup.get(this.level);
		switch (textField.getId()) {
			case 0: {
				if (text.equals(this.marcet.name)) { return; }
				this.marcet.name = text;
				this.initGui();
				break;
			}
			case 1: {
				int time = textField.getInteger();
				if (time < 5) { time = 0; }
				if (time > 360) { time = 360; }
				this.marcet.updateTime = time;
				this.initGui();
				break;
			}
			case 2: {
				if (md==null) { return; }
				md.buy = (float) (Math.round((double) textField.getInteger() * 100.0d) / 10000.0d);
				this.initGui();
				break;
			}
			case 3: {
				if (md==null) { return; }
				md.sell = (float) (Math.round((double) textField.getInteger() * 100.0d) / 10000.0d);
				this.initGui();
				break;
			}
			case 4: {
				if (md==null) { return; }
				md.xp = textField.getInteger();
				break;
			}
		}
	}

}
