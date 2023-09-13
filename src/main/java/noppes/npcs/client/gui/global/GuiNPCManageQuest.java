package noppes.npcs.client.gui.global;

import java.util.List;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
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
import noppes.npcs.util.AdditionalMethods;

public class GuiNPCManageQuest
extends GuiNPCInterface2
implements ISubGuiListener, ICustomScrollListener, GuiYesNoCallback {
	
	public static GuiScreen Instance;
	private final TreeMap<String, QuestCategory> categoryData;
	private final TreeMap<String, Quest> questData;
	private GuiCustomScroll scrollCategories;
	private GuiCustomScroll scrollQuests;
	private String selectedCategory = "";
	private String selectedQuest = "";
	private Quest copyQuest = null;
	char chr = Character.toChars(0x00A7)[0];

	public GuiNPCManageQuest(EntityNPCInterface npc) {
		super(npc);
		this.categoryData = Maps.<String, QuestCategory>newTreeMap();
		this.questData = Maps.<String, Quest>newTreeMap();
		GuiNPCManageQuest.Instance = this;
		Client.sendData(EnumPacketServer.QuestCategoryGet);
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
			if (this.selectedCategory.isEmpty()) { this.selectedCategory = category.title; }
		}
		if (!this.selectedCategory.isEmpty()) {
			if (this.categoryData.containsKey(this.selectedCategory)) {
				for (Quest quest : this.categoryData.get(this.selectedCategory).quests.values()) {
					boolean b = quest.isSetUp();
					String key = chr + "7ID:" + quest.id+"-\"" + chr + "r" + quest.getTitle() + chr + "7\"" + chr + (b ? "2 (" : "c (") + (new TextComponentTranslation("quest.has." + b).getFormattedText()) + chr + (b ? "2)" : "c)");
					this.questData.put(key, quest);
					if (this.selectedQuest.isEmpty()) { this.selectedQuest = key; }
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
					List<String> h = Lists.newArrayList(), quests = Lists.newArrayList(), dialogs = Lists.newArrayList();
					for (Quest q : qData.quests.values()) {
						if (q.nextQuestid!=quest.id) { continue; }
						quests.add(chr + "7ID:" + q.id + chr + "8 " +q.category.getName() + "/" + chr + "r" + q.getName());
					}
					for (Dialog d : dData.dialogs.values()) {
						if (d.quest!=quest.id) { continue; }
						dialogs.add(chr + "7ID:" + d.id + chr + "8 " +d.category.getName() + "/" + chr + "r" + d.getName());
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
					ht[pos] = h.toArray(new String[h.size()]);
					pos ++;
				}
			}
		}
		// scroll info
		this.addLabel(new GuiNpcLabel(0, "gui.categories", this.guiLeft + 8, this.guiTop + 4));
		this.addLabel(new GuiNpcLabel(1, "quest.quests", this.guiLeft + 175, this.guiTop + 4));
		// quest buttons
		int x = this.guiLeft + 350, y = this.guiTop + 8;
		this.addLabel(new GuiNpcLabel(3, "quest.quests", this.guiLeft + 356, this.guiTop + 8));
		this.addButton(new GuiNpcButton(13, x, y += 10, 64, 15, "selectServer.edit", !this.selectedQuest.isEmpty()));
		this.addButton(new GuiNpcButton(12, x, y += 17, 64, 15, "gui.remove", !this.selectedQuest.isEmpty()));
		this.addButton(new GuiNpcButton(11, x, y += 17, 64, 15, "gui.add", !this.selectedCategory.isEmpty()));
		this.addButton(new GuiNpcButton(10, x, y += 21, 64, 15, "gui.copy", !this.selectedCategory.isEmpty()));
		this.addButton(new GuiNpcButton(9, x, y += 17, 64, 15, "gui.paste", this.copyQuest!=null));
		// category buttons
		y = this.guiTop + 130;
		this.addLabel(new GuiNpcLabel(2, "gui.categories", x + 2, y));
		this.addButton(new GuiNpcButton(3, x, y += 10, 64, 15, "selectServer.edit", !this.selectedCategory.isEmpty()));
		this.addButton(new GuiNpcButton(2, x, y += 17, 64, 15, "gui.remove",!this.selectedCategory.isEmpty()));
		this.addButton(new GuiNpcButton(1, x, y += 17, 64, 15, "gui.add"));

		if (this.scrollCategories == null) { (this.scrollCategories = new GuiCustomScroll(this, 0)).setSize(170, 200); }
		this.scrollCategories.setList(Lists.newArrayList(this.categoryData.keySet()));
		this.scrollCategories.guiLeft = this.guiLeft + 4;
		this.scrollCategories.guiTop = this.guiTop + 14;
		if (!this.selectedCategory.isEmpty()) { this.scrollCategories.setSelected(this.selectedCategory); }
		this.addScroll(this.scrollCategories);

		if (this.scrollQuests == null) { (this.scrollQuests = new GuiCustomScroll(this, 1)).setSize(170, 200); }
		this.scrollQuests.setList(Lists.newArrayList(this.questData.keySet()));
		this.scrollQuests.guiLeft = this.guiLeft + 175;
		this.scrollQuests.guiTop = this.guiTop + 14;
		if (ht!=null) { this.scrollQuests.hoversTexts = ht; }
		if (!this.selectedQuest.isEmpty()) { this.scrollQuests.setSelected(this.selectedQuest); }
		this.addScroll(this.scrollQuests);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui !=null || !CustomNpcs.showDescriptions) { return; }
		if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.category.edit").getFormattedText());
		}
		else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.category.del").getFormattedText());
		}
		else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.category.add").getFormattedText());
		}
		else if (this.getButton(9)!=null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.quest.paste." + (this.copyQuest!=null), (this.copyQuest!=null ? this.copyQuest.getKey(): "")).getFormattedText());
		}
		else if (this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.quest.copy", this.selectedQuest).getFormattedText());
		}
		else if (this.getButton(11)!=null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.quest.add", this.selectedCategory).getFormattedText());
		}
		else if (this.getButton(12)!=null && this.getButton(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.quest.del", this.selectedQuest).getFormattedText());
		}
		else if (this.getButton(13)!=null && this.getButton(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("manager.hover.quest.edit", this.selectedQuest).getFormattedText());
		}
	}

	@Override
	public void buttonEvent(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch(button.id) {
			case 1: {
				this.setSubGui(new SubGuiEditText(1, AdditionalMethods.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 2: {
				if (!this.categoryData.containsKey(this.selectedCategory)) { return; }
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, this.categoryData.get(this.selectedCategory).title, new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 2);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 3: {
				if (!this.categoryData.containsKey(this.selectedCategory)) { return; }
				this.setSubGui(new SubGuiEditText(3, this.categoryData.get(this.selectedCategory).title));
				break;
			}
			case 9: { // paste
				if (this.copyQuest==null || !this.categoryData.containsKey(this.selectedCategory)) { return; }
				Quest quest = this.copyQuest.copy();
				quest.id = -1;
				quest.category = this.categoryData.get(this.selectedCategory);
				while (QuestController.instance.containsQuestName(quest.category, quest)) { quest.setName(quest.getName() + "_"); }
				this.selectedQuest = "" + quest.getTitle();
				Client.sendData(EnumPacketServer.QuestSave, quest.category.id, quest.writeToNBT(new NBTTagCompound()));
				this.initGui();
				break;
			}
			case 10: { // copy
				if (!this.questData.containsKey(this.selectedQuest)) { return; }
				this.copyQuest = this.questData.get(this.selectedQuest);
				this.initGui();
				break;
			}
			case 11: {
				this.setSubGui(new SubGuiEditText(11, AdditionalMethods.instance.deleteColor(new TextComponentTranslation("gui.new").getFormattedText())));
				break;
			}
			case 12: {
				if (!this.questData.containsKey(this.selectedQuest)) { return; }
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, this.questData.get(this.selectedQuest).getTitle(), new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 12);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 13: {
				if (!this.questData.containsKey(this.selectedQuest)) { return; }
				this.setSubGui(new GuiQuestEdit(this.questData.get(this.selectedQuest)));
				break;
			}
			default: { break; }
		}
	}

	public void close() {
		super.close();
	}

	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI((EntityPlayer) this.player, this);
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
	public void save() {
		GuiNpcTextField.unfocus();
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		if (guiCustomScroll.getSelected() == null) {
			return;
		}
		if (guiCustomScroll.id == 0) {
			if (this.selectedCategory.equals(guiCustomScroll.getSelected())) {
				return;
			}
			this.selectedCategory = guiCustomScroll.getSelected();
			this.selectedQuest = "";
			this.scrollQuests.selected = -1;
		}
		if (guiCustomScroll.id == 1) {
			if (this.selectedQuest.equals(guiCustomScroll.getSelected())) {
				return;
			}
			this.selectedQuest = guiCustomScroll.getSelected();
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
		if (subgui instanceof SubGuiEditText && ((SubGuiEditText) subgui).cancelled) {
			return;
		}
		if (subgui.id == 1) { // new category
			QuestCategory category = new QuestCategory();
			category.title = ((SubGuiEditText) subgui).text[0];
			this.selectedCategory = category.title;
			while (QuestController.instance.containsCategoryName(category)) {
				StringBuilder sb = new StringBuilder();
				QuestCategory questCategory = category;
				questCategory.title = sb.append(questCategory.title).append("_").toString();
				this.selectedCategory = questCategory.title;
			}
			Client.sendData(EnumPacketServer.QuestCategorySave, category.writeNBT(new NBTTagCompound()));
			this.initGui();
		}
		if (subgui.id == 3) { // rename category
			if (((SubGuiEditText) subgui).text[0].isEmpty()) { return; }
			QuestCategory category = this.categoryData.get(this.selectedCategory);
			category.title = ((SubGuiEditText) subgui).text[0];
			while (QuestController.instance.containsCategoryName(category)) { category.title += "_"; }
			this.selectedCategory = category.title;
			Client.sendData(EnumPacketServer.QuestCategorySave, category.writeNBT(new NBTTagCompound()));
			this.initGui();
		}
		if (subgui.id == 11) { // new quest
			if (((SubGuiEditText) subgui).text[0].isEmpty()) {
				return;
			}
			Quest quest = new Quest(this.categoryData.get(this.selectedCategory));
			quest.setName(((SubGuiEditText) subgui).text[0]);
			while (QuestController.instance.containsQuestName(this.categoryData.get(this.selectedCategory), quest)) {
				quest.setName(quest.getName() + "_");
			}
			this.selectedQuest = "" + quest.getTitle();
			Client.sendData(EnumPacketServer.QuestSave, this.categoryData.get(this.selectedCategory).id, quest.writeToNBT(new NBTTagCompound()));
			this.initGui();
		}
		if (subgui instanceof GuiQuestEdit) {
			this.initGui();
		}
	}
	
	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui==null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
		}
		super.keyTyped(c, i);
	}
}
