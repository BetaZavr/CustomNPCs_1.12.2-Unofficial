package noppes.npcs.client.gui.global;

import java.awt.*;
import java.util.*;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.SubGuiMailmanSendSetup;
import noppes.npcs.client.gui.SubGuiNpcCommand;
import noppes.npcs.client.gui.SubGuiNpcFactionOptions;
import noppes.npcs.client.gui.SubGuiNpcQuestExtra;
import noppes.npcs.client.gui.SubGuiNpcTextArea;
import noppes.npcs.client.gui.questtypes.SubGuiNpcQuestTypeDialog;
import noppes.npcs.client.gui.questtypes.SubGuiNpcQuestTypeKill;
import noppes.npcs.client.gui.questtypes.SubGuiNpcQuestTypeLocation;
import noppes.npcs.client.gui.questtypes.SubGuiNpcQuestTypeManual;
import noppes.npcs.client.gui.select.SubGuiQuestSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumQuestRepeat;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.quests.QuestObjective;

import javax.annotation.Nonnull;

public class SubGuiQuestEdit extends SubGuiInterface
		implements ICustomScrollListener, GuiSelectionListener, ITextfieldListener, IGuiData, GuiYesNoCallback {

	protected Map<String, QuestObjective> tasksData;
	protected GuiCustomScroll scrollTasks;
	protected Quest quest;
	protected String task = "";

	public SubGuiQuestEdit(Quest questIn) {
		super(0);
		setBackground("menubg.png");
		closeOnEsc = true;
		xSize = 386;
		ySize = 226;

		quest = questIn;
		NoppesUtilServer.setEditingQuest(player, questIn);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 3: setSubGui(new SubGuiNpcTextArea(0, quest.completeText)); break; // end text
			case 4: setSubGui(new SubGuiNpcTextArea(1, quest.logText)); break; // log text
			case 5: Client.sendData(EnumPacketServer.QuestOpenGui, EnumGuiType.QuestReward, quest.save(new NBTTagCompound())); break; // reward
			case 8: quest.repeat = EnumQuestRepeat.values()[button.getValue()]; break; // reiteration
            case 9: setSubGui(new SubGuiNpcQuestExtra(0, quest)); break; // NPC to End
			case 10: setSubGui(new SubGuiNpcFactionOptions(quest.factionOptions)); break; // faction
			case 11: setSubGui(new SubGuiQuestSelection(quest.nextQuest)); break; // next quest
			case 12: quest.nextQuest = -1; initGui(); break; // remove next quest
			case 13: setSubGui(new SubGuiMailmanSendSetup(quest.mail)); break; // mail
			case 14: quest.mail = new PlayerMail(); initGui(); break; // remove mail
			case 15: setSubGui(new SubGuiNpcCommand(quest.command)); break; // command
			case 16: {
				quest.questInterface.upPos(tasksData.get(scrollTasks.getSelected()));
				save();
				initGui();
				break;
			} // up
			case 17: {
				quest.questInterface.downPos(tasksData.get(scrollTasks.getSelected()));
				save();
				initGui();
				break;
			} // down
			case 18: quest.step = button.getValue(); break; // type step task
			case 19: setSubGui(new SubGuiQuestObjectiveSelect(this)); break; // add task
			case 20: {
				if (quest.questInterface.removeTask(tasksData.get(task))) {
					task = "";
					save();
					initGui();
				}
				break;
			} // remove task
			case 21: {
				if (task.isEmpty() || !tasksData.containsKey(task)) { return; }
				if (tasksData.get(task).getEnumType() == EnumQuestTask.DIALOG) { setSubGui(new SubGuiNpcQuestTypeDialog(npc, tasksData.get(task), this)); }
				else if (tasksData.get(task).getEnumType() == EnumQuestTask.KILL) { setSubGui(new SubGuiNpcQuestTypeKill(npc, tasksData.get(task), this)); }
				else if (tasksData.get(task).getEnumType() == EnumQuestTask.LOCATION) { setSubGui(new SubGuiNpcQuestTypeLocation(npc, tasksData.get(task), this)); }
				else if (tasksData.get(task).getEnumType() == EnumQuestTask.AREAKILL) { setSubGui(new SubGuiNpcQuestTypeKill(npc, tasksData.get(task), this)); }
				else if (tasksData.get(task).getEnumType() == EnumQuestTask.MANUAL) { setSubGui(new SubGuiNpcQuestTypeManual(npc, tasksData.get(task), this)); }
				else { Client.sendData(EnumPacketServer.QuestReset, quest.save(new NBTTagCompound()), quest.questInterface.getPos(tasksData.get(task))); }
				break;
			} // edit task
			case 22: {
				quest.setCancelable(button.getValue() == 0);
				getLabel(16).setIsEnable(quest.isCancelable());
				if (quest.isCancelable()) {
					if (quest.forgetDialogues.length == 0) {
						TreeMap<Integer, Dialog> dialogs = DialogController.instance.dialogs;
						for (int id : dialogs.keySet()) {
							if (dialogs.get(id).quest == quest.id) {
								quest.forgetDialogues = new int[] { id };
								break;
							}
						}
					}
					if (quest.forgetQuests.length == 0) {
						TreeMap<Integer, Quest> quests = QuestController.instance.quests;
						for (int id : quests.keySet()) {
							if (id != quest.id && quests.get(id).nextQuest == quest.id) {
								quest.forgetQuests = new int[] { id };
								break;
							}
						}
					}
					String[] texts = new String[] { "", "" };
					int i = 0;
					for (int id : quest.forgetDialogues) {
						texts[0] += id;
						if (i < quest.forgetDialogues.length - 1) { texts[0] += ","; }
						i++;
					}
					i = 0;
					for (int id : quest.forgetQuests) {
						texts[1] += id;
						if (i < quest.forgetQuests.length - 1) { texts[1] += ","; }
						i++;
					}
					SubGuiEditText subgui = new SubGuiEditText(1, texts);
					subgui.setHoverTexts(new TextComponentTranslation("quest.hover.forget.dialogues"),
							new TextComponentTranslation("quest.hover.forget.quests"));
					setSubGui(subgui);
				}
				break;
			} // cancelable
			case 23: quest.level = button.getValue(); break; // level
			case 24: {
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("message.change.id", "" + quest.id).getFormattedText(),
						new TextComponentTranslation("message.change").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			} // reset ID
			case 66: onClosed(); break;
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		if (parent instanceof GuiNPCInterface2) {
			((GuiNPCInterface) parent).setSubGui(this);
			NoppesUtil.openGUI(player, parent);
		}
		else { NoppesUtil.openGUI(player, this); }
		if (!result) { return; }
		if (id == 0) { Client.sendData(EnumPacketServer.QuestMinID, quest.id); }
	}

	@Override
	public void initGui() {
		super.initGui();
		quest = NoppesUtilServer.getEditingQuest(player);
		quest.questInterface.fix();
		tasksData = quest.questInterface.getKeys();
		NoppesUtilServer.setEditingQuest(player, quest);
		// name and id
		addLabel(new GuiNpcLabel(1, "gui.title", guiLeft + 4, guiTop + 10));
		addTextField(new GuiNpcTextField(1, this, guiLeft + 45, guiTop + 5, 127, 20, quest.getName())
				.setHoverText("quest.hover.edit.quest.name"));
		addLabel(new GuiNpcLabel(0, "ID: " + quest.id, guiLeft + 175, guiTop + 5));
		addLabel(new GuiNpcLabel(2, "type.level", guiLeft + 175, guiTop + 15));
		// end text
		addLabel(new GuiNpcLabel(3, "quest.completedtext", guiLeft + 4, guiTop + 35));
		addButton(new GuiNpcButton(3, guiLeft + 120, guiTop + 30, 50, 20, quest.completeText.isEmpty() ? "selectServer.edit" : "advanced.editingmode")
				.setHoverText("quest.hover.edit.quest.completedtext"));
		// log text
		addLabel(new GuiNpcLabel(4, "quest.questlogtext", guiLeft + 4, guiTop + 57));
		addButton(new GuiNpcButton(4, guiLeft + 120, guiTop + 52, 50, 20, quest.logText.isEmpty() ? "selectServer.edit" : "advanced.editingmode")
				.setHoverText("quest.hover.edit.quest.questlogtext"));
		// reward
		addLabel(new GuiNpcLabel(5, "quest.reward", guiLeft + 4, guiTop + 79));
		addButton(new GuiNpcButton(5, guiLeft + 120, guiTop + 74, 50, 20, quest.rewardItems.isEmpty() && quest.rewardExp <= 0 ? "selectServer.edit" : "advanced.editingmode")
				.setHoverText("quest.hover.edit.quest.reward"));
		// tasks
		addLabel(getTasksLabel());
		if (scrollTasks == null) { scrollTasks = new GuiCustomScroll(this, 6).setSize(209, 94); }
		scrollTasks.setList(new ArrayList<>(tasksData.keySet()));
		scrollTasks.guiLeft = guiLeft + 172;
		scrollTasks.guiTop = guiTop + 96;
		int pos = -1;
		if (!task.isEmpty()) {
			scrollTasks.setSelected(task);
			pos = quest.questInterface.getPos(tasksData.get(task));
		}
		addScroll(scrollTasks);
		// task offset
		addButton(new GuiNpcButton(16, guiLeft + 346, guiTop + 74, 35, 20, "type.up", !task.isEmpty() && pos != 0)
				.setHoverText("quest.hover.edit.quest.up"));
		addButton(new GuiNpcButton(17, guiLeft + 308, guiTop + 74, 35, 20, "type.down", !task.isEmpty() && pos > -1 && pos < tasksData.size() - 1)
				.setHoverText("quest.hover.edit.quest.down"));
		addButton(new GuiNpcButton(18, guiLeft + 172, guiTop + 192, 51, 20, new String[] { "attribute.slot.0", "quest.task.step.1", "quest.task.step.2" }, quest.step)
				.setIsEnable(!tasksData.isEmpty())
				.setHoverText("quest.hover.edit.quest.step"));
		// task settings
		addButton(new GuiNpcButton(19, guiLeft + 225, guiTop + 192, 50, 20, "gui.add", tasksData.size() < 9)
				.setHoverText("quest.hover.edit.quest.add"));
		addButton(new GuiNpcButton(20, guiLeft + 278, guiTop + 192, 50, 20, "gui.remove", scrollTasks.getSelected() != null)
				.setHoverText("quest.hover.edit.quest.del"));
		addButton(new GuiNpcButton(21, guiLeft + 331, guiTop + 192, 50, 20, "selectServer.edit", !task.isEmpty())
				.setHoverText("quest.hover.edit.quest.edit"));
		// repeat
		addButton(new GuiButtonBiDirectional(8, guiLeft + 4, guiTop + 148, 166, 20, new String[] { "gui.no", "gui.yes", "quest.mcdaily", "quest.mcweekly", "quest.rldaily", "quest.rlweekly" }, quest.repeat.ordinal())
				.setHoverText("quest.hover.edit.quest.repeat"));
		// completion
		addButton(new GuiNpcButton(9, guiLeft + 172, guiTop + 30, 90, 20, "gui.extraoptions")
				.setLayerColor(new Color(0xFF00FFF0).getRGB())
				.setHoverText("quest.hover.edit.quest.extra"));
		if (quest.completer == null && npc != null) {
			quest.completer = npc;
			quest.completerPos[0] = (int) npc.posX;
			quest.completerPos[1] = (int) (npc.posY + 0.5d);
			quest.completerPos[2] = (int) npc.posZ;
			quest.completerPos[3] = npc.world.provider.getDimension();
		}
		// faction
		addLabel(new GuiNpcLabel(10, "faction.options", guiLeft + 4, guiTop + 101));
		addButton(new GuiNpcButton(10, guiLeft + 120, guiTop + 96, 50, 20, quest.factionOptions.hasOptions() ? "advanced.editingmode" : "selectServer.edit")
				.setHoverText("quest.hover.edit.quest.faction"));
		// next quest
		addButton(new GuiNpcButton(11, guiLeft + 4, guiTop + 192, 144, 20, "quest.next"));
		if (quest.nextQuest != -1) {
			if (!quest.nextQuestTitle.isEmpty()) { getButton(11).setDisplayText(quest.nextQuestTitle); }
			Quest q = QuestController.instance.quests.get(quest.nextQuest);
			String hover = "";
			if (q != null) {
				hover = "<br>" + ((char) 167) + "7" + new TextComponentTranslation(q.category.title).getFormattedText() + ((char) 167) + "7/" +
						((char) 167) + "r" + new TextComponentTranslation(q.title).getFormattedText();
			}
			getButton(11).setHoverText(new TextComponentTranslation("quest.hover.edit.quest.next").getFormattedText() + hover);
		}
		else { getButton(11).setHoverText("quest.hover.edit.quest.next"); }
		addButton(new GuiNpcButton(12, guiLeft + 150, guiTop + 192, 20, 20, "X")
				.setHoverText("quest.hover.edit.quest.del.next"));
		// mail
		addButton(new GuiNpcButton(13, guiLeft + 4, guiTop + 170, 144, 20, "mailbox.setup")
				.setHoverText("quest.hover.edit.quest.mail"));
		if (!quest.mail.title.isEmpty()) { getButton(13).setDisplayText(quest.mail.title); }
		addButton(new GuiNpcButton(14, guiLeft + 150, guiTop + 170, 20, 20, "X")
				.setHoverText("quest.hover.edit.quest.del.mail"));
		// command
		addLabel(new GuiNpcLabel(15, "advMode.command", guiLeft + 4, guiTop + 123));
		addButton(new GuiNpcButton(15, guiLeft + 120, guiTop + 118, 50, 20, quest.command.isEmpty() ? "selectServer.edit" : "advanced.editingmode")
				.setHoverText("quest.hover.edit.quest.command"));
		// cancelable
		GuiNpcLabel label = new GuiNpcLabel(16, "quest.has." + (quest.forgetDialogues.length > 0 || quest.forgetQuests.length > 0), guiLeft + 266, guiTop + 58);
		label.enabled = quest.isCancelable();
		addLabel(label);
		addButton(new GuiNpcButton(22, guiLeft + 172, guiTop + 52, 90, 20, new String[] { "quest.cancelable.true", "quest.cancelable.false" }, quest.isCancelable() ? 0 : 1)
				.setHoverText("quest.hover.edit.quest.cancelable"));
		// level
		String[] lvls = new String[CustomNpcs.MaxLv + 1];
		lvls[0] = "gui.none";
		for (int g = 1; g <= CustomNpcs.MaxLv; g++) { lvls[g] = "" + g; }
		addButton(new GuiButtonBiDirectional(23, guiLeft + 269, guiTop + 5, 50, 20, lvls, quest.level)
				.setHoverText("quest.hover.edit.quest.level"));
		// reset ID
		addButton(new GuiNpcButton(24, guiLeft + 217, guiTop + 5, 50, 20, "gui.reset")
				.setHoverText("hover.reset.id"));
		// exit
		addButton(new GuiNpcButton(66, guiLeft + 361, guiTop + 5, 20, 20, "X")
				.setHoverText("hover.back"));
	}

	private GuiNpcLabel getTasksLabel() {
		String[] isGet = new String[] {"2", "", ""};
		TreeMap<Integer, Dialog> dialogs = DialogController.instance.dialogs;
		for (int id : dialogs.keySet()) {
			if (dialogs.get(id).quest == quest.id) {
				isGet = new String[] { "0", "" + id, ((char) 167) + "8" + dialogs.get(id).category.title + "/" + ((char) 167) + "r" + dialogs.get(id).title };
				break;
			}
		}
		if (isGet[0].equals("2")) {
			TreeMap<Integer, Quest> quests = QuestController.instance.quests;
			for (int id : quests.keySet()) {
				if (id != quest.id && quests.get(id).nextQuest == quest.id) {
					isGet = new String[] { "1", "" + id, ((char) 167) + "8" + quests.get(id).category.title + "/" + ((char) 167) + "r" + quests.get(id).getTitle() };
					break;
				}
			}
		}
		GuiNpcLabel label = new GuiNpcLabel(6, new TextComponentTranslation("gui.tasks", ((char) 167) + (isGet[0].equals("2") ? "4" : "2") + ((char) 167) + "l[?]").getFormattedText(), guiLeft + 174, guiTop + 84);
		label.setHoverText("quest.hover.edit.quest.tasks", new TextComponentTranslation("quest.hover.edit.is.get." + isGet[0], isGet[1], isGet[2]).getFormattedText());
		return label;
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
		Client.sendData(EnumPacketServer.QuestSave, quest.category.id, quest.save(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getSelected() == null) { return; }
		if (scroll.getID() == 6) { task = scroll.getSelected(); }
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (scroll.getID() == 6) {
			if (task.isEmpty()) { return; }
			if (tasksData.get(task).getEnumType() == EnumQuestTask.DIALOG) {
				setSubGui(new SubGuiNpcQuestTypeDialog(npc, tasksData.get(task), this));
			}
			else if (tasksData.get(task).getEnumType() == EnumQuestTask.KILL) {
				setSubGui(new SubGuiNpcQuestTypeKill(npc, tasksData.get(task), this));
			}
			else if (tasksData.get(task).getEnumType() == EnumQuestTask.LOCATION) {
				setSubGui(new SubGuiNpcQuestTypeLocation(npc, tasksData.get(task), this));
			}
			else if (tasksData.get(task).getEnumType() == EnumQuestTask.AREAKILL) {
				setSubGui(new SubGuiNpcQuestTypeKill(npc, tasksData.get(task), this));
			}
			else if (tasksData.get(task).getEnumType() == EnumQuestTask.MANUAL) {
				setSubGui(new SubGuiNpcQuestTypeManual(npc, tasksData.get(task), this));
			}
			else { // Item or Craft
				Client.sendData(EnumPacketServer.QuestReset, quest.save(new NBTTagCompound()), quest.questInterface.getPos(tasksData.get(task)));
			}
		}
	}

	@Override
	public void selected(int id, String name) {
		quest.nextQuest = id;
		quest.nextQuestTitle = name;
		initGui();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound != null && compound.hasKey("MinimumID", 3) && quest.id != compound.getInteger("MinimumID")) {
			Client.sendData(EnumPacketServer.QuestRemove, quest.id);
			quest.id = compound.getInteger("MinimumID");
			Client.sendData(EnumPacketServer.QuestSave, quest.category.id, quest.save(new NBTTagCompound()));
			initGui();
		}
	}

	@Override
	public void subGuiClosed(GuiScreen subgui) {
		if (subgui instanceof SubGuiNpcTextArea) {
			SubGuiNpcTextArea gui = (SubGuiNpcTextArea) subgui;
			if (gui.getId() == 0) {
				quest.completeText = gui.text;
			} else if (gui.getId() == 1) {
				quest.logText = gui.text;
			}
		} else if (subgui instanceof SubGuiNpcCommand) {
			SubGuiNpcCommand sub = (SubGuiNpcCommand) subgui;
			quest.command = sub.command;
		} else if (subgui instanceof SubGuiEditText && ((SubGuiEditText) subgui).text.length == 2) {
			while (((SubGuiEditText) subgui).text[0].contains(" ")) {
				((SubGuiEditText) subgui).text[0] = ((SubGuiEditText) subgui).text[0].replace(" ", "");
			}
			while (((SubGuiEditText) subgui).text[1].contains(" ")) {
				((SubGuiEditText) subgui).text[1] = ((SubGuiEditText) subgui).text[1].replace(" ", "");
			}
			List<Integer> vdt = new ArrayList<>();
			for (String td : ((SubGuiEditText) subgui).text[0].split(",")) {
				try {
					int id = Integer.parseInt(td);
					if (!vdt.contains(id)) {
						vdt.add(id);
					}
				} catch (NumberFormatException e) { LogWriter.error(e); }
			}
			Collections.sort(vdt);
			quest.forgetDialogues = new int[vdt.size()];
			int i = 0;
			for (int id : vdt) {
				quest.forgetDialogues[i] = id;
				i++;
			}

			List<Integer> vqt = new ArrayList<>();
			for (String tq : ((SubGuiEditText) subgui).text[1].split(",")) {
				try {
					int id = Integer.parseInt(tq);
					if (!vqt.contains(id)) {
						vqt.add(id);
					}
				} catch (NumberFormatException e) { LogWriter.error(e); }
			}
			Collections.sort(vqt);
			quest.forgetQuests = new int[vqt.size()];
			i = 0;
			for (int id : vqt) {
				quest.forgetQuests[i] = id;
				i++;
			}
		}
		initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField guiNpcTextField) {
		if (guiNpcTextField.getID() == 1) {
			quest.setName(guiNpcTextField.getText());
			while (QuestController.instance.containsQuestName(quest.category, quest)) { quest.setName(quest.getName() + "_"); }
		}
		initGui();
	}

}
