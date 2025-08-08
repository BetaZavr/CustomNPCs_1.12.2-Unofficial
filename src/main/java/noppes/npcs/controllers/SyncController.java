package noppes.npcs.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NBTTags;
import noppes.npcs.Server;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.config.ConfigLoader;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.util.BuilderData;

public class SyncController {

	public static final Map<Integer, BuilderData> dataBuilder = new HashMap<>();
	
	// SYNC_ADD or SYNC_END
	@SuppressWarnings("unchecked")
	public static void add(EnumSync synctype, NBTTagCompound compound, boolean syncEnd, EntityPlayer player) {
		switch (synctype) {
			case FactionsData: {
				NBTTagList list = compound.getTagList("Data", 10);
				for (int i = 0; i < list.tagCount(); ++i) {
					Faction faction = new Faction();
					faction.load(list.getCompoundTagAt(i));
					FactionController.instance.factionsSync.put(faction.id, faction);
				}
				if (syncEnd) {
					FactionController fData = FactionController.instance;
					fData.factions.clear();
					for (int id : fData.factionsSync.keySet()) {
						fData.factions.put(id, fData.factionsSync.get(id));
					}
					fData.factionsSync.clear();
				}
				break;
			}
			case QuestCategoriesData: {
				if (!compound.getKeySet().isEmpty()) {
					QuestCategory category;
					if (QuestController.instance.categoriesSync.containsKey(compound.getInteger("Slot"))) {
						category = QuestController.instance.categoriesSync.get(compound.getInteger("Slot"));
					}
					else { category = new QuestCategory(); }
					category.load(compound);
					QuestController.instance.categoriesSync.put(category.id, category);
				}
				if (syncEnd) {
					QuestController qData = QuestController.instance;
					TreeMap<Integer, Quest> quests = new TreeMap<>();
					for (QuestCategory category : qData.categoriesSync.values()) {
						for (Quest quest : category.quests.values()) { quests.put(quest.id, quest); }
					}
					qData.categories.clear();
					qData.categories.putAll(qData.categoriesSync);
					qData.quests.clear();
					qData.quests.putAll(quests);
					qData.categoriesSync.clear();
				}
				break;
			}
			case DialogCategoriesData: {
				if (!compound.getKeySet().isEmpty()) {
					DialogCategory category;
					if (DialogController.instance.categoriesSync.containsKey(compound.getInteger("Slot"))) {
						category = DialogController.instance.categoriesSync.get(compound.getInteger("Slot"));
					}
					else { category = new DialogCategory(); }
					category.load(compound);
					DialogController.instance.categoriesSync.put(category.id, category);
				}
				if (syncEnd) {
					DialogController dData = DialogController.instance;
					TreeMap<Integer, Dialog> dialogs = new TreeMap<>();
					for (DialogCategory category4 : dData.categoriesSync.values()) {
						for (Dialog dialog : category4.dialogs.values()) { dialogs.put(dialog.id, dialog); }
					}
					dData.categories.clear();
					dData.categories.putAll(dData.categoriesSync);
					dData.dialogs.clear();
					dData.dialogs.putAll(dialogs);
					dData.categoriesSync.clear();
				}
				break;
			}
			case DialogGuiSettings: {
				DialogController.instance.getGuiSettings().load(compound);
				break;
			}
			case ModData: {
				if (!syncEnd) { return; }
				ConfigLoader.load(compound);
				break;
			}
			case KeysData: {
				KeyController.getInstance().loadKeys(compound);
				CustomNpcs.proxy.updateKeys();
				break;
			}
			case TransportData: {
				TransportController.getInstance().loadCategories(compound);
				break;
			}
			case Debug: {
				List<String> list = CustomNpcs.debugData.logging();
				for (String str : list) {
					player.sendMessage(new TextComponentString(str));
				}
				try {
					Class<?> nirn = Class.forName("nirn.betazavr.Nirn");
					Object nirnMod = nirn.getField("instance").get(null);
					List<String> nirnList = (List<String>) nirn.getMethod("showDebugs").invoke(nirnMod);
					for (String str : nirnList) {
						player.sendMessage(new TextComponentString(str));
					}
				}
				catch (Exception ignored) { }
				break;
			}
			default: break;
		}
	}

	// SYNC_REMOVE
	public static void remove(EnumSync synctype, int id, ByteBuf buffer) {
		switch (synctype) {
			case FactionsData: {
				FactionController.instance.factions.remove(id);
				break;
			}
			case QuestData: {
				Quest quest = QuestController.instance.quests.remove(id);
				if (quest != null) {
					quest.category.quests.remove(id);
				}
				break;
			}
			case QuestCategoriesData: {
				QuestCategory category = QuestController.instance.categories.remove(id);
				if (category != null) { QuestController.instance.quests.keySet().removeAll(category.quests.keySet()); }
				break;
			}
			case DialogData: {
				Dialog dialog = DialogController.instance.dialogs.remove(id);
				if (dialog != null) {
					dialog.category.dialogs.remove(id);
				}
				break;
			}
			case DialogCategoriesData: {
				DialogCategory category = DialogController.instance.categories.remove(id);
				if (category != null) {
					DialogController.instance.dialogs.keySet().removeAll(category.dialogs.keySet());
				}
				break;
			}
			case RecipesData: {
				RecipeController.getInstance().delete(id);
				break;
			}
			case KeysData: {
				KeyController.getInstance().removeKeySetting(id);
				CustomNpcs.proxy.updateKeys();
				break;
			}
			case MarcetData: {
				MarcetController.getInstance().markets.remove(id);
				break;
			}
			case MarcetDeal: {
				MarcetController.getInstance().deals.remove(id);
				break;
			}
			case BankData: {
				BankController.getInstance().banks.remove(id);
				break;
			}
			case BankCeil: {
				Bank bank = BankController.getInstance().banks.get(buffer.readInt());
				if (bank != null) {
					bank.removeCeil(id);
				}
				break;
			}
			case Debug: {
				CustomNpcs.debugData.clear();
				break;
			}
			default: {
				break;
			}
		}
	}

	// SYNC_UPDATE
	public static void update(EnumSync synctype, NBTTagCompound compound, ByteBuf buffer, EntityPlayer player) {
		switch (synctype) {
			case FactionsData: {
				Faction faction = new Faction();
				faction.load(compound);
				FactionController.instance.factions.put(faction.id, faction);
				break;
			}
			case QuestData: {
				int id = compound.getInteger("Id");
				if (QuestController.instance.quests.containsKey(id)) {
					Quest quest = QuestController.instance.quests.get(id);
					quest.load(compound);
				} else {
					QuestCategory category = QuestController.instance.categories.get(buffer.readInt());
					Quest quest = new Quest(category);
					quest.load(compound);
					QuestController.instance.quests.put(quest.id, quest);
					category.quests.put(quest.id, quest);
				}
				for (Quest q : PlayerQuestController.getActiveQuests(player)) {
					if (q.id != id) {
						continue;
					}
					q.load(compound);
				}
				break;
			}
			case QuestCategoriesData: {
				QuestCategory category = new QuestCategory();
				category.load(compound);
				QuestController.instance.categories.put(category.id, category);
				break;
			}
			case DialogData: {
				if (DialogController.instance.dialogs.containsKey(compound.getInteger("DialogId"))) {
					Dialog dialog = DialogController.instance.dialogs.get(compound.getInteger("DialogId"));
					dialog.load(compound);
				} else {
					DialogCategory category = DialogController.instance.categories.get(buffer.readInt());
					Dialog dialog = new Dialog(category);
					dialog.load(compound);
					DialogController.instance.dialogs.put(dialog.id, dialog);
					category.dialogs.put(dialog.id, dialog);
				}
				break;
			}
			case DialogCategoriesData: {
				DialogCategory category = new DialogCategory();
				category.load(compound);
				DialogController.instance.categories.put(category.id, category);
				break;
			}
			case DialogGuiSettings: {
				DialogController.instance.getGuiSettings().load(compound);
				break;
			}
			case RecipesData: {
				if (compound.getKeySet().isEmpty()) {
					if (CustomNpcs.Server != null && CustomNpcs.Server.isSinglePlayer()) { return; }
					RecipeController.getInstance().clear();
				} else {
					RecipeController.getInstance().loadNBTRecipe(compound);

				}
				break;
			}
			case AnimationData: {
				if (compound.getKeySet().isEmpty()) {
					if (CustomNpcs.Server != null && CustomNpcs.Server.isSinglePlayer()) {
						return;
					}
					AnimationController.getInstance().animations.clear();
					AnimationController.getInstance().emotions.clear();
				} else if (compound.hasKey("delete", 1) && compound.getBoolean("delete")) {
					AnimationController.getInstance().removeAnimation(compound.getInteger("ID"));
				} else {
					AnimationController.getInstance().loadAnimation(compound);
				}
				break;
			}
			case EmotionData: {
				if (compound.getKeySet().isEmpty()) {
					if (CustomNpcs.Server != null && CustomNpcs.Server.isSinglePlayer()) {
						return;
					}
					AnimationController.getInstance().emotions.clear();
				} else if (compound.hasKey("delete", 1) && compound.getBoolean("delete")) {
					AnimationController.getInstance().removeEmotion(compound.getInteger("ID"));
				} else {
					AnimationController.getInstance().loadEmotion(compound);
				}
				break;
			}
			case PlayerGameData: {
				ClientProxy.playerData.game.readFromNBT(compound);
				CustomNpcs.proxy.updateGUI();
				break;
			}
			case PlayerQuestData: {
				ClientProxy.playerData.questData.loadNBTData(compound);
				CustomNpcs.proxy.updateGUI();
				break;
			}
			case KeysData: {
				KeyController.getInstance().loadKey(compound);
				CustomNpcs.proxy.updateKeys();
				break;
			}
			case BankData: {
				Bank bank = BankController.getInstance().getBank(compound.getInteger("BankID"));
				if (bank != null) {
					bank.readFromNBT(compound);
				} else {
					bank = new Bank();
					bank.readFromNBT(compound);
					BankController.getInstance().banks.put(bank.id, bank);
				}
				break;
			}
			case GameData: {
				PlayerData data = PlayerData.get(player);
				if (data != null) {
					data.game.readFromNBT(compound);
				}
				break;
			}
			case MailData: {
				if (compound.hasKey("MailData", 9)) {
					PlayerData data = PlayerData.get(player);
					if (data != null) {
						data.mailData.loadNBTData(compound);
					}
				}
				if (compound.hasKey("LettersBeDeleted", 3)) {
					CustomNpcs.MailTimeWhenLettersWillBeDeleted = compound.getInteger("LettersBeDeleted");
				}
				if (compound.hasKey("LettersBeReceived", 11)) {
					int[] vs = compound.getIntArray("LettersBeReceived");
					System.arraycopy(vs, 0, CustomNpcs.MailTimeWhenLettersWillBeReceived, 0, vs.length);
				}
				if (compound.hasKey("CostSendingLetter", 11)) {
					int[] vs = compound.getIntArray("CostSendingLetter");
					System.arraycopy(vs, 0, CustomNpcs.MailCostSendingLetter, 0, vs.length);
				}
				if (compound.hasKey("SendToYourself", 1)) {
					CustomNpcs.MailSendToYourself = compound.getBoolean("SendToYourself");
				}
				break;
			}
			case BuilderData: {
				BuilderData builder;
				if (SyncController.dataBuilder.containsKey(compound.getInteger("ID"))) { builder = SyncController.dataBuilder.get(compound.getInteger("ID")); }
				else { builder = new BuilderData(compound.getInteger("ID"), compound.getInteger("BuilderType")); }
				builder.read(compound);
				break;
			}
			case Debug: {
				CustomNpcs.VerboseDebug = compound.getBoolean("debug");
				break;
			}
			default: {
				break;
			}
		}
	}

	public static void syncAllDialogs(MinecraftServer server) {
		for (DialogCategory category : DialogController.instance.categories.values()) {
			Server.sendToAll(server, EnumPacketClient.SYNC_ADD, EnumSync.DialogCategoriesData, category.save(new NBTTagCompound()));
		}
		Server.sendToAll(server, EnumPacketClient.SYNC_END, EnumSync.DialogCategoriesData, new NBTTagCompound());
		Server.sendToAll(server, EnumPacketClient.SYNC_UPDATE, EnumSync.DialogGuiSettings, DialogController.instance.getGuiSettings().save());
	}

	public static void syncAllQuests(MinecraftServer server) {
		for (QuestCategory category : QuestController.instance.categories.values()) {
			Server.sendToAll(server, EnumPacketClient.SYNC_ADD, EnumSync.QuestCategoriesData, category.save(new NBTTagCompound()));
		}
		Server.sendToAll(server, EnumPacketClient.SYNC_END, EnumSync.QuestCategoriesData, new NBTTagCompound());
	}

	public static void syncPlayer(EntityPlayerMP player) {
		CustomNpcs.debugData.start(player);
		NBTTagList list = new NBTTagList();
		NBTTagCompound compound;
		for (Faction faction : FactionController.instance.factions.values()) {
			list.appendTag(faction.save(new NBTTagCompound()));
			if (list.tagCount() > 20) {
				compound = new NBTTagCompound();
				compound.setTag("Data", list);
				Server.sendData(player, EnumPacketClient.SYNC_ADD, EnumSync.FactionsData, compound);
				list = new NBTTagList();
			}
		}
		compound = new NBTTagCompound();
		compound.setTag("Data", list);
		Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.FactionsData, compound);

		for (QuestCategory category : QuestController.instance.categories.values()) {
			Server.sendData(player, EnumPacketClient.SYNC_ADD, EnumSync.QuestCategoriesData, category.save(new NBTTagCompound()));
		}
		Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.QuestCategoriesData, new NBTTagCompound());

		for (DialogCategory category : DialogController.instance.categories.values()) {
			Server.sendData(player, EnumPacketClient.SYNC_ADD, EnumSync.DialogCategoriesData, category.save(new NBTTagCompound()));
		}
		Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.DialogCategoriesData, new NBTTagCompound());
		Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.DialogGuiSettings, DialogController.instance.getGuiSettings().save());

		AnimationController.getInstance().sendTo(player);

		ConfigLoader.sendTo(player);

		PlayerData data = PlayerData.get(player);
		if (player.getServer() != null) {
            data.game.op = player.getServer().getPlayerList().canSendCommands(player.getGameProfile());
        }
		compound = data.getNBT();
		Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.PlayerData, compound);

		Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.KeysData, KeyController.getInstance().getNBT());

		syncScriptItems(player);
		syncScriptRecipes(player);

		BorderController.getInstance().sendTo(player);
		MarcetController.getInstance().sendTo(player, -1);
		ScriptController.Instance.sendClientTo(player);
		CustomNpcs.debugData.end(player);
	}

	private static void syncScriptRecipes(EntityPlayerMP player) {
		RecipeController.getInstance().sendTo(player);
		player.unlockRecipes(RecipeController.getInstance().getKnownRecipes());
	}

	public static void syncScriptItems(EntityPlayerMP player) {
		NBTTagCompound comp = new NBTTagCompound();
		comp.setTag("List", NBTTags.nbtIntegerStringMap(ItemScripted.Resources));
		Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.ItemScriptedModels, comp);
	}

	public static void syncScriptItemsEverybody() {
		NBTTagCompound comp = new NBTTagCompound();
		comp.setTag("List", NBTTags.nbtIntegerStringMap(ItemScripted.Resources));
		for (EntityPlayerMP player : CustomNpcs.Server.getPlayerList().getPlayers()) {
			Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.ItemScriptedModels, comp);
		}
	}

}
