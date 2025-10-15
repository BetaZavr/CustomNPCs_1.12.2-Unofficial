package noppes.npcs.client.gui.global;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class GuiNPCManageQuest extends GuiNPCInterface2 implements ICustomScrollListener, GuiYesNoCallback {

	protected final TreeMap<String, QuestCategory> categoryData = new TreeMap<>();
	protected final Map<String, Quest> questData = new LinkedHashMap<>();
	protected GuiCustomScroll scrollCategories;
	protected GuiCustomScroll scrollQuests;
	public static GuiScreen Instance;
	// New from Unofficial (BetaZavr)
	protected static boolean isName = true;
	protected String selectedCategory = "";
	protected String selectedQuest = "";
	protected Quest copyQuest = null;

	public GuiNPCManageQuest(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuGlobal;

		GuiNPCManageQuest.Instance = this;
		Client.sendData(EnumPacketServer.QuestCategoryGet);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 1: {
				setSubGui(new SubGuiEditText(1, Util.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 2: {
				if (!categoryData.containsKey(selectedCategory)) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this,
						categoryData.get(selectedCategory).title,
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 2);
				displayGuiScreen(guiyesno);
				break;
			}
			case 3: {
				if (!categoryData.containsKey(selectedCategory)) { return; }
				setSubGui(new SubGuiEditText(3, categoryData.get(selectedCategory).title));
				break;
			}
			case 9: {
				if (copyQuest == null || !categoryData.containsKey(selectedCategory)) { return; }
				Quest quest = copyQuest.copy();
				quest.id = -1;
				quest.category = categoryData.get(selectedCategory);
				StringBuilder t = new StringBuilder(quest.title);
				boolean has = true;
				while (has) {
					has = false;
					for (Quest q : quest.category.quests.values()) {
						if (quest.id != q.id && q.title.equalsIgnoreCase(t.toString())) {
							has = true;
							break;
						}
					}
					if (has) { t.append("_"); }
				}
				quest.setName(t.toString());
				selectedQuest = getKey(quest);
				Client.sendData(EnumPacketServer.QuestSave, quest.category.id, quest.save(new NBTTagCompound()));
				initGui();
				break;
			} // paste
			case 10: {
				if (!questData.containsKey(selectedQuest)) { return; }
				copyQuest = questData.get(selectedQuest);
				initGui();
				break;
			} // copy
			case 11: {
				setSubGui(new SubGuiEditText(11, Util.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 12: {
				if (!questData.containsKey(selectedQuest)) { return; }
				GuiYesNo guiyesno = new GuiYesNo(this, questData.get(selectedQuest).getTitle(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 12);
				displayGuiScreen(guiyesno);
				break;
			}
			case 13: {
				if (!questData.containsKey(selectedQuest)) { return; }
				setSubGui(new SubGuiQuestEdit(questData.get(selectedQuest)));
				break;
			}
			case 14: {
				GuiNPCManageQuest.isName = ((GuiNpcCheckBox) button).isSelected();
				button.setHoverText("hover.sort", new TextComponentTranslation("dialog.dialogs").getFormattedText(), ((GuiNpcCheckBox) button).getText());
				break;
			}
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI(player, this);
		if (!result) { return; }
		if (id == 2) {
			Client.sendData(EnumPacketServer.QuestCategoryRemove, categoryData.get(selectedCategory).id);
			selectedCategory = "";
			selectedQuest = "";
		}
		if (id == 12) {
			Client.sendData(EnumPacketServer.QuestRemove, questData.get(selectedQuest).id);
			selectedQuest = "";
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (hasSubGui()) { return; }
		drawHorizontalLine(guiLeft + 348, guiLeft + 414, guiTop + 128, new Color(0x80000000).getRGB());
	}

	@Override
	public void initGui() {
		super.initGui();
		categoryData.clear();
		questData.clear();
		QuestController qData = QuestController.instance;
		LinkedHashMap<Integer, List<String>> hts= new LinkedHashMap<>();
		for (QuestCategory category : qData.categories.values()) {
			categoryData.put(category.title, category);
			if (selectedCategory.isEmpty()) { selectedCategory = category.title; }
		}
		if (!selectedCategory.isEmpty()) {
			if (categoryData.containsKey(selectedCategory)) {
				Map<String, Quest> map = new TreeMap<>();
				for (Quest quest : categoryData.get(selectedCategory).quests.values()) { map.put(getKey(quest), quest); }
				List<Entry<String, Quest>> list = getEntryList(map);
				for (Entry<String, Quest> entry : list) {
					questData.put(entry.getKey(), entry.getValue());
					if (selectedQuest.isEmpty()) { selectedQuest = entry.getKey(); }
				}
			} else {
				selectedCategory = "";
				selectedQuest = "";
			}
			// Hover Text:
			if (!questData.isEmpty()) {
				int pos = 0;
				DialogController dData = DialogController.instance;
				for (Quest quest : questData.values()) { hts.put(pos++, getStrings(quest, qData, dData)); }
			}
		}
		if (!selectedCategory.isEmpty() && !categoryData.containsKey(selectedCategory)) { selectedCategory = ""; }
		if (!selectedQuest.isEmpty() && !questData.containsKey(selectedQuest)) { selectedQuest = ""; }
		// scroll info
		addLabel(new GuiNpcLabel(0, "gui.categories", guiLeft + 8, guiTop + 4));
		addLabel(new GuiNpcLabel(1, "quest.quests", guiLeft + 180, guiTop + 4));
		// quest buttons
		int x = guiLeft + 350, y = guiTop + 8;
		addLabel(new GuiNpcLabel(3, "quest.quests", guiLeft + 356, guiTop + 8));
		addButton(new GuiNpcButton(13, x, y += 10, 64, 15, "selectServer.edit", !selectedQuest.isEmpty())
				.setHoverText("manager.hover.quest.edit", selectedQuest));
		addButton(new GuiNpcButton(12, x, y += 17, 64, 15, "gui.remove", !selectedQuest.isEmpty())
				.setHoverText("manager.hover.quest.del", selectedQuest));
		addButton(new GuiNpcButton(11, x, y += 17, 64, 15, "gui.add", !selectedCategory.isEmpty())
				.setHoverText("manager.hover.quest.add", selectedCategory));
		addButton(new GuiNpcButton(10, x, y += 21, 64, 15, "gui.copy", !selectedCategory.isEmpty())
				.setIsEnable(!selectedQuest.isEmpty())
				.setHoverText("manager.hover.quest.copy", selectedQuest));
		addButton(new GuiNpcButton(9, x, y += 17, 64, 15, "gui.paste", copyQuest != null)
				.setHoverText("manager.hover.quest.paste." + (copyQuest != null), copyQuest != null ? copyQuest.getKey() : ""));
		addButton(new GuiNpcCheckBox(14, x, y + 17, 64, 14, "gui.name", "ID", GuiNPCManageQuest.isName)
				.setHoverText("hover.sort", new TextComponentTranslation("dialog.dialogs").getFormattedText(), GuiNPCManageQuest.isName ? new TextComponentTranslation("gui.name").getFormattedText() : "ID"));
		// category buttons
		y = guiTop + 134;
		addLabel(new GuiNpcLabel(2, "gui.categories", x + 2, y));
		addButton(new GuiNpcButton(3, x, y += 10, 64, 15, "selectServer.edit", !selectedCategory.isEmpty())
				.setHoverText("manager.hover.category.edit"));
		addButton(new GuiNpcButton(2, x, y += 17, 64, 15, "gui.remove", !selectedCategory.isEmpty())
				.setHoverText("manager.hover.category.del"));
		addButton(new GuiNpcButton(1, x, y + 17, 64, 15, "gui.add")
				.setHoverText("manager.hover.category.add"));
		if (scrollCategories == null) { scrollCategories = new GuiCustomScroll(this, 0).setSize(170, ySize - 3); }
		scrollCategories.setList(new ArrayList<>(categoryData.keySet()));
		scrollCategories.guiLeft = guiLeft + 4;
		scrollCategories.guiTop = guiTop + 15;
		if (!selectedCategory.isEmpty()) { scrollCategories.setSelected(selectedCategory); }
		addScroll(scrollCategories);
		if (scrollQuests == null) { scrollQuests = new GuiCustomScroll(this, 1).setSize(170, ySize - 3); }
		scrollQuests.setUnsortedList(new ArrayList<>(questData.keySet()));
		scrollQuests.setHoverTexts(hts);
		scrollQuests.guiLeft = guiLeft + 176;
		scrollQuests.guiTop = guiTop + 15;
		if (!selectedQuest.isEmpty()) { scrollQuests.setSelected(selectedQuest); }
		addScroll(scrollQuests);
	}

	private static List<Entry<String, Quest>> getEntryList(Map<String, Quest> map) {
		List<Entry<String, Quest>> list = new ArrayList<>(map.entrySet());
		list.sort((d_0, d_1) -> {
			if (GuiNPCManageQuest.isName) {
			String n_0 = Util.instance.deleteColor(new TextComponentTranslation(d_0.getValue().title).getFormattedText() + "_" + d_0.getValue().id).toLowerCase();
			String n_1 = Util.instance.deleteColor(new TextComponentTranslation(d_1.getValue().title).getFormattedText() + "_" + d_1.getValue().id).toLowerCase();
			return n_0.compareTo(n_1);
			} else {
			return Integer.compare(d_0.getValue().id, d_1.getValue().id);
			}
		});
		return list;
	}

	private String getKey(Quest quest) {
		boolean b = quest.isSetUp();
		return ((char) 167) + "7ID:" + quest.id + "-\"" + ((char) 167) + "r" + quest.getTitle() + ((char) 167) + "7\"" + ((char) 167)
				+ (b ? "2 (" : "c (") + (new TextComponentTranslation("quest.has." + b).getFormattedText())
				+ ((char) 167) + (b ? "2)" : "c)");
	}

	private List<String> getStrings(Quest quest, QuestController qData, DialogController dData) {
		List<String> h = new ArrayList<>();
		List<String> quests = new ArrayList<>();
		List<String> dialogs = new ArrayList<>();
		h.add(new TextComponentTranslation(quest.title).getFormattedText() + ":");
		for (Quest q : qData.quests.values()) {
			if (q.nextQuest != quest.id) { continue; }
			quests.add(((char) 167) + "7ID:" + q.id + ((char) 167) + "8 " + q.category.getName() + "/" + ((char) 167) + "r" + q.getName());
		}
		for (Dialog d : dData.dialogs.values()) {
			if (d.quest != quest.id) { continue; }
			dialogs.add(((char) 167) + "7ID:" + d.id + ((char) 167) + "8 " + d.category.getName() + "/" + ((char) 167) + "r" + d.getName());
		}
		if (quests.isEmpty() && dialogs.isEmpty()) { h.add(new TextComponentTranslation("quest.hover.quest.0").getFormattedText()); }
		if (!quests.isEmpty()) {
			h.add(new TextComponentTranslation("quest.hover.in.quests").getFormattedText());
			h.addAll(quests);
		}
		else { h.add(new TextComponentTranslation("quest.hover.quest.1").getFormattedText()); }
		if (!dialogs.isEmpty()) {
			h.add(new TextComponentTranslation("quest.hover.in.dialogs").getFormattedText());
			h.addAll(dialogs);
		}
		else { h.add(new TextComponentTranslation("quest.hover.quest.2").getFormattedText()); }
		return h;
	}


	@Override
	public void save() {
		GuiNpcTextField.unfocus();
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (!scroll.hasSelected()) { return; }
		if (scroll.getID() == 0) {
			if (selectedCategory.equals(scroll.getSelected())) { return; }
			selectedCategory = scroll.getSelected();
			selectedQuest = "";
			scrollQuests.setSelect(-1);
		}
		if (scroll.getID() == 1) {
			if (selectedQuest.equals(scroll.getSelected())) { return; }
			selectedQuest = scroll.getSelected();
		}
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (!selectedQuest.isEmpty() && scroll.getID() == 1) { setSubGui(new SubGuiQuestEdit(questData.get(selectedQuest))); }
		else if (!selectedCategory.isEmpty() && scroll.getID() == 0) { setSubGui(new SubGuiEditText(3, categoryData.get(selectedCategory).title)); }
	}

	@Override
	public void subGuiClosed(GuiScreen subgui) {
		if (subgui instanceof SubGuiEditText && !((SubGuiEditText) subgui).cancelled) {
			if (((SubGuiEditText) subgui).getId() == 1) { // create category
				QuestCategory category = new QuestCategory();
				StringBuilder t = new StringBuilder(((SubGuiEditText) subgui).text[0]);
				boolean has = true;
				while (has) {
					has = false;
					for (QuestCategory cat : QuestController.instance.categories.values()) {
						if (cat.id != category.id && cat.title.equalsIgnoreCase(t.toString())) {
							has = true;
							break;
						}
					}
					if (has) { t.append("_"); }
				}
				category.title = t.toString();
				selectedCategory = category.title;
				Client.sendData(EnumPacketServer.QuestCategorySave, category.save(new NBTTagCompound()));
				initGui();
			}
			if (((SubGuiEditText) subgui).getId() == 3) { // rename category
				if (((SubGuiEditText) subgui).text[0].isEmpty() || !categoryData.containsKey(selectedCategory)) { return; }
				QuestCategory category = categoryData.get(selectedCategory).copy();
				if (category.title.equals(((SubGuiEditText) subgui).text[0])) { return; }
				StringBuilder t = new StringBuilder(((SubGuiEditText) subgui).text[0]);
				boolean has = true;
				while (has) {
					has = false;
					for (QuestCategory cat : QuestController.instance.categories.values()) {
						if (cat.id != category.id && cat.title.equalsIgnoreCase(t.toString())) {
							has = true;
							break;
						}
					}
					if (has) { t.append("_"); }
				}
				category.title = t.toString();
				selectedCategory = category.title;
				Client.sendData(EnumPacketServer.QuestCategorySave, category.save(new NBTTagCompound()));
				initGui();
			}
			if (((SubGuiEditText) subgui).getId() == 11) { // create quest
				if (((SubGuiEditText) subgui).text[0].isEmpty()) { return; }
				Quest quest = new Quest(categoryData.get(selectedCategory));
				StringBuilder t = new StringBuilder(((SubGuiEditText) subgui).text[0]);
				boolean has = true;
				while (has) {
					has = false;
					for (Quest q : quest.category.quests.values()) {
						if (quest.id != q.id && q.title.equalsIgnoreCase(t.toString())) {
							has = true;
							break;
						}
					}
					if (has) { t.append("_"); }
				}
				quest.setName(t.toString());
				selectedQuest = getKey(quest);
				Client.sendData(EnumPacketServer.QuestSave, categoryData.get(selectedCategory).id, quest.save(new NBTTagCompound()));
				initGui();
			}
		}
		if (subgui instanceof SubGuiQuestEdit) { initGui(); }
	}

}
