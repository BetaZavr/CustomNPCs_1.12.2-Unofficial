package noppes.npcs.controllers.data;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.EventHooks;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestInterface;

public class PlayerQuestData {

	public HashMap<Integer, QuestData> activeQuests = new HashMap<Integer, QuestData>(); // [qID, data]
	public HashMap<Integer, Long> finishedQuests = new HashMap<Integer, Long>(); // [qID, time]
	public boolean updateClient; // ServerTickHandler.onPlayerTick() 114

	public PlayerQuestData() {
	}

	public boolean checkQuestCompletion(EntityPlayer player, QuestData data) {
		QuestInterface inter = data.quest.questInterface;
		if (inter.isCompleted(player)) {
			if (data.isCompleted && data.quest.completion == EnumQuestCompletion.Npc) { return false; }
			if (!data.quest.complete(player, data)) {
				Server.sendData((EntityPlayerMP) player, EnumPacketClient.MESSAGE, "quest.completed",
						data.quest.getTitle(), 2);
				Server.sendData((EntityPlayerMP) player, EnumPacketClient.CHAT, "quest.completed", ": ",
						data.quest.getTitle());
			}
			data.isCompleted = true;
			this.updateClient = true;
			EventHooks.onQuestFinished(PlayerData.get(player).scriptData, data.quest);
			return true;
		}
		return false;
	}

	public QuestData getQuestCompletion(EntityPlayer player, EntityNPCInterface npc) {
		for (QuestData data : this.activeQuests.values()) {
			Quest quest = data.quest;
			if (quest != null && quest.completion == EnumQuestCompletion.Npc
					&& quest.completer.getName().equals(npc.getName()) && quest.questInterface.isCompleted(player)) {
				return data;
			}
		}
		return null;
	}

	public void loadNBTData(NBTTagCompound mainCompound) { // Changed
		if (mainCompound == null) {
			return;
		}
		NBTTagCompound compound = mainCompound.getCompoundTag("QuestData");

		HashMap<Integer, Long> finishedMap = new HashMap<Integer, Long>();
		if (compound.hasKey("CompletedQuests", 9) && compound.getTagList("CompletedQuests", 10).tagCount() > 0) {
			for (int i = 0; i < compound.getTagList("CompletedQuests", 10).tagCount(); ++i) {
				NBTTagCompound dataNBT = compound.getTagList("CompletedQuests", 10).getCompoundTagAt(i);
				finishedMap.put(dataNBT.getInteger("Quest"), dataNBT.getLong("Date"));
			}
		}
		this.finishedQuests = finishedMap;

		HashMap<Integer, QuestData> activeMap = new HashMap<Integer, QuestData>();
		if (compound.hasKey("ActiveQuests", 9) && compound.getTagList("ActiveQuests", 10).tagCount() > 0) {
			for (int i = 0; i < compound.getTagList("ActiveQuests", 10).tagCount(); ++i) {
				NBTTagCompound dataNBT = compound.getTagList("ActiveQuests", 10).getCompoundTagAt(i);
				int id = dataNBT.getInteger("Quest");
				Quest quest = QuestController.instance.quests.get(id);
				if (quest != null) {
					QuestData data = new QuestData(quest);
					data.readEntityFromNBT(dataNBT);
					activeMap.put(id, data);
				}
			}
		}
		this.activeQuests = activeMap;

	}

	public void saveNBTData(NBTTagCompound maincompound) { // Changed
		NBTTagCompound compound = new NBTTagCompound();

		NBTTagList finishedList = new NBTTagList();
		for (int quest : this.finishedQuests.keySet()) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("Quest", quest);
			nbttagcompound.setLong("Date", (long) this.finishedQuests.get(quest));
			finishedList.appendTag(nbttagcompound);
		}
		compound.setTag("CompletedQuests", finishedList);

		NBTTagList activeList = new NBTTagList();
		for (int id : this.activeQuests.keySet()) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("Quest", id);
			this.activeQuests.get(id).writeEntityToNBT(nbt);
			activeList.appendTag(nbt);
		}
		compound.setTag("ActiveQuests", activeList);

		maincompound.setTag("QuestData", compound);
	}

}
