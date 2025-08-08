package noppes.npcs.controllers.data;

import java.util.*;

import com.google.common.base.Predicate;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.*;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestCategory;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.constants.EnumQuestRepeat;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.constants.EnumRewardType;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestInterface;
import noppes.npcs.quests.QuestObjective;
import noppes.npcs.util.Util;
import noppes.npcs.util.ValueUtil;

public class Quest implements ICompatibilty, IQuest, Predicate<EntityNPCInterface> {

	public boolean cancelable = false;
	public boolean showProgressInChat = true;
	public boolean showProgressInWindow = true;
	public boolean showRewardText = true;
	public int id = -1;
	public int level = 0;
	public int nextQuest = -1;
	public int rewardExp = 0;
	public int rewardMoney = 0;
	public int step = 0;
	public int extraButton = 0;
	public int version = VersionCompatibility.ModRev;
	public int[] forgetDialogues = new int[0];
	public int[] forgetQuests = new int[0];
	public int[] completerPos = new int[] { 0, 0, 0, 0 };
	public String command = "";
	public String completeText = "";
	public String logText = "";
	public String nextQuestTitle = "";
	public String rewardText = "";
	public String title = "default";
	public String extraButtonText = "";
	public QuestCategory category;
	public FactionOptions factionOptions = new FactionOptions();
	public ResourceLocation icon = new ResourceLocation(CustomNpcs.MODID, "textures/quest icon/q_0.png");
	public ResourceLocation texture = null;
	public PlayerMail mail = new PlayerMail();
	public QuestInterface questInterface = new QuestInterface();
	public NpcMiscInventory rewardItems = new NpcMiscInventory(9);
	public EnumQuestRepeat repeat = EnumQuestRepeat.NONE;
	public EnumQuestCompletion completion = EnumQuestCompletion.Npc;
	public EnumRewardType rewardType = EnumRewardType.RANDOM_ONE;
	public EntityNPCInterface completer = null;
	private UUID completerUUID = null;

	public Quest(QuestCategory categoryIn) { category = categoryIn; }

	@Override
	public IQuestObjective addTask() {
		return questInterface.addTask(EnumQuestTask.ITEM);
	}

	@Override
	public boolean apply(EntityNPCInterface entity) {
		return completerUUID == null || entity.getUniqueID().equals(completerUUID);
	}

	public boolean complete(EntityPlayer player, QuestData data) {
		if (completion == EnumQuestCompletion.Instant) {
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.QUEST_COMPLETION, data.quest.id);
			return true;
		}
		return false;
	}

	public Quest copy() {
		Quest quest = new Quest(category);
		quest.load(save(new NBTTagCompound()));
		return quest;
	}

	@Override
	public IQuestCategory getCategory() {
		return category;
	}

	@Override
	public ICustomNpc<?> getCompleterNpc() {
		if (completer == null) {
			return null;
		}
		return (ICustomNpc<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(completer);
	}

	@Override
	public String getCompleteText() {
		return completeText;
	}

	@Override
	public int getExtraButton() {
		return extraButton;
	}

	@Override
	public String getExtraButtonText() {
		return extraButtonText;
	}

	@Override
	public int[] getForgetDialogues() {
		return forgetDialogues;
	}

	@Override
	public int[] getForgetQuests() {
		return forgetQuests;
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public boolean getIsRepeatable() {
		return repeat != EnumQuestRepeat.NONE;
	}

	public String getKey() {
		char c = ((char) 167);
		return c + "7ID:" + id + c + "8" + category.title + "/" + c + "7 \"" + c + "5" + getTitle() + c + "7\"";
	}

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public String getLogText() {
		StringBuilder allTextLogs = new StringBuilder();
		String ent = "" + ((char) 10);
		Map<ItemStack, Integer> rewardMap = new LinkedHashMap<>();
		for (int i = 0; i < rewardItems.getSizeInventory(); i++) {
			ItemStack item = rewardItems.getStackInSlot(i);
			if (item.isEmpty()) { continue; }
			boolean has = false;
			if (rewardType == EnumRewardType.ALL) {
				for (ItemStack it : rewardMap.keySet()) {
					if (item.isItemEqual(it) && ItemStack.areItemStackShareTagsEqual(item, it)) {
                        rewardMap.compute(it, (k, c) -> c == null ? item.getCount() : c + item.getCount());
						has = true;
						break;
					}
				}
			}
			if (!has) {
				rewardMap.put(item, item.getCount());
			}
		}
		if (showRewardText) {
			if (!rewardMap.isEmpty() || rewardExp > 0 || rewardMoney > 0 || !rewardText.isEmpty()) {
				allTextLogs.append(ent).append(ent).append(new TextComponentTranslation("questlog.reward").getFormattedText());
			}
			if (!rewardMap.isEmpty()) {
				allTextLogs.append(ent).append(new TextComponentTranslation("questlog." + (rewardType == EnumRewardType.ONE_SELECT ? "one" : rewardType == EnumRewardType.RANDOM_ONE ? "rnd" : "all") + ".reward").getFormattedText());
				int j = 1;
				for (ItemStack item : rewardMap.keySet()) {
					int c = rewardMap.get(item);
					allTextLogs.append(ent).append(rewardMap.size() > 1 ? j + " - " : "").append(" ").append((char) 0xffff).append(" ").append(item.getDisplayName()).append(c > 1 ? " x" + c : "");
					j++;
				}
			}
			if (rewardMoney > 0) {
				allTextLogs.append(ent).append(new TextComponentTranslation("questlog.rewardmoney",
						Util.instance.getTextReducedNumber(rewardMoney, true, true, false),
						CustomNpcs.displayCurrencies).getFormattedText());
			}
			if (rewardExp > 0) {
				allTextLogs.append(ent).append(new TextComponentTranslation("questlog.rewardexp", "" + rewardExp).getFormattedText());
			}
        }
        if (!rewardText.isEmpty()) {
            allTextLogs.append(ent).append(rewardText.contains("%") ? rewardText : new TextComponentTranslation(rewardText).getFormattedText());
        }
        if (!logText.isEmpty()) {
            allTextLogs.append(ent).append(ent).append((char) 167).append("l").append(new TextComponentTranslation("gui.description").getFormattedText()).append(ent).append(logText.contains("%") ? logText : new TextComponentTranslation(logText).getFormattedText());
        }
        return allTextLogs.toString();
	}

	@Override
	public String getName() {
		return title;
	}

	@Override
	public Quest getNextQuest() {
		return (QuestController.instance == null) ? null : QuestController.instance.quests.get(nextQuest);
	}

	public QuestObjective[] getObjectives(EntityPlayer player) {
		if (player == null) {
			return new QuestObjective[0];
		}
		PlayerData data = PlayerData.get(player);
		if (data == null || !data.questData.activeQuests.containsKey(id)) {
			return new QuestObjective[0];
		}
		return questInterface.getObjectives(player);
	}

	@Override
	public IQuestObjective[] getObjectives(IPlayer<?> player) {
		if (!player.hasActiveQuest(id)) {
			throw new CustomNPCsException("Player doesnt have this quest active.");
		}
		return questInterface.getObjectives(player.getMCEntity());
	}

	@Override
	public IContainer getRewards() {
		return Objects.requireNonNull(NpcAPI.Instance()).getIContainer(rewardItems);
	}

	@Override
	public int getRewardType() {
		return rewardType.ordinal();
	}

	@Override
	public String getTitle() {
		String key = "";
		if (level > 0) {
			String chr = "" + ((char) 167);
			key = chr + (level <= CustomNpcs.MaxLv / 3 ? "2"
					: (float) level <= (float) CustomNpcs.MaxLv / 1.5f ? "e" : "c");
			key += level + chr + "7 Lv.: " + chr + "r";
		}
		key += new TextComponentTranslation(title).getFormattedText();
		return key;
	}

	@Override
	public int getVersion() {
		return version;
	}

	public boolean hasCompassSettings() {
		for (QuestObjective task : questInterface.tasks) {
			if (task.rangeCompass > 3 && task.pos.getX() != 0 && task.pos.getY() != 0 && task.pos.getZ() != 0) {
				return true;
			}
		}
		return false;
	}

	public boolean hasNewQuest() {
		return getNextQuest() != null;
	}

	@Override
	public boolean isCancelable() {
		return cancelable;
	}

	@Override
	public boolean isSetUp() {
		if (questInterface.tasks.length == 0) {
			return false;
		}
		for (QuestObjective task : questInterface.tasks) {
			if ((task.getEnumType() == EnumQuestTask.ITEM || task.getEnumType() == EnumQuestTask.CRAFT)) {
				if (task.getItemStack().isEmpty()) {
					return false;
				}
			} else if (task.getEnumType() == EnumQuestTask.DIALOG) {
				if (DialogController.instance.dialogs.get(task.getTargetID()) == null) {
					return false;
				}
			}
        }
		return true;
	}

	public void load(NBTTagCompound compound) {
		id = compound.getInteger("Id");
		loadPartial(compound);
	}

	public void loadPartial(NBTTagCompound compound) {
		version = compound.getInteger("ModRev");
		VersionCompatibility.CheckAvailabilityCompatibility(this, compound);
		title = compound.getString("Title");
		logText = compound.getString("Text");
		completeText = compound.getString("CompleteText");
		command = compound.getString("QuestCommand");
		nextQuest = compound.getInteger("NextQuestId");
		rewardExp = compound.getInteger("RewardExp");
		rewardItems.load(compound.getCompoundTag("Rewards"));
		completion = EnumQuestCompletion.values()[compound.getInteger("QuestCompletion")];
		repeat = EnumQuestRepeat.values()[compound.getInteger("QuestRepeat")];
		questInterface.readEntityFromNBT(compound, id);
		factionOptions.load(compound.getCompoundTag("QuestFactionPoints"));
		mail.readNBT(compound.getCompoundTag("QuestMail"));

		rewardType = EnumRewardType.values()[compound.getInteger("RewardType")];
		rewardMoney = compound.getInteger("RewardMoney");
		nextQuestTitle = compound.getString("NextQuestTitle");
		if (hasNewQuest()) {
			nextQuestTitle = getNextQuest().title;
		} else {
			nextQuestTitle = "";
		}
		if (compound.hasKey("QuestIcon", 8)) {
			icon = new ResourceLocation(compound.getString("QuestIcon"));
		} else {
			icon = new ResourceLocation(CustomNpcs.MODID, "textures/quest icon/q_0.png");
		}
		if (compound.hasKey("QuestTexture", 8)) {
			texture = new ResourceLocation(compound.getString("QuestTexture"));
		} else {
			texture = null;
		}
		extraButtonText = compound.getString("ExtraButtonText");
		level = compound.getInteger("QuestLevel");
		cancelable = compound.getBoolean("Cancelable");
		if (compound.hasKey("ShowProgressInChat", 1)) {
			showProgressInChat = compound.getBoolean("ShowProgressInChat");
		}
		if (compound.hasKey("ShowProgressInWindow", 1)) {
			showProgressInWindow = compound.getBoolean("ShowProgressInWindow");
		}
		if (compound.hasKey("ShowRewardText", 1)) {
			showRewardText = compound.getBoolean("ShowRewardText");
		}
		setExtraButton(compound.getInteger("ExtraButton"));
		rewardText = compound.getString("AddRewardText");
		step = compound.getInteger("Step") % 3;
		if (step < 0) {
			step *= -1;
		}
		forgetDialogues = compound.getIntArray("ForgetDialogues");
		forgetQuests = compound.getIntArray("ForgetQuests");
		completer = null;
		completerUUID = null;
		if (compound.hasKey("CompleterPos", 11)) {
			completerPos = compound.getIntArray("CompleterPos");
		}
		try {
			String name = compound.getCompoundTag("CompleterNpc").getString("Name");
			if (compound.hasKey("CompleterNpc", 8)) { name = compound.getString("CompleterNpc"); } // OLD
			else if (compound.hasKey("CompleterNpc", 10) &&
					compound.getCompoundTag("CompleterNpc").hasKey("UUIDMost", 4) &&
					compound.getCompoundTag("CompleterNpc").hasKey("UUIDLeast", 4)) {
					completerUUID = compound.getCompoundTag("CompleterNpc").getUniqueId("UUID");
			}
			World[] worlds = new World[0];
			if (CustomNpcs.Server != null) { worlds = CustomNpcs.Server.worlds; }
			else if (CustomNpcs.proxy.getPlayer() != null) { worlds = new World[] { CustomNpcs.proxy.getPlayer().world }; }
			for (World world : worlds) {
				for (EntityNPCInterface entity : world.getEntities(EntityNPCInterface.class, this)) {
					if (entity.getName().equals(name)) {
						completer = entity;
						if (completerUUID == null) { completerUUID = entity.getUniqueID(); }
						break;
					}
				}
				if (completer != null) { break; }
			}
			if (completer == null && worlds.length > 0 && worlds[0] != null) {
				Entity e = EntityList.createEntityFromNBT(compound.getCompoundTag("CompleterNpc"), worlds[0]);
				if (e instanceof EntityNPCInterface) {
					completer = (EntityNPCInterface) e;
					completerUUID = e.getUniqueID();
				}
			}
		} catch (Throwable t) { LogWriter.error("Error: ", t); }
	}

	@Override
	public boolean removeTask(IQuestObjective task) {
		return questInterface.removeTask((QuestObjective) task);
	}

	@Override
	public void save() {
		QuestController.instance.saveQuest(category, this);
	}

	@SideOnly(Side.SERVER)
	@Override
	public void sendChangeToAll() {
		Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, EnumSync.QuestData, save(new NBTTagCompound()), category.id);
	}

	@Override
	public void setCancelable(boolean cancelableIn) { cancelable = cancelableIn; }

	@Override
	public void setCompleterNpc(ICustomNpc<?> npc) {
		completer = (EntityNPCInterface) npc.getMCEntity();
	}

	@Override
	public void setCompleteText(String text) {
		completeText = text;
	}

	@Override
	public void setExtraButton(int type) {
		if (type < 0) { type *= -1; }
		extraButton = type % 6;
	}

	@Override
	public void setExtraButtonText(String hover) {
		extraButtonText = hover == null ? "" : hover;
	}

	@Override
	public void setForgetDialogues(int[] forget) {
		forgetDialogues = forget;
	}

	@Override
	public void setForgetQuests(int[] forget) {
		forgetQuests = forget;
	}

	@Override
	public void setLevel(int levelIn) {
		if (levelIn < 0 ) { levelIn *= -1; }
		level = ValueUtil.correctInt(levelIn, 1, CustomNpcs.MaxLv);
	}

	@Override
	public void setLogText(String text) {
		logText = text;
	}

	@Override
	public void setName(String name) {
		title = name;
	}

	@Override
	public void setNextQuest(IQuest quest) {
		if (quest == null) {
			nextQuest = -1;
			nextQuestTitle = "";
		} else {
			if (quest.getId() < 0) { throw new CustomNPCsException("Quest id is lower than 0"); }
			nextQuest = quest.getId();
			nextQuestTitle = quest.getTitle();
		}
	}

	@Override
	public void setRewardText(String text) {
		rewardText = text;
	}

	@Override
	public void setRewardType(int type) {
		if (type < 0 || type >= EnumRewardType.values().length) {
			return;
		}
		rewardType = EnumRewardType.values()[type];
	}

	@Override
	public void setVersion(int versionIn) {
		version = versionIn;
	}

	@Override
	public NBTTagCompound save(NBTTagCompound compound) {
		compound.setInteger("Id", id);
		return saveToPartial(compound);
	}

	public NBTTagCompound saveToPartial(NBTTagCompound compound) {
		compound.setInteger("ModRev", version);
		compound.setString("Title", title);
		compound.setString("Text", logText);
		compound.setString("CompleteText", completeText);
		compound.setInteger("NextQuestId", nextQuest);
		compound.setInteger("RewardExp", rewardExp);
		compound.setTag("Rewards", rewardItems.save());
		compound.setString("QuestCommand", command);
		compound.setInteger("QuestCompletion", completion.ordinal());
		compound.setInteger("QuestRepeat", repeat.ordinal());
		questInterface.writeEntityToNBT(compound);
		compound.setTag("QuestFactionPoints", factionOptions.save(new NBTTagCompound()));
		compound.setTag("QuestMail", mail.writeNBT());

		compound.setString("NextQuestTitle", nextQuestTitle);
		compound.setInteger("RewardMoney", rewardMoney);
		compound.setString("QuestIcon", icon.toString());
		if (texture != null) { compound.setString("QuestTexture", texture.toString()); }
		compound.setInteger("RewardType", rewardType.ordinal());
		compound.setInteger("QuestLevel", level);
		compound.setBoolean("Cancelable", cancelable);
		compound.setBoolean("ShowProgressInChat", showProgressInChat);
		compound.setBoolean("ShowProgressInWindow", showProgressInWindow);
		compound.setBoolean("ShowRewardText", showRewardText);
		compound.setString("ExtraButtonText", extraButtonText);
		compound.setInteger("ExtraButton", extraButton);
		compound.setString("AddRewardText", rewardText);
		compound.setInteger("Step", step);
		compound.setIntArray("ForgetDialogues", forgetDialogues);
		compound.setIntArray("ForgetQuests", forgetQuests);

		if (completer != null) {
			NBTTagCompound npcNbt = new NBTTagCompound();
			completer.writeToNBTOptional(npcNbt);
			compound.setTag("CompleterNpc", npcNbt);
			if (completerPos[0] == 0 && completerPos[1] == 0 && completerPos[2] == 0 && completerPos[3] == 0) {
				completerPos[0] = (int) completer.posX;
				completerPos[1] = (int) (completer.posY + 0.5d);
				completerPos[2] = (int) completer.posZ;
				completerPos[3] = completer.world.provider.getDimension();
			}
		}
		compound.setIntArray("CompleterPos", completerPos);

		return compound;
	}

}
