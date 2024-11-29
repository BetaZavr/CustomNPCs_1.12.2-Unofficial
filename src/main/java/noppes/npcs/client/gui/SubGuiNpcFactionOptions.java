package noppes.npcs.client.gui;

import java.util.*;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.FactionOption;
import noppes.npcs.controllers.data.FactionOptions;
import noppes.npcs.util.Util;

public class SubGuiNpcFactionOptions extends SubGuiInterface implements IScrollData, ICustomScrollListener, ITextfieldListener {

	private final Map<String, Integer> data = new HashMap<>();
	private final FactionOptions options;
	private GuiCustomScroll scroll;

	public SubGuiNpcFactionOptions(FactionOptions options) {
		this.options = options;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
		this.closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 1) {
			this.change(button.getValue() == 1, this.getTextField(1) == null ? 0 : this.getTextField(1).getInteger());
		}
		if (button.id == 66) {
			this.close();
		}
	}

	private void change(boolean isTake, int value) {
		if (!scroll.hasSelected() || !data.containsKey(this.scroll.getSelected())) { return; }
		FactionOption fo = null;
		int id = -1;
		if (this.scroll.getSelected() != null && this.data.containsKey(this.scroll.getSelected())) {
			id = data.get(this.scroll.getSelected());
			fo = options.get(id);
		}
		if (fo == null) {
			if (value == 0) {
				return;
			}
			fo = new FactionOption(id, value, isTake);
			options.fps.add(fo);
		} else {
			if (value == 0) {
				if (this.options.remove(id)) {
					fo = null;
				}
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
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(120, 196);
		}
		this.scroll.guiLeft = this.guiLeft + 4;
		this.scroll.guiTop = this.guiTop + 14;
		this.addScroll(this.scroll);
		this.addLabel(new GuiNpcLabel(1, new TextComponentTranslation("faction.options").getFormattedText() + ":",
				this.guiLeft + 5, this.guiTop + 4));

		FactionOption fo = null;
		if (this.scroll.getSelected() != null && this.data.get(this.scroll.getSelected()) != null) {
			fo = this.options.get(this.data.get(this.scroll.getSelected()));
		}
		GuiNpcLabel lable = new GuiNpcLabel(2, new TextComponentTranslation("gui.settings").getFormattedText() + ":",
				this.guiLeft + 130, this.guiTop + 4);
		lable.enabled = this.scroll.selected >= 0;
		this.addLabel(lable);
		GuiNpcTextField textField = new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 130,
				this.guiTop + 16, 110, 20, fo != null ? "" + fo.factionPoints : "0");
		textField.setNumbersOnly();
		textField.setMinMaxDefault(-100000, 100000, fo != null ? fo.factionPoints : 0);
		textField.enabled = this.scroll.selected >= 0;
		this.addTextField(textField);

		GuiNpcButton button = new GuiNpcButton(1, this.guiLeft + 130, this.guiTop + 38, 90, 20,
				new String[] { "gui.add", "gui.decrease" }, fo != null ? fo.decreaseFactionPoints ? 1 : 0 : 0);
		button.visible = this.scroll.selected >= 0;
		this.addButton(button);

		this.addButton(new GuiNpcButton(66, this.guiLeft + 130, this.guiTop + this.ySize - 26, 90, 20, "gui.back"));
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.FactionsGet);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		this.data.clear();
		String name = Util.instance.deleteColor(this.scroll.getSelected());
		if (name != null && name.contains("ID:") && name.indexOf(" - ") >= name.indexOf("ID:")) {
			name = name.substring(name.indexOf(" - ") + 3);
		}
		List<String> newList = new ArrayList<>();
		Map<String, String> hoverMap = new HashMap<>();
		Map<String, Integer> newData = new HashMap<>();
		for (String key : data.keySet()) {
			int id = data.get(key);
			String newName = Util.instance.deleteColor(key);
			if (newName.contains("ID:" + id + " - ")) {
				newName = newName.substring(newName.indexOf(" - ") + 3);
			}
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
		this.data.putAll(newData);
		this.scroll.setListNotSorted(newList);
		this.scroll.hoversTexts = new String[hoverMap.size()][];
		int i = 0;
		for (String key : newList) {
			this.scroll.hoversTexts[i] = new String[] { hoverMap.get(key) };
			i++;
		}
		if (name != null) { scroll.setSelected(name); }
		initGui();
	}

	@Override
	public void setSelected(String selected) {
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		this.change(this.getButton(1) != null && this.getButton(1).getValue() == 1, textField.getInteger());
	}

}
