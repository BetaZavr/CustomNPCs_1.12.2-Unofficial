package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.Bank;

import javax.annotation.Nonnull;

public class SubGuiEditBankAccess  extends SubGuiInterface
		implements ICustomScrollListener, ITextfieldListener {

	protected GuiCustomScroll scroll;
	protected String sel;
	public final List<String> names;
	public String owner;
	public boolean white;
	public boolean isChanging;


	public SubGuiEditBankAccess(int id, Bank bank) {
		super(id);
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
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				SubGuiEditText gui = new SubGuiEditText(3, "");
				gui.hovers = new String[] { "hover.player" };
				setSubGui(gui);
				break;
			} // add
			case 1: {
				if (scroll.getSelect() < 0 || scroll.getSelect() >= names.size()) { return; }
				if (sel.equals(scroll.getSelected())) { sel = ""; }
				int selId = scroll.getSelect();
				names.remove(scroll.getSelect());
				scroll.setSelect(selId - 1);
				if (scroll.getSelect() < 0) {
					if (names.isEmpty()) { scroll.setSelect(-1); }
					else { scroll.setSelect(0); }
				}
				initGui();
				break;
			} // delete
			case 2: {
				white = ((GuiNpcCheckBox) button).isSelected();
				button.setHoverText("bank.hover." + (white ? "iswhite" : "isblack"));
				break;
			} // add
			case 3: {
				isChanging = ((GuiNpcCheckBox) button).isSelected();
				button.setHoverText("bank.hover.changed." + isChanging);
				break;
			} // changed
			case 66: onClosed(); break;
		}
	}

	@Override
	public void subGuiClosed(GuiScreen gui) {
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
		addTextField(new GuiNpcTextField(0, this, x, y, 168, 20, owner)
				.setHoverText("bank.hover.owner"));
		// data
		if (scroll == null) { (scroll = new GuiCustomScroll(this, 0)).setSize(168, 145); }
		scroll.setList(names);
		scroll.guiLeft = x;
		scroll.guiTop = (y += 23);
		addScroll(scroll);
		if (!sel.isEmpty()) { scroll.setSelected(sel); }
		else {
			sel = "";
			if (scroll.hasSelected()) { sel = scroll.getSelected(); }
		}
		// white / black list
		addButton(new GuiNpcCheckBox(2, x, (y += 1 + scroll.height), 82, 12, "bank.iswhite", "bank.isblack", white)
				.setHoverText("bank.hover." + (white ? "iswhite" : "isblack")));
		// changed
		addButton(new GuiNpcCheckBox(3, x + 86, y, 82, 12, "bank.changed.true", "bank.changed.false", isChanging)
				.setHoverText("bank.hover.changed." + isChanging));
		// exit
		addButton(new GuiNpcButton(66, x += 1, (y += 14), 54, 20, "gui.back")
				.setHoverText("hover.back"));
		// add
		addButton(new GuiNpcButton(0, x += 56, y, 54, 20, "gui.add")
				.setHoverText("bank.hover.player.add"));
		// del
		addButton(new GuiNpcButton(1, x + 56, y, 54, 20, "gui.remove")
				.setIsEnable(scroll.hasSelected())
				.setHoverText("bank.hover.player.del"));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		sel = scroll.getSelected();
		if (getButton(1) != null) { getButton(1).setIsEnable(scroll.hasSelected()); }
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (hasSubGui()) { return; }
		owner = textField.getText();
	}

}
