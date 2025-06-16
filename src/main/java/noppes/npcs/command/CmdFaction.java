package noppes.npcs.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerFactionData;

import javax.annotation.Nonnull;

public class CmdFaction extends CommandNoppesBase {
	public List<PlayerData> data;
	public Faction selectedFaction;

	public int getRequiredPermissionLevel() {
		return 2;
	}

	@SubCommand(desc = "Add points", usage = "<points>")
	public void add(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		int points;
		try {
			points = Integer.parseInt(args[0]);
		} catch (NumberFormatException ex) {
			throw new CommandException(args[0]+" must be an integer");
		}
		int factionId = this.selectedFaction.id;
		for (PlayerData playerdata : this.data) {
			PlayerFactionData playerfactiondata = playerdata.factionData;
			playerfactiondata.increasePoints(playerdata.player, factionId, points);
			sender.sendMessage(new TextComponentString(points + " points added to player \""+playerdata.playername+"\" Faction ID: "+factionId));
			playerdata.save(true);
		}
	}

	@SubCommand(desc = "Drop relationship")
	public void drop(MinecraftServer server, ICommandSender sender, String[] args) {
		for (PlayerData playerdata : this.data) {
			playerdata.factionData.factionData.remove(selectedFaction.id);
			sender.sendMessage(new TextComponentString("Player \""+playerdata.playername+"\" has Faction ID: "+selectedFaction.id+" removed"));
			playerdata.save(true);
		}
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args == null) { return; }
		String playername = args[0];
		String factionName = args[1];
		this.data = PlayerDataController.instance.getPlayersData(sender, playername);
		if (this.data.isEmpty()) {
			throw new CommandException("Unknown player " + playername);
		}
		try {
			this.selectedFaction = FactionController.instance.getFaction(Integer.parseInt(factionName));
		} catch (NumberFormatException e) {
			this.selectedFaction = FactionController.instance.getFactionFromName(factionName);
		}
		if (this.selectedFaction == null) {
			throw new CommandException("Unknown faction " + factionName);
		}
		this.executeSub(server, sender, args[2], Arrays.copyOfRange(args, 3, args.length));
	}

	@Override
	public String getDescription() {
		return "Faction operations";
	}

	@Nonnull
	public String getName() {
		return "faction";
	}

	public @Nonnull List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender par1, @Nonnull String[] args, BlockPos pos) {
		if (args.length == 3) {
			return CommandBase.getListOfStringsMatchingLastWord(args, "add", "subtract", "set", "reset", "drop", "create");
		}
		return new ArrayList<>();
	}

	@Override
	public String getUsage() {
		return "<player> <faction> <command>";
	}

	@SubCommand(desc = "Reset points to default")
	public void reset(MinecraftServer server, ICommandSender sender, String[] args) {
		for (PlayerData playerdata : this.data) {
			playerdata.factionData.factionData.put(this.selectedFaction.id, this.selectedFaction.defaultPoints);
			playerdata.save(true);
		}
	}

	@Override
	public boolean runSubCommands() {
		return false;
	}

	@SubCommand(desc = "Set points", usage = "<points>")
	public void set(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		int points;
		try {
			points = Integer.parseInt(args[0]);
		} catch (NumberFormatException ex) {
			throw new CommandException(args[0]+" - must be an integer");
		}
		for (PlayerData playerdata : this.data) {
			PlayerFactionData playerfactiondata = playerdata.factionData;
			playerfactiondata.factionData.put(this.selectedFaction.id, points);
			playerdata.save(true);
		}
	}

	@SuppressWarnings("all")
	@SubCommand(desc = "Subtract points", usage = "<points>")
	public void subtract(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		int points;
		try {
			points = Integer.parseInt(args[0]);
		} catch (NumberFormatException ex) {
			throw new CommandException("Must be an integer");
		}
		int factionId = this.selectedFaction.id;
		for (PlayerData playerdata : this.data) {
			PlayerFactionData playerfactiondata = playerdata.factionData;
			playerfactiondata.increasePoints(playerdata.player, factionId, -points);
			playerdata.save(true);
		}
	}

}
