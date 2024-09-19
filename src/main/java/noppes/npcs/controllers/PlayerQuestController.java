package noppes.npcs.controllers;

import java.util.HashMap;
import java.util.Objects;
import java.util.Vector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestRepeat;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.data.MiniMapData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.quests.QuestObjective;
import noppes.npcs.util.Util;

public class PlayerQuestController {

	public static void addActiveQuest(Quest quest, EntityPlayer player, boolean skipBeAccepted) {
		PlayerData playerdata = PlayerData.get(player);
		PlayerQuestData data = playerdata.questData;
		LogWriter.debug("AddActiveQuest: " + quest.getTitle() + "; skipAccepted: " + skipBeAccepted);
		if (skipBeAccepted || playerdata.scriptData.getPlayer().canQuestBeAccepted(quest.id)) {
			if (EventHooks.onQuestStarted(playerdata.scriptData, quest)) {
				return;
			}
			data.activeQuests.put(quest.id, new QuestData(quest));
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.MESSAGE, "quest.newquest", quest.getTitle(), 2);
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.CHAT, "quest.newquest", ": ", quest.getTitle());
			playerdata.updateClient = true;
			if (player == null) {
				return;
			}
			int taskId = 0;
			for (IQuestObjective obj : quest.getObjectives((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player))) {
				if (obj.getType() == EnumQuestTask.ITEM.ordinal()) {
					playerdata.questData.checkQuestCompletion(player, playerdata.questData.activeQuests.get(quest.id));
				}
				if (obj.isSetPointOnMiniMap() && !playerdata.minimap.modName.equals("non")) {
					String name = quest.getTitle() + "_";
					if (obj.getType() == EnumQuestTask.ITEM.ordinal() || obj.getType() == EnumQuestTask.CRAFT.ordinal()) {
						name += obj.getItem().getDisplayName();
					}
					if (obj.getType() == EnumQuestTask.DIALOG.ordinal()) {
						IDialog d = DialogController.instance.get(obj.getTargetID());
						if (d != null) {
							name += d.getName();
						} else {
							name += obj.getTargetName();
						}
					} else {
						name += obj.getTargetName();
					}
					MiniMapData mmd = playerdata.minimap.getQuestTask(quest.id, taskId, name, obj.getCompassDimension());
					if (mmd == null) {
						mmd = (MiniMapData) playerdata.minimap.addPoint(obj.getCompassDimension());
					}
					mmd.setName(Util.instance.deleteColor(name));
					mmd.setPos(obj.getCompassPos());
					mmd.setQuestId(quest.id);
					mmd.setTaskId(taskId);
				}
				taskId++;
			}
		}
	}

	public static boolean canQuestBeAccepted(EntityPlayer player, int questId) {
		Quest quest = QuestController.instance.quests.get(questId);
		if (quest == null) {
			return false;
		}
		PlayerQuestData data = PlayerData.get(player).questData;
		if (data.activeQuests.containsKey(quest.id)) {
			return false;
		}
		if (!data.finishedQuests.containsKey(quest.id) || quest.repeat == EnumQuestRepeat.REPEATABLE) {
			return true;
		}
		if (quest.repeat == EnumQuestRepeat.NONE) {
			return false;
		}
		long questTime = data.finishedQuests.get(quest.id);
		if (quest.repeat == EnumQuestRepeat.MCDAILY) {
			return player.world.getTotalWorldTime() - questTime >= 24000L;
		}
		if (quest.repeat == EnumQuestRepeat.MCWEEKLY) {
			return player.world.getTotalWorldTime() - questTime >= 168000L;
		}
		if (quest.repeat == EnumQuestRepeat.RLDAILY) {
			return System.currentTimeMillis() - questTime >= 86400000L;
		}
		return quest.repeat == EnumQuestRepeat.RLWEEKLY && System.currentTimeMillis() - questTime >= 604800000L;
	}

	public static Vector<Quest> getActiveQuests(EntityPlayer player) {
		Vector<Quest> quests = new Vector<>();
		PlayerQuestData data = PlayerData.get(player).questData;
		for (QuestData questdata : data.activeQuests.values()) {
			if (questdata.quest == null) {
				continue;
			}
			quests.add(questdata.quest);
		}
		return quests;
	}

	public static boolean getRemoveActiveQuest(EntityPlayer player, int id) {
		PlayerData playerdata = PlayerData.get(player);
		PlayerQuestData data = playerdata.questData;
		playerdata.minimap.removeQuestPoints(id);
		if (!data.activeQuests.containsKey(id)) { return false; }
		HashMap<Integer, QuestData> newData = new HashMap<>();
		boolean del = false;
		for (int qid : data.activeQuests.keySet()) {
			if (qid == id) {
				del = true;
				Quest quest = QuestController.instance.quests.get(id);
                for (int dialogId : quest.forgetDialogues) {
                    playerdata.dialogData.dialogsRead.remove(dialogId);
                }
                for (int questId : quest.forgetQuests) {
                    playerdata.questData.finishedQuests.remove(questId);
                }
                continue;
			}
			newData.put(qid, data.activeQuests.get(qid));
		}
		if (del) {
			playerdata.questData.activeQuests = newData;
			playerdata.updateClient = true;
		}
		return del;
	}

	public static boolean isQuestActive(EntityPlayer player, int quest) {
		PlayerQuestData data = PlayerData.get(player).questData;
		return data.activeQuests.containsKey(quest);
	}

	public static boolean isQuestCompleted(EntityPlayer player, int quest) {
		PlayerQuestData data = PlayerData.get(player).questData;
		QuestData q = data.activeQuests.get(quest);
		return q != null && q.isCompleted;
	}

	public static boolean isQuestFinished(EntityPlayer player, int questid) {
		PlayerQuestData data = PlayerData.get(player).questData;
		return data.finishedQuests.containsKey(questid);
	}

	public static void setQuestFinished(Quest quest, EntityPlayer player) {
		PlayerData playerdata = PlayerData.get(player);
		PlayerQuestData data = playerdata.questData;
		data.activeQuests.remove(quest.id);
		playerdata.minimap.removeQuestPoints(quest.id);
		if (quest.repeat == EnumQuestRepeat.RLDAILY || quest.repeat == EnumQuestRepeat.RLWEEKLY) {
			data.finishedQuests.put(quest.id, System.currentTimeMillis());
		} else {
			data.finishedQuests.put(quest.id, player.world.getTotalWorldTime());
		}
		if (quest.repeat != EnumQuestRepeat.NONE) { // Change
			for (IQuestObjective obj : quest.questInterface.getObjectives(player)) { // forget dialogues
				if (((QuestObjective) obj).getEnumType() != EnumQuestTask.DIALOG) {
					continue;
				}
				playerdata.dialogData.dialogsRead.remove(obj.getTargetID());
			}
			for (int dID : quest.forgetDialogues) {
				playerdata.dialogData.dialogsRead.remove(dID);
			}
			for (int qID : quest.forgetQuests) {
				playerdata.questData.finishedQuests.remove(qID);
			}
		}
		playerdata.updateClient = true;
	}

}
