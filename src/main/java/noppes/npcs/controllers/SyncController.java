package noppes.npcs.controllers;

import java.util.List;
import java.util.TreeMap;

import com.google.common.collect.Maps;

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

public class SyncController {
	
	// SYNC_ADD or SYNC_END
	public static void add(EnumSync synctype, NBTTagCompound compound, boolean syncEnd, EntityPlayer player) {
		switch (synctype) {
			case FactionsData: {
				NBTTagList list = compound.getTagList("Data", 10);
				for (int i = 0; i < list.tagCount(); ++i) {
					Faction faction = new Faction();
					faction.readNBT(list.getCompoundTagAt(i));
					FactionController.instance.factionsSync.put(faction.id, faction);
				}
				if (syncEnd) {
					FactionController fData = FactionController.instance;
					fData.factions.clear();
					for (int id : fData.factionsSync.keySet()) { fData.factions.put(new Integer(id), fData.factionsSync.get(id)); }
					fData.factionsSync.clear();
				}
				break;
			}
			case QuestCategoriesData: {
				if (compound.getKeySet().size() > 0) {
					QuestCategory category = new QuestCategory();
					category.readNBT(compound);
					QuestController.instance.categoriesSync.put(category.id, category);
				}
				if (syncEnd) {
					QuestController qData = QuestController.instance;
					TreeMap<Integer, Quest> quests = Maps.<Integer, Quest>newTreeMap();
					for (QuestCategory category2 : qData.categoriesSync.values()) {
						for (Quest quest : category2.quests.values()) {
							quests.put(quest.id, quest);
						}
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
				if (compound.getKeySet().size() > 0) {
					DialogCategory category3 = new DialogCategory();
					category3.readNBT(compound);
					DialogController.instance.categoriesSync.put(category3.id, category3);
				}
				if (syncEnd) {
					DialogController dData = DialogController.instance;
					TreeMap<Integer, Dialog> dialogs = Maps.<Integer, Dialog>newTreeMap();
					for (DialogCategory category4 : dData.categoriesSync.values()) {
						for (Dialog dialog : category4.dialogs.values()) {
							dialogs.put(dialog.id, dialog);
						}
					}
					dData.categories.clear();
					dData.categories.putAll(dData.categoriesSync);
					dData.dialogs.clear();
					dData.dialogs.putAll(dialogs);
					dData.categoriesSync.clear();
				}
				break;
			}
			case RecipesData: {
				CustomNpcs.proxy.updateRecipes(null, false, false, "SyncController.clientSync()");
				break;
			}
			case ModData: {
				if (!syncEnd) {
					return;
				}
				if (compound.hasKey("ShowLR")) { CustomNpcs.showLR = compound.getBoolean("ShowLR"); }
				if (compound.hasKey("ShowMoney")) { CustomNpcs.showMoney = compound.getBoolean("ShowMoney"); }

				CustomNpcs.showServerQuestCompass = compound.getBoolean("ShowQuestCompass");
				if (CustomNpcs.showQuestCompass && !CustomNpcs.showServerQuestCompass) { CustomNpcs.showQuestCompass = false; }
				
				CustomNpcs.recalculateLR = compound.getBoolean("RecalculateLR");
				CustomNpcs.charCurrencies = compound.getString("CharCurrencies");
				CustomNpcs.maxBuilderBlocks = compound.getInteger("MaxBuilderBlocks");
				CustomNpcs.maxItemInDropsNPC = compound.getInteger("MaxItemInDropsNPC");
				CustomNpcs.scriptMaxTabs = compound.getInteger("ScriptMaxTabs");
				CustomNpcs.dialogShowFitsSpeed = compound.getInteger("DialogFitsSpeed");
				CustomNpcs.mailTimeWhenLettersWillBeDeleted = compound.getInteger("LettersBeDeleted");
				int[] vs = compound.getIntArray("LettersBeReceived");
				for (int i = 0; i < vs.length; i++) {
					CustomNpcs.mailTimeWhenLettersWillBeReceived[i] = vs[i];
				}
				vs = compound.getIntArray("CostSendingLetter");
				for (int i = 0; i < vs.length; i++) {
					CustomNpcs.mailCostSendingLetter[i] = vs[i];
				}
				CustomNpcs.forgeEventNames.clear();
				for (int i = 0; i < compound.getTagList("ForgeEventNames", 10).tagCount(); i++) {
					NBTTagCompound nbt = compound.getTagList("ForgeEventNames", 10).getCompoundTagAt(i);
					String name = nbt.getString("Name");
					Class<?> cls = null;
					try {
						cls = Class.forName(nbt.getString("Class"));
					} catch (ClassNotFoundException e) {
					}
					CustomNpcs.forgeEventNames.put(cls, name);
				}
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
				List<String> list = CustomNpcs.showDebugs();
				for (String str : list) { player.sendMessage(new TextComponentString(str)); }
				break;
			}
			default: { break; }
		}
	}
	
	// SYNC_REMOVE
	public static void remove(EnumSync synctype, int id, EntityPlayer player, ByteBuf buffer) {
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
				QuestCategory category2 = QuestController.instance.categories.remove(id);
				if (category2 != null) {
					QuestController.instance.quests.keySet().removeAll(category2.quests.keySet());
				}
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
				MarcetController.getInstance().marcets.remove(id);
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
				if (bank!=null) {
					bank.removeCeil(id);
				}
				break;
			}
			case Debug: {
				CustomNpcs.debugData.clear();
				break;
			}
			default: { break; }
		}
	}
	
	// SYNC_UPDATE
	public static void update(EnumSync synctype, NBTTagCompound compound, ByteBuf buffer, EntityPlayer player) {
		switch (synctype) {
			case FactionsData: {
				Faction faction = new Faction();
				faction.readNBT(compound);
				FactionController.instance.factions.put(faction.id, faction);
				break;
			}
			case QuestData: {
				int id = compound.getInteger("Id");
				if (QuestController.instance.quests.containsKey(id)) {
					Quest quest = QuestController.instance.quests.get(id);
					quest.readNBT(compound);
				} else {
					QuestCategory category = QuestController.instance.categories.get(buffer.readInt());
					Quest quest = new Quest(category);
					quest.readNBT(compound);
					QuestController.instance.quests.put(quest.id, quest);
					category.quests.put(quest.id, quest);
				}
				for (Quest q : PlayerQuestController.getActiveQuests(player)) {
					if (q.id != id) { continue; }
					q.readNBT(compound);
				}
				break;
			}
			case QuestCategoriesData: {
				QuestCategory category = new QuestCategory();
				category.readNBT(compound);
				QuestController.instance.categories.put(category.id, category);
				break;
			}
			case DialogData: {
				if (DialogController.instance.dialogs.containsKey(compound.getInteger("DialogId"))) {
					Dialog dialog = DialogController.instance.dialogs.get(compound.getInteger("DialogId"));
					dialog.readNBT(compound);
				} else {
					DialogCategory category = DialogController.instance.categories.get(buffer.readInt());
					Dialog dialog = new Dialog(category);
					dialog.readNBT(compound);
					DialogController.instance.dialogs.put(dialog.id, dialog);
					category.dialogs.put(dialog.id, dialog);
				}
				break;
			}
			case DialogCategoriesData: {
				DialogCategory category = new DialogCategory();
				category.readNBT(compound);
				DialogController.instance.categories.put(category.id, category);
				break;
			}
			case RecipesData: {
				if (compound.getKeySet().size()==0) {
					if (CustomNpcs.Server!=null && CustomNpcs.Server.isSinglePlayer()) { return; }
					RecipeController.getInstance().globalList.clear();
					RecipeController.getInstance().modList.clear();
				} else if (compound.hasKey("delete", 1) && compound.getBoolean("delete")) {
					RecipeController.getInstance().delete(compound.getString("Name"), compound.getString("Group"));
				} else {
					RecipeController.getInstance().loadNBTRecipe(compound);
				}
				break;
			}
			case AnimationData: {
				if (compound.getKeySet().size()==0) {
					if (CustomNpcs.Server!=null && CustomNpcs.Server.isSinglePlayer()) { return; }
					AnimationController.getInstance().animations.clear();
				} else if (compound.hasKey("delete", 1) && compound.getBoolean("delete")) {
					AnimationController.getInstance().removeAnimation(compound.getInteger("ID"));
				} else {
					AnimationController.getInstance().loadAnimation(compound);
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
				PlayerData data = CustomNpcs.proxy.getPlayerData(player);
				if (data != null) { data.game.readFromNBT(compound); }
				break;
			}
			case MailData: {
				if (compound.hasKey("MailData", 9)) {
					PlayerData data = CustomNpcs.proxy.getPlayerData(player);
					if (data != null) { data.mailData.loadNBTData(compound); }
				}
				if (compound.hasKey("LettersBeDeleted", 3)) { CustomNpcs.mailTimeWhenLettersWillBeDeleted = compound.getInteger("LettersBeDeleted"); }
				if (compound.hasKey("LettersBeReceived", 11)) {
					int[] vs = compound.getIntArray("LettersBeReceived");
					for (int i = 0; i < vs.length; i++) {
						CustomNpcs.mailTimeWhenLettersWillBeReceived[i] = vs[i];
					}
				}
				if (compound.hasKey("CostSendingLetter", 11)) {
					int[] vs = compound.getIntArray("CostSendingLetter");
					for (int i = 0; i < vs.length; i++) {
						CustomNpcs.mailCostSendingLetter[i] = vs[i];
					}
				}
				if (compound.hasKey("SendToYourself", 1)) { CustomNpcs.mailSendToYourself = compound.getBoolean("SendToYourself"); }
				break;
			}
			case Debug: {
				CustomNpcs.VerboseDebug = compound.getBoolean("debug");
				break;
			}
			default: { break; }
		}
	}

	public static void syncAllDialogs(MinecraftServer server) {
		for (DialogCategory category : DialogController.instance.categories.values()) {
			Server.sendToAll(server, EnumPacketClient.SYNC_ADD, EnumSync.DialogCategoriesData, category.writeNBT(new NBTTagCompound()));
		}
		Server.sendToAll(server, EnumPacketClient.SYNC_END, EnumSync.DialogCategoriesData, new NBTTagCompound());
	}

	public static void syncAllQuests(MinecraftServer server) {
		for (QuestCategory category : QuestController.instance.categories.values()) {
			Server.sendToAll(server, EnumPacketClient.SYNC_ADD, EnumSync.QuestCategoriesData, category.writeNBT(new NBTTagCompound()));
		}
		Server.sendToAll(server, EnumPacketClient.SYNC_END, EnumSync.QuestCategoriesData, new NBTTagCompound());
	}

	public static void syncPlayer(EntityPlayerMP player) {
		NBTTagList list = new NBTTagList();
		NBTTagCompound compound = new NBTTagCompound();
		for (Faction faction : FactionController.instance.factions.values()) {
			list.appendTag(faction.writeNBT(new NBTTagCompound()));
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
			Server.sendData(player, EnumPacketClient.SYNC_ADD, EnumSync.QuestCategoriesData, category.writeNBT(new NBTTagCompound()));
		}
		Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.QuestCategoriesData, new NBTTagCompound());

		for (DialogCategory category2 : DialogController.instance.categories.values()) {
			Server.sendData(player, EnumPacketClient.SYNC_ADD, EnumSync.DialogCategoriesData, category2.writeNBT(new NBTTagCompound()));
		}
		Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.DialogCategoriesData, new NBTTagCompound());

		RecipeController.getInstance().sendTo(player);
		AnimationController.getInstance().sendTo(player);

		compound = new NBTTagCompound();
		if (!CustomNpcs.showLR) {
			compound.setBoolean("ShowLR", CustomNpcs.showLR);
		}
		if (!CustomNpcs.showMoney) {
			compound.setBoolean("ShowMoney", CustomNpcs.showMoney);
		}
		compound.setBoolean("RecalculateLR", CustomNpcs.recalculateLR);
		compound.setBoolean("ShowQuestCompass", CustomNpcs.showQuestCompass);
		compound.setString("CharCurrencies", CustomNpcs.charCurrencies);
		compound.setInteger("MaxBuilderBlocks", CustomNpcs.maxBuilderBlocks);
		compound.setInteger("MaxItemInDropsNPC", CustomNpcs.maxItemInDropsNPC);
		compound.setInteger("LettersBeDeleted", CustomNpcs.mailTimeWhenLettersWillBeDeleted);
		compound.setInteger("ScriptMaxTabs", CustomNpcs.scriptMaxTabs);
		compound.setInteger("DialogFitsSpeed", CustomNpcs.dialogShowFitsSpeed);
		compound.setIntArray("LettersBeReceived", CustomNpcs.mailTimeWhenLettersWillBeReceived);
		compound.setIntArray("CostSendingLetter", CustomNpcs.mailCostSendingLetter);
		compound.setBoolean("SendToYourself", CustomNpcs.mailSendToYourself);
		
		list = new NBTTagList();
		for (Class<?> cls : CustomNpcs.forgeEventNames.keySet()) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("Name", CustomNpcs.forgeEventNames.get(cls));
			nbt.setString("Class", cls.getName());
			list.appendTag(nbt);
		}
		compound.setTag("ForgeEventNames", list);
		Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.ModData, compound);

		PlayerData data = PlayerData.get((EntityPlayer) player);
		if (player.getServer()!=null && player.getServer().getPlayerList()!=null && player.getGameProfile()!=null) {
			data.game.op = player.getServer().getPlayerList().canSendCommands(player.getGameProfile());
		}
		compound = data.getNBT();
		Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.PlayerData, compound);
		
		Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.KeysData, KeyController.getInstance().getNBT());
		
		syncScriptItems(player);
		syncScriptRecipes(player);
		
		BorderController.getInstance().sendTo(player);
		MarcetController.getInstance().sendTo(player, - 1);
		ScriptController.Instance.sendClientTo(player);
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

	public static void syncScriptRecipes(EntityPlayerMP player) {
		player.unlockRecipes(RecipeController.getInstance().getKnownRecipes());
	}

}
