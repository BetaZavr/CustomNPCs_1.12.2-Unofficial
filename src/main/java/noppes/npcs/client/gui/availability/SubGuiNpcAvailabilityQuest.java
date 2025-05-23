package noppes.npcs.client.gui.availability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.client.gui.select.GuiQuestSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumAvailabilityQuest;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.Quest;

public class SubGuiNpcAvailabilityQuest
extends SubGuiInterface
implements ICustomScrollListener, GuiSelectionListener {

	protected static final String[] types = new String[] { "availability.always", "availability.after", "availability.before", "availability.active", "availability.notactive", "availability.completed", "availability.canstart" };
	protected final Availability availability;
	protected final Map<String, EnumAvailabilityQuest> dataEnum = new HashMap<>();
	protected final Map<String, Integer> dataIDs = new HashMap<>();
	protected GuiCustomScroll scroll;
	protected String select = "";

	public SubGuiNpcAvailabilityQuest(Availability availabilityIn) {
		setBackground("menubg.png");
		xSize = 316;
		ySize = 217;
		closeOnEsc = true;

		availability = availabilityIn;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0 : {
				if (select.isEmpty()) { return; }
				EnumAvailabilityQuest ead = EnumAvailabilityQuest.values()[button.getValue()];
				int id = dataIDs.get(select);
				availability.quests.put(id, ead);
				Quest quest = QuestController.instance.quests.get(dataIDs.get(select));
				select = "ID:" + id + " - ";
				if (quest == null) {
					select += ((char) 167) + "4" + (new TextComponentTranslation("quest.found").getFormattedText());
				} else {
					select += ((char) 167) + "7" + quest.getCategory().getName() + "/" + ((char) 167) + "r" + quest.getName() + ((char) 167) + "7 (" + ((char) 167) + "9" + new TextComponentTranslation(("availability." + ead).toLowerCase()).getFormattedText() + ((char) 167) + "7)";
				}
				initGui();
				break;
			}
			case 1 : {
				setSubGui(new GuiQuestSelection(select.isEmpty() ? 0 : dataIDs.get(select)));
				break;
			}
			case 2 : {
				availability.quests.remove(dataIDs.get(select));
				select = "";
				initGui();
				break;
			}
			case 3 : { // More
				save();
				initGui();
				break;
			}
			case 66 : close(); break;
		}
	}

	@Override
	public void close() {
		super.close();
		List<Integer> delete = new ArrayList<>();
		for (int id : availability.quests.keySet()) {
			if (availability.quests.get(id) == EnumAvailabilityQuest.Always) {
				delete.add(id);
			}
		}
		for (int id : delete) {
			availability.quests.remove(id);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// title
		addLabel(new GuiNpcLabel(1, "availability.available", guiLeft, guiTop + 4));
		getLabel(1).setCenter(xSize);
		// exit
		GuiNpcButton button = new GuiNpcButton(66, guiLeft + 6, guiTop + 192, 70, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
		// data
		if (scroll == null) { (scroll = new GuiCustomScroll(this, 6)).setSize(xSize - 12, ySize - 66); }
		dataIDs.clear();
		dataEnum.clear();
		for (int id : availability.quests.keySet()) {
			String key = "ID:" + id + " - ";
			IQuest q = QuestController.instance.get(id);
			if (q == null) {
				key += ((char) 167) + "4" + (new TextComponentTranslation("quest.notfound").getFormattedText());
			} else {
				key += ((char) 167) + "7" + q.getCategory().getName() + "/" + ((char) 167) + "r" + q.getName() + ((char) 167) + "7 (" + ((char) 167) + "9" + new TextComponentTranslation(("availability." + availability.quests.get(id)).toLowerCase()).getFormattedText() + ((char) 167) + "7)";
			}
			dataIDs.put(key, id);
			dataEnum.put(key, availability.quests.get(id));
		}
		if (!select.isEmpty() && !dataIDs.containsKey(select)) { select = ""; }
		scroll.setList(new ArrayList<>(dataIDs.keySet()));
		scroll.guiLeft = guiLeft + 6;
		scroll.guiTop = guiTop + 14;
		if (!select.isEmpty()) { scroll.setSelected(select); }
		addScroll(scroll);
		int p = 0;
		if (!select.isEmpty()) { p = dataEnum.get(select).ordinal(); }
		// type
		button = new GuiNpcButton(0, guiLeft + 6, guiTop + ySize - 46, 100, 20, types, p);
		button.setHoverText("availability.hover.enum.type");
		addButton(button);
		// select
		button = new GuiNpcButton(1, guiLeft + 108, guiTop + ySize - 46, 180, 20, "availability.select");
		button.setHoverText("availability.hover.quest");
		addButton(button);
		// del
		button = new GuiNpcButton(2, guiLeft + 290, guiTop + ySize - 46, 20, 20, "X");
		button.setHoverText("availability.hover.remove");
		addButton(button);
		// extra
		button = new GuiNpcButton(3, guiLeft + xSize - 76, guiTop + 192, 70, 20, "availability.more");
		button.setEnabled(!select.isEmpty());
		button.setHoverText("availability.hover.more");
		addButton(button);
		updateGuiButtons();
	}

	@Override
	public void save() {
		if (select.isEmpty()) {
			return;
		}
		EnumAvailabilityQuest ead = EnumAvailabilityQuest.values()[getButton(0).getValue()];
		int id = dataIDs.get(select);
		if (ead != EnumAvailabilityQuest.Always) {
			availability.quests.put(id, ead);
			dataEnum.put(select, ead);
		} else {
			availability.quests.remove(id);
		}
		select = "";
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		select = scroll.getSelected();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
		setSubGui(new GuiQuestSelection(dataIDs.get(select)));
	}

	@Override
	public void selected(int id, String name) {
		if (id <= 0) {
			return;
		}
		if (!select.isEmpty()) {
			availability.quests.remove(dataIDs.get(select));
		}
		Quest quest = QuestController.instance.quests.get(id);
		select = "ID:" + id + " - " + ((char) 167) + "7" + quest.category.getName() + "/" + ((char) 167) + "r" + quest.getName() + ((char) 167) + "7 (" + ((char) 167) + "9" + new TextComponentTranslation("availability.after").getFormattedText() + ((char) 167) + "7)";
		availability.quests.put(id, EnumAvailabilityQuest.After);
		initGui();
	}

	private void updateGuiButtons() {
		int p = 0;
		getButton(1).setDisplayText("availability.selectquest");
		Quest quest = null;
		if (!select.isEmpty()) {
			quest = QuestController.instance.quests.get(dataIDs.get(select));
			p = dataEnum.get(select).ordinal();
		}
		getButton(0).setDisplay(p);
		getButton(0).setEnabled(!select.isEmpty());
		getButton(1).setEnabled(p != 0 || select.isEmpty());
		getButton(1).setDisplayText(quest == null ? "availability.select" : quest.getName());
		getButton(2).setEnabled(p != 0);
	}

}
