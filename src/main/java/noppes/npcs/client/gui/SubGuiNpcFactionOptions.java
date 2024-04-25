package noppes.npcs.client.gui;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
import noppes.npcs.util.AdditionalMethods;

public class SubGuiNpcFactionOptions extends SubGuiInterface
		implements IScrollData, ICustomScrollListener, ITextfieldListener {

	private Map<String, Integer> data;
	private FactionOptions options;
	private GuiCustomScroll scroll;

	public SubGuiNpcFactionOptions(FactionOptions options) {
		this.data = new HashMap<String, Integer>();
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
		FactionOption fo = null;
		int id = -1;
		if (this.scroll.getSelected() != null && this.data.containsKey(this.scroll.getSelected())) {
			id = this.data.get(this.scroll.getSelected());
			fo = this.options.get(id);
		}
		if (fo == null) {
			if (value == 0) {
				return;
			}
			fo = new FactionOption(this.data.get(this.scroll.getSelected()), value, isTake);
			this.options.fps.add(fo);
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
		if (fo != null) {
			fo.cheak();
		}
		this.setData(null, (HashMap<String, Integer>) this.data);
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
		Client.sendData(EnumPacketServer.FactionsGet, new Object[0]);
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		String name = AdditionalMethods.instance.deleteColor(this.scroll.getSelected());
		if (name != null && name.indexOf("ID:") >= 0 && name.indexOf(" - ") >= name.indexOf("ID:")) {
			name = name.substring(name.indexOf(" - ") + 3);
		}
		List<String> newList = Lists.<String>newArrayList();
		Map<String, String> hoverMap = Maps.<String, String>newHashMap();
		Map<String, Integer> newData = Maps.<String, Integer>newHashMap();
		for (String key : data.keySet()) {
			int id = data.get(key);
			String newName = AdditionalMethods.instance.deleteColor(key);
			if (newName.indexOf("ID:" + id + " - ") >= 0) {
				newName = newName.substring(newName.indexOf(" - ") + 3);
			}
			String str = ((char) 167) + "7ID:" + id + " - " + newName;
			if (this.options.hasFaction(id)) {
				FactionOption fo = this.options.get(id);
				str = ((char) 167) + "7ID:" + id + " - " + ((char) 167) + (fo.decreaseFactionPoints ? "c" : "2")
						+ newName;
			}
			newList.add(str);
			hoverMap.put(str, newName);
			newData.put(str, id);
			if (name != null && name.equals(newName)) {
				name = str;
			}
		}
		Collections.sort(newList);
		this.data = newData;
		this.scroll.setListNotSorted(newList);
		this.scroll.hoversTexts = new String[hoverMap.size()][];
		int i = 0;
		for (String key : newList) {
			this.scroll.hoversTexts[i] = new String[] { hoverMap.get(key) };
			i++;
		}

		if (name != null) {
			this.scroll.setSelected(name);
		}
		this.initGui();
	}

	@Override
	public void setSelected(String selected) {
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		this.change(this.getButton(1) == null ? false : this.getButton(1).getValue() == 1, textField.getInteger());
	}

}
