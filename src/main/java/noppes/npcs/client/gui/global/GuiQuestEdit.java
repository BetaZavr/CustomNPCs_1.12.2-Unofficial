package noppes.npcs.client.gui.global;

import java.awt.*;
import java.util.*;
import java.util.List;

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
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeDialog;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeKill;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeLocation;
import noppes.npcs.client.gui.questtypes.GuiNpcQuestTypeManual;
import noppes.npcs.client.gui.select.GuiQuestSelection;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
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

public class GuiQuestEdit
extends SubGuiInterface
implements ICustomScrollListener, ISubGuiListener, GuiSelectionListener, ITextfieldListener, IGuiData, GuiYesNoCallback {

    private Quest quest;
	private GuiCustomScroll scrollTasks;
	private String task = "";
	private Map<String, QuestObjective> tasksData;

	public GuiQuestEdit(Quest q) {
		super();
		setBackground("menubg.png");
		xSize = 386;
		ySize = 226;
		closeOnEsc = true;

		quest = q;
		NoppesUtilServer.setEditingQuest(player, q);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 3: { // end text
				this.setSubGui(new SubGuiNpcTextArea(0, this.quest.completeText));
				break;
			}
			case 4: { // log text
				this.setSubGui(new SubGuiNpcTextArea(1, this.quest.logText));
				break;
			}
			case 5: { // reward
				Client.sendData(EnumPacketServer.QuestOpenGui, EnumGuiType.QuestReward,
						this.quest.writeToNBT(new NBTTagCompound()));
				break;
			}
			case 8: { // reiteration
				this.quest.repeat = EnumQuestRepeat.values()[button.getValue()];
				break;
			}
			case 9: { // NPC to End
				this.setSubGui(new SubGuiNpcQuestExtra(0, this.quest));
				break;
			}
			case 10: { // faction
				this.setSubGui(new SubGuiNpcFactionOptions(this.quest.factionOptions));
				break;
			}
			case 11: { // next quest
				this.setSubGui(new GuiQuestSelection(this.quest.nextQuest));
				break;
			}
			case 12: { // remove next quest
				this.quest.nextQuest = -1;
				this.initGui();
				break;
			}
			case 13: { // mail
				this.setSubGui(new SubGuiMailmanSendSetup(this.quest.mail));
				break;
			}
			case 14: { // remove mail
				this.quest.mail = new PlayerMail();
				this.initGui();
				break;
			}
			case 15: { // command
				this.setSubGui(new SubGuiNpcCommand(this.quest.command));
				break;
			}
			case 16: { // up
				this.quest.questInterface.upPos(this.tasksData.get(this.scrollTasks.getSelected()));
				Client.sendData(EnumPacketServer.QuestSave, this.quest.category.id,
						this.quest.writeToNBT(new NBTTagCompound()));
				this.initGui();
				break;
			}
			case 17: { // down
				this.quest.questInterface.downPos(this.tasksData.get(this.scrollTasks.getSelected()));
				Client.sendData(EnumPacketServer.QuestSave, this.quest.category.id,
						this.quest.writeToNBT(new NBTTagCompound()));
				this.initGui();
				break;
			}
			case 18: { // type step task
				this.quest.step = button.getValue();
				break;
			}
			case 19: { // add task
				this.setSubGui(new SubGuiQuestObjectiveSelect(this));
				break;
			}
			case 20: { // remove task

				if (this.quest.questInterface.removeTask(this.tasksData.get(this.task))) {
					this.task = "";
					Client.sendData(EnumPacketServer.QuestSave, this.quest.category.id,
							this.quest.writeToNBT(new NBTTagCompound()));
					this.initGui();
				}
				break;
			}
			case 21: { // edit task
				if (this.task.isEmpty() || !this.tasksData.containsKey(this.task)) {
					return;
				}
				if (this.tasksData.get(this.task).getEnumType() == EnumQuestTask.DIALOG) {
					this.setSubGui(new GuiNpcQuestTypeDialog(this.npc, this.tasksData.get(this.task), this));
				} else if (this.tasksData.get(this.task).getEnumType() == EnumQuestTask.KILL) {
					this.setSubGui(new GuiNpcQuestTypeKill(this.npc, this.tasksData.get(this.task), this));
				} else if (this.tasksData.get(this.task).getEnumType() == EnumQuestTask.LOCATION) {
					this.setSubGui(new GuiNpcQuestTypeLocation(this.npc, this.tasksData.get(this.task), this));
				} else if (this.tasksData.get(this.task).getEnumType() == EnumQuestTask.AREAKILL) {
					this.setSubGui(new GuiNpcQuestTypeKill(this.npc, this.tasksData.get(this.task), this));
				} else if (this.tasksData.get(this.task).getEnumType() == EnumQuestTask.MANUAL) {
					this.setSubGui(new GuiNpcQuestTypeManual(this.npc, this.tasksData.get(this.task), this));
				} else { // Item or Craft
					Client.sendData(EnumPacketServer.QuestReset, this.quest.writeToNBT(new NBTTagCompound()),
							this.quest.questInterface.getPos(this.tasksData.get(this.task)));
				}
				break;
			}
			case 22: { // cancelable
				this.quest.setCancelable(button.getValue() == 0);
				this.getLabel(16).enabled = this.quest.isCancelable();
				if (this.quest.isCancelable()) {
					if (this.quest.forgetDialogues.length == 0) {
						TreeMap<Integer, Dialog> dialogs = DialogController.instance.dialogs;
						for (int id : dialogs.keySet()) {
							if (dialogs.get(id).quest == this.quest.id) {
								this.quest.forgetDialogues = new int[] { id };
								break;
							}
						}
					}
					if (this.quest.forgetQuests.length == 0) {
						TreeMap<Integer, Quest> quests = QuestController.instance.quests;
						for (int id : quests.keySet()) {
							if (id != this.quest.id && quests.get(id).nextQuest == this.quest.id) {
								this.quest.forgetQuests = new int[] { id };
								break;
							}
						}
					}
					String[] texts = new String[] { "", "" };
					int i = 0;
					for (int id : this.quest.forgetDialogues) {
						texts[0] += id;
						if (i < this.quest.forgetDialogues.length - 1) {
							texts[0] += ",";
						}
						i++;
					}
					i = 0;
					for (int id : this.quest.forgetQuests) {
						texts[1] += id;
						if (i < this.quest.forgetQuests.length - 1) {
							texts[1] += ",";
						}
						i++;
					}
					SubGuiEditText subgui = new SubGuiEditText(1, texts);
					subgui.setHoverTexts(new String[] { "quest.hover.forget.dialogues", "quest.hover.forget.quests" });
					this.setSubGui(subgui);
				}
				break;
			}
			case 23: { // level
				this.quest.level = button.getValue();
				break;
			}
			case 24: { // reset ID
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("message.change.id", "" + this.quest.id).getFormattedText(),
						new TextComponentTranslation("message.change").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			}
			case 66: { // exit
				this.close();
				break;
			}
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		if (this.parent instanceof GuiNPCInterface2) {
			((GuiNPCInterface) this.parent).setSubGui(this);
			NoppesUtil.openGUI(this.player, this.parent);
		} else {
			NoppesUtil.openGUI(this.player, this);
		}
		if (!result) {
			return;
		}
		if (id == 0) {
			Client.sendData(EnumPacketServer.QuestMinID, this.quest.id);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.quest = NoppesUtilServer.getEditingQuest(this.player);
		this.quest.questInterface.fix();
		this.tasksData = this.quest.questInterface.getKeys();
		NoppesUtilServer.setEditingQuest(this.player, this.quest);
		// name and id
		this.addLabel(new GuiNpcLabel(1, "gui.title", this.guiLeft + 4, this.guiTop + 10));
		GuiNpcTextField textField = new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 45, this.guiTop + 5, 127, 20, this.quest.getName());
		textField.setHoverText("quest.hover.edit.quest.name");
		addTextField(textField);
		this.addLabel(new GuiNpcLabel(0, "ID: " + this.quest.id, this.guiLeft + 175, this.guiTop + 5));
		this.addLabel(new GuiNpcLabel(2, "type.level", this.guiLeft + 175, this.guiTop + 15));
		// end text
		this.addLabel(new GuiNpcLabel(3, "quest.completedtext", this.guiLeft + 4, this.guiTop + 35));
		GuiNpcButton button = new GuiNpcButton(3, this.guiLeft + 120, this.guiTop + 30, 50, 20, this.quest.completeText.isEmpty() ? "selectServer.edit" : "advanced.editingmode");
		button.setHoverText("quest.hover.edit.quest.completedtext");
		addButton(button);
		// log text
		this.addLabel(new GuiNpcLabel(4, "quest.questlogtext", this.guiLeft + 4, this.guiTop + 57));
		button = new GuiNpcButton(4, this.guiLeft + 120, this.guiTop + 52, 50, 20, this.quest.logText.isEmpty() ? "selectServer.edit" : "advanced.editingmode");
		button.setHoverText("quest.hover.edit.quest.questlogtext");
		addButton(button);
		// reward
		this.addLabel(new GuiNpcLabel(5, "quest.reward", this.guiLeft + 4, this.guiTop + 79));
		button = new GuiNpcButton(5, this.guiLeft + 120, this.guiTop + 74, 50, 20, this.quest.rewardItems.isEmpty() && this.quest.rewardExp <= 0 ? "selectServer.edit" : "advanced.editingmode");
		button.setHoverText("quest.hover.edit.quest.reward");
		addButton(button);
		// tasks
		addLabel(getTasksLabel());
		if (this.scrollTasks == null) { (this.scrollTasks = new GuiCustomScroll(this, 6)).setSize(209, 94); }
		this.scrollTasks.setList(new ArrayList<>(tasksData.keySet()));
		this.scrollTasks.guiLeft = this.guiLeft + 172;
		this.scrollTasks.guiTop = this.guiTop + 96;
		int pos = -1;
		if (!this.task.isEmpty()) {
			this.scrollTasks.setSelected(this.task);
			pos = this.quest.questInterface.getPos(this.tasksData.get(this.task));
		}
		this.addScroll(this.scrollTasks);
		// task offset
		button = new GuiNpcButton(16, this.guiLeft + 346, this.guiTop + 74, 35, 20, "type.up", !this.task.isEmpty() && pos != 0);
		button.setHoverText("quest.hover.edit.quest.up");
		addButton(button);
		button = new GuiNpcButton(17, this.guiLeft + 308, this.guiTop + 74, 35, 20, "type.down", !this.task.isEmpty() && pos > -1 && pos < this.tasksData.size() - 1);
		button.setHoverText("quest.hover.edit.quest.down");
		addButton(button);
		button = new GuiNpcButton(18, this.guiLeft + 172, this.guiTop + 192, 51, 20, new String[] { "attribute.slot.0", "quest.task.step.1", "quest.task.step.2" }, this.quest.step);
		button.setEnabled(!this.tasksData.isEmpty());
		button.setHoverText("quest.hover.edit.quest.step");
		addButton(button);
		// task settings
		button = new GuiNpcButton(19, this.guiLeft + 225, this.guiTop + 192, 50, 20, "gui.add", this.tasksData.size() < 9);
		button.setHoverText("quest.hover.edit.quest.add");
		addButton(button);
		button = new GuiNpcButton(20, this.guiLeft + 278, this.guiTop + 192, 50, 20, "gui.remove", this.scrollTasks.getSelected() != null);
		button.setHoverText("quest.hover.edit.quest.del");
		addButton(button);
		button = new GuiNpcButton(21, this.guiLeft + 331, this.guiTop + 192, 50, 20, "selectServer.edit", !this.task.isEmpty());
		button.setHoverText("quest.hover.edit.quest.edit");
		addButton(button);
		// repeat
		button = new GuiButtonBiDirectional(8, this.guiLeft + 4, this.guiTop + 148, 166, 20, new String[] { "gui.no", "gui.yes", "quest.mcdaily", "quest.mcweekly", "quest.rldaily", "quest.rlweekly" }, this.quest.repeat.ordinal());
		button.setHoverText("quest.hover.edit.quest.repeat");
		addButton(button);
		// completion
		button = new GuiNpcButton(9, this.guiLeft + 172, this.guiTop + 30, 90, 20, "gui.extraoptions");
		button.layerColor = new Color(0xFF00FFF0).getRGB();
		button.setHoverText("quest.hover.edit.quest.extra");
		addButton(button);
		if (this.quest.completer == null && npc != null) {
			this.quest.completer = this.npc;
			this.quest.completerPos[0] = (int) this.npc.posX;
			this.quest.completerPos[1] = (int) (this.npc.posY + 0.5d);
			this.quest.completerPos[2] = (int) this.npc.posZ;
			this.quest.completerPos[3] = this.npc.world.provider.getDimension();
		}
		// faction
		this.addLabel(new GuiNpcLabel(10, "faction.options", this.guiLeft + 4, this.guiTop + 101));
		button = new GuiNpcButton(10, this.guiLeft + 120, this.guiTop + 96, 50, 20, this.quest.factionOptions.hasOptions() ? "advanced.editingmode" : "selectServer.edit");
		button.setHoverText("quest.hover.edit.quest.faction");
		addButton(button);
		// next quest
		button = new GuiNpcButton(11, this.guiLeft + 4, this.guiTop + 192, 144, 20, "quest.next");
		if (quest.nextQuest != -1) {
			if (!quest.nextQuestTitle.isEmpty()) { button.setDisplayText(quest.nextQuestTitle); }
			Quest q = QuestController.instance.quests.get(quest.nextQuest);
			String hover = "";
			if (q != null) {
				hover = "<br>" + ((char) 167) + "7" + new TextComponentTranslation(q.category.title).getFormattedText() + ((char) 167) + "7/" +
						((char) 167) + "r" + new TextComponentTranslation(q.title).getFormattedText();
			}
			button.setHoverText(new TextComponentTranslation("quest.hover.edit.quest.next").getFormattedText() + hover);
		}
		else { button.setHoverText("quest.hover.edit.quest.next"); }
		addButton(button);
		button = new GuiNpcButton(12, this.guiLeft + 150, this.guiTop + 192, 20, 20, "X");
		button.setHoverText("quest.hover.edit.quest.del.next");
		addButton(button);
		// mail
		button = new GuiNpcButton(13, this.guiLeft + 4, this.guiTop + 170, 144, 20, "mailbox.setup");
		if (!this.quest.mail.title.isEmpty()) { button.setDisplayText(this.quest.mail.title); }
		button.setHoverText("quest.hover.edit.quest.mail");
		addButton(button);
		button = new GuiNpcButton(14, this.guiLeft + 150, this.guiTop + 170, 20, 20, "X");
		button.setHoverText("quest.hover.edit.quest.del.mail");
		addButton(button);
		// command
		this.addLabel(new GuiNpcLabel(15, "advMode.command", this.guiLeft + 4, this.guiTop + 123));
		button = new GuiNpcButton(15, this.guiLeft + 120, this.guiTop + 118, 50, 20, this.quest.command.isEmpty() ? "selectServer.edit" : "advanced.editingmode");
		button.setHoverText("quest.hover.edit.quest.command");
		addButton(button);
		// cancelable
		GuiNpcLabel label = new GuiNpcLabel(16, "quest.has." + (this.quest.forgetDialogues.length > 0 || this.quest.forgetQuests.length > 0), this.guiLeft + 266, this.guiTop + 58);
		label.enabled = this.quest.isCancelable();
		addLabel(label);
		button = new GuiNpcButton(22, this.guiLeft + 172, this.guiTop + 52, 90, 20, new String[] { "quest.cancelable.true", "quest.cancelable.false" }, this.quest.isCancelable() ? 0 : 1);
		button.setHoverText("quest.hover.edit.quest.cancelable");
		addButton(button);
		// level
		String[] lvls = new String[CustomNpcs.MaxLv + 1];
		lvls[0] = "gui.none";
		for (int g = 1; g <= CustomNpcs.MaxLv; g++) { lvls[g] = "" + g; }
		button = new GuiButtonBiDirectional(23, this.guiLeft + 269, this.guiTop + 5, 50, 20, lvls, this.quest.level);
		button.setHoverText("quest.hover.edit.quest.level");
		addButton(button);
		// reset ID
		button = new GuiNpcButton(24, this.guiLeft + 217, this.guiTop + 5, 50, 20, "gui.reset");
		button.setHoverText("hover.reset.id");
		addButton(button);
		// exit
		button = new GuiNpcButton(66, this.guiLeft + 361, this.guiTop + 5, 20, 20, "X");
		button.setHoverText("hover.back");
		addButton(button);
	}

	private GuiNpcLabel getTasksLabel() {
		String[] isGet = new String[] {"2", "", ""};
		TreeMap<Integer, Dialog> dialogs = DialogController.instance.dialogs;
		for (int id : dialogs.keySet()) {
			if (dialogs.get(id).quest == this.quest.id) {
				isGet = new String[] { "0", "" + id, ((char) 167) + "8" + dialogs.get(id).category.title + "/" + ((char) 167) + "r" + dialogs.get(id).title };
				break;
			}
		}
		if (isGet[0].equals("2")) {
			TreeMap<Integer, Quest> quests = QuestController.instance.quests;
			for (int id : quests.keySet()) {
				if (id != this.quest.id && quests.get(id).nextQuest == this.quest.id) {
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
		Client.sendData(EnumPacketServer.QuestSave, this.quest.category.id,
				this.quest.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getSelected() == null) {
			return;
		}
		if (scroll.id == 6) {
			this.task = scroll.getSelected();
		}
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (scroll.id == 6) {
			if (this.task.isEmpty()) {
				return;
			}
			if (this.tasksData.get(this.task).getEnumType() == EnumQuestTask.DIALOG) {
				this.setSubGui(new GuiNpcQuestTypeDialog(this.npc, this.tasksData.get(this.task), this));
			} else if (this.tasksData.get(this.task).getEnumType() == EnumQuestTask.KILL) {
				this.setSubGui(new GuiNpcQuestTypeKill(this.npc, this.tasksData.get(this.task), this));
			} else if (this.tasksData.get(this.task).getEnumType() == EnumQuestTask.LOCATION) {
				this.setSubGui(new GuiNpcQuestTypeLocation(this.npc, this.tasksData.get(this.task), this));
			} else if (this.tasksData.get(this.task).getEnumType() == EnumQuestTask.AREAKILL) {
				this.setSubGui(new GuiNpcQuestTypeKill(this.npc, this.tasksData.get(this.task), this));
			} else if (this.tasksData.get(this.task).getEnumType() == EnumQuestTask.MANUAL) {
				this.setSubGui(new GuiNpcQuestTypeManual(this.npc, this.tasksData.get(this.task), this));
			} else { // Item or Craft
				Client.sendData(EnumPacketServer.QuestReset, this.quest.writeToNBT(new NBTTagCompound()),
						this.quest.questInterface.getPos(this.tasksData.get(this.task)));
			}
		}
	}

	@Override
	public void selected(int id, String name) {
		this.quest.nextQuest = id;
		this.quest.nextQuestTitle = name;
		this.initGui();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound != null && compound.hasKey("MinimumID", 3) && this.quest.id != compound.getInteger("MinimumID")) {
			Client.sendData(EnumPacketServer.QuestRemove, this.quest.id);
			this.quest.id = compound.getInteger("MinimumID");
			Client.sendData(EnumPacketServer.QuestSave, this.quest.category.id,
					this.quest.writeToNBT(new NBTTagCompound()));
			this.initGui();
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcTextArea) {
			SubGuiNpcTextArea gui = (SubGuiNpcTextArea) subgui;
			if (gui.getId() == 0) {
				this.quest.completeText = gui.text;
			} else if (gui.getId() == 1) {
				this.quest.logText = gui.text;
			}
		} else if (subgui instanceof SubGuiNpcCommand) {
			SubGuiNpcCommand sub = (SubGuiNpcCommand) subgui;
			this.quest.command = sub.command;
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
				} catch (NumberFormatException e) { LogWriter.error("Error:", e); }
			}
			Collections.sort(vdt);
			this.quest.forgetDialogues = new int[vdt.size()];
			int i = 0;
			for (int id : vdt) {
				this.quest.forgetDialogues[i] = id;
				i++;
			}

			List<Integer> vqt = new ArrayList<>();
			for (String tq : ((SubGuiEditText) subgui).text[1].split(",")) {
				try {
					int id = Integer.parseInt(tq);
					if (!vqt.contains(id)) {
						vqt.add(id);
					}
				} catch (NumberFormatException e) { LogWriter.error("Error:", e); }
			}
			Collections.sort(vqt);
			this.quest.forgetQuests = new int[vqt.size()];
			i = 0;
			for (int id : vqt) {
				this.quest.forgetQuests[i] = id;
				i++;
			}
		}
		this.initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField guiNpcTextField) {
		if (guiNpcTextField.getId() == 1) {
			this.quest.setName(guiNpcTextField.getText());
			while (QuestController.instance.containsQuestName(this.quest.category, this.quest)) {
				StringBuilder sb = new StringBuilder();
				Quest quest = this.quest;
				quest.setName(sb.append(quest.getName()).append("_").toString());
			}
		}
		this.initGui();
	}

}
