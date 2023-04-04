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
import noppes.npcs.constants.EnumCompanionStage;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerData;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.controllers.DialogController;
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
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.ForgeScriptData;
import noppes.npcs.controllers.data.Marcet;
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
import noppes.npcs.roles.JobSpawner;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.roles.RoleTransporter;
import noppes.npcs.roles.data.SpawnNPCData;
import noppes.npcs.schematics.Schematic;
import noppes.npcs.schematics.SchematicWrapper;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.BuilderData;
import noppes.npcs.util.IPermission;

public class PacketHandlerServer {
	
	@SubscribeEvent
	public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).player;
		if (CustomNpcs.OpsOnly && !NoppesUtilServer.isOp(player)) {
			this.warn(player, "tried to use custom npcs without being an op");
			return;
		}
		ByteBuf buffer = event.getPacket().payload();
		player.getServer().addScheduledTask(() -> {
			EnumPacketServer type = null;
			try {
				type = EnumPacketServer.values()[buffer.readInt()];
				LogWriter.debug("Received: " + type);
				ItemStack item = player.inventory.getCurrentItem();
				EntityNPCInterface npc = NoppesUtilServer.getEditingNpc(player);
				List<EnumPacketServer> notHasPerm = Lists.<EnumPacketServer>newArrayList();
				if (!type.needsNpc || npc!= null) {
					if (!type.hasPermission() || CustomNpcsPermissions.hasPermission(player, type.permission)) {
						if (!notHasPerm.contains(type) && !type.isExempt() && !this.allowItem(item, type)) {
							this.warn(player, "tried to use custom npcs without a tool in hand, possibly a hacker");
						} else {
							this.handlePacket(type, buffer, player, npc);
						}
					}
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

	@SuppressWarnings("unused")
	private void handlePacket(EnumPacketServer type, ByteBuf buffer, EntityPlayerMP player, EntityNPCInterface npc)
			throws Exception {
		CustomNpcs.debugData.startDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
		if (type == EnumPacketServer.Delete) {
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
			NoppesUtilServer.sendBank(player, bank);
		} else if (type == EnumPacketServer.BankSave) {
			Bank bank = new Bank();
			bank.readEntityFromNBT(Server.readNBT(buffer));
			BankController.getInstance().saveBank(bank);
			NoppesUtilServer.sendBankDataAll(player);
			NoppesUtilServer.sendBank(player, bank);
		} else if (type == EnumPacketServer.BankRemove) {
			BankController.getInstance().removeBank(buffer.readInt());
			NoppesUtilServer.sendBankDataAll(player);
			NoppesUtilServer.sendBank(player, new Bank());
		} else if (type == EnumPacketServer.RemoteMainMenu) {
			Entity entity = player.world.getEntityByID(buffer.readInt());
			if (entity == null || !(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.MainMenuDisplay, (EntityNPCInterface) entity);
		} else if (type == EnumPacketServer.RemoteDelete) {
			Entity entity = player.world.getEntityByID(buffer.readInt());
			if (entity == null || !(entity instanceof EntityLivingBase)) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			if (entity instanceof EntityNPCInterface) {
				npc = (EntityNPCInterface) entity;
				npc.delete();
			}
			else { entity.setDead();}
			NoppesUtilServer.deleteEntity((EntityLivingBase) entity, player);
			NoppesUtilServer.sendNearbyEntitys(player, buffer.readBoolean());
		} else if (type == EnumPacketServer.RemoteNpcsGet) {
			NoppesUtilServer.sendNearbyEntitys(player, buffer.readBoolean());
			Server.sendData(player, EnumPacketClient.SCROLL_SELECTED, CustomNpcs.FreezeNPCs ? "Unfreeze Npcs" : "Freeze Npcs");
		} else if (type == EnumPacketServer.RemoteFreeze) {
			CustomNpcs.FreezeNPCs = !CustomNpcs.FreezeNPCs;
			player.sendMessage(new TextComponentString(CustomNpcs.FreezeNPCs ? "Freeze Npcs" : "Unfreeze Npcs"));
			Server.sendData(player, EnumPacketClient.SCROLL_SELECTED, CustomNpcs.FreezeNPCs ? "Freeze Npcs" : "Unfreeze Npcs");
		} else if (type == EnumPacketServer.RemoteReset) {
			Entity entity = player.world.getEntityByID(buffer.readInt());
			if (entity == null || !(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			npc = (EntityNPCInterface) entity;
			npc.reset();
		} else if (type == EnumPacketServer.RemoteTpToNpc) {
			Entity entity = player.world.getEntityByID(buffer.readInt());
			if (entity == null || !(entity instanceof EntityLivingBase)) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
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
			Server.sendData(player, EnumPacketClient.GUI_UPDATE, new Object[0]);
		} else if (type == EnumPacketServer.DialogCategoryRemove) {
			DialogController.instance.removeCategory(buffer.readInt());
			Server.sendData(player, EnumPacketClient.GUI_UPDATE, new Object[0]);
		} else if (type == EnumPacketServer.DialogSave) {
			DialogCategory category = DialogController.instance.categories.get(buffer.readInt());
			if (category == null) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			Dialog dialog = new Dialog(category);
			dialog.readNBT(Server.readNBT(buffer));
			DialogController.instance.saveDialog(category, dialog);
			Server.sendData(player, EnumPacketClient.GUI_UPDATE, new Object[0]);
		} else if (type == EnumPacketServer.DialogRemove) {
			Dialog dialog = DialogController.instance.dialogs.get(buffer.readInt());
			if (dialog != null && dialog.category != null) {
				DialogController.instance.removeDialog(dialog);
				Server.sendData(player, EnumPacketClient.GUI_UPDATE, new Object[0]);
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
			if (slot<0 || slot>=npc.dialogs.length) { return; }
			int[] newIDs = new int[npc.dialogs.length-1];
			for (int i=0, j=0; i<npc.dialogs.length; i++) {
				if (i==slot) { continue;}
				newIDs[j] = npc.dialogs[i];
				j++;
			}
			npc.dialogs = newIDs;
			NoppesUtilServer.sendNpcDialogs(player);
			//npc.dialogs.remove(buffer.readInt());
		} else if (type == EnumPacketServer.DialogNpcMove) {
			NoppesUtilServer.moveNpcDialogs(player, buffer.readInt(), buffer.readBoolean());
		} else if (type == EnumPacketServer.SpawnerNpcMove) {
			NoppesUtilServer.moveNpcSpawn(player, buffer.readInt(), buffer.readBoolean(), buffer.readBoolean());
		}  else if (type == EnumPacketServer.QuestCategorySave) {
			QuestCategory category = new QuestCategory();
			category.readNBT(Server.readNBT(buffer));
			QuestController.instance.saveCategory(category);
			Server.sendData(player, EnumPacketClient.GUI_UPDATE, new Object[0]);
		} else if (type == EnumPacketServer.QuestCategoryRemove) {
			QuestController.instance.removeCategory(buffer.readInt());
			Server.sendData(player, EnumPacketClient.GUI_UPDATE, new Object[0]);
		} else if (type == EnumPacketServer.QuestSave) {
			QuestCategory category = QuestController.instance.categories.get(buffer.readInt());
			if (category == null) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			Quest quest = new Quest(category);
			quest.readNBT(Server.readNBT(buffer));
			QuestController.instance.saveQuest(category, quest);
			Server.sendData(player, EnumPacketClient.GUI_UPDATE, new Object[0]);
			Server.sendToAll(CustomNpcs.Server, EnumPacketClient.SYNC_UPDATE, 3,
					category.writeNBT(new NBTTagCompound()));
		} else if (type == EnumPacketServer.QuestReset) {
			Quest quest = new Quest(null);
			quest.readNBT(Server.readNBT(buffer));
			NoppesUtilServer.setEditingQuest(player, quest);
			int taskPos = buffer.readInt();
			int itemSlot = buffer.readInt();
			if (itemSlot >= 0) {
				NoppesUtilServer.sendOpenGui(player, EnumGuiType.QuestTypeItem, npc, taskPos, itemSlot, 0);
			}
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
		} else if (type == EnumPacketServer.QuestDialogGetTitle) { // Changed
			Dialog dialog = DialogController.instance.dialogs.get(buffer.readInt());
			if (dialog == null) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			NBTTagCompound compound = new NBTTagCompound();
			compound.setString("Title", dialog.title);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.QuestRemove) {
			Quest quest = QuestController.instance.quests.get(buffer.readInt());
			if (quest != null) {
				QuestController.instance.removeQuest(quest);
				Server.sendData(player, EnumPacketClient.GUI_UPDATE, new Object[0]);
			}
		} else if (type == EnumPacketServer.TransportCategoriesGet) {
			NoppesUtilServer.sendTransportCategoryData(player);
		} else if (type == EnumPacketServer.TransportCategorySave) {
			TransportController.getInstance().saveCategory(Server.readString(buffer), buffer.readInt());
		} else if (type == EnumPacketServer.TransportCategoryRemove) {
			TransportController.getInstance().removeCategory(buffer.readInt());
			NoppesUtilServer.sendTransportCategoryData(player);
		} else if (type == EnumPacketServer.TransportRemove) {
			int id = buffer.readInt();
			TransportLocation loc = TransportController.getInstance().removeLocation(id);
			if (loc != null) {
				NoppesUtilServer.sendTransportData(player, loc.category.id);
			}
		} else if (type == EnumPacketServer.TransportsGet) {
			NoppesUtilServer.sendTransportData(player, buffer.readInt());
		} else if (type == EnumPacketServer.TransportSave) {
			int cat = buffer.readInt();
			TransportLocation location = TransportController.getInstance().saveLocation(cat, Server.readNBT(buffer),
					player, npc);
			if (location != null) {
				if (npc.advanced.role != 4) {
					CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
					return;
				}
				RoleTransporter role = (RoleTransporter) npc.roleInterface;
				role.setTransport(location);
			}
		} else if (type == EnumPacketServer.TransportGetLocation) {
			if (npc.advanced.role != 4) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			RoleTransporter role = (RoleTransporter) npc.roleInterface;
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
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
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
				return;
			}
			else if (EnumPlayerData.values()[id]==EnumPlayerData.Wipe) {
				List<String> list = Lists.<String>newArrayList();
				for (String username : PlayerDataController.instance.nameUUIDs.keySet()) {
					list.add(username);
				}
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
					// EnumPlayerData.Players
					File file = new File(CustomNpcs.getWorldSaveDirectory("playerdata"), playerdata.uuid + ".json");
					if (file.exists()) { file.delete(); }
					if (pl != null) {
						playerdata.setNBT(new NBTTagCompound());
						SyncController.syncPlayer((EntityPlayerMP) pl);
					} else {
						PlayerDataController.instance.nameUUIDs.remove(name);
					}
					playerdata.save(true);
				}
				NoppesUtilServer.sendPlayerData(EnumPlayerData.Players, player, null);
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
System.out.println("start size: "+npc.inventory.drops.size());
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.inventory.writeEntityToNBT(new NBTTagCompound()));
		} else if (type == EnumPacketServer.MainmenuInvSave) {
			npc.inventory.readEntityFromNBT(Server.readNBT(buffer));
			npc.updateAI = true;
			npc.updateClient = true;
Server.sendData(player, EnumPacketClient.GUI_DATA, npc.inventory.writeEntityToNBT(new NBTTagCompound()));
		} else if (type == EnumPacketServer.MainmenuInvDropSave) {
System.out.println("start size: "+npc.inventory.drops.size());
			int slot = buffer.readInt();
System.out.println("slot: "+slot);
			NBTTagCompound compound = Server.readNBT(buffer);
System.out.println("compound: "+compound);
			IItemStack stack = NpcAPI.Instance().getIItemStack(new ItemStack(compound.getCompoundTag("Item")));
System.out.println("stack: "+stack);
			if (slot == -1) { // add new
				if (stack.isEmpty()) { return; }
				DropSet drop = (DropSet) npc.inventory.addDropItem(stack, 1.0d);
				drop.load(compound);
System.out.println("add slot: "+slot+"; size: "+npc.inventory.drops.size());
			}
			else if (stack.isEmpty()) { // remove
System.out.println("remove slot: "+slot+" == "+npc.inventory.removeDrop(slot)+"; size: "+npc.inventory.drops.size());
				npc.inventory.removeDrop(slot);
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
			npc.advanced.readToNBT(Server.readNBT(buffer));
			npc.updateAI = true;
			npc.updateClient = true;
		} else if (type == EnumPacketServer.MainmenuAdvancedMarkData) {
			MarkData data = MarkData.get(npc);
			data.setNBT(Server.readNBT(buffer));
			data.syncClients();
		} else if (type == EnumPacketServer.JobSave) {
			NBTTagCompound original = npc.jobInterface.writeToNBT(new NBTTagCompound());
			NBTTagCompound compound = Server.readNBT(buffer);
			for (String name : compound.getKeySet()) {
				if (npc.advanced.job==6 && (name.equals("DataEntitysWhenAlive") || name.equals("DataEntitysWhenDead"))) {
					continue;
				}
				original.setTag(name, compound.getTag(name));
			}
			npc.jobInterface.readFromNBT(original);
			npc.updateClient = true;
		}
		else if (type == EnumPacketServer.JobClear) {
			if (!(npc.jobInterface instanceof JobSpawner)) { return; }
			((JobSpawner) npc.jobInterface).clear(buffer.readBoolean());
			
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("JobData", true);
			npc.jobInterface.writeToNBT(compound);
			((JobSpawner) npc.jobInterface).cleanCompound(compound);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if (type == EnumPacketServer.JobGet) {
			if (npc.jobInterface == null) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("JobData", true);
			npc.jobInterface.writeToNBT(compound);
			if (npc.advanced.job == 6) {
				((JobSpawner) npc.jobInterface).cleanCompound(compound);
			}
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.JobSpawnerAdd) {
			if (npc.advanced.job != 6) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			JobSpawner job = (JobSpawner) npc.jobInterface;
			boolean isServerSide = buffer.readBoolean();
			boolean isDead = buffer.readBoolean();
			int slot = buffer.readInt();
			if (isServerSide) {
				String name = Server.readString(buffer);
				int tab = buffer.readInt();
				SpawnNPCData sd = job.get(slot, isDead);
				if (sd==null) {
					sd = new SpawnNPCData();
					slot = job.size(isDead);
				}
				sd.compound = ServerCloneController.Instance.getCloneData(player, name, tab);
				if (sd.compound==null) { sd.compound = new NBTTagCompound(); }
				sd.compound.setString("ClonedName", name);
				sd.compound.setInteger("ClonedTab", tab);
				sd.typeClones = 2;
				job.readJobCompound(slot, isDead, sd.writeToNBT());
			} else {
				SpawnNPCData sd = job.readJobCompound(slot, isDead, Server.readNBT(buffer));
				int tab = sd.compound.getInteger("ClonedTab");
				sd.typeClones = (tab>0 && tab<10) ? 0 : 1;
			}
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("JobData", true);
			npc.jobInterface.writeToNBT(compound);
			job.cleanCompound(compound);
			compound.setInteger("SetPos", slot);
			compound.setBoolean("SetDead", isDead);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.RoleCompanionUpdate) {
			if (npc.advanced.role != 6) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			((RoleCompanion) npc.roleInterface).matureTo(EnumCompanionStage.values()[buffer.readInt()]);
			npc.updateClient = true;
		} else if (type == EnumPacketServer.JobSpawnerRemove) {
			if (npc.advanced.job != 6) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			JobSpawner job = (JobSpawner) npc.jobInterface;
			int slot = buffer.readInt();
			boolean isDead = buffer.readBoolean();
			job.removeSpawned(slot, isDead);
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("JobData", true);
			npc.jobInterface.writeToNBT(compound);
			job.cleanCompound(compound);
			compound.setInteger("SetPos", -1);
			compound.setBoolean("SetDie", isDead);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.RoleSave) {
			npc.roleInterface.readFromNBT(Server.readNBT(buffer));
			npc.updateClient = true;
		} else if (type == EnumPacketServer.RoleGet) {
			if (npc.roleInterface == null) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("RoleData", true);
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.roleInterface.writeToNBT(compound));
		} else if (type == EnumPacketServer.MerchantUpdate) {
			Entity entity = player.world.getEntityByID(buffer.readInt());
			if (entity == null || !(entity instanceof EntityVillager)) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
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
			player.openGui(CustomNpcs.instance, EnumGuiType.PlayerMailman.ordinal(), player.world, 1, 0, 0);
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
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.ais.writeToNBT(new NBTTagCompound()));
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
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			if (!server) { nbtData.setTag("EntityNBT", compound); }
			Entity entity = NoppesUtilServer.spawnClone(compound, x + 0.5, y + 1, z + 0.5, player.world);
			ItemStack stack = player.getHeldItemMainhand();
			NBTTagCompound nbt = null;
			if (stack!=null && stack.getItem()==CustomItems.cloner) {
				nbt = stack.getTagCompound();
				if (nbt==null) { stack.setTagCompound(nbt = new NBTTagCompound()); }
			}
			if (entity == null) {
				if (nbt!=null && nbt.hasKey("Settings")) {
					nbt.removeTag("Settings");
					player.openContainer.detectAndSendChanges();
				}
				player.sendMessage(new TextComponentString("Failed to create an entity out of your clone"));
			} else {
				if (nbt!=null) {
					if (!nbtData.hasKey("Name", 8) || nbtData.getString("Name").isEmpty()) {
						nbtData.setString("Name", entity.getName());
					}
					if (nbt.hasKey("Settings") && nbt.getCompoundTag("Settings").getString("Name").equals(nbtData.getString("Name"))) {
						return;
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
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
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
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.DimensionSettings) {
			int id = buffer.readInt();
			CustomWorldInfo wi = Server.readWorldInfo(buffer);
			if (id==0) {
				DimensionHandler.getInstance().createDimension(player, wi);
			} else {
				CustomWorldInfo cwi = (CustomWorldInfo) DimensionHandler.getInstance().getMCWorldInfo(id);
				if (cwi!=null) {
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
			NoppesUtilPlayer.teleportPlayer(player, coords.getX(), coords.getY(), coords.getZ(), dimension);
		} else if (type == EnumPacketServer.ScriptBlockDataGet) {
			TileEntity tile = player.world
					.getTileEntity(new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt()));
			if (!(tile instanceof TileScripted)) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			NBTTagCompound compound = ((TileScripted) tile).getNBT(new NBTTagCompound());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.ScriptItemDataGet) {
			ItemScriptedWrapper iw = (ItemScriptedWrapper) NpcAPI.Instance()
					.getIItemStack(player.getHeldItemMainhand());
			NBTTagCompound compound = iw.getMCNbt();
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.ScriptItemDataSave) {
			if (!player.isCreative()) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
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
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.ScriptForgeSave) {
			ScriptController.Instance.setForgeScripts(Server.readNBT(buffer));
		} else if (type == EnumPacketServer.ScriptPlayerGet) {
			NBTTagCompound compound = ScriptController.Instance.playerScripts.writeToNBT(new NBTTagCompound());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
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
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			TileScripted script = (TileScripted) tile;
			script.setNBT(Server.readNBT(buffer));
			script.lastInited = -1L;
		} else if (type == EnumPacketServer.ScriptDoorDataSave) {
			TileEntity tile = player.world
					.getTileEntity(new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt()));
			if (!(tile instanceof TileScriptedDoor)) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			TileScriptedDoor script = (TileScriptedDoor) tile;
			script.setNBT(Server.readNBT(buffer));
			script.lastInited = -1L;
		} else if (type == EnumPacketServer.ScriptDoorDataGet) {
			TileEntity tile = player.world
					.getTileEntity(new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt()));
			if (!(tile instanceof TileScriptedDoor)) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			NBTTagCompound compound = ((TileScriptedDoor) tile).getNBT(new NBTTagCompound());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		} else if (type == EnumPacketServer.SchematicsTile) {
			BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			TileBuilder tile = (TileBuilder) player.world.getTileEntity(pos);
			if (tile == null) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
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
			if (t instanceof TileBuilder) { // Builder Block
				TileBuilder tile = (TileBuilder) t;
				SchematicWrapper schem = tile.getSchematic();
				schem.init(pos.add(1, tile.yOffest, 1), player.world, tile.rotation * 90);
				SchematicController.Instance.build(tile.getSchematic(), player);
				player.world.setBlockToAir(pos);
			} else { // Builder Item
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
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
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
			}
		} else if (type == EnumPacketServer.NbtBookSaveEntity) {
			int entityId = buffer.readInt();
			NBTTagCompound compound = Server.readNBT(buffer);
			Entity entity = player.world.getEntityByID(entityId);
			if (entity != null) {
				entity.readFromNBT(compound);
			}
		}
		// New
		else if (type == EnumPacketServer.NbtBookSaveItem) {
			NBTTagCompound stackNBT = Server.readNBT(buffer);
			ItemStack stack = new ItemStack(stackNBT);
			if (stack==null || stack.isEmpty()) {
				player.sendMessage(new TextComponentTranslation("nbt.book.not.correct.nbt"));
				return;
			}
			player.inventory.offHandInventory.set(0, stack);
			player.openContainer.detectAndSendChanges();
			NBTTagCompound compound = new NBTTagCompound();
			compound.setBoolean("Item", true);
			compound.setTag("Data", stackNBT);
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if (type == EnumPacketServer.RecipesAddGroup) {
			int size = buffer.readInt();
			String group = Server.readString(buffer);
			if (size==3) {
				RecipeController.getInstance().globalList.put(group, Lists.<INpcRecipe>newArrayList());
			} else {
				RecipeController.getInstance().modList.put(group, Lists.<INpcRecipe>newArrayList());
			}
			NpcShapedRecipes recipe = new NpcShapedRecipes(group, "default", size, size, NonNullList.create(), ItemStack.EMPTY);
			recipe.global = (size==3);
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
					if (rec.getClass()==NpcShapedRecipes.class) {
						((NpcShapedRecipes) rec).group = group;
					} else {
						((NpcShapelessRecipes) rec).group = group;
					}
					IRecipe r = RecipeController.Registry.getValue(((IRecipe) rec).getRegistryName());
					if (r instanceof INpcRecipe) {
						if (r.getClass()==NpcShapedRecipes.class) {
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
					if (!rec.getName().equals(old)) { continue; }
					if (rec.getClass()==NpcShapedRecipes.class) {
						((NpcShapedRecipes) rec).name = name;
					} else {
						((NpcShapelessRecipes) rec).name = name;
					}
					IRecipe r = RecipeController.Registry.getValue(((IRecipe) rec).getRegistryName());
					if (r instanceof INpcRecipe) {
						if (r.getClass()==NpcShapedRecipes.class) {
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
			NBTTagCompound compound = Server.readNBT(buffer);
			boolean bo = buffer.readBoolean();
			if (bo) {
				if (npc.roleInterface instanceof RoleTrader) {
					((RoleTrader) npc.roleInterface).marcet = compound.getInteger("MarcetID");
				}
			} else {
				Marcet marcet = new Marcet();
				marcet.readEntityFromNBT(compound);
				MarcetController.getInstance().saveMarcet(marcet);
			}
		} else if (type == EnumPacketServer.TraderMarketGet) {
			MarcetController mData = MarcetController.getInstance();
			Server.sendData(player, EnumPacketClient.SET_MARCETS, mData.getNBT());
		} else if (type == EnumPacketServer.TraderMarketNew) {
			MarcetController mData = MarcetController.getInstance();
			Marcet marcet = mData.getMarcet(buffer.readInt());
			if (marcet == null) {
				marcet = mData.addMarcet();
			} // New Marcet
			else {
				marcet.addDeal();
			} // New Deal
			MarcetController.getInstance().saveMarcet(marcet);
			Server.sendData(player, EnumPacketClient.SET_MARCETS, mData.getNBT());
		} else if (type == EnumPacketServer.TraderMarketDel) {
			MarcetController mData = MarcetController.getInstance();
			Marcet marcet = mData.getMarcet(buffer.readInt());
			if (marcet == null) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
				return;
			}
			Deal deal = marcet.data.get(buffer.readInt());
			if (deal == null) { // Remove Marcet
				for (EntityPlayer listener : marcet.listeners) {
					Server.sendData((EntityPlayerMP) listener, EnumPacketClient.MARCET_CLOSE, marcet.id);
				}
				marcet.listeners.clear();
				mData.removeMarcet(marcet.id);
			} else {
				marcet.remove(deal.id);
				MarcetController.getInstance().saveMarcet(marcet);
			}
			Server.sendData(player, EnumPacketClient.SET_MARCETS, mData.getNBT());
		}
		else if (type == EnumPacketServer.GetClone) {
			NBTTagCompound npcNbt = null;
			if (buffer.readBoolean()) { // is Job Spawner
				if (npc!=null && npc.advanced.job == 6) {
					SpawnNPCData sd = ((JobSpawner) npc.jobInterface).get(buffer.readInt(), buffer.readBoolean());
					if (sd!=null && sd.compound!=null) {
						npcNbt = sd.compound;
						if (sd.typeClones==2) {
							npcNbt = ServerCloneController.Instance.getCloneData(player, sd.compound.getString("ClonedName"), sd.compound.getInteger("ClonedTab"));
						}
					}
				}
			}
			else { // get Server Clone
				npcNbt = ServerCloneController.Instance.getCloneData(player, Server.readString(buffer), buffer.readInt());
			}
			if (npcNbt!=null) {
				NBTTagCompound compound = new NBTTagCompound();
				compound.setTag("NPCData", npcNbt);
				Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
			}
		}
		else if (type == EnumPacketServer.ScriptPotionGet) { // New
			PotionScriptData data = ScriptController.Instance.potionScripts;
			NBTTagCompound compound = data.writeToNBT(new NBTTagCompound());
			compound.setTag("Languages", ScriptController.Instance.nbtLanguages());
			Server.sendData(player, EnumPacketClient.GUI_DATA, compound);
		}
		else if (type == EnumPacketServer.ScriptPotionSave) { // New
			ScriptController.Instance.setPotionScripts(Server.readNBT(buffer));
		}
		else if (type == EnumPacketServer.TeleportTo) { // New
			int dimensionId = buffer.readInt();
			BlockPos pos = new BlockPos(buffer.readInt(), buffer.readInt(), buffer.readInt());
			if (player.world.provider.getDimension()==dimensionId) {
				player.setPositionAndUpdate(pos.getX()+0.5d, pos.getY(), pos.getZ()+0.5d);
				return;
			}
			try { AdditionalMethods.teleportEntity(CustomNpcs.Server, player, dimensionId, pos); } catch (Exception e) { }
		}
		else if (type == EnumPacketServer.RegionData) { // New
			int t = buffer.readInt();
			BorderController bData = BorderController.getInstance();
			if (t==0) { // set reg id to item
				int regId = buffer.readInt();
				if (!bData.regions.containsKey(regId)) {
					bData.sendTo(player);
					return;
				}
				if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemBoundary) {
					NBTTagCompound compound = player.getHeldItemMainhand().getTagCompound();
					if (compound==null) { player.getHeldItemMainhand().setTagCompound(compound = new NBTTagCompound()); }
					compound.setInteger("RegionID", regId);
				}
			}
			else if (t==1) { // remove region
				int regId = buffer.readInt();
				if (!bData.regions.containsKey(regId)) {
					bData.sendTo(player);
					return;
				}
				if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemBoundary) {
					NBTTagCompound compound = player.getHeldItemMainhand().getTagCompound();
					if (compound==null) { player.getHeldItemMainhand().setTagCompound(compound = new NBTTagCompound()); }
					compound.removeTag("RegionID");
				}
				if (bData.removeRegion(regId)) {
					for (EntityPlayerMP p : CustomNpcs.Server.getPlayerList().getPlayers()) { bData.sendTo(p); }
				}
			}
			if (t==2) { // save region
				Zone3D reg = bData.loadRegion(Server.readNBT(buffer));
				if (reg!=null) {
					bData.saveRegions();
					bData.sendToAll(reg.id);
				}
			}
		}
		else if (type == EnumPacketServer.OpenBuilder) { // New
			if (player.getHeldItemMainhand().isEmpty() || !(player.getHeldItemMainhand().getItem() instanceof ItemBuilder) || !player.getHeldItemMainhand().hasTagCompound()) { return; }
			int id = player.getHeldItemMainhand().getTagCompound().getInteger("ID");
			NBTTagCompound compound = Server.readNBT(buffer);
			if (id != compound.getInteger("ID")) {
				id = compound.getInteger("ID");
				player.getHeldItemMainhand().getTagCompound().setInteger("ID", id);
			}
			if (!CommonProxy.dataBuilder.containsKey(id)) { CommonProxy.dataBuilder.put(id, new BuilderData()); }
			CommonProxy.dataBuilder.get(id).read(compound);
			Server.sendData(player, EnumPacketClient.BUILDER_SETTING, CommonProxy.dataBuilder.get(id).getNbt());
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.BuilderSetting, null, id, 0, 0);
		} else if (type == EnumPacketServer.BuilderSetting) {
			NBTTagCompound compound = Server.readNBT(buffer);
			int id = compound.getInteger("ID");
			if (!CommonProxy.dataBuilder.containsKey(id)) { CommonProxy.dataBuilder.put(id, new BuilderData()); }
			CommonProxy.dataBuilder.get(id).read(compound);
			if (player.getHeldItemMainhand().isEmpty() || !(player.getHeldItemMainhand().getItem() instanceof ItemBuilder) || !player.getHeldItemMainhand().hasTagCompound()) { return; }
			NBTTagCompound nbtStack = player.getHeldItemMainhand().getTagCompound();
			if (nbtStack==null) { player.getHeldItemMainhand().setTagCompound(nbtStack = new NBTTagCompound()); }
			for (String key : compound.getKeySet()) { nbtStack.setTag(key, compound.getTag(key)); }
		} else if (type == EnumPacketServer.DialogCategoryGet) {
			for (DialogCategory category2 : DialogController.instance.categories.values()) {
				Server.sendData(player, EnumPacketClient.SYNC_ADD, 5, category2.writeNBT(new NBTTagCompound()));
			}
			Server.sendData(player, EnumPacketClient.SYNC_END, 5, new NBTTagCompound());
		} else if (type == EnumPacketServer.QuestCategoryGet) {
			for (QuestCategory category : QuestController.instance.categories.values()) {
				Server.sendData(player, EnumPacketClient.SYNC_ADD, 3, category.writeNBT(new NBTTagCompound()));
			}
			Server.sendData(player, EnumPacketClient.SYNC_END, 3, new NBTTagCompound());
		}
		
		CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerServer_Received_"+type.toString());
	}
	
}
