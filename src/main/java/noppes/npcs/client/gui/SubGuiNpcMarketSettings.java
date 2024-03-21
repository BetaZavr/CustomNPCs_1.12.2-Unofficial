package noppes.npcs.client.gui;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.MarkupData;
import noppes.npcs.util.AdditionalMethods;

public class SubGuiNpcMarketSettings
extends SubGuiInterface
implements ICustomScrollListener, ITextfieldListener, ISubGuiListener, GuiYesNoCallback {

	public Marcet marcet;
	public int level;
	private Map<String, Integer> data;
	private GuiCustomScroll scroll;

	public SubGuiNpcMarketSettings(Marcet marcet) {
		super();
		this.marcet = marcet;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 217;
		this.closeOnEsc = true;
		this.level = 0;
		this.data = Maps.<String, Integer>newHashMap();
	}
	
	@Override
	public void initGui() {
		super.initGui();
		int lID = 0, x = this.guiLeft + 4, y = this.guiTop + 5;
		this.addLabel(new GuiNpcLabel(lID++, "role.marketname", x + 2, y + 5));
		this.addTextField(new GuiNpcTextField(0, this, x + 80, y, 167, 18, "" + this.marcet.name));
		
		y += 22;
		this.addLabel(new GuiNpcLabel(lID++, "market.uptime", x + 2, y + 5));
		this.addTextField(new GuiNpcTextField(1, this, x + 80, y, 60, 18, "" + this.marcet.updateTime));
		this.getTextField(1).setNumbersOnly();
		this.getTextField(1).setMinMaxDefault(0, 360, this.marcet.updateTime);
		if (this.marcet.updateTime>=5) {
			y += 22;
			this.addButton(new GuiNpcButton(0, x, y, 170, 20, new String[] { "market.limited.0" , "market.limited.1", "market.limited.2" }, this.marcet.limitedType));
		}

		this.addLabel(new GuiNpcLabel(lID++, "gui.sections", x + 176, y - 17));
		if (this.scroll == null) { (this.scroll = new GuiCustomScroll(this, 0)).setSize(72, 60); }
		this.scroll.guiLeft = x + 175;
		this.scroll.guiTop = y;
		List<String> list = Lists.<String>newArrayList();
		this.scroll.hoversTexts = new String[this.marcet.sections.size()][];
		int i = 0;
		this.data.clear();
		for (int id : this.marcet.sections.keySet()) {
			this.scroll.hoversTexts[i] = new String[] { "ID: "+ id, new TextComponentTranslation("gui.name").getFormattedText() + ": " + this.marcet.sections.get(id) };
			String key = new TextComponentTranslation(this.marcet.sections.get(id)).getFormattedText();
			this.data.put(key, id);
			list.add(key);
			i++;
		}
		this.scroll.setListNotSorted(list);
		this.addScroll(this.scroll);

		y += 22;
		this.addButton(new GuiNpcButton(1, x, y, 170, 20, "lines.title"));
		
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
		
		this.addButton(new GuiNpcButton(5, x + 175, y, 37, 20, "type.add"));
		this.addButton(new GuiNpcButton(6, x + 213, y, 35, 20, "type.del"));
		this.getButton(6).setEnabled(this.marcet.sections.size()>1 && this.scroll.selected > 0);
		
		y += 25;
		String[] values = new String[this.marcet.markup.size()];
		i = 0;
		for (int level : this.marcet.markup.keySet()) {
			values[i] = (new TextComponentTranslation("type.level")).getFormattedText() + " " + level;
			i++;
		}
		this.addLabel(new GuiNpcLabel(lID++, "gui.type", x + 2, y + 5));
		this.addButton(new GuiNpcButton(4, x + 22, y, 50, 20, values, this.level));
		
		MarkupData md = this.marcet.markup.get(this.level);
		if (md==null) {
			this.level = 0;
			if (!this.marcet.markup.containsKey(0)) { this.marcet.markup.put(0, new MarkupData(0, 0.15f, 0.80f, 1000)); }
			md = this.marcet.markup.get(this.level);
		}
		this.addLabel(new GuiNpcLabel(lID++, "market.extra.markup", x + 76, y + 5));
		this.addLabel(new GuiNpcLabel(lID++, "%", x + 174, y + 5));
		this.addTextField(new GuiNpcTextField(2, this, x + 120, y, 50, 20, ""+(int) (md.buy * 100.0f)));
		this.getTextField(2).setNumbersOnly();
		this.getTextField(2).setMinMaxDefault(-100, 500, (int) (md.buy * 100.0f));

		this.addLabel(new GuiNpcLabel(lID++, "%", x + 238, y + 5));
		this.addTextField(new GuiNpcTextField(3, this, x + 184, y, 50, 20, ""+(int) (md.sell * 100.0f)));
		this.getTextField(3).setDoubleNumbersOnly();
		this.getTextField(3).setMinMaxDoubleDefault(-500, 100, (int) (md.sell * 100.0f));

		y += 22;
		this.addLabel(new GuiNpcLabel(lID++, "quest.exp", x + 76, y + 5));
		this.addTextField(new GuiNpcTextField(4, this, x + 120, y, 50, 20, ""+md.xp));
		this.getTextField(4).setNumbersOnly();
		this.getTextField(4).setMinMaxDefault(0, Integer.MAX_VALUE, md.xp);
		
		this.addButton(new GuiNpcButton(66, x, this.guiTop + this.ySize - 24, 60, 20, "gui.done"));
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
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
			case 5: { // add section
				this.setSubGui(new SubGuiEditText(1, AdditionalMethods.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 6: { // del section
				if (this.scroll.selected < 0) { return; }
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, new TextComponentTranslation("gui.sections").getFormattedText() + ": " + this.scroll.getSelected(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 66: { // exit
				this.close();
				break;
			}
		}
	}
	
	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI((EntityPlayer) this.player, this);
		if (!result || this.scroll.selected < 0 || !this.data.containsKey(this.scroll.getSelected()) || this.marcet.sections.size() < 2) { return; }
		this.marcet.sections.remove(this.data.get(this.scroll.getSelected()));
		int i = 0;
		Map<Integer, String> newMap = Maps.<Integer, String>newTreeMap();
		for (String name : this.marcet.sections.values()) {
			newMap.put(i, name);
			i++;
		}
		this.marcet.sections.clear();
		this.marcet.sections.putAll(newMap);
		if (this.scroll.selected > 0) { this.scroll.selected--; }
		this.initGui();
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
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.section.add").getFormattedText());
		} else if (this.getButton(6)!=null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("market.hover.section.del").getFormattedText());
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
		if (this.hasSubGui()) { return; }
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

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiEditText) {
			if (((SubGuiEditText) subgui).cancelled) { return; }
			if (((SubGuiEditText) subgui).id == 1) {
				String name = ((SubGuiEditText) subgui).text[0];
				while(this.marcet.sections.containsValue(name)) { name += "_"; }
				this.marcet.sections.put(this.marcet.sections.size(), name);
			} else if (((SubGuiEditText) subgui).id == 2) {
				if (!this.data.containsKey(this.scroll.getSelected())) { return; }
				String name = ((SubGuiEditText) subgui).text[0];
				int idSel = this.data.get(this.scroll.getSelected());
				boolean next = true;
				while(next) {
					next = false;
					for (int id : this.marcet.sections.keySet()) {
						if (id == idSel) { continue; }
						if (this.marcet.sections.get(id).equals(name)) {
							name += "_";
							next = true;
							break;
						}
					}
				}
				this.marcet.sections.put(idSel, name);
				this.initGui();
			}
			this.initGui();
		}
		else if (subgui instanceof SubGuiNPCLinesEdit) {
			SubGuiNPCLinesEdit sub = (SubGuiNPCLinesEdit) subgui;
			sub.lines.correctLines();
			this.marcet.lines = sub.lines;
		}
	}
	
	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (this.getButton(6) !=null) { this.getButton(6).setEnabled(this.marcet.sections.size()>1 && this.scroll.selected > 0); }
	}
	
	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		if (this.scroll.getSelected() == null) { return; }
		this.setSubGui(new SubGuiEditText(2, this.scroll.getSelected()));
	}
	
}
