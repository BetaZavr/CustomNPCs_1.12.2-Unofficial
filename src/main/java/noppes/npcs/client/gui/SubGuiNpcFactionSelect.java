package noppes.npcs.client.gui;

import java.util.*;
import java.util.Map.Entry;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.global.GuiNPCManageFactions;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class SubGuiNpcFactionSelect extends SubGuiInterface implements ICustomScrollListener {

	protected final String name;
	protected final HashMap<String, Integer> base;
	protected final Map<String, Integer> data = new LinkedHashMap<>();
	protected GuiCustomScroll scrollHostileFactions;
	public HashSet<Integer> selectFactions;

	public SubGuiNpcFactionSelect(int id, String nameIn, HashSet<Integer> setFactions, HashMap<String, Integer> baseIn) {
		super(id);
		background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubg.png");
		xSize = 171;
		ySize = 217;
		closeOnEsc = true;

		name = nameIn;
		base = baseIn;
		selectFactions = new HashSet<>(setFactions);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 14: {
				GuiNPCManageFactions.isName = ((GuiNpcCheckBox) button).isSelected();
				button.setHoverText("hover.sort",
						new TextComponentTranslation("global.factions").getFormattedText(),
						((GuiNpcCheckBox) button).getText());
				break;
			}
			case 66: onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		List<Entry<String, Integer>> newList = new ArrayList<>(base.entrySet());
		newList.sort((f_0, f_1) -> {
            if (GuiNPCManageFactions.isName) { return f_0.getKey().compareTo(f_1.getKey()); }
			else { return f_0.getValue().compareTo(f_1.getValue()); }
        });
		HashSet<String> set = new HashSet<>();
		data.clear();
		for (Entry<String, Integer> entry : newList) {
			int id = entry.getValue();
			String name = Util.instance.deleteColor(new TextComponentTranslation(entry.getKey()).getFormattedText());
			if (name.contains("ID:" + id + " ")) { name = name.substring(name.indexOf(" ") + 3); }
			String key = ((char) 167) + "7ID:" + id + " " + ((char) 167) + "r" + name;
			data.put(key, id);
			if (key.equals(name)) { continue; }
			if (selectFactions.contains(id)) { set.add(key); }
		}
		if (scrollHostileFactions == null) { scrollHostileFactions = new GuiCustomScroll(this, 1, true, true).setSize(163, 185); }
		scrollHostileFactions.guiLeft = guiLeft + 4;
		scrollHostileFactions.guiTop = guiTop + 28;
		addScroll(scrollHostileFactions.setUnsortedList(new ArrayList<>(data.keySet()))
				.setSelectedList(set));
		addLabel(new GuiNpcLabel(0, Util.instance.deleteColor(name), guiLeft + 4, guiTop + 4));
		addLabel(new GuiNpcLabel(1, "faction.select", guiLeft + 4, guiTop + 16));
		addButton(new GuiNpcButton(66, guiLeft + 123, guiTop + 6, 45, 20, "gui.done").setHoverText("hover.back"));
		GuiNpcCheckBox button;
		addButton(button = new GuiNpcCheckBox(14, guiLeft + 91, guiTop + 6, 30, 12, "gui.name", "ID", GuiNPCManageFactions.isName));
		button.setHoverText("hover.sort", new TextComponentTranslation("global.factions").getFormattedText(), button.getText());
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getID() == 1) {
			HashSet<Integer> set = new HashSet<>();
			List<String> list = scroll.getSelectedList();
			HashSet<String> newList = new HashSet<>();
			for (String key : data.keySet()) {
				int id = data.get(key);
				if (!list.contains(key)) { continue; }
				set.add(id);
				newList.add(key);
			}
			selectFactions = set;
			if (list.size() != newList.size()) { scroll.setSelectedList(newList); }
		}
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

}
