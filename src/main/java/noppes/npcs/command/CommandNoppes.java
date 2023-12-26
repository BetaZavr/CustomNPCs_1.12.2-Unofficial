package noppes.npcs.command;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.api.CustomNPCsException;

public class CommandNoppes
extends CommandBase {
	
	public CmdHelp help;
	public Map<String, CommandNoppesBase> map;

	public CommandNoppes() {
		this.map = new HashMap<String, CommandNoppesBase>();
		this.help = new CmdHelp(this);
		this.registerCommand(this.help);
		this.registerCommand(new CmdScript());
		this.registerCommand(new CmdScene());
		this.registerCommand(new CmdSlay());
		this.registerCommand(new CmdQuest());
		this.registerCommand(new CmdDialog());
		this.registerCommand(new CmdSchematics());
		this.registerCommand(new CmdFaction());
		this.registerCommand(new CmdNPC());
		this.registerCommand(new CmdClone());
		this.registerCommand(new CmdConfig());
		this.registerCommand(new CmdMark());
		this.registerCommand(new CmdDimensions());
		this.registerCommand(new CmdPlayers());
		this.registerCommand(new CmdDebug());
	}

	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) {
			this.help.execute(server, sender, args);
			return;
		}
		CommandNoppesBase command = this.getCommand(args);
		if (command == null) {
			throw new CommandException("Unknown command " + args[0], new Object[0]);
		}
		args = Arrays.copyOfRange(args, 1, args.length);
		if (command.subcommands.isEmpty() || !command.runSubCommands()) {
			if (!sender.canUseCommand(command.getRequiredPermissionLevel(),
					"commands.noppes." + command.getName().toLowerCase())) {
				throw new CommandException("You are not allowed to use this command", new Object[0]);
			}
			command.canRun(server, sender, command.getUsage(), args);
			command.execute(server, sender, args);
		} else {
			if (args.length == 0) {
				this.help.execute(server, sender, new String[] { command.getName() });
				return;
			}
			command.executeSub(server, sender, args[0], Arrays.copyOfRange(args, 1, args.length));
		}
	}

	public CommandNoppesBase getCommand(String[] args) {
		if (args.length == 0) {
			return null;
		}
		return this.map.get(args[0].toLowerCase());
	}

	public String getName() {
		return "noppes";
	}

	public int getRequiredPermissionLevel() {
		return 2;
	}

	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		if (args.length == 1) {
			return CommandBase.getListOfStringsMatchingLastWord(args, (Collection<String>) this.map.keySet());
		}
		CommandNoppesBase command = this.getCommand(args);
		if (command == null) {
			return null;
		}
		if (args.length == 2 && command.runSubCommands()) {
			return CommandBase.getListOfStringsMatchingLastWord(args, (Collection<?>) command.subcommands.keySet());
		}
		String[] useArgs = command.getUsage().split(" ");
		if (command.runSubCommands()) {
			Method m = command.subcommands.get(args[1].toLowerCase());
			if (m != null) {
				useArgs = m.getAnnotation(CommandNoppesBase.SubCommand.class).usage().split(" ");
			}
		}
		if (useArgs.length - 3 > -1 && useArgs.length >= args.length - 3) {
			String usage = useArgs[args.length - 3];
			if (usage.equals("<player>") || usage.equals("[player]")) {
				return CommandBase.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
			}
		}
		return command.getTabCompletions(server, sender, (String[]) Arrays.copyOfRange(args, 1, args.length), pos);
	}

	public String getUsage(ICommandSender sender) {
		return "Use as /noppes subcommand";
	}

	public void registerCommand(CommandNoppesBase command) {
		String name = command.getName().toLowerCase();
		if (this.map.containsKey(name)) {
			throw new CustomNPCsException("Already a subcommand with the name: " + name, new Object[0]);
		}
		this.map.put(name, command);
	}
}
