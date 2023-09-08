package noppes.npcs.controllers;

import java.util.HashMap;
import java.util.TreeMap;

import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NBTTags;
import noppes.npcs.Server;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.items.ItemScripted;

public class SyncController {
	
	// SYNC_ADD or SYNC_END
	public static void clientSync(int synctype, NBTTagCompound compound, boolean syncEnd, EntityPlayer player) {
		switch (synctype) {
			case 1: {
				NBTTagList list = compound.getTagList("Data", 10);
				for (int i = 0; i < list.tagCount(); ++i) {
					Faction faction = new Faction();
					faction.readNBT(list.getCompoundTagAt(i));
					FactionController.instance.factionsSync.put(faction.id, faction);
				}
				if (syncEnd) {
					FactionController.instance.factions = FactionController.instance.factionsSync;
					FactionController.instance.factionsSync = new HashMap<Integer, Faction>();
				}
				break;
			}
			case 3: {
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
			case 5: {
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
			case 6: {
				CustomNpcs.proxy.updateRecipes(null, false, false, "SyncController.clientSync()");
				break;
			}
			case 7: {
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
			case 10: {
				KeyController.getInstance().loadKeys(compound);
				CustomNpcs.proxy.updateKeys();
				break;
			}
		}
	}
	
	// SYNC_REMOVE
	public static void clientSyncRemove(int synctype, int id, EntityPlayer player) {
		switch (synctype) {
			case 1: {
				FactionController.instance.factions.remove(id);
				break;
			}
			case 2: {
				Quest quest = QuestController.instance.quests.remove(id);
				if (quest != null) {
					quest.category.quests.remove(id);
				}
				break;
			}
			case 3: {
				QuestCategory category2 = QuestController.instance.categories.remove(id);
				if (category2 != null) {
					QuestController.instance.quests.keySet().removeAll(category2.quests.keySet());
				}
				break;
			}
			case 4: {
				Dialog dialog = DialogController.instance.dialogs.remove(id);
				if (dialog != null) {
					dialog.category.dialogs.remove(id);
				}
				break;
			}
			case 5: {
				DialogCategory category = DialogController.instance.categories.remove(id);
				if (category != null) {
					DialogController.instance.dialogs.keySet().removeAll(category.dialogs.keySet());
				}
				break;
			}
			case 6: {
				RecipeController.getInstance().delete(id);
				break;
			}
			case 10: {
				KeyController.getInstance().removeKeySetting(id);
				CustomNpcs.proxy.updateKeys();
				break;
			}
		}
	}
	
	// SYNC_UPDATE
	public static void clientSyncUpdate(int synctype, NBTTagCompound compound, ByteBuf buffer, EntityPlayer player) {
		switch (synctype) {
			case 1: {
				Faction faction = new Faction();
				faction.readNBT(compound);
				FactionController.instance.factions.put(faction.id, faction);
				break;
			}
			case 2: {
				QuestCategory category = QuestController.instance.categories.get(buffer.readInt());
				Quest quest = new Quest(category);
				quest.readNBT(compound);
				QuestController.instance.quests.put(quest.id, quest);
				category.quests.put(quest.id, quest);
				for (Quest q : PlayerQuestController.getActiveQuests(player)) {
					if (q.id != quest.id) {
						continue;
					}
					q.readNBT(compound);
				}
				break;
			}
			case 3: {
				QuestCategory category2 = new QuestCategory();
				category2.readNBT(compound);
				QuestController.instance.categories.put(category2.id, category2);
				break;
			}
			case 4: {
				DialogCategory category = DialogController.instance.categories.get(buffer.readInt());
				Dialog dialog = new Dialog(category);
				dialog.readNBT(compound);
				DialogController.instance.dialogs.put(dialog.id, dialog);
				category.dialogs.put(dialog.id, dialog);
				break;
			}
			case 5: {
				DialogCategory category = new DialogCategory();
				category.readNBT(compound);
				DialogController.instance.categories.put(category.id, category);
				break;
			}
			case 6: {
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
			case 7: {
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
			case 9: {
				ClientProxy.playerData.game.readFromNBT(compound);
				CustomNpcs.proxy.updateGUI();
				break;
			}
			case 10: {
				KeyController.getInstance().loadKey(compound);
				CustomNpcs.proxy.updateKeys();
				break;
			}
		}
	}

	public static void syncAllDialogs(MinecraftServer server) {
		for (DialogCategory category : DialogController.instance.categories.values()) {
			Server.sendToAll(server, EnumPacketClient.SYNC_ADD, 5, category.writeNBT(new NBTTagCompound()));
		}
		Server.sendToAll(server, EnumPacketClient.SYNC_END, 5, new NBTTagCompound());
	}

	public static void syncAllQuests(MinecraftServer server) {
		for (QuestCategory category : QuestController.instance.categories.values()) {
			Server.sendToAll(server, EnumPacketClient.SYNC_ADD, 3, category.writeNBT(new NBTTagCompound()));
		}
		Server.sendToAll(server, EnumPacketClient.SYNC_END, 3, new NBTTagCompound());
	}

	public static void syncPlayer(EntityPlayerMP player) {
		NBTTagList list = new NBTTagList();
		NBTTagCompound compound = new NBTTagCompound();
		for (Faction faction : FactionController.instance.factions.values()) {
			list.appendTag(faction.writeNBT(new NBTTagCompound()));
			if (list.tagCount() > 20) {
				compound = new NBTTagCompound();
				compound.setTag("Data", list);
				Server.sendData(player, EnumPacketClient.SYNC_ADD, 1, compound);
				list = new NBTTagList();
			}
		}
		compound = new NBTTagCompound();
		compound.setTag("Data", list);
		Server.sendData(player, EnumPacketClient.SYNC_END, 1, compound);

		for (QuestCategory category : QuestController.instance.categories.values()) {
			Server.sendData(player, EnumPacketClient.SYNC_ADD, 3, category.writeNBT(new NBTTagCompound()));
		}
		Server.sendData(player, EnumPacketClient.SYNC_END, 3, new NBTTagCompound());
		for (DialogCategory category2 : DialogController.instance.categories.values()) {
			Server.sendData(player, EnumPacketClient.SYNC_ADD, 5, category2.writeNBT(new NBTTagCompound()));
		}
		Server.sendData(player, EnumPacketClient.SYNC_END, 5, new NBTTagCompound());

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
		
		list = new NBTTagList();
		for (Class<?> cls : CustomNpcs.forgeEventNames.keySet()) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("Name", CustomNpcs.forgeEventNames.get(cls));
			nbt.setString("Class", cls.getName());
			list.appendTag(nbt);
		}
		compound.setTag("ForgeEventNames", list);
		Server.sendData(player, EnumPacketClient.SYNC_END, 7, compound);

		PlayerData data = PlayerData.get((EntityPlayer) player);
		if (player.getServer()!=null && player.getServer().getPlayerList()!=null && player.getGameProfile()!=null) {
			data.game.op = player.getServer().getPlayerList().canSendCommands(player.getGameProfile());
		}
		compound = data.getNBT();
		Server.sendData(player, EnumPacketClient.SYNC_END, 8, compound);
		
		Server.sendData(player, EnumPacketClient.SYNC_END, 10, KeyController.getInstance().getNBT());
		
		syncScriptItems(player);
		syncScriptRecipes(player);
		
		BorderController.getInstance().sendTo(player);
		MarcetController.getInstance().sendTo(player);
		ScriptController.Instance.sendClientTo(player);
	}

	public static void syncScriptItems(EntityPlayerMP player) {
		NBTTagCompound comp = new NBTTagCompound();
		comp.setTag("List", NBTTags.nbtIntegerStringMap(ItemScripted.Resources));
		Server.sendData(player, EnumPacketClient.SYNC_END, 9, comp);
	}

	public static void syncScriptItemsEverybody() {
		NBTTagCompound comp = new NBTTagCompound();
		comp.setTag("List", NBTTags.nbtIntegerStringMap(ItemScripted.Resources));
		for (EntityPlayerMP player : CustomNpcs.Server.getPlayerList().getPlayers()) {
			Server.sendData(player, EnumPacketClient.SYNC_END, 9, comp);
		}
	}

	public static void syncScriptRecipes(EntityPlayerMP player) {
		player.unlockRecipes(RecipeController.getInstance().getKnownRecipes());
	}

}
