package noppes.npcs.command;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.SyncController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.util.ValueUtil;

import javax.annotation.Nonnull;

public class CmdQuest extends CommandNoppesBase {

	@SubCommand(desc = "Finish a quest", usage = "<player> <quest>")
	public void finish(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String playername = args[0];
		int questid;
		try {
			questid = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			throw new CommandException("QuestID must be an integer");
		}
		List<PlayerData> data = PlayerDataController.instance.getPlayersData(sender, playername);
		if (data.isEmpty()) {
			throw new CommandException(String.format("Unknown player '%s'", playername));
		}
		Quest quest = QuestController.instance.quests.get(questid);
		if (quest == null) {
			throw new CommandException("Unknown QuestID");
		}
		for (PlayerData playerdata : data) {
			boolean hasFinishedQuest = playerdata.questData.finishedQuests.containsKey(questid);
			playerdata.questData.finishedQuests.put(questid, System.currentTimeMillis());
			if (playerdata.questData.activeQuests.containsKey(questid)) {
				playerdata.questData.activeQuests.remove(questid);
				hasFinishedQuest = false;
			}
			try {
				EntityPlayerMP player = server.getPlayerList().getPlayerByUsername(playername);
				if (!hasFinishedQuest && player != null) {
					Server.sendData(player, EnumPacketClient.MESSAGE, "quest.completed", quest.getTitle(), 2);
					Server.sendData(player, EnumPacketClient.CHAT, "quest.completed", ": ", quest.getTitle());
				}
			} catch (Exception e) { LogWriter.error("Error:", e); }
			playerdata.updateClient = true;
			playerdata.save(true);
		}
	}

	@Override
	public String getDescription() {
		return "Quest operations";
	}

	@Nonnull
	public String getName() {
		return "quest";
	}

	@SubCommand(desc = "get/set objectives for quests progress", usage = "<player> <quest> [objective] [value]", permission = 2)
	public void objective(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		EntityPlayer player = CommandBase.getPlayer(server, sender, args[0]);
		int questid;
		try {
			questid = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			throw new CommandException("QuestID must be an integer");
		}
		Quest quest = QuestController.instance.quests.get(questid);
		if (quest == null) {
			throw new CommandException("Unknown QuestID");
		}
		PlayerData data = PlayerData.get(player);
		if (!data.questData.activeQuests.containsKey(quest.id)) {
			throw new CommandException("Player doesnt have quest active");
		}
		IQuestObjective[] objectives = quest.questInterface.getObjectives(player);
		if (args.length == 2) {
			for (IQuestObjective ob : objectives) {
				sender.sendMessage(new TextComponentString(ob.getText()));
			}
			return;
		}
		int objective;
		try {
			objective = Integer.parseInt(args[2]);
		} catch (NumberFormatException ex2) {
			throw new CommandException("Objective must be an integer. Most often 0, 1 or 2");
		}
		if (objective < 0 || objective >= objectives.length) {
			throw new CommandException("Invalid objective number was given");
		}
		if (args.length == 3) {
			sender.sendMessage(new TextComponentString(objectives[objective].getText()));
			return;
		}
		IQuestObjective object = objectives[objective];
		String s = args[3];
		int value;
		try {
			value = Integer.parseInt(args[3]);
		} catch (NumberFormatException ex3) {
			throw new CommandException("Value must be an integer.");
		}
		if (s.startsWith("-") || s.startsWith("+")) {
			value = ValueUtil.correctInt(object.getProgress() + value, 0, object.getMaxProgress());
		}
		object.setProgress(value);
	}

	@SubCommand(desc = "reload quests from disk")
	public void reload(MinecraftServer server, ICommandSender sender, String[] args) {
		new QuestController().load();
		SyncController.syncAllQuests(server);
	}

	@SubCommand(desc = "Removes a quest from finished and active quests", usage = "<player> <quest>", permission = 2)
	public void remove(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String playername = args[0];
		int questid;
		try {
			questid = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			throw new CommandException("QuestID must be an integer");
		}
		List<PlayerData> data = PlayerDataController.instance.getPlayersData(sender, playername);
		if (data.isEmpty()) {
			throw new CommandException(String.format("Unknown player '%s'", playername));
		}
		Quest quest = QuestController.instance.quests.get(questid);
		if (quest == null) {
			throw new CommandException("Unknown QuestID");
		}
		for (PlayerData playerdata : data) {
			playerdata.questData.activeQuests.remove(questid);
			playerdata.questData.finishedQuests.remove(questid);
			playerdata.save(true);
		}
	}

	@SubCommand(desc = "Start a quest", usage = "<player> <quest>", permission = 2)
	public void start(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String playername = args[0];
		int questid;
		try {
			questid = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			throw new CommandException("QuestID must be an integer");
		}
		List<EntityPlayerMP> players = EntitySelector.matchEntities(sender, playername,
				EntityPlayerMP.class);
		if (players.isEmpty()) {
			throw new CommandException("Unknown player '%s'", playername);
		}
		Quest quest = QuestController.instance.quests.get(questid);
		if (quest == null) {
			throw new CommandException("Unknown QuestID");
		}
		for (EntityPlayerMP player : players) {
			PlayerQuestController.addActiveQuest(quest, player, true);
		}
	}

	@SubCommand(desc = "Stop a started quest", usage = "<player> <quest>", permission = 2)
	public void stop(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String playername = args[0];
		int questid;
		try {
			questid = Integer.parseInt(args[1]);
		} catch (NumberFormatException ex) {
			throw new CommandException("QuestID must be an integer");
		}
		List<PlayerData> data = PlayerDataController.instance.getPlayersData(sender, playername);
		if (data.isEmpty()) {
			throw new CommandException(String.format("Unknown player '%s'", playername));
		}
		Quest quest = QuestController.instance.quests.get(questid);
		if (quest == null) {
			throw new CommandException("Unknown QuestID");
		}
		for (PlayerData playerdata : data) {
			playerdata.questData.activeQuests.remove(questid);
			playerdata.save(true);
		}
	}

}
