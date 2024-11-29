package noppes.npcs.client.gui.global;

import java.util.*;
import java.util.Map.Entry;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class GuiNPCManageQuest extends GuiNPCInterface2 implements ISubGuiListener, ICustomScrollListener, GuiYesNoCallback {

	public static GuiScreen Instance;
	private static boolean isName = true;
	private final TreeMap<String, QuestCategory> categoryData = new TreeMap<>();
	private final Map<String, Quest> questData = new LinkedHashMap<>();
	private GuiCustomScroll scrollCategories;
	private GuiCustomScroll scrollQuests;
	private String selectedCategory = "";
	private String selectedQuest = "";
	private Quest copyQuest = null;
	String chr = "" + ((char) 167);

	public GuiNPCManageQuest(EntityNPCInterface npc) {
		super(npc);
		GuiNPCManageQuest.Instance = this;
		Client.sendData(EnumPacketServer.QuestCategoryGet);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 1: {
				this.setSubGui(new SubGuiEditText(1, Util.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 2: {
				if (!this.categoryData.containsKey(this.selectedCategory)) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this,
						this.categoryData.get(this.selectedCategory).title,
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 2);
				this.displayGuiScreen(guiyesno);
				break;
			}
			case 3: {
				if (!this.categoryData.containsKey(this.selectedCategory)) {
					return;
				}
				this.setSubGui(new SubGuiEditText(3, this.categoryData.get(this.selectedCategory).title));
				break;
			}
			case 9: { // paste
				if (this.copyQuest == null || !this.categoryData.containsKey(this.selectedCategory)) {
					return;
				}
				Quest quest = this.copyQuest.copy();
				quest.id = -1;
				quest.category = this.categoryData.get(this.selectedCategory);

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
				this.selectedQuest = getKey(quest);
				Client.sendData(EnumPacketServer.QuestSave, quest.category.id, quest.writeToNBT(new NBTTagCompound()));
				this.initGui();
				break;
			}
			case 10: { // copy
				if (!this.questData.containsKey(this.selectedQuest)) {
					return;
				}
				this.copyQuest = this.questData.get(this.selectedQuest);
				this.initGui();
				break;
			}
			case 11: {
				this.setSubGui(new SubGuiEditText(11, Util.instance
						.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 12: {
				if (!this.questData.containsKey(this.selectedQuest)) {
					return;
				}
				GuiYesNo guiyesno = new GuiYesNo(this, this.questData.get(this.selectedQuest).getTitle(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 12);
				this.displayGuiScreen(guiyesno);
				break;
			}
			case 13: {
				if (!this.questData.containsKey(this.selectedQuest)) {
					return;
				}
				this.setSubGui(new GuiQuestEdit(this.questData.get(this.selectedQuest)));
				break;
			}
			case 14: {
				GuiNPCManageQuest.isName = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
		}
	}

	public void close() {
		super.close();
	}

	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI(this.player, this);
		if (!result) {
			return;
		}
		if (id == 2) {
			Client.sendData(EnumPacketServer.QuestCategoryRemove, this.categoryData.get(this.selectedCategory).id);
			this.selectedCategory = "";
			this.selectedQuest = "";
		}
		if (id == 12) {
			Client.sendData(EnumPacketServer.QuestRemove, this.questData.get(this.selectedQuest).id);
			this.selectedQuest = "";
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.hasSubGui()) {
			return;
		}
		this.drawHorizontalLine(this.guiLeft + 348, this.guiLeft + 414, this.guiTop + 128, 0x80000000);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.category.edit").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.category.del").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.category.add").getFormattedText());
		} else if (this.getButton(9) != null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.quest.paste." + (this.copyQuest != null),
					(this.copyQuest != null ? this.copyQuest.getKey() : "")).getFormattedText());
		} else if (this.getButton(10) != null && this.getButton(10).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("manager.hover.quest.copy", this.selectedQuest).getFormattedText());
		} else if (this.getButton(11) != null && this.getButton(11).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("manager.hover.quest.add", this.selectedCategory).getFormattedText());
		} else if (this.getButton(12) != null && this.getButton(12).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("manager.hover.quest.del", this.selectedQuest).getFormattedText());
		} else if (this.getButton(13) != null && this.getButton(13).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("manager.hover.quest.edit", this.selectedQuest).getFormattedText());
		} else if (this.getButton(14) != null && this.getButton(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.sort",
					new TextComponentTranslation("dialog.dialogs").getFormattedText(),
					((GuiNpcCheckBox) this.getButton(14)).getText()).getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.categoryData.clear();
		this.questData.clear();
		QuestController qData = QuestController.instance;
		String[][] ht = null;
		for (QuestCategory category : qData.categories.values()) {
			this.categoryData.put(category.title, category);
			if (this.selectedCategory.isEmpty()) {
				this.selectedCategory = category.title;
			}
		}
		if (!this.selectedCategory.isEmpty()) {
			if (this.categoryData.containsKey(this.selectedCategory)) {
				Map<String, Quest> map = new TreeMap<>();
				for (Quest quest : this.categoryData.get(this.selectedCategory).quests.values()) {
					map.put(getKey(quest), quest);
				}
				List<Entry<String, Quest>> list = getEntryList(map);
				for (Entry<String, Quest> entry : list) {
					this.questData.put(entry.getKey(), entry.getValue());
					if (this.selectedQuest.isEmpty()) {
						this.selectedQuest = entry.getKey();
					}
				}
			} else {
				this.selectedCategory = "";
				this.selectedQuest = "";
			}
			// Hover Text:
			if (!this.questData.isEmpty()) {
				int pos = 0;
				ht = new String[this.questData.size()][];
				DialogController dData = DialogController.instance;
				for (Quest quest : this.questData.values()) {
					final List<String> h = getStrings(quest, qData, dData);
					ht[pos] = h.toArray(new String[0]);
					pos++;
				}
			}
		}
		if (!selectedCategory.isEmpty() && !categoryData.containsKey(selectedCategory)) {
			selectedCategory = "";
		}
		if (!selectedQuest.isEmpty() && !questData.containsKey(selectedQuest)) {
			selectedQuest = "";
		}
		// scroll info
		this.addLabel(new GuiNpcLabel(0, "gui.categories", this.guiLeft + 8, this.guiTop + 4));
		this.addLabel(new GuiNpcLabel(1, "quest.quests", this.guiLeft + 180, this.guiTop + 4));
		// quest buttons
		int x = this.guiLeft + 350, y = this.guiTop + 8;
		this.addLabel(new GuiNpcLabel(3, "quest.quests", this.guiLeft + 356, this.guiTop + 8));
		this.addButton(new GuiNpcButton(13, x, y += 10, 64, 15, "selectServer.edit", !this.selectedQuest.isEmpty()));
		this.addButton(new GuiNpcButton(12, x, y += 17, 64, 15, "gui.remove", !this.selectedQuest.isEmpty()));
		this.addButton(new GuiNpcButton(11, x, y += 17, 64, 15, "gui.add", !this.selectedCategory.isEmpty()));
		this.addButton(new GuiNpcButton(10, x, y += 21, 64, 15, "gui.copy", !this.selectedCategory.isEmpty()));
		this.getButton(10).setEnabled(!selectedQuest.isEmpty());
		this.addButton(new GuiNpcButton(9, x, y += 17, 64, 15, "gui.paste", this.copyQuest != null));
		addButton(new GuiNpcCheckBox(14, x, y + 17, 64, 14, "gui.name", "ID", GuiNPCManageQuest.isName));
		// category buttons
		y = this.guiTop + 134;
		this.addLabel(new GuiNpcLabel(2, "gui.categories", x + 2, y));
		this.addButton(new GuiNpcButton(3, x, y += 10, 64, 15, "selectServer.edit", !this.selectedCategory.isEmpty()));
		this.addButton(new GuiNpcButton(2, x, y += 17, 64, 15, "gui.remove", !this.selectedCategory.isEmpty()));
		this.addButton(new GuiNpcButton(1, x, y + 17, 64, 15, "gui.add"));

		if (this.scrollCategories == null) {
			(this.scrollCategories = new GuiCustomScroll(this, 0)).setSize(170, this.ySize - 3);
		}
		this.scrollCategories.setList(new ArrayList<>(categoryData.keySet()));
		this.scrollCategories.guiLeft = this.guiLeft + 4;
		this.scrollCategories.guiTop = this.guiTop + 15;
		if (!this.selectedCategory.isEmpty()) {
			this.scrollCategories.setSelected(this.selectedCategory);
		}
		this.addScroll(this.scrollCategories);

		if (this.scrollQuests == null) {
			(this.scrollQuests = new GuiCustomScroll(this, 1)).setSize(170, this.ySize - 3);
		}
		this.scrollQuests.setListNotSorted(new ArrayList<>(questData.keySet()));
		this.scrollQuests.guiLeft = this.guiLeft + 176;
		this.scrollQuests.guiTop = this.guiTop + 15;
		if (ht != null) {
			this.scrollQuests.hoversTexts = ht;
		}
		if (!this.selectedQuest.isEmpty()) {
			this.scrollQuests.setSelected(this.selectedQuest);
		}
		this.addScroll(this.scrollQuests);
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
		return chr + "7ID:" + quest.id + "-\"" + chr + "r" + quest.getTitle() + chr + "7\"" + chr
				+ (b ? "2 (" : "c (") + (new TextComponentTranslation("quest.has." + b).getFormattedText())
				+ chr + (b ? "2)" : "c)");
	}

	private List<String> getStrings(Quest quest, QuestController qData, DialogController dData) {
		List<String> h = new ArrayList<>();
		List<String> quests = new ArrayList<>();
		List<String> dialogs = new ArrayList<>();
		h.add(new TextComponentTranslation(quest.title).getFormattedText() + ":");
		for (Quest q : qData.quests.values()) {
			if (q.nextQuest != quest.id) {
				continue;
			}
			quests.add(chr + "7ID:" + q.id + chr + "8 " + q.category.getName() + "/" + chr + "r"
					+ q.getName());
		}
		for (Dialog d : dData.dialogs.values()) {
			if (d.quest != quest.id) {
				continue;
			}
			dialogs.add(chr + "7ID:" + d.id + chr + "8 " + d.category.getName() + "/" + chr + "r"
					+ d.getName());
		}

		if (quests.isEmpty() && dialogs.isEmpty()) {
			h.add(new TextComponentTranslation("quest.hover.quest.0").getFormattedText());
		}

		if (!quests.isEmpty()) {
			h.add(new TextComponentTranslation("quest.hover.in.quests").getFormattedText());
			h.addAll(quests);
		} else {
			h.add(new TextComponentTranslation("quest.hover.quest.1").getFormattedText());
		}

		if (!dialogs.isEmpty()) {
			h.add(new TextComponentTranslation("quest.hover.in.dialogs").getFormattedText());
			h.addAll(dialogs);
		} else {
			h.add(new TextComponentTranslation("quest.hover.quest.2").getFormattedText());
		}
		return h;
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui == null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getSelected() == null) {
			return;
		}
		if (scroll.id == 0) {
			if (this.selectedCategory.equals(scroll.getSelected())) {
				return;
			}
			this.selectedCategory = scroll.getSelected();
			this.selectedQuest = "";
			this.scrollQuests.selected = -1;
		}
		if (scroll.id == 1) {
			if (this.selectedQuest.equals(scroll.getSelected())) {
				return;
			}
			this.selectedQuest = scroll.getSelected();
		}
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (!this.selectedQuest.isEmpty() && scroll.id == 1) {
			this.setSubGui(new GuiQuestEdit(this.questData.get(this.selectedQuest)));
		}
		if (!this.selectedCategory.isEmpty() && scroll.id == 0) {
			this.setSubGui(new SubGuiEditText(3, this.categoryData.get(this.selectedCategory).title));
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiEditText && !((SubGuiEditText) subgui).cancelled) {
			if (subgui.id == 1) { // new category
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
				this.selectedCategory = category.title;
				Client.sendData(EnumPacketServer.QuestCategorySave, category.writeNBT(new NBTTagCompound()));
				this.initGui();
			}
			if (subgui.id == 3) { // rename category
				if (((SubGuiEditText) subgui).text[0].isEmpty() || !this.categoryData.containsKey(this.selectedCategory)) { return; }
				QuestCategory category = this.categoryData.get(this.selectedCategory).copy();
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
				this.selectedCategory = category.title;
				Client.sendData(EnumPacketServer.QuestCategorySave, category.writeNBT(new NBTTagCompound()));
				this.initGui();
			}
			if (subgui.id == 11) { // new quest
				if (((SubGuiEditText) subgui).text[0].isEmpty()) {
					return;
				}
				Quest quest = new Quest(this.categoryData.get(this.selectedCategory));

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

				this.selectedQuest = getKey(quest);
				Client.sendData(EnumPacketServer.QuestSave, this.categoryData.get(this.selectedCategory).id, quest.writeToNBT(new NBTTagCompound()));
				this.initGui();
			}
		}
		if (subgui instanceof GuiQuestEdit) {
			this.initGui();
		}
	}

}
