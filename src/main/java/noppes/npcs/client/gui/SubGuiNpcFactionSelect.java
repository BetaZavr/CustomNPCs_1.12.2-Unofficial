package noppes.npcs.client.gui;

import java.util.*;
import java.util.Map.Entry;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.global.GuiNPCManageFactions;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.util.Util;

public class SubGuiNpcFactionSelect extends SubGuiInterface implements ICustomScrollListener {

	private final String name;
	private final HashMap<String, Integer> base;
	private final Map<String, Integer> data = new LinkedHashMap<>();
	private GuiCustomScroll scrollHostileFactions;
	public HashSet<Integer> selectFactions;

	public SubGuiNpcFactionSelect(int id, String name, HashSet<Integer> setFactions, HashMap<String, Integer> base) {
		this.background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubg.png");
		this.xSize = 171;
		this.ySize = 217;
		this.closeOnEsc = true;
		this.id = id;
		this.name = name;
		this.base = base;
		this.selectFactions = new HashSet<>(setFactions);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
		case 14: {
			GuiNPCManageFactions.isName = ((GuiNpcCheckBox) button).isSelected();
			break;
		}
		case 66: {
			this.close();
			break;
		}
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getButton(14) != null && this.getButton(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.sort",
					new TextComponentTranslation("global.factions").getFormattedText(),
					((GuiNpcCheckBox) this.getButton(14)).getText()).getFormattedText());
		} else if (this.getButton(66) != null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		List<Entry<String, Integer>> newList = new ArrayList<>(base.entrySet());
		newList.sort((f_0, f_1) -> {
            if (GuiNPCManageFactions.isName) {
                return f_0.getKey().compareTo(f_1.getKey());
            } else {
                return f_0.getValue().compareTo(f_1.getValue());
            }
        });
		HashSet<String> set = new HashSet<>();
		this.data.clear();
		for (Entry<String, Integer> entry : newList) {
			int id = entry.getValue();
			String name = Util.instance.deleteColor(new TextComponentTranslation(entry.getKey()).getFormattedText());
			if (name.contains("ID:" + id + " ")) {
				name = name.substring(name.indexOf(" ") + 3);
			}
			String key = ((char) 167) + "7ID:" + id + " " + ((char) 167) + "r" + name;
			this.data.put(key, id);
			if (key.equals(this.name)) {
				continue;
			}
			if (this.selectFactions.contains(id)) {
				set.add(key);
			}
		}

		if (this.scrollHostileFactions == null) {
			(this.scrollHostileFactions = new GuiCustomScroll(this, 1, true)).setSize(163, 185);
		}
		this.scrollHostileFactions.guiLeft = this.guiLeft + 4;
		this.scrollHostileFactions.guiTop = this.guiTop + 28;
		this.scrollHostileFactions.setListNotSorted(new ArrayList<>(data.keySet()));
		this.scrollHostileFactions.setSelectedList(set);

		this.addScroll(this.scrollHostileFactions);
		this.addLabel(new GuiNpcLabel(0, Util.instance.deleteColor(this.name), this.guiLeft + 4, this.guiTop + 4));
		this.addLabel(new GuiNpcLabel(1, "faction.select", this.guiLeft + 4, this.guiTop + 16));

		this.addButton(new GuiNpcButton(66, this.guiLeft + 123, this.guiTop + 6, 45, 20, "gui.done"));

		addButton(new GuiNpcCheckBox(14, this.guiLeft + 91, this.guiTop + 6, 30, 12, "gui.name", "ID", GuiNPCManageFactions.isName));
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.id == 1) {
			HashSet<Integer> set = new HashSet<>();
			HashSet<String> list = scroll.getSelectedList();
			HashSet<String> newList = new HashSet<>();
			for (String key : this.data.keySet()) {
				int id = this.data.get(key);
				if (!list.contains(key)) {
					continue;
				}
				set.add(id);
				newList.add(key);
			}
			this.selectFactions = set;
			if (list.size() != newList.size()) {
				scroll.setSelectedList(newList);
			}
		}
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
	}

}
