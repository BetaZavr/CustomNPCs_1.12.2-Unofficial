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
        if (args.length == 0) {
			this.sendMessage(sender, "------Noppes Commands------");
			for (Map.Entry<String, CommandNoppesBase> entry : this.parent.map.entrySet()) {
				this.sendMessage(sender, entry.getKey() + ": " + entry.getValue().getUsage(sender));
			}
			return;
		}
		CommandNoppesBase command = this.parent.getCommand(args);
		if (command == null) {
			throw new CommandException("Unknown command " + args[0]);
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
				sender.sendMessage(new TextComponentTranslation(entry2.getKey() + ": " + entry2.getValue().getAnnotation(SubCommand.class).desc()));
			}
		} else {
			this.sendMessage(sender, "------" + command.getName() + "." + args[1].toLowerCase() + " Command------");
			SubCommand sc = m.getAnnotation(SubCommand.class);
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
