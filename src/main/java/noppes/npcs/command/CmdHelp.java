package noppes.npcs.command;

import java.lang.reflect.Method;
import java.util.Map;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.CommandNoppesBase;

import javax.annotation.Nonnull;

public class CmdHelp extends CommandNoppesBase {

	private final CommandNoppes parent;

	public CmdHelp(CommandNoppes parent) {
		this.parent = parent;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		int per = this.getPermissionLevel(server, sender);
        if (args == null || args.length == 0) {
			this.sendMessage(sender, "------Noppes Commands------");
			for (Map.Entry<String, CommandNoppesBase> entry : this.parent.map.entrySet()) {
				if (entry.getValue().getRequiredPermissionLevel() > per) {
					if (per != 0) { this.sendMessage(sender, ((char) 167) + "c" + entry.getKey() + ((char) 167) + "7: " + entry.getValue().getUsage(sender)); }
					continue;
				}
				this.sendMessage(sender, ((char) 167) + "6" + entry.getKey() + ((char) 167) + "r: " + entry.getValue().getUsage(sender));
			}
			return;
		}
		CommandNoppesBase command = this.parent.getCommand(args);
		if (command == null) {
			throw new CommandException("Unknown command " + args[0]);
		}
		if (command.getRequiredPermissionLevel() > per) {
			throw new CommandException("You are not allowed to use \""+command.getName().toLowerCase()+"\" command");
		}
		if (command.subcommands.isEmpty()) {
			sender.sendMessage(new TextComponentTranslation(command.getUsage(sender)));
			return;
		}
		Method m = null;
		if (args.length > 1) {
			m = command.subcommands.get(args[1].toLowerCase());
		}
		if (m == null) {
			this.sendMessage(sender, "------" + command.getName() + " SubCommands------");
			for (Map.Entry<String, Method> entry2 : command.subcommands.entrySet()) {
				SubCommand sc = entry2.getValue().getAnnotation(SubCommand.class);
				if (sc == null || sc.permission() > per) { continue; }
				sender.sendMessage(new TextComponentTranslation(((char) 167) + "e" + entry2.getKey() + ((char) 167) + "r: " + sc.desc()));
			}
		}
		else {
			SubCommand sc = m.getAnnotation(SubCommand.class);
			if (sc == null || sc.permission() > per) {
				throw new CommandException("You are not allowed to use \""+command.getName().toLowerCase()+"\" command");
			}
			this.sendMessage(sender, "------" + command.getName() + "." + args[1].toLowerCase() + " Command------");
			sender.sendMessage(new TextComponentTranslation(sc.desc()));
			if (!sc.usage().isEmpty()) {
				sender.sendMessage(new TextComponentTranslation("Usage: " + sc.usage()));
			}
		}
	}

	@Override
	public String getDescription() {
		return "help [command]";
	}

	@Nonnull
	public String getName() {
		return "help";
	}
}
