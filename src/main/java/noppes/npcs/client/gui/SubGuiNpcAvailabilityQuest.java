package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.client.gui.select.GuiQuestSelection;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumAvailabilityQuest;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.Quest;

public class SubGuiNpcAvailabilityQuest extends SubGuiInterface implements ICustomScrollListener, GuiSelectionListener {

	private Availability availabitily;
	private String chr = "" + ((char) 167);
	private Map<String, EnumAvailabilityQuest> dataEnum;
	private Map<String, Integer> dataIDs;
	// New
	private GuiCustomScroll scroll;
	private String select;

	public SubGuiNpcAvailabilityQuest(Availability availabitily) {
		this.availabitily = availabitily;
		setBackground("menubg.png");
		xSize = 316;
		ySize = 217;

		dataIDs = new HashMap<String, Integer>();
		dataEnum = new HashMap<String, EnumAvailabilityQuest>();
		select = "";
		closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			if (this.select.isEmpty()) {
				return;
			}
			EnumAvailabilityQuest ead = EnumAvailabilityQuest.values()[button.getValue()];
			int id = this.dataIDs.get(this.select);
			this.availabitily.quests.put(id, ead);
			Quest quest = QuestController.instance.quests.get(this.dataIDs.get(this.select));
			this.select = "ID:" + id + " - ";
			if (quest == null) {
				this.select += chr + "4" + (new TextComponentTranslation("quest.notfound").getFormattedText());
			} else {
				this.select += chr + "7" + quest.getCategory().getName() + "/" + chr + "r" + quest.getName() + chr
						+ "7 (" + chr + "9"
						+ new TextComponentTranslation(("availability." + ead).toLowerCase()).getFormattedText() + chr
						+ "7)";
			}
			this.initGui();
		}
		if (button.id == 1) {
			this.setSubGui(new GuiQuestSelection(this.select.isEmpty() ? 0 : this.dataIDs.get(this.select)));
		}
		if (button.id == 2) {
			this.availabitily.quests.remove(this.dataIDs.get(this.select));
			this.select = "";
			this.initGui();
		}
		if (button.id == 3) { // More
			this.save();
			this.initGui();
		}
		if (button.id == 66) {
			this.close();
		}
	}

	// New
	@Override
	public void close() {
		super.close();
		List<Integer> delete = new ArrayList<Integer>();
		for (int id : this.availabitily.quests.keySet()) {
			if (this.availabitily.quests.get(id) == EnumAvailabilityQuest.Always) {
				delete.add(id);
			}
		}
		for (int id : delete) {
			this.availabitily.quests.remove(id);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (isMouseHover(mouseX, mouseY, this.guiLeft + 6, this.guiTop + this.ySize - 46, 100, 20)) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.enum.type").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + 108, this.guiTop + this.ySize - 46, 180, 20)) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.quest").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + 290, this.guiTop + this.ySize - 46, 20, 20)) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.remove").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + this.xSize - 76, this.guiTop + 192, 70, 20)) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.more").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + 6, this.guiTop + 192, 70, 20)) {
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
		this.addLabel(new GuiNpcLabel(1, "availability.available", this.guiLeft, this.guiTop + 4));
		this.getLabel(1).center(this.xSize);
		// New
		this.addButton(new GuiNpcButton(66, this.guiLeft + 6, this.guiTop + 192, 70, 20, "gui.done"));
		// New
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 6)).setSize(this.xSize - 12, this.ySize - 66);
		}
		this.dataIDs = new HashMap<String, Integer>();
		this.dataEnum = new HashMap<String, EnumAvailabilityQuest>();
		for (int id : this.availabitily.quests.keySet()) {
			String key = "ID:" + id + " - ";
			IQuest q = QuestController.instance.get(id);
			if (q == null) {
				key += chr + "4" + (new TextComponentTranslation("quest.notfound").getFormattedText());
			} else {
				key += chr + "7" + q.getCategory().getName() + "/" + chr + "r" + q.getName() + chr + "7 (" + chr + "9"
						+ new TextComponentTranslation(
								("availability." + this.availabitily.quests.get(id)).toLowerCase()).getFormattedText()
						+ chr + "7)";
			}
			this.dataIDs.put(key, id);
			this.dataEnum.put(key, this.availabitily.quests.get(id));
		}
		if (!this.select.isEmpty() && !this.dataIDs.containsKey(this.select)) {
			this.select = "";
		}
		this.scroll.setList(Lists.newArrayList(this.dataIDs.keySet()));
		this.scroll.guiLeft = this.guiLeft + 6;
		this.scroll.guiTop = this.guiTop + 14;
		if (!this.select.isEmpty()) {
			this.scroll.setSelected(this.select);
		}
		this.addScroll(this.scroll);
		int p = 0;
		if (!this.select.isEmpty()) {
			p = this.dataEnum.get(this.select).ordinal();
		}
		this.addButton(new GuiNpcButton(0, this.guiLeft + 6, this.guiTop + this.ySize - 46, 100, 20,
				new String[] { "availability.always", "availability.after", "availability.before",
						"availability.active", "availability.notactive", "availability.completed",
						"availability.canstart" },
				p));
		this.addButton(
				new GuiNpcButton(1, this.guiLeft + 108, this.guiTop + this.ySize - 46, 180, 20, "availability.select"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 290, this.guiTop + this.ySize - 46, 20, 20, "X"));

		this.addButton(
				new GuiNpcButton(3, this.guiLeft + this.xSize - 76, this.guiTop + 192, 70, 20, "availability.more"));
		this.getButton(3).setEnabled(!this.select.isEmpty());
		this.updateGuiButtons();
	}

	@Override
	public void save() {
		if (this.select.isEmpty()) {
			return;
		}
		EnumAvailabilityQuest ead = EnumAvailabilityQuest.values()[this.getButton(0).getValue()];
		int id = this.dataIDs.get(this.select);
		if (ead != EnumAvailabilityQuest.Always) {
			this.availabitily.quests.put(id, ead);
			this.dataEnum.put(this.select, ead);
		} else {
			this.availabitily.quests.remove(id);
		}
		this.select = "";
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		this.select = scroll.getSelected();
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		this.setSubGui(new GuiQuestSelection(this.dataIDs.get(this.select)));
	}

	@Override
	public void selected(int id, String name) {
		if (id <= 0) {
			return;
		}
		if (!this.select.isEmpty()) {
			this.availabitily.quests.remove(this.dataIDs.get(this.select));
		}
		Quest quest = QuestController.instance.quests.get(id);
		this.select = "ID:" + id + " - " + chr + "7" + quest.category.getName() + "/" + chr + "r" + quest.getName()
				+ chr + "7 (" + chr + "9" + new TextComponentTranslation("availability.after").getFormattedText() + chr
				+ "7)";
		this.availabitily.quests.put(id, EnumAvailabilityQuest.After);
		this.initGui();
		this.updateGuiButtons();
	}

	private void updateGuiButtons() {
		// New
		int p = 0;
		this.getButton(1).setDisplayText("availability.selectquest");
		Quest quest = null;
		if (!this.select.isEmpty()) {
			quest = QuestController.instance.quests.get(this.dataIDs.get(this.select));
			p = this.dataEnum.get(this.select).ordinal();
		}
		this.getButton(0).setDisplay(p);
		this.getButton(0).setEnabled(!this.select.isEmpty());
		this.getButton(1).setEnabled(p != 0 || this.select.isEmpty());
		this.getButton(1).setDisplayText(quest == null ? "availability.select" : quest.getName());
		this.getButton(2).setEnabled(p != 0);
	}

}
