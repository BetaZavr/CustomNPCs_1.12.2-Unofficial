package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestCategory;

public class QuestCategory
implements IQuestCategory {
	
	public int id;
	public final TreeMap<Integer, Quest> quests;
	public String title;

	public QuestCategory() {
		this.id = -1;
		this.title = "";
		this.quests = Maps.<Integer, Quest>newTreeMap();
	}

	@Override
	public IQuest create() {
		return new Quest(this);
	}

	@Override
	public String getName() {
		return this.title;
	}

	@Override
	public List<IQuest> quests() {
		return new ArrayList<IQuest>(this.quests.values());
	}

	public void readNBT(NBTTagCompound nbttagcompound) {
		this.id = nbttagcompound.getInteger("Slot");
		this.title = nbttagcompound.getString("Title");
		NBTTagList dialogsList = nbttagcompound.getTagList("Dialogs", 10);
		if (dialogsList != null) {
			for (int ii = 0; ii < dialogsList.tagCount(); ++ii) {
				NBTTagCompound nbttagcompound2 = dialogsList.getCompoundTagAt(ii);
				Quest quest = new Quest(this);
				quest.readNBT(nbttagcompound2);
				this.quests.put(quest.id, quest);
			}
		}
	}

	public NBTTagCompound writeNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("Slot", this.id);
		nbttagcompound.setString("Title", this.title);
		NBTTagList dialogs = new NBTTagList();
		for (int dialogId : this.quests.keySet()) {
			Quest quest = this.quests.get(dialogId);
			dialogs.appendTag(quest.writeToNBT(new NBTTagCompound()));
		}
		nbttagcompound.setTag("Dialogs", dialogs);
		return nbttagcompound;
	}

}
