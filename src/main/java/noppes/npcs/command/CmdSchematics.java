package noppes.npcs.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.LogWriter;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.schematics.SchematicWrapper;

import javax.annotation.Nonnull;

public class CmdSchematics extends CommandNoppesBase {

	public int getRequiredPermissionLevel() {
		return 2;
	}

	@SubCommand(desc = "Build the schematic", usage = "<name> [rotation] [[world:]x,y,z]]", permission = 2)
	public void build(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String name = args[0];
		SchematicWrapper schem = SchematicController.Instance.load(name);
		if (schem == null) {
			throw new CommandException("Unknown schematic: " + name);
		}
		this.sendMessage(sender, "width: " + schem.schema.getWidth() + ", length: " + schem.schema.getLength() + ", height: " + schem.schema.getHeight());
		BlockPos pos = sender.getPosition();
		World world = sender.getEntityWorld();
		int rotation = 0;
		if (args.length > 1) {
			try {
				rotation = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) { LogWriter.error("Error:", e); }
		}
		if (args.length > 2) {
			String location = args[2];
			if (location.contains(":")) {
				String[] par = location.split(":");
				location = par[1];
				world = this.getWorld(server, par[0]);
				if (world == null) {
					throw new CommandException("\""+par[0]+"\" is an unknown world");
				}
			}
			if (location.contains(",")) {
				String[] par = location.split(",");
				if (par.length != 3) {
					throw new CommandException("Location should be x,y,z. Length: "+par.length);
				}
				try {
					pos = CommandBase.parseBlockPos(sender, par, 0, false);
				} catch (NumberInvalidException e) {
					throw new CommandException("Location should be in numbers " + location);
				}
			}
		}
		if (pos.getX() == 0 && pos.getY() == 0 && pos.getZ() == 0) {
			throw new CommandException("Location needed - " + pos);
		}
		schem.init(pos, world, rotation);
		SchematicController.Instance.build(schem, sender);
	}

	@Override
	public String getDescription() {
		return "Schematic operation";
	}

	@Nonnull
	public String getName() {
		return "schema";
	}

	public @Nonnull List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender par1, @Nonnull String[] args, BlockPos pos) {
		if (args[0].equalsIgnoreCase("build") && args.length == 2) {
			List<String> list = SchematicController.Instance.list();
			return CommandBase.getListOfStringsMatchingLastWord(args, list.toArray(new String[0]));
		}
		return new ArrayList<>();
	}

	public World getWorld(MinecraftServer server, String t) {
		for (WorldServer w : server.worlds) {
			if (w != null && (w.provider.getDimension() + "").equalsIgnoreCase(t)) {
				return w;
			}
		}
		return null;
	}

	@SubCommand(desc = "Gives info about the current build", permission = 2)
	public void info(MinecraftServer server, ICommandSender sender, String[] args) {
		SchematicController.Instance.info(sender);
	}

	@SubCommand(desc = "Lists available schematics", permission = 2)
	public void list(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		List<String> list = SchematicController.Instance.list();
		if (list.isEmpty()) {
			throw new CommandException("No available schematics " + list);
		}
		StringBuilder s = new StringBuilder();
		for (String file : list) {
			s.append(file).append(", ");
		}
		this.sendMessage(sender, s.toString());
	}

	@SubCommand(desc = "Stops the current build", permission = 2)
	public void stop(MinecraftServer server, ICommandSender sender, String[] args) {
		SchematicController.Instance.stop(sender);
	}
}
