package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.questtypes.SubGuiNpcQuestTypeDialog;
import noppes.npcs.client.gui.questtypes.SubGuiNpcQuestTypeKill;
import noppes.npcs.client.gui.questtypes.SubGuiNpcQuestTypeLocation;
import noppes.npcs.client.gui.questtypes.SubGuiNpcQuestTypeManual;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.quests.QuestObjective;

import javax.annotation.Nonnull;

public class SubGuiQuestObjectiveSelect extends SubGuiInterface {

	protected Quest quest;

	public SubGuiQuestObjectiveSelect(GuiScreen gui) {
		super(0);
		setBackground("companion_empty.png");
		closeOnEsc = true;
		xSize = 172;
		ySize = 167;

		parent = gui;
		quest = NoppesUtilServer.getEditingQuest(player);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		QuestObjective task;
		switch (button.getID()) {
			case 66: onClosed(); return;
			case 71: {
				task = (QuestObjective) quest.addTask();
				if (task == null) { return; }
				task.setType(EnumQuestTask.ITEM);
				Client.sendData(EnumPacketServer.QuestReset, quest.save(new NBTTagCompound()), quest.questInterface.getPos(task));
				return;
			} // collect item
			case 72: {
				task = (QuestObjective) quest.addTask();
				if (task == null) { return; }
				task.setType(EnumQuestTask.CRAFT);
				Client.sendData(EnumPacketServer.QuestReset, quest.save(new NBTTagCompound()), quest.questInterface.getPos(task));
				return;
			}
			case 73: {
				task = (QuestObjective) quest.addTask();
				if (task == null) { return; }
				task.setType(EnumQuestTask.KILL);
				setSubGui(new SubGuiNpcQuestTypeKill(npc, task, parent));
				return;
			}
			case 74: {
				task = (QuestObjective) quest.addTask();
				if (task == null) { return; }
				task.setType(EnumQuestTask.AREAKILL);
				setSubGui(new SubGuiNpcQuestTypeKill(npc, task, parent));
				return;
			}
			case 75: {
				task = (QuestObjective) quest.addTask();
				if (task == null) { return; }
				task.setType(EnumQuestTask.DIALOG);
				setSubGui(new SubGuiNpcQuestTypeDialog(npc, task, parent));
				return;
			}
			case 76: {
				task = (QuestObjective) quest.addTask();
				if (task == null) { return; }
				task.setType(EnumQuestTask.LOCATION);
				setSubGui(new SubGuiNpcQuestTypeLocation(npc, task, parent));
				return;
			}
			case 77: {
				task = (QuestObjective) quest.addTask();
				if (task == null) { return; }
				task.setType(EnumQuestTask.MANUAL);
				setSubGui(new SubGuiNpcQuestTypeManual(npc, task, parent));
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		addLabel(new GuiNpcLabel(80, "task.chose", guiLeft + 4, guiTop + 5));
		addButton(new GuiNpcButton(71, guiLeft + 4, guiTop + 18, 80, 20, "enum.quest.item")
				.setHoverText("drop.hover.task.0"));
		addButton( new GuiNpcButton(72, guiLeft + 87, guiTop + 18, 80, 20, "enum.quest.craft")
				.setHoverText("drop.hover.task.1"));
		addButton(new GuiNpcButton(73, guiLeft + 4, guiTop + 40, 80, 20, "enum.quest.kill")
				.setHoverText("drop.hover.task.2"));
		addButton(new GuiNpcButton(74, guiLeft + 87, guiTop + 40, 80, 20, "enum.quest.area_kill")
				.setHoverText("drop.hover.task.3"));
		addButton(new GuiNpcButton(75, guiLeft + 4, guiTop + 62, 80, 20, "enum.quest.dialog")
				.setHoverText("drop.hover.task.4"));
		addButton(new GuiNpcButton(76, guiLeft + 87, guiTop + 62, 80, 20, "enum.quest.location")
				.setHoverText("drop.hover.task.5"));
		addButton(new GuiNpcButton(77, guiLeft + 4, guiTop + 84, 80, 20, "enum.quest.manual")
				.setHoverText("drop.hover.task.6"));
		addButton(new GuiNpcButton(66, guiLeft + 4, guiTop + 142, 80, 20, "gui.back")
				.setHoverText("hover.back"));
	}

}
