package noppes.npcs.controllers.data;

import java.util.TreeMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestCategory;

public class QuestCategory implements IQuestCategory {

	public final TreeMap<Integer, Quest> quests = new TreeMap<>();
	public int id = -1;
	public String title = "";

	@Override
	public IQuest create() {
		return new Quest(this);
	}

	@Override
	public String getName() {
		return new TextComponentTranslation(title).getFormattedText();
	}

	@Override
	public IQuest[] quests() {
		return quests.values().toArray(new IQuest[0]);
	}

	public void load(NBTTagCompound nbttagcompound) {
		id = nbttagcompound.getInteger("Slot");
		title = nbttagcompound.getString("Title");
		NBTTagList dialogsList = nbttagcompound.getTagList("Dialogs", 10);
        for (int ii = 0; ii < dialogsList.tagCount(); ++ii) {
            NBTTagCompound compound = dialogsList.getCompoundTagAt(ii);
            Quest quest = new Quest(this);
            quest.load(compound);
            quests.put(quest.id, quest);
        }
    }

	public NBTTagCompound save(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("Slot", id);
		nbttagcompound.setString("Title", title);
		NBTTagList dialogs = new NBTTagList();
		for (int dialogId : quests.keySet()) {
			Quest quest = quests.get(dialogId);
			dialogs.appendTag(quest.save(new NBTTagCompound()));
		}
		nbttagcompound.setTag("Dialogs", dialogs);
		return nbttagcompound;
	}

	public QuestCategory copy() {
		QuestCategory newCat = new QuestCategory();
		newCat.load(save(new NBTTagCompound()));
		return newCat;
	}

}
