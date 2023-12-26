package noppes.npcs.controllers;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestRepeat;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.quests.QuestObjective;

public class PlayerQuestController {

	public static void addActiveQuest(Quest quest, EntityPlayer player) {
		PlayerData playerdata = PlayerData.get(player);
		PlayerQuestData data = playerdata.questData;
		LogWriter.debug("AddActiveQuest: " + quest.getTitle() + "; data: " + data);
		if (playerdata.scriptData.getPlayer().canQuestBeAccepted(quest.id)) {
			if (EventHooks.onQuestStarted(playerdata.scriptData, quest)) { return; }
			data.activeQuests.put(quest.id, new QuestData(quest));
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.MESSAGE, "quest.newquest", quest.getTitle(), 2);
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.CHAT, "quest.newquest", ": ", quest.getTitle());
			playerdata.updateClient = true;
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
		Vector<Quest> quests = new Vector<Quest>();
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
		if (!data.activeQuests.containsKey(id)) {
			return false;
		}
		HashMap<Integer, QuestData> newData = new HashMap<Integer, QuestData>();
		boolean del = false;
		for (int qid : data.activeQuests.keySet()) {
			if (qid == id) {
				del = true;
				Quest quest = QuestController.instance.quests.get(id);
				if (quest.forgetDialogues.length > 0) {
					for (int dialogId : quest.forgetDialogues) {
						playerdata.dialogData.dialogsRead.remove(dialogId);
					}
				}
				if (quest.forgetQuests.length > 0) {
					for (int questId : quest.forgetQuests) {
						playerdata.questData.finishedQuests.remove(questId);
					}
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

	public static boolean hasActiveQuests(EntityPlayer player) {
		PlayerQuestData data = PlayerData.get(player).questData;
		return !data.activeQuests.isEmpty();
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
