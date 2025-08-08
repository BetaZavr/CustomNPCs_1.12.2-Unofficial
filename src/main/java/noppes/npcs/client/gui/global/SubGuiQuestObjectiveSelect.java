package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeDialog;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeKill;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeLocation;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeManual;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.IGuiNpcButton;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.quests.QuestObjective;

public class SubGuiQuestObjectiveSelect
extends SubGuiInterface {

	public Quest quest;

	public SubGuiQuestObjectiveSelect(GuiScreen gui) {
		setBackground("companion_empty.png");
		xSize = 172;
		ySize = 167;
		closeOnEsc = true;

		parent = gui;
		quest = NoppesUtilServer.getEditingQuest(player);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		QuestObjective task;
		switch (button.getID()) {
			case 66: close(); return;
			case 71: { // collect item
				task = (QuestObjective) quest.addTask();
				if (task == null) {
					return;
				}
				task.setType(EnumQuestTask.ITEM);
				Client.sendData(EnumPacketServer.QuestReset, quest.save(new NBTTagCompound()), quest.questInterface.getPos(task));
				return;
			}
			case 72: {
				task = (QuestObjective) quest.addTask();
				if (task == null) {
					return;
				}
				task.setType(EnumQuestTask.CRAFT);
				Client.sendData(EnumPacketServer.QuestReset, quest.save(new NBTTagCompound()), quest.questInterface.getPos(task));
				return;
			}
			case 73: {
				task = (QuestObjective) quest.addTask();
				if (task == null) {
					return;
				}
				task.setType(EnumQuestTask.KILL);
				setSubGui(new GuiNpcQuestTypeKill(npc, task, parent));
				return;
			}
			case 74: {
				task = (QuestObjective) quest.addTask();
				if (task == null) {
					return;
				}
				task.setType(EnumQuestTask.AREAKILL);
				setSubGui(new GuiNpcQuestTypeKill(npc, task, parent));
				return;
			}
			case 75: {
				task = (QuestObjective) quest.addTask();
				if (task == null) {
					return;
				}
				task.setType(EnumQuestTask.DIALOG);
				setSubGui(new GuiNpcQuestTypeDialog(npc, task, parent));
				return;
			}
			case 76: {
				task = (QuestObjective) quest.addTask();
				if (task == null) {
					return;
				}
				task.setType(EnumQuestTask.LOCATION);
				setSubGui(new GuiNpcQuestTypeLocation(npc, task, parent));
				return;
			}
			case 77: {
				task = (QuestObjective) quest.addTask();
				if (task == null) {
					return;
				}
				task.setType(EnumQuestTask.MANUAL);
				setSubGui(new GuiNpcQuestTypeManual(npc, task, parent));
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		addLabel(new GuiNpcLabel(80, "task.chose", guiLeft + 4, guiTop + 5));
		GuiNpcButton button = new GuiNpcButton(71, guiLeft + 4, guiTop + 18, 80, 20, "enum.quest.item");
		button.setHoverText("drop.hover.task.0");
		addButton(button);
		button = new GuiNpcButton(72, guiLeft + 87, guiTop + 18, 80, 20, "enum.quest.craft");
		button.setHoverText("drop.hover.task.1");
		addButton(button);
		button = new GuiNpcButton(73, guiLeft + 4, guiTop + 40, 80, 20, "enum.quest.kill");
		button.setHoverText("drop.hover.task.2");
		addButton(button);
		button = new GuiNpcButton(74, guiLeft + 87, guiTop + 40, 80, 20, "enum.quest.area_kill");
		button.setHoverText("drop.hover.task.3");
		addButton(button);
		button = new GuiNpcButton(75, guiLeft + 4, guiTop + 62, 80, 20, "enum.quest.dialog");
		button.setHoverText("drop.hover.task.4");
		addButton(button);
		button = new GuiNpcButton(76, guiLeft + 87, guiTop + 62, 80, 20, "enum.quest.location");
		button.setHoverText("drop.hover.task.5");
		addButton(button);
		button = new GuiNpcButton(77, guiLeft + 4, guiTop + 84, 80, 20, "enum.quest.manual");
		button.setHoverText("drop.hover.task.6");
		addButton(button);
		button = new GuiNpcButton(66, guiLeft + 4, guiTop + 142, 80, 20, "gui.back");
		button.setHoverText("hover.back");
		addButton(button);
	}

}
