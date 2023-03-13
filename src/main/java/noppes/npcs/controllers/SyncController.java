package noppes.npcs.controllers;

import java.util.HashMap;

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

	public static void clientSync(int synctype, NBTTagCompound compound, boolean syncEnd, EntityPlayer player) { // SYNC_ADD or SYNC_END
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
					HashMap<Integer, Quest> quests = new HashMap<Integer, Quest>();
					for (QuestCategory category2 : QuestController.instance.categoriesSync.values()) {
						for (Quest quest : category2.quests.values()) {
							quests.put(quest.id, quest);
						}
					}
					QuestController.instance.categories = QuestController.instance.categoriesSync;
					QuestController.instance.quests = quests;
					QuestController.instance.categoriesSync = new HashMap<Integer, QuestCategory>();
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
					HashMap<Integer, Dialog> dialogs = new HashMap<Integer, Dialog>();
					for (DialogCategory category4 : DialogController.instance.categoriesSync.values()) {
						for (Dialog dialog : category4.dialogs.values()) {
							dialogs.put(dialog.id, dialog);
						}
					}
					DialogController.instance.categories = DialogController.instance.categoriesSync;
					DialogController.instance.dialogs = dialogs;
					DialogController.instance.categoriesSync = new HashMap<Integer, DialogCategory>();
					
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
				CustomNpcs.recalculateLR = compound.getBoolean("RecalculateLR");
				CustomNpcs.charCurrencies = compound.getString("CharCurrencies");
				CustomNpcs.maxBuilderBlocks = compound.getInteger("MaxBuilderBlocks");
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
		}
	}

	public static void clientSyncRemove(int synctype, int id, EntityPlayer player) { // SYNC_REMOVE
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
		}
	}

	public static void clientSyncUpdate(int synctype, NBTTagCompound compound, ByteBuf buffer, EntityPlayer player) { // SYNC_UPDATE
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
			case 9: {
				ClientProxy.playerData.game.readFromNBT(compound);
				CustomNpcs.proxy.updateGUI();
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

		compound = new NBTTagCompound();
		if (!CustomNpcs.showLR) {
			compound.setBoolean("ShowLR", CustomNpcs.showLR);
		}
		if (!CustomNpcs.showMoney) {
			compound.setBoolean("ShowMoney", CustomNpcs.showMoney);
		}
		compound.setBoolean("RecalculateLR", CustomNpcs.recalculateLR);
		compound.setString("CharCurrencies", CustomNpcs.charCurrencies);
		compound.setInteger("MaxBuilderBlocks", CustomNpcs.maxBuilderBlocks);
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

		syncScriptItems(player);

		syncScriptRecipes(player);
		
		BorderController.getInstance().sendTo(player);
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
