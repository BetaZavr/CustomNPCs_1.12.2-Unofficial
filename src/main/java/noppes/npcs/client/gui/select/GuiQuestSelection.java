package noppes.npcs.client.gui.select;

import java.util.ArrayList;
import java.util.HashMap;

import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;

public class GuiQuestSelection
extends SubGuiInterface
implements ICustomScrollListener {

	private final HashMap<String, QuestCategory> categoryData = new HashMap<>();
	private final HashMap<String, Quest> questData = new HashMap<>();
	private GuiSelectionListener listener;
	private GuiCustomScroll scrollCategories;
	private GuiCustomScroll scrollQuests;
	private QuestCategory selectedCategory;
	public Quest selectedQuest;

	public GuiQuestSelection(int quest) {
		drawDefaultBackground = false;
		title = "";
		setBackground("menubg.png");
		xSize = 366;
		ySize = 226;

		selectedQuest = QuestController.instance.quests.get(quest);
		if (selectedQuest != null) { selectedCategory = selectedQuest.category; }
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() == 2) {
			if (selectedQuest != null) { scrollDoubleClicked(null, null); }
			else { close(); }
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (parent instanceof GuiSelectionListener) { listener = (GuiSelectionListener) parent; }
		addLabel(new GuiNpcLabel(0, "gui.categories", guiLeft + 8, guiTop + 4));
		addLabel(new GuiNpcLabel(1, "quest.quests", guiLeft + 175, guiTop + 4));
		addButton(new GuiNpcButton(2, guiLeft + xSize - 26, guiTop + 4, 20, 20, "X"));
		categoryData.clear();
		for (QuestCategory category : QuestController.instance.categories.values()) { categoryData.put(category.title, category); }
		questData.clear();
		if (selectedCategory != null) { for (Quest quest : selectedCategory.quests.values()) { questData.put(quest.getTitle(), quest); } }
		if (scrollCategories == null) { (scrollCategories = new GuiCustomScroll(this, 0)).setSize(170, 200); }
		scrollCategories.setList(new ArrayList<>(categoryData.keySet()));
		if (selectedCategory != null) { scrollCategories.setSelected(selectedCategory.title); }
		scrollCategories.guiLeft = guiLeft + 4;
		scrollCategories.guiTop = guiTop + 14;
		addScroll(scrollCategories);
		if (scrollQuests == null) { (scrollQuests = new GuiCustomScroll(this, 1)).setSize(170, 200); }
		scrollQuests.setList(new ArrayList<>(questData.keySet()));
		if (selectedQuest != null) { scrollQuests.setSelected(selectedQuest.getTitle()); }
		scrollQuests.guiLeft = guiLeft + 175;
		scrollQuests.guiTop = guiTop + 14;
		addScroll(scrollQuests);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (scroll.getID() == 0) {
			selectedCategory = categoryData.get(scrollCategories.getSelected());
			selectedQuest = null;
			scrollQuests.setSelect(-1);
		}
		if (scroll.getID() == 1) {
			selectedQuest = questData.get(scrollQuests.getSelected());
		}
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) {
		if (selectedQuest == null) {
			return;
		}
		if (listener != null) {
			listener.selected(selectedQuest.id, selectedQuest.getTitle());
		}
		close();
	}
}
