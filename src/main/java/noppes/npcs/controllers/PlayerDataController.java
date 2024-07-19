package noppes.npcs.controllers;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

public class PlayerDataController {

	public static PlayerDataController instance;

	public PlayerDataController() {
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.startDebug("Common", null, "loadPlayersData");
		}
		PlayerDataController.instance = this;
		File dir = CustomNpcs.getWorldSaveDirectory("playerdata");
		if (dir == null) { return; }
        for (File playerDir : Objects.requireNonNull(dir.listFiles())) {
			// OLD
			if (!playerDir.isDirectory() && playerDir.getName().endsWith(".json")) {
				try {
					NBTTagCompound nbt = NBTJsonUtil.LoadFile(playerDir);
					String uuid = "nouuidplayer", name = "nonameplayer";
					if (nbt.hasKey("PlayerName", 8) && !nbt.getString("PlayerName").isEmpty()) {
						name = nbt.getString("PlayerName");
					}
					if (nbt.hasKey("UUID", 8) && !nbt.getString("UUID").isEmpty()) {
						uuid = nbt.getString("UUID");
					}

					// banks
					File banksDirTemp = CustomNpcs.getWorldSaveDirectory("playerdata/" + uuid + "/banks");
					if (banksDirTemp == null) { return; }
                    if (!banksDirTemp.exists()) {
						banksDirTemp.mkdirs();
					}
					if (nbt.hasKey("BankData", 9)) {
						for (int i = 0; i < nbt.getTagList("BankData", 10).tagCount(); i++) {
							NBTTagCompound nbtOldBank = nbt.getTagList("BankData", 10).getCompoundTagAt(i);
							NBTTagCompound nbtBD = new NBTTagCompound();
							int bankID = nbtOldBank.getInteger("DataBankId");
							nbtBD.setInteger("id", bankID);
							int maxCells = nbtOldBank.getInteger("UnlockedSlots");
							NBTTagList list = new NBTTagList();
							for (int c = 0; c < nbtOldBank.getTagList("BankInv", 10).tagCount(); c++) {
								NBTTagCompound nbtOldCeil = nbtOldBank.getTagList("BankInv", 10).getCompoundTagAt(c);
								int ceilID = nbtOldCeil.getInteger("Slot");
								if (ceilID >= maxCells) {
									continue;
								}
								NBTTagCompound nbtCeil = new NBTTagCompound();
								int slots = 27;
								for (int u = 0; u < nbtOldBank.getTagList("UpdatedSlots", 10).tagCount(); u++) {
									if (nbtOldBank.getTagList("UpdatedSlots", 10).getCompoundTagAt(u)
											.getInteger("Slot") == ceilID) {
										if (nbtOldBank.getTagList("UpdatedSlots", 10).getCompoundTagAt(u)
												.getBoolean("Boolean")) {
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
								try {
									bankFile.createNewFile();
								} catch (Exception e) { LogWriter.error("Error:", e); }
							}
							try {
								CompressedStreamTools.writeCompressed(nbtBD, Files.newOutputStream(bankFile.toPath()));
							} catch (Exception e) { LogWriter.error("Error:", e); }
						}
					}

					// main
					File playerDirTemp = new File(dir, uuid);
					if (!playerDirTemp.exists()) {
						playerDirTemp.mkdirs();
					}
					File tempFile = new File(playerDirTemp, name + ".json");
					if (!tempFile.exists()) {
						try {
							tempFile.createNewFile();
						} catch (Exception e) { LogWriter.error("Error:", e); }
					}
					try {
						nbt.removeTag("BankData");
						NBTJsonUtil.SaveFile(tempFile, nbt);
					} catch (Exception e) { LogWriter.error("Error:", e); }

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
		PlayerData data = this.getDataFromUsername(server, username);
		if (data != null) {
			data.mailData.addMail(mail);
			data.save(false);
		}
	}

	public PlayerData getDataFromUsername(MinecraftServer server, String user_name_or_uuid) {
		EntityPlayer player = server.getPlayerList().getPlayerByUsername(user_name_or_uuid);
		if (player == null) {
			try {
				player = server.getPlayerList().getPlayerByUUID(UUID.fromString(user_name_or_uuid));
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
		PlayerData data = null;
		if (player == null) {
			File playerDir = this.getPlayerDirectory(user_name_or_uuid);
			if (playerDir != null) {
				data = new PlayerData();
				for (File f : Objects.requireNonNull(playerDir.listFiles())) {
					if (f.isFile() && f.getName().endsWith(".json")) {
						try {
							NBTTagCompound nbt = NBTJsonUtil.LoadFile(f);
							if (!nbt.hasKey("GameData", 10)) {
								continue;
							}
							data.setNBT(nbt);
							data.uuid = playerDir.getName();
							if (data.playername == null || data.playername.isEmpty()) {
								data.playername = f.getName().substring(0, f.getName().lastIndexOf("."));
							}
						} catch (Exception e) { LogWriter.error("Error:", e); }
					}
				}
			}
		} else {
			data = PlayerData.get(player);
		}
		return data;
	}

	private File getPlayerDirectory(String user_name_or_uuid) {
		for (File playerDir : Objects.requireNonNull(Objects.requireNonNull(CustomNpcs.getWorldSaveDirectory("playerdata")).listFiles())) {
			if (!playerDir.isDirectory()) {
				continue;
			}
			if (playerDir.getName().equalsIgnoreCase(user_name_or_uuid)) {
				return playerDir;
			}
			for (File file : Objects.requireNonNull(playerDir.listFiles())) {
				if (file.isFile() && file.getName().endsWith(".json")
						&& file.getName().replace(".json", "").equalsIgnoreCase(user_name_or_uuid)) {
					return playerDir;
				}
			}
		}
		return null;
	}

	public List<String> getPlayerNames() {
		List<String> list = Lists.newArrayList();
		for (File playerDir : Objects.requireNonNull(Objects.requireNonNull(CustomNpcs.getWorldSaveDirectory("playerdata")).listFiles())) {
			if (!playerDir.isDirectory()) {
				continue;
			}
			for (File file : Objects.requireNonNull(playerDir.listFiles())) {
				if (file.isFile() && file.getName().endsWith(".json")) {
					list.add(file.getName().replace(".json", ""));
					break;
				}
			}
		}
		return list;
	}

	public List<PlayerData> getPlayersData(ICommandSender sender, String username) throws CommandException {
		ArrayList<PlayerData> list = new ArrayList<>();
		List<EntityPlayerMP> players = EntitySelector.matchEntities(sender, username,
				EntityPlayerMP.class);
		if (players.isEmpty()) {
			PlayerData data = this.getDataFromUsername(Objects.requireNonNull(sender.getServer()), username);
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
		if (playerDir == null) {
			return "";
		}
		return playerDir.getName();
	}

}
