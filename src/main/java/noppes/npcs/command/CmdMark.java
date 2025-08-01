package noppes.npcs.command;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.LogWriter;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.controllers.data.MarkData;

import javax.annotation.Nonnull;

public class CmdMark extends CommandNoppesBase {

	public int getRequiredPermissionLevel() {
		return 2;
	}

	@SubCommand(desc = "Clear mark", usage = "<@e>", permission = 2)
	public void clear(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		List<Entity> list = getEntityList(server, sender, args[0]);
		for (Entity e : list) {
			if (!(e instanceof EntityLivingBase)) {
				continue;
			}
			MarkData data = MarkData.get((EntityLivingBase) e);
			data.marks.clear();
			data.syncClients();
		}
	}

	@Override
	public String getDescription() {
		return "Mark operations";
	}

	@Nonnull
	public String getName() {
		return "mark";
	}

	@SubCommand(desc = "Set mark (warning overrides existing marks)", usage = "<@e> <type> [color]", permission = 2)
	public void set(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		List<Entity> list = getEntityList(server, sender, args[0]);
		int type = 0;
		try {
			type = Integer.parseInt(args[1]);
		} catch (Exception e) { LogWriter.error(e); }
		int color = 16777215;
		if (args.length > 2) {
			try {
				color = Integer.parseInt(args[2], 16);
			} catch (Exception e) { LogWriter.error(e); }
		}
		for (Entity e : list) {
			if (!(e instanceof EntityLivingBase)) {
				continue;
			}
			MarkData data = MarkData.get((EntityLivingBase) e);
			data.marks.clear();
			data.addMark(type, color);
		}
	}
}
