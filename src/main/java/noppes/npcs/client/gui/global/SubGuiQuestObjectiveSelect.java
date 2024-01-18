package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeDialog;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeKill;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeLocation;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeManual;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.quests.QuestObjective;

// New
public class SubGuiQuestObjectiveSelect extends SubGuiInterface {

	public Quest quest;

	public SubGuiQuestObjectiveSelect(GuiScreen parent) {
		this.quest = NoppesUtilServer.getEditingQuest(this.player);
		this.setBackground("companion_empty.png");
		this.xSize = 172;
		this.ySize = 167;
		this.closeOnEsc = true;
		this.parent = parent;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		QuestObjective task = null;
		switch (button.id) {
			case 66: {
				this.close();
				return;
			}
			case 71: { // collect item
				task = (QuestObjective) this.quest.addTask();
				if (task == null) {
					return;
				}
				task.setType(EnumQuestTask.ITEM);
				Client.sendData(EnumPacketServer.QuestReset, this.quest.writeToNBT(new NBTTagCompound()),
						this.quest.questInterface.getPos(task), task.slotID);
				return;
			}
			case 72: {
				task = (QuestObjective) this.quest.addTask();
				if (task == null) {
					return;
				}
				task.setType(EnumQuestTask.CRAFT);
				Client.sendData(EnumPacketServer.QuestReset, this.quest.writeToNBT(new NBTTagCompound()),
						this.quest.questInterface.getPos(task), task.slotID);
				return;
			}
			case 73: {
				task = (QuestObjective) this.quest.addTask();
				if (task == null) {
					return;
				}
				task.setType(EnumQuestTask.KILL);
				this.setSubGui(new GuiNpcQuestTypeKill(this.npc, task, this.parent));
				return;
			}
			case 74: {
				task = (QuestObjective) this.quest.addTask();
				if (task == null) {
					return;
				}
				task.setType(EnumQuestTask.AREAKILL);
				this.setSubGui(new GuiNpcQuestTypeKill(this.npc, task, this.parent));
				return;
			}
			case 75: {
				task = (QuestObjective) this.quest.addTask();
				if (task == null) {
					return;
				}
				task.setType(EnumQuestTask.DIALOG);
				this.setSubGui(new GuiNpcQuestTypeDialog(this.npc, task, this.parent));
				return;
			}
			case 76: {
				task = (QuestObjective) this.quest.addTask();
				if (task == null) {
					return;
				}
				task.setType(EnumQuestTask.LOCATION);
				this.setSubGui(new GuiNpcQuestTypeLocation(this.npc, task, this.parent));
				return;
			}
			case 77: {
				task = (QuestObjective) this.quest.addTask();
				if (task == null) {
					return;
				}
				task.setType(EnumQuestTask.MANUAL);
				this.setSubGui(new GuiNpcQuestTypeManual(this.npc, task, this.parent));
				return;
			}
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (this.subgui != null || !CustomNpcs.showDescriptions) { return; }
		if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 20, 76, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.task.0", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 87, this.guiTop + 20, 76, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.task.1", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 42, 76, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.task.2", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 87, this.guiTop + 42, 76, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.task.3", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 64, 76, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.task.4", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 87, this.guiTop + 64, 76, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.task.5", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 84, 76, 16)) {
			this.setHoverText(new TextComponentTranslation("drop.hover.task.6", new Object[0]).getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 6, this.guiTop + 144, 76, 16)) {
			this.setHoverText(new TextComponentTranslation("hover.back", new Object[0]).getFormattedText());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int anyIDs = 80;
		this.addLabel(new GuiNpcLabel(anyIDs++, "task.chose", this.guiLeft + 4, this.guiTop + 5));
		this.addButton(new GuiNpcButton(71, this.guiLeft + 4, this.guiTop + 18, 80, 20, "quest.item"));
		this.addButton(new GuiNpcButton(72, this.guiLeft + 87, this.guiTop + 18, 80, 20, "quest.craft"));
		this.addButton(new GuiNpcButton(73, this.guiLeft + 4, this.guiTop + 40, 80, 20, "quest.kill"));
		this.addButton(new GuiNpcButton(74, this.guiLeft + 87, this.guiTop + 40, 80, 20, "quest.areakill"));
		this.addButton(new GuiNpcButton(75, this.guiLeft + 4, this.guiTop + 62, 80, 20, "quest.dialog"));
		this.addButton(new GuiNpcButton(76, this.guiLeft + 87, this.guiTop + 62, 80, 20, "quest.location"));
		this.addButton(new GuiNpcButton(77, this.guiLeft + 4, this.guiTop + 84, 80, 20, "quest.manual"));
		this.addButton(new GuiNpcButton(66, this.guiLeft + 4, this.guiTop + 142, 80, 20, "gui.back"));
	}

}
