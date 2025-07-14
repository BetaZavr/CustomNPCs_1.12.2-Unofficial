package noppes.npcs.command;

import java.util.*;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.dimensions.DimensionHandler;

import javax.annotation.Nonnull;

public class CmdDimensions extends CommandNoppesBase {

	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getDescription() {
		return "World operations";
	}

	@Nonnull
	@Override
	public String getName() {
		return "world";
	}

	public @Nonnull List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, BlockPos pos) {
		List<String> list = new ArrayList<>();
		if (args.length == 3) {
			Set<Integer> s = new TreeSet<>();
			for (int id : DimensionManager.getIDs()) {
				if (DimensionHandler.getInstance().isDelete(id)) {
					continue;
				}
				s.add(id);
			}
            s.add(-1);
            s.add(1);
			for (int id : s) {
				list.add("" + id);
			}
		} else if (args.length >= 4 && args.length <= 6) {
			list.add("~");
		}
		return list;
	}

	@SubCommand(desc = "Set spawn block in dimension", permission = 2)
	public void setspawn(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender == null) {
			return;
		}
		int id = sender.getEntityWorld().provider.getDimension(), x = sender.getPosition().getX(),
				y = sender.getPosition().getY() + 1, z = sender.getPosition().getZ();
		if (args.length == 3) {
			try {
				double dx = parseCoordinate(sender.getPosition().getX(), args[0], true).getResult();
				double dy = parseCoordinate(sender.getPosition().getY(), args[1], 0, 255, false).getResult();
				double dz = parseCoordinate(sender.getPosition().getZ(), args[2], true).getResult();
				x = (int) dx;
				y = (int) dy;
				z = (int) dz;
			} catch (NumberFormatException e) { LogWriter.error("Error:", e); }
		}
		if (args.length == 4) {
			try {
				id = Integer.parseInt(args[0]);
				double dx = parseCoordinate(sender.getPosition().getX(), args[1], true).getResult();
				double dy = parseCoordinate(sender.getPosition().getY(), args[2], 0, 255, false).getResult();
				double dz = parseCoordinate(sender.getPosition().getZ(), args[3], true).getResult();
				x = (int) dx;
				y = (int) dy;
				z = (int) dz;
			} catch (NumberFormatException e) { LogWriter.error("Error:", e); }
		}
		if (!DimensionManager.isDimensionRegistered(id)) {
			throw new CommandException("DimensionID: " + id + " - not found");
		}
		BlockPos pos = new BlockPos(x, y, z);
		DimensionManager.getProvider(id).setSpawnPoint(pos);
		DimensionManager.getWorld(id).setSpawnPoint(pos);
		server.getPlayerList().sendPacketToAllPlayers(new SPacketSpawnPosition(pos));
		sender.sendMessage(
				new TextComponentString("Set new spawn pos: [" + x + ", " + y + ", " + z + "] in dimension ID: " + id));
	}

	@SubCommand(desc = "Transfer player to dimension", usage = "<player> <dimensionID>", permission = 2)
	public void tp(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		EntityPlayerMP player = CommandBase.getPlayer(server, sender, args[0]);
		int id;
		try {
			id = Integer.parseInt(args[1]);
			if (!DimensionManager.isDimensionRegistered(id) || DimensionHandler.getInstance().isDelete(id)) {
				throw new CommandException("DimensionID: " + id + " - not found");
			}
		} catch (NumberFormatException ex) {
			throw new CommandException("DimensionID \"" + args[1]+"\" - must be an integer");
		}

		WorldServer world = Objects.requireNonNull(sender.getServer()).getWorld(id);
		BlockPos coords = world.getSpawnCoordinate();
		double x, y, z;
		if (coords == null) {
			coords = world.getSpawnPoint();
		}
        if (!world.isAirBlock(coords)) {
            coords = world.getTopSolidOrLiquidBlock(coords);
        } else if (!world.isAirBlock(coords.up())) {
            while (world.isAirBlock(coords) && coords.getY() > 0) {
                coords = coords.down();
            }
            if (coords.getY() == 0) {
                coords = world.getTopSolidOrLiquidBlock(coords);
            }
        }
        x = coords.getX();
        y = coords.getY();
        z = coords.getZ();
        if (args.length == 5) {
			try {
				double dx = parseCoordinate(sender.getPosition().getX(), args[2], true).getResult();
				double dy = parseCoordinate(sender.getPosition().getY(), args[3], 0, 255, false).getResult();
				double dz = parseCoordinate(sender.getPosition().getZ(), args[4], true).getResult();
				x = dx;
				y = dy;
				z = dz;
			} catch (NumberFormatException e) { LogWriter.error("Error:", e); }
		}
		NoppesUtilPlayer.teleportPlayer(player, x, y, z, id, player.rotationYaw, player.rotationPitch);
	}

}
