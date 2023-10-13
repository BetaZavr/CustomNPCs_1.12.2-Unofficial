package noppes.npcs.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.PlayerBankData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.NBTJsonUtil;

public class PlayerDataController {
	
	public static PlayerDataController instance;
	public Map<String, String> nameUUIDs;

	public PlayerDataController() {
		if (CustomNpcs.VerboseDebug) {
			CustomNpcs.debugData.startDebug("Common", null, "loadPlayersData");
		}
		PlayerDataController.instance = this;
		File dir = CustomNpcs.getWorldSaveDirectory("playerdata");
		Map<String, String> map = new HashMap<String, String>();
		for (File file : dir.listFiles()) {
			if (!file.isDirectory()) {
				if (file.getName().endsWith(".json")) {
					try {
						NBTTagCompound compound = NBTJsonUtil.LoadFile(file);
						if (compound.hasKey("PlayerName") && !compound.getString("PlayerName").isEmpty()) {
							map.put(compound.getString("PlayerName"), file.getName().substring(0, file.getName().length() - 5));
						}
						else { AdditionalMethods.removeFile(file); }
					} catch (Exception e) {
						LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
					}
				}
			}
		}
		this.nameUUIDs = map;
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


	public PlayerBankData getBankData(PlayerBankData bankData, int bankId) {
		Bank bank = BankController.getInstance().getBank(bankId);
		if (!bankData.hasBank(bank.id)) { return null; }
		return bankData;
	}
	
	public PlayerBankData getBankData(EntityPlayer player, int bankId) {
		Bank bank = BankController.getInstance().getBank(bankId);
		PlayerBankData data = PlayerData.get(player).bankData;
		if (!data.hasBank(bank.id)) {
			data.loadNew(bank.id);
		}
		return data;
	}

	public PlayerData getDataFromUsername(MinecraftServer server, String username) {
		EntityPlayer player = (EntityPlayer) server.getPlayerList().getPlayerByUsername(username);
		PlayerData data = null;
		if (player == null) {
			for (String name : this.nameUUIDs.keySet()) {
				if (name.equalsIgnoreCase(username)) {
					data = new PlayerData();
					data.setNBT(PlayerData.loadPlayerData(this.nameUUIDs.get(name)));
					break;
				}
			}
		} else {
			data = PlayerData.get(player);
		}
		return data;
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

	public String hasPlayer(String username) {
		for (String name : this.nameUUIDs.keySet()) {
			if (name.equalsIgnoreCase(username)) {
				return name;
			}
		}
		return "";
	}
}
