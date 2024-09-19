package noppes.npcs.command;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.api.CustomNPCsException;

import javax.annotation.Nonnull;

public class CommandNoppes extends CommandBase {

	public CmdHelp help;
	public Map<String, CommandNoppesBase> map;

	public CommandNoppes() {
		this.map = new HashMap<>();
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
		this.registerCommand(new CmdAccepts());
	}

	@Override
	public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
		if (args.length == 0) {
			this.help.execute(server, sender, args);
			return;
		}
		CommandNoppesBase command = this.getCommand(args);
		if (command == null) {
			throw new CommandException("Unknown command " + args[0]);
		}
		args = Arrays.copyOfRange(args, 1, args.length);
		if (command.subcommands.isEmpty() || !command.runSubCommands()) {
			if (command.getRequiredPermissionLevel() > this.getPermissionLevel(server, sender)) {
				throw new CommandException("You are not allowed to use \""+command.getName().toLowerCase()+"\" command");
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

	@Override
	public @Nonnull String getName() {
		return "noppes";
	}

	@Override
	public int getRequiredPermissionLevel()
	{
		return 0;
	}

	@Override
	public @Nonnull List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, BlockPos pos) {
		if (args.length == 1) {
			int per = this.getPermissionLevel(server, sender);
			List<String> list = Lists.newArrayList();
			for (Map.Entry<String, CommandNoppesBase> entry : this.map.entrySet()) {
				if (entry.getValue().getRequiredPermissionLevel() > per) { continue; }
				list.add(entry.getKey());
			}
			return list;
		}
		CommandNoppesBase command = this.getCommand(args);
		if (command == null) {
			return Lists.newArrayList();
		}
		if (args.length == 2 && command.runSubCommands()) {
			return CommandBase.getListOfStringsMatchingLastWord(args, command.subcommands.keySet());
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
		return command.getTabCompletions(server, sender, Arrays.copyOfRange(args, 1, args.length), pos);
	}

	@Override
	public @Nonnull String getUsage(@Nonnull ICommandSender sender) {
		return "Use as /noppes subcommand";
	}

	public void registerCommand(CommandNoppesBase command) {
		String name = command.getName().toLowerCase();
		if (this.map.containsKey(name)) { throw new CustomNPCsException("Already a subcommand with the name: " + name); }
		this.map.put(name, command);
	}

	@Override
	public boolean checkPermission(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender) {
		int thisPer = this.getRequiredPermissionLevel();
		if (sender instanceof EntityPlayerMP) {
			return thisPer <= this.getPermissionLevel(server, sender);
		}
		return sender.canUseCommand(thisPer, this.getName());
	}

	protected int getPermissionLevel(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender) {
		int per = 4;
		if (sender instanceof EntityPlayerMP) {
			per = 0;
			if (((EntityPlayerMP) sender).isCreative()) { per = server.isSinglePlayer() ? 4 : 2; }
			else {
				UserListOpsEntry util = server.getPlayerList().getOppedPlayers().getEntry(((EntityPlayerMP) sender).getGameProfile());
				if (util != null) { per = util.getPermissionLevel(); }
			}
		}
		return per;
	}

}
