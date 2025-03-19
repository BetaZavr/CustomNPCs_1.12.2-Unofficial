package noppes.npcs.client.gui;

import java.util.*;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.Lines;
import noppes.npcs.entity.EntityNPCInterface;

public class SubGuiNPCLinesEdit
extends SubGuiInterface
implements ICustomScrollListener, ISubGuiListener, ITextfieldListener {

	public Lines lines;
	private final Map<String, Integer> data = new LinkedHashMap<>();
	private GuiCustomScroll scroll;
	private String select = "";
    private final String title;

	public SubGuiNPCLinesEdit(int id, EntityNPCInterface npc, Lines lines, String title) {
		super(npc);
		setBackground("menubg.png");
		xSize = 256;
		ySize = 217;
		closeOnEsc = true;

		this.id = id;
		this.lines = lines.copy();
		if (title == null) { title = ""; }
		this.title = title;
		Client.sendData(EnumPacketServer.MainmenuAdvancedGet);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (select.isEmpty() && scroll.hasSelected()) {
			select = scroll.getSelected();
		}
		switch (button.getID()) {
			case 0: { // add
				setSubGui(new SubGuiEditText(0, CustomNpcs.DefaultInteractLine));
				break;
			}
			case 1: { // remove
				if (!data.containsKey(select)) {
					return;
				}
				lines.remove(data.get(select));
				if (scroll != null && scroll.getSelect() > 0) {
					scroll.setSelect(scroll.getSelect() - 1);
				}
				initGui();
				break;
			}
			case 2: { // sel sound
				if (!data.containsKey(select) || !lines.lines.containsKey(data.get(select))) {
					setSubGui(new SubGuiEditText(0, CustomNpcs.DefaultInteractLine)); // add
					return;
				}
				setSubGui(new GuiSoundSelection(lines.lines.get(data.get(select)).getSound()));
				break;
			}
			case 66: {
				close();
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		data.clear();
		int p = 0;
		ITextComponent t = new TextComponentTranslation("parameter.position");
		ITextComponent m = new TextComponentTranslation("parameter.iline.text");
		ITextComponent s = new TextComponentTranslation("parameter.sound.name");
		t.getStyle().setColor(TextFormatting.GRAY);
		m.getStyle().setColor(TextFormatting.GRAY);
		s.getStyle().setColor(TextFormatting.GRAY);
		List<String> suffixes = new ArrayList<>();
		LinkedHashMap<Integer, List<String>> hts= new LinkedHashMap<>();
		for (int i : lines.lines.keySet()) {
			Line l = lines.lines.get(i);
			data.put(((char) 167) + "7" + i + ": " + ((char) 167) + "r" + l.getText(), i);
			List<String> hover = new ArrayList<>();
			hover.add(t.getFormattedText() + ((char) 167) + "7: " + ((char) 167) + "f" + i);
			hover.add(m.getFormattedText() + ((char) 167) + "7:");
			hover.add(l.getText());
			if (!l.getSound().isEmpty()) {
				hover.add(s.getFormattedText() + ((char) 167) + "7:");
				hover.add(l.getSound());
				suffixes.add(((char) 167) + "7[" + ((char) 167) + "eS" + ((char) 167) + "7]");
			} else {
				suffixes.add("");
			}
			hts.put(p, hover);
		}
		if (scroll == null) {
			(scroll = new GuiCustomScroll(this, 0)).setSize(xSize - 12, ySize - 63);
		}
		List<String> list = new ArrayList<>(data.keySet());
		scroll.setListNotSorted(list);
		scroll.guiLeft = guiLeft + 6;
		scroll.guiTop = guiTop + 14;
		Line line = null;
		if (!select.isEmpty()) {
			if (list.contains(select)) {
				line = lines.lines.get(data.get(select));
				scroll.setSelected(select);
			} else {
				select = "";
			}
		}
		scroll.setSuffixes(suffixes);
		scroll.setHoverTexts(hts);
		addScroll(scroll);
		// title
		addLabel(new GuiNpcLabel(1, title.isEmpty() ? "" : title, guiLeft, guiTop + 4));
		getLabel(1).setCenter(xSize);
		// text
		int x = guiLeft + 6;
		int y = guiTop + 170;
		GuiNpcTextField textField = new GuiNpcTextField(0, this, x, y, xSize - 86, 20, line == null ? "" : line.getText());
		textField.setHoverText("lines.hover.text");
		addTextField(textField);
		GuiNpcButton button = new GuiNpcButton(0, guiLeft + xSize - 77, y, 50, 20, "gui.add");
		button.setHoverText("lines.hover.add");
		addButton(button);
		button = new GuiNpcButton(1, guiLeft + xSize - 25, y, 20, 20, "X");
		button.setHoverText("lines.hover.remove");
		addButton(button);
		// sound
		y += 22;
		textField = new GuiNpcTextField(1, this, x + 52, y, xSize - 116, 20, line == null ? "" : line.getSound());
		textField.setHoverText("lines.hover.sound");
		addTextField(textField);
		button = new GuiNpcButton(2, guiLeft + xSize - 55, y, 50, 20, "availability.select");
		button.setHoverText("bard.hover.select");
		addButton(button);
		button = new GuiNpcButton(66, x, y, 50, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (!data.containsKey(scroll.getSelected())) {
			return;
		}
		select = scroll.getSelected();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) { }

	@Override
	public void subGuiClosed(ISubGuiInterface subgui) {
		if (subgui instanceof SubGuiEditText) {
			SubGuiEditText sub = (SubGuiEditText) subgui;
			if (sub.cancelled || sub.text[0].isEmpty()) {
				return;
			}
			Line line = new Line(sub.text[0]);
			lines.correctLines();
			int p = lines.lines.size();
			lines.lines.put(p, line);
			select = ((char) 167) + "7" + p + ": " + ((char) 167) + "r" + line.getText();
			initGui();
		} else if (subgui instanceof GuiSoundSelection) {
			if (!data.containsKey(select)) {
				return;
			}
			GuiSoundSelection sub = (GuiSoundSelection) subgui;
			if (sub.selectedResource == null) {
				return;
			}
			if (!data.containsKey(select) || !lines.lines.containsKey(data.get(select))) {
				return;
			}
			lines.lines.get(data.get(select)).setSound(sub.selectedResource.toString());
			initGui();
		}
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		if (hasSubGui()) {
			return;
		}
        if (textField.getID() == 0) {
            if (!data.containsKey(select) || !lines.lines.containsKey(data.get(select))) {
                return;
            }
            lines.lines.get(data.get(select)).setText(textField.getText());
            select = textField.getText();
            initGui();
        }
	}

}
