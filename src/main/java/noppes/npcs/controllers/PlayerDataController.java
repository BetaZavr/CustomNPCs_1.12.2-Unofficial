package noppes.npcs.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.NBTJsonUtil;
import noppes.npcs.util.NBTJsonUtil.JsonException;

public class PlayerDataController {
	
	public static PlayerDataController instance;

	public PlayerDataController() {
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.startDebug("Common", null, "loadPlayersData");
		}
		PlayerDataController.instance = this;
		File dir = CustomNpcs.getWorldSaveDirectory("playerdata");
		for (File playerDir : dir.listFiles()) {
			// OLD
			if (!playerDir.isDirectory() && CustomNpcs.FixUpdateFromPre_1_12 && playerDir.getName().endsWith(".json")) {
				try {
					NBTTagCompound nbt = NBTJsonUtil.LoadFile(playerDir);
					String uuid = "nouuidplayer", name = "nonameplayer";
					if (nbt.hasKey("PlayerName", 8) && !nbt.getString("PlayerName").isEmpty()) { name = nbt.getString("PlayerName"); }
					if (nbt.hasKey("UUID", 8) && !nbt.getString("UUID").isEmpty()) { uuid = nbt.getString("UUID"); }
					
					// banks
					File banksDirTemp = CustomNpcs.getWorldSaveDirectory("playerdata/"+uuid+"/banks");
					if (!banksDirTemp.exists()) { banksDirTemp.mkdirs(); }
					if (nbt.hasKey("BankData", 9)) {
						for (int i = 0; i < nbt.getTagList("BankData", 10).tagCount(); i++) {
							NBTTagCompound nbtOldBank = nbt.getTagList("BankData", 10).getCompoundTagAt(i);
							NBTTagCompound nbtBD = new NBTTagCompound();
							int bankID = nbtOldBank.getInteger("DataBankId");
							nbtBD.setInteger("id", bankID);
							int maxCeils = nbtOldBank.getInteger("UnlockedSlots");
							NBTTagList list = new NBTTagList();
							for (int c = 0; c < nbtOldBank.getTagList("BankInv", 10).tagCount(); c++) {
								NBTTagCompound nbtOldCeil = nbtOldBank.getTagList("BankInv", 10).getCompoundTagAt(c);
								int ceilID = nbtOldCeil.getInteger("Slot");
								if (ceilID >= maxCeils) { continue; }
								NBTTagCompound nbtCeil = new NBTTagCompound();
								int slots = 27;
								for (int u = 0; u < nbtOldBank.getTagList("UpdatedSlots", 10).tagCount(); u++) {
									if (nbtOldBank.getTagList("UpdatedSlots", 10).getCompoundTagAt(u).getInteger("Slot")==ceilID) {
										if (nbtOldBank.getTagList("UpdatedSlots", 10).getCompoundTagAt(u).getBoolean("Boolean")) {
											slots = 54;
										}
										break;
									}
								}
								NpcMiscInventory inv = new NpcMiscInventory(slots);
								inv.setFromNBT(nbtOldCeil.getCompoundTag("BankItems"));
								nbtCeil.setInteger("ceil", ceilID);
								nbtCeil.setInteger("slots", slots);
								NBTTagCompound invNbt = inv.getToNBT();
								nbtCeil.setTag("NpcMiscInv", invNbt.getTag("NpcMiscInv"));
								list.appendTag(nbtCeil);
							}
							nbtBD.setTag("ceils", list);
							File bankFile = new File(banksDirTemp, bankID + ".dat");
							if (!bankFile.exists()) {
								try { bankFile.createNewFile(); } catch (Exception e) { e.printStackTrace(); }
							}
							try { CompressedStreamTools.writeCompressed(nbtBD, new FileOutputStream(bankFile)); }
							catch (Exception e) { e.printStackTrace(); }
						}
					}

					// main
					File playerDirTemp = new File(dir, uuid);
					if (!playerDirTemp.exists()) { playerDirTemp.mkdirs(); }
					File tempFile = new File(playerDirTemp, name + ".json");
					if (!tempFile.exists()) {
						try { tempFile.createNewFile(); } catch (Exception e) { e.printStackTrace(); }
					}
					try {
						nbt.removeTag("BankData");
						NBTJsonUtil.SaveFile(tempFile, nbt);
					}
					catch (Exception e) { e.printStackTrace(); }
					
					AdditionalMethods.removeFile(playerDir);
				} catch (Exception e) {
					LogWriter.error("Error loading Old file: " + playerDir.getAbsolutePath(), e);
				}
			}
		}
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.endDebug("Common", null, "loadPlayersData");
		}
	}

	public void addPlayerMessage(MinecraftServer server, String username, PlayerMail mail) {
		mail.time = System.currentTimeMillis();
		PlayerData data = this.getDataFromUsername(server, username);
		data.mailData.playermail.add(mail.copy());
		data.save(false);
	}

	public PlayerData getDataFromUsername(MinecraftServer server, String username) {
		EntityPlayer player = (EntityPlayer) server.getPlayerList().getPlayerByUsername(username);
		PlayerData data = null;
		if (player == null) {
			File playerDir = this.getPlayerDirectory(username);
			if (playerDir != null) {
				data = new PlayerData();
				File file = new File(playerDir, username + ".json");
				try { data.setNBT(NBTJsonUtil.LoadFile(file)); }
				catch (IOException | JsonException e) { e.printStackTrace(); }
				data.uuid = playerDir.getName();
				data.playername = username;
			}
		}
		else { data = PlayerData.get(player); }
		return data;
	}

	private File getPlayerDirectory(String user_name_or_uuid) {
		for (File playerDir : CustomNpcs.getWorldSaveDirectory("playerdata").listFiles()) {
			if (!playerDir.isDirectory()) { continue; }
			if (playerDir.getName().equals(user_name_or_uuid)) {
				return playerDir;
			}
			for (File file : playerDir.listFiles()) {
				if (file.isFile() && file.getName().endsWith(".json") && file.getName().replace(".json", "").equals(user_name_or_uuid)) {
					return playerDir;
				}
			}
		}
		return null;
	}

	public List<PlayerData> getPlayersData(ICommandSender sender, String username) throws CommandException {
		ArrayList<PlayerData> list = new ArrayList<PlayerData>();
		List<EntityPlayerMP> players = (List<EntityPlayerMP>) EntitySelector.matchEntities(sender, username, EntityPlayerMP.class);
		if (players.isEmpty()) {
			PlayerData data = this.getDataFromUsername(sender.getServer(), username);
			if (data != null) {
				list.add(data);
			}
		} else {
			for (EntityPlayer player : players) {
				list.add(PlayerData.get(player));
			}
		}
		return list;
	}

	public String hasPlayer(String user_name_or_uuid) {
		File playerDir = this.getPlayerDirectory(user_name_or_uuid);
		if (playerDir==null) { return ""; }
		return playerDir.getName();
	}

	public List<String> getPlayerNames() {
		List<String> list = Lists.<String>newArrayList();
		for (File playerDir : CustomNpcs.getWorldSaveDirectory("playerdata").listFiles()) {
			if (!playerDir.isDirectory()) { continue; }
			for (File file : playerDir.listFiles()) {
				if (file.isFile() && file.getName().endsWith(".json")) {
					list.add(file.getName().replace(".json", ""));
					break;
				}
			}
		}
		return list;
	}

}
