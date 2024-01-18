package noppes.npcs.client.gui;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.Lines;
import noppes.npcs.entity.EntityNPCInterface;

public class SubGuiNPCLinesEdit
extends SubGuiInterface
implements ICustomScrollListener, ISubGuiListener, ITextfieldListener  {
	
	public Lines lines;
	private Map<String, Integer> data;
	private GuiCustomScroll scroll;
	private String select, title;

	public SubGuiNPCLinesEdit(int id, EntityNPCInterface npc, Lines lines, String title) {
		super();
		this.id = id;
		this.npc = npc;
		this.lines = lines.copy();
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 217;
		this.closeOnEsc = true;
		this.data = Maps.<String, Integer>newLinkedHashMap();
		this.select = "";
		if (title==null) { title = ""; }
		this.title = title;
		Client.sendData(EnumPacketServer.MainmenuAdvancedGet);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (this.select.isEmpty() && this.scroll.hasSelected()) {
			this.select = this.scroll.getSelected();
		}
		switch(button.id) {
			case 0: { // add
				this.setSubGui(new SubGuiEditText(0, CustomNpcs.DefaultInteractLine));
				break;
			}
			case 1: { // remove
				if (!this.data.containsKey(this.select)) { return; }
				this.lines.remove(this.data.get(this.select));
				this.initGui();
				break;
			}
			case 2: { // sel sound
				if (!this.data.containsKey(this.select) || !this.lines.lines.containsKey(this.data.get(this.select))) {
					this.setSubGui(new SubGuiEditText(0, CustomNpcs.DefaultInteractLine)); // add
					return;
				}
				this.setSubGui(new GuiSoundSelection(this.lines.lines.get(this.data.get(this.select)).getSound()));
				break;
			}
			case 66: {
				this.close();
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.data.clear();
		String[][] ht = new String[this.lines.lines.size()][];
		int p = 0;
		ITextComponent t = new TextComponentTranslation("parameter.position");
		ITextComponent m = new TextComponentTranslation("parameter.iline.text");
		ITextComponent s = new TextComponentTranslation("parameter.sound.name");
		t.getStyle().setColor(TextFormatting.GRAY);
		m.getStyle().setColor(TextFormatting.GRAY);
		s.getStyle().setColor(TextFormatting.GRAY);
		List<String> suffixs = Lists.<String>newArrayList();
		for (int i : this.lines.lines.keySet()) {
			Line l = this.lines.lines.get(i);
			this.data.put(((char) 167) + "7" + i + ": " + ((char) 167) + "r" + l.getText(), i);
			String hover = t.getFormattedText() + ((char) 167) +"7: " + ((char) 167) + "f" + i + "<br>" +
					m.getFormattedText() + ((char) 167) +"7:" + "<br>" + l.getText();
			if (!l.getSound().isEmpty()) {
				hover += "<br>" + s.getFormattedText() + ((char) 167) +"7:"+ "<br>" + l.getSound();
				suffixs.add(((char) 167) + "7[" + ((char) 167) + "eS" + ((char) 167) + "7]");
			} else {
				suffixs.add("");
			}
			ht[p] = hover.split("<br>");
		}
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(this.xSize - 12, this.ySize - 63);
		}
		List<String> list = Lists.<String>newArrayList(this.data.keySet());
		this.scroll.setListNotSorted(list);
		this.scroll.guiLeft = this.guiLeft + 6;
		this.scroll.guiTop = this.guiTop + 14;
		Line line = null;
		if (!this.select.isEmpty()) {
			if (list.contains(this.select)) {
				line = this.lines.lines.get(this.data.get(this.select));
				this.scroll.setSelected(this.select);
			}
			else { this.select = ""; }
		}
		this.scroll.hoversTexts = ht;
		this.scroll.setSuffixs(suffixs);
		this.addScroll(this.scroll);
		this.addLabel(new GuiNpcLabel(1, this.title.isEmpty() ? "" : this.title, this.guiLeft, this.guiTop + 4));
		this.getLabel(1).center(this.xSize);
		int x = this.guiLeft + 6, y = this.guiTop + 170;
		this.addTextField(new GuiNpcTextField(0, this, x, y, this.xSize - 86, 20, line==null ? "" : line.getText()));
		this.addButton(new GuiNpcButton(0, this.guiLeft + this.xSize - 77, y, 50, 20, "gui.add"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + this.xSize - 25, y, 20, 20, "X"));
		y += 22;
		this.addTextField(new GuiNpcTextField(1, this, x + 52, y, this.xSize - 116, 20, line==null ? "" : line.getSound()));
		this.addButton(new GuiNpcButton(2, this.guiLeft + this.xSize - 55, y, 50, 20, "availability.select"));
		this.addButton(new GuiNpcButton(66, x, y, 50, 20, "gui.done"));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("lines.hover.add").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("lines.hover.remove").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bard.hover.select").getFormattedText());
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("lines.hover.text").getFormattedText());
		} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("lines.hover.sound").getFormattedText());
		}
	}
	
	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiEditText) {
			SubGuiEditText sub = (SubGuiEditText) subgui;
			if (sub.cancelled || sub.text[0].isEmpty()) { return; }
			Line line = new Line(sub.text[0]);
			this.lines.correctLines();
			int p = this.lines.lines.size();
			this.lines.lines.put(p, line);
			this.select = ((char) 167) + "7" + p + ": " + ((char) 167) + "r" + line.getText();
			this.initGui();
		} else if (subgui instanceof GuiSoundSelection) {
			if (!this.data.containsKey(this.select)) { return; }
			GuiSoundSelection sub = (GuiSoundSelection) subgui;
			if (sub.selectedResource == null) { return; }
			if (!this.data.containsKey(this.select) || !this.lines.lines.containsKey(this.data.get(this.select))) { return; }
			this.lines.lines.get(this.data.get(this.select)).setSound(sub.selectedResource.toString());
			this.initGui();
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (!this.data.containsKey(scroll.getSelected())) { return; }
		this.select = scroll.getSelected();
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch(textField.getId()) {
			case 0: {
				if (!this.data.containsKey(this.select) || !this.lines.lines.containsKey(this.data.get(this.select))) { return; }
				this.lines.lines.get(this.data.get(this.select)).setText(textField.getText());
				this.select = textField.getText();
				this.initGui();
				break;
			}
		}
	}
	
}
