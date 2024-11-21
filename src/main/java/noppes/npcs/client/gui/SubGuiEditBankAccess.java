package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 0: { // add
				SubGuiEditText gui = new SubGuiEditText(3, "");
				gui.hovers = new String[] { "hover.player" };
				setSubGui(gui);
				break;
			}
			case 1: { // delete
				if (scroll.selected < 0 || scroll.selected >= names.size()) {
					return;
				}
				if (sel.equals(scroll.getSelected())) {
					sel = "";
				}
				names.remove(this.scroll.selected);
				scroll.selected--;
				if (scroll.selected < 0) {
					if (names.isEmpty()) {
						scroll.selected = -1;
					} else {
						scroll.selected = 0;
					}

				}
				initGui();
				break;
			}
			case 2: { // add
				white = ((GuiNpcCheckBox) button).isSelected();
				initGui();
				break;
			}
			case 3: {
				isChanging = ((GuiNpcCheckBox) button).isSelected();
				initGui();
				break;
			}
			case 66: {
				close();
				break;
			}
		}
	}

	@Override
	public void closeSubGui(SubGuiInterface gui) {
		super.closeSubGui(gui);
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
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions && !hasSubGui()) {
			return;
		}
		if (getButton(0) != null && getButton(0).isMouseOver()) {
			setHoverText(new TextComponentTranslation("bank.hover.player.add").getFormattedText());
		} else if (getButton(1) != null && getButton(1).isMouseOver()) {
			setHoverText(new TextComponentTranslation("bank.hover.player.del").getFormattedText());
		} else if (getButton(2) != null && getButton(2).isMouseOver()) {
			setHoverText(new TextComponentTranslation("bank.hover." + (white ? "iswhite" : "isblack")).getFormattedText());
		} else if (getButton(3) != null && getButton(3).isMouseOver()) {
			setHoverText(new TextComponentTranslation("bank.hover.changed." + isChanging).getFormattedText());
		} else if (getButton(66) != null && getButton(66).isMouseOver()) {
			setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else if (getTextField(0) != null && getTextField(0).isMouseOver()) {
			setHoverText(new TextComponentTranslation("bank.hover.owner").getFormattedText());
		}
		if (hoverText != null) {
			drawHoveringText(Arrays.asList(hoverText), mouseX, mouseY, fontRenderer);
			hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = guiLeft + 4;
		int y = guiTop + 14;

		addLabel(new GuiNpcLabel(0, new TextComponentTranslation("bank.owner").getFormattedText() + ":", x, y - 10));
		addTextField(new GuiNpcTextField(0, this, x, y, 168, 20, owner));

		if (scroll == null) {
			(scroll = new GuiCustomScroll(this, 0)).setSize(168, 145);
		}
		scroll.setList(names);
		scroll.guiLeft = x;
		scroll.guiTop = (y += 23);
		addScroll(scroll);
		if (!sel.isEmpty()) {
			scroll.setSelected(sel);
		} else {
			sel = "";
			if (scroll.getSelected() != null) {
				sel = scroll.getSelected();
			}
		}

		GuiNpcCheckBox checkBox = new GuiNpcCheckBox(2, x, (y += 1 + scroll.height), 82, 12, "bank." + (white ? "iswhite" : "isblack"));
		checkBox.setSelected(white);
		addButton(checkBox);

		checkBox = new GuiNpcCheckBox(3, x + 86, y, 82, 12, "bank.changed." + isChanging);
		checkBox.setSelected(isChanging);
		addButton(checkBox);

		addButton(new GuiNpcButton(66, x += 1, (y += 14), 54, 20, "gui.back"));
		addButton(new GuiNpcButton(0, x += 56, y, 54, 20, "gui.add"));
		addButton(new GuiNpcButton(1, x + 56, y, 54, 20, "gui.remove"));
		getButton(1).enabled = scroll.hasSelected();
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		sel = scroll.getSelected();
		if (getButton(1) != null) {
			getButton(1).enabled = scroll.hasSelected();
		}
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (hasSubGui()) {
			return;
		}
		owner = textField.getText();
	}

}
