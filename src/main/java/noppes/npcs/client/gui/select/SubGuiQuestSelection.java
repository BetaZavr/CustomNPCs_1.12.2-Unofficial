package noppes.npcs.client.gui.select;

import java.util.ArrayList;
import java.util.HashMap;

import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;

import javax.annotation.Nonnull;

public class SubGuiQuestSelection extends SubGuiInterface implements ICustomScrollListener {

	protected final HashMap<String, QuestCategory> categoryData = new HashMap<>();
	protected final HashMap<String, Quest> questData = new HashMap<>();
	protected GuiSelectionListener listener;
	protected GuiCustomScroll scrollCategories;
	protected GuiCustomScroll scrollQuests;
	protected QuestCategory selectedCategory;
	public Quest selectedQuest;

	public SubGuiQuestSelection(int questID) {
		super(0);
		setBackground("menubg.png");
		drawDefaultBackground = false;
		title = "";
		xSize = 366;
		ySize = 226;

		selectedQuest = QuestController.instance.quests.get(questID);
		if (selectedQuest != null) { selectedCategory = selectedQuest.category; }
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		if (button.getID() == 2) {
			if (selectedQuest != null) { scrollDoubleClicked(null, null); }
			else { onClosed(); }
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
		if (scrollCategories == null) { scrollCategories = new GuiCustomScroll(this, 0).setSize(170, 200); }
		scrollCategories.setList(new ArrayList<>(categoryData.keySet()));
		if (selectedCategory != null) { scrollCategories.setSelected(selectedCategory.title); }
		scrollCategories.guiLeft = guiLeft + 4;
		scrollCategories.guiTop = guiTop + 14;
		addScroll(scrollCategories);
		if (scrollQuests == null) { scrollQuests = new GuiCustomScroll(this, 1).setSize(170, 200); }
		scrollQuests.setList(new ArrayList<>(questData.keySet()));
		if (selectedQuest != null) { scrollQuests.setSelected(selectedQuest.getTitle()); }
		scrollQuests.guiLeft = guiLeft + 175;
		scrollQuests.guiTop = guiTop + 14;
		addScroll(scrollQuests);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getID() == 0) {
			selectedCategory = categoryData.get(scrollCategories.getSelected());
			selectedQuest = null;
			scrollQuests.setSelect(-1);
		}
		if (scroll.getID() == 1) { selectedQuest = questData.get(scrollQuests.getSelected()); }
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (selectedQuest == null) { return; }
		if (listener != null) { listener.selected(selectedQuest.id, selectedQuest.getTitle()); }
		onClosed();
	}
}
