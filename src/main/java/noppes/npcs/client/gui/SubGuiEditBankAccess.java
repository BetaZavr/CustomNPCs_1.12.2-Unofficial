package noppes.npcs.client.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.controllers.data.Bank;

public class SubGuiEditBankAccess
extends SubGuiInterface
implements ICustomScrollListener, ITextfieldListener {

	public final List<String> names;
	public String owner;
	public boolean white;
	
	private GuiCustomScroll scroll;
	private String sel;

	public SubGuiEditBankAccess(int id, Bank bank) {
		this.owner = bank.owner;
		this.names = Lists.newArrayList(bank.access);
		this.white = bank.isWhiteList;
		this.sel = "";
		
		this.setBackground("smallbg.png");
		this.xSize = 176;
		this.ySize = 223;
		this.closeOnEsc = true;
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = this.guiLeft + 4;
		int y = this.guiTop + 14;
		
		this.addLabel(new GuiNpcLabel(0, new TextComponentTranslation("bank.owner").getFormattedText()+":", x, y - 10));
		this.addTextField(new GuiNpcTextField(0, this, x, y, 168, 20, this.owner));
		
		if (this.scroll == null) { (this.scroll = new GuiCustomScroll(this, 0)).setSize(168, 145); }
		Collections.sort(this.names);
		this.scroll.setList(this.names);
		this.scroll.guiLeft = x;
		this.scroll.guiTop = (y += 23);
		this.addScroll(this.scroll);
		if (!this.sel.isEmpty()) { this.scroll.setSelected(this.sel); }
		else {
			this.sel = "";
			if (this.scroll.getSelected()!=null) { this.sel = this.scroll.getSelected(); }
		}
		
		GuiNpcCheckBox checkBox = new GuiNpcCheckBox(2, x, (y += 1 + this.scroll.height), 168, 12, "bank."+(this.white ? "iswhite" : "isblack"));
		checkBox.setSelected(this.white);
		this.addButton(checkBox);
		
		this.addButton(new GuiNpcButton(66, x, (y += 14), 60, 20, "gui.back"));
		
		this.addButton(new GuiNpcButton(0, x + 62, y, 52, 20, "gui.add"));
		this.addButton(new GuiNpcButton(1, x + 116, y, 52, 20, "gui.remove"));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.getButton(1)!=null) { this.getButton(1).enabled = this.scroll.hasSelected(); }
		if (!CustomNpcs.ShowDescriptions && !this.hasSubGui()) { return; }
		if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover.player.add").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover.player.del").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover."+(this.white ? "iswhite" : "isblack")).getFormattedText());
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bank.hover.owner").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch(button.id) {
			case 0: { // add
				SubGuiEditText gui = new SubGuiEditText(3, "");
				gui.hovers = new String[] { "hover.player" };
				this.setSubGui(gui);
				break;
			}
			case 1: { // delete
				if (this.scroll.selected < 0 || this.scroll.selected >= this.names.size()) { return; }
				if (this.sel.equals(this.scroll.getSelected())) {
					this.sel = "";
				}
				this.names.remove(this.scroll.selected);
				this.scroll.selected--;
				if (this.scroll.selected < 0) {
					if (this.names.isEmpty()) { this.scroll.selected = -1; }
					else { this.scroll.selected = 0; }
					
				}
				this.initGui();
				break;
			}
			case 2: { // add
				this.white = ((GuiNpcCheckBox) button).isSelected();
				this.initGui();
				break;
			}
			case 66: {
				this.close();
				break;
			}
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (this.hasSubGui()) { return; }
		this.owner = textField.getText();
	}

	@Override
	public void closeSubGui(SubGuiInterface gui) {
		super.closeSubGui(gui);
		if (gui instanceof SubGuiEditText) {
			String name = ((SubGuiEditText) gui).text[0];
			if (name.length() < 4 ||
					name.indexOf(' ')!=-1 ||
					(!Character.isLetter(name.charAt(0)) && name.charAt(0) != '_') ||
					this.names.contains(name) ||
					this.owner.equals(name)
				) { return; }
			this.sel = name;
			this.names.add(this.sel);
			this.initGui();
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		this.sel = scroll.getSelected();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }
	
}
