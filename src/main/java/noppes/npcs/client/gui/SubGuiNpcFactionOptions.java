package noppes.npcs.client.gui;

import java.util.*;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.FactionOption;
import noppes.npcs.controllers.data.FactionOptions;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class SubGuiNpcFactionOptions extends SubGuiInterface
		implements IScrollData, ICustomScrollListener, ITextfieldListener {

	protected final Map<String, Integer> data = new HashMap<>();
	protected final FactionOptions options;
	protected GuiCustomScroll scroll;

	public SubGuiNpcFactionOptions(FactionOptions factionOptions) {
		super(0);
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;

		options = factionOptions;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 1 : change(button.getValue() == 1, getTextField(1) == null ? 0 : getTextField(1).getInteger()); break;
			case 66 : onClosed(); break;
		}
	}

	private void change(boolean isTake, int value) {
		if (!scroll.hasSelected() || !data.containsKey(scroll.getSelected())) { return; }
		FactionOption fo = null;
		int id = -1;
		if (scroll.getSelected() != null && data.containsKey(scroll.getSelected())) {
			id = data.get(scroll.getSelected());
			fo = options.get(id);
		}
		if (fo == null) {
			if (value == 0) { return; }
			fo = new FactionOption(id, value, isTake);
			options.fps.add(fo);
		} else {
			if (value == 0) {
				if (options.remove(id)) { fo = null; }
			} else {
				fo.factionPoints = value;
				fo.decreaseFactionPoints = isTake;
			}
		}
		if (fo != null) { fo.check(); }
		setData(null, new HashMap<>(data));
	}

	@Override
	public void initGui() {
		super.initGui();
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(120, 196); }
		scroll.guiLeft = guiLeft + 4;
		scroll.guiTop = guiTop + 14;
		addScroll(scroll);

		addLabel(new GuiNpcLabel(1, new TextComponentTranslation("faction.options").getFormattedText() + ":", guiLeft + 5, guiTop + 4));

		FactionOption fo = null;
		if (scroll.getSelected() != null && data.get(scroll.getSelected()) != null) { fo = options.get(data.get(scroll.getSelected())); }
		GuiNpcLabel label = new GuiNpcLabel(2, new TextComponentTranslation("gui.settings").getFormattedText() + ":", guiLeft + 130, guiTop + 4);
		label.setIsEnable(scroll.getSelect() >= 0);
		addLabel(label);
		// faction points
		addTextField(new GuiNpcTextField(1, this, guiLeft + 130, guiTop + 16, 110, 20, fo != null ? "" + fo.factionPoints : "0")
				.setMinMaxDefault(-100000, 100000, fo != null ? fo.factionPoints : 0)
				.setIsEnable(scroll.getSelect() >= 0)
				.setHoverText("faction.hover.option.points"));

		addButton(new GuiNpcButton(1, guiLeft + 130, guiTop + 38, 90, 20, new String[] { "gui.add", "gui.decrease" }, fo != null ? fo.decreaseFactionPoints ? 1 : 0 : 0)
				.setIsVisible(scroll.getSelect() >= 0)
				.setHoverText("faction.hover.option.decrease"));

		addButton(new GuiNpcButton(66, guiLeft + 130, guiTop + ySize - 26, 90, 20, "gui.back")
				.setHoverText("hover.back"));
	}

	@Override
	public void initPacket() { Client.sendData(EnumPacketServer.FactionsGet); }

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) { initGui(); }

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { }

	@Override
	public void setData(Vector<String> dataList, HashMap<String, Integer> dataMap) {
		data.clear();
		String name = Util.instance.deleteColor(scroll.getSelected());
		if (name != null && name.contains("ID:") && name.indexOf(" - ") >= name.indexOf("ID:")) { name = name.substring(name.indexOf(" - ") + 3); }
		List<String> newList = new ArrayList<>();
		Map<String, String> hoverMap = new HashMap<>();
		Map<String, Integer> newData = new HashMap<>();
		for (String key : dataMap.keySet()) {
			int id = dataMap.get(key);
			String newName = Util.instance.deleteColor(key);
			if (newName.contains("ID:" + id + " - ")) { newName = newName.substring(newName.indexOf(" - ") + 3); }
			newName = new TextComponentTranslation(newName).getFormattedText();
			String str = ((char) 167) + "7ID:" + id + " - " + newName;
			if (options.hasFaction(id)) {
				FactionOption fo = options.get(id);
				str = ((char) 167) + "7ID:" + id + " - " + ((char) 167) + (fo.decreaseFactionPoints ? "c" : "2") + newName;
			}
			newList.add(str);
			hoverMap.put(str, newName);
			newData.put(str, id);
			if (name != null && name.equals(Util.instance.deleteColor(newName))) { name = str; }
		}
		Collections.sort(newList);
		data.putAll(newData);
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		int i = 0;
		for (String key : newList) {
			hts.put(i, Collections.singletonList(hoverMap.get(key)));
			i++;
		}
		scroll.setUnsortedList(newList).setHoverTexts(hts);
		if (name != null) { scroll.setSelected(name); }
		initGui();
	}

	@Override
	public void setSelected(String selected) { }

	@Override
	public void unFocused(GuiNpcTextField textField) {
		change(getButton(1) != null && getButton(1).getValue() == 1, textField.getInteger());
	}

}
