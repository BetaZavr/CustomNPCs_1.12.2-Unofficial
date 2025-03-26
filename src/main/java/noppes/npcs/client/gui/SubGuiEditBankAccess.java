package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.Bank;

public class SubGuiEditBankAccess
extends SubGuiInterface
implements ICustomScrollListener, ITextfieldListener, ISubGuiListener {

	public final List<String> names;
	public String owner;
	public boolean white;
	public boolean isChanging;

	private GuiCustomScroll scroll;
	private String sel;

	public SubGuiEditBankAccess(int id, Bank bank) {
		this.id = id;
		setBackground("smallbg.png");
		xSize = 176;
		ySize = 223;
		closeOnEsc = true;

		owner = bank.owner;
		names = new ArrayList<>(bank.access);
		white = bank.isWhiteList;
		isChanging = bank.isChanging;
		sel = "";
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0: { // add
				SubGuiEditText gui = new SubGuiEditText(3, "");
				gui.hovers = new String[] { "hover.player" };
				setSubGui(gui);
				break;
			}
			case 1: { // delete
				if (scroll.getSelect() < 0 || scroll.getSelect() >= names.size()) {
					return;
				}
				if (sel.equals(scroll.getSelected())) {
					sel = "";
				}
				int selId = scroll.getSelect();
				names.remove(scroll.getSelect());
				scroll.setSelect(selId - 1);
				if (scroll.getSelect() < 0) {
					if (names.isEmpty()) {
						scroll.setSelect(-1);
					} else {
						scroll.setSelect(0);
					}
				}
				initGui();
				break;
			}
			case 2: { // add
				white = ((GuiNpcCheckBox) button).isSelected();
				button.setHoverText("bank.hover." + (white ? "iswhite" : "isblack"));
				break;
			}
			case 3: {
				isChanging = ((GuiNpcCheckBox) button).isSelected();
				button.setHoverText("bank.hover.changed." + isChanging);
				break;
			}
			case 66: {
				close();
				break;
			}
		}
	}

	@Override
	public void subGuiClosed(ISubGuiInterface gui) {
		if (gui instanceof SubGuiEditText) {
			String name = ((SubGuiEditText) gui).text[0];
			if (name.length() < 4 || name.indexOf(' ') != -1
					|| (!Character.isLetter(name.charAt(0)) && name.charAt(0) != '_') || names.contains(name)
					|| owner.equals(name)) {
				return;
			}
			sel = name;
			names.add(sel);
			initGui();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = guiLeft + 4;
		int y = guiTop + 14;
		// owner
		addLabel(new GuiNpcLabel(0, new TextComponentTranslation("bank.owner").getFormattedText() + ":", x, y - 10));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, x, y, 168, 20, owner);
		textField.setHoverText("bank.hover.owner");
		addTextField(textField);
		// data
		if (scroll == null) { (scroll = new GuiCustomScroll(this, 0)).setSize(168, 145); }
		scroll.setList(names);
		scroll.guiLeft = x;
		scroll.guiTop = (y += 23);
		addScroll(scroll);
		if (!sel.isEmpty()) { scroll.setSelected(sel); }
		else {
			sel = "";
			if (scroll.getSelected() != null) {
				sel = scroll.getSelected();
			}
		}
		// white / black list
		GuiNpcButton button = new GuiNpcCheckBox(2, x, (y += 1 + scroll.height), 82, 12, "bank.iswhite", "bank.isblack", white);
		button.setHoverText("bank.hover." + (white ? "iswhite" : "isblack"));
		addButton(button);
		// changed
		button = new GuiNpcCheckBox(3, x + 86, y, 82, 12, "bank.changed.true", "bank.changed.false", isChanging);
		button.setHoverText("bank.hover.changed." + isChanging);
		addButton(button);
		// exit
		button = new GuiNpcButton(66, x += 1, (y += 14), 54, 20, "gui.back");
		button.setHoverText("hover.back");
		addButton(button);
		// add
		button = new GuiNpcButton(0, x += 56, y, 54, 20, "gui.add");
		button.setHoverText("bank.hover.player.add");
		addButton(button);
		// del
		button = new GuiNpcButton(1, x + 56, y, 54, 20, "gui.remove");
		button.setEnabled(scroll.hasSelected());
		button.setHoverText("bank.hover.player.del");
		addButton(button);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		sel = scroll.getSelected();
		if (getButton(1) != null) {
			getButton(1).setEnabled(scroll.hasSelected());
		}
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		if (hasSubGui()) {
			return;
		}
		owner = textField.getFullText();
	}

}
