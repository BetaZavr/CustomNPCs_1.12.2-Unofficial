package noppes.npcs.client.gui;

import java.util.*;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.select.SubGuiSoundSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.Lines;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class SubGuiNPCLinesEdit extends SubGuiInterface
		implements ICustomScrollListener, ITextfieldListener {

	protected final Map<String, Integer> data = new LinkedHashMap<>();
	protected GuiCustomScroll scroll;
	protected String select = "";
	protected final String title;
	public Lines lines;

	public SubGuiNPCLinesEdit(int id, EntityNPCInterface npc, Lines linesIn, String titleIn) {
		super(id, npc);
		setBackground("menubg.png");
		xSize = 256;
		ySize = 217;
		closeOnEsc = true;

		lines = linesIn.copy();
		title = titleIn == null ? "" : titleIn;
		Client.sendData(EnumPacketServer.MainmenuAdvancedGet);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		if (select.isEmpty() && scroll.hasSelected()) { select = scroll.getSelected(); }
		switch (button.getID()) {
			case 0: setSubGui(new SubGuiEditText(0, CustomNpcs.DefaultInteractLine)); break; // add
			case 1: {
				if (!data.containsKey(select)) { return; }
				lines.remove(data.get(select));
				if (scroll != null && scroll.hasSelected()) { scroll.setSelect(scroll.getSelect() - 1); }
				initGui();
				break;
			} // remove
			case 2: {
				if (!data.containsKey(select) || !lines.lines.containsKey(data.get(select))) {
					setSubGui(new SubGuiEditText(0, CustomNpcs.DefaultInteractLine)); // add
					return;
				}
				setSubGui(new SubGuiSoundSelection(lines.lines.get(data.get(select)).getSound()));
				break;
			} // sel sound
			case 66: onClosed(); break;
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
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(xSize - 12, ySize - 63); }
		List<String> list = new ArrayList<>(data.keySet());
		scroll.setUnsortedList(list);
		scroll.guiLeft = guiLeft + 6;
		scroll.guiTop = guiTop + 14;
		Line line = null;
		if (!select.isEmpty()) {
			if (list.contains(select)) {
				line = lines.lines.get(data.get(select));
				scroll.setSelected(select);
			} else { select = ""; }
		}
		scroll.setSuffixes(suffixes).setHoverTexts(hts);
		addScroll(scroll);
		// title
		addLabel(new GuiNpcLabel(1, title.isEmpty() ? "" : title, guiLeft, guiTop + 4));
		getLabel(1).setCenter(xSize);
		// text
		int x = guiLeft + 6;
		int y = guiTop + 170;
		addTextField(new GuiNpcTextField(0, this, x, y, xSize - 86, 20, line == null ? "" : line.getText())
				.setHoverText("lines.hover.text"));
		addButton(new GuiNpcButton(0, guiLeft + xSize - 77, y, 50, 20, "gui.add")
				.setHoverText("lines.hover.add"));
		addButton(new GuiNpcButton(1, guiLeft + xSize - 25, y, 20, 20, "X")
				.setHoverText("lines.hover.remove"));
		// sound
		addTextField(new GuiNpcTextField(1, this, x + 52, y += 22, xSize - 116, 20, line == null ? "" : line.getSound())
				.setHoverText("lines.hover.sound"));
		addButton(new GuiNpcButton(2, guiLeft + xSize - 55, y, 50, 20, "availability.select")
				.setHoverText("bard.hover.select"));
		addButton(new GuiNpcButton(66, x, y, 50, 20, "gui.done")
				.setHoverText("hover.back"));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (!data.containsKey(scroll.getSelected())) { return; }
		select = scroll.getSelected();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

	@Override
	public void subGuiClosed(GuiScreen subgui) {
		if (subgui instanceof SubGuiEditText) {
			SubGuiEditText sub = (SubGuiEditText) subgui;
			if (sub.cancelled || sub.text[0].isEmpty()) { return; }
			Line line = new Line(sub.text[0]);
			lines.correctLines();
			int p = lines.lines.size();
			lines.lines.put(p, line);
			select = ((char) 167) + "7" + p + ": " + ((char) 167) + "r" + line.getText();
			initGui();
		}
		else if (subgui instanceof SubGuiSoundSelection) {
			if (!data.containsKey(select)) { return; }
			SubGuiSoundSelection sub = (SubGuiSoundSelection) subgui;
			if (sub.selectedResource == null || !data.containsKey(select) || !lines.lines.containsKey(data.get(select))) { return; }
			lines.lines.get(data.get(select)).setSound(sub.selectedResource.toString());
			initGui();
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (hasSubGui()) { return; }
        if (textField.getID() == 0) {
            if (!data.containsKey(select) || !lines.lines.containsKey(data.get(select))) { return; }
            lines.lines.get(data.get(select)).setText(textField.getText());
            select = textField.getText();
            initGui();
        }
	}

}
