package noppes.npcs.client.gui;

import java.util.*;
import java.util.Map.Entry;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.global.GuiNPCManageFactions;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.util.Util;

public class SubGuiNpcFactionSelect
extends SubGuiInterface
implements ICustomScrollListener {

	private final String name;
	private final HashMap<String, Integer> base;
	private final Map<String, Integer> data = new LinkedHashMap<>();
	private GuiCustomScroll scrollHostileFactions;
	public HashSet<Integer> selectFactions;

	public SubGuiNpcFactionSelect(int id, String name, HashSet<Integer> setFactions, HashMap<String, Integer> base) {
		background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubg.png");
		xSize = 171;
		ySize = 217;
		closeOnEsc = true;

		this.id = id;
		this.name = name;
		this.base = base;
		selectFactions = new HashSet<>(setFactions);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getId()) {
			case 14: {
				GuiNPCManageFactions.isName = ((GuiNpcCheckBox) button).isSelected();
				button.setHoverText("hover.sort",
						new TextComponentTranslation("global.factions").getFormattedText(),
						((GuiNpcCheckBox) button).getText());
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
		List<Entry<String, Integer>> newList = new ArrayList<>(base.entrySet());
		newList.sort((f_0, f_1) -> {
            if (GuiNPCManageFactions.isName) {
                return f_0.getKey().compareTo(f_1.getKey());
            } else {
                return f_0.getValue().compareTo(f_1.getValue());
            }
        });
		HashSet<String> set = new HashSet<>();
		data.clear();
		for (Entry<String, Integer> entry : newList) {
			int id = entry.getValue();
			String name = Util.instance.deleteColor(new TextComponentTranslation(entry.getKey()).getFormattedText());
			if (name.contains("ID:" + id + " ")) {
				name = name.substring(name.indexOf(" ") + 3);
			}
			String key = ((char) 167) + "7ID:" + id + " " + ((char) 167) + "r" + name;
			data.put(key, id);
			if (key.equals(name)) {
				continue;
			}
			if (selectFactions.contains(id)) {
				set.add(key);
			}
		}

		if (scrollHostileFactions == null) {
			(scrollHostileFactions = new GuiCustomScroll(this, 1, true)).setSize(163, 185);
		}
		scrollHostileFactions.guiLeft = guiLeft + 4;
		scrollHostileFactions.guiTop = guiTop + 28;
		scrollHostileFactions.setListNotSorted(new ArrayList<>(data.keySet()));
		scrollHostileFactions.setSelectedList(set);

		addScroll(scrollHostileFactions);
		addLabel(new GuiNpcLabel(0, Util.instance.deleteColor(name), guiLeft + 4, guiTop + 4));
		addLabel(new GuiNpcLabel(1, "faction.select", guiLeft + 4, guiTop + 16));

		GuiNpcButton button = new GuiNpcButton(66, guiLeft + 123, guiTop + 6, 45, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);

		button = new GuiNpcCheckBox(14, guiLeft + 91, guiTop + 6, 30, 12, "gui.name", "ID", GuiNPCManageFactions.isName);
		button.setHoverText("hover.sort",
				new TextComponentTranslation("global.factions").getFormattedText(),
				((GuiNpcCheckBox) button).getText());
		addButton(button);
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (scroll.getId() == 1) {
			HashSet<Integer> set = new HashSet<>();
			HashSet<String> list = scroll.getSelectedList();
			HashSet<String> newList = new HashSet<>();
			for (String key : data.keySet()) {
				int id = data.get(key);
				if (!list.contains(key)) {
					continue;
				}
				set.add(id);
				newList.add(key);
			}
			selectFactions = set;
			if (list.size() != newList.size()) {
				scroll.setSelectedList(newList);
			}
		}
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
	}

}
