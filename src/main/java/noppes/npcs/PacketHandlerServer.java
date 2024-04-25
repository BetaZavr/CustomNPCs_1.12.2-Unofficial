package noppes.npcs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.blocks.tiles.TileBuilder;
import noppes.npcs.blocks.tiles.TileCopy;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.blocks.tiles.TileScriptedDoor;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.constants.EnumCompanionStage;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerData;
import noppes.npcs.constants.EnumQuestRepeat;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.DropController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.LinkedNpcController;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.SpawnController;
import noppes.npcs.controllers.SyncController;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.Bank.CeilSettings;
import noppes.npcs.controllers.data.BankData;
import noppes.npcs.controllers.data.ClientScriptData;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.DropsTemplate;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.ForgeScriptData;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.PotionScriptData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestCategory;
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.controllers.data.Zone3D;
import noppes.npcs.dimensions.CustomWorldInfo;
import noppes.npcs.dimensions.DimensionHandler;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataScenes;
import noppes.npcs.entity.data.DropSet;
import noppes.npcs.items.ItemBoundary;
import noppes.npcs.items.ItemBuilder;
import noppes.npcs.items.crafting.NpcShapedRecipes;
import noppes.npcs.items.crafting.NpcShapelessRecipes;
import noppes.npcs.roles.JobBard;
import noppes.npcs.roles.JobSpawner;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleTransporter;
import noppes.npcs.roles.data.SpawnNPCData;
import noppes.npcs.schematics.Schematic;
import noppes.npcs.schematics.SchematicWrapper;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.BuilderData;
import noppes.npcs.util.IPermission;

public class PacketHandlerServer {

	private static List<EnumPacketServer> list;

	static {
		PacketHandlerServer.list = new ArrayList<EnumPacketServer>();
		PacketHandlerServer.list.add(EnumPacketServer.RemoteReset);
	}

	private boolean allowItem(ItemStack stack, EnumPacketServer type) {
		if (stack == null || stack.getItem() == null) {
			return false;
		}
		Item item = stack.getItem();
		IPermission permission = null;
		if (item instanceof IPermission) {
			permission = (IPermission) item;
		} else if (item instanceof ItemBlock && ((ItemBlock) item).getBlock() instanceof IPermission) {
			permission = (IPermission) ((ItemBlock) item).getBlock();
		}
		return permission != null && permission.isAllowed(type);
	}

	private void handlePacket(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player, EntityNPCInterface npc)
			throws Exception {
		CustomNpcs.debugData.startDebug("Server", type.toString(), "PacketHandlerServer_Received");
		if (type == EnumPacketServer.Delete) {
			if (npc.advanced.jobInterface instanceof JobBard) {
				JobBard job = (JobBard) npc.advanced.jobInterface;
				Server.sendData(player, EnumPacketClient.STOP_SOUND, job.song,
						(job.isStreamer ? SoundCategory.AMBIENT : SoundCategory.MUSIC).ordinal());
			}
			npc.delete();
			NoppesUtilServer.deleteEntity(npc, player);
		} else if (type == EnumPacketServer.SceneStart) {
			if (CustomNpcs.SceneButtonsEnabled) {
				DataScenes.Toggle(player, buffer.readInt() + "btn");
			}
		} else if (type == EnumPacketServer.SceneReset) {
			if (CustomNpcs.SceneButtonsEnabled) {
				DataScenes.Reset(player, null);
			}
		} else if (type == EnumPacketServer.LinkedAdd) {
			LinkedNpcController.Instance.addData(Server.readString(buffer));
			List<String> list = new ArrayList<String>();
			for (LinkedNpcController.LinkedData data : LinkedNpcController.Instance.list) {
				list.add(data.name);
			}
			Server.sendData(player, EnumPacketClient.SCROLL_LIST, list);
		} else if (type == EnumPacketServer.LinkedRemove) {
			LinkedNpcController.Instance.removeData(Server.readString(buffer));
			List<String> list = new ArrayList<String>();
			for (LinkedNpcController.LinkedData data : LinkedNpcController.Instance.list) {
				list.add(data.name);
			}
			Server.sendData(player, EnumPacketClient.SCROLL_LIST, list);
		} else if (type == EnumPacketServer.LinkedGetAll) {
			List<String> list = new ArrayList<String>();
			for (LinkedNpcController.LinkedData data : LinkedNpcController.Instance.list) {
				list.add(data.name);
			}
			Server.sendData(player, EnumPacketClient.SCROLL_LIST, list);
			if (npc != null) {
				Server.sendData(player, EnumPacketClient.SCROLL_SELECTED, npc.linkedName);
			}
		} else if (type == EnumPacketServer.LinkedSet) {
			npc.linkedName = Server.readString(buffer);
			LinkedNpcController.Instance.loadNpcData(npc);
		} else if (type == EnumPacketServer.NpcMenuClose) {
			npc.reset();
			if (npc.linkedData != null) {
				LinkedNpcController.Instance.saveNpcData(npc);
			}
			NoppesUtilServer.setEditingNpc(player, null);
		} else if (type == EnumPacketServer.BanksGet) {
			NoppesUtilServer.sendBankDataAll(player);
		} else if (type == EnumPacketServer.BankGet) {
			Bank bank = BankController.getInstance().getBank(buffer.readInt());
			NoppesUtilServer.sendBank(player, bank, buffer.readInt());
		} else if (type == EnumPacketServer.BankSave) {
			Bank bank = new Bank();
			bank.readFromNBT(Server.readNBT(buffer));
			BankController.getInstance().saveBank(bank);
			BankController.getInstance().change(bank);
			NoppesUtilServer.sendBankDataAll(player);
			NoppesUtilServer.sendBank(player, bank, 0);
		} else if (type == EnumPacketServer.BankShow) {
			/*
			 * int id = buffer.readInt(); if (EnumPlayerData.values().length <= id ||
			 * EnumPlayerData.values()[id] != EnumPlayerData.Bank) {
			 * CustomNpcs.debugData.endDebug("Server", type.toString(),
			 * "PacketHandlerServer_Received"); return; } String playerName =
			 * Server.readString(buffer); int bankId = buffer.readInt(); PlayerData
			 * playerdata = null; EntityPlayer pl = (EntityPlayer)
			 * player.getServer().getPlayerList().getPlayerByUsername(playerName); if (pl ==
			 * null) { playerdata =
			 * PlayerDataController.instance.getDataFromUsername(player.getServer(),
			 * playerName); } else { playerdata = PlayerData.get(pl); } if (playerdata==null
			 * || !playerdata.bankData.banks.containsKey(bankId)) { return; } NBTTagCompound
			 * compound = new NBTTagCompound(); playerdata.bankData.saveNBTData(compound);
			 * compound.setString("PlayerName", playerName); Server.sendData(player,
			 * EnumPacketClient.SHOW_BANK_PLAYER, compound); BankData bd =
			 * playerdata.bankData.banks.get(bankId); ContainerNPCBankInterface.editBank =
			 * playerdata.bankData; bd.openBankGui(player, npc, bankId, 0);
			 */
		} else if (type == EnumPacketServer.BankAddCeil) {
			Bank bank = BankController.getInstance().getBank(buffer.readInt());
			if (bank == null) {
				NoppesUtilServer.sendBank(player, bank, buffer.readInt());
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			CeilSettings cs = bank.addCeil();
			NoppesUtilServer.sendBank(player, bank, cs.ceil);
		} else if (type == EnumPacketServer.BankRemove) {
			int bankId = buffer.readInt();
			int ceilId = buffer.readInt();
			if (ceilId < 0) {
				Server.sendData(player, EnumPacketClient.SYNC_REMOVE, EnumSync.BankData, bankId);
				BankController.getInstance().removeBank(bankId);
				NoppesUtilServer.sendBankDataAll(player);
			} else {
				Bank bank = BankController.getInstance().getBank(bankId);
				if (bank == null) {
					CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
					return;
				}
				bank.removeCeil(ceilId);
				ceilId--;
				if (ceilId < 0) {
					ceilId = 0;
				}
				Server.sendData(player, EnumPacketClient.SYNC_REMOVE, EnumSync.BankCeil, ceilId, bankId);
				NoppesUtilServer.sendBank(player, bank, ceilId);
			}
		} else if (type == EnumPacketServer.RemoteMainMenu) {
			Entity entity = player.world.getEntityByID(buffer.readInt());
			if (entity == null || !(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.MainMenuDisplay, (EntityNPCInterface) entity);
		} else if (type == EnumPacketServer.RemoteDelete) {
			int id = buffer.readInt();
			Entity entity = player.world.getEntityByID(id);
			if (entity == null) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			if (entity instanceof EntityNPCInterface) {
				npc = (EntityNPCInterface) entity;
				npc.delete();
			} else {
				entity.setDead();
			}
			NoppesUtilServer.deleteEntity((EntityLivingBase) entity, player);
			NoppesUtilServer.sendNearbyEntitys(player, buffer.readBoolean());
		} else if (type == EnumPacketServer.RemoteNpcsGet) {
			NoppesUtilServer.sendNearbyEntitys(player, buffer.readBoolean());
			Server.sendData(player, EnumPacketClient.SCROLL_SELECTED,
					CustomNpcs.FreezeNPCs ? "Unfreeze Npcs" : "Freeze Npcs");
		} else if (type == EnumPacketServer.RemoteFreeze) {
			CustomNpcs.FreezeNPCs = !CustomNpcs.FreezeNPCs;
			player.sendMessage(new TextComponentString(CustomNpcs.FreezeNPCs ? "Freeze Npcs" : "Unfreeze Npcs"));
			Server.sendData(player, EnumPacketClient.SCROLL_SELECTED,
					CustomNpcs.FreezeNPCs ? "Freeze Npcs" : "Unfreeze Npcs");
		} else if (type == EnumPacketServer.RemoteReset) {
			Entity entity = player.world.getEntityByID(buffer.readInt());
			if (entity == null || !(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			npc = (EntityNPCInterface) entity;
			npc.reset();
		} else if (type == EnumPacketServer.RemoteTpToNpc) {
			Entity entity = player.world.getEntityByID(buffer.readInt());
			if (entity == null) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			player.connection.setPlayerLocation(entity.posX, entity.posY, entity.posZ, 0.0f, 0.0f);
		} else if (type == EnumPacketServer.Gui) {
			EnumGuiType gui = EnumGuiType.values()[buffer.readInt()];
			int x = buffer.readInt();
			int y = buffer.readInt();
			int z = buffer.readInt();
			NoppesUtilServer.sendOpenGui(player, gui, npc, x, y, z);
		} else if (type == EnumPacketServer.RecipesGet) {
			int size = buffer.readInt();
			String group = Server.readString(buffer);
			String name = Server.readString(buffer);
			NoppesUtilServer.sendRecipeData(player, size, group, name);
			if (!name.isEmpty()) {
				RecipeController rData = RecipeController.getInstance();
				INpcRecipe recipe = rData.getRecipe(group, name);
				if (recipe != null && (size == 3 && !recipe.isGlobal() || size == 4 && recipe.isGlobal())) {
					recipe = null;
				}
				if (recipe != null) {
					NoppesUtilServer.setRecipeGui(player, recipe);
				}
				Server.sendData(player, EnumPacketClient.GUI_UPDATE);
			}
		} else if (type == EnumPacketServer.RecipeGet) {
			int size = buffer.readInt();
			String group = Server.readString(buffer);
			String name = Server.readString(buffer);
			INpcRecipe recipe = RecipeController.getInstance().getRecipe(group, name);
			if (recipe != null && (size == 3 && !recipe.isGlobal() || size == 4 && recipe.isGlobal())) {
				recipe = null;
			}
			if (recipe == null) {
				recipe = new NpcShapedRecipes();
			}
			NoppesUtilServer.setRecipeGui(player, recipe);
			Server.sendData(player, EnumPacketClient.GUI_UPDATE);
		} else if (type == EnumPacketServer.RecipeRemove) {
			int size = buffer.readInt();
			String group = Server.readString(buffer);
			String name = Server.readString(buffer);
			if (RecipeController.getInstance().delete(group, name)) {
				NoppesUtilServer.sendRecipeData(player, size, group, name);
				NoppesUtilServer.setRecipeGui(player, new NpcShapedRecipes());
			}
			Server.sendData(player, EnumPacketClient.GUI_UPDATE);
		} else if (type == EnumPacketServer.RecipeSave) {
			NBTTagCompound compound = Server.readNBT(buffer);
			INpcRecipe recipe;
			if (compound.getBoolean("IsShaped")) {
				recipe = NpcShapedRecipes.read(compound);
			} else {
				recipe = NpcShapelessRecipes.read(compound);
			}
			INpcRecipe r = RecipeController.getInstance().putRecipe(recipe);
			NoppesUtilServer.sendRecipeData(player, r.isGlobal() ? 3 : 4, r.getNpcGroup(), r.getName());
			NoppesUtilServer.setRecipeGui(player, r);
		} else if (type == EnumPacketServer.NaturalSpawnGetAll) {
			NoppesUtilServer.sendScrollData(player, SpawnController.instance.getScroll());
		} else if (type == EnumPacketServer.NaturalSpawnGet) {
			SpawnData spawn = SpawnController.instance.getSpawnData(buffer.readInt());
			if (spawn != null) {
				Server.sendData(player, EnumPacketClient.GUI_DATA, spawn.writeNBT(new NBTTagCompound()));
			}
		} else if (type == EnumPacketServer.NaturalSpawnSave) {
			SpawnData data = new SpawnData();
			data.readNBT(Server.readNBT(buffer));
			SpawnController.instance.saveSpawnData(data);
			NoppesUtilServer.sendScrollData(player, SpawnController.instance.getScroll());
		} else if (type == EnumPacketServer.NaturalSpawnRemove) {
			SpawnController.instance.removeSpawnData(buffer.readInt());
			NoppesUtilServer.sendScrollData(player, SpawnController.instance.getScroll());
		} else if (type == EnumPacketServer.DialogCategorySave) {
			DialogCategory category = new DialogCategory();
			category.readNBT(Server.readNBT(buffer));
			DialogController.instance.saveCategory(category);
			Server.sendData(player, EnumPacketClient.GUI_UPDATE);
		} else if (type == EnumPacketServer.DialogCategoryRemove) {
			DialogController.instance.removeCategory(buffer.readInt());
			Server.sendData(player, EnumPacketClient.GUI_UPDATE);
		} else if (type == EnumPacketServer.DialogSave) {
			DialogCategory category = DialogController.instance.categories.get(buffer.readInt());
			if (category == null) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			Dialog dialog = new Dialog(category);
			dialog.readNBT(Server.readNBT(buffer));
			DialogController.instance.saveDialog(category, dialog);
			Server.sendData(player, EnumPacketClient.GUI_UPDATE);
		} else if (type == EnumPacketServer.DialogRemove) {
			Dialog dialog = DialogController.instance.dialogs.get(buffer.readInt());
			if (dialog != null && dialog.category != null) {
				DialogController.instance.removeDialog(dialog);
				Server.sendData(player, EnumPacketClient.GUI_UPDATE);
			}
		} else if (type == EnumPacketServer.QuestOpenGui) {
			Quest quest = new Quest(null);
			int gui = buffer.readInt();
			quest.readNBT(Server.readNBT(buffer));
			NoppesUtilServer.setEditingQuest(player, quest);
			player.openGui(CustomNpcs.instance, gui, player.world, 0, 0, 0);
		} else if (type == EnumPacketServer.DialogNpcGet) {
			NoppesUtilServer.sendNpcDialogs(player);
		} else if (type == EnumPacketServer.DialogNpcSet) {
			int slot = buffer.readInt();
			int dialogID = buffer.readInt();
			NBTTagCompound compound = NoppesUtilServer.setNpcDialog(slot, dialogID, player);
			if (compound != null) {
				Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
			} else {
				Server.sendData(player, EnumPacketClient.GUI_DATA, new NBTTagCompound());
			}
		} else if (type == EnumPacketServer.DialogNpcRemove) {
			int slot = buffer.readInt();
			if (slot < 0 || slot >= npc.dialogs.length) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			int[] newIDs = new int[npc.dialogs.length - 1];
			for (int i = 0, j = 0; i < npc.dialogs.length; i++) {
				if (i == slot) {
					continue;
				}
				newIDs[j] = npc.dialogs[i];
				j++;
			}
			npc.dialogs = newIDs;
			NoppesUtilServer.sendNpcDialogs(player);
		} else if (type == EnumPacketServer.DialogNpcMove) {
			NoppesUtilServer.moveNpcDialogs(player, buffer.readInt(), buffer.readBoolean());
		} else if (type == EnumPacketServer.SpawnerNpcMove) {
			NoppesUtilServer.moveNpcSpawn(player, buffer.readInt(), buffer.readBoolean(), buffer.readBoolean());
		} else if (type == EnumPacketServer.QuestCategorySave) {
			QuestCategory category = new QuestCategory();
			category.readNBT(Server.readNBT(buffer));
			QuestController.instance.saveCategory(category);
			Server.sendData(player, EnumPacketClient.GUI_UPDATE);
		} else if (type == EnumPacketServer.QuestCategoryRemove) {
			QuestController.instance.removeCategory(buffer.readInt());
			Server.sendData(player, EnumPacketClient.GUI_UPDATE);
		} else if (type == EnumPacketServer.QuestSave) {
			QuestCategory category = QuestController.instance.categories.get(buffer.readInt());
			if (category == null) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			Quest quest = new Quest(category);
			quest.readNBT(Server.readNBT(buffer));

			if (quest.completer == null) {
				if (npc != null) {
					NBTTagCompound compound = new NBTTagCompound();
					npc.writeEntityToNBT(compound);
					Entity e = EntityList.createEntityFromNBT(compound, npc.world);
					if (e instanceof EntityNPCInterface) {
						quest.completer = (EntityNPCInterface) e;
					}
				}
			} else if (npc != null && quest.completer.getName().equals(npc.getName())) {
				NBTTagCompound compound = new NBTTagCompound();
				npc.writeEntityToNBT(compound);
				quest.completer.readEntityFromNBT(compound);
			} else if (CustomNpcs.Server != null) {
				for (WorldServer w : CustomNpcs.Server.worlds) {
					boolean found = false;
					for (Entity e : w.loadedEntityList) {
						if (e instanceof EntityNPCInterface && e.getName().equals(quest.completer.getName())) {
							NBTTagCompound nbtNPC = new NBTTagCompound();
							((EntityNPCInterface) e).writeEntityToNBT(nbtNPC);
							quest.completer.readEntityFromNBT(nbtNPC);
							found = true;
							break;
						}
					}
					if (found) {
						break;
					}
				}
			} else {
				quest.completer.display.setName(npc.getName());
			}

			QuestController.instance.saveQuest(category, quest);
			Server.sendData(player, EnumPacketClient.GUI_UPDATE);
			Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, EnumSync.QuestCategoriesData,
					category.writeNBT(new NBTTagCompound()));
		} else if (type == EnumPacketServer.QuestReset) {
			Quest quest = new Quest(null);
			quest.readNBT(Server.readNBT(buffer));
			NoppesUtilServer.setEditingQuest(player, quest);
			int taskPos = buffer.readInt();
			if (taskPos >= 0) {
				NoppesUtilServer.sendOpenGui(player, EnumGuiType.QuestTypeItem, npc, taskPos, 0, 0);
			}
		} else if (type == EnumPacketServer.DialogMinID) {
			int idNow = buffer.readInt();
			List<Integer> ids = new ArrayList<Integer>();
			for (Integer qId : DialogController.instance.dialogs.keySet()) {
				ids.add(qId);
			}
			Collections.sort(ids);
			int id = 1;
			for (int i : ids) {
				if (id == i && id != idNow) {
					id++;
					continue;
				}
				break;
			}
			NBTTagCompound compound = new NBTTagCompound();
			compound.setInteger("MinimumID", id);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.FactionMinID) {
			int idNow = buffer.readInt();
			List<Integer> ids = new ArrayList<Integer>();
			for (Integer qId : FactionController.instance.factions.keySet()) {
				ids.add(qId);
			}
			Collections.sort(ids);
			int id = 0;
			for (int i : ids) {
				if (id == i && id != idNow) {
					id++;
					continue;
				}
				break;
			}
			NBTTagCompound compound = new NBTTagCompound();
			compound.setInteger("MinimumID", id);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.QuestMinID) {
			int idNow = buffer.readInt();
			List<Integer> ids = new ArrayList<Integer>();
			for (Integer qId : QuestController.instance.quests.keySet()) {
				ids.add(qId);
			}
			Collections.sort(ids);
			int id = 1;
			for (int i : ids) {
				if (id == i && id != idNow) {
					id++;
					continue;
				}
				break;
			}
			NBTTagCompound compound = new NBTTagCompound();
			compound.setInteger("MinimumID", id);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.QuestDialogGetTitle) {
			Dialog dialog = DialogController.instance.dialogs.get(buffer.readInt());
			if (dialog == null) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			NBTTagCompound compound = new NBTTagCompound();
			compound.setString("Title", dialog.title);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.QuestRemove) {
			int id = buffer.readInt();
			if (QuestController.instance.quests.containsKey(id)) {
				QuestController.instance.removeQuest(QuestController.instance.quests.get(id));
			}
			for (QuestCategory cat : QuestController.instance.categories.values()) {
				if (cat.quests.containsKey(id)) {
					cat.quests.remove(id);
				}
			}
			Server.sendData(player, EnumPacketClient.GUI_UPDATE);
		} else if (type == EnumPacketServer.TransportCategoriesGet) {
			try {
				int id = buffer.readInt();
				NoppesUtilServer.sendTransportData(player, id);
			} catch (Exception e) {
				NoppesUtilServer.sendTransportData(player);
			}
		} else if (type == EnumPacketServer.TransportCategorySave) {
			NBTTagCompound compound = Server.readNBT(buffer);
			int id = compound.getInteger("CategoryId");
			TransportController.getInstance().saveCategory(compound);
			if (id < 0) {
				NoppesUtilServer.sendTransportData(player);
			}
		} else if (type == EnumPacketServer.TransportCategoryRemove) {
			TransportController.getInstance().removeCategory(buffer.readInt());
			NoppesUtilServer.sendTransportData(player);
		} else if (type == EnumPacketServer.TransportRemove) {
			int id = buffer.readInt();
			TransportLocation loc = TransportController.getInstance().removeLocation(id);
			if (loc != null) {
				NoppesUtilServer.sendTransportData(player);
			}
		} else if (type == EnumPacketServer.TransportSave) {
			int cat = buffer.readInt();
			TransportLocation location = TransportController.getInstance().saveLocation(cat, Server.readNBT(buffer),
					player, npc);
			if (location != null) {
				if (!(npc.advanced.roleInterface instanceof RoleTransporter)) {
					CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
					return;
				}
				RoleTransporter role = (RoleTransporter) npc.advanced.roleInterface;
				role.setTransport(location);
			}
		} else if (type == EnumPacketServer.TransportGetLocation) {
			if (!(npc.advanced.roleInterface instanceof RoleTransporter)) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			RoleTransporter role = (RoleTransporter) npc.advanced.roleInterface;
			if (role.hasTransport()) {
				Server.sendData(player, EnumPacketClient.GUI_DATA, role.getLocation().writeNBT());
				Server.sendData(player, EnumPacketClient.SCROLL_SELECTED, role.getLocation().category.title);
			}
		} else if (type == EnumPacketServer.FactionSet) {
			npc.setFaction(buffer.readInt());
		} else if (type == EnumPacketServer.FactionSave) {
			Faction faction = new Faction();
			faction.readNBT(Server.readNBT(buffer));
			FactionController.instance.saveFaction(faction);
			NoppesUtilServer.sendFactionDataAll(player);
			NBTTagCompound compound = new NBTTagCompound();
			faction.writeNBT(compound);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.FactionRemove) {
			FactionController.instance.delete(buffer.readInt());
			NoppesUtilServer.sendFactionDataAll(player);
			NBTTagCompound compound = new NBTTagCompound();
			new Faction().writeNBT(compound);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.PlayerDataGet) {
			int id = buffer.readInt();
			if (EnumPlayerData.values().length <= id) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			String name = null;
			EnumPlayerData datatype = EnumPlayerData.values()[id];
			if (datatype != EnumPlayerData.Players) {
				name = Server.readString(buffer);
			}
			NoppesUtilServer.sendPlayerData(datatype, player, name);
		} else if (type == EnumPacketServer.PlayerDataRemove) {
			int id = buffer.readInt();
			if (EnumPlayerData.values().length <= id) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			} else if (EnumPlayerData.values()[id] == EnumPlayerData.Wipe) {
				List<String> list = PlayerDataController.instance.getPlayerNames();
				for (String username : player.getServer().getPlayerList().getOnlinePlayerNames()) {
					list.add(username);
				}
				for (String name : list) {
					EntityPlayer pl = (EntityPlayer) player.getServer().getPlayerList().getPlayerByUsername(name);
					PlayerData playerdata = null;
					if (pl == null) {
						playerdata = PlayerDataController.instance.getDataFromUsername(player.getServer(), name);
					} else {
						playerdata = PlayerData.get(pl);
					}
					playerdata.setNBT(new NBTTagCompound());
					playerdata.save(true);
					if (pl != null) {
						SyncController.syncPlayer((EntityPlayerMP) pl);
					}
				}
				AdditionalMethods.removeFile(CustomNpcs.getWorldSaveDirectory("playerdata"));
				CustomNpcs.getWorldSaveDirectory("playerdata").mkdirs();
				NoppesUtilServer.sendPlayerData(EnumPlayerData.Players, player, null);
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			NoppesUtilServer.removePlayerData(id, buffer, player);
		} else if (type == EnumPacketServer.MainmenuDisplayGet) {
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.display.writeToNBT(new NBTTagCompound()));
		} else if (type == EnumPacketServer.MainmenuDisplaySave) {
			npc.display.readToNBT(Server.readNBT(buffer));
			npc.updateClient = true;
		} else if (type == EnumPacketServer.MainmenuStatsGet) {
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.stats.writeToNBT(new NBTTagCompound()));
		} else if (type == EnumPacketServer.MainmenuStatsSave) {
			npc.stats.readToNBT(Server.readNBT(buffer));
			npc.updateClient = true;
		} else if (type == EnumPacketServer.MainmenuInvGet) {
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.inventory.writeEntityToNBT(new NBTTagCompound()));
			DropController.getInstance().setdTo(player);
		} else if (type == EnumPacketServer.MainmenuInvSave) {
			npc.inventory.readEntityFromNBT(Server.readNBT(buffer));
			npc.updateAI = true;
			npc.updateClient = true;
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.inventory.writeEntityToNBT(new NBTTagCompound()));
		} else if (type == EnumPacketServer.MainmenuInvDropSave) {
			int dropType = buffer.readInt();
			int groupId = buffer.readInt();
			int slot = buffer.readInt();
			NBTTagCompound compound = Server.readNBT(buffer);
			IItemStack stack = NpcAPI.Instance().getIItemStack(new ItemStack(compound.getCompoundTag("Item")));
			DropsTemplate template = DropController.getInstance().templates.get(npc.inventory.saveDropsName);
			if (stack.isEmpty()) {
				if (dropType == 1) {
					if (template != null) {
						if (slot < 0) {
							template.removeGroup(groupId);
						} else {
							template.removeDrop(groupId, slot);
						}
					}
				} else {
					npc.inventory.removeDrop(slot);
				}
			} else if (slot == -1) {
				DropSet drop = null;
				if (dropType == 1) {
					if (template != null) {
						template.addDropItem(groupId, stack, 1.0d);
					}
				} else {
					drop = (DropSet) npc.inventory.addDropItem(stack, 1.0d);
				}
				if (drop != null) {
					drop.load(compound);
				}
			} else {
				if (dropType == 1) {
					if (template != null && template.groups.containsKey(groupId)
							&& template.groups.get(groupId).containsKey(slot)) {
						template.groups.get(groupId).get(slot).load(compound);
					}
				} else if (npc.inventory.drops.containsKey(slot)) {
					npc.inventory.drops.get(slot).load(compound);
				}
			}
			npc.updateAI = true;
			npc.updateClient = true;
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.inventory.writeEntityToNBT(new NBTTagCompound()));
		} else if (type == EnumPacketServer.MainmenuAIGet) {
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.ais.writeToNBT(new NBTTagCompound()));
		} else if (type == EnumPacketServer.MainmenuAISave) {
			npc.ais.readToNBT(Server.readNBT(buffer));
			npc.setHealth(npc.getMaxHealth());
			npc.updateAI = true;
			npc.updateClient = true;
		} else if (type == EnumPacketServer.MainmenuAdvancedGet) {
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.advanced.writeToNBT(new NBTTagCompound()));
		} else if (type == EnumPacketServer.MainmenuAdvancedSave) {
			NBTTagCompound compound = Server.readNBT(buffer);
			npc.advanced.readToNBT(compound);
			npc.updateAI = true;
			npc.updateClient = true;
		} else if (type == EnumPacketServer.MainmenuAdvancedMarkData) {
			MarkData data = MarkData.get(npc);
			data.setNBT(Server.readNBT(buffer));
			data.syncClients();
		} else if (type == EnumPacketServer.JobSave) {
			NBTTagCompound original = npc.advanced.jobInterface.writeToNBT(new NBTTagCompound());
			NBTTagCompound compound = Server.readNBT(buffer);
			for (String name : compound.getKeySet()) {
				if (name.equals("DataEntitysWhenAlive") || name.equals("DataEntitysWhenDead")) {
					continue;
				}
				original.setTag(name, compound.getTag(name));
			}
			if (original.hasKey("Type", 3)) {
				npc.advanced.jobInterface.readFromNBT(original);
				npc.updateClient = true;
			}
		} else if (type == EnumPacketServer.JobClear) {
			if (!(npc.advanced.jobInterface instanceof JobSpawner)) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			((JobSpawner) npc.advanced.jobInterface).clear(buffer.readBoolean());

			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("JobData", true);
			npc.advanced.jobInterface.writeToNBT(compound);
			((JobSpawner) npc.advanced.jobInterface).cleanCompound(compound);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.JobGet) {
			if (npc.advanced.jobInterface == null) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("JobData", true);
			npc.advanced.jobInterface.writeToNBT(compound);
			if (npc.advanced.jobInterface instanceof JobSpawner) {
				((JobSpawner) npc.advanced.jobInterface).cleanCompound(compound);
			}
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.JobSpawnerAdd) {
			if (!(npc.advanced.jobInterface instanceof JobSpawner)) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			JobSpawner job = (JobSpawner) npc.advanced.jobInterface;
			boolean isServerSide = buffer.readBoolean();
			boolean isDead = buffer.readBoolean();
			int slot = buffer.readInt();
			if (isServerSide) {
				String name = Server.readString(buffer);
				int tab = buffer.readInt();
				SpawnNPCData sd = job.get(slot, isDead);
				if (sd == null) {
					sd = new SpawnNPCData();
					slot = job.size(isDead);
				}
				sd.compound = ServerCloneController.Instance.getCloneData(player, name, tab);
				if (sd.compound == null) {
					sd.compound = new NBTTagCompound();
				}
				sd.compound.setString("ClonedName", name);
				sd.compound.setInteger("ClonedTab", tab);
				sd.typeClones = 2;
				job.readJobCompound(slot, isDead, sd.writeToNBT());
			} else {
				SpawnNPCData sd = job.readJobCompound(slot, isDead, Server.readNBT(buffer));
				int tab = sd.compound.getInteger("ClonedTab");
				sd.typeClones = (tab > 0 && tab < 10) ? 0 : 1;
			}
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("JobData", true);
			npc.advanced.jobInterface.writeToNBT(compound);
			job.cleanCompound(compound);
			compound.setInteger("SetPos", slot);
			compound.setBoolean("SetDead", isDead);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.RoleCompanionUpdate) {
			if (!(npc.advanced.roleInterface instanceof RoleCompanion)) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			((RoleCompanion) npc.advanced.roleInterface).matureTo(EnumCompanionStage.values()[buffer.readInt()]);
			npc.updateClient = true;
		} else if (type == EnumPacketServer.JobSpawnerRemove) {
			if (!(npc.advanced.jobInterface instanceof JobSpawner)) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			JobSpawner job = (JobSpawner) npc.advanced.jobInterface;
			int slot = buffer.readInt();
			boolean isDead = buffer.readBoolean();
			job.removeSpawned(slot, isDead);
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("JobData", true);
			npc.advanced.jobInterface.writeToNBT(compound);
			job.cleanCompound(compound);
			compound.setInteger("SetPos", -1);
			compound.setBoolean("SetDie", isDead);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.RoleSave) {
			NBTTagCompound compound = Server.readNBT(buffer);
			if (compound.hasKey("Type", 3)) {
				npc.advanced.roleInterface.readFromNBT(compound);
				npc.updateClient = true;
			}
		} else if (type == EnumPacketServer.RoleGet) {
			if (npc.advanced.roleInterface == null) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			NBTTagCompound compound = npc.advanced.roleInterface.writeToNBT(new NBTTagCompound());
			compound.setBoolean("RoleData", true);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.MerchantUpdate) {
			Entity entity = player.world.getEntityByID(buffer.readInt());
			if (entity == null || !(entity instanceof EntityVillager)) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			MerchantRecipeList list = MerchantRecipeList.readFromBuf(new PacketBuffer(buffer));
			((EntityVillager) entity).setRecipes(list);
		} else if (type == EnumPacketServer.ModelDataSave) {
			if (npc instanceof EntityCustomNpc) {
				((EntityCustomNpc) npc).modelData.readFromNBT(Server.readNBT(buffer));
			}
		} else if (type == EnumPacketServer.MailOpenSetup) {
			PlayerMail mail = new PlayerMail();
			mail.readNBT(Server.readNBT(buffer));
			ContainerMail.staticmail = mail;
			player.openGui(CustomNpcs.instance, EnumGuiType.PlayerMailOpen.ordinal(), player.world, 1, 0, 0);
		} else if (type == EnumPacketServer.TransformSave) {
			boolean isValid = npc.transform.isValid();
			npc.transform.readOptions(Server.readNBT(buffer));
			if (isValid != npc.transform.isValid()) {
				npc.updateAI = true;
			}
		} else if (type == EnumPacketServer.TransformGet) {
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.transform.writeOptions(new NBTTagCompound()));
		} else if (type == EnumPacketServer.TransformLoad) {
			if (npc.transform.isValid()) {
				npc.transform.transform(buffer.readBoolean());
			}
		} else if (type == EnumPacketServer.MovingPathGet) {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("MovingPathNew", NBTTags.nbtIntegerArraySet(npc.ais.getMovingPath()));
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.MovingPathSave) {
			npc.ais.setMovingPath(NBTTags.getIntegerArraySet(Server.readNBT(buffer).getTagList("MovingPathNew", 10)));
		} else if (type == EnumPacketServer.SpawnRider) {
			Entity entity = EntityList.createEntityFromNBT(Server.readNBT(buffer), player.world);
			player.world.spawnEntity(entity);
			entity.startRiding(ServerEventsHandler.mounted, true);
		} else if (type == EnumPacketServer.PlayerRider) {
			player.startRiding(ServerEventsHandler.mounted, true);
		} else if (type == EnumPacketServer.SpawnMob) {
			boolean server = buffer.readBoolean();
			int x = buffer.readInt();
			int y = buffer.readInt();
			int z = buffer.readInt();
			NBTTagCompound compound;
			NBTTagCompound nbtData = new NBTTagCompound();
			if (server) {
				String name = Server.readString(buffer);
				int tab = buffer.readInt();
				nbtData.setString("Name", name);
				nbtData.setInteger("Tab", tab);
				nbtData.setBoolean("isServerClone", true);
				compound = ServerCloneController.Instance.getCloneData(player, name, tab);
			} else {
				compound = Server.readNBT(buffer);
				if (compound != null) {
					nbtData.setString("Name", compound.getString("Name"));
					nbtData.setBoolean("isServerClone", false);
				}
			}
			if (compound == null) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			if (!server) {
				nbtData.setTag("EntityNBT", compound);
			}
			Entity entity = NoppesUtilServer.spawnClone(compound, x + 0.5, y + 1, z + 0.5, player.world);
			ItemStack stack = player.getHeldItemMainhand();
			NBTTagCompound nbt = null;
			if (stack != null && stack.getItem() == CustomRegisters.cloner) {
				nbt = stack.getTagCompound();
				if (nbt == null) {
					stack.setTagCompound(nbt = new NBTTagCompound());
				}
			}
			if (entity == null) {
				if (nbt != null && nbt.hasKey("Settings")) {
					nbt.removeTag("Settings");
					player.openContainer.detectAndSendChanges();
				}
				player.sendMessage(new TextComponentString("Failed to create an entity out of your clone"));
			} else {
				if (nbt != null) {
					if (!nbtData.hasKey("Name", 8) || nbtData.getString("Name").isEmpty()) {
						nbtData.setString("Name", entity.getName());
					}
					nbt.setTag("Settings", nbtData);
					player.openContainer.detectAndSendChanges();
				}
			}
		} else if (type == EnumPacketServer.MobSpawner) {
			boolean server = buffer.readBoolean();
			BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			NBTTagCompound compound;
			if (server) {
				compound = ServerCloneController.Instance.getCloneData(player, Server.readString(buffer),
						buffer.readInt());
			} else {
				compound = Server.readNBT(buffer);
			}
			if (compound != null) {
				NoppesUtilServer.createMobSpawner(pos, compound, player);
			}
		} else if (type == EnumPacketServer.ClonePreSave) {
			boolean bo = ServerCloneController.Instance.getCloneData(null, Server.readString(buffer),
					buffer.readInt()) != null;
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("NameExists", bo);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.CloneSave) {
			PlayerData data = PlayerData.get(player);
			if (data.cloned == null) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			ServerCloneController.Instance.addClone(data.cloned, Server.readString(buffer), buffer.readInt());
		} else if (type == EnumPacketServer.CloneRemove) {
			int tab = buffer.readInt();
			ServerCloneController.Instance.removeClone(Server.readString(buffer), tab);
			NBTTagList list = new NBTTagList();
			for (String name : ServerCloneController.Instance.getClones(tab)) {
				list.appendTag(new NBTTagString(name));
			}
			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("List", list);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.CloneList) {
			NBTTagList list = new NBTTagList();
			for (String name : ServerCloneController.Instance.getClones(buffer.readInt())) {
				list.appendTag(new NBTTagString(name));
			}
			NBTTagCompound compound = new NBTTagCompound();
			compound.setTag("List", list);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.ScriptDataSave) {
			npc.script.readFromNBT(Server.readNBT(buffer));
			npc.updateAI = true;
			npc.script.lastInited = -1L;
		} else if (type == EnumPacketServer.ScriptDataGet) {
			NBTTagCompound compound = npc.script.writeToNBT(new NBTTagCompound());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages(false));
			compound.setString("DirPath", ScriptController.Instance.dir.getAbsolutePath());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.DimensionSettings) {
			int id = buffer.readInt();
			CustomWorldInfo wi = Server.readWorldInfo(buffer);
			if (id == 0) {
				DimensionHandler.getInstance().createDimension(player, wi);
			} else {
				CustomWorldInfo cwi = (CustomWorldInfo) DimensionHandler.getInstance().getMCWorldInfo(id);
				if (cwi != null) {
					cwi.load(wi.read());
				}
			}
		} else if (type == EnumPacketServer.DimensionDelete) {
			int delID = buffer.readInt();
			DimensionHandler.getInstance().deleteDimension(player, delID);
			NoppesUtilServer.sendScrollData(player, DimensionHandler.getInstance().getMapDimensionsIDs());
		} else if (type == EnumPacketServer.DimensionsGet) {
			NoppesUtilServer.sendScrollData(player, DimensionHandler.getInstance().getMapDimensionsIDs());
		} else if (type == EnumPacketServer.DimensionTeleport) {
			int dimension = buffer.readInt();
			WorldServer world = player.getServer().getWorld(dimension);
			BlockPos coords = world.getSpawnCoordinate();
			if (coords == null) {
				coords = world.getSpawnPoint();
				if (!world.isAirBlock(coords)) {
					coords = world.getTopSolidOrLiquidBlock(coords);
				} else {
					while (world.isAirBlock(coords) && coords.getY() > 0) {
						coords = coords.down();
					}
					if (coords.getY() == 0) {
						coords = world.getTopSolidOrLiquidBlock(coords);
					}
				}
			}
			NoppesUtilPlayer.teleportPlayer(player, coords.getX(), coords.getY(), coords.getZ(), dimension,
					player.rotationYaw, player.rotationPitch);
		} else if (type == EnumPacketServer.ScriptBlockDataGet) {
			TileEntity tile = player.world
					.getTileEntity(new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt()));
			if (!(tile instanceof TileScripted)) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			NBTTagCompound compound = ((TileScripted) tile).getNBT(new NBTTagCompound());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages(false));
			compound.setString("DirPath", ScriptController.Instance.dir.getAbsolutePath());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.ScriptItemDataGet) {
			ItemScriptedWrapper iw = (ItemScriptedWrapper) NpcAPI.Instance()
					.getIItemStack(player.getHeldItemMainhand());
			NBTTagCompound compound = iw.getMCNbt();
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages(false));
			compound.setString("DirPath", ScriptController.Instance.dir.getAbsolutePath());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.ScriptItemDataSave) {
			if (!player.isCreative()) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			NBTTagCompound compound = Server.readNBT(buffer);
			ItemScriptedWrapper wrapper = (ItemScriptedWrapper) NpcAPI.Instance()
					.getIItemStack(player.getHeldItemMainhand());
			wrapper.setMCNbt(compound);
			wrapper.lastInited = -1L;
			wrapper.saveScriptData();
			wrapper.updateClient = true;
			player.sendContainerToPlayer(player.inventoryContainer);
		} else if (type == EnumPacketServer.ScriptForgeGet) {
			ForgeScriptData data = ScriptController.Instance.forgeScripts;
			NBTTagCompound compound = data.writeToNBT(new NBTTagCompound());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages(false));
			compound.setString("DirPath", ScriptController.Instance.dir.getAbsolutePath());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.ScriptForgeSave) {
			ScriptController.Instance.setForgeScripts(Server.readNBT(buffer));
		} else if (type == EnumPacketServer.ScriptClientGet) {
			ClientScriptData data = ScriptController.Instance.clientScripts;
			NBTTagCompound compound = data.writeToNBT(new NBTTagCompound());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages(true));
			compound.setString("DirPath", ScriptController.Instance.clientDir.getAbsolutePath());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.ScriptClientSave) {
			ScriptController.Instance.setClientScripts(Server.readNBT(buffer));
			for (EntityPlayerMP pl : player.mcServer.getPlayerList().getPlayers()) {
				ScriptController.Instance.sendClientTo(pl);
			}
		} else if (type == EnumPacketServer.ScriptPlayerGet) {
			NBTTagCompound compound = ScriptController.Instance.playerScripts.writeToNBT(new NBTTagCompound());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages(false));
			compound.setString("DirPath", ScriptController.Instance.dir.getAbsolutePath());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.ScriptPlayerSave) {
			ScriptController.Instance.setPlayerScripts(Server.readNBT(buffer));
		} else if (type == EnumPacketServer.FactionsGet) {
			NoppesUtilServer.sendFactionDataAll(player);
		} else if (type == EnumPacketServer.FactionGet) {
			NBTTagCompound compound = new NBTTagCompound();
			Faction faction = FactionController.instance.getFaction(buffer.readInt());
			faction.writeNBT(compound);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.SaveTileEntity) {
			NoppesUtilServer.saveTileEntity(player, Server.readNBT(buffer));
		} else if (type == EnumPacketServer.GetTileEntity) {
			BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			TileEntity tile = player.world.getTileEntity(pos);
			NBTTagCompound compound = new NBTTagCompound();
			tile.writeToNBT(compound);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.ScriptBlockDataSave) {
			TileEntity tile = player.world
					.getTileEntity(new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt()));
			if (!(tile instanceof TileScripted)) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			TileScripted script = (TileScripted) tile;
			script.setNBT(Server.readNBT(buffer));
			script.lastInited = -1L;
		} else if (type == EnumPacketServer.ScriptDoorDataSave) {
			TileEntity tile = player.world
					.getTileEntity(new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt()));
			if (!(tile instanceof TileScriptedDoor)) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			TileScriptedDoor script = (TileScriptedDoor) tile;
			script.setNBT(Server.readNBT(buffer));
			script.lastInited = -1L;
		} else if (type == EnumPacketServer.ScriptDoorDataGet) {
			TileEntity tile = player.world
					.getTileEntity(new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt()));
			if (!(tile instanceof TileScriptedDoor)) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			NBTTagCompound compound = ((TileScriptedDoor) tile).getNBT(new NBTTagCompound());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages(false));
			compound.setString("DirPath", ScriptController.Instance.dir.getAbsolutePath());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.SchematicsTile) {
			BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			TileBuilder tile = (TileBuilder) player.world.getTileEntity(pos);
			if (tile == null) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			Server.sendData(player, EnumPacketClient.GUI_DATA, tile.writePartNBT(new NBTTagCompound()));
			Server.sendData(player, EnumPacketClient.SCROLL_LIST, SchematicController.Instance.list());
			if (tile.hasSchematic()) {
				Server.sendData(player, EnumPacketClient.GUI_DATA, tile.getSchematic().getNBTSmall());
			}
		} else if (type == EnumPacketServer.SchematicsSet) {
			BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			TileBuilder tile = (TileBuilder) player.world.getTileEntity(pos);
			String name = Server.readString(buffer);
			tile.setSchematic(SchematicController.Instance.load(name));
			if (tile.hasSchematic()) {
				Server.sendData(player, EnumPacketClient.GUI_DATA, tile.getSchematic().getNBTSmall());
			}
		} else if (type == EnumPacketServer.SchematicsBuild) {
			BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			TileEntity t = player.world.getTileEntity(pos);
			if (t instanceof TileBuilder) {
				TileBuilder tile = (TileBuilder) t;
				SchematicWrapper schem = tile.getSchematic();
				schem.init(pos.add(1, tile.yOffest, 1), player.world, tile.rotation * 90);
				SchematicController.Instance.build(tile.getSchematic(), player);
				player.world.setBlockToAir(pos);
			} else {
				int rotaion = buffer.readInt();
				NBTTagCompound compound = Server.readNBT(buffer);
				Schematic schema = new Schematic("");
				schema.load(compound);
				SchematicController.buildBlocks(player, pos, rotaion, schema);
			}
		} else if (type == EnumPacketServer.SchematicsTileSave) {
			BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			TileBuilder tile = (TileBuilder) player.world.getTileEntity(pos);
			if (tile != null) {
				tile.readPartNBT(Server.readNBT(buffer));
			}
		} else if (type == EnumPacketServer.SchematicStore) {
			String name = Server.readString(buffer);
			int t = buffer.readInt();
			TileCopy tile = (TileCopy) NoppesUtilServer.saveTileEntity(player, Server.readNBT(buffer));
			if (tile == null || name.isEmpty()) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			SchematicController.Instance.save(player, name, t, tile.getPos(), tile.height, tile.width, tile.length);
		} else if (type == EnumPacketServer.NbtBookSaveBlock) {
			BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			NBTTagCompound compound = Server.readNBT(buffer);
			TileEntity tile = player.world.getTileEntity(pos);
			if (tile != null) {
				tile.readFromNBT(compound);
				tile.markDirty();
				CustomNpcs.proxy.fixTileEntityData(tile);
			}
		} else if (type == EnumPacketServer.NbtBookSaveEntity) {
			int entityId = buffer.readInt();
			NBTTagCompound compound = Server.readNBT(buffer);
			Entity entity = player.world.getEntityByID(entityId);
			if (entity != null) {
				entity.readFromNBT(compound);
			}
		} else if (type == EnumPacketServer.NbtBookSaveItem) {
			NBTTagCompound stackNBT = Server.readNBT(buffer);
			ItemStack stack = new ItemStack(stackNBT);
			if (stack == null || stack.isEmpty()) {
				player.sendMessage(new TextComponentTranslation("nbt.book.not.correct.nbt"));
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			player.inventory.offHandInventory.set(0, stack);
			player.openContainer.detectAndSendChanges();
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("Item", true);
			compound.setTag("Data", stackNBT);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.NbtBookCopyStack) {
			EntityItem entity = new EntityItem(player.world, player.posX, player.posY, player.posZ,
					new ItemStack(Server.readNBT(buffer)));
			entity.setPickupDelay(5);
			player.world.spawnEntity(entity);
		} else if (type == EnumPacketServer.RecipesAddGroup) {
			int size = buffer.readInt();
			String group = Server.readString(buffer);
			if (size == 3) {
				RecipeController.getInstance().globalList.put(group, Lists.<INpcRecipe>newArrayList());
			} else {
				RecipeController.getInstance().modList.put(group, Lists.<INpcRecipe>newArrayList());
			}
			NpcShapedRecipes recipe = new NpcShapedRecipes(group, "default", size, size, NonNullList.create(),
					ItemStack.EMPTY);
			recipe.global = (size == 3);
			RecipeController.getInstance().putRecipe(recipe);
			NoppesUtilServer.sendRecipeData(player, size, group, "default");
			NoppesUtilServer.setRecipeGui(player, new NpcShapedRecipes());
			Server.sendData(player, EnumPacketClient.GUI_UPDATE);
		} else if (type == EnumPacketServer.RecipesRenameGroup) {
			int size = buffer.readInt();
			String old = Server.readString(buffer);
			RecipeController rData = RecipeController.getInstance();
			String group = Server.readString(buffer);
			String recipe = Server.readString(buffer);
			Map<String, List<INpcRecipe>> map = size == 3 ? rData.globalList : rData.modList;
			if (map.containsKey(old)) {
				RecipeController.Registry.unfreeze();
				map.put(group, map.get(old));
				map.remove(old);
				for (INpcRecipe rec : map.get(group)) {
					if (rec.getClass() == NpcShapedRecipes.class) {
						((NpcShapedRecipes) rec).group = group;
					} else {
						((NpcShapelessRecipes) rec).group = group;
					}
					IRecipe r = RecipeController.Registry.getValue(((IRecipe) rec).getRegistryName());
					if (r instanceof INpcRecipe) {
						if (r.getClass() == NpcShapedRecipes.class) {
							((NpcShapedRecipes) r).group = group;
						} else {
							((NpcShapelessRecipes) r).group = group;
						}
					}
				}
				RecipeController.Registry.freeze();
				CustomNpcs.proxy.updateRecipes(null, true, false, "PacketHandlerServer.RecipesRenameGroup");
				NoppesUtilServer.sendRecipeData(player, size, group, recipe);
				NoppesUtilServer.setRecipeGui(player, rData.getRecipe(group, recipe));
			}
			Server.sendData(player, EnumPacketClient.GUI_UPDATE);

		} else if (type == EnumPacketServer.RecipeRemoveGroup) {
			int size = buffer.readInt();
			String group = Server.readString(buffer);
			RecipeController rData = RecipeController.getInstance();
			Map<String, List<INpcRecipe>> map = size == 3 ? rData.globalList : rData.modList;
			if (map.containsKey(group)) {
				RecipeController.Registry.unfreeze();
				for (INpcRecipe rec : map.get(group)) {
					IRecipe r = RecipeController.Registry.getValue(((IRecipe) rec).getRegistryName());
					if (r instanceof INpcRecipe) {
						RecipeController.Registry.remove(r.getRegistryName());
					}
				}
				map.remove(group);
				RecipeController.Registry.freeze();
				CustomNpcs.proxy.updateRecipes(null, true, false, "PacketHandlerServer.RecipeRemoveGroup");
				NoppesUtilServer.sendRecipeData(player, size, "", "");
				NoppesUtilServer.setRecipeGui(player, new NpcShapedRecipes());
			}
			Server.sendData(player, EnumPacketClient.GUI_UPDATE);

		} else if (type == EnumPacketServer.RecipesRename) {
			int size = buffer.readInt();
			String old = Server.readString(buffer);
			String group = Server.readString(buffer);
			RecipeController rData = RecipeController.getInstance();
			String name = Server.readString(buffer);
			INpcRecipe recipe = null;
			Map<String, List<INpcRecipe>> map = size == 3 ? rData.globalList : rData.modList;
			if (map.containsKey(group)) {
				for (INpcRecipe rec : map.get(group)) {
					if (!rec.getName().equals(old)) {
						continue;
					}
					if (rec.getClass() == NpcShapedRecipes.class) {
						((NpcShapedRecipes) rec).name = name;
					} else {
						((NpcShapelessRecipes) rec).name = name;
					}
					IRecipe r = RecipeController.Registry.getValue(((IRecipe) rec).getRegistryName());
					if (r instanceof INpcRecipe) {
						if (r.getClass() == NpcShapedRecipes.class) {
							((NpcShapedRecipes) r).name = name;
						} else {
							((NpcShapelessRecipes) r).name = name;
						}
					}
					RecipeController.Registry.freeze();
					CustomNpcs.proxy.updateRecipes(rec, true, false, "PacketHandlerServer.RecipesRename");
					NoppesUtilServer.sendRecipeData(player, size, group, name);
					NoppesUtilServer.setRecipeGui(player, recipe);
					break;
				}
			}
			Server.sendData(player, EnumPacketClient.GUI_UPDATE);
		} else if (type == EnumPacketServer.TraderMarketSave) {
			MarcetController mData = MarcetController.getInstance();
			NBTTagCompound compound = Server.readNBT(buffer);
			if (compound.hasKey("DealID", 3)) {
				mData.loadDeal(compound);
			} else {
				mData.loadMarcet(compound);
			}
			MarcetController.getInstance().saveMarcets();
		} else if (type == EnumPacketServer.TraderMarketGet) {
			int id = -1;
			try {
				id = buffer.readInt();
			} catch (Exception e) {
			}
			MarcetController.getInstance().sendTo(player, id);
		} else if (type == EnumPacketServer.TraderMarketDel) {
			MarcetController mData = MarcetController.getInstance();
			int marcetID = buffer.readInt();
			int dealID = buffer.readInt();
			if (marcetID >= 0 && dealID < 0) {
				Server.sendData(player, EnumPacketClient.SYNC_REMOVE, EnumSync.MarcetData, marcetID);
				mData.removeMarcet(marcetID);
			}
			if (marcetID < 0 && dealID >= 0) {
				Server.sendData(player, EnumPacketClient.SYNC_REMOVE, EnumSync.MarcetDeal, dealID);
				mData.removeDeal(dealID);
			}
			MarcetController.getInstance().sendTo(player, marcetID);
		} else if (type == EnumPacketServer.GetClone) {
			NBTTagCompound npcNbt = null;
			if (buffer.readBoolean()) {
				if (npc != null && npc.advanced.jobInterface instanceof JobSpawner) {
					SpawnNPCData sd = ((JobSpawner) npc.advanced.jobInterface).get(buffer.readInt(),
							buffer.readBoolean());
					if (sd != null && sd.compound != null) {
						npcNbt = sd.compound;
						if (sd.typeClones == 2) {
							npcNbt = ServerCloneController.Instance.getCloneData(player,
									sd.compound.getString("ClonedName"), sd.compound.getInteger("ClonedTab"));
						}
					}
				}
			} else {
				npcNbt = ServerCloneController.Instance.getCloneData(player, Server.readString(buffer),
						buffer.readInt());
			}
			if (npcNbt != null) {
				NBTTagCompound compound = new NBTTagCompound();
				compound.setTag("NPCData", npcNbt);
				Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
			}
		} else if (type == EnumPacketServer.ScriptPotionGet) {
			PotionScriptData data = ScriptController.Instance.potionScripts;
			NBTTagCompound compound = data.writeToNBT(new NBTTagCompound());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages(false));
			compound.setString("DirPath", ScriptController.Instance.dir.getAbsolutePath());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.ScriptPotionSave) {
			ScriptController.Instance.setPotionScripts(Server.readNBT(buffer));
		} else if (type == EnumPacketServer.TeleportTo) {
			int dimensionId = buffer.readInt();
			BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			if (player.world.provider.getDimension() == dimensionId) {
				player.setPositionAndUpdate(pos.getX() + 0.5d, pos.getY(), pos.getZ() + 0.5d);
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			try {
				AdditionalMethods.teleportEntity(CustomNpcs.Server, player, dimensionId, pos);
			} catch (Exception e) {
			}
		} else if (type == EnumPacketServer.RegionData) {
			int t = buffer.readInt();
			BorderController bData = BorderController.getInstance();
			if (t == 0) {
				int regId = buffer.readInt();
				if (!bData.regions.containsKey(regId)) {
					bData.sendTo(player);
					CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
					return;
				}
				if (!player.getHeldItemMainhand().isEmpty()
						&& player.getHeldItemMainhand().getItem() instanceof ItemBoundary) {
					NBTTagCompound compound = player.getHeldItemMainhand().getTagCompound();
					if (compound == null) {
						player.getHeldItemMainhand().setTagCompound(compound = new NBTTagCompound());
					}
					compound.setInteger("RegionID", regId);
				}
			} else if (t == 1) {
				int regId = buffer.readInt();
				if (!bData.regions.containsKey(regId)) {
					bData.sendTo(player);
					CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
					return;
				}
				if (!player.getHeldItemMainhand().isEmpty()
						&& player.getHeldItemMainhand().getItem() instanceof ItemBoundary) {
					NBTTagCompound compound = player.getHeldItemMainhand().getTagCompound();
					if (compound == null) {
						player.getHeldItemMainhand().setTagCompound(compound = new NBTTagCompound());
					}
					compound.removeTag("RegionID");
				}
				if (bData.removeRegion(regId)) {
					for (EntityPlayerMP p : CustomNpcs.Server.getPlayerList().getPlayers()) {
						bData.sendTo(p);
					}
				}
			}
			if (t == 2) {
				Zone3D reg = bData.loadRegion(Server.readNBT(buffer));
				if (reg != null) {
					bData.saveRegions();
					bData.update(reg.getId());
				}
			}
		} else if (type == EnumPacketServer.OpenBuilder) {
			if (player.getHeldItemMainhand().isEmpty()
					|| !(player.getHeldItemMainhand().getItem() instanceof ItemBuilder)
					|| !player.getHeldItemMainhand().hasTagCompound()) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			int id = player.getHeldItemMainhand().getTagCompound().getInteger("ID");
			NBTTagCompound compound = Server.readNBT(buffer);
			if (id != compound.getInteger("ID")) {
				id = compound.getInteger("ID");
				player.getHeldItemMainhand().getTagCompound().setInteger("ID", id);
			}
			if (!CommonProxy.dataBuilder.containsKey(id)) {
				CommonProxy.dataBuilder.put(id, new BuilderData());
			}
			CommonProxy.dataBuilder.get(id).read(compound);
			Server.sendData(player, EnumPacketClient.BUILDER_SETTING, CommonProxy.dataBuilder.get(id).getNbt());
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.BuilderSetting, null, id, 0, 0);
		} else if (type == EnumPacketServer.BuilderSetting) {
			NBTTagCompound compound = Server.readNBT(buffer);
			int id = compound.getInteger("ID");
			if (!CommonProxy.dataBuilder.containsKey(id)) {
				CommonProxy.dataBuilder.put(id, new BuilderData());
			}
			CommonProxy.dataBuilder.get(id).read(compound);
			if (player.getHeldItemMainhand().isEmpty()
					|| !(player.getHeldItemMainhand().getItem() instanceof ItemBuilder)
					|| !player.getHeldItemMainhand().hasTagCompound()) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			NBTTagCompound nbtStack = player.getHeldItemMainhand().getTagCompound();
			if (nbtStack == null) {
				player.getHeldItemMainhand().setTagCompound(nbtStack = new NBTTagCompound());
			}
			for (String key : compound.getKeySet()) {
				nbtStack.setTag(key, compound.getTag(key));
			}
		} else if (type == EnumPacketServer.DialogCategoryGet) {
			for (DialogCategory category2 : DialogController.instance.categories.values()) {
				Server.sendData(player, EnumPacketClient.SYNC_ADD, EnumSync.DialogCategoriesData,
						category2.writeNBT(new NBTTagCompound()));
			}
			Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.DialogCategoriesData, new NBTTagCompound());
		} else if (type == EnumPacketServer.QuestCategoryGet) {
			for (QuestCategory category : QuestController.instance.categories.values()) {
				Server.sendData(player, EnumPacketClient.SYNC_ADD, EnumSync.QuestCategoriesData,
						category.writeNBT(new NBTTagCompound()));
			}
			Server.sendData(player, EnumPacketClient.SYNC_END, EnumSync.QuestCategoriesData, new NBTTagCompound());
		} else if (type == EnumPacketServer.AnimationGet) {
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.animation.save(new NBTTagCompound()));
			AnimationController.getInstance().sendTo(player);
		} else if (type == EnumPacketServer.AnimationChange) {
			NBTTagCompound compound = Server.readNBT(buffer);
			if (compound.getKeySet().size() == 0) {
				AnimationController.getInstance().animations.clear();
			} else if (compound.hasKey("delete", 1) && compound.getBoolean("delete")) {
				AnimationController.getInstance().removeAnimation(compound.getInteger("ID"));
			} else if (compound.hasKey("save", 1) && compound.getBoolean("save")) {
				AnimationController.getInstance().save();
			} else {
				AnimationController.getInstance().loadAnimation(compound);
			}
			Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, EnumSync.AnimationData, compound);
		} else if (type == EnumPacketServer.AnimationSave) {
			npc.animation.load(Server.readNBT(buffer));
			npc.updateClient = true;
		} else if (type == EnumPacketServer.AnimationGlobalSave) {
			AnimationController aData = AnimationController.getInstance();
			NBTTagCompound compound = Server.readNBT(buffer);
			AnimationConfig ac = (AnimationConfig) aData.createNew();
			int id = ac.id;
			ac.readFromNBT(compound);
			ac.id = id;
			aData.save();
			player.sendMessage(new TextComponentTranslation("animation.message.save", ac.name));
			aData.sendTo(player);
		} else if (type == EnumPacketServer.PlayerDataSet) {
			EnumPlayerData datatype = EnumPlayerData.values()[buffer.readInt()];
			String playerName = Server.readString(buffer);
			EntityPlayer pl = (EntityPlayer) player.getServer().getPlayerList().getPlayerByUsername(playerName);
			PlayerData playerdata = null;
			if (pl == null) {
				playerdata = PlayerDataController.instance.getDataFromUsername(player.getServer(), playerName);
			} else {
				playerdata = PlayerData.get(pl);
			}
			if (playerdata == null) {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			boolean isChange = false;
			int t = buffer.readInt();
			switch (datatype) {
			case Quest: {
				int questId = buffer.readInt();
				Quest q = QuestController.instance.quests.get(questId);
				if (t == 0 && q != null && !playerdata.questData.finishedQuests.containsKey(questId)) {
					if (playerdata.questData.activeQuests.containsKey(questId)) {
						playerdata.questData.activeQuests.remove(questId);
					}
					if (q.repeat == EnumQuestRepeat.RLDAILY || q.repeat == EnumQuestRepeat.RLWEEKLY) {
						playerdata.questData.finishedQuests.put(questId, System.currentTimeMillis());
					} else {
						playerdata.questData.finishedQuests.put(questId, player.world.getTotalWorldTime());
					}
					isChange = true;
				} else if (t == 1 && (playerdata.questData.finishedQuests.containsKey(questId)
						|| playerdata.questData.activeQuests.containsKey(questId))) {
					playerdata.questData.finishedQuests.remove(questId);
					playerdata.questData.activeQuests.remove(questId);
					isChange = true;
				} else if (t == 3) {
					playerdata.questData.finishedQuests.clear();
					playerdata.questData.activeQuests.clear();
					isChange = true;
				}
				break;
			}
			case Dialog: {
				int dialogId = buffer.readInt();
				Dialog d = DialogController.instance.dialogs.get(dialogId);
				if (t == 0 && d != null && !playerdata.dialogData.dialogsRead.contains(dialogId)) {
					playerdata.dialogData.dialogsRead.add(dialogId);
					isChange = true;
				} else if (t == 1 && playerdata.dialogData.dialogsRead.contains(dialogId)) {
					playerdata.dialogData.dialogsRead.remove(dialogId);
					isChange = true;
				} else if (t == 3) {
					playerdata.dialogData.dialogsRead.clear();
					isChange = true;
				}
				break;
			}
			case Transport: {
				int locationId = buffer.readInt();
				TransportLocation l = TransportController.getInstance().getTransport(locationId);
				if (t == 0 && l != null && !playerdata.transportData.transports.contains(locationId)) {
					playerdata.transportData.transports.add(locationId);
					isChange = true;
				} else if (t == 1 && playerdata.transportData.transports.contains(locationId)) {
					playerdata.transportData.transports.remove(locationId);
					isChange = true;
				} else if (t == 3) {
					playerdata.transportData.transports.clear();
					isChange = true;
				}
				break;
			}
			case Bank: {
				int bankId = buffer.readInt();
				BankData bd = playerdata.bankData.get(bankId);
				if (t == 0 && bd != null && bd.bank != null) {
					bd.save();
					isChange = true;
				} else if (t == 1) {
					playerdata.bankData.remove(bankId);
					isChange = true;
				} else if (t == 3) {
					bd.clear();
					bd.save();
					isChange = true;
				}
				break;
			}
			case Factions: {
				int factionId = buffer.readInt();
				Faction f = FactionController.instance.factions.get(factionId);
				if (t == 0 && f != null && !playerdata.factionData.factionData.containsKey(factionId)) {
					playerdata.factionData.factionData.put(factionId, f.defaultPoints);
					isChange = true;
				} else if (t == 1 && playerdata.factionData.factionData.containsKey(factionId)) {
					playerdata.factionData.factionData.remove(factionId);
					isChange = true;
				} else if (t == 2 && playerdata.factionData.factionData.containsKey(factionId)) {
					playerdata.factionData.factionData.put(factionId, buffer.readInt());
					isChange = true;
				} else if (t == 3) {
					playerdata.factionData.factionData.clear();
					isChange = true;
				}
				break;
			}
			case Game: {
				if (t == 3) {
					playerdata.game.setMoney(0L);
					playerdata.game.marketData.clear();
				} else {
					NBTTagCompound compound = Server.readNBT(buffer);
					playerdata.game.readFromNBT(compound);
				}
				isChange = true;
				break;
			}
			default: {
				CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
				return;
			}
			}
			if (isChange) {
				playerdata.save(true);
				if (pl != null) {
					pl.sendMessage(new TextComponentTranslation("message.change.mod.data"));
				}
				NoppesUtilServer.sendPlayerData(datatype, player, playerName);
			}
		} else if (type == EnumPacketServer.PlayerData—leaning) {
			long time = buffer.readLong();
			File dirGame = CustomNpcs.getWorldSaveDirectory().getParentFile();
			File dirMod = CustomNpcs.getWorldSaveDirectory("playerdata");

			int i = 0, s = dirMod.listFiles().length;
			List<String> opn = Lists.<String>newArrayList(player.getServer().getPlayerList().getOnlinePlayerNames());
			for (File dir : dirMod.listFiles()) {
				if (!dir.isDirectory()) {
					if (dir.getName().endsWith(".json")) {
						AdditionalMethods.removeFile(dir);
					}
					continue;
				}
				String uuid = dir.getName();
				boolean needDelete = false;
				for (File file : dir.listFiles()) {
					if (file.getName().endsWith(".json") && file.lastModified() < time) {
						String name = file.getName().substring(0, file.getName().length() - 5);
						needDelete = !opn.contains(name);
						break;
					}
				}
				if (needDelete) {
					dir.delete(); // delete mod data
					File advancements = new File(dirGame, "advancements/" + uuid);
					if (advancements.exists()) {
						advancements.delete();
					}
					File stats = new File(dirGame, "stats/" + uuid);
					if (stats.exists()) {
						stats.delete();
					}
					File playerdata = new File(dirGame, "playerdata/" + uuid + ".dat");
					if (playerdata.exists()) {
						playerdata.delete();
					}
					i++;
				}
			}
			if (i > 0) {
				player.sendMessage(new TextComponentTranslation("message.data.cleaning.true", "" + i, "" + s));
				NoppesUtilServer.sendPlayerData(EnumPlayerData.Players, player, null);
			} else {
				player.sendMessage(new TextComponentTranslation("message.data.cleaning.false", "" + s));
			}
		} else if (type == EnumPacketServer.PlayerMailsGet) {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setInteger("LettersBeDeleted", CustomNpcs.MailTimeWhenLettersWillBeDeleted);
			compound.setIntArray("LettersBeReceived", CustomNpcs.MailTimeWhenLettersWillBeReceived);
			compound.setIntArray("CostSendingLetter", CustomNpcs.MailCostSendingLetter);
			compound.setBoolean("SendToYourself", CustomNpcs.MailSendToYourself);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.PlayerMailsSave) {
			NBTTagCompound compound = Server.readNBT(buffer);
			CustomNpcs.MailTimeWhenLettersWillBeDeleted = compound.getInteger("LettersBeDeleted");
			int[] vs = compound.getIntArray("LettersBeReceived");
			for (int i = 0; i < vs.length; i++) {
				CustomNpcs.MailTimeWhenLettersWillBeReceived[i] = vs[i];
			}
			vs = compound.getIntArray("CostSendingLetter");
			for (int i = 0; i < vs.length; i++) {
				CustomNpcs.MailCostSendingLetter[i] = vs[i];
			}
			CustomNpcs.MailSendToYourself = compound.getBoolean("SendToYourself");
			compound = new NBTTagCompound();
			compound.setInteger("LettersBeDeleted", CustomNpcs.MailTimeWhenLettersWillBeDeleted);
			compound.setIntArray("LettersBeReceived", CustomNpcs.MailTimeWhenLettersWillBeReceived);
			compound.setIntArray("CostSendingLetter", CustomNpcs.MailCostSendingLetter);
			compound.setBoolean("SendToYourself", CustomNpcs.MailSendToYourself);
			if (player.world.getMinecraftServer().getPlayerList().getPlayers().size() > 1) {
				for (EntityPlayerMP pl : player.world.getMinecraftServer().getPlayerList().getPlayers()) {
					Server.sendData(pl, EnumPacketClient.SYNC_UPDATE, EnumSync.MailData, compound);
				}
			}
		}
		CustomNpcs.debugData.endDebug("Server", type.toString(), "PacketHandlerServer_Received");
	}

	@SubscribeEvent
	public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).player;
		ByteBuf buffer = event.getPacket().payload();
		player.getServer().addScheduledTask(() -> {
			EnumPacketServer type = null;
			try {
				type = EnumPacketServer.values()[buffer.readInt()];
				if (CustomNpcs.OpsOnly && !NoppesUtilServer.isOp(player)) {
					this.warn(player, "tried to use custom npcs without being an op TypePacket: " + type);
					return;
				}
				if (!PacketHandlerServer.list.contains(type)) {
					LogWriter.debug("Received: " + type);
				}
				ItemStack item = player.inventory.getCurrentItem();
				EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
				List<EnumPacketServer> notHasPerm = Lists.<EnumPacketServer>newArrayList();
				if (!type.needsNpc || npc != null) {
					if (!type.hasPermission() || CustomNpcsPermissions.hasPermission(player, type.permission)) {
						if (!notHasPerm.contains(type) && !type.isExempt() && !this.allowItem(item, type)) {
							this.warn(player, "tried to use custom npcs without a tool in hand, possibly a hacker");
						} else {
							this.handlePacket(type, buffer, player, npc);
						}
					}
				} else {
					LogWriter.error("Error with EnumPacketServer." + type + ". Not found Editing Npc");
				}
			} catch (Exception e) {
				LogWriter.error("Error with EnumPacketServer." + type, e);
			} finally {
				buffer.release();
			}
		});
	}

	private void warn(EntityPlayer player, String warning) {
		player.getServer().logWarning(player.getName() + ": " + warning);
	}

}
