package noppes.npcs.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.*;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.LogWriter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CommandNoppesBase extends CommandBase {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.METHOD })
	public @interface SubCommand {
		String desc();

		String name() default "";

		int permission() default 0;

		String usage() default "";
	}

	public Map<String, Method> subcommands;

	public CommandNoppesBase() {
		subcommands = new HashMap<>();
		for (Method m : getClass().getDeclaredMethods()) {
			SubCommand sc = m.getAnnotation(SubCommand.class);
			if (sc != null) {
				String name = sc.name();
				if (name.isEmpty()) { name = m.getName().toLowerCase(); }
				subcommands.put(name, m);
			}
		}
	}

	public void canRun(MinecraftServer server, ICommandSender sender, String usage, String[] args) throws CommandException {
		String[] np = usage.split(" ");
		List<String> required = new ArrayList<>();
		for (int i = 0; i < np.length; ++i) {
			String command = np[i];
			if (command.startsWith("<")) { required.add(command); }
			if (command.equals("<player>") && args.length > i) { CommandBase.getPlayer(server, sender, args[i]); }
		}
		if (args.length < required.size()) {
			throw new CommandException("Missing parameter: " + required.get(args.length));
		}
	}

	public void execute(@Nullable MinecraftServer server, @Nullable ICommandSender sender, @Nullable String[] args) throws CommandException {
	}

	public void executeSub(MinecraftServer server, ICommandSender sender, String command, String[] args) throws CommandException {
		Method m = subcommands.get(command.toLowerCase());
		if (m == null) {
			throw new CommandException("Unknown subcommand " + command);
		}
		SubCommand sc = m.getAnnotation(SubCommand.class);
		if (sc.permission() > getPermissionLevel(server, sender)) {
			LogWriter.debug("TEST: "+sc.permission()+" / "+getPermissionLevel(server, sender));
			throw new CommandException("You are not allowed to use \""+Objects.requireNonNull(getName()).toLowerCase() + "." + command.toLowerCase()+"\" command");
		}
		canRun(server, sender, sc.usage(), args);
		try {
			m.invoke(this, server, sender, args);
		} catch (Exception e) {
			if (e.getCause() instanceof CommandException) {
				throw (CommandException) e.getCause();
			}
			LogWriter.error("Error:", e);
		}
	}

	public abstract String getDescription();

	public abstract @Nonnull String getName();

	public int getRequiredPermissionLevel() {
		return 0;
	}

	public String getUsage() {
		return "";
	}

	public @Nonnull String getUsage(@Nullable ICommandSender sender) {
		return getDescription();
	}

	public boolean runSubCommands() {
		return !subcommands.isEmpty();
	}

	protected void sendMessage(ICommandSender sender, String message, Object... obs) {
		sender.sendMessage(new TextComponentTranslation(message, obs));
	}

	@Override
	public boolean checkPermission(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender) {
		int thisPer = getRequiredPermissionLevel();
		if (sender instanceof EntityPlayerMP) {
			return thisPer <= getPermissionLevel(server, sender);
		}
		return sender.canUseCommand(thisPer, getName());
	}

	@SuppressWarnings("all")
	protected int getPermissionLevel(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender) {
		int per = 4;
		if (sender instanceof EntityPlayerMP) {
			per = 0;
			UserListOpsEntry util = server.getPlayerList().getOppedPlayers().getEntry(((EntityPlayerMP) sender).getGameProfile());
			if (util != null) { per = util.getPermissionLevel(); }
			if (((EntityPlayerMP) sender).isCreative()) { per = Math.max(server.isSinglePlayer() ? 4 : 2, per); }
		}
		return per;
	}


}
